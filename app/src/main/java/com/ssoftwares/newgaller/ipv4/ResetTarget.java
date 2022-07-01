package com.ssoftwares.newgaller.ipv4;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.ssoftwares.newgaller.views.BaseActivity;
import com.ssoftwares.newgaller.R;
import com.ssoftwares.newgaller.utils.Commands;
import com.ssoftwares.newgaller.utils.Constants;
import com.ssoftwares.newgaller.utils.MyUtils;
import com.ssoftwares.newgaller.views.ConnectionActivity;
import com.ssoftwares.newgaller.modals.UserLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ResetTarget extends BaseActivity {

    private static final String TAG = "Reset Target";
    private CheckBox ipv4Check;
    private CheckBox ipv6Check;
    private int resetType;
    private TextView resetText;
    private Button submit;
    private final int FACTORY_RESET = 1;
    private final int RESET_SAVED_CONFIG = 2;

    private android.app.AlertDialog dialog;
    private boolean isResponse;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_target);

        mHandler = new MyHandler(this);

        getSupportActionBar().setTitle("Reset Target");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ipv4Check = findViewById(R.id.ipv4_checkbox);
        ipv6Check = findViewById(R.id.ipv6_checkbox);
        Button resetDialog = findViewById(R.id.reset_type_bt);
        LinearLayout layout = findViewById(R.id.factory_reset_type);
        resetText = findViewById(R.id.reset_text);

        submit = findViewById(R.id.submit_command);
        resetType = -1;


        resetDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] items = { "Reset to last saved configurations" , "Factory Reset"};
                new AlertDialog.Builder(ResetTarget.this)
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0){
                                    resetType = RESET_SAVED_CONFIG;
                                    resetText.setText(items[0]);
                                    layout.setVisibility(View.GONE);
                                } else {
                                    resetType = FACTORY_RESET;
                                    resetText.setText(items[1]);
                                    layout.setVisibility(View.VISIBLE);
                                }
                            }
                        }).show();
            }
        });

        ipv4Check.setOnCheckedChangeListener((compoundButton, b) -> {
            if (ipv4Check.isChecked()) {
                ipv6Check.setChecked(false);
            } else
                ipv6Check.setChecked(true);
        });

        ipv6Check.setOnCheckedChangeListener((compoundButton, b) -> {
            if (ipv6Check.isChecked()) {
                ipv4Check.setChecked(false);
            } else
                ipv4Check.setChecked(true);
        });

        submit.setOnClickListener(view -> {
            try {
                submitData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public void readBuffer(Object object) {
        byte[] data = (byte[]) object;
        if (isResponse) {
            isResponse = false;
            dialog.dismiss();
            if (Arrays.equals(data, MyUtils.stringTobytes(Constants.initalString))) {
                startActivity(new Intent(this , ConnectionActivity.class));
                finish();
                Toast.makeText(usbService, "Successfully Reset , Please Connect Utility Again", Toast.LENGTH_SHORT).show();
            } else {
                readResponse(data);
            }
        } else {
            dialog.dismiss();
            Toast.makeText(usbService, "Unknown Response ", Toast.LENGTH_SHORT).show();
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
        submit.setEnabled(status);
    }

    private void submitData() throws IOException {
        showDialog();
        validateData();
        if (!validateData()) {
            return;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] SOF = MyUtils.stringTobytes(Constants.SOF);
        outputStream.write(SOF);

        byte[] mac_address = MyUtils.stringTobytes(macAddress);
        outputStream.write(mac_address);

        byte[] o_id = MyUtils.stringTobytes(oID);
        outputStream.write(o_id);

        byte[] command = MyUtils.stringTobytes(Commands.CMD_RESET_TARGET);
        outputStream.write(command);

        byte[] data = getData();

        byte[] employee_id = MyUtils.stringTobytes(employeeId);
        outputStream.write(employee_id);

        String dataLength = String.format("%04X", data.length + 1);
        outputStream.write(MyUtils.stringTobytes(dataLength));

        outputStream.write(MyUtils.stringTobytes(Constants.SOF));
        outputStream.write(data);

        byte checksum = MyUtils.generateChecksum(outputStream.toByteArray());
        outputStream.write(checksum);

        String finalData = MyUtils.hexToString(outputStream.toByteArray());
        Log.v(TAG, finalData);

        usbService.write(outputStream.toByteArray());
        isResponse = true;
        ((TextView) dialog.findViewById(R.id.dialog_text)).setText("Resetting Device");

    }

    private void showDialog() {
        dialog = new android.app.AlertDialog.Builder(this)
                .setView(R.layout.custom_dialog)
                .setCancelable(false)
                .show();
    }
    private byte[] getData() {
        byte[] constant;
        if (resetType == RESET_SAVED_CONFIG){
            constant = MyUtils.stringTobytes("0xFF");
        } else {
            if (ipv6Check.isChecked()){
                constant = MyUtils.stringTobytes("0x02");
            } else {
                constant = MyUtils.stringTobytes("0x01");
            }
        }
        return constant;
    }

    private boolean validateData() {

        if (resetType == -1){
            Toast.makeText(this, "Please select reset type", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (resetType ==  FACTORY_RESET){
            if (!ipv4Check.isChecked() && !ipv6Check.isChecked()){
                Toast.makeText(this, "Please select factory reset type", Toast.LENGTH_SHORT).show();
                return false ;
            }
        }
        return true;
    }



}
