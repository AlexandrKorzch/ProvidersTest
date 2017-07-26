package com.korzh.user.fileprovidertest;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.korzh.user.fileprovidertest.adapter.MyAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = "MainActivity";
    private static final int READ_CONTACTS_AND_STORAGE = 456;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_CALL_LOG,

            }, READ_CONTACTS_AND_STORAGE);
        } else {
            startLogic();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_CONTACTS_AND_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLogic();
                }
            }
        }
    }

    private void startLogic() {
        initRecyclerView();
        intiButtons();
        showAllProviders();
    }

    private void showAllProviders() {
        List<String> dataList = new ArrayList<>();
        for (PackageInfo pack : getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS)) {
            ProviderInfo[] providers = pack.providers;
            if (providers != null) {
                for (ProviderInfo provider : providers) {
                    dataList.add(provider.authority);
                }
            }
        }
        String[] dataArray = listToArray(dataList);
        showData(dataArray);
    }


    private void showCalls() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Cursor managedCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        assert managedCursor != null;
        List<String> calls = new ArrayList<>();
        int number = managedCursor.getColumnIndex( CallLog.Calls.NUMBER );
        int type = managedCursor.getColumnIndex( CallLog.Calls.TYPE );

        while (managedCursor.moveToNext() ) {
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String dir = null;
            int dircode = Integer.parseInt( callType );
            switch( dircode ) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            calls.add(dir+ " "+ phNumber);
        }
        managedCursor.close();

        showData(listToArray(calls));
    }


    private void showContacts() {
        List<String> contacts = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                StringBuilder contact = new StringBuilder();
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                contact.append(name);
                contact.append(": ");

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));

                        contact.append(phoneNo);
                        contact.append(" ");
                        contacts.add(contact.toString());
                    }
                    pCur.close();
                }
            }
        }
        showData(listToArray(contacts));
    }


    private void showPicturesList() {
        final String[] projection = { MediaStore.Images.Media.DATA };
        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        final String[] selectionArgs = { CAMERA_IMAGE_BUCKET_ID };
        final Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);
        parceAndShowPictures(cursor);
    }

    private void parceAndShowPictures(Cursor cursor) {
        ArrayList<String> result = new ArrayList<>(cursor.getCount());
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                final String data = cursor.getString(dataColumn);
                Log.d(TAG, "parceAndShowPictures: data - "+data);
                result.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        showData(listToArray(result));
    }

    private static final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera";
    private static final String CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME);
    private static String getBucketId(String path) {return String.valueOf(path.toLowerCase().hashCode());}


    private void showMyData(){
//        final Uri CONTACT_URI = Uri.parse("content://microsoft.aspnet.signalr.provider/hello");
//        Cursor cursor = getContentResolver().query(CONTACT_URI, null, null,null, null);
//        parceAndShowPictures(cursor);
//        cursor.close();


        final Uri CONTACT_URI = Uri.parse("content://com.example.myapp.fileprovider/IMG_20170724_202742.jpg");
        Cursor cursor = getContentResolver().query(CONTACT_URI, null, null,null, null);
        Log.d(TAG, "showMyData: cursor = "+cursor);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> resolvedIntentActivities = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
            String packageName = resolvedIntentInfo.activityInfo.packageName;
            grantUriPermission(packageName, CONTACT_URI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

//        context.revokeUriPermissionfileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);


    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    private void intiButtons() {
        Button btnCalls = (Button) findViewById(R.id.btn_calls);
        Button btnContacts = (Button) findViewById(R.id.btn_contacts);
        Button btnPhotos = (Button) findViewById(R.id.btn_photos);
        Button btnMyProvider = (Button) findViewById(R.id.btn_provider);
        btnCalls.setText("Calls");
        btnContacts.setText("Contacts");
        btnPhotos.setText("Photos");
        btnMyProvider.setText("MyProvider");

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        switch(view.getId()){
                            case R.id.btn_calls:showCalls();break;
                            case R.id.btn_contacts: showContacts();break;
                            case R.id.btn_photos: showPicturesList();break;
                            case R.id.btn_provider: showMyData();
                        }
                    }
                }).start();
            }
        };

        btnCalls.setOnClickListener(clickListener);
        btnContacts.setOnClickListener(clickListener);
        btnPhotos.setOnClickListener(clickListener);
        btnMyProvider.setOnClickListener(clickListener);
    }

    private void showData(final String[] data) {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                MyAdapter adapter = new MyAdapter(data);
                mRecyclerView.setAdapter(adapter);
            }
        });
    }

    @NonNull
    private String[] listToArray(List<String> dataList) {
        String[] dataArray = new String[dataList.size()];
        dataArray = dataList.toArray(dataArray);
        return dataArray;
    }

}
