package com.example.gopal.inventoryrealm;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import java.io.File;
import java.util.Collections;
import java.util.Objects;

import io.realm.Realm;

import static com.example.gopal.inventoryrealm.DriveServiceHelper.MY_PREFS_NAME;

public class BackUpActivity extends AppCompatActivity {
    private static final int REQUEST_SIGN_IN = 1;
    private static final String NO_NETWORK = "no_network";
    private static final String FILEID_NOT_FOUND = "field";
    private final String TAG = "BackupActivity";
    private Context context;
    private Realm realm;
    private File DOWNLOADS_FOLDER_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    // private SharedPreferenceManager sharedPreferenceManager;
    private String lastBackupDate;
    private boolean hasBackedUp;
    private boolean isBackUp = false;
    private boolean isRestore = false;
    private GoogleSignInClient googleSignInClient;
    private DriveServiceHelper driveServiceHelper;              //This is helper class which does the actual work of uploading and downloading and can be found in the same package
    private GoogleSignInAccount signInAccount;
    private String backupEmail = "";
    private String fileId;
    private ProgressDialog progressDialog;
    //  private ServerCallManager serverCallManager;
    public static String REALM_DB_PATH ;
    RelativeLayout rootLayout;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_up);
        context = this.getApplicationContext();
        realm = Realm.getDefaultInstance();
        // EventBus.getDefault().register(this);

        REALM_DB_PATH = realm.getPath();
        rootLayout = findViewById(R.id.rootView);
        Button backUp = findViewById(R.id.back_up);
        Button restore = findViewById(R.id.restore);
        backUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backUpData();
            }
        });
        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restoreData();

            }
        });
        progressDialog = new ProgressDialog(this);

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        hasBackedUp = prefs.getBoolean("key2",true);



        //   sharedPreferenceManager = new SharedPreferenceManager(context);
        //   lastBackupDate = sharedPreferenceManager.getStringFromPreferences("LastBackupDate", SharedPreferenceManager.UTIL_STORE);

      /*  if (!lastBackupDate.equalsIgnoreCase("None"))
            textViewBackupDate.setText(lastBackupDate);
        else
            textViewBackupDate.setText(R.string.never);

        hasBackedUp = sharedPreferenceManager.getBooleanFromPreferences("BackedUp", SharedPreferenceManager.UTIL_STORE);
        progressDialog = new ProgressDialog(BackupActivity.this);

        serverCallManager = new ServerCallManager(this);*/
    }

    public void backUpData() {
      /*  backUpButton.setMode(ActionProcessButton.Mode.ENDLESS);
        backUpButton.setProgress(1);*/
        // First check if we have storage permissions
        checkStoragePermissions(this);
        isBackUp = true;
        requestSignIn();
    }

    private void checkStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public void restoreData() {
        if (hasBackedUp) {
            isRestore = true;
            requestSignIn();
        } else {
            Toast.makeText(context, R.string.No_backup_available_to_restore, Toast.LENGTH_SHORT).show();
        }
    }

    private void requestSignIn() {
        //This method opens the google account sign up dialog to choose which account to backup or restore from

        /*
            Method creates a googleSignInClient with emailId chosen by user and passes it on.
         */

        Log.i(TAG, "Signing in");

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SIGN_IN:
                if (resultCode == RESULT_OK && data != null) {
                    Log.i(TAG, "Handling Signing in intent");
                    handleSignInIntent(data);
                }
                break;
        }
    }

    private void handleSignInIntent(Intent data) {
        //This method opens the Allow/Deny dialog for first time users for permission checking purposes

        /*
            The intent object received in method parameter contains the email id which the user has chosen along with other details.
            This is required to create a drive service object which is then used by the custom DriveServiceHelper class's instance object to be used by the user's google drive
         */

        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(final GoogleSignInAccount googleSignInAccount) {
                        signInAccount = googleSignInAccount;
                        GoogleAccountCredential googleAccountCredential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_FILE));
                        googleAccountCredential.setSelectedAccount(googleSignInAccount.getAccount());
                        Drive googleDriveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
                                new GsonFactory(),
                                googleAccountCredential)
                                .setApplicationName(getResources().getString(R.string.app_name))
                                .build();

                        driveServiceHelper = new DriveServiceHelper(googleDriveService, getApplicationContext());

                        if (isBackUp)
                            createFolder();
                        else if (isRestore) {
                            /*If the user is restoring backup from a previous file
                               check if the user has selected the same email as the backup email stored in server database and proceed accordingly
                             */
                            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                            backupEmail = prefs.getString("key4","gt");
                            fileId = prefs.getString("key5","hdj");
                            RestoreFileFoundEvent restoreFileFoundEvent = new RestoreFileFoundEvent();
                            restoreFileFoundEvent.setGoogleSignInAccount(googleSignInAccount);
                            restoreFile(restoreFileFoundEvent);

                          /*  Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                                    *//* backupEmail = serverCallManager.fetchBackupEmail();
                                       fileId = serverCallManager.fetchDriveFileId();*//*
                                    backupEmail = prefs.getString("key4","gt");
                                    fileId = prefs.getString("key5","hdj");

                                    RestoreFileFoundEvent restoreFileFoundEvent = new RestoreFileFoundEvent();
                                    restoreFileFoundEvent.setGoogleSignInAccount(googleSignInAccount);
                                    // EventBus.getDefault().post(restoreFileFoundEvent);
                                    restoreFile(restoreFileFoundEvent);
                                }
                            });
                            thread.start();*/
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "generic_sign_in_failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void restoreFile(RestoreFileFoundEvent restoreFileFoundEvent) {
        if (fileId != null && !fileId.equalsIgnoreCase("") && !fileId.equalsIgnoreCase(NO_NETWORK)
                && !fileId.equalsIgnoreCase(FILEID_NOT_FOUND) && !fileId.equalsIgnoreCase("-1")) {
            if (Objects.requireNonNull(restoreFileFoundEvent.getGoogleSignInAccount().getEmail()).equalsIgnoreCase(backupEmail)) {
                queryForRestore();
            } else {
                Snackbar snackbar = Snackbar.make(rootLayout,"Please_select_proper_backup_email_to_restore_from", Snackbar.LENGTH_LONG);
                snackbar.show();
                googleSignInClient.revokeAccess();
            }
        } else {
          /*  getParent().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, "no_backup_found", Toast.LENGTH_SHORT).show();
                }
            });*/
           Toast.makeText(context, "no_backup_found", Toast.LENGTH_SHORT).show();
        }
        // googleSignInClient.revokeAccess();
    }

    private void queryForRestore() {
        progressDialog.setMessage(getResources().getString(R.string.Restoring_Database_Please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();
        driveServiceHelper.queryFileTask()
                .addOnSuccessListener(new OnSuccessListener<FileList>() {
                    @Override
                    public void onSuccess(FileList fileList) {
                        StringBuilder stringBuilder = new StringBuilder();
                        //String builder to build the file name string
                        for (com.google.api.services.drive.model.File file : fileList.getFiles()) {
                            stringBuilder.append(file.getName()).append("\n");
                        }

                        //if string is not equal to blank it contains a file name and can be restored
                        if (!stringBuilder.toString().equalsIgnoreCase(" ")) {

                            //got the file id from shared preference and send it to DriveHelper to restore backup
                            driveServiceHelper.restoreFileTask(fileId)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(context, "Restored", Toast.LENGTH_SHORT).show();
                                            isRestore = false;
                                            progressDialog.dismiss();
                                            startActivity(new Intent(context,MainActivity.class));

                                        }
                                    });
                        } else {
                            Toast.makeText(context, "No_backups_available", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void createFolder() {
        progressDialog.setMessage(getResources().getString(R.string.Backing_up_Please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();

        driveServiceHelper.queryFileTask()
                .addOnSuccessListener(new OnSuccessListener<FileList>() {
                    @Override
                    public void onSuccess(FileList fileList) {
                        Log.e(TAG, "QueryFileTask is successful");

                        //String builder to build the file name string
                        StringBuilder stringBuilder = new StringBuilder();
                        for (com.google.api.services.drive.model.File file : fileList.getFiles()) {
                            stringBuilder.append(file.getName()).append("\n");
                        }
                        Log.e(TAG, "File Name:" + stringBuilder.toString());

                        //if string is not equal to blank it contains a file already and we need to delete it first
                        if (!stringBuilder.toString().equalsIgnoreCase(" ")) {
                            //call th
                            driveServiceHelper.deleteFileTask()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.e(TAG, "Already existing File gets deleted");
                                            driveServiceHelper.createFolderTask(signInAccount)
                                                    .addOnCompleteListener(new OnCompleteListener<String>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<String> task) {
                                                            Log.e(TAG, "Created New Folder");
                                                            if (task.isSuccessful())
                                                                createFileInFolder();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e(TAG, "Created New Folder Failed  " + e.getMessage() + "   /  " + e.getCause());
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "Already existing file not deleted");
                                        }
                                    });
                        } else {
                             Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                          Toast.makeText(context, "generic_failed", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "QueryFileTask is Failed: " + e.getMessage());
                        progressDialog.dismiss();
                    }
                });
    }

    private void createFileInFolder() {

        driveServiceHelper.createFileTask(signInAccount)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Log.e(TAG, "Create File Inside Folder Success");

                    }
                })
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        Log.e(TAG, "Create File Inside Folder Completed");
                         Toast.makeText(context, "Backed_Up", Toast.LENGTH_SHORT).show();
                        hasBackedUp = true;
                        //backUpButton.setProgress(100);
//                        SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager(context);
//                        sharedPreferenceManager.saveToPreferences(hasBackedUp, "BackedUp", SharedPreferenceManager.UTIL_STORE);
//                        sharedPreferenceManager.saveToPreferences(DateTimeManager.getCurrentDateAsString(), "LastBackupDate", SharedPreferenceManager.UTIL_STORE);
//
//                        textViewBackupDate.setText(DateTimeManager.getCurrentDateAsString());
//                        Drivemodel drivemodel = new Drivemodel(sharedPreferenceManager.getStringFromPreferences(SharedPreferenceManager.USER_MOBILE,SharedPreferenceManager.USER_AUTH_STORE),sharedPreferenceManager.getStringFromPreferences("BackupFileId", SharedPreferenceManager.UTIL_STORE),sharedPreferenceManager.getStringFromPreferences("BackupFolderId", SharedPreferenceManager.UTIL_STORE),sharedPreferenceManager.getStringFromPreferences("BackupEmail", SharedPreferenceManager.UTIL_STORE));

                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Create File Inside Folder Failed" + e.getMessage() + "  /  " + e.getCause());
                        isBackUp = false;
                        googleSignInClient.revokeAccess();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
       // realm.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
      //  realm.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // EventBus.getDefault().unregister(this);
    }

}
