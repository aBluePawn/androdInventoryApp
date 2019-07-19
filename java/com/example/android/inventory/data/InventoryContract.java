package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class InventoryContract {

    private InventoryContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";

    // set the base content uri
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class BooksEntry implements BaseColumns {

        //set the path for the inventory table
        public static final String PATH_INVENTORY_BOOKS = "books";

        //set the complete content uri
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY_BOOKS);

        //table name
        public static final String TABLE_NAME = "books";

        // id column
        public static final String _ID = BaseColumns._ID;

        // title column
        public static final String COLUMN_BOOKS_TITLE = "title";

        // author column
        public static final String COLUMN_BOOKS_AUTHOR = "author";

        // year published column
        public static final String COLUMN_BOOKS_YEAR_PUBLISHED = "year";

        // pages count column
        public static final String COLUMN_BOOKS_PAGES_COUNT = "pages";

        // price column
        public static final String COLUMN_BOOKS_PRICE = "price";

        // quantity column
        public static final String COLUMN_BOOKS_QUANTITY = "quantity";

        // image column
        public static final String COLUMN_BOOKS_IMAGE = "image";

        // The MIME type of the {@link #CONTENT_URI} for a list of books.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY_BOOKS;

        // The MIME type of the {@link #CONTENT_URI} for a single book.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY_BOOKS;
    }
}
