package com.example.gopal.inventoryrealm;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmResults;

public class DetailsActivity extends AppCompatActivity {
    private TextView mNameTextView;
    private TextView mNameTextViewHeader;
    private TextView mPriceTextView;
    private TextView mPriceTextViewHeader;
    private TextView mQuantityTextView;
    private TextView mQuantityTextViewHeader;
    private TextView mSupplierNameTextView;
    private TextView mSupplierNameTextViewHeader;
    private TextView mSupplierContactTextView;
    private TextView mSupplierContactTextViewHeader;
    private Uri mCurrentItemUri;
    private String mSupplierPhoneNumber;
    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mNameTextView = findViewById(R.id.product_name);
        mNameTextViewHeader = findViewById(R.id.product_name_header);
        mPriceTextView = findViewById(R.id.product_price);
        mPriceTextViewHeader = findViewById(R.id.product_price_header);
        mQuantityTextView = findViewById(R.id.product_quantity);
        mQuantityTextViewHeader = findViewById(R.id.product_quantity_header);
        mSupplierNameTextView = findViewById(R.id.seller_name);
        mSupplierNameTextViewHeader = findViewById(R.id.seller_name_header);
        mSupplierContactTextView = findViewById(R.id.seller_phone_no);
        mSupplierContactTextViewHeader = findViewById(R.id.seller_phone_no_header);

        // Call button is clicked
        ImageView imageView = findViewById(R.id.call);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callSupplier();
            }
        });

        realm = Realm.getDefaultInstance();

        setUpData();
    }

    public void setUpData(){

        Product product  = realm.where(Product.class).findFirst();
        mSupplierPhoneNumber = product.getSupplierPhoneNumber();
        //Setting the value
        mNameTextView.setText(product.getProductName());
        mNameTextViewHeader.setText(R.string.product_name_header);
        mPriceTextView.setText(product.getProductPrice());
        mPriceTextViewHeader.setText(R.string.product_price);
        mQuantityTextView.setText(String.valueOf(product.getProductQuantity()));
        mQuantityTextViewHeader.setText(R.string.product_quantity);
        mSupplierNameTextView.setText(product.getSupplierName());
        mSupplierNameTextViewHeader.setText(R.string.supplier_name);
        mSupplierContactTextView.setText(product.getSupplierPhoneNumber());
        mSupplierContactTextViewHeader.setText(R.string.supplier_phone_no_header);



    }

    // calling phone to supplier
    public void callSupplier(){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + mSupplierPhoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.edit:
                Intent intent = new Intent(this,EditorActivity.class);
                startActivity(intent);
                break;
            case R.id.delete:
                // Do delete operation
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
