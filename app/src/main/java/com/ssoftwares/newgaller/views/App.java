package com.ssoftwares.newgaller.views;

import android.app.Application;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .schemaVersion(2)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(configuration);
//        new Instabug.Builder(this, "3028aa45cad36f5fd1c14c9c90c4f387")
//                .build();
    }
}
