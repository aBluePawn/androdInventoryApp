package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
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

import com.example.android.inventory.data.InventoryContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    // LoaderManager can handle multiple loaders, so each will have a unique identifier.
    private static final int BOOK_LOADER = 1;

    InventoryCursorAdapter bookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newBookIntent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(newBookIntent);
            }
        });

        // the list view that will be used with the CursorAdapter
        ListView bookListView = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);

        getLoaderManager().initLoader(BOOK_LOADER, null, this);

        // the book Adapter that will populate the list view
        bookAdapter = new InventoryCursorAdapter(this, null, 0);

        bookListView.setAdapter(bookAdapter);

        // Setup the item click listener
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent bookDetailsIntent = new Intent(getApplicationContext(), EditorActivity.class);

                // Form the content URI that represents the specific book that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link BookEntry#CONTENT_URI}.
                Uri currentBookUri = ContentUris.withAppendedId(InventoryContract.BooksEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                bookDetailsIntent.setData(currentBookUri);

                // Launch the {@link EditorActivity} to display the data for the current book.
                startActivity(bookDetailsIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void insertBook() {

        // create the values for inserting some dummy data
        ContentValues values = new ContentValues();
        values.put(InventoryContract.BooksEntry.COLUMN_BOOKS_TITLE, "Dune");
        values.put(InventoryContract.BooksEntry.COLUMN_BOOKS_AUTHOR, "Frank Herbert");
        values.put(InventoryContract.BooksEntry.COLUMN_BOOKS_QUANTITY, "22");
        values.put(InventoryContract.BooksEntry.COLUMN_BOOKS_PRICE, "2");
        values.put(InventoryContract.BooksEntry.COLUMN_BOOKS_YEAR_PUBLISHED, "1965");
        values.put(InventoryContract.BooksEntry.COLUMN_BOOKS_IMAGE, getString(R.string.dune_cover));
        values.put(InventoryContract.BooksEntry.COLUMN_BOOKS_PAGES_COUNT, "412");

        // insert the values into the database
        getContentResolver().insert(InventoryContract.BooksEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertBook();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // set up the database query for the info that will be displayed into the listView for each book
        String[] projection = {
                InventoryContract.BooksEntry._ID,
                InventoryContract.BooksEntry.COLUMN_BOOKS_TITLE,
                InventoryContract.BooksEntry.COLUMN_BOOKS_AUTHOR,
                InventoryContract.BooksEntry.COLUMN_BOOKS_QUANTITY,
                InventoryContract.BooksEntry.COLUMN_BOOKS_PRICE,
                InventoryContract.BooksEntry.COLUMN_BOOKS_IMAGE
        };

        return new CursorLoader(this,
                InventoryContract.BooksEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        bookAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookAdapter.changeCursor(null);
    }
}