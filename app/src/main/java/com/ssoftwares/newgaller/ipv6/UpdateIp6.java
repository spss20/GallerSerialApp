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

public class UpdateIp6 extends BaseActivity {

    private static final String TAG = "UpdateIp (IPV6)";
    EditText targetIP;
    EditText subnetPrefix;
    EditText gatewayIp;
    EditText dnsServer1;
    EditText dnsServer2;
    Button submit;

    AlertDialog dialog;
    boolean isResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_ip6);

        mHandler = new MyHandler(this);
        getSupportActionBar().setTitle("Update Controller IP");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        targetIP = findViewById(R.id.target_ip);
        subnetPrefix = findViewById(R.id.subnet_prefix);
        gatewayIp = findViewById(R.id.gateway_ip);
        dnsServer1 = findViewById(R.id.dns_server_1);
        dnsServer2 = findViewById(R.id.dns_server_2);
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
                        "Make sure if this device is ipv6 in actual", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    @Override
    public void onStatusChange(Boolean status) {
        submit.setEnabled(status);
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
                .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private byte[] getData() throws IOException {
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        dataStream.write(MyUtils.stringTobytes(Constants.IPV6));

        String targetIpString = targetIP.getText().toString();
        if (TextUtils.isEmpty(targetIpString)){
            targetIP.setError("Target Ip cannot be emmpty");
            return null;
        }
        byte[] target_ip = targetIP.getText().toString().getBytes();

        byte subnet_prefix;
        try {
            int subnetPrefixInt = Integer.parseInt(subnetPrefix.getText().toString());
            if (subnetPrefixInt > 255){
                subnetPrefix.setError("Subnet Prefix can't be more then 255");
                return null;
            } else {
                subnet_prefix = (byte) subnetPrefixInt;
            }
        } catch (NumberFormatException e){
            subnetPrefix.setError("Subnet Prefix can only be a number");
            return null;
        }

        String gatewayIpString = gatewayIp.getText().toString();
        if (TextUtils.isEmpty(gatewayIpString)){
            gatewayIp.setError("Gateway IP cannot be empty");
            return null;
        }
        byte[] gateway_ip = gatewayIp.getText().toString().getBytes();

        String dnsServer1String = dnsServer1.getText().toString();
        if (TextUtils.isEmpty(dnsServer1String)){
            dnsServer1.setError("Dns Server cannot be empty");
            return null;
        }
        byte[] dns_server = dnsServer1.getText().toString().getBytes();

        String dnsServer2String = dnsServer2.getText().toString();
        if (TextUtils.isEmpty(dnsServer2String)){
            dnsServer2.setError("Dns Server cannot be empty");
            return null;
        }
        byte[] dns_server_2 = dnsServer2.getText().toString().getBytes();

        dataStream.write(target_ip.length);
        dataStream.write(target_ip);
        dataStream.write(subnet_prefix);
        dataStream.write(gateway_ip.length);
        dataStream.write(gateway_ip);
        dataStream.write(dns_server.length);
        dataStream.write(dns_server);
        dataStream.write(dns_server_2.length);
        dataStream.write(dns_server_2);

        return dataStream.toByteArray();
    }

}
