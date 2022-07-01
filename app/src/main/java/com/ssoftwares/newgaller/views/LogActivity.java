package com.ssoftwares.newgaller.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ssoftwares.newgaller.R;
import com.ssoftwares.newgaller.modals.CommandLog;
import com.ssoftwares.newgaller.modals.UserLog;

import java.text.DateFormat;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class LogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        getSupportActionBar().setTitle("Your Logs");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Realm realm = Realm.getDefaultInstance();
        RealmResults<CommandLog> userLogs = realm.where(CommandLog.class).sort("timestamp" , Sort.DESCENDING).findAll();

        ArrayList<String> myArray = new ArrayList<>();
        for (CommandLog log: userLogs){
            myArray.add(log.getType() + " : " + log.getHex() + " \nTime: " +
                    DateUtils.getRelativeTimeSpanString(log.getTimestamp() , System.currentTimeMillis() ,
                            DateUtils.MINUTE_IN_MILLIS));
        }

        ListView listView = findViewById(R.id.log_listview);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this ,
                android.R.layout.simple_list_item_1 , myArray);
        listView.setAdapter(arrayAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
