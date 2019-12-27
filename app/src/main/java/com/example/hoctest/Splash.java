package com.example.hoctest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends AppCompatActivity {
    private static int WELCOME_TIME = 4000;

    private Thread mSplashThread;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Splash.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fadein,R.anim.fadeout);
                finish();
            }
        },WELCOME_TIME);

    }
}
