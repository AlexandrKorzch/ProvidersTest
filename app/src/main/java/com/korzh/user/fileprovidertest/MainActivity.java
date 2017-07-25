package com.korzh.user.fileprovidertest;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.korzh.user.fileprovidertest.adapter.MyAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MainActivity";
    private static final int READ_CONTACTS_AND_STORAGE = 456;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();




/*        завдання
        1) зчитайте данні з системного контентпровайдера (e.g. Calls / Contacts / Photos)
        2) спробуйте створити свій. можна заімплементити лише пару операцій: insert, query. під капотом це не обов‘язково має бути sqlite. можна навіть константи повертати для тесту
        3) використайте FileProvider щоб розшарити файл*/


//        getContacts();
//        loadPhotosFromPhone();
//        getMusic();
//        getMusic2();
//        getCalls();
    }

    private void requestPermissions() {
        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED

                ) {

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
                for (int i = 0; i < providers.length; i++) {
                    dataList.add(providers[i].authority);
                }
            }
        }
        String[] dataArray = listToArray(dataList);
        showData(dataArray);
    }



    private void getCallDetails() {
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






    private void getMusic2() {
        ContentResolver cr = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cur = cr.query(uri, null, selection, null, sortOrder);
        int count = 0;

        if (cur != null) {
            count = cur.getCount();

            if (count > 0) {
                while (cur.moveToNext()) {
                    String data = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                    // Add code to get more column here

                    // Save to your list here
                }

            }
        }

        cur.close();
    }

    private void getMusic() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };

        Cursor cursor = this.managedQuery(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);

        List<String> songs = new ArrayList<String>();

        while (cursor.moveToNext()) {
            songs.add(cursor.getString(0) + "||"
                    + cursor.getString(1) + "||"
                    + cursor.getString(2) + "||"
                    + cursor.getString(3) + "||"
                    + cursor.getString(4) + "||"
                    + cursor.getString(5));
        }

        int length = songs.size();
    }


    private void getContacts() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

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

                        Log.d(TAG, "onCreate: " + "Name: " + name
                                + ", Phone No: " + phoneNo);

                    }
                    pCur.close();
                }
            }
        }
    }


    private void loadPhotosFromPhone() {
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {MediaStore.Images.Media.DATA};
        return new CursorLoader(this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        int count = data.getCount();

        ArrayList<String> alstPhotos = new ArrayList<>();

        for (data.moveToLast(); !data.isBeforeFirst(); data.moveToPrevious()) {
            String photoPath = data.getString(0);
            alstPhotos.add(photoPath);
        }

        int size = alstPhotos.size();

        // Use alstPhotos
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void getCalls() {

        //// TODO: 19.07.17
    }


    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    private void intiButtons() {
        Button calls = (Button) findViewById(R.id.btn_calls);
        Button contacts = (Button) findViewById(R.id.btn_contacts);
        Button photos = (Button) findViewById(R.id.btn_photos);
        calls.setText("Calls");
        contacts.setText("Contacts");
        photos.setText("Photos");

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(view.getId()){
                    case R.id.btn_calls:getCallDetails();break;

                    case R.id.btn_contacts: {

                        break;
                    }
                    case R.id.btn_photos: {

                    }
                }

            }
        };

        calls.setOnClickListener(clickListener);
        contacts.setOnClickListener(clickListener);
        photos.setOnClickListener(clickListener);


        // TODO: 7/25/17  
        calls.setText("Calls");

        calls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCallDetails();
            }
        });
    }

    private void showData(String[] data) {
        MyAdapter adapter = new MyAdapter(data);
        mRecyclerView.setAdapter(adapter);
    }


    @NonNull
    private String[] listToArray(List<String> dataList) {
        String[] dataArray = new String[dataList.size()];
        dataArray = dataList.toArray(dataArray);
        return dataArray;
    }

}
