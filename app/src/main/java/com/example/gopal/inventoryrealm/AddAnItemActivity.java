package com.example.gopal.inventoryrealm;

import android.content.DialogInterface;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import io.realm.Realm;

public class AddAnItemActivity extends AppCompatActivity {
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneNoEditText;
    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_an_item);

        mNameEditText = findViewById(R.id.product_name);
        mPriceEditText = findViewById(R.id.product_price);
        mQuantityEditText = findViewById(R.id.product_quantity);
        mSupplierNameEditText = findViewById(R.id.seller_name);
        mSupplierPhoneNoEditText = findViewById(R.id.seller_phone_number);

        //Initializing Realm
        Realm.init(this);
        realm = Realm.getDefaultInstance();
    }

    public void saveData(){

        realm.beginTransaction();

        Product product = realm.createObject(Product.class);
        product.setProductName(mNameEditText.getText().toString());
        product.setProductPrice(mPriceEditText.getText().toString());
        product.setProductQuantity(Integer.valueOf(mQuantityEditText.getText().toString()));
        product.setSupplierName(mSupplierNameEditText.getText().toString());
        product.setSupplierPhoneNumber(mSupplierPhoneNoEditText.getText().toString());

        realm.commitTransaction();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_button:
                //save data in database & display it as well
                saveData();
                finish();
                break;
            case android.R.id.home:
              onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

   /* public void viewRecord(){
        RealmResults<Student> results = realm.where(Student.class).findAll();

        text.setText("");

        for(Student student : results){
            text.append(student.getRoll_no() + " " + student.getName() + "\n");
        }
    }

    public void updateRecord(){
        RealmResults<Student> results = realm.where(Student.class).equalTo("roll_no", Integer.parseInt(roll_no.getText().toString())).findAll();

        realm.beginTransaction();

        for(Student student : results){
            student.setName(name.getText().toString());
        }

        realm.commitTransaction();
    }

    public void deleteRecord(){
        RealmResults<Student> results = realm.where(Student.class).equalTo("roll_no", Integer.parseInt(roll_no.getText().toString())).findAll();

        realm.beginTransaction();

        results.deleteAllFromRealm();

        realm.commitTransaction();
    }*/


    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }
}
