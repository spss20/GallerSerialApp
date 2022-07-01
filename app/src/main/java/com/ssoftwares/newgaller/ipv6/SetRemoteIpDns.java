package com.ssoftwares.newgaller.ipv6;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ssoftwares.newgaller.R;
import com.ssoftwares.newgaller.utils.Commands;
import com.ssoftwares.newgaller.utils.Constants;
import com.ssoftwares.newgaller.utils.MyUtils;
import com.ssoftwares.newgaller.views.BaseActivity;
import com.ssoftwares.newgaller.modals.UserLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class SetRemoteIpDns extends BaseActivity {

    private static final String TAG = "SetRemoteIpDnsIPV6";
    EditText remoteIp1;
    EditText remoteIp2;
    EditText remoteIp3;
    Button submit;
    AlertDialog dialog;
    boolean isResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_remote_ip_dns);
        mHandler = new MyHandler(this);

        getSupportActionBar().setTitle("Set Remote IP DNS");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        remoteIp1 = findViewById(R.id.remote_ip_1);
        remoteIp2 = findViewById(R.id.remote_ip_2);
        remoteIp3 = findViewById(R.id.remote_ip_3);
        submit = findViewById(R.id.submit_bt);

        submit.setOnClickListener(new View.OnClickListener() {
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
            ((TextView) dialog.findViewById(R.id.dialog_text)).setText("Checking Response");
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

        if (data[0] != Constants.first_byte) {
            Toast.makeText(usbService, "Unknown Response Recieved", Toast.LENGTH_SHORT).show();
        }

        if (data[1] != Constants.second_byte) {
            Toast.makeText(usbService, "Unknown Response Recieved", Toast.LENGTH_SHORT).show();
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
                        "Make sure if this device is IPV6 in actual", Toast.LENGTH_SHORT).show();
                break;

        }

    }

    private void submitData() throws IOException {

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
            Toast.makeText(usbService, "Correct the following error in red", Toast.LENGTH_SHORT).show();
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
            if (isResponse) {
                dialog.dismiss();
                Toast.makeText(usbService, "Request Timeout : Please try again", Toast.LENGTH_SHORT).show();
                isResponse = false;
            }
        }, 15000);

        ((TextView) dialog.findViewById(R.id.dialog_text)).setText("Receiving Response");

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
        dataStream.write(MyUtils.stringTobytes(Constants.IPV6));

        String remoteIp1String = remoteIp1.getText().toString();
        if (TextUtils.isEmpty(remoteIp1String)){
            remoteIp1.setError("Target Ip cannot be emmpty");
            return null;
        }
        byte[] remote_ip_1 = remoteIp1String.getBytes();

        String remoteIp2String = remoteIp2.getText().toString();
        if (TextUtils.isEmpty(remoteIp2String)){
            remoteIp2.setError("Gateway IP cannot be empty");
            return null;
        }
        byte[] remote_ip_2 = remoteIp2String.getBytes();

        String remoteIp3String = remoteIp3.getText().toString();
        if (TextUtils.isEmpty(remoteIp3String)){
            remoteIp3.setError("Dns Server cannot be empty");
            return null;
        }
        byte[] remote_ip_3 = remoteIp3String.getBytes();

        dataStream.write(remote_ip_1.length);
        dataStream.write(remote_ip_1);
        dataStream.write(remote_ip_2.length);
        dataStream.write(remote_ip_2);
        dataStream.write(remote_ip_3.length);
        dataStream.write(remote_ip_3);

        return dataStream.toByteArray();
    }

    @Override
    public void onStatusChange(Boolean status) {
        submit.setEnabled(status);
    }
}
