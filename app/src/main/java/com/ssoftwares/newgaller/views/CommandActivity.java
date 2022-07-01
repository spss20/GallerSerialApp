package com.ssoftwares.newgaller.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ssoftwares.newgaller.R;
import com.ssoftwares.newgaller.ipv4.ResetTarget;
import com.ssoftwares.newgaller.ipv4.SetNtp;
import com.ssoftwares.newgaller.ipv4.SetRemoteServer;
import com.ssoftwares.newgaller.ipv4.UpdateIp;
import com.ssoftwares.newgaller.ipv6.SetNtpServer;
import com.ssoftwares.newgaller.ipv6.SetRemoteIpDns;
import com.ssoftwares.newgaller.ipv6.UpdateIp6;
import com.ssoftwares.newgaller.modals.Device;
import com.ssoftwares.newgaller.modals.UserLog;
import com.ssoftwares.newgaller.utils.Commands;
import com.ssoftwares.newgaller.utils.Constants;
import com.ssoftwares.newgaller.utils.MyUtils;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import io.realm.RealmResults;

public class CommandActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "CommandActivity";
    private AlertDialog dialog;
    private boolean isResponse;
    private boolean isSecondRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commands);

        mHandler = new MyHandler(this);

        RelativeLayout getCommands = findViewById(R.id.open_get_commands);
        RelativeLayout setCommands = findViewById(R.id.open_set_commands);
        RelativeLayout actionCommands = findViewById(R.id.open_action_commands);

        ImageView getArrow = findViewById(R.id.get_arrow);
        ImageView getArrow2 = findViewById(R.id.get_arrow_2);
        ImageView getArrow3 = findViewById(R.id.get_arrow_3);

        ExpandableLayout getLayout = findViewById(R.id.get_layout);
        ExpandableLayout setLayout = findViewById(R.id.set_layout);
        ExpandableLayout actionLayout = findViewById(R.id.action_layout);

        RotateAnimation rotateAnimation = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(1000);
        rotateAnimation.setFillAfter(true);

        RotateAnimation rotateAnimation1 = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation1.setFillAfter(true);
        rotateAnimation1.setDuration(1000);

        actionCommands.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionLayout.isExpanded()) {
                    actionLayout.collapse();
                    getArrow3.startAnimation(rotateAnimation);
                } else {
                    actionLayout.expand();
                    getArrow3.startAnimation(rotateAnimation1);
                }
            }
        });
        getCommands.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getLayout.isExpanded()) {
                    getLayout.collapse();
                    getArrow.startAnimation(rotateAnimation);
                } else {
                    getLayout.expand();
                    getArrow.startAnimation(rotateAnimation1);
                }
            }
        });

        setCommands.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (setLayout.isExpanded()) {
                    setLayout.collapse();
                    getArrow2.startAnimation(rotateAnimation);
                } else {
                    setLayout.expand();
                    getArrow2.startAnimation(rotateAnimation1);
                }
            }
        });

        RelativeLayout updateIp = findViewById(R.id.update_ip);
        RelativeLayout getRemoteIp = findViewById(R.id.get_remote_ip);
        RelativeLayout setRemoteIp = findViewById(R.id.set_remote_ip);
        RelativeLayout getNtp = findViewById(R.id.get_ntp);
        RelativeLayout setNtp = findViewById(R.id.set_ntp);
        RelativeLayout getRtc = findViewById(R.id.get_rtc);
        RelativeLayout setRtc = findViewById(R.id.set_rtc);
        RelativeLayout resetTarget = findViewById(R.id.reset_target);
        RelativeLayout lastUpdateDetail = findViewById(R.id.last_update_details);
        RelativeLayout getVersion = findViewById(R.id.get_version);
        RelativeLayout getControllerIP = findViewById(R.id.get_controller_ip);
        RelativeLayout getOemId = findViewById(R.id.get_oem_id);
        RelativeLayout getInputConf = findViewById(R.id.input_configurations);
        RelativeLayout inputStates = findViewById(R.id.input_states);
        RelativeLayout getPortId = findViewById(R.id.get_port_id);
        RelativeLayout erasePower = findViewById(R.id.erase_power_outage);
        RelativeLayout connectionTimeout = findViewById(R.id.set_connection_timeout);
        RelativeLayout setPortId = findViewById(R.id.set_port_id);
        RelativeLayout reboot = findViewById(R.id.reboot_utility);
        RelativeLayout setIpType = findViewById(R.id.set_ip_type);
        RelativeLayout firmwareUpgrade = findViewById(R.id.firmware_upgrade);
        RelativeLayout getPowerOutage = findViewById(R.id.get_power_outage);
        TextView ipVersion = findViewById(R.id.ip_version_tv);

        if (ipv6) {
            getSupportActionBar().setTitle("Commands (IPV6)");
            ipVersion.setText("Enable IPV4");
        } else {
            getSupportActionBar().setTitle("Commands (IPV4)");
            ipVersion.setText("Enable IPV6");
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getPowerOutage.setOnClickListener(this);
        firmwareUpgrade.setOnClickListener(this);
        setIpType.setOnClickListener(this);
        getPortId.setOnClickListener(this);
        reboot.setOnClickListener(this);
        setPortId.setOnClickListener(this);
        connectionTimeout.setOnClickListener(this);
        getControllerIP.setOnClickListener(this);
        getInputConf.setOnClickListener(this);
        getOemId.setOnClickListener(this);
        getVersion.setOnClickListener(this);
        updateIp.setOnClickListener(this);
        getRemoteIp.setOnClickListener(this);
        setRemoteIp.setOnClickListener(this);
        getNtp.setOnClickListener(this);
        setNtp.setOnClickListener(this);
        getRtc.setOnClickListener(this);
        setRtc.setOnClickListener(this);
        resetTarget.setOnClickListener(this);
        erasePower.setOnClickListener(this);
        lastUpdateDetail.setOnClickListener(this);
        inputStates.setOnClickListener(this);

    }

    @Override
    public void readBuffer(Object object) {
        byte[] data = (byte[]) object;
        Log.v(TAG , MyUtils.hexToString(data));
        if (isResponse) {
            ((TextView) dialog.findViewById(R.id.dialog_text)).setText("Checking Response");
            isResponse = false;
            readResponse(data);
        } else {
            if (Arrays.equals(data, MyUtils.stringTobytes(Constants.initalString))) {
                usbService.write(MyUtils.stringTobytes(Constants.replyString));
            }
        }
    }

    private void readResponse(byte[] data) {

        byte[] mac_address = new byte[6];
        String command;
        int status;

        if (data[0] != Constants.first_byte || data[1] != Constants.second_byte) {
            Toast.makeText(usbService, "Unknown Response Recieved", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        System.arraycopy(data, 2, mac_address, 0, 6);

        command = "0x" + Integer.toHexString(data[9] & 0xFF).toUpperCase();

        byte[] lengthBytes = {data[10], data[11]};
        int dataLength = MyUtils.getHexInteger(lengthBytes);

        status = data[12];
        UserLog userLog = new UserLog();
        userLog.setTimestamp(System.currentTimeMillis());
        userLog.setMac(macAddress);
        StringBuilder message = new StringBuilder();


        switch (status) {
            case Constants.SUCCESS:
                dialog.dismiss();
                AlertDialog infoDialog = new AlertDialog.Builder(this)
                        .setView(R.layout.custom_dialog_1)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();

                switch (command) {
                    case Commands.CMD_ENABLE_IPV4_6:
                        infoDialog.dismiss();
                        if (ipv6) {
                            Toast.makeText(usbService, "Changed To IPV4 Successfully", Toast.LENGTH_SHORT).show();
                            SharedPreferences preferences = getSharedPreferences(Constants.prefPath, MODE_PRIVATE);
                            preferences.edit().putBoolean("ipv6", false).apply();
                        } else {
                            Toast.makeText(usbService, "Changed To IPV6 Successfully", Toast.LENGTH_SHORT).show();
                            SharedPreferences preferences = getSharedPreferences(Constants.prefPath, MODE_PRIVATE);
                            preferences.edit().putBoolean("ipv6", true).apply();
                        }
                        startActivity(new Intent(this, ConnectionActivity.class));
                        finish();
                        break;
                    case Commands.CMD_REBOOT_TARGET:
                        infoDialog.dismiss();
                        startActivity(new Intent(this, ConnectionActivity.class));
                        finish();
                        Toast.makeText(usbService, "Reboot Successfull", Toast.LENGTH_SHORT).show();
                        break;
                    case Commands.CMD_CONNECTION_TIMEOUT:
                        infoDialog.dismiss();
                        Toast.makeText(usbService, "Command connection timeout Successfull", Toast.LENGTH_SHORT).show();
                        break;
                    case Commands.CMD_ERASE_POWER_OUTAGE:
                        infoDialog.dismiss();
                        Device devices = realm.where(Device.class).equalTo("sapId" , sap).findFirst();
                        realm.beginTransaction();
                        devices.setPowerReset(true);
                        realm.commitTransaction();
                        Toast.makeText(usbService, "Command Executed Successfully", Toast.LENGTH_SHORT).show();
                        break;
                    case Commands.CMD_SET_RTC:
                        infoDialog.dismiss();
                        if (isSecondRequest) {
                            Toast.makeText(usbService, "Command Executed Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                sendTime();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    case Commands.CMD_GET_NTP_SERVER:
                        getNtpServer(data, infoDialog);
                        break;
                    case Commands.CMD_GET_PORT_ID:
                        getPortId(data, infoDialog);
                        break;
                    case Commands.CMD_GET_POWER_OUTAGE:
                        getPowerOutage(data , infoDialog);
                        break;
                    case Commands.CMD_GET_INPUT_CONFIG:
                        getInputConfig(data, infoDialog);
                        break;
                    case Commands.CMD_GET_RTC:
                        commandRtc(data, infoDialog);
                        break;
                    case Commands.CMD_GET_REMOTE_IP_DNS:
                        commandGetRemoteIp(data, infoDialog);
                        break;
                    case Commands.CMD_GET_FIRMWARE_VER:
                        getFirmwareVersion(data, dataLength, infoDialog);
                        break;
                    case Commands.CMD_LAST_UPDATE_DETAIL:
                        getLastUpdateDetail(data, infoDialog);
                        break;
                    case Commands.CMD_GET_CONTROLLER_IP:
                        getContollerIp(data, infoDialog);
                        break;
                    case Commands.CMD_GET_OEM_ID:
                        getOEM(data, infoDialog);
                        break;
                }
                break;

            case Constants.FAILED:
                dialog.dismiss();
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                message.append(" failed");
                break;

            case Constants.INCORRECT_PARAMS:
                dialog.dismiss();
                checkMacAddress(mac_address);

                break;
            case Constants.INCORRECT_PASSWORD:
                dialog.dismiss();
                startActivity(new Intent(this, MainActivity.class));
                finish();
                message.append(" have incorrect password");
                Toast.makeText(usbService, "Utility Communication Disconnected. Establish Connection Again", Toast.LENGTH_SHORT).show();
                break;
            case Constants.FEATURE_NOT_SUPPORTED:
                dialog.dismiss();
                Toast.makeText(this, "Feature Not Supported\n" +
                        "Make sure if this device is ipv6 in actual", Toast.LENGTH_SHORT).show();
                message.append(" is not supported");
                break;
        }

    }

    private void getPowerOutage(byte[] data, AlertDialog infoDialog) {
        int totalPowerSize = data[15] & 0xFF;
        byte[] totalPowerOutage = new byte[totalPowerSize];

        int startBit = 16;
        for (int i = 0; i<totalPowerSize; i++ ){
            totalPowerOutage[i] = data[startBit];
            startBit++;
        }
        int ebRunSize = data[startBit] & 0xFF;
        byte[] ebRunTime = new byte[ebRunSize];
        startBit++;
        for (int i = 0 ; i<ebRunSize; i++){
            ebRunTime[i] = data[startBit];
            startBit++;
        }
        int dgRunSize = data[startBit] & 0xFF;
        byte[] dgRunTime = new byte[dgRunSize];
        startBit++;
        for (int i = 0; i<dgRunSize; i++){
            dgRunTime[i] = data[startBit];
            startBit++;
        }
        ((TextView) infoDialog.findViewById(R.id.key_1)).setText("Total Power Outage");
        ((TextView) infoDialog.findViewById(R.id.key_2)).setText("EB Run Time");
        ((TextView) infoDialog.findViewById(R.id.key_3)).setText("DG Run Time");
        ((TextView) infoDialog.findViewById(R.id.value_1)).setText(new String(totalPowerOutage));
        ((TextView) infoDialog.findViewById(R.id.value_2)).setText(new String(ebRunTime));
        ((TextView) infoDialog.findViewById(R.id.value_3)).setText(new String(dgRunTime));

        infoDialog.findViewById(R.id.row_4).setVisibility(View.GONE);
        infoDialog.findViewById(R.id.row_5).setVisibility(View.GONE);
    }

    private void getPortId(byte[] data, AlertDialog infoDialog) {
        byte[] portNumber = {data[16], data[15]};
        ((TextView) infoDialog.findViewById(R.id.key_1)).setText("Port Id: ");
        ((TextView) infoDialog.findViewById(R.id.value_1)).setText(MyUtils.hexToString(portNumber));
        infoDialog.findViewById(R.id.row_2).setVisibility(View.GONE);
        infoDialog.findViewById(R.id.row_3).setVisibility(View.GONE);
    }

    private void getInputConfig(byte[] data, AlertDialog infoDialog) {
        Toast.makeText(usbService, "Under Development", Toast.LENGTH_SHORT).show();
        int numberOfInputs = data[15];
        ArrayList<String> inputsStatus = new ArrayList<>();
        for (int i = 0; i < numberOfInputs; i++) {

        }
    }

    private void getNtpServer(byte[] data, AlertDialog infoDialog) {
        byte ipvType = data[15];
        if (ipvType == -15) {
            byte[] ntp_server_1 = {data[16], data[17], data[18], data[19]};
            byte[] ntp_server_2 = {data[20], data[21], data[22], data[23]};

            ((TextView) infoDialog.findViewById(R.id.key_1)).setText("NTP Server 1");
            ((TextView) infoDialog.findViewById(R.id.key_2)).setText("NTP Server 2");
            ((TextView) infoDialog.findViewById(R.id.key_3)).setText("IPV TYPE");
            ((TextView) infoDialog.findViewById(R.id.value_1)).setText(MyUtils.formatIp(ntp_server_1));
            ((TextView) infoDialog.findViewById(R.id.value_2)).setText(MyUtils.formatIp(ntp_server_2));
            ((TextView) infoDialog.findViewById(R.id.value_3)).setText("IPV4");

//            infoDialog.findViewById(R.id.row_3).setVisibility(View.GONE);
            infoDialog.findViewById(R.id.row_4).setVisibility(View.GONE);
            infoDialog.findViewById(R.id.row_5).setVisibility(View.GONE);

        } else if (ipvType == -14) {
            int NtpServer1Length = data[16] & 0xFF;
            int endByte = 17 + NtpServer1Length;
            byte[] ntp_server_1 = new byte[NtpServer1Length];
            System.arraycopy(data, 17, ntp_server_1, 0, NtpServer1Length);

            int NtpServer2Length = data[endByte] & 0xFF;
            byte[] ntp_server_2 = new byte[NtpServer2Length];
            System.arraycopy(data, endByte + 1, ntp_server_2, 0, NtpServer2Length);

            ((TextView) infoDialog.findViewById(R.id.key_1)).setText("NTP Server 1");
            ((TextView) infoDialog.findViewById(R.id.key_2)).setText("NTP Server 2");
            ((TextView) infoDialog.findViewById(R.id.key_3)).setText("IPV TYPE");

            ((TextView) infoDialog.findViewById(R.id.value_1)).setText(new String(ntp_server_1));
            ((TextView) infoDialog.findViewById(R.id.value_2)).setText(new String(ntp_server_2));
            ((TextView) infoDialog.findViewById(R.id.value_3)).setText("IPV6");

//            infoDialog.findViewById(R.id.row_3).setVisibility(View.GONE);
            infoDialog.findViewById(R.id.row_4).setVisibility(View.GONE);
            infoDialog.findViewById(R.id.row_5).setVisibility(View.GONE);

        }

        Toast.makeText(this, "Command Executed Successfully", Toast.LENGTH_SHORT).show();
    }

    private void getOEM(byte[] data, AlertDialog infoDialog) {
        int oemId = data[15] & 0xFF;
        String oemCompany = "";
        switch (oemId) {
            case 1:
                oemCompany = "M/s Radient";
                break;
            case 2:
                oemCompany = "M/s Sterna";
                break;
            case 3:
                oemCompany = "M/s Access Computech";
                break;
            default:
                oemCompany = "M/s XXXX Company";
        }

        ((TextView) infoDialog.findViewById(R.id.key_1)).setText("Oem Id: ");
        ((TextView) infoDialog.findViewById(R.id.value_1)).setText(oemCompany);
        infoDialog.findViewById(R.id.row_2).setVisibility(View.GONE);
        infoDialog.findViewById(R.id.row_3).setVisibility(View.GONE);

    }

    private void getContollerIp(byte[] data, AlertDialog infoDialog) {
        byte ipvType = data[15];
        if (ipvType == -15) {
            byte[] targetIp = {data[19], data[18], data[17], data[16]};
            byte[] subnetMask = {data[23], data[22], data[21], data[20]};
            byte[] gatewayIp = {data[27], data[26], data[25], data[24]};
            byte[] dnsServer1 = {data[31], data[30], data[29], data[28]};
            byte[] dnsServer2 = {data[35], data[34], data[33], data[32]};

            ((TextView) infoDialog.findViewById(R.id.key_1)).setText("Target IP   ");
            ((TextView) infoDialog.findViewById(R.id.key_2)).setText("Subnet Mask ");
            ((TextView) infoDialog.findViewById(R.id.key_3)).setText("GatewayIP   ");
            ((TextView) infoDialog.findViewById(R.id.key_4)).setText("Dns Server 1");
            ((TextView) infoDialog.findViewById(R.id.key_5)).setText("Dns Server 2");
            ((TextView) infoDialog.findViewById(R.id.key_6)).setText("Link Local");

            infoDialog.findViewById(R.id.row_4).setVisibility(View.VISIBLE);
            infoDialog.findViewById(R.id.row_5).setVisibility(View.VISIBLE);
            infoDialog.findViewById(R.id.row_6).setVisibility(View.VISIBLE);

            ((TextView) infoDialog.findViewById(R.id.value_1)).setText(MyUtils.formatIp(targetIp));
            ((TextView) infoDialog.findViewById(R.id.value_2)).setText(MyUtils.formatIp(subnetMask));
            ((TextView) infoDialog.findViewById(R.id.value_3)).setText(MyUtils.formatIp(gatewayIp));
            ((TextView) infoDialog.findViewById(R.id.value_4)).setText(MyUtils.formatIp(dnsServer1));
            ((TextView) infoDialog.findViewById(R.id.value_5)).setText(MyUtils.formatIp(dnsServer2));

            try {
                String[] macPart = macAddress.split("\\s");
                StringBuilder builder = new StringBuilder();
                builder.append("fe80::");
                int addition = Integer.parseInt(macPart[0], 16) + 2;
                builder.append(String.format("%02X" , addition));
                builder.append(macPart[1] + ":" + macPart[2] + "ff:fe" + macPart[3]);
                builder.append(":" + macPart[4] + macPart[5]);
                ((TextView) infoDialog.findViewById(R.id.value_6)).setText(builder.toString().toLowerCase());
            } catch (NumberFormatException e){
                Toast.makeText(this, "Please restart app to see link local", Toast.LENGTH_SHORT).show();
            }
        } else if (ipvType == -14) {
            int targetIpLength = data[16] & 0xFF;
            byte[] targetIpBytes = new byte[targetIpLength];
            int startBit = 17;
            System.arraycopy(data, startBit, targetIpBytes, 0, targetIpLength);

            int subnetPrefixByte = 17 + targetIpLength;
            int subnetPrefix = data[subnetPrefixByte] & 0xFF;

            int gatewayIpLength = data[subnetPrefixByte + 1] & 0xFF;
            int startBit1 = subnetPrefixByte + 2;
            int endBit1 = startBit1 + gatewayIpLength;
            byte[] gatewayIpBytes = new byte[gatewayIpLength];
            System.arraycopy(data, startBit1, gatewayIpBytes, 0, gatewayIpLength);

            int dnsServer1Length = data[endBit1] & 0xFF;
            int startBit2 = endBit1 + 1;
            int endBit2 = startBit2 + dnsServer1Length;
            byte[] dnsServer1 = new byte[dnsServer1Length];
            System.arraycopy(data, startBit2, dnsServer1, 0, dnsServer1Length);

            int dnsServer2Length = data[endBit2] & 0xFF;
            int startBit3 = endBit2 + 1;
            byte[] dnsServer2 = new byte[dnsServer2Length];
            System.arraycopy(data, startBit3, dnsServer2, 0, dnsServer2Length);

            ((TextView) infoDialog.findViewById(R.id.key_1)).setText("Target IP   ");
            ((TextView) infoDialog.findViewById(R.id.key_2)).setText("Subnet Prefix ");
            ((TextView) infoDialog.findViewById(R.id.key_3)).setText("GatewayIP   ");
            ((TextView) infoDialog.findViewById(R.id.key_4)).setText("Dns Server 1");
            ((TextView) infoDialog.findViewById(R.id.key_5)).setText("Dns Server 2");
            ((TextView) infoDialog.findViewById(R.id.key_6)).setText("Link Local");

            infoDialog.findViewById(R.id.row_4).setVisibility(View.VISIBLE);
            infoDialog.findViewById(R.id.row_5).setVisibility(View.VISIBLE);
            infoDialog.findViewById(R.id.row_6).setVisibility(View.VISIBLE);

            ((TextView) infoDialog.findViewById(R.id.value_1)).setText(new String(targetIpBytes));
            ((TextView) infoDialog.findViewById(R.id.value_2)).setText(String.valueOf(subnetPrefixByte));
            ((TextView) infoDialog.findViewById(R.id.value_3)).setText(new String(gatewayIpBytes));
            ((TextView) infoDialog.findViewById(R.id.value_4)).setText(new String(dnsServer1));
            ((TextView) infoDialog.findViewById(R.id.value_5)).setText(new String(dnsServer2));

            try {
                String[] macPart = macAddress.split("\\s");
                StringBuilder builder = new StringBuilder();
                builder.append("fe80::");
                int addition = Integer.parseInt(macPart[0], 16) + 2;
                builder.append(String.format("%02X" , addition));
                builder.append(macPart[1] + ":" + macPart[2] + "ff:fe" + macPart[3]);
                builder.append(":" + macPart[4] + macPart[5]);
                ((TextView) infoDialog.findViewById(R.id.value_6)).setText(builder.toString().toLowerCase());
            } catch (NumberFormatException e){
                Toast.makeText(this, "Please restart app to see link local", Toast.LENGTH_SHORT).show();
            }


        }
    }

    private void getLastUpdateDetail(byte[] data, AlertDialog infoDialog) {
        byte lastCommand = data[15];
        String command = MyUtils.getCommandName(lastCommand);
        byte[] eId = {data[16], data[17]};
        int employeeId = MyUtils.getHexInteger(eId);
        byte[] timeStamp = {data[21], data[20], data[19], data[18]};
        long millisecond = MyUtils.getHexLong(timeStamp) * 1000;
        Date date = new Date(millisecond);
        byte statusByte = data[22];
        String status = MyUtils.getStatus(statusByte);

        ((TextView) infoDialog.findViewById(R.id.key_1)).setText("Last Command");
        ((TextView) infoDialog.findViewById(R.id.key_2)).setText("Employee ID");
        ((TextView) infoDialog.findViewById(R.id.key_3)).setText("Timestamp");
        ((TextView) infoDialog.findViewById(R.id.key_4)).setText("Status");

        infoDialog.findViewById(R.id.row_4).setVisibility(View.VISIBLE);

        ((TextView) infoDialog.findViewById(R.id.value_1)).setText(command);
        ((TextView) infoDialog.findViewById(R.id.value_2)).setText(String.valueOf(employeeId));
        ((TextView) infoDialog.findViewById(R.id.value_3)).setText(DateFormat.getDateTimeInstance().format(date));
        ((TextView) infoDialog.findViewById(R.id.value_4)).setText(status);


    }

    private void commandGetRemoteIp(byte[] data, AlertDialog infoDialog) {
        byte ipvType = data[15];
        if (ipvType == -15) {
            byte[] remoteIp1 = {data[16], data[17], data[18], data[19]};
            byte[] remoteIp2 = {data[20], data[21], data[22], data[23]};
            byte[] remoteIp3 = {data[24], data[25], data[26], data[27]};

            ((TextView) infoDialog.findViewById(R.id.key_1)).setText("Remote Ip 1");
            ((TextView) infoDialog.findViewById(R.id.key_2)).setText("Remote Ip 2");
            ((TextView) infoDialog.findViewById(R.id.key_3)).setText("Remote Ip 3");
            ((TextView) infoDialog.findViewById(R.id.value_1)).setText(MyUtils.formatIp(remoteIp1));
            ((TextView) infoDialog.findViewById(R.id.value_2)).setText(MyUtils.formatIp(remoteIp2));
            ((TextView) infoDialog.findViewById(R.id.value_3)).setText(MyUtils.formatIp(remoteIp3));

        } else if (ipvType == -14) {
            int remoteIp1Length = data[16] & 0xFF;
            int startBit = 17;
            int endBit = startBit + remoteIp1Length;
            byte[] remote_ip_1 = new byte[remoteIp1Length];
            System.arraycopy(data, startBit, remote_ip_1, 0, remoteIp1Length);

            int remoteIp2Length = data[endBit] & 0xFF;
            int startBit1 = endBit + 1;
            int endBit1 = startBit1 + remoteIp2Length;
            byte[] remote_ip_2 = new byte[remoteIp2Length];
            System.arraycopy(data, startBit1, remote_ip_2, 0, remoteIp2Length);

            int remoteIp3Length = data[endBit] & 0xFF;
            int startBit2 = endBit1 + 1;
            byte[] remote_ip_3 = new byte[remoteIp3Length];
            System.arraycopy(data, startBit2, remote_ip_3, 0, remoteIp3Length);

            ((TextView) infoDialog.findViewById(R.id.key_1)).setText("Remote Ip 1");
            ((TextView) infoDialog.findViewById(R.id.key_2)).setText("Remote Ip 2");
            ((TextView) infoDialog.findViewById(R.id.key_3)).setText("Remote Ip 3");
            ((TextView) infoDialog.findViewById(R.id.value_1)).setText(new String(remote_ip_1));
            ((TextView) infoDialog.findViewById(R.id.value_2)).setText(new String(remote_ip_2));
            ((TextView) infoDialog.findViewById(R.id.value_3)).setText(new String(remote_ip_3));

        }

        Toast.makeText(this, "Command Executed Successfully", Toast.LENGTH_SHORT).show();
    }

    private void getFirmwareVersion(byte[] data, int dataLength, AlertDialog infoDialog) {

        byte[] ascii = new byte[dataLength - 1];

        int version_length = data[15];
        int x = 16;
        for (int i = 0; i < version_length; i++) {
            ascii[i] = data[x];
            x++;
        }

        String version = new String(ascii);

        ((TextView) infoDialog.findViewById(R.id.key_1)).setText("Firmware Version :");
        ((TextView) infoDialog.findViewById(R.id.value_1)).setText(version);
        infoDialog.findViewById(R.id.row_2).setVisibility(View.GONE);
        infoDialog.findViewById(R.id.row_3).setVisibility(View.GONE);

        Toast.makeText(this, "Command Executed Successfully", Toast.LENGTH_SHORT).show();
    }

    private void sendTime() throws IOException {
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

        long currentTime = System.currentTimeMillis() / 1000;
        data = MyUtils.stringTobytes(String.format("%08X", currentTime));

        if (data == null || data.length != 4) {
            Toast.makeText(this, "An Unknown Error Occured! Try Again", Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] finalDate = new byte[4];
        for (int i = 0; i < 4; i++) {
            finalDate[i] = data[3 - i];
        }

        byte[] employee_id = MyUtils.stringTobytes(employeeId);
        outputStream.write(employee_id);

        String dataLength = String.format("%04X", 4);
        outputStream.write(MyUtils.stringTobytes(dataLength));

        outputStream.write(MyUtils.stringTobytes(Constants.SOF));
        outputStream.write(finalDate);

        byte checksum = MyUtils.generateChecksum(outputStream.toByteArray());
        outputStream.write(checksum);

        usbService.write(outputStream.toByteArray());
        isSecondRequest = true;
    }

    private void commandRtc(byte[] data, AlertDialog infoDialog) {
        byte[] time = {data[18], data[17], data[16], data[15]};
        long millisecond = MyUtils.getHexLong(time) * 1000;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millisecond);
        String date = DateFormat.getDateTimeInstance().format(calendar.getTime());

        ((TextView) infoDialog.findViewById(R.id.key_1)).setText("Date:");
        ((TextView) infoDialog.findViewById(R.id.value_1)).setText(date);
        infoDialog.findViewById(R.id.row_2).setVisibility(View.GONE);
        infoDialog.findViewById(R.id.row_3).setVisibility(View.GONE);
        Toast.makeText(this, "Command Executed Successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChange(Boolean status) {
        if (dialog != null) {
            dialog.dismiss();
        }
    }


    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.firmware_upgrade:
                    startActivity(new Intent(this, FirmwareUpgrade.class));
                    break;
                case R.id.set_ip_type:
                    setIpType();
                    break;
                case R.id.set_port_id:
                    Toast.makeText(usbService, "An Unknown Error Occured", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.reboot_utility:
                    reboot();
                    break;
                case R.id.set_connection_timeout:
                    setConnectionTimeout();
                    break;
                case R.id.get_power_outage:
                    submitData(Commands.CMD_GET_POWER_OUTAGE , null);
                    break;
                case R.id.erase_power_outage:
                    new AlertDialog.Builder(this)
                            .setTitle("Confirmation")
                            .setMessage("Are you sure you want to excecute this command?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    try {
                                        submitData(Commands.CMD_ERASE_POWER_OUTAGE, null);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                    break;
                case R.id.get_port_id:
                    sendPortIdRequest();
                    break;
                case R.id.input_configurations:
                    startActivity(new Intent(this, SetInputConfigurations.class));
                    break;
                case R.id.input_states:
                    startActivity(new Intent(this , InputStates.class));
                    break;
                case R.id.get_oem_id:
                    submitData(Commands.CMD_GET_OEM_ID, null);
                    break;
                case R.id.get_controller_ip:
                    submitData(Commands.CMD_GET_CONTROLLER_IP, null);
                    break;
                case R.id.last_update_details:
                    submitData(Commands.CMD_LAST_UPDATE_DETAIL, null);
                    break;
                case R.id.get_version:
                    submitData(Commands.CMD_GET_FIRMWARE_VER, null);
                    break;
                case R.id.update_ip:
                    if (ipv6)
                        startActivity(new Intent(this, UpdateIp6.class));
                    else
                        startActivity(new Intent(CommandActivity.this, UpdateIp.class));
                    break;

                case R.id.set_remote_ip:
                    if (ipv6)
                        startActivity(new Intent(this, SetRemoteIpDns.class));
                    else
                        startActivity(new Intent(this, SetRemoteServer.class));
                    break;

                case R.id.get_rtc:
                    submitData(Commands.CMD_GET_RTC, null);
                    break;
                case R.id.set_rtc:
                    sendRequest();
                    break;
                case R.id.set_ntp:
                    if (ipv6)
                        startActivity(new Intent(this, SetNtpServer.class));
                    else
                        startActivity(new Intent(this, SetNtp.class));
                    break;

                case R.id.get_ntp:
                    submitData(Commands.CMD_GET_NTP_SERVER, null);
                    break;

                case R.id.reset_target:
                    startActivity(new Intent(this, ResetTarget.class));
                    break;

                case R.id.get_remote_ip:
                    submitData(Commands.CMD_GET_REMOTE_IP_DNS, null);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest() throws IOException {

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

    private void setIpType() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        if (ipv6) {
            dialog.setMessage("Are you sure that you want to change the current host to IPV4?");
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        submitData(Commands.CMD_ENABLE_IPV4_6, "0x01");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            dialog.setMessage("Are you sure that you want to change the current host to IPV6?");
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        submitData(Commands.CMD_ENABLE_IPV4_6, "0x02");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();

    }

    private void reboot() {
        String[] types = {"Cold Reboot", "Warm Reboot"};
        new AlertDialog.Builder(this)
                .setItems(types, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new AlertDialog.Builder(CommandActivity.this)
                                .setTitle("Are you sure you want to perform this action?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        try {
                                            if (i == 0) {
                                                submitData(Commands.CMD_REBOOT_TARGET, "0x01");
                                            } else {
                                                submitData(Commands.CMD_REBOOT_TARGET, "0x02");
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();
                    }
                })
                .show();
    }

    private void setConnectionTimeout() {
        View view = LayoutInflater.from(this).inflate(R.layout.timeout_dialog, null, false);
        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText editText = view.findViewById(R.id.timeout);
                        try {
                            int seconds = Integer.parseInt(editText.getText().toString());
                            if (seconds > 255) {
                                Toast.makeText(usbService, "Timeout cannot be more then 255 second", Toast.LENGTH_SHORT).show();
                            } else {
                                try {
                                    String hex = String.format("%02X", seconds);
                                    submitData(Commands.CMD_CONNECTION_TIMEOUT, hex);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(usbService, "Please Enter Valid Value", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false)
                .show();
    }

    private void sendPortIdRequest() {
        String[] items = {"Get Target Port Number", "Get Host Port Number For Online Event/Alarm (for smart card based contollerd)"
                , "Get Host Port no. for heartbeat", "Get Host Port no. for online event/alerts (for  biometric contollers)"};
        new AlertDialog.Builder(this)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            switch (i) {
                                case 0:
                                    submitData(Commands.CMD_GET_PORT_ID, "0x01");
                                    break;
                                case 1:
                                    submitData(Commands.CMD_GET_PORT_ID, "0x02");
                                    break;
                                case 2:
                                    submitData(Commands.CMD_GET_PORT_ID, "0x03");
                                    break;
                                case 3:
                                    submitData(Commands.CMD_GET_PORT_ID, "0x04");

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                })
                .show();
    }

    private void submitData(String command, String subCommand) throws IOException {
        showDialog();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] SOF = MyUtils.stringTobytes(Constants.SOF);
        outputStream.write(SOF);

        byte[] mac_address = MyUtils.stringTobytes(macAddress);
        outputStream.write(mac_address);

        byte[] o_id = MyUtils.stringTobytes(oID);
        outputStream.write(o_id);

        byte[] commandBytes = MyUtils.stringTobytes(command);
        outputStream.write(commandBytes);

        byte[] employee_id = MyUtils.stringTobytes(employeeId);
        outputStream.write(employee_id);

        String dataLength;
        if (subCommand == null) {
            dataLength = String.format("%04X", 0);
            outputStream.write(MyUtils.stringTobytes(dataLength));
            outputStream.write(MyUtils.stringTobytes(Constants.SOF));
        } else {
            byte[] data = MyUtils.stringTobytes(subCommand);
            dataLength = String.format("%04X", data.length);
            outputStream.write(MyUtils.stringTobytes(dataLength));
            outputStream.write(MyUtils.stringTobytes(Constants.SOF));
            outputStream.write(data);
        }

        byte checksum = MyUtils.generateChecksum(outputStream.toByteArray());
        outputStream.write(checksum);

        usbService.write(outputStream.toByteArray());

        isResponse = true;
        new Handler().postDelayed(() -> {
            if (isResponse) {
                dialog.dismiss();
                Toast.makeText(usbService, "Request Timeout : Please try again", Toast.LENGTH_SHORT).show();
                isResponse = false;
            }
        }, 70000);
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

}
