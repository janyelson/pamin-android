package br.lavid.pamin.com.pamin.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import br.lavid.pamin.com.pamin.data.PaminDataContract.CulturalRegisterEntry;


public class PaminDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 17;
    public static final String DATABASE_NAME = "Pamin.db";
    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + CulturalRegisterEntry.TABLE_NAME;
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String BLOP_TYPE = " BLOB";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + CulturalRegisterEntry.TABLE_NAME + " (" +
                    CulturalRegisterEntry.COLUMN_NAME_ID + INT_TYPE + COMMA_SEP +
                    CulturalRegisterEntry.COLUMN_NAME_PROMOTER + TEXT_TYPE + COMMA_SEP +
                    CulturalRegisterEntry.COLUMN_NAME_PROMOTER_CONTACT + TEXT_TYPE + COMMA_SEP +
                    CulturalRegisterEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    CulturalRegisterEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    CulturalRegisterEntry.COLUMN_NAME_START_DATE + INT_TYPE + COMMA_SEP +
                    CulturalRegisterEntry.COLUMN_NAME_END_DATE + INT_TYPE + COMMA_SEP +
                    CulturalRegisterEntry.COLUMN_NAME_PRICE + REAL_TYPE + COMMA_SEP +
                    CulturalRegisterEntry.COLUMN_NAME_LATITUDE + REAL_TYPE + COMMA_SEP +
                    CulturalRegisterEntry.COLUMN_NAME_LONGITUDE + REAL_TYPE + COMMA_SEP +
                    CulturalRegisterEntry.COLUMN_NAME_CATEGORY + TEXT_TYPE + COMMA_SEP +
                    CulturalRegisterEntry.COLUMN_NAME_WHERE + TEXT_TYPE + COMMA_SEP +
                    CulturalRegisterEntry.COLUMN_NAME_PICs_VIDs + TEXT_TYPE +
                    " )";

    public PaminDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
