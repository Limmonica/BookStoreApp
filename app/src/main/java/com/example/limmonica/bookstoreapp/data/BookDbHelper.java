package com.example.limmonica.bookstoreapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.limmonica.bookstoreapp.data.BookContract.BookEntry;

/**
 * Database helper for BookStore app. Manages database creation and version management.
 */
public class BookDbHelper extends SQLiteOpenHelper {

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "library.db";

    /**
     * Database version
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * CREATE TABLE books(
     * _id INTEGER PRIMARY KEY AUTOINCREMENT,
     * name TEXT NOT NULL,
     * price INTEGER NOT NULL DEFAULT 0,
     * quantity INTEGER NOT NULL DEFAULT 0,
     * supplier TEXT,
     * phone TEXT);
     */
    private static final String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME +
            "(" + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BookEntry.COLUMN_BOOK_PRODUCT_NAME + " TEXT NOT NULL, "
            + BookEntry.COLUMN_BOOK_PRICE + " INTEGER NOT NULL DEFAULT 0, "
            + BookEntry.COLUMN_BOOK_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
            + BookEntry.COLUMN_BOOK_SUPPLIER_NAME + " TEXT, "
            + BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER + " TEXT);";

    /**
     * DROP TABLE IF EXISTS books
     */
    private static final String SQL_DELETE_BOOKS_TABLE = "DROP TABLE IF EXISTS " + BookEntry.TABLE_NAME;

    /**
     * Constructs a new instance of {@link BookDbHelper}.
     *
     * @param context is the context of the app
     */
    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Method called when the database is created for the first time.
     *
     * @param db is the SQLite database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    /**
     * Method called when the database needs to be upgraded.
     *
     * @param db         is the SQLite database
     * @param oldVersion is the version of the database prior to upgrading
     * @param newVersion is the version of the database after the upgrading
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Execute the SQL statement
        db.execSQL(SQL_DELETE_BOOKS_TABLE);
        // Recreate the database by calling the onCreate() method
        onCreate(db);
    }
}
