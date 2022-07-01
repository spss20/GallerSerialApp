package com.ssoftwares.newgaller.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.ssoftwares.newgaller.R;
import com.ssoftwares.newgaller.UsbService;
import com.ssoftwares.newgaller.modals.Device;
import com.ssoftwares.newgaller.utils.Commands;
import com.ssoftwares.newgaller.utils.Constants;
import com.ssoftwares.newgaller.utils.MyUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

import io.realm.Realm;
import io.realm.RealmResults;


public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private Button sendCommand;
    private TextView status;
    private ImageView imageView;
    private TextView deviceDetails;
    private boolean isSecondRequest;
    private String powerOutage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new MyHandler(this);

        sendCommand = findViewById(R.id.send_commands);
        status = findViewById(R.id.status);
        imageView = findViewById(R.id.circular_status_view);
        deviceDetails = findViewById(R.id.device_details_tv);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Serial Communication");

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout
                , toolbar, R.string.open_nav, R.string.close_nav);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View headerView = navigationView.getHeaderView(0);

        TextView profileName = headerView.findViewById(R.id.profile_name);
        TextView profilePhone = headerView.findViewById(R.id.profile_mobile);
        SharedPreferences preferences = getSharedPreferences(Constants.prefPath, MODE_PRIVATE);
        profileName.setText(preferences.getString("name", null));
        profilePhone.setText("+91 " + preferences.getString("mobile", null));

        sendCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CommandActivity.class));
            }
        });

        findViewById(R.id.send_report).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean permission = checkForPermission();
                if (!permission) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Please allow permissions", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                RealmResults<Device> devicesList = realm.where(Device.class).findAll();
                if (devicesList.size() == 0) {
                    Toast.makeText(MainActivity.this, "No device data found", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (devicesList.size() > 1) {
                    ArrayList<String> sapIds = new ArrayList<>();
                    for (Device device : devicesList) {
                        if (DateUtils.isToday(device.getTimestamp())) {
                            sapIds.add(device.getSapId() + "/" +
                                    DateUtils.getRelativeTimeSpanString(device.getTimestamp(), System.currentTimeMillis()
                                            , DateUtils.MINUTE_IN_MILLIS));
                        }
                    }
                    Collections.reverse(sapIds);
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Multiple device's found.")
                            .setItems(sapIds.toArray(new CharSequence[sapIds.size()]), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String sapId = sapIds.get(i).split("/")[0];
                                    sendReport(sapId);
                                }
                            })
                            .show();
                } else sendReport(devicesList.get(0).getSapId());
            }
        });

        findViewById(R.id.reload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceDetails.setText("");
                try {
                    submitData(Commands.CMD_GET_OEM_ID, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void sendReport(String sapId) {
        final AlertDialog[] dialog = new AlertDialog[1];
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                try {
                    Realm realm = Realm.getDefaultInstance();
                    Device device = realm.where(Device.class).equalTo("sapId", strings[0]).findFirst();
                    if (device == null){
                        Log.v(TAG , "Device is null");
                        return null;
                    }
                    SharedPreferences preferences = getSharedPreferences(Constants.prefPath, MODE_PRIVATE);
                    String fileName = strings[0].replaceAll("\\s+", "") + ".pdf";

                    File newFile;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        newFile = new File(getExternalCacheDir() , "Galler");
                    } else {
                        newFile = new File(Environment.getExternalStorageDirectory() + "/Galler");
                    }
                    if (!newFile.exists()) {
                        boolean abcd = newFile.mkdir();
                        if (!abcd) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Error: Cannot make file", Toast.LENGTH_SHORT).show();
                                }
                            });
                            return null;
                        }
                    }
                    File finalName;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        finalName = new File(getExternalCacheDir() , "Galler" +"/" +  fileName);
                    } else {
                        finalName = new File(Environment.getExternalStorageDirectory() + "/Galler" + "/" + fileName);
                    }
                    Document document = new Document();
                    PdfWriter.getInstance(document, new FileOutputStream(finalName));
                    document.open();
                    document.setPageSize(PageSize.A4);
                    document.addCreationDate();
                    document.addAuthor("Sikarwar Softwares");
                    document.addCreator("Surya Pratap");
                    InputStream ims = getAssets().open("logo.jpg");
                    Bitmap bmp = BitmapFactory.decodeStream(ims);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    Image image = Image.getInstance(stream.toByteArray());
                    image.setAlignment(Element.ALIGN_MIDDLE);
                    document.add(image);

                    Font f = new Font(Font.FontFamily.TIMES_ROMAN, 21, Font.UNDERLINE, BaseColor.BLUE);
                    Font f1 = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.NORMAL, BaseColor.BLACK);
                    Font f4 = new Font(Font.FontFamily.TIMES_ROMAN, 25, Font.NORMAL, BaseColor.BLACK);

                    String deviceType = preferences.getString("oid" , "00");
                    String oid;
                    switch (Integer.parseInt(deviceType)){
                        case 1:
                            oid = "DCEM";
                            break;
                        case 2:
                            oid ="RTU";
                            break;
                        case 3:
                            oid = "SNMP";
                            break;
                        case 4:
                            oid = "SAS";
                            break;
                        default: oid = "Unknown";
                    }
                    Paragraph header = new Paragraph(oid + " Site Testing Report", f4);
                    header.setSpacingAfter(20);
                    header.setAlignment(Element.ALIGN_CENTER);
                    document.add(header);
                    document.add(new Paragraph("User Details", f));
                    int employeeId = MyUtils.getHexInteger(MyUtils.stringTobytes(preferences.getString("eId", null)));
                    String userDetails = "Name: " + preferences.getString("name", null) +
                            "\nPhone: +91" + preferences.getString("mobile", null) +
                            "\nEmployee ID: " + employeeId;
                    Paragraph userPara = new Paragraph(userDetails, f1);
                    userPara.setSpacingAfter(10);
                    document.add(userPara);

                    Paragraph sap = new Paragraph("SAP ID: " + preferences.getString("sap", "Unknown"), f);
                    sap.setSpacingAfter(10);
                    document.add(sap);

                    document.add(new Paragraph("Mac Address: " + device.getMacAddress(), f1));
                    Paragraph detailPara = new Paragraph(device.getDeviceDetails(), f1);
                    detailPara.setSpacingAfter(10);
                    document.add(detailPara);

                    Font outageFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.NORMAL);
                    String hardcodedpower;
                    if (device.isPowerReset()) {
                        hardcodedpower = "Total Power Outage: 000.00\nEb Run Time: 000.00\nDg Run Time: 000.00";
                    } else
                        hardcodedpower = "Total Power Outage: Not executed\nEb Run Time: Not executed\nDg Run Time: Not executed";
                    PdfPTable outage = new PdfPTable(new float[]{1, 1});
                    outage.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                    outage.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
                    outage.setTotalWidth(PageSize.A4.getWidth());
                    outage.getDefaultCell().setMinimumHeight(40);
                    outage.getDefaultCell().setPadding(10);
                    outage.setWidthPercentage(100);
                    outage.setSpacingBefore(30);
                    outage.addCell(new Phrase("Power Outage Before", outageFont));
                    outage.addCell(new Phrase("Power Outage After", outageFont));
                    outage.setHeaderRows(1);
                    PdfPCell[] cells = outage.getRow(0).getCells();
                    cells[0].setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cells[1].setBackgroundColor(BaseColor.LIGHT_GRAY);
                    outage.addCell(new Phrase(device.getPowerOutage(), f1));
                    outage.addCell(new Phrase(hardcodedpower, f1));
                    document.add(outage);

                    File file = new File(getExternalFilesDir("images"), sapId + ".png");
                    if (file.exists()) {
                        Uri photoURI = FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".provider", file);

                        int rotation = 0;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            InputStream ims2 = getContentResolver().openInputStream(photoURI);
                            ExifInterface ei = new ExifInterface(ims2);
                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_UNDEFINED);
                            switch (orientation) {
                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    rotation = 90;
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    rotation = 180;
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    rotation = 270;
                                    break;
                            }
                        }
                        InputStream ims3 = getContentResolver().openInputStream(photoURI);
                        Bitmap bmp2 = BitmapFactory.decodeStream(ims3);
                        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                        Bitmap siteImage = scaleDown(bmp2, 700, rotation);
                        siteImage.compress(Bitmap.CompressFormat.PNG, 100, stream2);
                        Image image2 = Image.getInstance(stream2.toByteArray());
                        document.newPage();
                        document.add(new Paragraph("Site Picture", f));
                        document.add(image2);
                    }
                    document.close();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri pdfUri = FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName()
                            + ".provider", finalName);
                    intent.setDataAndType(pdfUri, "application/pdf");
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                    return null;
                } catch (IOException | DocumentException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPreExecute() {
                dialog[0] = new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Generating Report...")
                        .show();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                dialog[0].dismiss();
            }
        }.execute(sapId);

    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, float degrees) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, true);
        if (degrees == 0) {
            return newBitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        Bitmap rotatedBitmap = Bitmap.createBitmap(newBitmap, 0, 0, newBitmap.getWidth(), newBitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }

    private boolean checkForPermission() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

        if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, permissions[1]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, 100);
            return false;
        } else {
            return true;
        }
    }

    private void getOEM(byte[] data) {
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

        deviceDetails.setText("OEM ID: " + oemCompany);
    }

    private void getContollerIp(byte[] data) {
        byte ipvType = data[15];
        if (ipvType == -15) {
            byte[] targetIp = {data[19], data[18], data[17], data[16]};
            byte[] subnetMask = {data[23], data[22], data[21], data[20]};
            byte[] gatewayIp = {data[27], data[26], data[25], data[24]};
            byte[] dnsServer1 = {data[31], data[30], data[29], data[28]};
            byte[] dnsServer2 = {data[35], data[34], data[33], data[32]};

            deviceDetails.append("\nIP Version: IPV4");
            deviceDetails.append("\nTarget IP: " + MyUtils.formatIp(targetIp));
            deviceDetails.append("\nSubnet Mask: " + MyUtils.formatIp(subnetMask));
            deviceDetails.append("\nGateway IP " + MyUtils.formatIp(gatewayIp));
            deviceDetails.append("\nDns Server 1: " + MyUtils.formatIp(dnsServer1));
            deviceDetails.append("\nDns Server 2: " + MyUtils.formatIp(dnsServer2));

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
            int endBit3 = startBit3 + dnsServer2Length;
            byte[] dnsServer2 = new byte[dnsServer2Length];
            System.arraycopy(data, startBit3, dnsServer2, 0, dnsServer2Length);

            deviceDetails.append("\nIP Version: IPV6");
            deviceDetails.append("\nTarget IP: " + new String(targetIpBytes));
            deviceDetails.append("\nSubnet Prefix: " + subnetPrefixByte);
            deviceDetails.append("\nGateway IP " + new String(gatewayIpBytes));
            deviceDetails.append("\nDns Server 1: " + new String(dnsServer1));
            deviceDetails.append("\nDns Server 2: " + new String(dnsServer2));
        }
    }

    private void getFirmwareVersion(byte[] data, int dataLength) {
        byte[] ascii = new byte[dataLength - 1];

        int version_length = data[15];
        int x = 16;
        for (int i = 0; i < version_length; i++) {
            ascii[i] = data[x];
            x++;
        }

        String version = new String(ascii);
        deviceDetails.append("\nFirmware Version: " + version);

    }

    private void commandRtc(byte[] data) {
        byte[] time = {data[18], data[17], data[16], data[15]};
        long millisecond = MyUtils.getHexLong(time) * 1000;
        Log.v("Device TIme", String.valueOf(millisecond));
        Log.v("System Time", String.valueOf(System.currentTimeMillis()));
        long difference = System.currentTimeMillis() - millisecond;
        Log.v("Time Difference", String.valueOf(difference));
        if (Math.abs(difference) > 40000) {
            Toast.makeText(this, "Automatically Correcting Time", Toast.LENGTH_SHORT).show();
            try {
                sendRequest();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millisecond);
        String date = DateFormat.getDateTimeInstance().format(calendar.getTime());


        deviceDetails.append("\nDevice Time: " + date);
        Device device1 = realm.where(Device.class).equalTo("sapId", sap).findFirst();
        if (device1 == null) {
            Device device = new Device(sap , macAddress, deviceDetails.getText().toString(),
                    System.currentTimeMillis());
            device.setoId(oID);
            device.setPowerOutage(powerOutage);
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(device);
            realm.commitTransaction();
        } else {
            realm.beginTransaction();
            device1.setDeviceDetails(deviceDetails.getText().toString());
            device1.setTimestamp(System.currentTimeMillis());
            device1.setMacAddress(macAddress);
            device1.setoId(oID);
            if (!device1.isPowerReset()) {
                device1.setPowerOutage(powerOutage);
            }
            realm.commitTransaction();
        }
    }

    private void getPowerOutage(byte[] data) {
        int totalPowerSize = data[15] & 0xFF;
        byte[] totalPowerOutage = new byte[totalPowerSize];

        int startBit = 16;
        for (int i = 0; i < totalPowerSize; i++) {
            totalPowerOutage[i] = data[startBit];
            startBit++;
        }
        int ebRunSize = data[startBit] & 0xFF;
        byte[] ebRunTime = new byte[ebRunSize];
        startBit++;
        for (int i = 0; i < ebRunSize; i++) {
            ebRunTime[i] = data[startBit];
            startBit++;
        }
        int dgRunSize = data[startBit] & 0xFF;
        byte[] dgRunTime = new byte[dgRunSize];
        startBit++;
        for (int i = 0; i < dgRunSize; i++) {
            dgRunTime[i] = data[startBit];
            startBit++;
        }
        String totalPower = new String(totalPowerOutage);
        String ebRun = new String(ebRunTime);
        String dgRun = new String(dgRunTime);
        powerOutage = "Total Power Outage: " + totalPower + "\nEb Run Time: " + ebRun
                + "\nDg Run Time: " + dgRun;
    }

    @Override
    public void readBuffer(Object object) {
        byte[] data = (byte[]) object;
        try {
            readResponse(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readResponse(byte[] data) throws IOException {

        byte[] mac_address = new byte[6];
        String command;
        int status;

        if (data[0] != Constants.first_byte || data[1] != Constants.second_byte) {
            if (Arrays.equals(data, MyUtils.stringTobytes(Constants.initalString))) {
                usbService.write(MyUtils.stringTobytes(Constants.replyString));
                startActivity(new Intent(this, ConnectionActivity.class));
                finish();
            }
            return;
        }

        System.arraycopy(data, 2, mac_address, 0, 6);

        command = "0x" + Integer.toHexString(data[9] & 0xFF).toUpperCase();

        byte[] lengthBytes = {data[10], data[11]};
        int dataLength = MyUtils.getHexInteger(lengthBytes);

        status = data[12];
        switch (status) {
            case Constants.SUCCESS:
                switch (command) {
                    case Commands.CMD_GET_OEM_ID:
                        submitData(Commands.CMD_GET_CONTROLLER_IP, null);
                        getOEM(data);
                        break;
                    case Commands.CMD_GET_CONTROLLER_IP:
                        submitData(Commands.CMD_GET_FIRMWARE_VER, null);
                        getContollerIp(data);
                        break;
                    case Commands.CMD_GET_FIRMWARE_VER:
                        submitData(Commands.CMD_GET_POWER_OUTAGE, null);
                        getFirmwareVersion(data, dataLength);
                        break;
                    case Commands.CMD_GET_POWER_OUTAGE:
                        submitData(Commands.CMD_GET_RTC, null);
                        getPowerOutage(data);
                        break;
                    case Commands.CMD_GET_RTC:
                        commandRtc(data);
                        break;
                    case Commands.CMD_SET_RTC:
                        if (isSecondRequest) {
                            Toast.makeText(usbService, "Time corrected successfully", Toast.LENGTH_SHORT).show();
                            submitData(Commands.CMD_GET_RTC, null);
                            isSecondRequest = false;
                        } else {
                            try {
                                sendTime();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                }

                break;

            case Constants.FAILED:
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                break;

            case Constants.INCORRECT_PARAMS:
                checkMacAddress(mac_address);
                submitData(Commands.CMD_GET_OEM_ID, null);
                break;
            case Constants.INCORRECT_PASSWORD:
                startActivity(new Intent(this, ConnectionActivity.class));
                finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onStatusChange(Boolean x) {
        if (x) {
            status.setText("Connected to utility");
            imageView.setColorFilter(getColor(android.R.color.holo_green_dark));
            if (!UsbService.IS_DATA_FETCHED) {
                try {
                    deviceDetails.setText("Fetching Information...");
                    submitData(Commands.CMD_GET_OEM_ID, null);
                    UsbService.IS_DATA_FETCHED = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            sendCommand.setEnabled(true);
        } else {
            status.setText("Not Connected");
            imageView.setColorFilter(getColor(android.R.color.holo_red_dark));
            sendCommand.setEnabled(false);
            deviceDetails.setText("No Information Available");
            UsbService.IS_DATA_FETCHED = false;
        }
    }

    private void submitData(String command, String subCommand) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] SOF = MyUtils.stringTobytes(Constants.SOF);
        outputStream.write(SOF);

//        if (macAddress == null){
//            byte[] mac_address = MyUtils.stringTobytes("00 00 00 00 00 00");
//            outputStream.write(mac_address);
//        } else {
            byte[] mac_address = MyUtils.stringTobytes(macAddress);
            outputStream.write(mac_address);
//        }
        byte[] o_id = MyUtils.stringTobytes(oID);
        outputStream.write(o_id);

        byte[] commandBytes = MyUtils.stringTobytes(command);
        outputStream.write(commandBytes);

        byte[] employee_id = MyUtils.stringTobytes(employeeId);
        outputStream.write(employee_id);

        String dataLength;
        if (subCommand == null) {
            dataLength = String.format("%04X", 0);
        } else {
            dataLength = String.format("%04X", 1);
        }
        outputStream.write(MyUtils.stringTobytes(dataLength));
        outputStream.write(MyUtils.stringTobytes(Constants.SOF));
        if (subCommand != null) {
            outputStream.write(MyUtils.stringTobytes(subCommand));
        }
        byte checksum = MyUtils.generateChecksum(outputStream.toByteArray());
        outputStream.write(checksum);
        usbService.write(outputStream.toByteArray());
    }

    private void sendRequest() throws IOException {

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

}
