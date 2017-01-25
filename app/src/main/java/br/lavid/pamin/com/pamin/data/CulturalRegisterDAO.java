package br.lavid.pamin.com.pamin.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Calendar;
import java.util.LinkedList;

import br.lavid.pamin.com.pamin.data.PaminDataContract.CulturalRegisterEntry;
import br.lavid.pamin.com.pamin.models.CloudnaryPicture;
import br.lavid.pamin.com.pamin.models.CulturalRegister;

/**
 * Created by araujojordan on 22/06/15.
 */
public class CulturalRegisterDAO {

    public static String strSeparator = "__,__";
    private PaminDbHelper paminDbHelper;
    private final Object lock = new Object();

    public CulturalRegisterDAO(Context ctx) {
        paminDbHelper = new PaminDbHelper(ctx);
    }

    public static String convertArrayToString(String[] array) {
        String str = "";
        for (int i = 0; i < array.length; i++) {
            str = str + array[i];
            // Do not append comma at the end of last element
            if (i < array.length - 1) {
                str = str + strSeparator;
            }
        }
        return str;
    }

    public static String[] convertStringToArray(String str) {
        try {
            return str.split(strSeparator);
        } catch (Exception error) {
            return new String[0];
        }
    }

    public CulturalRegister load(int id) {

        SQLiteDatabase db = paminDbHelper.getReadableDatabase();

        Cursor c = db.query(
                CulturalRegisterEntry.TABLE_NAME,
                null,
                CulturalRegisterEntry.COLUMN_NAME_ID + " = ? ",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();

        if (c.moveToFirst()) {
            startDate.setTimeInMillis(c.getLong(5));
            endDate.setTimeInMillis(c.getLong(6));

            CulturalRegister cultReg = new CulturalRegister(
                    c.getInt(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    null,
                    startDate,
                    endDate,
                    c.getDouble(7),
                    c.getDouble(8),
                    c.getDouble(9),
                    c.getString(10),
                    c.getString(11)
            );

            db.close();
            return cultReg;
        }

        db.close();
        return null;
    }

    public void update(CulturalRegister cult_reg) {
        SQLiteDatabase db = paminDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CulturalRegisterEntry.COLUMN_NAME_TITLE, cult_reg.getTitle());
        values.put(CulturalRegisterEntry.COLUMN_NAME_DESCRIPTION, cult_reg.getDescription());
        values.put(CulturalRegisterEntry.COLUMN_NAME_WHERE, cult_reg.getWhere());

        if (cult_reg.getStartDate() != null)
            values.put(CulturalRegisterEntry.COLUMN_NAME_START_DATE, cult_reg.getStartDate().getTimeInMillis());
        if (cult_reg.getEndDate() != null)
            values.put(CulturalRegisterEntry.COLUMN_NAME_END_DATE, cult_reg.getEndDate().getTimeInMillis());

        values.put(CulturalRegisterEntry.COLUMN_NAME_CATEGORY, cult_reg.getCategory());
        values.put(CulturalRegisterEntry.COLUMN_NAME_PROMOTER, cult_reg.getPromoter());
        values.put(CulturalRegisterEntry.COLUMN_NAME_PROMOTER_CONTACT, cult_reg.getPromoterContact());
        values.put(CulturalRegisterEntry.COLUMN_NAME_LATITUDE, cult_reg.getLatitude());
        values.put(CulturalRegisterEntry.COLUMN_NAME_LONGITUDE, cult_reg.getLongitude());
        values.put(CulturalRegisterEntry.COLUMN_NAME_PRICE, cult_reg.getPrice());

        //String[] picturesAndVids = new String[cult_reg.getPictures().size()];
        String picturesAndVids = "";
        //int i = 0;
        //for (CloudnaryPicture cp : cult_reg.getPictures()) {
        CloudnaryPicture cp = null;

        if(cult_reg.getPictures() != null && cult_reg.getPictures().isEmpty()) {
            try {
                synchronized (lock) {
                    cp = cult_reg.getPictures().getFirst();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (cp == null || cp.getCompleteUrl() == null || cp.getCompleteUrl().isEmpty()){}
                //continue;
        else {
            Log.v("CrDAO", "Saving picORvid" + ": " + cp.getCompleteUrl());
            //Log.v("CrDAO", "Saving picORvid" + i + ": " + cp.getCompleteUrl());
            picturesAndVids = cp.getCompleteUrl();
        }
            //i++;
        //)
        values.put(CulturalRegisterEntry.COLUMN_NAME_PICs_VIDs, picturesAndVids);//Array para string

        long result = db.update(CulturalRegisterEntry.TABLE_NAME, values, CulturalRegisterEntry.COLUMN_NAME_ID + "='" + cult_reg.getIdCulturalRegister() + "'", null);
        db.close();
    }

    public boolean delete(CulturalRegister cult_reg) {
        Log.v("Sync", "Delete old " + cult_reg.getTitle());
        SQLiteDatabase db = paminDbHelper.getWritableDatabase();
        boolean result = db.delete(CulturalRegisterEntry.TABLE_NAME,
                CulturalRegisterEntry.COLUMN_NAME_ID + "=" + cult_reg.getIdCulturalRegister(),
                null) > 0;
        Log.v("Sync", "Delete successful: " + result);
        db.close();
        return result;
    }

    public long save(CulturalRegister cult_reg) {
        SQLiteDatabase db = paminDbHelper.getWritableDatabase();

        Log.e("Error", "Eroror  11111"  );

        ContentValues values = new ContentValues();
        values.put(CulturalRegisterEntry.COLUMN_NAME_ID, cult_reg.getIdCulturalRegister());
        values.put(CulturalRegisterEntry.COLUMN_NAME_TITLE, cult_reg.getTitle());
        values.put(CulturalRegisterEntry.COLUMN_NAME_DESCRIPTION, cult_reg.getDescription());
        values.put(CulturalRegisterEntry.COLUMN_NAME_WHERE, cult_reg.getWhere());

        if (cult_reg.getStartDate() != null)
            values.put(CulturalRegisterEntry.COLUMN_NAME_START_DATE, cult_reg.getStartDate().getTimeInMillis());
        if (cult_reg.getEndDate() != null)
            values.put(CulturalRegisterEntry.COLUMN_NAME_END_DATE, cult_reg.getEndDate().getTimeInMillis());

        values.put(CulturalRegisterEntry.COLUMN_NAME_CATEGORY, cult_reg.getCategory());
        values.put(CulturalRegisterEntry.COLUMN_NAME_PROMOTER, cult_reg.getPromoter());
        values.put(CulturalRegisterEntry.COLUMN_NAME_PROMOTER_CONTACT, cult_reg.getPromoterContact());
        values.put(CulturalRegisterEntry.COLUMN_NAME_LATITUDE, cult_reg.getLatitude());
        values.put(CulturalRegisterEntry.COLUMN_NAME_LONGITUDE, cult_reg.getLongitude());
        values.put(CulturalRegisterEntry.COLUMN_NAME_PRICE, cult_reg.getPrice());

        //String[] picturesAndVids = new String[cult_reg.getPictures().size()];
        String picturesAndVids = "";
        int i = 0;
        //for (CloudnaryPicture cp : cult_reg.getPictures()) {
            Log.e("Error", "Eroror");
            CloudnaryPicture cp = null;
        Log.e("Error", "ErororINICIO");
            if(cult_reg.getPictures() != null && !cult_reg.getPictures().isEmpty()) {
                Log.e("Error", "ErororMEIO");
                try {
                    synchronized (lock) {
                        cp = cult_reg.getPictures().getFirst();
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }
                Log.e("Error", "ErororFIM");
            }
            Log.e("Error", "Eroror 22222");
            if (cp == null || cp.getCompleteUrl() == null || cp.getCompleteUrl().isEmpty()){}//continue;
            else {Log.v("CrDAO", "Saving picORvid" + i + ": " + cp.getCompleteUrl()); picturesAndVids = cp.getCompleteUrl();}

            //i++;
        //}
        //convertArrayToString
        values.put(CulturalRegisterEntry.COLUMN_NAME_PICs_VIDs, picturesAndVids); //Array para string
        Log.e("Error", "Eroror  33333");

        long result = db.insert(CulturalRegisterEntry.TABLE_NAME, null, values);
        db.close();
        return result;
    }

    public LinkedList<CulturalRegister> getAll() {
        SQLiteDatabase db = paminDbHelper.getReadableDatabase();

        Cursor c = db.query(CulturalRegisterEntry.TABLE_NAME,
                null, null, null, null, null, null);

        Log.v("Reading", "Elements in DB: " + c.getCount());

        LinkedList<CulturalRegister> culturalRegisters = new LinkedList<CulturalRegister>();

        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();


        if (c.moveToFirst()) {
            do {
                startDate.setTimeInMillis(c.getLong(5));
                endDate.setTimeInMillis(c.getLong(6));
                CulturalRegister cultReg = new CulturalRegister(
                        c.getInt(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4),
                        null,
                        startDate,
                        endDate,
                        c.getDouble(7),
                        c.getDouble(8),
                        c.getDouble(9),
                        c.getString(10),
                        c.getString(11)
                );

                //String[] picsVids = convertStringToArray(c.getString(12));
                String picORvid = c.getString(12);

                //for (String picORvid : picsVids) {
                    if (picORvid == null || picORvid.isEmpty() || picORvid == ""){}//continue
                    else {
                        Log.v("CrDAO", "Loading picORvid: " + picORvid);
                        cultReg.addPicture(new CloudnaryPicture(picORvid));
                    }

                //}
                culturalRegisters.add(cultReg);

            } while (c.moveToNext());
        }

        db.close();
        return culturalRegisters;
    }

    public void deleteAll() {
        SQLiteDatabase db = paminDbHelper.getWritableDatabase();
        db.execSQL(PaminDbHelper.SQL_DELETE_ENTRIES);
        paminDbHelper.onCreate(db);
        db.close();
    }
}
