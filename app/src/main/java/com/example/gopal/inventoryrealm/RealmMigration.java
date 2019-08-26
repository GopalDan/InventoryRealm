package com.example.gopal.inventoryrealm;

import io.realm.DynamicRealm;

/**
 * Created by Gopal on 8/26/2019.
 */

public class RealmMigration implements io.realm.RealmMigration{
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        if(oldVersion == 0){
            // Migrate to a new version
            //RealmInitialization.customInitialization(this);
            oldVersion++;
        }
        if(oldVersion == 1){
            // Migrate to a new version
            oldVersion++;
        }
    }
}
