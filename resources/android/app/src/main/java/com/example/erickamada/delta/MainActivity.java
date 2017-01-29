package com.example.erickamada.delta;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    private Button mPlayerButton;
    private Button mRecordButton;

    public static String Data="Data";

    public static int cal =0;
    public static int fat;
    public static int chol;
    public static int sodium;
    public static int carb;
    public static int pro;

    public static int reqcal =0;
    public static int reqfat;
    public static int reqchol;
    public static int reqsodium;
    public static int reqcarb;
    public static int reqpro;


    boolean mStartRecording = true;
    boolean mStartPlaying = true;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }

    private void onRecord(boolean start) throws IOException {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mStartRecording = true;
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() throws IOException {
        mStartRecording = false;
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
        System.out.println("pls work");

        //sendAudioStream();
        URL url = new URL("http://172.17.42.9:4444/uploadFile");
        new DownloadFilesTask().execute(url);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);

        mRecordButton = (Button) findViewById(R.id.record_button);
        mPlayerButton = (Button) findViewById(R.id.play_button);

        // Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/audiorecordtest.m4a";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        mRecordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    onRecord(mStartRecording);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (mStartRecording) {
                    ((Button) v).setText("Stop recording");
                } else {
                    ((Button) v).setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        });
        mPlayerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    ((Button) v).setText("Stop playing");
                    System.out.println("pls work");
                } else {
                    ((Button) v).setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private File audioFile() {
        return null; // TODO
    }

/*    private void sendAudioStream() throws IOException {
        URL url = new URL("http://172.17.42.9:4444/uploadFile");

        InputStream audioInputStream = new FileInputStream(new File(mFileName));
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try {
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            System.out.println("pls work2");
            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            System.out.println("pls work2.5");
            copyStream(audioInputStream,out);
            System.out.println("pls work3");
            //InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            //readStream(in); TODO read response.
        } finally {
            System.out.println("pls work4");
            urlConnection.disconnect();
        }

    }*/

    /*private static void copyStream(InputStream input, OutputStream output)
            throws IOException
    {
        System.out.println("pls 1");
        byte[] buffer = new byte[1024]; // Adjust if you want
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, bytesRead);
        }
        System.out.println("pls 2");
    }*/

    class DownloadFilesTask extends AsyncTask<URL, Integer, String> {
        protected String doInBackground(URL... urls) {
            URL url;
            InputStream audioInputStream;
            HttpURLConnection urlConnection = null;

            try {

                url = new URL("http://172.17.45.153:4444/uploadFile");
                audioInputStream = new FileInputStream(new File(mFileName));
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                System.out.println("pls work2");
                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                System.out.println("pls work2.5");

                copyStream(audioInputStream, out);
                out.flush();
                out.close();
                System.out.println("pls work3");
                //InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                //OutputStream os = new ByteArrayOutputStream();
                /*try {
                    copyStream(in, os);
                    return  os.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/


            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("pls work4");
                if (urlConnection != null) {
                    urlConnection.disconnect();
                    try {
                        return inputread();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println("result" + result);
            try {
                JSONObject jobject =new JSONObject(result);
                System.out.println(jobject.get("name"));
                JSONObject dataobj = jobject.getJSONObject("data");

                cal=dataobj.getInt("calories");
                fat=dataobj.getInt("fat");
                chol=dataobj.getInt("cholesterol");
                sodium=dataobj.getInt("sodium");
                carb=dataobj.getInt("carbohydrates");
                pro=dataobj.getInt("protein");
                System.out.println(pro);
                JSONObject goalobj = jobject.getJSONObject("goal");

                reqcal=goalobj.getInt("calories");
                reqfat=goalobj.getInt("fat");
                reqchol=goalobj.getInt("cholesterol");
                reqsodium=goalobj.getInt("sodium");
                reqcarb=goalobj.getInt("carbohydrates");
                reqpro=goalobj.getInt("protein");



            } catch (JSONException e) {
                e.printStackTrace();
            }



            Intent intent = new Intent(MainActivity.this, deltaMessage.class);
            intent.putExtra("DATA", result);
            startActivity(intent);

        }

        protected void copyStream(InputStream input, OutputStream output)
                throws IOException {
            System.out.println("pls 1");
            byte[] buffer = new byte[1024]; // Adjust if you want
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                //Log.d("BYTES", Arrays.toString(buffer));
            }
            System.out.println("pls 2");
        }

        private String inputread() throws IOException {


            URL furl = new URL("http://172.17.45.153:4444/getUser");
            HttpURLConnection furlConnection = (HttpURLConnection) furl.openConnection();
            try

            {
                InputStream ig = new BufferedInputStream(furlConnection.getInputStream());
                return readStream(ig);
            }

            finally

            {
                furlConnection.disconnect();
            }

            }

        private String readStream(InputStream in) {
            try {
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                int i = in.read();
                while(i != -1) {
                    bo.write(i);
                    i = in.read();
                }
                return bo.toString();
            } catch (IOException e) {
                return "";
            }
        }


    }


}



