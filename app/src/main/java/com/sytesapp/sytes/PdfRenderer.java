package com.sytesapp.sytes;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebView;

public class PdfRenderer extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView pdfViewer = new WebView(this);
        setContentView(pdfViewer);
        pdfViewer.getSettings().setJavaScriptEnabled(true);
        pdfViewer.loadUrl("https://docs.google.com/gview?embedded=true&url=" + getIntent().getStringExtra("link"));
    }
}
