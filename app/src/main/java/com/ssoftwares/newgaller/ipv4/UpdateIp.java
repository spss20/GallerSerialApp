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

public class UpdateIp extends BaseActivity {

    private static final String TAG = "UpdateIp";
    private EditText targetIp;
    private EditText subnetMask;
    private EditText gatewayIp;
    private EditText dnsServerIp;
    private EditText dnsServerIp2;
    private Button submitButton;
    private AlertDialog dialog;

    private boolean isResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_ip);

        mHandler = new MyHandler(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Update IP");

        targetIp = findViewById(R.id.target_ip);
        subnetMask = findViewById(R.id.subnet_mask);
        gatewayIp = findViewById(R.id.gateway_ip);
        dnsServerIp = findViewById(R.id.dns_server_ip);
        dnsServerIp2 = findViewById(R.id.dns_server_2);


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
        int oid;
        int command;
        int status;

        if (data[0] != Constants.first_byte) {
            Toast.makeText(usbService, "Unknown Response Recieved", Toast.LENGTH_SHORT).show();
        }

        if (data[1] != Constants.second_byte) {
            Toast.makeText(usbService, "Unknown Response Recieved", Toast.LENGTH_SHORT).show();
        }

        System.arraycopy(data, 2, mac_address, 0, 6);

        oid = data[8];
        command = data[9];
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

        showDialog();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] SOF = MyUtils.stringTobytes(Constants.SOF);
        outputStream.write(SOF);

        byte[] mac_address = MyUtils.stringTobytes(macAddress);
        outputStream.write(mac_address);

        byte[] o_id = MyUtils.stringTobytes(oID);
        outputStream.write(o_id);

        byte[] command = MyUtils.stringTobytes(Commands.CMD_UPDATE_IP);
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
        dataStream.write(MyUtils.stringTobytes(Constants.IPV4));

        byte[] target_ip = MyUtils.fetchReverseIp(targetIp.getText().toString());
        byte[] subnet_mask = MyUtils.fetchReverseIp(subnetMask.getText().toString());
        byte[] gateway_ip = MyUtils.fetchReverseIp(gatewayIp.getText().toString());
        byte[] dns_server = MyUtils.fetchReverseIp(dnsServerIp.getText().toString());
        byte[] dns_server_2 = MyUtils.fetchReverseIp(dnsServerIp2.getText().toString());

        if (target_ip == null){
            targetIp.setError("Incorrect Target Ip");
            return null;
        }
        if (subnet_mask == null){
            subnetMask.setError("Incorrect Target Ip");
            return null;
        }
        if (gateway_ip == null){
            gatewayIp.setError("Incorrect Target Ip");
            return null;
        }
        if (dns_server == null){
            dnsServerIp.setError("Incorrect Target Ip");
            return null;
        }
        if (dns_server_2 == null){
            dnsServerIp2.setError("Incorrect Target Ip");
            return null;
        }
        dataStream.write(target_ip);
        dataStream.write(subnet_mask);
        dataStream.write(gateway_ip);
        dataStream.write(dns_server);
        dataStream.write(dns_server_2);

        return dataStream.toByteArray();
    }


}
