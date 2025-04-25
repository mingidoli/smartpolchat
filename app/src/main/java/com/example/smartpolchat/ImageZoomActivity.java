package com.example.smartpolchat;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.chrisbanes.photoview.PhotoView;
import android.widget.ImageView;

public class ImageZoomActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_zoom);

        PhotoView photoView = findViewById(R.id.zoomedImage);
        int imageResId = getIntent().getIntExtra("imageResId", -1);

        if (imageResId != -1) {
            photoView.setImageResource(imageResId);
        }
    }
}