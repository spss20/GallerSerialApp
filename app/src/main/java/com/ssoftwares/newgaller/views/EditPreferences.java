package com.ssoftwares.newgaller.views;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ssoftwares.newgaller.R;
import com.ssoftwares.newgaller.utils.Constants;
import com.ssoftwares.newgaller.utils.MyUtils;

public class EditPreferences extends AppCompatActivity {

    private EditText macAddress;
    private EditText oID;
    private EditText employeeId;
    private RadioGroup ipSelectionGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_preferences);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Preferences");

        macAddress = findViewById(R.id.mac_address);
        oID = findViewById(R.id.oid);
        employeeId = findViewById(R.id.employee_id);
        ipSelectionGroup = findViewById(R.id.ip_selection_group);
        Button save = findViewById(R.id.submit_command);

        Log.v("mac", macAddress.getText().toString());

        SharedPreferences preferences = getSharedPreferences(Constants.prefPath, MODE_PRIVATE);
        String maccy = preferences.getString("mac", null);
        if (maccy != null) {
            macAddress.setText(maccy);
            oID.setText(preferences.getString("oid", null));
            String id = preferences.getString("eId", null);
            int abcd = MyUtils.getHexInteger(MyUtils.stringTobytes(id));
            employeeId.setText(String.valueOf(abcd));
            boolean ipv6 = preferences.getBoolean("ipv6", false);
            if (ipv6)
                ipSelectionGroup.check(R.id.ipv6);
            else ipSelectionGroup.check(R.id.ipv4);
        }

        save.setOnClickListener(view -> {
            if (validateData()) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("mac", macAddress.getText().toString());
                editor.putString("oid", oID.getText().toString());
                String dataLength = String.format("%04X", Integer.parseInt(employeeId.getText().toString()));
                editor.putString("eId", dataLength);
                int id = ipSelectionGroup.getCheckedRadioButtonId();
                if (id == R.id.ipv6) {
                    editor.putBoolean("ipv6", true);
                } else editor.putBoolean("ipv6", false);
                editor.apply();
                Toast.makeText(EditPreferences.this, "Saved! Please Restart App", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    private boolean validateData() {

        byte[] mac_address = MyUtils.stringTobytes(macAddress.getText().toString());

        if (mac_address == null || mac_address.length != 6) {
            macAddress.setError("Enter mac address of correct 6 bytes");
            return false;
        }

        byte[] o_id = MyUtils.stringTobytes(oID.getText().toString());

        if (o_id == null || o_id.length != 1) {
            oID.setError("Enter oid of correct 1 bytes");
            return false;
        }

        if (employeeId.getText().toString().isEmpty()) {
            employeeId.setError("Employee ID cant be empty");
            return false;
        }

        try {
            int id = Integer.parseInt(employeeId.getText().toString());
            if (id > 65025) {
                employeeId.setError("Incorrect Employee ID");
                return false;
            }
        } catch (NumberFormatException e) {
            employeeId.setError("Incorrect Employee ID");
            return false;
        }

        return true;
    }

}
