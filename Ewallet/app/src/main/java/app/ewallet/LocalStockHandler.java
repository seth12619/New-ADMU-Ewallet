package app.ewallet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Shows items at a certain shop terminal | Inventory
 * Created by Seth Legaspi on 10/29/2015.
 */
public class LocalStockHandler extends SQLiteOpenHelper {

    private int PRIMARY_KEY = 101;
    private static final int DATABASE_VERSION = 1;

    //Database Name
    private static final String DATABASE_NAME = "LocalDB_EWallet";

    //Table name
    private static final String TABLE_STOCK = "stock";

    //Students column names
    private static final String KEY_PRIM = "Primary_id";
    private static final String KEY_ID_SHOPTERMINAL = "Shop_Terminal_ID"; //1st column
    private static final String KEY_ID_ITEM = "Item_ID";
    private static final String KEY_TS_STOCK = "stock_ts";
    private static final String KEY_QUANTIY ="quantity";

    public LocalStockHandler(Context context) {
        super(context, DATABASE_NAME, null , DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_STOCK + "(" +
                KEY_PRIM + " INTEGER PRIMARY KEY," +
                KEY_ID_SHOPTERMINAL + " INT," +
                KEY_ID_ITEM + " INT," +
                KEY_TS_STOCK + " DATETIME," +
                KEY_QUANTIY + " INT" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCK);
        //onCreate(db);
    }

    public int generatePrimaryKey()
    {
        return PRIMARY_KEY++;
    }
    public int getPrimaryKey()
    {
        return PRIMARY_KEY;
    }

    public void addStock(Stock stock) {
        SQLiteDatabase db = getWritableDatabase();

        onCreate(db);

        ContentValues values = new ContentValues();
        values.put(KEY_PRIM, stock.getPrim());
        values.put(KEY_ID_SHOPTERMINAL, stock.getShopID()); //1st col
        values.put(KEY_ID_ITEM, stock.getItemID()); //2nd col
        values.put(KEY_TS_STOCK, stock.getTimeStamp()); //3rd col
        values.put(KEY_QUANTIY, stock.getQty());

        db.insert(TABLE_STOCK, null, values);
        db.close();
    }

    public Stock getStock(int itemid) {
        SQLiteDatabase db = getReadableDatabase();


        Cursor cursor = db.query(TABLE_STOCK, new String[]{KEY_PRIM, KEY_ID_SHOPTERMINAL, KEY_ID_ITEM,
                        KEY_TS_STOCK, KEY_QUANTIY}, KEY_ID_ITEM + "=?",
                new String[]{String.valueOf(itemid)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Stock stock = new Stock();
        Log.i("cursor0", cursor.getString(0));
        Log.i("cursor1", cursor.getString(1));
        Log.i("cursor2", cursor.getString(2));
        Log.i("cursor3", cursor.getString(3));
        Log.i("cursor4", cursor.getString(4));
        stock.setPrim(Integer.parseInt(cursor.getString(0)));
        stock.setShopID(cursor.getString(1));
        stock.setItemID(Integer.parseInt(cursor.getString(2)));
        stock.setTimeStamp(cursor.getString(3));
        stock.setQty(Integer.parseInt(cursor.getString(4)));

        db.close();
        return stock;
    }

    public Stock getStockViaItem(int itemID) {
        SQLiteDatabase db = getReadableDatabase();


        Cursor cursor = db.query(TABLE_STOCK, new String[]{KEY_PRIM, KEY_ID_SHOPTERMINAL, KEY_ID_ITEM,
                        KEY_TS_STOCK, KEY_QUANTIY}, KEY_ID_ITEM + "=?",
                new String[]{String.valueOf(itemID)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Stock stock = new Stock();
        stock.setPrim(Integer.parseInt(cursor.getString(0)));
        stock.setShopID(cursor.getString(1));
        stock.setItemID(Integer.parseInt(cursor.getString(2)));
        stock.setTimeStamp(cursor.getString(3));
        stock.setQty(Integer.parseInt(cursor.getString(4)));

        db.close();
        return stock;
    }

    public boolean checkExist(int ID) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * from " + TABLE_STOCK + " where " + KEY_PRIM + " = " + ID;
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

    public boolean checkItemExist(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * from " + TABLE_STOCK + " where " + KEY_ID_ITEM + " = " + id;
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

    public void update(int itemId, int newQty) {
        SQLiteDatabase db = getWritableDatabase();
        onCreate(db);
        String query = "UPDATE " + TABLE_STOCK + " SET " + KEY_QUANTIY + " = " + newQty + " WHERE " + KEY_ID_ITEM + " = " + itemId;
        db.execSQL(query);
        db.close();
    }

    public void drop() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCK);
        onCreate(db);
        db.close();
    }

    public String getJson() {
        SQLiteDatabase db = getReadableDatabase();
        JSONArray array = new JSONArray();
        Cursor cursor;
        String query = "SELECT * FROM " + TABLE_STOCK;
        cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            int name = cursor.getInt(cursor.getColumnIndex(KEY_ID_SHOPTERMINAL));
            int itemId = cursor.getInt(cursor.getColumnIndex(KEY_ID_ITEM));
            String cost = cursor.getString(cursor.getColumnIndex(KEY_TS_STOCK));
            int qty = cursor.getInt(cursor.getColumnIndex(KEY_QUANTIY));

            JSONObject jo = new JSONObject();
            try {
                jo.put("shopID", name);
                jo.put("itemID", itemId);
                jo.put("ts", cost);
                jo.put("qty", qty);
                array.put(jo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return array.toString();
    }
}
