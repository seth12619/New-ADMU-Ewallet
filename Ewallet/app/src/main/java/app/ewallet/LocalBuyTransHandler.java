package app.ewallet;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Shows who bought at this store
 * Created by Seth Legaspi on 10/29/2015.
 */
public class LocalBuyTransHandler extends SQLiteOpenHelper {
    private int PRIMARY_KEY = 10001; //Most significant digit is the store number
    private static final int DATABASE_VERSION = 1;

    //Database Name
    private static final String DATABASE_NAME = "LocalDB_EWallet";

    //Table name
    private static final String TABLE_BUY = "buy_transaction";

    //Students column names
    private static final String KEY_ID_TRANSACTION = "Buy_Transaction_ID"; //1st column
    private static final String KEY_TS_TRANSACTION = "buy_transaction_TS";
    private static final String KEY_ID_NUMBER = "id_number";
    private static final String KEY_ID_SHOPTERMINAL = "shop_terminal_id";
    private static final String KEY_SEND_STAMP = "send_stamp";

    public LocalBuyTransHandler(Context context) {
        super(context, DATABASE_NAME, null , DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_BUY + "(" +
                  KEY_ID_TRANSACTION + " INTEGER PRIMARY KEY," +   //MUST MAKE BUY TRANSACTION ID AUTO INCREMENT. T.T
                  KEY_TS_TRANSACTION + " DATETIME," +
                  KEY_ID_NUMBER + " INT," +
                  KEY_ID_SHOPTERMINAL + " TEXT," +
                  KEY_SEND_STAMP + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUY);
        //onCreate(db);
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

    public void addBuyTrans(BuyTransaction bt) {
        SQLiteDatabase db = getWritableDatabase();
        onCreate(db);

        ContentValues values = new ContentValues();
        values.put(KEY_ID_TRANSACTION, bt.getTransID());
        values.put(KEY_TS_TRANSACTION, bt.getTimeStamp()); //2nd col
        values.put(KEY_ID_NUMBER, bt.getIDNum()); //3rd col
        values.put(KEY_ID_SHOPTERMINAL, bt.getShopID());
        values.put(KEY_SEND_STAMP, bt.getSendStamp());

        db.insert(TABLE_BUY, null, values);
        db.close();
    }

    public BuyTransaction getBuyTransaction(int id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_BUY, new String[]{KEY_ID_TRANSACTION, KEY_TS_TRANSACTION,
                        KEY_ID_NUMBER, KEY_ID_SHOPTERMINAL, KEY_SEND_STAMP}, KEY_ID_TRANSACTION + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        BuyTransaction transaction = new BuyTransaction();
        transaction.setTransID(Integer.parseInt(cursor.getString(0)));
        transaction.setTimeStamp(cursor.getString(1));
        transaction.setIDNum(Integer.parseInt(cursor.getString(2)));
        transaction.setShopID(cursor.getString(3));
        transaction.setSendStamp(cursor.getString(4));

        db.close();
        return transaction;
    }

    public boolean checkExist(int ID) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * from " + TABLE_BUY + " where " + KEY_ID_TRANSACTION  +" = " + ID;
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

    public String getJson() {
        SQLiteDatabase db = getReadableDatabase();
        JSONArray array = new JSONArray();
        Cursor cursor;
        String query = "SELECT * FROM " + TABLE_BUY;
        cursor = db.rawQuery(query,null);
        while (cursor.moveToNext()) {
            int buyTransactionID = cursor.getInt(cursor.getColumnIndex(KEY_ID_TRANSACTION));
            String ts = cursor.getString(cursor.getColumnIndex(KEY_TS_TRANSACTION));
            int idNum = cursor.getInt(cursor.getColumnIndex(KEY_ID_NUMBER));
            String shopTerminal = cursor.getString(cursor.getColumnIndex(KEY_ID_SHOPTERMINAL));
            String sendStamp = cursor.getString(cursor.getColumnIndex(KEY_SEND_STAMP));

            JSONObject jo = new JSONObject();
            try {
                jo.put("btID",buyTransactionID);
                jo.put("ts",ts);
                jo.put("idnum", idNum);
                jo.put("shopTerminal",shopTerminal);
                jo.put("sendStamp", sendStamp);

                array.put(jo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return array.toString();
    }

    public void drop() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUY);
        onCreate(db);
        db.close();
    }

    public void updateSendStamp(int btID, String stamp) {
        SQLiteDatabase db = getWritableDatabase();
        onCreate(db);
        String query = "UPDATE " + TABLE_BUY + " SET " + KEY_SEND_STAMP + " = '" + stamp + "' WHERE " + KEY_ID_TRANSACTION + " = " + btID;
        db.execSQL(query);
        db.close();
    }


}
