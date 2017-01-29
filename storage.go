package main

import (
	"io"
	"log"
	"os"
	"time"

	"cloud.google.com/go/storage"
	"golang.org/x/net/context"
)

type ObjectAttrsToUpdate struct {
	ContentType        string
	ContentLanguage    string
	ContentEncoding    string
	ContentDisposition string
	CacheControl       string
	Metadata           map[string]string // set to map[string]string{} to delete
}

func saveFile(b []byte, fileName string) error {
	myFile, err := os.Create(fileName)
	defer myFile.Close()
	if err != nil {
		log.Println("save file err 1:", err)
	}

	_, err = myFile.Write(b)
	if err != nil {
		log.Println("save file err 2:", err)
	}

	return err
}

func mainStorage(fileName string) error {
	bktName := "deltafoods"
	objName := time.Now().Format("20060102150405") + ".flac"

	// creates client
	ctx := context.Background()
	client, err := storage.NewClient(ctx)
	if err != nil {
		log.Println("main storage err 1:", err)
	}

	// save file
	f, err := os.Open(fileName)
	defer f.Close()
	if err != nil {
		log.Println("main storage err 2:", err)
	}

	// initial variables
	bkt := client.Bucket(bktName)
	obj := bkt.Object(objName)
	wc := obj.NewWriter(ctx)

	// copy file contents to writer
	if _, err = io.Copy(wc, f); err != nil {
		log.Println("main storage err 3:", err)
	}

	// close writer
	if err := wc.Close(); err != nil {
		log.Println("main storage err 4:", err)
	}

	// make file public
	if err := makePublic(client, bktName, objName); err != nil {
		log.Println("main storage err 5:", err)
	}

	// get attributes
	objAttrs, err := client.Bucket(bktName).Object(objName).Attrs(ctx)
	if err != nil {
		log.Println("main storage err 6:", err)
	}
	log.Println(objAttrs)

	// update attributes
	objAttrs, err = obj.Update(ctx, storage.ObjectAttrsToUpdate{
		ContentType:     "application/octet-stream",
		ContentEncoding: "FLAC",
	})
	log.Println(objAttrs)

	return err
}

func makePublic(client *storage.Client, bucket, object string) error {
	ctx := context.Background()

	acl := client.Bucket(bucket).Object(object).ACL()

	return acl.Set(ctx, storage.AllUsers, storage.RoleReader)
}
