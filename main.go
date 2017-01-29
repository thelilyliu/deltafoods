package main

import (
	"bytes"
	"encoding/json"
	"html/template"
	"io/ioutil"
	"log"
	"net/http"
	"os/exec"
	"path"
	"strings"
	"time"

	"github.com/husobee/vestigo"
)

type Page struct {
	Viewer string
}

type Error struct {
	Code    int    `json:"code"`
	Message string `json:"message"`
}

const (
	errorStatusCode = 555
	serverName      = "GWS"
	userName        = "User"
)

func main() {
	router := vestigo.NewRouter()

	// set up router global CORS policy
	router.SetGlobalCors(&vestigo.CorsAccessControl{
		AllowOrigin:      []string{"*"},
		AllowCredentials: false,
		MaxAge:           3600 * time.Second,
	})

	fileServerResources := http.FileServer(http.Dir("resources"))
	router.Get("/resources/*", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Vary", "Accept-Encoding")
		w.Header().Set("Cache-Control", "public, max-age=86400")
		w.Header().Set("Server", serverName)
		r.URL.Path = strings.TrimPrefix(r.URL.Path, "/resources")
		fileServerResources.ServeHTTP(w, r)
	})

	router.Post("/updateData", updateData)
	router.Post("/uploadFile", uploadFile)
	router.Get("/getUser", getUser)
	router.Get("/", viewIndex)

	log.Println("Listening...")
	if err := http.ListenAndServe(":4444", router); err != nil {
		log.Println(err)
	}
}

/*
  ========================================
  Pages
  ========================================
*/

func viewIndex(w http.ResponseWriter, r *http.Request) {
	log.Println("=== view index ===")
	returnCode := 0

	setHeader(w)
	var page Page

	layout := path.Join("resources/html", "index.html")
	content := path.Join("resources/html", "content.html")

	t, err := template.ParseFiles(layout, content)
	if err != nil {
		returnCode = 1
		log.Println("view index err 1:", err)
	}

	if returnCode == 0 {
		if err := t.ExecuteTemplate(w, "my-template", page); err != nil {
			returnCode = 2
			log.Println("view index err 2:", err)
		}
	}

	// error handling
	if returnCode != 0 {
		handleError(returnCode, errorStatusCode, "Index page could not be viewed.", w)
	}
}

/*
  ========================================
  Get User
  ========================================
*/

func getUser(w http.ResponseWriter, r *http.Request) {
	log.Println("=== get user ===")
	returnCode := 0

	user := new(User)
	user.Name = userName

	if err := getUserDB(user); err != nil {
		returnCode = 1
		log.Println("get user err 1:", err)
	}

	if returnCode == 0 {
		if err := json.NewEncoder(w).Encode(user); err != nil {
			returnCode = 2
			log.Println("get user err 2:", err)
		}
	}

	// error handling
	if returnCode != 0 {
		handleError(returnCode, errorStatusCode, "User could not be gotten.", w)
	}
}

/*
  ========================================
  Upload File
  ========================================
*/

func uploadFile(w http.ResponseWriter, r *http.Request) {
	log.Println("=== upload file ===")
	returnCode := 0

	fileName := "inputfile.m4a"

	b, err := ioutil.ReadAll(r.Body)
	if err != nil {
		returnCode = 1
		log.Println("upload file err 1:", err)
	}

	if returnCode == 0 {
		if err := saveFile(b, fileName); err != nil {
			returnCode = 2
			log.Println("upload file err 2:", err)
		}
	}

	if returnCode == 0 {
		var out bytes.Buffer
		var stderr bytes.Buffer

		cmd := exec.Command("./script.sh")
		cmd.Stdout = &out
		cmd.Stderr = &stderr

		if err := cmd.Run(); err != nil {
			returnCode = 3
			log.Println("upload file err 3:", err)
		}
	}

	if returnCode == 0 {
		if err := mainStorage(fileName); err != nil {
			returnCode = 4
			log.Println("upload file err 4:", err)
		}
	}

	if returnCode == 0 {
		w.Write([]byte(fileName))
	}

	// error handling
	if returnCode != 0 {
		handleError(returnCode, errorStatusCode, "File could not be uploaded.", w)
	}
}

/*
  ========================================
  Update Data
  ========================================
*/

func updateData(w http.ResponseWriter, r *http.Request) {
	log.Println("=== update data ===")
	returnCode := 0

	user := new(User)
	user.Name = userName
	nutr := new(Nutrient)

	if err := json.NewDecoder(r.Body).Decode(nutr); err != nil {
		returnCode = 1
		log.Println("update data err 1:", err)
	}

	if returnCode == 0 {
		if err := updateDataDB(user, nutr); err != nil {
			returnCode = 2
			log.Println("update data err 2:", err)
		}
	}

	if returnCode == 0 {
		if err := json.NewEncoder(w).Encode(user); err != nil {
			returnCode = 3
			log.Println("update data err 3:", err)
		}
	}

	// error handling
	if returnCode != 0 {
		handleError(returnCode, errorStatusCode, "Data could not be updated.", w)
	}
}

/*
  ========================================
  Basics
  ========================================
*/

func setHeader(w http.ResponseWriter) {
	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	w.Header().Set("Cache-control", "no-cache, no-store, max-age=0, must-revalidate")
	w.Header().Set("Expires", "Fri, 01 Jan 1990 00:00:00 GMT")
	w.Header().Set("Server", serverName)
}

func handleError(returnCode, statusCode int, message string, w http.ResponseWriter) {
	error := new(Error)
	error.Code = returnCode
	error.Message = message

	w.WriteHeader(statusCode)
	if err := json.NewEncoder(w).Encode(error); err != nil {
		log.Println(err)
	}
}
