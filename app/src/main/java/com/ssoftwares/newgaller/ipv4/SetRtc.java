package com.ssoftwares.newgaller.ipv4;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ssoftwares.newgaller.views.BaseActivity;
import com.ssoftwares.newgaller.R;
import com.ssoftwares.newgaller.utils.Commands;
import com.ssoftwares.newgaller.utils.Constants;
import com.ssoftwares.newgaller.utils.MyUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class SetRtc extends BaseActivity {

    private static final String TAG = "SetRtc";
    private EditText timeTick;
    private Button submitButton;
    private AlertDialog dialog;
    private ImageView selectDate;
    private TextView setCurrentTime;
    private TextView timeString;

    private boolean isResponse;
    private boolean isCurrentTime;
    private boolean isSecondRequest;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_rtc);

        mHandler = new MyHandler(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Set RTC");

        timeTick = findViewById(R.id.time_tick);
        selectDate = findViewById(R.id.select_date);
        setCurrentTime = findViewById(R.id.set_current_date);
        timeString = findViewById(R.id.time_string);

        selectedDate = Calendar.getInstance();

        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(SetRtc.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        selectedDate.set(i , i1 , i2);

                        new TimePickerDialog(SetRtc.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                selectedDate.set(Calendar.HOUR_OF_DAY , i);
                                selectedDate.set(Calendar.MINUTE , i1);
                                timeTick.setText(String.valueOf(selectedDate.getTimeInMillis()/1000));

                                String time = DateFormat.getDateTimeInstance(DateFormat.FULL , DateFormat.FULL).format(selectedDate.getTime());
                                timeString.setText(time);
                            }
                        } , selectedDate.get(Calendar.HOUR_OF_DAY) , selectedDate.get(Calendar.MINUTE) , true)
                                .show();
                    }
                } , selectedDate.get(Calendar.YEAR) , selectedDate.get(Calendar.MONTH) , selectedDate.get(Calendar.DAY_OF_MONTH))
                        .show();

            }
        });
        setCurrentTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeString.setText("Current date time will be send");
                timeTick.setText("Current date time will be send");
                isCurrentTime = true;
            }
        });

        submitButton= findViewById(R.id.submit_command);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sendRequest();
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
                if (isSecondRequest) {
                    dialog.dismiss();
                    isResponse = false;
                    isSecondRequest = false;
                    Toast.makeText(this, "Command Executed Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        sendData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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

        }
    }

    @Override
    public void onStatusChange(Boolean status) {
        submitButton.setEnabled(status);
    }

    private void sendRequest() throws IOException {
        validateData();
        if (!validateData()) {
            Toast.makeText(this, "Failed: correct the following error in red", Toast.LENGTH_SHORT).show();
            return;
        }

        showDialog();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] SOF = MyUtils.stringTobytes(Constants.SOF);
        outputStream.write(SOF);

        byte[] mac_address = MyUtils.stringTobytes(macAddress);
        outputStream.write(mac_address);

        byte[] o_id = MyUtils.stringTobytes(oID);
        outputStream.write(o_id);

        byte[] command = MyUtils.stringTobytes(Commands.CMD_SET_RTC);
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
            if (isResponse){
                dialog.dismiss();
                Toast.makeText(usbService, "Request Timeout : Please try again", Toast.LENGTH_SHORT).show();
                isResponse = false;
            }
        }, 20000);

        ((TextView)dialog.findViewById(R.id.dialog_text)).setText("Receiving Response");

    }

    private void showDialog() {
        dialog = new AlertDialog.Builder(this)
                .setView(R.layout.custom_dialog)
                .setCancelable(false)
                .show();
    }


    private void sendData() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] SOF = MyUtils.stringTobytes(Constants.SOF);
        outputStream.write(SOF);

        byte[] mac_address = MyUtils.stringTobytes(macAddress);
        outputStream.write(mac_address);

        byte[] o_id = MyUtils.stringTobytes(oID);
        outputStream.write(o_id);

        byte[] command = MyUtils.stringTobytes(Commands.CMD_SET_RTC);
        outputStream.write(command);

        byte[] data;
        if (isCurrentTime){
            long currentTime = System.currentTimeMillis()/1000;
            Log.v("Current TIme" , String.valueOf(currentTime));
            data = MyUtils.stringTobytes(String.format("%08X", currentTime));
        } else {
            data = MyUtils.stringTobytes(String.format("%08X", Long.valueOf(timeTick.getText().toString())));
        }

        if (data == null || data.length != 4) {
            Toast.makeText(this, "An Unknown Error Occured! Try Again", Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] finalDate = new byte[4];
        for (int i = 0 ; i<4 ; i++){
            finalDate[i] = data[3-i];
        }

        byte[] employee_id = MyUtils.stringTobytes(employeeId);
        outputStream.write(employee_id);

        String dataLength = String.format("%04X", 4);
        outputStream.write(MyUtils.stringTobytes(dataLength));

        outputStream.write(MyUtils.stringTobytes(Constants.SOF));
        outputStream.write(finalDate);

        byte checksum = MyUtils.generateChecksum(outputStream.toByteArray());
        outputStream.write(checksum);

        String finalData = MyUtils.hexToString(outputStream.toByteArray());
        Log.v(TAG, finalData);
        usbService.write(outputStream.toByteArray());
        isSecondRequest = true;
    }

    private boolean validateData() {

          if (TextUtils.isEmpty(timeTick.getText().toString())){
              timeTick.setError("Epoch time can't be empty");
              return false;
          }

        return true;
    }

}
