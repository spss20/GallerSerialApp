package com.ssoftwares.newgaller.views;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ssoftwares.newgaller.UsbService;
import com.ssoftwares.newgaller.utils.Constants;
import com.ssoftwares.newgaller.utils.MyUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Set;

import io.realm.Realm;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    onStatusChange(true);
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    onStatusChange(false);
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    onStatusChange(false);
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    onStatusChange(false);
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    onStatusChange(false);
                    break;
            }
        }
    };

    public UsbService usbService;
    public MyHandler mHandler;
    public String macAddress;
    public String oID;
    public String employeeId;
    public String sap;
    public boolean ipv6;
    private onServiceConnected mInterface;
    public Realm realm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(Constants.prefPath, MODE_PRIVATE);
        macAddress = preferences.getString("mac", null);
        oID = preferences.getString("oid", null);
        employeeId = preferences.getString("eId", null);
        ipv6 = preferences.getBoolean("ipv6", false);
        sap = preferences.getString("sap", null);
//        RealmConfiguration configuration = new RealmConfiguration.Builder()
//                .name("galler.db")
//                .schemaVersion(1)
//                .deleteRealmIfMigrationNeeded()
//                .build();
        realm = Realm.getDefaultInstance();
        realm.getConfiguration().shouldDeleteRealmIfMigrationNeeded();

        Log.v(TAG, "onCreateCalled");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }


    @Override
    protected void onStart() {
        super.onStart();

//            if (maccy != null) {
//                macAddress.setText(maccy);
//                oID.setText();
//                employeeId.setText(preferences.getString("eId", null));
//            } else {
//                startActivity(new Intent(this , SharedPreferences.class));
//            }

    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
            if (usbService.isSerialPortConnected()) {
                onStatusChange(true);
            } else {
                onStatusChange(false);
            }
            if (mInterface != null)
                mInterface.onConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    public class MyHandler extends Handler {
        private final WeakReference<Activity> mActivity;

        public MyHandler(Activity activity) {
            mActivity = new WeakReference<>(activity);
        }


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    Toast.makeText(mActivity.get(), "Message from serial port", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.SYNC_READ:
                    readBuffer(msg.obj);
                    break;
            }
        }
    }

    public abstract void readBuffer(Object object);

    public void checkMacAddress(byte[] mac_address) {
        byte[] existingMac = MyUtils.stringTobytes(macAddress);

        if (Arrays.equals(mac_address, existingMac)) {
            Toast.makeText(usbService, "One of the provided param is incorrect", Toast.LENGTH_SHORT).show();
        } else {
            String mac = MyUtils.hexToString(mac_address).trim();
//
//            File originalFile = new File(getExternalFilesDir("images"), macAddress + ".png");
//            if (originalFile.exists()) {
//                File renamedFile = new File(getExternalFilesDir("images"), mac + ".png");
//                originalFile.renameTo(renamedFile);
//                Log.v("SiteImage", "Renamed Successfully");
//            } else Log.v("SiteImage", "File does not exist");

            getSharedPreferences(Constants.prefPath, MODE_PRIVATE).edit().putString("mac", mac).apply();
            macAddress = mac;

//            Toast.makeText(usbService, "Incorrect mac address. The correct mac address is : " +
//                    mac + " . It is automatically corrected", Toast.LENGTH_LONG).show();
        }
    }

    public abstract void onStatusChange(Boolean status);

    public interface onServiceConnected {
        void onConnected();
    }

    public void setServiceListener(onServiceConnected listener) {
        this.mInterface = listener;
    }
}
