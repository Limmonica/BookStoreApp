package com.example.limmonica.bookstoreapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.limmonica.bookstoreapp.data.BookContract.BookEntry;
import com.example.limmonica.bookstoreapp.data.BookDbHelper;

/**
 * Displays list of books that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity {

    /**
     * EditText field to enter the product's name / book's title
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the book's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the book's quantity
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to enter the book supplier's name
     */
    private EditText mSupplierText;

    /**
     * EditText field to enter the book supplier's phone number
     */
    private EditText mPhoneEditText;

    /**
     * TextView to display the
     */
    private TextView displayBooksInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_book_name);
        mPriceEditText = findViewById(R.id.edit_book_price);
        mQuantityEditText = findViewById(R.id.edit_book_quantity);
        mSupplierText = findViewById(R.id.edit_book_supplier);
        mPhoneEditText = findViewById(R.id.edit_book_supplier_phone);
        displayBooksInfo = findViewById(R.id.summary_display);

        // Find the button which adds the user input to the database
        Button addBook = findViewById(R.id.add_book_button);
        // Setup a click listener on the button to add the book in the database
        addBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertBook();
            }
        });

        // Read and display the information related to the books in the database
        readAndDisplayBooksInfo();
    }

    /**
     * Get user input from editor and save new book into database.
     */
    private void insertBook() {

        // Read from input fields
        String nameString = mNameEditText.getText().toString().trim();
        int price = Integer.parseInt(mPriceEditText.getText().toString().trim());
        int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
        String supplierString = mSupplierText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();

        // Create a database helper
        BookDbHelper mDbHelper = new BookDbHelper(this);

        // Get the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a ContentValues object where the column names are the keys
        // and the book attributes are the values
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_PRODUCT_NAME, nameString);
        values.put(BookEntry.COLUMN_BOOK_PRICE, price);
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_NAME, supplierString);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER, phoneString);

        // Insert a new row for book in the database, returning the ID of that new row
        long newRowId = db.insert(BookEntry.TABLE_NAME, null, values);

        // Show a toast message depending on wether or not the insertion was successful
        if (newRowId == -1) {
            // If the row id is -1, then there was an error with insertion
            Toast.makeText(this, getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast with the row ID
            Toast.makeText(this, getResources().getString(R.string.toast_success) + newRowId, Toast.LENGTH_SHORT).show();
            mNameEditText.setText("");
            mPriceEditText.setText("");
            mQuantityEditText.setText("");
            mSupplierText.setText("");
            mPhoneEditText.setText("");
            recreate();
        }
    }

    /**
     * Temporary helper method to read and display information in the onscreen TextView about the
     * state of the books database
     */
    private void readAndDisplayBooksInfo() {

        // Create a database helper
        BookDbHelper mDbHelper = new BookDbHelper(this);

        // Get the database in read mode
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // will be used after this query.
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_PRODUCT_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_SUPPLIER_NAME,
                BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER
        };

        // Perform a query on the books table
        Cursor cursor = db.query(
                BookEntry.TABLE_NAME,   // the table to query
                projection,             // the array of columns to return
                null,          // the columns for the WHERE clause
                null,       // the values for the WHERE clause
                null,           // don't group the rows
                null,            // don't filter by row groups
                null);          // the sort order

        try {
            // Create header summary:
            // The books table contains <number of rows in Cursor> books.
            //    _id | name | price | quantity | supplier | phone
            displayBooksInfo.setText("The table contains: " + cursor.getCount() + " books. \n \n");
            displayBooksInfo.append(
                    BookEntry._ID + " | " +
                            BookEntry.COLUMN_BOOK_PRODUCT_NAME + " | " +
                            BookEntry.COLUMN_BOOK_PRICE + " | " +
                            BookEntry.COLUMN_BOOK_QUANTITY + " | " +
                            BookEntry.COLUMN_BOOK_SUPPLIER_NAME + " | " +
                            BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER + "\n");

            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(BookEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER);

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // Use the index to extract the String/Int value of the word at the current row
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                int currentPrice = cursor.getInt(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                String currentSupplier = cursor.getString(supplierColumnIndex);
                String currentPhone = cursor.getString(phoneColumnIndex);
                // Display the values from each column of the current row in the cursor in the TextView
                displayBooksInfo.append("\n" +
                        currentID + " | " +
                        currentName + " | " +
                        currentPrice + " | " +
                        currentQuantity + " | " +
                        currentSupplier + " | " +
                        currentPhone + " | ");
            }
        } finally {
            // Always close the cursor when done reading
            cursor.close();
            // Close the database
            db.close();
        }
    }
}
