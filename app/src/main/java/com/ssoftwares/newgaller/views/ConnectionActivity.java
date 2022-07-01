package com.ssoftwares.newgaller.views;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ssoftwares.newgaller.R;
import com.ssoftwares.newgaller.UsbService;
import com.ssoftwares.newgaller.modals.CommandLog;
import com.ssoftwares.newgaller.utils.Commands;
import com.ssoftwares.newgaller.utils.Constants;
import com.ssoftwares.newgaller.utils.MyUtils;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ConnectionActivity extends BaseActivity {

    private static final String TAG = "ConnectionActivity";
    private String[] deviceArray = new String[]{"Not Selected", "DCEM", "RTU", "SNMP", "SAS"};
    private boolean isVerifying;
    private Uri uri;
    private TextView imageText;

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.log_menu , menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == R.id.view_logs){
//            startActivity(new Intent(this , LogActivity.class));
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        mHandler = new MyHandler(this);
        if (UsbService.IS_DATA_FETCHED) {
            UsbService.IS_DATA_FETCHED = false;
        }
//        getSupportActionBar().setTitle("Galler Serial App");
    }

    @Override
    public void readBuffer(Object object) {
        byte[] buffer = (byte[]) object;

        realm.beginTransaction();
        CommandLog log = new CommandLog(System.currentTimeMillis() ,
                "Response" ,MyUtils.hexToString(buffer));
        realm.copyToRealm(log);
        realm.commitTransaction();
//        Log.v(TAG , MyUtils.hexToString(buffer));
        if (Arrays.equals(buffer, MyUtils.stringTobytes(Constants.initalString))) {
            usbService.write(MyUtils.stringTobytes(Constants.replyString));
            if (isVerifying) {
                try {
                    submitData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            readResponse(buffer);
        }
    }

    private void readResponse(byte[] data) {

        byte[] mac_address = new byte[6];
        int status;

        if (data[0] != Constants.first_byte || data[1] != Constants.second_byte) {
            return;
        }

        System.arraycopy(data, 2, mac_address, 0, 6);

        status = data[12];

        switch (status) {
            case Constants.SUCCESS:
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("image", uri);
                startActivity(intent);
                finish();
                Toast.makeText(this, "Communication Established Successfully", Toast.LENGTH_SHORT).show();
                break;

            case Constants.FAILED:
                Log.v(TAG, "Failed command");
                Toast.makeText(this, "Cannot Establish Connection", Toast.LENGTH_SHORT).show();
                setContentView(R.layout.device_details);
                init();

                break;

            case Constants.INCORRECT_PARAMS:
                checkMacAddress(mac_address);
                setContentView(R.layout.device_details);
                init();

                break;
            case Constants.INCORRECT_PASSWORD:
                Toast.makeText(this, getString(R.string.incorrect_password), Toast.LENGTH_SHORT).show();
                setContentView(R.layout.device_details);
                init();
                break;
        }

    }

    @Override
    public void onStatusChange(Boolean status) {
        if (status) {
            setContentView(R.layout.device_details);
            init();
            if (uri != null){
                imageText.setText("Image Picked");
                imageText.setTextColor(ContextCompat.getColor(this ,  R.color.colorPrimary));
            }
        } else {
            setContentView(R.layout.activity_connection);
        }
    }

    private void init() {
//        EditText macAddress = findViewById(R.id.mac_address);
        Spinner deviceSpinner = findViewById(R.id.oid);
        RadioGroup ipSelectionGroup = findViewById(R.id.ip_selection_group);
        EditText sapName = findViewById(R.id.sap_name);
        imageText = findViewById(R.id.image_text);
        TextView clearAll = findViewById(R.id.clear_all);
        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                macAddress.setText(null);
                deviceSpinner.setSelection(0);
                sapName.setText(null);
                imageText.setText("Pick Site Image");
                uri = null;
            }
        });

        Button siteImage = findViewById(R.id.site_image);
        Button proceed = findViewById(R.id.proceed);

        deviceSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, deviceArray));

        SharedPreferences preferences = getSharedPreferences(Constants.prefPath, MODE_PRIVATE);
        String maccy = preferences.getString("mac", null);

        if (maccy != null) {
//            macAddress.setText(maccy);
            int oId = Integer.parseInt(preferences.getString("oid", null));
            deviceSpinner.setSelection(oId, true);

            boolean ipv6 = preferences.getBoolean("ipv6", false);
            if (ipv6)
                ipSelectionGroup.check(R.id.ipv6);
            else ipSelectionGroup.check(R.id.ipv4);
            sapName.setText(preferences.getString("sap", null));
            if (uri != null) {
                imageText.setText("Image Picked Successfully");
            }
        }

        siteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isPermission = checkForPermission();
                if (isPermission) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, 101);
                }
            }
        });

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (uri == null){
                    Toast.makeText(ConnectionActivity.this, "Please select site picture", Toast.LENGTH_SHORT).show();
                    return;
                }

//                byte[] mac_address = MyUtils.stringTobytes(macAddress.getText().toString());
//                if (mac_address == null || mac_address.length != 6) {
//                    macAddress.setError("Enter mac address of correct 6 bytes");
//                    return;
//                }
                if (deviceSpinner.getSelectedItemPosition() == 0) {
                    Toast.makeText(usbService, "Select Device ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (sapName.getText().toString().isEmpty()) {
                    sapName.setError("Empty");
                    return;
                }

                SharedPreferences.Editor editor = preferences.edit();
                if (macAddress == null){
                    editor.putString("mac", "00 00 00 00 00 00");
                }
                String deviceId = String.format("%02d", deviceSpinner.getSelectedItemPosition());
                editor.putString("oid", deviceId);

                int id = ipSelectionGroup.getCheckedRadioButtonId();
                if (id == R.id.ipv6) {
                    editor.putBoolean("ipv6", true);
                } else editor.putBoolean("ipv6", false);
                editor.putString("sap", sapName.getText().toString());
                editor.apply();

                setContentView(R.layout.verifying);
                isVerifying = true;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.v(TAG , "Image copying started");
                            InputStream in = getContentResolver().openInputStream(uri);
                            String filename = sapName.getText().toString() + ".png";
                            File image = new File(getExternalFilesDir("images"), filename);
                            image.createNewFile();
                            FileOutputStream out = new FileOutputStream(image);
                            IOUtils.copy(in , out);
                            in.close();
                            out.close();
                            Log.v(TAG , "Image copying successfull");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        submitData();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();

            }
        });
    }

    private boolean checkForPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, permissions, 99);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 99) {
            if (grantResults.length != 0 & grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 101);
            } else {
                Toast.makeText(this, "Permission Denied. Please grant permissions", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 & resultCode == RESULT_OK) {
            if (data != null) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    uri = imageUri;
                    if (imageText == null) {
                        Log.v("Image Text", "Is null");
                    }
                    imageText.setText("Image Picked");
                }
            }
        }
    }

    private void submitData() throws IOException {
        Log.v(TAG , "Submitting Data");
        SharedPreferences preferences = getSharedPreferences(Constants.prefPath, MODE_PRIVATE);
        String macAddress = preferences.getString("mac", "00 00 00 00 00 00");
        String oID = preferences.getString("oid", null);
        String employeeId = preferences.getString("eId", null);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] SOF = MyUtils.stringTobytes(Constants.SOF);
        outputStream.write(SOF);

        byte[] mac_address = MyUtils.stringTobytes(macAddress);
        outputStream.write(mac_address);

        byte[] o_id = MyUtils.stringTobytes(oID);
        outputStream.write(o_id);

        byte[] command = MyUtils.stringTobytes(Commands.CMD_AUTHENTICATION);
        outputStream.write(command);

        byte[] employee_id = MyUtils.stringTobytes(employeeId);
        outputStream.write(employee_id);

        byte[] data = MyUtils.stringTobytes("44 45 41 44 42 45 45 46");

        String dataLength = String.format("%04X", data.length);
        outputStream.write(MyUtils.stringTobytes(dataLength));

        outputStream.write(MyUtils.stringTobytes(Constants.SOF));
        outputStream.write(data);

        byte checksum = MyUtils.generateChecksum(outputStream.toByteArray());
        outputStream.write(checksum);

        String finalData = MyUtils.hexToString(outputStream.toByteArray());
//        Log.v(TAG, finalData);

        usbService.write(outputStream.toByteArray());
        realm.beginTransaction();
        CommandLog log = new CommandLog(System.currentTimeMillis() , "Sent" , finalData);
        realm.copyToRealm(log);
        realm.commitTransaction();
    }
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();


    public String getName (){
        final String[] name = new String[1];
        firebaseFirestore.collection("Users")
                .document("User123")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        name[0] = documentSnapshot.getString("name");
                    }
                });
        return name[0];
    }

}
