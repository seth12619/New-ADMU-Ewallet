package app.ewallet;

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
public class LocalBalanceHandler extends SQLiteOpenHelper {

    //Database Version
    private static final int DATABASE_VERSION = 1;

    //Database Name
    private static final String DATABASE_NAME = "LocalDB_ShopBalance";

    //Table name
    private static final String TABLE_BALANCE = "ShopBalance";

    //Students column names
    private static final String KEY_SHOP_ID = "Shop_ID"; //1st column
    private static final String KEY_BALANCE = "Balance"; //2nd column

    public LocalBalanceHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates the Table for the database
     * @param db = the local Database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_BALANCE + "(" +
                KEY_SHOP_ID + " INTEGER PRIMARY KEY," +
                KEY_BALANCE + " NUM" + ")";

        db.execSQL(CREATE_TABLE);
    }

    /**
     * Updates the database if ever
     * @param db - The database
     * @param i - previous version number
     * @param i1 - new version number
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BALANCE);
        onCreate(db);
    }
    public void drop() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BALANCE);
        onCreate(db);
        db.close();
    }

    public void addShopBalance(Balance balance) {
        SQLiteDatabase db = getWritableDatabase();
        onCreate(db);

        ContentValues values = new ContentValues();
        values.put(KEY_SHOP_ID, balance.getShopID());
        values.put(KEY_BALANCE, balance.getBalance());


        db.insert(TABLE_BALANCE, null, values);
        db.close();
    }

    public String getJson() {
        SQLiteDatabase db = getReadableDatabase();
        JSONArray array = new JSONArray();
        Cursor cursor;
        String query = "SELECT * FROM " + TABLE_BALANCE;
        cursor = db.rawQuery(query,null);
        while (cursor.moveToNext()) {
            String id = String.valueOf(cursor.getInt(cursor.getColumnIndex(KEY_SHOP_ID)));
            Double bal = cursor.getDouble(cursor.getColumnIndex(KEY_BALANCE));

            JSONObject jo = new JSONObject();
            try {
                jo.put("id", id);
                jo.put("bal",bal);
                array.put(jo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return array.toString();
    }

    public void updateBalance(int shopID, Double newBal) {
        SQLiteDatabase db = getWritableDatabase();
        onCreate(db);
        String query = "UPDATE " + TABLE_BALANCE + " SET " + KEY_BALANCE + " = " + newBal + " WHERE " + KEY_SHOP_ID + " = " + shopID;
        db.execSQL(query);
        db.close();
    }

}
