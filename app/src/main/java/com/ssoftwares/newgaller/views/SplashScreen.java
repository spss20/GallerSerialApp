package com.ssoftwares.newgaller.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ssoftwares.newgaller.R;
import com.ssoftwares.newgaller.utils.Constants;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = getSharedPreferences(Constants.prefPath, MODE_PRIVATE);
                boolean authenticated = preferences.getBoolean("authenticated" , true);
                if (authenticated) {
                    if (preferences.getBoolean("isFirstRun", true)) {
                        startActivity(new Intent(SplashScreen.this, UserDetailActivity.class));
                    } else {
                        startActivity(new Intent(SplashScreen.this, ConnectionActivity.class));
                    }
                    finish();
                } else {
                    Toast.makeText(SplashScreen.this, "You are not allowed to use this app." +
                            "Contact admin for support.", Toast.LENGTH_LONG).show();
                }
            }
        } , 1500);
        FirebaseFirestore.getInstance().collection("authentication")
                .document("hello")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        boolean isAuthenticated = documentSnapshot.getBoolean("surya");
                        SharedPreferences preferences = getSharedPreferences(Constants.prefPath, MODE_PRIVATE);
                        preferences.edit().putBoolean("authenticated" , isAuthenticated)
                                .apply();
                    }
                });
    }
}
