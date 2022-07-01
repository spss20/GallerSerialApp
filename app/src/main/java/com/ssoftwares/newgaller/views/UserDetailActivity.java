package com.ssoftwares.newgaller.views;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ssoftwares.newgaller.R;
import com.ssoftwares.newgaller.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserDetailActivity extends AppCompatActivity {

    private EditText name;
    private EditText mobileNumber;
    private EditText employeeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        name = findViewById(R.id.user_name);
        mobileNumber = findViewById(R.id.user_mobile);
        employeeId = findViewById(R.id.user_employee_id);
        Button next = findViewById(R.id.next);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(getDrawable(R.drawable.ic_user));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Enter Details");

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

    }

    private void saveData(){
        boolean check = validateData();
        if (!check) {
            return;
        }
        SharedPreferences preferences = getSharedPreferences(Constants.prefPath, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String dataLength = String.format("%04X", Integer.parseInt(employeeId.getText().toString()));
        editor.putString("eId", dataLength);
        editor.putString("name" , name.getText().toString());
        editor.putString("mobile" , mobileNumber.getText().toString());
        editor.putBoolean("isFirstRun" , false);
        editor.apply();
        startActivity(new Intent(this , ConnectionActivity.class));
        finish();
    }

    private boolean validateData() {
       if (TextUtils.isEmpty(name.getText().toString())) {
            name.setError("Name is empty");
            return false;
        } else if (mobileNumber.getText().toString().isEmpty()) {
            mobileNumber.setError("Number is empty");
            return false;
        } else if (mobileNumber.getText().toString().length() != 10) {
            mobileNumber.setError("Mobile number incorrect");
            return false;
        } else if (employeeId.getText().toString().isEmpty()) {
            employeeId.setError("Employee ID empty");
            return false;
        } else if (Integer.parseInt(employeeId.getText().toString()) > 65025) {
            employeeId.setError("Employee ID incorrect");
            return false;
        }
        return true;
    }
}
