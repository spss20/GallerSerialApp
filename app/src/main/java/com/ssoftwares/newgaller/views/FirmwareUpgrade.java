package com.ssoftwares.newgaller.views;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ssoftwares.newgaller.R;
import com.ssoftwares.newgaller.modals.UserLog;
import com.ssoftwares.newgaller.utils.Commands;
import com.ssoftwares.newgaller.utils.Constants;
import com.ssoftwares.newgaller.utils.MyUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FirmwareUpgrade extends BaseActivity {

    private static final String TAG = "MainActivity";
    private TextView fileName;
    private InputStream stream;
    private Button flashFile;
    private AlertDialog dialog;
    private int percentage;
    private int packetSize = 4096;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firmware_upgrade);

        mHandler = new MyHandler(this);
        Button pickFile = findViewById(R.id.pick_file);
        fileName = findViewById(R.id.file_name);
        flashFile = findViewById(R.id.flash_file);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Firmware Upgrade ");
        pickFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermissionCheck();
            }
        });

        flashFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stream != null) {
                    percentage = 0;
                    dialog = new AlertDialog.Builder(FirmwareUpgrade.this)
                            .setView(R.layout.firmware_upgrade_dialog)
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    try {
                                        stream.reset();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setCancelable(false)
                            .show();
                    try {
                        initializeUpgrade();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void readBuffer(Object object) {
        byte[] data = (byte[]) object;
        readResponse(data);

    }

    private void readResponse(byte[] data) {

        Log.v("Response" , MyUtils.hexToString(data));
        byte[] mac_address = new byte[6];
        int oid;
        int command;
        int status;

        if (data[0] != Constants.first_byte) {
            Toast.makeText(usbService, "Unknown Response Recieved", Toast.LENGTH_SHORT).show();
            if (dialog != null)
                dialog.dismiss();
            return;
        }

        if (data[1] != Constants.second_byte) {
            Toast.makeText(usbService, "Unknown Response Recieved", Toast.LENGTH_SHORT).show();
            if (dialog != null)
                dialog.dismiss();
            return;
        }

        System.arraycopy(data, 2, mac_address, 0, 6);

        status = data[12];

        Log.v("Status", String.valueOf(status));



        switch (status) {
            case Constants.NEXT_PACKET:
                ((TextView) dialog.findViewById(R.id.uploaded_size)).setText(percentage + " Packet");
                ((ProgressBar) dialog.findViewById(R.id.upgrade_progress)).setProgress(percentage);
                percentage++;
                try {
                    sendStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Constants.FIRMWARE_DOWNLOAD_FAILED:
                dialog.dismiss();
                Toast.makeText(usbService, "Firmware Download Failed", Toast.LENGTH_SHORT).show();
                break;
            case Constants.SUCCESS:
                dialog.dismiss();
                Toast.makeText(this, "Firmware Upgraded Successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this , ConnectionActivity.class));
                finish();
                break;

            case Constants.FAILED:
                dialog.dismiss();
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                break;

            case Constants.INCORRECT_PARAMS:
                dialog.dismiss();
                checkMacAddress(mac_address);
                break;
        }

    }

    @Override
    public void onStatusChange(Boolean status) {

    }

    private void PermissionCheck() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

        if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, permissions[1]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, 100);
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, 200);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK) {
            try {
                if (data != null && data.getData() != null) {
                    stream = getContentResolver().openInputStream(data.getData());
                    fileName.setText(data.getData().getLastPathSegment());
                    flashFile.setEnabled(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void initializeUpgrade() throws IOException{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] SOF = MyUtils.stringTobytes(Constants.SOF);
        outputStream.write(SOF);

        byte[] mac_address = MyUtils.stringTobytes(macAddress);
        outputStream.write(mac_address);

        byte[] o_id = MyUtils.stringTobytes(oID);
        outputStream.write(o_id);

        byte[] command = MyUtils.stringTobytes(Commands.CMD_FIRMWARE_UPGRADE);
        outputStream.write(command);

        byte[] employee_id = MyUtils.stringTobytes(employeeId);
        outputStream.write(employee_id);

        String dataLength = String.format("%04X", 4);
        outputStream.write(MyUtils.stringTobytes(dataLength));

        outputStream.write(MyUtils.stringTobytes(Constants.SOF));

        int totalSize = stream.available();
        Log.v("Total Size" , String.valueOf(totalSize));
        int remainder = packetSize - totalSize % packetSize;
        Log.v("Remainder Size" , String.valueOf(remainder));
        int finalSize = totalSize + remainder;
        Log.v("Final Size" , String.valueOf(finalSize));
        ((TextView) dialog.findViewById(R.id.total_size)).setText(finalSize/packetSize + " Packets");
        ((ProgressBar) dialog.findViewById(R.id.upgrade_progress)).setMax(finalSize/packetSize);
        String data = String.format("%08X" , finalSize);
//        outputStream.write(MyUtils.stringTobytes(String.format("%08X" , 206848)));
        outputStream.write(MyUtils.stringTobytes(data));

        byte checksum = MyUtils.generateChecksum(outputStream.toByteArray());
        outputStream.write(checksum);

        String finalData = MyUtils.hexToString(outputStream.toByteArray());
        Log.v("Initial Data", finalData);
        usbService.write(outputStream.toByteArray());
    }

    private void sendStream() throws IOException {
        if (stream == null || stream.available() == 0) {
            Toast.makeText(this, "Stream not available", Toast.LENGTH_SHORT).show();
            return;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] SOF = MyUtils.stringTobytes(Constants.SOF);
        outputStream.write(SOF);

        byte[] mac_address = MyUtils.stringTobytes(macAddress);
        outputStream.write(mac_address);

        byte[] o_id = MyUtils.stringTobytes(oID);
        outputStream.write(o_id);

        byte[] command = MyUtils.stringTobytes(Commands.CMD_FIRMWARE_UPGRADE);
        outputStream.write(command);

        byte[] employee_id = MyUtils.stringTobytes(employeeId);
        outputStream.write(employee_id);

        String dataLength = String.format("%04X", packetSize);
        outputStream.write(MyUtils.stringTobytes(dataLength));

        outputStream.write(MyUtils.stringTobytes(Constants.SOF));
        byte[] bytes = new byte[packetSize];
        int i = stream.read(bytes);
        if (i == -1) {
            Log.v("HEx", "i is -1");
        }
        outputStream.write(bytes);
        byte checksum = MyUtils.generateChecksum(outputStream.toByteArray());
        outputStream.write(checksum);
//        Log.v("Next packet" , MyUtils.hexToString(outputStream.toByteArray()));
        usbService.write(outputStream.toByteArray());
    }
}
