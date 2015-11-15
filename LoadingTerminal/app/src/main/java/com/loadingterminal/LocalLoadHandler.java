package com.loadingterminal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seth Legaspi on 11/15/2015.
 */
public class LocalLoadHandler extends SQLiteOpenHelper {
    private int PRIMARY_KEY = 10001; //Most significant digit is the store number
    //Database Version
    private static final int DATABASE_VERSION = 1;

    //Database Name
    private static final String DATABASE_NAME = "LocalDB_EWallet";

    //Table name
    private static final String TABLE_TRANSACTION = "Transactions";

    //Students column names
    private static final String KEY_ID_TRANSACTION = "ID_Transaction"; //1st column
    private static final String KEY_AMOUNT = "Amount Loaded"; //2nd column
    private static final String KEY_TRANSACTION_TS = "Timestamp"; //3rd column
    private static final String KEY_ID_NUMBER = "ID number";
    private static final String KEY_TERMINAL_ID = "Terminal ID";
    private static final String KEY_SEND_STAMP = "Send Stamp";

    public LocalLoadHandler(Context context) {
        super(context, DATABASE_NAME, null , DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_TRANSACTION + "(" +
                KEY_ID_TRANSACTION + " INTEGER PRIMARY KEY," +
                KEY_AMOUNT + " NUM," +
                KEY_TRANSACTION_TS + " DATETIME," +
                KEY_ID_NUMBER + " INT," +
                KEY_TERMINAL_ID + " TEXT," +
                KEY_SEND_STAMP + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        onCreate(db);
    }

    public int generatePrimaryKey()
    {
        return PRIMARY_KEY++;
    }
    public void setPrimaryKey(int inputKey)
    {
        PRIMARY_KEY = inputKey;
    }
    public int getPrimaryKey()
    {
        return PRIMARY_KEY;
    }

    public void drop() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        onCreate(db);
        db.close();
    }

    public void addTransaction(LoadTransaction load) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID_TRANSACTION, load.getIDTransaction());
        values.put(KEY_AMOUNT, load.getAmt());
        values.put(KEY_TRANSACTION_TS, load.getTS());
        values.put(KEY_ID_NUMBER, load.getIDNum());
        values.put(KEY_TERMINAL_ID, load.getTerminalID());
        values.put(KEY_SEND_STAMP, load.getSendStamp());

        db.insert(TABLE_TRANSACTION, null, values);
        db.close();
    }

    public String getJson() {
        SQLiteDatabase db = getReadableDatabase();
        JSONArray array = new JSONArray();
        Cursor cursor;
        String query = "SELECT * FROM " + TABLE_TRANSACTION;
        cursor = db.rawQuery(query,null);
        while (cursor.moveToNext()) {
            int loadTransactionID = cursor.getInt(cursor.getColumnIndex(KEY_ID_TRANSACTION));
            Double amount = cursor.getDouble(cursor.getColumnIndex(KEY_AMOUNT));
            String ts = cursor.getString(cursor.getColumnIndex(KEY_TRANSACTION_TS));
            int idNum = cursor.getInt(cursor.getColumnIndex(KEY_ID_NUMBER));
            String loadTerminal = cursor.getString(cursor.getColumnIndex(KEY_TERMINAL_ID));
            String sendStamp = cursor.getString(cursor.getColumnIndex(KEY_SEND_STAMP));

            JSONObject jo = new JSONObject();
            try {
                jo.put("loadID",loadTransactionID);
                jo.put("amount", amount);
                jo.put("ts",ts);
                jo.put("idnum", idNum);
                jo.put("loadTerminal",loadTerminal);
                jo.put("sendStamp", sendStamp);

                array.put(jo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return array.toString();
    }

    public LoadTransaction getLoadTransaction(int id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_TRANSACTION, new String[]{KEY_ID_TRANSACTION, KEY_AMOUNT,
                        KEY_TRANSACTION_TS, KEY_ID_NUMBER, KEY_TERMINAL_ID, KEY_SEND_STAMP}, KEY_ID_TRANSACTION + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        LoadTransaction transaction = new LoadTransaction();
        transaction.setIDTransaction(Integer.parseInt(cursor.getString(0)));
        transaction.setAmountLoaded(Double.parseDouble(cursor.getString(1)));
        transaction.setTS(cursor.getString(2));
        transaction.setIDnum(Integer.parseInt(cursor.getString(3)));
        transaction.setTerminalID(cursor.getString(4));
        transaction.setSendStamp(cursor.getString(5));

        db.close();
        return transaction;
    }

    public void updateSendStamp(int btID, String stamp) {
        SQLiteDatabase db = getWritableDatabase();
        onCreate(db);
        String query = "UPDATE " + TABLE_TRANSACTION + " SET " + KEY_SEND_STAMP + " = '" + stamp + "' WHERE " + KEY_ID_TRANSACTION + " = " + btID;
        db.execSQL(query);
        db.close();
    }

    public boolean checkExist(int ID) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * from " + TABLE_TRANSACTION + " where " + KEY_ID_TRANSACTION  +" = " + ID;
        try {
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.getCount() < 1) {
                db.close();
                cursor.close();
                return false;
            } else
                db.close();
            cursor.close();
            return true;
        } catch (Exception e) {
            db.close();
            return false;
        }
    }

}
