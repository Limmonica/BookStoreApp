package com.example.limmonica.bookstoreapp.data;

import android.provider.BaseColumns;

/**
 * API Contract for the Pets app.
 * Final class for providing constants
 */
public final class BookContract {

    /**
     * Empty constructor to prevent accidental instantiating of the contract class
     */
    private BookContract() {
    }

    /**
     * Inner class which defines constant values for the books database table.
     * Each entry in the table represents a single book.
     */
    public static final class BookEntry implements BaseColumns {

        /**
         * Name of the database table for books
         */
        public final static String TABLE_NAME = "books";

        /**
         * Unique ID number for the book (only for use in the database table)
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Title of the book.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_BOOK_PRODUCT_NAME = "name";

        /**
         * Price of the book.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_BOOK_PRICE = "price";

        /**
         * Quantity of the book.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_BOOK_QUANTITY = "quantity";

        /**
         * Supplier of the book.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_BOOK_SUPPLIER_NAME = "supplier";

        /**
         * Supplier phone number.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_BOOK_SUPPLIER_PHONE_NUMBER = "phone";
    }
}
