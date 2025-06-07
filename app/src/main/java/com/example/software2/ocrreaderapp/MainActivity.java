package com.example.software2.ocrreaderapp;

import static android.Manifest.permission.CAMERA;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private SurfaceView surfaceView;

    private CameraSource cameraSource;
    private TextRecognizer textRecognizer;
    Button button;
    Button button1;
    Button button2;
    Button button3;

    private TextToSpeech textToSpeech;
    private String stringResult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button2 = findViewById(R.id.btn2);
        button3 = findViewById(R.id.btn3);
        button = findViewById(R.id.btn);
        button1 = findViewById(R.id.btn1);
        button1.setVisibility(View.GONE);
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PackageManager.PERMISSION_GRANTED);
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeech.setLanguage(Locale.US);

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraSource.release();
    }

    private void textRecognizer() {
        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setRequestedPreviewSize(1280, 1024)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView = findViewById(R.id.surfaceView);
        Context context = this;
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                try {

                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    cameraSource.start(surfaceView.getHolder());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
    }

    private void capture() {
        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(@NonNull Detector.Detections<TextBlock> detections) {

                SparseArray<TextBlock> sparseArray = detections.getDetectedItems();
                StringBuilder stringBuilder = new StringBuilder();

                for (int i = 0; i < sparseArray.size(); ++i) {
                    TextBlock textBlock = sparseArray.valueAt(i);
                    if (textBlock != null) {
                        textBlock.getValue();
                        stringBuilder.append(Arrays.toString(new String[]{textBlock.getValue()}));
                    }
                }

                final String stringText = stringBuilder.toString();

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        stringResult = stringText;
                        resultObtained();
                    }
                });
            }
        });
    }

    private void resultObtained() {
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        button1.setVisibility(View.VISIBLE);
        textView.setText(stringResult);
    }


    public void buttonStart(View view) {
        setContentView(R.layout.surface);
        Button capture = findViewById(R.id.capture);
        capture.setOnClickListener(v -> capture());
        textRecognizer();
    }


    public void createMyPDF(View view) {
        textToSpeech.speak("pdf is generated", TextToSpeech.QUEUE_FLUSH, null);
        Toast.makeText(getApplicationContext(), "PDF is generated", Toast.LENGTH_LONG).show();
        PdfDocument myPdfDocument = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(399, 660, 1).create();
        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);

        Paint myPaint = new Paint();
        String myString = textView.getText().toString();
        int x = 15, y = 40;

        for (String line : myString.split("\n")) {
            myPage.getCanvas().drawText(line, x, y, myPaint);
            y += myPaint.descent() - myPaint.ascent();
        }
        myPdfDocument.finishPage(myPage);
        String myFilePath = Environment.getExternalStorageDirectory().getPath() + "/myPDFFile.pdf";

        File myFile = new File(myFilePath);
        int i = 0;
        while (myFile.exists()) {
            i++;
            myFile = new File(Environment.getExternalStorageDirectory(), "myPDFFile(" + i + ").pdf");
        }
            try {
                myPdfDocument.writeTo(new FileOutputStream(myFile));
            } catch (Exception e) {
                e.printStackTrace();
                textView.setText("ERROR");
            }

            myPdfDocument.close();
        }


    public void listner(View view) {
        textToSpeech.speak(stringResult, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    public void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        super.onPause();
    }

    public void stop(View view) {
        textToSpeech.stop();
    }
}

