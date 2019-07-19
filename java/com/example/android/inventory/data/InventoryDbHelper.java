package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class InventoryDbHelper extends SQLiteOpenHelper {

    //the name of the database
    public static final String DATABASE_NAME = "inventory.db";

    //set the database version
    public static final int DATA_BASE_VERSION = 1;

    // set the string that we will use to create the table - columns and data type
    public final String SQL_CREATE_BOOKS_ENTRIES = "CREATE TABLE " + InventoryContract.BooksEntry.TABLE_NAME + " (" +
            InventoryContract.BooksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            InventoryContract.BooksEntry.COLUMN_BOOKS_TITLE + " TEXT NOT NULL," +
            InventoryContract.BooksEntry.COLUMN_BOOKS_AUTHOR + " TEXT NOT NULL," +
            InventoryContract.BooksEntry.COLUMN_BOOKS_PAGES_COUNT + " INTEGER NOT NULL," +
            InventoryContract.BooksEntry.COLUMN_BOOKS_YEAR_PUBLISHED + " INTEGER NOT NULL," +
            InventoryContract.BooksEntry.COLUMN_BOOKS_QUANTITY + " INTEGER NOT NULL DEFAULT 0," +
            InventoryContract.BooksEntry.COLUMN_BOOKS_PRICE + " REAL NOT NULL DEFAULT 0," +
            InventoryContract.BooksEntry.COLUMN_BOOKS_IMAGE + " TEXT)";

    public final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS" + InventoryContract.BooksEntry.TABLE_NAME;

    // constructor
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATA_BASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        createTable(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int i, int i1) {
        database.execSQL(SQL_DELETE_ENTRIES);
        createTable(database);
    }

    private void createTable(SQLiteDatabase database) {
        database.execSQL(SQL_CREATE_BOOKS_ENTRIES);
    }
}