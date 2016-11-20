package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.Constants;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.db.DBHelper;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

/**
 * Created by himashi on 11/19/16.
 */

public class PersistentAccountDAO implements AccountDAO {

    private final Context context;

    public PersistentAccountDAO(Context context) {
        this.context = context;
    }

    @Override
    public List<String> getAccountNumbersList() {

        SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
        db.beginTransaction();
        List<String> accouNumbertList = new ArrayList<String>();

        try {
            Cursor cursor = db.query(Constants.TABLE_ACCOUNT, new String[]{Constants.KEY_ACCOUNT_ID}, null, null, null, null, null);


            if (cursor.moveToFirst()) {
                cursor.moveToFirst();
                do {
                    String string = cursor.getString(cursor.getColumnIndex(Constants.KEY_ACCOUNT_ID));
                    accouNumbertList.add(string);
                } while (cursor.moveToNext());
            }

            System.out.println("ACCOUNT NO LIST " + accouNumbertList.size());

            db.setTransactionSuccessful();
            db.endTransaction();

        } finally {
            if (db != null) {
                db.close();
            }
        }


        return accouNumbertList;
    }

    @Override
    public List<Account> getAccountsList() {
        SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
        db.beginTransaction();
        List<Account> accoutList = new ArrayList<>();

        try {
            Cursor cursor = db.query(Constants.TABLE_ACCOUNT, new String[]{Constants.KEY_ACCOUNT_ID, Constants.KEY_BANK_NAME,
                    Constants.KEY_ACCOUNT_HOLDER_NAME, Constants.KEY_BALANCE}, null, null, null, null, null);


            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String accNo = cursor.getString(cursor.getColumnIndex(Constants.KEY_ACCOUNT_ID));
                    String bankName = cursor.getString(cursor.getColumnIndex(Constants.KEY_BANK_NAME));
                    String holderName = cursor.getString(cursor.getColumnIndex(Constants.KEY_ACCOUNT_HOLDER_NAME));
                    double balance = cursor.getDouble(cursor.getColumnIndex(Constants.KEY_BALANCE));

                    accoutList.add(new Account(accNo, bankName, holderName, balance));
                }
            }
            System.out.println("ACCOUNT LIST " + accoutList);
            db.setTransactionSuccessful();
            db.endTransaction();

        } finally {
            if (db != null) {
                db.close();
            }
        }


        return accoutList;
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
        db.beginTransaction();
        Account account = null;

        try {
            Cursor cursor = db.query(Constants.TABLE_ACCOUNT, new String[]{Constants.KEY_ACCOUNT_ID, Constants.KEY_BANK_NAME,
                    Constants.KEY_ACCOUNT_HOLDER_NAME, Constants.KEY_BALANCE}, Constants.KEY_ACCOUNT_ID + "=?", new String[]{accountNo}, null, null, null);


            if (cursor != null) {
                cursor.moveToFirst();
                String accNo = cursor.getString(cursor.getColumnIndex(Constants.KEY_ACCOUNT_ID));
                String bankName = cursor.getString(cursor.getColumnIndex(Constants.KEY_BANK_NAME));
                String holderName = cursor.getString(cursor.getColumnIndex(Constants.KEY_ACCOUNT_HOLDER_NAME));
                double balance = cursor.getDouble(cursor.getColumnIndex(Constants.KEY_BALANCE));

                account = new Account(accNo, bankName, holderName, balance);

            } else {
                String msg = "Account " + accountNo + " is invalid.";
                throw new InvalidAccountException(msg);
            }
            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (SQLiteException e) {

            System.out.println(e);
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);

        } finally {
            if (db != null) {
                db.close();
            }
        }


        return account;
    }

    @Override
    public void addAccount(Account account) {
        SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Constants.KEY_ACCOUNT_ID, account.getAccountNo());
            contentValues.put(Constants.KEY_BANK_NAME, account.getBankName());
            contentValues.put(Constants.KEY_ACCOUNT_HOLDER_NAME, account.getAccountHolderName());
            contentValues.put(Constants.KEY_BALANCE, account.getBalance());

            long insert = db.insert(Constants.TABLE_ACCOUNT, null, contentValues);
            if (insert > 0) {
                System.out.println("ADD ACCOUNT " + insert);
            }
            db.setTransactionSuccessful();
            db.endTransaction();

        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
        db.beginTransaction();

        try {

            int delete = db.delete(Constants.TABLE_ACCOUNT, Constants.KEY_ACCOUNT_ID + "=?", new String[]{accountNo});

            if (delete < 1) {
                String msg = "Account " + accountNo + " is invalid.";
                throw new InvalidAccountException(msg);
            }
            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (SQLiteException e) {

            System.out.println(e);
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);

        } finally {
            if (db != null) {
                db.close();
            }
        }

    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {

        SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
        db.beginTransaction();


        try {
            Account account;
            synchronized (db) {
                account = getAccount(accountNo, db);
            }


            if (account != null) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Constants.KEY_ACCOUNT_ID, accountNo);

                switch (expenseType) {
                    case EXPENSE:
                        account.setBalance(account.getBalance() - amount);
                        break;
                    case INCOME:
                        account.setBalance(account.getBalance() + amount);
                        break;
                }

                contentValues.put(Constants.KEY_BALANCE, account.getBalance());

                int update = db.update(Constants.TABLE_ACCOUNT, contentValues, Constants.KEY_ACCOUNT_ID + "=?", new String[]{accountNo});

                if (update < 0) {
                    String msg = "Account " + accountNo + " is invalid.";
                    throw new InvalidAccountException(msg);
                }

            } else {
                String msg = "Account " + accountNo + " is invalid.";
                throw new InvalidAccountException(msg);
            }

            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (SQLiteException e) {

            System.out.println(e);
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);

        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    public Account getAccount(String accountNo, SQLiteDatabase sqLiteDatabase) throws InvalidAccountException {
        SQLiteDatabase db = sqLiteDatabase;
        db.beginTransaction();
        Account account = null;

        if (accountNo == null) {
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }

        try {
            Cursor cursor = db.query(Constants.TABLE_ACCOUNT, new String[]{Constants.KEY_ACCOUNT_ID, Constants.KEY_BANK_NAME,
                    Constants.KEY_ACCOUNT_HOLDER_NAME, Constants.KEY_BALANCE}, Constants.KEY_ACCOUNT_ID + "=?", new String[]{accountNo}, null, null, null);


            if (cursor != null) {
                cursor.moveToFirst();
                String accNo = cursor.getString(cursor.getColumnIndex(Constants.KEY_ACCOUNT_ID));
                String bankName = cursor.getString(cursor.getColumnIndex(Constants.KEY_BANK_NAME));
                String holderName = cursor.getString(cursor.getColumnIndex(Constants.KEY_ACCOUNT_HOLDER_NAME));
                double balance = cursor.getDouble(cursor.getColumnIndex(Constants.KEY_BALANCE));

                account = new Account(accNo, bankName, holderName, balance);

            } else {
                String msg = "Account " + accountNo + " is invalid.";
                throw new InvalidAccountException(msg);
            }
            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (SQLiteException e) {

            System.out.println(e);
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);

        }


        return account;
    }

    public void updateBalance(String accountNo, ExpenseType expenseType, double amount, SQLiteDatabase sqliteDB) throws InvalidAccountException {
        SQLiteDatabase db = sqliteDB;
        db.beginTransaction();


        try {
            Account account;
            synchronized (db) {
                account = getAccount(accountNo, db);
            }


            if (account != null) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Constants.KEY_ACCOUNT_ID, accountNo);

                switch (expenseType) {
                    case EXPENSE:
                        account.setBalance(account.getBalance() - amount);
                        break;
                    case INCOME:
                        account.setBalance(account.getBalance() + amount);
                        break;
                }

                contentValues.put(Constants.KEY_BALANCE, account.getBalance());

                int update = db.update(Constants.TABLE_ACCOUNT, contentValues, Constants.KEY_ACCOUNT_ID + "=?", new String[]{accountNo});

                if (update < 0) {
                    String msg = "Account " + accountNo + " is invalid.";
                    throw new InvalidAccountException(msg);
                }

            } else {
                String msg = "Account " + accountNo + " is invalid.";
                throw new InvalidAccountException(msg);
            }

            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (SQLiteException | InvalidAccountException e) {

            System.out.println(e);
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);


        } finally {
            if (db != null) {
                db.close();
            }
        }

    }
}
