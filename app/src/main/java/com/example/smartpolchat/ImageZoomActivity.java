package com.example.smartpolchat;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;

public class ImageZoomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_zoom);

        String imageName = getIntent().getStringExtra("imageName");
        PhotoView photoView = findViewById(R.id.photo_view);

        if (imageName != null) {
            int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
            photoView.setImageResource(resId);
        }
    }
}