package com.loadingterminal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Seth Legaspi on 11/5/2015.
 */
public class LocalUserDBhandler extends SQLiteOpenHelper {
    //Database Version
    private static final int DATABASE_VERSION = 1;

    //Database Name
    private static final String DATABASE_NAME = "LocalDB_EWallet";

    //Table name
    private static final String TABLE_USERS = "Users";

    //Students column names
    private static final String KEY_ID_NUMBER = "ID_Number"; //1st column
    private static final String KEY_NAME = "Name"; //2nd column
    private static final String KEY_PIN = "Pin"; //3rd column


    /**
     * Constructor for the local Database
     * @param context - Context of where this Database is going to be used (The where) Hint: this
     */
    public LocalUserDBhandler(Context context) {
        super(context, DATABASE_NAME, null , DATABASE_VERSION);
    }

    /**
     * Creates a table with a the columns: ([intPK]ID_NUMBER, [text] Name, [int] PIN, [num] Balance)
     * @param db - The database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + "(" +
                KEY_ID_NUMBER + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT," +
                KEY_PIN + " INT" + ")";
        db.execSQL(CREATE_TABLE);
    }

    /**
     * Updates the database with a newer one
     * @param db - The new database
     * @param i - Old version number of the database
     * @param i1 - New version number of the database (does not necessarily need to be different from 'i')
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public void drop() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
        db.close();
    }

    /**
     * Adds a new row for the table
     * @param student - The Student 'object'
     */
    public void addStud(User student) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID_NUMBER, student.getID()); //ID Number of the student -1st col
        values.put(KEY_NAME, student.getName()); //Name of the student -2nd col
        values.put(KEY_PIN, student.getPIN()); //PIN of the student -3rd col

        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    /**
     *  Gets the entire Student object of a certain ID Number
     *  Note, Everything is turned to a String so I can put em all in 1 array and just call em to put in an object later
     * @param id - ID number of the student to be queried
     * @return - Returns the Student object so you can do object oriented things with it
     */
    public User getStudent(int id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS, new String[]{KEY_ID_NUMBER, KEY_NAME,
                        KEY_PIN}, KEY_ID_NUMBER + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        User student = new User();
        student.setID(Integer.parseInt(cursor.getString(0)));
        student.setName(cursor.getString(1));
        student.setPin(Integer.parseInt(cursor.getString(2)));

        db.close();
        return student;
    }

    /**
     * To check if a certain student exists in the Database yet
     * @param ID - ID Number to be checked
     * @return - Returns true or false depending if this certain item exists
     */
    public boolean checkExist(int ID) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * from " + TABLE_USERS + " where ID_Number = " + ID;
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

