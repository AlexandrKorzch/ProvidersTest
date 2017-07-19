package com.korzh.user.fileprovidertest;

import android.content.ContentResolver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*        завдання
        1) зчитайте данні з системного контентпровайдера (e.g. Calls / Contacts / Photos)
        2) спробуйте створити свій. можна заімплементити лише пару операцій: insert, query. під капотом це не обов‘язково має бути sqlite. можна навіть константи повертати для тесту
        3) використайте FileProvider щоб розшарити файл*/

        getAllProviders();
        getContacts();
        loadPhotosFromPhone();
        getMusic();
        getMusic2();
        getCalls();
    }

    private void getMusic2() {
        ContentResolver cr = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cur = cr.query(uri, null, selection, null, sortOrder);
        int count = 0;

        if(cur != null)
        {
            count = cur.getCount();

            if(count > 0)
            {
                while(cur.moveToNext())
                {
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

        while(cursor.moveToNext()) {
            songs.add(cursor.getString(0) + "||"
                    + cursor.getString(1) + "||"
                    + cursor.getString(2) + "||"
                    + cursor.getString(3) + "||"
                    + cursor.getString(4) + "||"
                    + cursor.getString(5));
        }

        int length = songs.size();
    }

    private void getAllProviders() {
        for (PackageInfo pack : getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS)) {
            ProviderInfo[] providers = pack.providers;
            if (providers != null) {
                for (ProviderInfo provider : providers) {
                    Log.d(TAG, "provider: " + provider.authority);
                }
            }
        }
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
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));

                        Log.d(TAG, "onCreate: "+"Name: " + name
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

        for (data.moveToLast(); !data.isBeforeFirst(); data.moveToPrevious()){
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
}
