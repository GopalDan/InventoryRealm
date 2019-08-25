package com.example.gopal.inventoryrealm;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Gopal on 8/25/2019.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // The default Realm file is "default.realm" in Context.getFilesDir();
        // we'll change it to "myrealm.realm"
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().name("myrealm.realm").build();
        Realm.setDefaultConfiguration(config);
    }
}
