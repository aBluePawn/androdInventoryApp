package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int BOOK_LOADER = 1;

    // the uri for current book. If we add a new book the uri is set to null.
    // If we are editing a existing book then we will get the uri from the intent.
    private Uri currentBookUri;

    // declare the editText views that are used to collect data from the user

    private EditText titleEditText;
    private EditText authorEditText;
    private EditText yearEditText;
    private EditText pagesEditText;
    private EditText priceEditText;
    private EditText quantityEditText;

    // the field used to select the book cover image.
    // this spinner shows a list of images saved in the drawable folder
    private Spinner imageSpinner;

    // variable to hold the image name option
    String imageString = "no_image_available";

    // the String that is used to set the quantity column
    private String quantityString;

    // variable to check if any of the fields have been edited
    private boolean bookHasChanged = false;

    // check if the user changed any fields
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            bookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // get the data sent by the intent that opened this activity
        // if the intent has a book URI, we will edit the book
        // if no URI is present, we will add a new book
        Intent intent = getIntent();
        currentBookUri = intent.getData();

        // set the title of the activity to Add New Book
        if (currentBookUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_book));
            invalidateOptionsMenu();

            // set the title of the activity to Edit Book
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_book));
            getLoaderManager().initLoader(BOOK_LOADER, null, this);
        }

        // initialize the EditText views
        titleEditText = findViewById(R.id.edit_book_title);
        authorEditText = findViewById(R.id.edit_book_author);
        yearEditText = findViewById(R.id.edit_book_year);
        pagesEditText = findViewById(R.id.edit_book_pages);
        priceEditText = findViewById(R.id.edit_book_price);
        quantityEditText = findViewById(R.id.edit_book_quantity);

        // initialize the spinner to select the book cover image
        imageSpinner = findViewById(R.id.image_spinner);

        // set the listener for these views
        titleEditText.setOnTouchListener(touchListener);
        authorEditText.setOnTouchListener(touchListener);
        yearEditText.setOnTouchListener(touchListener);
        pagesEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);
        imageSpinner.setOnTouchListener(touchListener);

        // setup the image spinner
        setupSpinner();

        // the buttons to modify the quantity field
        // if the user specifies a value to modify the quantity field by, we will use that value.
        // if not, we will increase or decrease the quantity by 1
        Button increaseQuantityButton = findViewById(R.id.increase_quantity_button);
        Button decreaseQuantityButton = findViewById(R.id.decrease_quantity_button);

        // if this is a new book - no quantity available, we set the quantity to 0
        // so that the user can use the increase/decrease buttons to change this value.
        quantityString = quantityEditText.getText().toString().trim();
        if ((TextUtils.isEmpty(quantityString))) {
            quantityEditText.setText("0");
        }

        increaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // we increase the quantity, so we leave the number positive
                updateQuantity(1);
            }
        });

        decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // we decrease the quantity, so we make the number negative
                updateQuantity((-1));
            }
        });
    }

    private void setupSpinner() {
        // Create adapter for spinner. The list options are from a String array.
        ArrayAdapter imageSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_image_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        imageSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        imageSpinner.setAdapter(imageSpinnerAdapter);

        imageSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.dune_cover))) {
                        imageString = getString(R.string.dune_cover);
                    } else if (selection.equals(getString(R.string.google_logo))) {
                        imageString = getString(R.string.google_logo);
                    } else if (selection.equals(getString(R.string.android_dev))) {
                        imageString = getString(R.string.android_dev);
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                imageString = getString(R.string.no_image_available);
            }
        });
    }

    @Override
    public void onBackPressed() {
        //if the book hasn't changed, continue with handling back button press
        if (!bookHasChanged) {
            super.onBackPressed();
            return;
        }

        // otherwise if there are unsaved changes, setup a dialog to warn the user
        // create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // user clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentBookUri == null) {
            // If this is a new book, hide the "Delete" menu item.
            MenuItem menuItemDelete = menu.findItem(R.id.action_delete);
            menuItemDelete.setVisible(false);

            // If this is a new book, hide the "Order" menu item.
            MenuItem menuItemOrder = menu.findItem(R.id.action_order);
            menuItemOrder.setVisible(false);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // save book to database
                saveBook();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // show the delete confirmation dialog
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Order" menu option
            case R.id.action_order:
                orderBooks();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                //if the book hasn't changed, continue to
                // Navigate up to parent activity (MainActivity)
                if (!bookHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // user clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

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
        builder.setNegativeButton(R.string.cancel, null);

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveBook() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String titleString = titleEditText.getText().toString().trim();
        String authorString = authorEditText.getText().toString().trim();
        String yearString = yearEditText.getText().toString().trim();
        String pagesString = pagesEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        quantityString = quantityEditText.getText().toString().trim();

        // check if this is supposed to be a new book
        // and check if all the fields in the editor are blank
        if (currentBookUri == null &&
                TextUtils.isEmpty(titleString) &&
                TextUtils.isEmpty(authorString) &&
                TextUtils.isEmpty(yearString) &&
                TextUtils.isEmpty(pagesString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) &&
                imageString.equals("no_image_available")) {
            // there is no need to create a new book if there is no data in the editor's fields
            return;
        }

        // check if the title is added
        if (TextUtils.isEmpty(titleString)) {
            Toast.makeText(this, getString(R.string.editor_title_required), Toast.LENGTH_SHORT).show();
            return;
        }

        //check if the author is added
        if (TextUtils.isEmpty(authorString)) {
            Toast.makeText(this, getString(R.string.editor_author_required), Toast.LENGTH_SHORT).show();
            return;
        }

        //check if the year of publication is added
        if (TextUtils.isEmpty(yearString)) {
            Toast.makeText(this, getString(R.string.editor_year_required), Toast.LENGTH_SHORT).show();
            return;
        }
        // get the year input as an integer
        int yearOfPublication = Integer.parseInt(yearString);

        //check if the number of pages is added
        if (TextUtils.isEmpty(pagesString)) {
            Toast.makeText(this, getString(R.string.editor_pages_required), Toast.LENGTH_SHORT).show();
            return;
        }
        // get the number of pages input as an integer
        int pages = Integer.parseInt(pagesString);

        // next we check if the user had set the quantity field
        // set the initial default quantity value to 0
        // and after that we validate the user input
        int quantity = 0;

        //check if the quantity was changed, and update that value
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        // set the default price value to 0.0
        double price = 0.0;

        //check if the price was changed, and update that value
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }

        // add these strings to ContentValues, as keys - values pairs.
        ContentValues values = new ContentValues();
        values.put(InventoryContract.BooksEntry.COLUMN_BOOKS_TITLE, titleString);
        values.put(InventoryContract.BooksEntry.COLUMN_BOOKS_AUTHOR, authorString);
        values.put(InventoryContract.BooksEntry.COLUMN_BOOKS_YEAR_PUBLISHED, yearOfPublication);
        values.put(InventoryContract.BooksEntry.COLUMN_BOOKS_PAGES_COUNT, pages);
        values.put(InventoryContract.BooksEntry.COLUMN_BOOKS_PRICE, price);
        values.put(InventoryContract.BooksEntry.COLUMN_BOOKS_QUANTITY, quantity);
        values.put(InventoryContract.BooksEntry.COLUMN_BOOKS_IMAGE, imageString);

        if (currentBookUri == null) {
            // This is a NEW book, so insert a new book into the provider,
            // returning the content URI for the new book.
            Uri newUri = getContentResolver().insert(InventoryContract.BooksEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_book_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_book_successful), Toast.LENGTH_SHORT).show();
            }
            // this is an existing book, and we update the database.
        } else {
            int rowsAffected = getContentResolver().update(currentBookUri, values, null, null);

            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_book_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_book_successful), Toast.LENGTH_SHORT).show();
            }
        }
        // close the EditorActivity
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all book attributes, define a projection that contains
        // all columns from the book table
        String[] projection = {
                InventoryContract.BooksEntry._ID,
                InventoryContract.BooksEntry.COLUMN_BOOKS_TITLE,
                InventoryContract.BooksEntry.COLUMN_BOOKS_AUTHOR,
                InventoryContract.BooksEntry.COLUMN_BOOKS_PRICE,
                InventoryContract.BooksEntry.COLUMN_BOOKS_PAGES_COUNT,
                InventoryContract.BooksEntry.COLUMN_BOOKS_YEAR_PUBLISHED,
                InventoryContract.BooksEntry.COLUMN_BOOKS_QUANTITY,
                InventoryContract.BooksEntry.COLUMN_BOOKS_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentBookUri,         // Query the content URI for the current book
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;

        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            int titleColumnIndex = cursor.getColumnIndex(InventoryContract.BooksEntry.COLUMN_BOOKS_TITLE);
            int authorColumnIndex = cursor.getColumnIndex(InventoryContract.BooksEntry.COLUMN_BOOKS_AUTHOR);
            int yearColumnIndex = cursor.getColumnIndex(InventoryContract.BooksEntry.COLUMN_BOOKS_YEAR_PUBLISHED);
            int pagesColumnIndex = cursor.getColumnIndex(InventoryContract.BooksEntry.COLUMN_BOOKS_PAGES_COUNT);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.BooksEntry.COLUMN_BOOKS_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.BooksEntry.COLUMN_BOOKS_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(InventoryContract.BooksEntry.COLUMN_BOOKS_IMAGE);

            String title = cursor.getString(titleColumnIndex);
            String author = cursor.getString(authorColumnIndex);
            String year = cursor.getString(yearColumnIndex);
            String pages = cursor.getString(pagesColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String quantityString = cursor.getString(quantityColumnIndex);
            String imageName = cursor.getString(imageColumnIndex);

            titleEditText.setText(title);
            authorEditText.setText(author);
            yearEditText.setText(year);
            pagesEditText.setText(pages);
            priceEditText.setText(price);
            quantityEditText.setText(quantityString);

            //display the book cover image
            ImageView bookCover = findViewById(R.id.book_cover);
            int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());

            // if the imageName is not a valid file, display the broken image icon
            // validate the user input for image
            if (resId == 0) {
                imageName = getString(R.string.no_image_available);
                resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
            }
            bookCover.setImageResource(resId);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        titleEditText.setText("");
        authorEditText.setText("");
        yearEditText.setText("");
        pagesEditText.setText("");
        priceEditText.setText("0");
        quantityEditText.setText("");
        imageString = getString(R.string.no_image_available);
    }

    public void deleteBook() {
        // Only perform the delete if this is an existing book.
        if (currentBookUri != null) {
            // Call the ContentResolver to delete the book at the given content URI.
            // Pass in null for the selection and selection args because the currentBookUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(currentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful), Toast.LENGTH_SHORT).show();
            }
        }
        // Close the EditorActivity
        finish();
    }

    /**
     * this method is called when the user wants to order more copies of this book
     */
    public void orderBooks() {

        // get the author and title of current book
        String title = titleEditText.getText().toString().trim();
        String author = authorEditText.getText().toString().trim();

        // create the body of the message that will be sent with the intent
        String emailBody = getString(R.string.email_text) +
                "\nAuthor: " + author +
                "\nTitle: " + title +
                "\nQuantity: ";

        // create and start an implicit intent that sends an email to our book supplier
        Intent orderIntent = new Intent(Intent.ACTION_SEND);
        orderIntent.setType("message/rfc822");

        // add the supplier's email address to the intent
        orderIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.email_adress_supplier)});

        //set the email subject
        orderIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));

        // set the body of the email. We got the book details form the database,
        // and the user will have to add other details (like quantity) once the email app is open.
        orderIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        // the user will be asked to select an app to send the email with
        startActivity(Intent.createChooser(orderIntent, "Email:"));
    }

    /**
     * this method is called when the user wants to modify the number of copies of this book available for sale
     *
     * @param value the value to modify the quantity by.
     */
    public void updateQuantity(int value) {
        // get the current quantity
        quantityString = quantityEditText.getText().toString().trim();

        // configure the value to modify by
        // we look for the value from the user input
        // if none is found, we use 1
        int valueToModifyBy;
        EditText modifyQuantityBy = findViewById(R.id.edit_modify_quantity);
        String userInput = modifyQuantityBy.getText().toString().trim();

        if (userInput.matches("")) {
            valueToModifyBy = 1;
        } else {
            valueToModifyBy = Integer.parseInt(userInput);
        }
        // calculate the new quantity
        int quantity = Integer.parseInt(quantityString) + valueToModifyBy * value;

        // if quantity is negative, exit without saving the new value
        if (quantity < 0) {
            Toast.makeText(this, getString(R.string.editor_negative_quantity), Toast.LENGTH_SHORT).show();
            return;
        }
        // set the new value for the quantity field
        // when the book will be update into the database this is the value that will be used
        quantityEditText.setText(String.valueOf(quantity));
    }
}
