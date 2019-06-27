package com.example.gopal.inventoryrealm;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.realm.Realm;

public class EditorActivity extends AppCompatActivity {
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneNoEditText;
    private Button mUpButton;
    private Button mDownButton;
    private TextView mQuantityTextView;
    private int mQuantity;
    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);


        mNameEditText = findViewById(R.id.product_name);
        mPriceEditText = findViewById(R.id.product_price);
        mQuantityTextView = findViewById(R.id.product_quantity);
        mSupplierNameEditText = findViewById(R.id.seller_name);
        mSupplierPhoneNoEditText = findViewById(R.id.seller_phone_number);

        mUpButton = findViewById(R.id.up_button);
        mDownButton = findViewById(R.id.down_button);

        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mQuantity += 1;
                mQuantityTextView.setText(String.valueOf(mQuantity));
            }
        });

        mDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mQuantity>0){
                    mQuantity -= 1;
                }

                if(mQuantity<0){
                    Toast.makeText(EditorActivity.this,"Quantity can't be negative", Toast.LENGTH_SHORT).show();
                }
                else {
                    mQuantityTextView.setText(String.valueOf(mQuantity));
                }
            }
        });

        Realm.init(this);
        realm = Realm.getDefaultInstance();

       setupWithData();
    }

    public void setupWithData(){
        Product product  = realm.where(Product.class).findFirst();
        mQuantity = product.getProductQuantity();
        mNameEditText.setText(product.getProductName());
        mPriceEditText.setText(product.getProductPrice());
        mQuantityTextView.setText(String.valueOf(product.getProductQuantity()));
        mSupplierNameEditText.setText(product.getSupplierName());
        mSupplierPhoneNoEditText.setText(product.getSupplierPhoneNumber());
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
                //update the data in database
                UpdateData();
                finish();
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void UpdateData(){
        //getting values
        String name = mNameEditText.getText().toString().trim();
        String price = mPriceEditText.getText().toString().trim();
        String quantity = mQuantityTextView.getText().toString().trim();
        String supplierName = mSupplierNameEditText.getText().toString().trim();
        String phoneNo = mSupplierPhoneNoEditText.getText().toString().trim();

        Product product = realm.where(Product.class).findFirst();

        realm.beginTransaction();
        product.setProductName(mNameEditText.getText().toString());
        product.setProductPrice(mPriceEditText.getText().toString());
        product.setProductQuantity(Integer.valueOf(mQuantityTextView.getText().toString()));
        product.setSupplierName(mSupplierNameEditText.getText().toString());
        product.setSupplierPhoneNumber(mSupplierPhoneNoEditText.getText().toString());

        realm.commitTransaction();


    }
}
