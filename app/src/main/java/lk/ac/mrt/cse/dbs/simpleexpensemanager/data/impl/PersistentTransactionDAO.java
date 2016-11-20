package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.Constants;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.R;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.db.DBHelper;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

/**
 * Created by himashi on 11/19/16.
 */

public class PersistentTransactionDAO implements TransactionDAO {
    private final Context context;
    PersistentAccountDAO persistentAccountDAO;

    public PersistentTransactionDAO(Context context) {
        this.context = context;
        persistentAccountDAO = new PersistentAccountDAO(context);
    }

    @Override
    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) {
        SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues contentValues = new ContentValues();

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String dateString = sdf.format(new Date());

            contentValues.put(Constants.KEY_DATE, dateString);
            contentValues.put(Constants.KEY_ACCOUNT_ID, accountNo);


            switch (expenseType) {
                case EXPENSE:
                    contentValues.put(Constants.KEY_EXPENSE_TYPE, "EXPENSE");
                    break;
                case INCOME:
                    contentValues.put(Constants.KEY_EXPENSE_TYPE, "INCOME");
                    break;
            }

            contentValues.put(Constants.KEY_AMOUNT, amount);

            long insert = db.insert(Constants.TABLE_TRANSACTION, null, contentValues);

//            if(insert>0){
//                persistentAccountDAO.updateBalance(accountNo,expenseType,amount);
//            }

            db.setTransactionSuccessful();
            db.endTransaction();


        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    @Override
    public List<Transaction> getAllTransactionLogs() {
        SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
        db.beginTransaction();
        List<Transaction> transactionList = new ArrayList<>();

        try {
            Cursor cursor = db.query(Constants.TABLE_TRANSACTION, new String[]{Constants.KEY_DATE, Constants.KEY_ACCOUNT_ID,
                    Constants.KEY_EXPENSE_TYPE, Constants.KEY_AMOUNT}, null, null, null, null, null);


            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String date = cursor.getString(cursor.getColumnIndex(Constants.KEY_DATE));
                    String accNo = cursor.getString(cursor.getColumnIndex(Constants.KEY_ACCOUNT_ID));
                    String expenseType = cursor.getString(cursor.getColumnIndex(Constants.KEY_EXPENSE_TYPE));
                    double amount = cursor.getDouble(cursor.getColumnIndex(Constants.KEY_AMOUNT));

                    DateFormat df = new SimpleDateFormat(context.getString(R.string.config_date_log_pattern));
                    Date startDate = null;
                    try {
                        startDate = df.parse(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (expenseType.equals("EXPENSE"))
                        transactionList.add(new Transaction(startDate, accNo, ExpenseType.EXPENSE, amount));
                    else
                        transactionList.add(new Transaction(startDate, accNo, ExpenseType.INCOME, amount));


                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();

        } finally {
            if (db != null) {
                db.close();
            }
        }


        return transactionList;
    }

    @Override
    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
        db.beginTransaction();
        List<Transaction> accoutList = new ArrayList<>();

        try {
            Cursor cursor = db.rawQuery("SELECT * FROM '" + Constants.TABLE_TRANSACTION + "' ORDER BY '" + Constants.KEY_DATE + "' DESC LIMIT '" + String.valueOf(limit) + "'", null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String date = cursor.getString(cursor.getColumnIndex(Constants.KEY_DATE));
                    String accNo = cursor.getString(cursor.getColumnIndex(Constants.KEY_ACCOUNT_ID));
                    String expenseType = cursor.getString(cursor.getColumnIndex(Constants.KEY_EXPENSE_TYPE));
                    double amount = cursor.getDouble(cursor.getColumnIndex(Constants.KEY_AMOUNT));

                    DateFormat df = new SimpleDateFormat(context.getString(R.string.config_date_log_pattern));
                    Date startDate = null;
                    try {
                        startDate = df.parse(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (expenseType.equals("EXPENSE"))
                        accoutList.add(new Transaction(startDate, accNo, ExpenseType.EXPENSE, amount));
                    else
                        accoutList.add(new Transaction(startDate, accNo, ExpenseType.INCOME, amount));

                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();

        } finally {
            if (db != null) {
                db.close();
            }
        }


        return accoutList;
    }
}
