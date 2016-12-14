package br.lavid.pamin.com.pamin.data;

import android.provider.BaseColumns;

/**
 * Created by araujojordan on 22/06/15.
 */
public final class PaminDataContract {

    private PaminDataContract() {
    }

    public static abstract class CulturalRegisterEntry implements BaseColumns {

        public static final String TABLE_NAME = "culturalregister";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_CATEGORY = "category";
        public static final String COLUMN_NAME_WHERE = "whereColumm";
        public static final String COLUMN_NAME_PROMOTER = "promoter";
        public static final String COLUMN_NAME_PROMOTER_CONTACT = "promoterContact";
        public static final String COLUMN_NAME_START_DATE = "startDate";
        public static final String COLUMN_NAME_END_DATE = "endDate";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_PICs_VIDs = "picsVids";
    }
}
