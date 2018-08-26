package com.example.limmonica.bookstoreapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.limmonica.bookstoreapp.data.BookContract.BookEntry;

/**
 * Displays list of books that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the book data loader
     */
    private static final int BOOK_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    BookCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Find the FAB to open EditorActivity where to add a new book
        FloatingActionButton fab = findViewById(R.id.fab);
        // Setup a click listener on the FAB to open the EditorActivity
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the book data
        ListView bookListView = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when it has 0 items
        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of book data in the Cursor
        mCursorAdapter = new BookCursorAdapter(this, null);
        // Attach the adapter to the ListView
        bookListView.setAdapter(mCursorAdapter);

        // Setup the item click listener on the list item to open the editor
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific book that was clicked on,
                // by appending the "id", passed as input to this method, onto the
                // {@link BookEntry#CONTENT_URI}
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentBookUri);

                // Launch the {@link EditorActivity} to display the data for the current pet
                startActivity(intent);
            }
        });

        // Start the loader
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded book data into the database. For debugging purposes only.
     */
    private void insertDummyData() {

        // Create a ContentValues object where column names are the keys,
        // and the book's attributes are the values.
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_PRODUCT_NAME, "Gone With The Wind, Margaret Mitchell");
        values.put(BookEntry.COLUMN_BOOK_PRICE, "9");
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, "10");
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_NAME, "Amazon");
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER, "+1 800 800 333");

        // Insert a new row for the book in the provider using the ContentResolver.
        getContentResolver().insert(BookEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all books in the database.
     */
    private void deleteAllBooks() {
        // Call the ContentResolver to delete the books at the generic given content URI
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
        // Show a toast message depending on whether or not the delete was successful
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete
            Toast.makeText(this, R.string.delete_all_books_failed, Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful
            Toast.makeText(this, R.string.delete_all_books_successful, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Prompt the user to confirm that they want to delete all books
     */
    private void showDeleteAllConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete all the books
                deleteAllBooks();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Method adds menu items to the Options overflow menu in the app bar
     * of the {@link CatalogActivity}.
     * <p>
     * Overrides the default implementation which populates the menu
     * with standard system menu items.
     *
     * @param menu is the options menu in which menu items are placed.
     * @return true for the menu to be displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    /**
     * Method called whenever an item in the options menu is selected.
     *
     * @param item is the menu item that was selected
     * @return true to allow the method processing
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert Dummy Data" menu option
            case R.id.action_insert_dummy_data:
                insertDummyData();
                return true;
            // Respond to a click on the "Delete All Entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteAllConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies which columns from the database will be used after this query.
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_PRODUCT_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_SUPPLIER_NAME,
                BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                BookEntry.CONTENT_URI,          // Provider content URI to query
                projection,                     // Columns to include in the resulting Cursor
                null,                  // No selection clause
                null,               // No selection arguments
                null);                 // Default sort order

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link BookCursorAdapter} with this new cursor containing updated book data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
