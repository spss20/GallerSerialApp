package com.ssoftwares.newgaller.ipv4;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ssoftwares.newgaller.views.BaseActivity;
import com.ssoftwares.newgaller.R;
import com.ssoftwares.newgaller.utils.Commands;
import com.ssoftwares.newgaller.utils.Constants;
import com.ssoftwares.newgaller.utils.MyUtils;
import com.ssoftwares.newgaller.modals.UserLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class SetRemoteServer extends BaseActivity {

    private static final String TAG = "SetRemoteServer";
    private EditText remoteServer1;
    private EditText remoteServer2;
    private EditText remoteServer3;
    private Button submitButton;
    private AlertDialog dialog;

    private boolean isResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_remote_server);

        mHandler = new MyHandler(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Set Remote Server");
        remoteServer1 = findViewById(R.id.remote_ip_1);
        remoteServer2 = findViewById(R.id.remote_ip_2);
        remoteServer3 = findViewById(R.id.remote_ip_3);

        submitButton = findViewById(R.id.submit_command);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    submitData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void readBuffer(Object object) {

        byte[] data = (byte[]) object;
        if (isResponse) {
            ((TextView)dialog.findViewById(R.id.dialog_text)).setText("Checking Response");
            isResponse = false;
            readResponse(data);
        } else {
            Log.v("response : ", MyUtils.hexToString(data));
            if (Arrays.equals(data, MyUtils.stringTobytes(Constants.initalString))) {
                Log.v(TAG, "Initialize command sent");
                usbService.write(MyUtils.stringTobytes(Constants.replyString));
            }
        }
    }

    private void readResponse(byte[] data) {

        byte[] mac_address = new byte[6];

        int status;

        if (data[0] != Constants.first_byte || data[1] != Constants.second_byte) {
            Toast.makeText(usbService, "Unknown Response Recieved", Toast.LENGTH_SHORT).show();
            return;
        }

        System.arraycopy(data, 2, mac_address, 0, 6);

        status = data[12];

        switch (status) {
            case Constants.SUCCESS:
                dialog.dismiss();
                Toast.makeText(this, "Command Executed Successfully", Toast.LENGTH_SHORT).show();

                break;

            case Constants.FAILED:
                dialog.dismiss();
                Log.v(TAG, "Failed command");
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();

                break;

            case Constants.INCORRECT_PARAMS:
                dialog.dismiss();
                checkMacAddress(mac_address);

                break;
            case Constants.FEATURE_NOT_SUPPORTED:
                dialog.dismiss();
                Toast.makeText(this, "Feature Not Supported\n" +
                        "Make sure if this device is IPV4 in actual", Toast.LENGTH_SHORT).show();

                break;
        }

    }

    @Override
    public void onStatusChange(Boolean status) {
        submitButton.setEnabled(status);
    }

    private void submitData() throws IOException {
//        validateData();
//        if (!validateData()) {
//            Toast.makeText(this, "Failed: correct the following error in red", Toast.LENGTH_SHORT).show();
//            return;
//        }
        showDialog();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] SOF = MyUtils.stringTobytes(Constants.SOF);
        outputStream.write(SOF);

        byte[] mac_address = MyUtils.stringTobytes(macAddress);
        outputStream.write(mac_address);

        byte[] o_id = MyUtils.stringTobytes(oID);
        outputStream.write(o_id);

        byte[] command = MyUtils.stringTobytes(Commands.CMD_SET_REMOTE_IP_DNS);
        outputStream.write(command);

        byte[] employee_id = MyUtils.stringTobytes(employeeId);
        outputStream.write(employee_id);

        byte[] data = getData();
        if (data == null){
            dialog.dismiss();
            return;
        }
        String dataLength = String.format("%04X", data.length);
        outputStream.write(MyUtils.stringTobytes(dataLength));

        outputStream.write(MyUtils.stringTobytes(Constants.SOF));
        outputStream.write(data);

        byte checksum = MyUtils.generateChecksum(outputStream.toByteArray());
        outputStream.write(checksum);

        String finalData = MyUtils.hexToString(outputStream.toByteArray());
        Log.v(TAG, finalData);

        usbService.write(outputStream.toByteArray());
        isResponse = true;

        new Handler().postDelayed(() -> {
            if (isResponse){
                dialog.dismiss();
                Toast.makeText(usbService, "Request Timeout : Please try again", Toast.LENGTH_SHORT).show();
                isResponse = false;
            }
        }, 15000);

        ((TextView)dialog.findViewById(R.id.dialog_text)).setText("Receiving Response");
    }

    private void showDialog() {
        dialog = new AlertDialog.Builder(this)
                .setView(R.layout.custom_dialog)
                .setCancelable(false)
                .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }
    private byte[] getData() throws IOException {
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        dataStream.write(MyUtils.stringTobytes(Constants.IPV4));

        byte[] remote_ip_1 = MyUtils.fetchIp(remoteServer1.getText().toString());
        byte[] remote_ip_2 = MyUtils.fetchIp(remoteServer2.getText().toString());
        byte[] remote_ip_3 = MyUtils.fetchIp(remoteServer3.getText().toString());

        if (remote_ip_1 == null){
            remoteServer1.setError("Incorrect IP address");
            return null;
        }

        if (remote_ip_2 == null){
            remoteServer2.setError("Incorrect IP address");
            return null;
        }

        if (remote_ip_3 == null){
            remoteServer3.setError("Incorrect IP address");
            return null;
        }

        dataStream.write(remote_ip_1);
        dataStream.write(remote_ip_2);
        dataStream.write(remote_ip_3);

        return dataStream.toByteArray();
    }

}
