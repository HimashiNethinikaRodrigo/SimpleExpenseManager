package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.Constants;


/**
 * Created by himashi on 11/20/16.
 */

public class DBHelper extends SQLiteOpenHelper {


    private static DBHelper dbHelper;


    public static synchronized DBHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new DBHelper(context.getApplicationContext());
        }
        return dbHelper;
    }


    private static final String CREATE_TABLE_ACCOUNT = "CREATE TABLE " +
            Constants.TABLE_ACCOUNT + "(" +
            Constants.KEY_ACCOUNT_NO + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            Constants.KEY_ACCOUNT_ID + " TEXT ," +
            Constants.KEY_BANK_NAME + " TEXT," +
            Constants.KEY_ACCOUNT_HOLDER_NAME + " TEXT," +
            Constants.KEY_BALANCE + " DECIMAL " +
            ")";

    private static final String CREATE_TABLE_TRANSACTION = "CREATE TABLE " +
            Constants.TABLE_TRANSACTION + "(" +
            Constants.KEY_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            Constants.KEY_DATE + " DATETIME, " +
            Constants.KEY_ACCOUNT_ID + " TEXT," +
            Constants.KEY_EXPENSE_TYPE + " TEXT," +
            Constants.KEY_AMOUNT + " DECIMAL " +
            ")";


    public DBHelper(Context context) {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ACCOUNT);
        db.execSQL(CREATE_TABLE_TRANSACTION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_ACCOUNT);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_TRANSACTION);
        onCreate(db);
    }
}
