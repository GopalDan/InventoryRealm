package com.example.gopal.inventoryrealm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import io.realm.Realm;

public class AddAnItemActivity extends AppCompatActivity {
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneNoEditText;
    Realm realm;
    boolean shouldBeSaved = false;
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
        setContentView(R.layout.activity_add_an_item);

        mNameEditText = findViewById(R.id.product_name);
        mPriceEditText = findViewById(R.id.product_price);
        mQuantityEditText = findViewById(R.id.product_quantity);
        mSupplierNameEditText = findViewById(R.id.seller_name);
        mSupplierPhoneNoEditText = findViewById(R.id.seller_phone_number);

        //Initializing Realm
        realm = Realm.getDefaultInstance();

         /* Setup OnTouchListeners on all the input fields, so we can determine if the user
         has touched or modified them. This will let us know if there are unsaved changes
         or not, if the user tries to leave the editor without saving.*/

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneNoEditText.setOnTouchListener(mTouchListener);
    }

    public void saveData(){
        String productName = mNameEditText.getText().toString();
        String productPrice = mPriceEditText.getText().toString();
        String productQuantity = mQuantityEditText.getText().toString();
        String supplierName = mSupplierNameEditText.getText().toString();
        String supplierPhoneNumber = mSupplierPhoneNoEditText.getText().toString();

        if (productName.isEmpty()){
            Toast.makeText(this,"Enter product name",Toast.LENGTH_SHORT).show();
            return;
        }if(productPrice.isEmpty()){
            Toast.makeText(this,"Enter product price",Toast.LENGTH_SHORT).show();return;
        }if(productQuantity.isEmpty() ){
            Toast.makeText(this,"Enter product quantity",Toast.LENGTH_SHORT).show();return;
        }if(supplierName.isEmpty()){
            Toast.makeText(this,"Enter seller name",Toast.LENGTH_SHORT).show();return;
        }if(supplierPhoneNumber.isEmpty()){
            Toast.makeText(this,"Enter seller phone number",Toast.LENGTH_SHORT).show();return;
        }

        realm.beginTransaction();

        Product product = realm.createObject(Product.class);
        product.setProductName(mNameEditText.getText().toString());
        product.setProductPrice(mPriceEditText.getText().toString());
        product.setProductQuantity(Integer.valueOf(mQuantityEditText.getText().toString()));
        product.setSupplierName(mSupplierNameEditText.getText().toString());
        product.setSupplierPhoneNumber(mSupplierPhoneNoEditText.getText().toString());
        realm.commitTransaction();
        shouldBeSaved = true;
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
                if (shouldBeSaved) {
                    EventBus.getDefault().post(new DeleteEventBus());
                    finish();

                }
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
    protected void onStop() {
        super.onStop();
        realm.close();
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

}
