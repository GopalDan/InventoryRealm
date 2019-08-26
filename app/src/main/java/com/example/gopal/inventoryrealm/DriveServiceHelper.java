package com.example.gopal.inventoryrealm;

/**
 * Created by Gopal on 8/24/2019.
 */


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import static android.content.Context.MODE_PRIVATE;

public class DriveServiceHelper {
    public static final String MY_PREFS_NAME = "key";
    String TAG = "DriveServiceHelper";
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;
    private final String EXPORT_REALM_FILE_NAME = "exportedFile.realm";
    private final String RESTORED_REALM_FILE_NAME = "finakyaBackup.realm";
    private File googleFile;
    private Context context;
    private String folderID;
    private String fileId;
    private String myNumber;
    private Realm realm;
    private java.io.File DOWNLOADS_FOLDER_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private String EXPORT_FILE_PATH;
    private String RESTORE_FILE_PATH;
    private String REALM_DB_PATH = BackUpActivity.REALM_DB_PATH;
    private java.io.File restoredFile;
    private FileContent fileContent;
    // private SharedPreferenceManager sharedPreferenceManager;
   // private DatabaseReference databaseReference;

    public DriveServiceHelper(Drive mDriveService, Context context) {
        this.mDriveService = mDriveService;
        this.context = context;

    }

    Task<String> createFolderTask(GoogleSignInAccount googleSignInAccount) {
        return Tasks.call(mExecutor, new Callable<String>() {
            @Override
            public String call() throws Exception {
                File file = new File()
                        .setParents(Collections.singletonList(context.getResources().getString(R.string.root)))
                        .setMimeType("application/vnd.google-apps.folder")
                        .setName(context.getResources().getString(R.string.Finakya_Folder));

                googleFile = mDriveService.files().create(file).execute();
                if (googleFile == null) {
                    throw new Exception(context.getResources().getString(R.string.No_Folder_Available));
                } else {
                    folderID = googleFile.getId();
                    SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("key3", folderID);
                    editor.apply();
                }



                // sharedPreferenceManager = new SharedPreferenceManager(context);
                // sharedPreferenceManager.saveToPreferences(googleFile.getId(), "BackupFolderId", SharedPreferenceManager.UTIL_STORE);

                return googleFile.getId();
            }
        });
    }

    Task<String> createFileTask(final GoogleSignInAccount googleSignInAccount) {
        return Tasks.call(mExecutor, new Callable<String>() {
            @Override
            public String call() throws Exception {
                File file = new File()
                        .setParents(Collections.singletonList(folderID))
                        .setMimeType("text/plain")
                        .setName("backup.realm");

                //creating local backup
                realm = Realm.getDefaultInstance();
                Log.e(TAG,"Value: " + realm.getPath());
                java.io.File exportRealmFile = null;
                EXPORT_FILE_PATH = DOWNLOADS_FOLDER_PATH + "/" + EXPORT_REALM_FILE_NAME;
                try {

                   // boolean vaue = DOWNLOADS_FOLDER_PATH.mkdirs();
                    // create a backup file
                    exportRealmFile = new java.io.File(DOWNLOADS_FOLDER_PATH, EXPORT_REALM_FILE_NAME);
                    // if backup file already exists, delete it
                    if (exportRealmFile.exists())
                        exportRealmFile.delete();
                    if(Realm.getDefaultConfiguration()!=null){
                   int x =  Realm.getGlobalInstanceCount(Realm.getDefaultConfiguration());
                        // copy current realm to backup file
                        realm.writeCopyTo(exportRealmFile);}
                } catch (Exception e) {
                    e.printStackTrace();
                }
                realm.close();

                //writing to folder
                if (exportRealmFile != null) {
                    fileContent = new FileContent("text/plain", exportRealmFile);
                    googleFile = mDriveService.files().create(file, fileContent).setFields("id").execute();
                }

                if (googleFile == null) {
                    throw new Exception(context.getResources().getString(R.string.No_File_Available));
                } else {
                    fileId = googleFile.getId();
                }
                // sharedPreferenceManager = new SharedPreferenceManager(context);
                // sharedPreferenceManager.saveToPreferences(fileId, "BackupFileId", SharedPreferenceManager.UTIL_STORE);
                // myNumber = sharedPreferenceManager.getStringFromPreferences(SharedPreferenceManager.USER_MOBILE, SharedPreferenceManager.USER_AUTH_STORE);

                // sendDriveDetailsToServer(googleSignInAccount);
                SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putBoolean("key2",true);
                editor.putString("key5", fileId);
                editor.putString("key4", googleSignInAccount.getEmail());
                editor.apply();

                return googleFile.getId();
            }
        });
    }

/*    private void sendDriveDetailsToServer(GoogleSignInAccount googleSignInAccount) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Drivemodel drivemodel = new Drivemodel(myNumber, fileId, folderID, googleSignInAccount.getEmail());
                ServerCallManager serverCallManager = new ServerCallManager(context);
                serverCallManager.postDriveDetails(drivemodel);
            }
        });

        thread.start();
    }*/

    Task<FileList> queryFileTask() {
        return Tasks.call(mExecutor, new Callable<FileList>() {
            @Override
            public FileList call() throws Exception {
                return mDriveService.files().list().setSpaces("drive").execute();
            }
        });
    }

    public Task<Void> deleteFileTask() {
        return Tasks.call(mExecutor, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
               /* sharedPreferenceManager = new SharedPreferenceManager(context);
                folderID = sharedPreferenceManager.getStringFromPreferences("BackupFolderId", SharedPreferenceManager.UTIL_STORE);
                fileId = sharedPreferenceManager.getStringFromPreferences("BackupFileId", SharedPreferenceManager.UTIL_STORE);
*/
                if (fileId != null)
                    mDriveService.files().delete(fileId).execute();
                if (folderID != null)
                    mDriveService.files().delete(folderID).execute();

                return null;
            }
        });
    }

    Task<Void> restoreFileTask(final String idOfFile) {
        return Tasks.call(mExecutor, new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                RESTORE_FILE_PATH = DOWNLOADS_FOLDER_PATH + "/" + RESTORED_REALM_FILE_NAME;
                try {
                    restoredFile = new java.io.File(RESTORE_FILE_PATH);
                    OutputStream outputStream = new FileOutputStream(restoredFile);

                    //Retrieve file metadata
                    mDriveService.files().get(idOfFile).executeMediaAndDownloadTo(outputStream);
                    File fileMetadata = mDriveService.files().get(idOfFile).execute();
                    //long retrieveDataFileSize = fileMetadata.getSize();
                      String name = fileMetadata.getName();
                } catch (FileNotFoundException e) {
                    Log.i("DriveServiceHelper", "File not found" + e.getMessage());
                }

                if (Realm.getDefaultConfiguration() != null) {
                    java.io.File orgFile = new java.io.File(REALM_DB_PATH);         //TODO find out HOW IS THIS WORKING WHEN REALM_DB_PATH IS NOT SET!!
                    orgFile.delete();
                    //  Log.i("DriveServiceHelper", "File Exists: " + orgFile.exists());
                   // Realm.init(context.getApplicationContext());
                   /* KeyManager keyManager = new KeyManager(context.getApplicationContext());
                    byte[] key = keyManager.getRealmKey();

                    RealmConfiguration backUpConfig = new RealmConfiguration.Builder()
                            .encryptionKey(key)
                            .schemaVersion(0)
                            .compactOnLaunch()
                            .directory(DOWNLOADS_FOLDER_PATH)
                            .name("exportedFile.realm")
                            .build();*/

                    SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putBoolean("key1",true);
                    editor.apply();

                    java.io.File backFile = new java.io.File(DOWNLOADS_FOLDER_PATH + "/" + RESTORED_REALM_FILE_NAME);
                    copyFileUsingStream(backFile, orgFile);
                    RealmConfigurationChanged();
                }

                return null;
            }
        });
    }

    private void RealmConfigurationChanged(){
        Realm.init(context);
      //  RealmInitialization.defaultInitialization(context);
        RealmConfiguration backUpConfig = new RealmConfiguration.Builder()
                .schemaVersion(0)
                .compactOnLaunch()
                .directory(DOWNLOADS_FOLDER_PATH)
                .name("exportedFile.realm")
                .build();
        Realm.setDefaultConfiguration(backUpConfig);
    }

    private void copyFileUsingStream(java.io.File source, java.io.File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            is.close();
            os.close();
        }
    }

   /* Task<Pair<String, String>> openFileUsingStorageAccessFramework(ContentResolver contentResolver, Uri uri) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the document's display name from its metadata.
            String name;
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    name = cursor.getString(nameIndex);
                } else {
                    throw new IOException(context.getResources().getString(R.string.Empty_Cursor));
                }
            }

            // Read the document's contents as a String.
            String content;
            try (InputStream is = contentResolver.openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                content = stringBuilder.toString();
            }

            return Pair.create(name, content);
        });
    }*/
}