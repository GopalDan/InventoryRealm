package com.example.gopal.inventoryrealm;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text);

        //FAB button is clicked
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddAnItemActivity.class);
                startActivity(intent);
            }
        });
        //Initializing Realm
        Realm.init(this);
        realm = Realm.getDefaultInstance();


        showProduct();
    }

    public void showProduct(){
        RealmResults<Product> results = realm.where(Product.class).findAll();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ProductRecyclerApapter(this,results));
    }

    @Override
    protected void onResume() {
        showProduct();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.details:
                startActivity(new Intent(this, DetailsActivity.class));break;
            case R.id.delete_all:
                // Delete the first item
                deleteFirstItem();
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
