package com.example.limmonica.bookstoreapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.button.MaterialButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.limmonica.bookstoreapp.data.BookContract.BookEntry;

/**
 * {@link BookCursorAdapter} is an adapter for a list or grid view that uses a {@link Cursor}
 * of book data as its source.
 */
public class BookCursorAdapter extends CursorAdapter {

    private static final String CURRENCY = " $";
    private static final String PIECES = " pcs.";

    /**
     * Constructs a new {@link BookCursorAdapter}.
     *
     * @param context is the context of the app
     * @param cursor  is the cursor from which to get the data
     */
    BookCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    /**
     * Method inflates a new blank list item view and returns it.
     *
     * @param context is the context of the app
     * @param cursor  is the cursor from which to get the data. The cursor is already moved to the
     *                correct position.
     * @param parent  the parent to which the new view is attached to
     * @return the newly created list item view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Return the inflated list item view
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the book data (in the current row pointed to by the cursor) to the given
     * list item layout.
     *
     * @param view    existing view, returned by newView() method
     * @param context context of the app
     * @param cursor  the cursor from which to get the data. The cursor is already moved to the
     *                correct row
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find fields to populate in inflated template
        TextView titleTextView = view.findViewById(R.id.book_title_text_view);
        TextView priceTextView = view.findViewById(R.id.book_price_text_view);
        TextView quantityTextView = view.findViewById(R.id.book_quantity_text_view);
        MaterialButton saleButton = view.findViewById(R.id.sale_button);

        // Find the columns of book attributes that we're interested in
        int titleColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
        final int bookIdColumnIndex = cursor.getInt(cursor.getColumnIndex(BookEntry._ID));

        // Read the book attributes from the Cursor for the current book
        String bookTitle = cursor.getString(titleColumnIndex);
        int bookPrice = cursor.getInt(priceColumnIndex);
        final int bookQuantity = cursor.getInt(quantityColumnIndex);

        // Update the TextViews with the attributes for the current book
        titleTextView.setText(bookTitle);
        // Format the price by adding " $"
        String formatPrice = String.valueOf(bookPrice) + CURRENCY;
        // Set the text on the text view
        priceTextView.setText(formatPrice);
        // Format the quantity by adding " pcs."
        String formatQuantity = bookQuantity + PIECES;
        // Set the text on the text view
        quantityTextView.setText(String.valueOf(formatQuantity));

        // Set a click listener on the Sale Button
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the Uri of the current book on which the sale_button button was pressed
                Uri bookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, bookIdColumnIndex);
                // Update the quantity of the book on pressing button Sale
                setSale(context, bookUri, bookQuantity);
            }
        });
    }

    /**
     * Method to update the book quantity based on pressing the button Sale
     *
     * @param context          is the context of the app
     * @param bookUri          is the URI of the current book
     * @param existentQuantity is the available quantity of the current book
     */
    private void setSale(Context context, Uri bookUri, int existentQuantity) {

        // Initialize the remaining quantity of the book, after button sale_button is pressed
        int newQuantity;

        // If the available quantity is more than 1
        if (existentQuantity >= 1) {
            // Reduce it by 1
            newQuantity = existentQuantity - 1;
        } else {
            // Otherwise set it to 0
            newQuantity = 0;
        }

        // Create a new ContentValues object
        ContentValues values = new ContentValues();
        // Update the book quantity
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, newQuantity);

        // Get the number of rows affected in order to know if the operation was successful or not
        int rowsAffected = context.getContentResolver().update(bookUri, values, null, null);

        // If there are no rows affected
        if (rowsAffected == 0) {
            // Display a toast message saying there was an error with updating the sale_button
            Toast.makeText(context.getApplicationContext(), R.string.sale_update_failure, Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, show a toast message saying the sale_button update was successful
            Toast.makeText(context.getApplicationContext(), R.string.sale_update_success, Toast.LENGTH_SHORT).show();
        }
    }
}
