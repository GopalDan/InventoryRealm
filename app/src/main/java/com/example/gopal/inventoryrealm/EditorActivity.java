package com.example.gopal.inventoryrealm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.realm.Realm;
import io.realm.RealmResults;

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
    int position;

    /** Boolean flag that keeps track of whether the pet has been edited (true) or not (false) */
    private boolean mItemHasChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

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

        realm = Realm.getDefaultInstance();
        Bundle extras = getIntent().getExtras();
        position = extras.getInt("key1",1);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneNoEditText.setOnTouchListener(mTouchListener);
        mUpButton.setOnTouchListener(mTouchListener);
        mDownButton.setOnTouchListener(mTouchListener);

       setupWithData(position);
    }

    public void setupWithData(int position){
        RealmResults<Product> products = realm.where(Product.class).findAll();
        Product product = products.get(position);
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
                if(!mItemHasChanged){
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
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

        RealmResults<Product> products = realm.where(Product.class).findAll();
        Product product = products.get(position);

        realm.beginTransaction();
        product.setProductName(mNameEditText.getText().toString());
        product.setProductPrice(mPriceEditText.getText().toString());
        product.setProductQuantity(Integer.valueOf(mQuantityTextView.getText().toString()));
        product.setSupplierName(mSupplierNameEditText.getText().toString());
        product.setSupplierPhoneNumber(mSupplierPhoneNoEditText.getText().toString());

        realm.commitTransaction();

    }
    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.save_title);
        builder.setPositiveButton(R.string.positive_save_action, discardButtonClickListener);
        builder.setNegativeButton(R.string.negative_save_action, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        realm.close();
    }
}
