package com.ssoftwares.newgaller.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ssoftwares.newgaller.R;
import com.ssoftwares.newgaller.modals.InputObject;
import com.ssoftwares.newgaller.modals.UserLog;
import com.ssoftwares.newgaller.utils.Commands;
import com.ssoftwares.newgaller.utils.Constants;
import com.ssoftwares.newgaller.utils.MyUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class InputStates extends BaseActivity {

    private static final String TAG = "InputStates";
    private AlertDialog dialog;
    private boolean isResponse;
    private ArrayList<InputObject> inputConfigs;
    private RecyclerView inputRecycler;
    private InputsAdapter adapter;
    Button applyChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_input_configurations);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Input States");
        mHandler = new MyHandler(this);
        inputConfigs = new ArrayList<>();
        inputRecycler = findViewById(R.id.inputs_recycler);
        applyChanges = findViewById(R.id.apply_changes);

        adapter = new InputsAdapter(this , inputConfigs , true);
        inputRecycler.setLayoutManager(new LinearLayoutManager(this));
        inputRecycler.setAdapter(adapter);

        applyChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    setInputConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        setServiceListener(new onServiceConnected() {
            @Override
            public void onConnected() {
                try {
                    getInputConfig();
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

        Log.v("Response" , MyUtils.hexToString(data));

        if (data[0] != Constants.first_byte || data[1] != Constants.second_byte) {
            Toast.makeText(usbService, "Unknown Response Recieved", Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] mac_address = new byte[6];
        System.arraycopy(data, 2, mac_address, 0, 6);

        int oid = data[8];
        int command = data[9];
        int status = data[12];



        switch (status) {
            case Constants.SUCCESS:
                dialog.dismiss();
                if (command == 33) {
                    inputConfigs.clear();
                    int noOfinputs = data[15] & 0xFF;
                    int startBit = 16;
                    for (int i = 0; i < noOfinputs; i++) {
                        int id = data[startBit] & 0xFF;
                        int value = data[startBit + noOfinputs];
                        InputObject input;
                        if (value == 0) {
                            input = new InputObject(id, false);
                        } else {
                            input = new InputObject(id, true);
                        }
                        inputConfigs.add(input);
                        startBit++;
                    }
                    adapter.updateData(inputConfigs);
                } else
                    Toast.makeText(usbService, "Changes applied successfully", Toast.LENGTH_SHORT).show();
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
            case Constants.INCORRECT_PASSWORD:
                startActivity(new Intent(this , MainActivity.class));
                Toast.makeText(usbService, "Unauthenticated: Please establish communication again", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }
    @Override
    public void onStatusChange(Boolean status) {
        applyChanges.setEnabled(status);
    }

    private void getInputConfig() throws IOException {
        showDialog();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] SOF = MyUtils.stringTobytes(Constants.SOF);
        outputStream.write(SOF);
        byte[] mac_address = MyUtils.stringTobytes(macAddress);
        outputStream.write(mac_address);
        byte[] o_id = MyUtils.stringTobytes(oID);
        outputStream.write(o_id);
        byte[] command = MyUtils.stringTobytes(Commands.CMD_GET_INPUT_STATES);
        outputStream.write(command);
        byte[] employee_id = MyUtils.stringTobytes(employeeId);
        outputStream.write(employee_id);
        String dataLength = String.format("%04X", 0);
        outputStream.write(MyUtils.stringTobytes(dataLength));
        outputStream.write(MyUtils.stringTobytes(Constants.SOF));
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
        }, 7000);

        ((TextView) dialog.findViewById(R.id.dialog_text)).setText("Receiving Response");
    }

    private void setInputConfig() throws IOException {

        showDialog();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] SOF = MyUtils.stringTobytes(Constants.SOF);
        outputStream.write(SOF);

        byte[] mac_address = MyUtils.stringTobytes(macAddress);
        outputStream.write(mac_address);

        byte[] o_id = MyUtils.stringTobytes(oID);
        outputStream.write(o_id);

        byte[] command = MyUtils.stringTobytes(Commands.CMD_SET_INPUT_STATES);
        outputStream.write(command);

        byte[] employee_id = MyUtils.stringTobytes(employeeId);
        outputStream.write(employee_id);

        byte[] data = getData();
        if (data == null) {
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
            if (isResponse) {
                dialog.dismiss();
                Toast.makeText(usbService, "Request Timeout : Please try again", Toast.LENGTH_SHORT).show();
                isResponse = false;
            }
        }, 7000);

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
        ArrayList<InputObject> inputConfigs =  adapter.getList();
        if (inputConfigs.isEmpty()){
            Toast.makeText(usbService, "An Error Occured", Toast.LENGTH_SHORT).show();
            return null;
        }
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        dataStream.write((byte) inputConfigs.size());
        for (InputObject inputObject: inputConfigs){
            dataStream.write((byte) inputObject.getId());
        }
        for (InputObject inputObject: inputConfigs){
            if (inputObject.isValue()){
                dataStream.write((byte) 1);
            } else dataStream.write((byte) 0);
        }
        return dataStream.toByteArray();
    }
}
