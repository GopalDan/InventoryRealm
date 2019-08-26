package com.example.gopal.inventoryrealm;

import android.content.Context;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Gopal on 8/25/2019.
 */

public class RealmInitialization {
    private static java.io.File DOWNLOADS_FOLDER_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

    /*public RealmInitialization(Context context){
        Realm.init(context);
    }*/

    public static void defaultInitialization(Context context){
        Realm.init(context);
    }

    public static void customInitialization(Context context){
        Realm.init(context);
        RealmConfiguration backUpConfig = new RealmConfiguration.Builder()
                .schemaVersion(1)
                .compactOnLaunch()
                .directory(DOWNLOADS_FOLDER_PATH)
                .name("exportedFile.realm")
                .build();
        Realm.setDefaultConfiguration(backUpConfig);
    }
}
