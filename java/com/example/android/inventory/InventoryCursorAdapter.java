package com.example.android.inventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        ImageView bookCover = view.findViewById(R.id.book_cover_main);
        TextView bookTitle = view.findViewById(R.id.title);
        TextView bookAuthor = view.findViewById(R.id.author);
        TextView bookQuantity = view.findViewById(R.id.quantity);
        TextView bookPrice = view.findViewById(R.id.price);

        String title = cursor.getString(cursor.getColumnIndex(InventoryContract.BooksEntry.COLUMN_BOOKS_TITLE));
        String author = cursor.getString(cursor.getColumnIndex(InventoryContract.BooksEntry.COLUMN_BOOKS_AUTHOR));
        final String quantity = cursor.getString(cursor.getColumnIndex(InventoryContract.BooksEntry.COLUMN_BOOKS_QUANTITY));
        String price = cursor.getString(cursor.getColumnIndex(InventoryContract.BooksEntry.COLUMN_BOOKS_PRICE));
        String imageName = cursor.getString(cursor.getColumnIndex(InventoryContract.BooksEntry.COLUMN_BOOKS_IMAGE));

        bookTitle.setText(title);
        bookAuthor.setText(author);
        bookQuantity.setText(quantity);
        bookPrice.setText(price);

        //display the book cover image
        int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        // if the imageName is not a valid file, display the broken image icon
        // validate the user input for image
        if (resId == 0) {
            imageName = "no_image_available";
            resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        }
        bookCover.setImageResource(resId);

        // set up the sale button
        Button saleButton = view.findViewById(R.id.sale_button);
        final int bookId = cursor.getInt(cursor.getColumnIndex(InventoryContract.BooksEntry._ID));
        final String initialQuantity = quantity;
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int currentQuantity = Integer.parseInt(initialQuantity);
                if (currentQuantity > 0) {

                    currentQuantity--;

                    ContentValues values = new ContentValues();
                    values.put(InventoryContract.BooksEntry.COLUMN_BOOKS_QUANTITY, currentQuantity);

                    String selection = InventoryContract.BooksEntry._ID + "=?";
                    String[] selectionArgs = new String[]{String.valueOf(bookId)};
                    context.getContentResolver().update(InventoryContract.BooksEntry.CONTENT_URI,
                            values, selection, selectionArgs);

                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.out_of_stock_quantity), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}