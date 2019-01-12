package com.example.limmonica.bookstoreapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.limmonica.bookstoreapp.data.BookContract.BookEntry;

import java.util.Locale;

/**
 * Allows user to create a new book or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the book data loader
     */
    private static final int EXISTING_BOOK_LOADER = 0;

    private static final int PERMISSION_CODE = 1;

    /**
     * EditText field to enter the product's name / book's title
     */
    private EditText mTitleEditText;

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
    private EditText mSupplierEditText;

    /**
     * EditText field to enter the book supplier's phone number
     */
    private EditText mPhoneEditText;

    /**
     * Button to increase the quantity
     */
    private MaterialButton mPlusButton;

    /**
     * Button to decrease the quantity
     */
    private MaterialButton mMinusButton;

    /**
     * Content URI for the existing book (null if it's a new book)
     */
    private Uri mCurrentBookUri;

    /**
     * Stores the changes to the input fields
     */
    private boolean mBookHasChanged;

    // Setup a listener for any touches on a View
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find the button views
        mPlusButton = findViewById(R.id.plus_button);
        mMinusButton = findViewById(R.id.minus_button);
        MaterialButton mOrderButton = findViewById(R.id.order_button);

        // Check the intent used to open this activity
        mCurrentBookUri = getIntent().getData();
        // If the intent does not contain a book content URI, then we are creating a new book
        if (mCurrentBookUri == null) {
            // This is a new book, so change the app bar title to "Add a Book"
            setTitle(getString(R.string.editor_activity_title_add_book));
            // Hide the + button
            mPlusButton.setVisibility(View.GONE);
            // Hide the - button
            mMinusButton.setVisibility(View.GONE);
            // Hide the Order button
            mOrderButton.setVisibility(View.GONE);
            // Invalidate the options menu, so that the "Delete" menu option can be hidden
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing book, so change the app bar title to "Edit Book"
            setTitle(getString(R.string.editor_activity_title_edit_book));
            // Show the + button
            mPlusButton.setVisibility(View.VISIBLE);
            // Show the - button
            mMinusButton.setVisibility(View.VISIBLE);
            // Show the Order button
            mOrderButton.setVisibility(View.VISIBLE);
            // Initialize a loader to read the book data from the database and display the current
            // values in the editor
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mTitleEditText = findViewById(R.id.edit_book_name);
        mPriceEditText = findViewById(R.id.edit_book_price);
        mQuantityEditText = findViewById(R.id.edit_book_quantity);
        mSupplierEditText = findViewById(R.id.edit_book_supplier);
        mPhoneEditText = findViewById(R.id.edit_book_supplier_phone);

        // Setup OnTouchListeners on all the input fields in order to know if there are unsaved
        // changes or not
        mTitleEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);
        mPlusButton.setOnTouchListener(mTouchListener);
        mMinusButton.setOnTouchListener(mTouchListener);

        mPlusButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                // Read the quantity string value from the input
                String quantityString = mQuantityEditText.getText().toString().trim();
                // Transform it into an integer
                int quantity = Integer.parseInt(quantityString);
                //
                modifyQuantity(mCurrentBookUri, quantity, mPlusButton);
            }
        });

        mMinusButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                // Read the quantity string value from the input
                String quantityString = mQuantityEditText.getText().toString().trim();
                // Transform it into an integer
                int quantity = Integer.parseInt(quantityString);
                //
                modifyQuantity(mCurrentBookUri, quantity, mMinusButton);
            }
        });

        mOrderButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                makeCall();
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all book attributes, define a projection that contains all columns
        // from the book table
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_SUPPLIER_NAME,
                BookEntry.COLUMN_BOOK_SUPPLIER_PHONE};

        // Loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(
                this,       // Parent activity context
                mCurrentBookUri,    // Query the content URI for the current book
                projection,         // Columns to include in the resulting Cursor
                null,      // No selection clause
                null,   // No selection arguments
                null);     // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Move at the first and only row of the cursor and read data from it
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int titleColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE);

            // Extract out the value from the Cursor for the given column index
            String title = cursor.getString(titleColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);

            // Update the views on the screen with the values from the database
            mTitleEditText.setText(title);
            mPriceEditText.setText(String.format(Locale.getDefault(), "%s", Integer.toString(price)));
            mQuantityEditText.setText(String.format(Locale.getDefault(), "%s", Integer.toString(quantity)));
            mSupplierEditText.setText(supplier);
            mPhoneEditText.setText(phone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out the data from the input fields
        mTitleEditText.setText("");
        mPriceEditText.setText(String.valueOf(0));
        mQuantityEditText.setText(String.valueOf(0));
        mSupplierEditText.setText("");
        mPhoneEditText.setText("");
    }

    /**
     * Method called to validate that no null inputs are accepted before saving the book
     */
    private boolean validateInputs() {
        // Read from input fields
        String titleString = mTitleEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();

        // If the field is empty, show a toast message prompting the user to insert a value
        if (TextUtils.isEmpty(titleString)) {
            Toast.makeText(this, R.string.book_title_invalid, Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, R.string.book_price_invalid, Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, R.string.book_quantity_invalid, Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(supplierString)) {
            Toast.makeText(this, R.string.book_supplier_invalid, Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(phoneString)) {
            Toast.makeText(this, R.string.book_phone_invalid, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get user input from editor and save book into database.
     */
    private void saveBook() {
        // Read from input fields
        String nameString = mTitleEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();

        // Check if this is a new book and check if all fields in the editor are blank
        if (mCurrentBookUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(supplierString) &&
                TextUtils.isEmpty(phoneString)) {
            // Since no fields were modified, we can return early without creating a new book
            return;
        }

        // Create a ContentValues object where the column names are the keys
        // and the book attributes are the values
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_NAME, nameString);
        // If the price is not provided, don't try to parse the string, use 0 by default
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(BookEntry.COLUMN_BOOK_PRICE, price);
        // If the quantity is not provided, don't try to parse the string, use 0 by default
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_NAME, supplierString);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE, phoneString);

        // Determine if this is a new or existing book by checking if mCurrentBook is null or not
        if (mCurrentBookUri == null) {
            // Insert a new row for book in the database, returning the content URI of that new book
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the content URI is null, then there was an error with insertion
                Toast.makeText(this, getResources().getString(R.string.editor_insert_book_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise the insertion was successful and we can display a toast
                Toast.makeText(this, getString(R.string.editor_insert_book_success), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an existing book, so update the book with content URI:
            // mCurrentBookUri and pass in the new ContentValues.
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update
                Toast.makeText(this, getString(R.string.editor_update_book_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise the update was successful and we can display a toast
                Toast.makeText(this, getString(R.string.editor_update_book_successful), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Method called to perform the deletion of the book in the database.
     */
    private void deleteBook() {
        // If this is an existing book
        if (mCurrentBookUri != null) {
            // Call the ContentResolver to delete the book at the given content URI
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete
                Toast.makeText(this, getString(R.string.editor_delete_book_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful
                Toast.makeText(this, getString(R.string.editor_delete_book_successful), Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost if
     * they continue leaving the editor
     *
     * @param discardButtonClickListener is the click listener for what to do when the user confirms
     *                                   they want to discard their changes
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener
                                                  discardButtonClickListener) {
        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the message
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        // Set the click listener for the positive button
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        // Set the click listener for the negative button
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked "Keep Editing" button, so dismiss the dialog
                // and continue editing the book
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
     * Prompt the user to confirm that they want to delete this book.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
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
     * Method called when the back button is pressed
     */
    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise, if there are unsaved changes setup a dialog to warn the user
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide "Delete" menu item
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * Method adds menu items to the app bar of the {@link EditorActivity}: "Save" as action and
     * "Delete" in the Options menu.
     * <p>
     * Overrides the default implementation which populates the menu
     * with standard system menu items
     *
     * @param menu is the options menu in which menu items are placed.
     * @return true for the menu to be displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
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
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // If all inputs are valid
                if (validateInputs()) {
                    // Save book to database
                    saveBook();
                    // Exit activity
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Show confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the book hasn't changed, continue with navigating up to
                // parent activity (Catalog Activity)
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                //Otherwise if there are unsaved changes, setup a dialog to warn the user
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method called when the user presses the + button or the - button to increase or decrease the
     * quantity by 1
     *
     * @param bookUri          is the URI of the current book
     * @param existentQuantity is the existent quantity of the book
     * @param view             is the view the user is tapping on
     */
    private void modifyQuantity(Uri bookUri, int existentQuantity, View view) {

        // If the user pressed the PLUS button
        if (view.getId() == R.id.plus_button) {
            // Initialize the increased quantity
            int newQuantity;
            // If the existent quantity is higher or equal to 0 (it can be 0 as we only increase it)
            if (existentQuantity >= 0) {
                // Increase the quantity by 1
                newQuantity = existentQuantity + 1;
                // Display a toast message saying that the quantity was incremented by 1
                Toast.makeText(getApplicationContext(), R.string.increased_quantity, Toast.LENGTH_SHORT).show();
                // Create a new ContentValues object
                ContentValues values = new ContentValues();
                // Update the book quantity in the database
                values.put(BookEntry.COLUMN_BOOK_QUANTITY, newQuantity);
                getContentResolver().update(bookUri, values, null, null);
            }
            // Else, if the user pressed the MINUS button
        } else if (view.getId() == R.id.minus_button) {
            // Initialize the decreased quantity
            int newQuantity;
            // If the existent quantity is higher than 0 (it can't be equal to 0 as we can't decrease from there)
            if (existentQuantity > 0) {
                // Decrease the quantity by 1
                newQuantity = existentQuantity - 1;
                // Display a toast message saying that the quantity was decremented by 1
                Toast.makeText(getApplicationContext(), R.string.decreased_quantity, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, set the quantity to 0
                newQuantity = 0;
                // Display a toast message saying that the book is out of stock
                Toast.makeText(getApplicationContext(), R.string.out_of_stock, Toast.LENGTH_SHORT).show();
            }
            // Create a new ContentValues object
            ContentValues values = new ContentValues();
            // Update the book quantity in the database
            values.put(BookEntry.COLUMN_BOOK_QUANTITY, newQuantity);
            getContentResolver().update(bookUri, values, null, null);
        }
    }

    /**
     * Callback method for the result from requesting permissions to make the call
     *
     * @param requestCode  the request code passed when requesting permission
     * @param permissions  the requested permission
     * @param grantResults the grant result code for the corresponding permission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeCall();
            } else {
                Toast.makeText(this, R.string.call_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Method called when the user taps on the "Order" button to make a call to the supplier's
     * phone number and place an order for that book
     */
    private void makeCall() {

        // Read the phone number from the text input
        String phoneNumber = mPhoneEditText.getText().toString().trim();

        // Format it in a phone number
        String formattedPhoneNumber = PhoneNumberUtils.formatNumber(phoneNumber);

        // If the activity doesn't have permission to make calls
        if (ContextCompat.checkSelfPermission(EditorActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            // Request the permission
            ActivityCompat.requestPermissions(EditorActivity.this,
                    new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CODE);
            // Otherwise, if the permission has already been granted
        } else {
            // Create a new intent to make the call and place the order of the book
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + formattedPhoneNumber));
            startActivity(callIntent);
        }
    }
}
