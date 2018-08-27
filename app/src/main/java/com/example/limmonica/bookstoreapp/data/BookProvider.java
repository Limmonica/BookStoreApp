package com.example.limmonica.bookstoreapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.limmonica.bookstoreapp.data.BookContract.BookEntry;

/**
 * {@link ContentProvider} for BookStore app.
 */
public class BookProvider extends ContentProvider {

    /**
     * Tag for log messages
     */
    public static final String LOG_TAG = BookProvider.class.getSimpleName();
    /**
     * URI matcher code for the content URI for the books table
     */
    private static final int BOOKS = 100;
    /**
     * URI matcher code for the content URI for a single book in the books table
     */
    private static final int BOOK_ID = 101;
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // The content URI of the form "content://com.example.android.bookstoreapp/books" will map
        // to the integer code {@link #BOOKS}. This URI is used to provide access to MULTIPLE rows
        // of the books table.
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);

        // The content URI of the form "content://com.example.android.bookstoreapp/books/#" will
        // map to the integer code {@link #BOOK_ID}. This URI is used to provide access to ONE
        // single row of the books table.
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#",
                BOOK_ID);
    }

    /**
     * Database helper object
     */
    private BookDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // Create and initialize a BookDbHelper object to gain access to the books database.
        mDbHelper = new BookDbHelper(getContext());
        return true;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection
     * arguments and sort order.
     */
    @Override
    public Cursor query(@NonNull Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // The cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // Perform database query on books table
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case BOOK_ID:
                // For the BOOK_ID code, extract out the ID from the URI.
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // Perform a query on the books table to return a Cursor containing the
                // row of the table specified by the selection
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor in order to know what content URI the Cursor was
        // created for
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        // Return the cursor
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported " + uri);
        }
    }

    /**
     * Insert a book into the database with the given content values. Return the new content URI
     * for that specific row in the database
     */
    private Uri insertBook(Uri uri, ContentValues values) {

        // name TEXT NOT NULL - Check that the name is not null
        String name = values.getAsString(BookEntry.COLUMN_BOOK_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Book requires a name");
        }

        // price INTEGER NOT NULL DEFAULT 0 - Check that the price is greater than or equal to 0
        Integer price = values.getAsInteger(BookEntry.COLUMN_BOOK_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Book requires a valid price");
        }

        // quantity INTEGER NOT NULL DEFAULT 0 - Check that the quantity is not null
        Integer quantity = values.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Book requires a valid quantity");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new book with the given values
        long id = database.insert(BookEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the book content URI
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the new URI with the ID (of the newly inserted row) appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri,
                      ContentValues contentValues,
                      String selection,
                      String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // For the BOOK code update all the rows that match the selection and selection
                // arguments
                return updateBook(uri, contentValues, selection, selectionArgs);
            case BOOK_ID:
                // For the BOOK_ID code, extract out the ID from the URI,
                // so we know which row to update.
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update books in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more books).
     * Return the number of rows that were successfully updated.
     */
    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the {@link BookEntry#COLUMN_BOOK_NAME} key is present,
        // check that the name value is not null
        if (values.containsKey(BookEntry.COLUMN_BOOK_NAME)) {
            String name = values.getAsString(BookEntry.COLUMN_BOOK_NAME);
            // Handle the case when the value is null
            if (name == null) {
                throw new IllegalArgumentException("Book require a name");
            }
        }

        // If the {@link BookEntry#COLUMN_BOOK_PRICE key is present,
        // check that the price value is valid
        if (values.containsKey(BookEntry.COLUMN_BOOK_PRICE)) {
            Integer price = values.getAsInteger(BookEntry.COLUMN_BOOK_PRICE);
            // Handle the case when the value is not valid
            if (price != 0 && price < 0) {
                throw new IllegalArgumentException("Book requires a valid price");
            }
        }

        // If the {@link BookEntry#COLUMN_BOOK_QUANTITY key is present,
        // check that the quantity value is valid
        if (values.containsKey(BookEntry.COLUMN_BOOK_QUANTITY)) {
            Integer quantity = values.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
            // Handle the case when the quantity is not valid
            if (quantity != 0 && quantity < 0) {
                throw new IllegalArgumentException("Book requires a valid quantity");
            }
        }

        // Return the number of rows that were affected
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        int rowsUpdated = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the given URI
        // has changed
        if (rowsUpdated != 0) {
            // Notify all listeners that the data has changed for the book content URI
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri,
                      String selection,
                      String[] selectionArgs) {

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        // Match the URI
        final int match = sUriMatcher.match(uri);
        // Based on the URI code delete all the books or delete a single book
        switch (match) {
            case BOOKS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                // Delete a single row given by the ID in the URI
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }
}