package com.example.smartpolchat;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.chrisbanes.photoview.PhotoView;

public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        PhotoView imageView = findViewById(R.id.fullscreenImageView);
        int resId = getIntent().getIntExtra("imageResId", -1);

        if (resId != -1) {
            imageView.setImageResource(resId);
        }

        imageView.setOnClickListener(v -> finish()); // 탭하면 뒤로가기
    }
}