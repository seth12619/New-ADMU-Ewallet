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
 * Shows how many of this item was bought at this Buy Transaction ID
 * Created by Seth Legaspi on 10/29/2015.
 */
public class LocalitemOrder extends SQLiteOpenHelper {

    private int PRIMARY_KEY = 10010;
    private static final int DATABASE_VERSION = 1;

    //Database Name
    private static final String DATABASE_NAME = "LocalDB_EWallet";

    //Table name
    private static final String TABLE_ITEM_ORDER = "item_order";

    //Students column names
    private static final String KEY_ID_PRIMARYKEY = "My_Primary_Key";
    private static final String KEY_ID_BUYTRANSACTION = "Buy_Transaction_ID"; //1st column
    private static final String KEY_ID_ITEM = "Item_ID";
    private static final String KEY_QUANTITY = "quantity";

    public LocalitemOrder(Context context) {
        super(context, DATABASE_NAME, null , DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_ITEM_ORDER + "(" +
                KEY_ID_PRIMARYKEY + " INTEGER PRIMARY KEY," +
                KEY_ID_BUYTRANSACTION + " INT," +
                KEY_ID_ITEM + " INT," +
                KEY_QUANTITY + " INT" + ")";
        db.execSQL(CREATE_TABLE);
    }

    public int generatePrimaryKey()
    {
        return PRIMARY_KEY++;
    }
    public int getPrimaryKey()
    {
        return PRIMARY_KEY;
    }

    public void addItemOrder(ItemOrder itemOrder) {
        SQLiteDatabase db = getWritableDatabase();
        onCreate(db);

        ContentValues values = new ContentValues();
        values.put(KEY_ID_PRIMARYKEY, getPrimaryKey());
        values.put(KEY_ID_BUYTRANSACTION, itemOrder.getBuyTransID()); //1st col
        values.put(KEY_ID_ITEM, itemOrder.getItemID()); //2nd col
        values.put(KEY_QUANTITY, itemOrder.getQty()); //3rd col

        db.insert(TABLE_ITEM_ORDER, null, values);
        generatePrimaryKey();
        db.close();
    }

    public ItemOrder getItemOrder(int itemid) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_ITEM_ORDER, new String[]{KEY_ID_BUYTRANSACTION, KEY_ID_ITEM,
                        KEY_QUANTITY}, KEY_ID_ITEM + "=?",
                new String[]{String.valueOf(itemid)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        ItemOrder itemOrder = new ItemOrder();

        /*
        Log.i("cursor0", cursor.getString(0));
        Log.i("cursor0", cursor.getString(1));
        Log.i("cursor0", cursor.getString(2));
        */
        itemOrder.setBuyTransID(Integer.parseInt(cursor.getString(0)));
        itemOrder.setItemID(Integer.parseInt(cursor.getString(1)));
        itemOrder.setQty(Integer.parseInt(cursor.getString(2)));

        db.close();
        return itemOrder;
    }

    public boolean checkExist(int ID) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * from " + TABLE_ITEM_ORDER + " where " + KEY_ID_PRIMARYKEY + " = " + ID;
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEM_ORDER);
        //onCreate(db);
    }



    public void drop() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEM_ORDER);
        onCreate(db);
        db.close();
    }

    public String getJson() {
        SQLiteDatabase db = getReadableDatabase();
        JSONArray array = new JSONArray();
        Cursor cursor;
        String query = "SELECT * FROM " + TABLE_ITEM_ORDER;
        cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            int buyTransactionID = cursor.getInt(cursor.getColumnIndex(KEY_ID_BUYTRANSACTION));
            int itemID = cursor.getInt(cursor.getColumnIndex(KEY_ID_ITEM));
            int qty = cursor.getInt(cursor.getColumnIndex(KEY_QUANTITY));

            JSONObject jo = new JSONObject();
            try {
                jo.put("btID", buyTransactionID);
                jo.put("itemID", itemID);
                jo.put("qty", qty);

                array.put(jo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array.toString();
    }
}
