package com.example.gopal.inventoryrealm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

import static com.example.gopal.inventoryrealm.DriveServiceHelper.MY_PREFS_NAME;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    TextView textView,emptyTextView;
    private final String RESTORED_REALM_FILE_NAME = "/finakyaBackup.realm";
    private final String EXPORT_REALM_FILE_NAME = "/exportedFile.realm";

    private File DOWNLOADS_FOLDER_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    String ASSET_FILE = DOWNLOADS_FOLDER_PATH + RESTORED_REALM_FILE_NAME;
    String ASSET_FILE_1 = DOWNLOADS_FOLDER_PATH + EXPORT_REALM_FILE_NAME;

    Realm realm;
    SharedPreferences sharedPreferences;
    boolean isBackUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // Log.e(TAG,"onCreate");
        EventBus.getDefault().register(this);
        textView = findViewById(R.id.text);
        emptyTextView = findViewById(R.id.empty_view);
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
         isBackUp = prefs.getBoolean("key1",false);
       /* if(isBackUp){
            RealmInitialization.customInitialization(this);
        }else{
            RealmInitialization.defaultInitialization(this);
        }*/
        Realm.init(this);
//        RealmConfiguration defaultConfig = new RealmConfiguration.Builder()
//                .schemaVersion(0)
//              //  .assetFile(ASSET_FILE)
//                .build();
//        Realm.setDefaultConfiguration(defaultConfig);
       realm = Realm.getDefaultInstance();
        String path = realm.getPath();
        Log.e(TAG,"path: " + path);


        //FAB button is clicked
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddAnItemActivity.class);
                startActivity(intent);
            }
        });
        showProduct(new DeleteEventBus());
    }

    @Subscribe(threadMode=ThreadMode.MAIN)
    public void showProduct(DeleteEventBus deleteEventBus){
        RealmResults<Product> results = realm.where(Product.class).findAll();
        if(results.size()== 0) {
            emptyTextView.setVisibility(View.VISIBLE);
            RecyclerView recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new ProductRecyclerApapter(this,results));
        }
        else {
            emptyTextView.setVisibility(View.GONE);
            RecyclerView recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new ProductRecyclerApapter(this,results));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
       // Log.e(TAG,"onResume");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
       // Log.e(TAG,"onDestroy");
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Log.e(TAG,"onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
       // Log.e(TAG,"onStop");
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Log.e(TAG,"onStart");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_all:
                // Delete the first item
                deleteFirstItem();
                EventBus.getDefault().post(new DeleteEventBus());
                break;
            case R.id.back_up_restore:
                startActivity(new Intent(this,BackUpActivity.class));
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteFirstItem(){
        RealmResults<Product> products = realm.where(Product.class).findAll();
        realm.beginTransaction();
        products.deleteFirstFromRealm();
        realm.commitTransaction();
    }

}
