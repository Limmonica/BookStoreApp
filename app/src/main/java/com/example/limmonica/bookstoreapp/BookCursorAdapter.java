package com.example.limmonica.bookstoreapp;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.limmonica.bookstoreapp.data.BookContract.BookEntry;

/**
 * {@link BookCursorAdapter} is an adapter for a list or grid view that uses a {@link Cursor}
 * of book data as its source.
 */
public class BookCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link BookCursorAdapter}.
     *
     * @param context is the context of the app
     * @param cursor  is the cursor from which to get the data
     */
    public BookCursorAdapter(Context context, Cursor cursor) {
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
    public void bindView(View view, Context context, Cursor cursor) {

        // Find fields to populate in inflated template
        TextView titleTextView = view.findViewById(R.id.book_title_text_view);
        TextView priceTextView = view.findViewById(R.id.book_price_text_view);
        TextView quantityTextView = view.findViewById(R.id.book_quantity_text_view);
        TextView supplierTextView = view.findViewById(R.id.book_supplier_text_view);
        TextView orderTextView = view.findViewById(R.id.book_order_text_view);

        // Find the columns of book attributes that we're interested in
        int titleColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
        int supplierColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
        int orderColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER);

        // Read the book attributes from the Cursor for the current book
        String bookTitle = cursor.getString(titleColumnIndex);
        String bookPrice = cursor.getString(priceColumnIndex);
        String bookQuantity = cursor.getString(quantityColumnIndex);
        String bookSupplier = cursor.getString(supplierColumnIndex);
        String bookOrder = cursor.getString(orderColumnIndex);

        // If the supplier's name is empty or null, use default text
        if (TextUtils.isEmpty(bookSupplier)) {
            bookSupplier = context.getString(R.string.unknown_supplier);
        }

        // If the supplier's phone number is empty or null, use default text
        if (TextUtils.isEmpty(bookOrder)) {
            bookOrder = context.getString(R.string.not_available);
        }

        // Update the TextViews with the attributes for the current book
        titleTextView.setText(bookTitle);
        priceTextView.setText(bookPrice);
        quantityTextView.setText(bookQuantity);
        supplierTextView.setText(bookSupplier);
        orderTextView.setText(bookOrder);
    }
}
