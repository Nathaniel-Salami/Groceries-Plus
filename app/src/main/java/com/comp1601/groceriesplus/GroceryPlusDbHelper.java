package com.comp1601.groceriesplus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GroceryPlusDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "GroceryPlus.db";

    public GroceryPlusDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void resetDatabase() {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(GroceryPlusContract.SQL_DELETE_LIST_TABLE);
        database.execSQL(GroceryPlusContract.SQL_DELETE_ITEM_HISTORY_TABLE);

        database.execSQL(GroceryPlusContract.SQL_CREATE_LIST_TABLE);
        database.execSQL(GroceryPlusContract.SQL_CREATE_ITEM_HISTORY_TABLE);
        database.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(GroceryPlusContract.SQL_CREATE_LIST_TABLE);
        //db.execSQL(GroceryPlusContract.SQL_CREATE_ITEM_TABLE);
        db.execSQL(GroceryPlusContract.SQL_CREATE_ITEM_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(GroceryPlusContract.SQL_DELETE_ITEM_HISTORY_TABLE);
        //db.execSQL(GroceryPlusContract.SQL_DELETE_ITEM_TABLE);
        db.execSQL(GroceryPlusContract.SQL_DELETE_LIST_TABLE);
        onCreate(db);
    }

    public long insertNewGroceryList(GroceryList gList) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(GroceryPlusContract.GroceryLists.LIST_NAME, gList.getName());
        values.put(GroceryPlusContract.GroceryLists.TYPE, gList.getListType().getIntValue());
        values.put(GroceryPlusContract.GroceryLists.POSITION, gList.getPosition());

        int activeVal = gList.isActive() ? 1 : 0;
        values.put(GroceryPlusContract.GroceryLists.ACTIVE, activeVal);

        values.put(GroceryPlusContract.GroceryLists.CREATED_DATE, ToolBox.dateToDbString(gList.getCreatedDate()));
        values.put(GroceryPlusContract.GroceryLists.DUE_DATE, ToolBox.dateToDbString(gList.getDueDate()));
        values.put(GroceryPlusContract.GroceryLists.ACTIVE_DATE, ToolBox.dateToDbString(gList.getActiveDate()));

        // Insert the new row, returning the primary key value of the new row
        long newGListId = db.insert(GroceryPlusContract.GroceryLists.TABLE_NAME, null, values);

        return newGListId;
    }

    public Boolean updateGroceryList(GroceryList gList) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(GroceryPlusContract.GroceryLists.LIST_NAME, gList.getName());
        values.put(GroceryPlusContract.GroceryLists.TYPE, gList.getListType().getIntValue());
        values.put(GroceryPlusContract.GroceryLists.POSITION, gList.getPosition());

        int activeVal = gList.isActive() ? 1 : 0;
        values.put(GroceryPlusContract.GroceryLists.ACTIVE, activeVal);

        values.put(GroceryPlusContract.GroceryLists.CREATED_DATE, ToolBox.dateToDbString(gList.getCreatedDate()));
        values.put(GroceryPlusContract.GroceryLists.DUE_DATE, ToolBox.dateToDbString(gList.getDueDate()));
        values.put(GroceryPlusContract.GroceryLists.ACTIVE_DATE, ToolBox.dateToDbString(gList.getActiveDate()));

        // Which row to update, based on the title
        String selection = GroceryPlusContract.GroceryLists.LIST_ID + " = ?";
        String[] selectionArgs = { "" + gList.getID() };

        int count = db.update(
                GroceryPlusContract.GroceryLists.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        //update grocewry items
        this.updateGroceryItems(gList);

        return count != 0;
    }

    public List<GroceryList> selectAllGroceryList() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from " + GroceryPlusContract.GroceryLists.TABLE_NAME,null);

        List<GroceryList> gLists = new ArrayList<>();

        while(cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(GroceryPlusContract.GroceryLists.LIST_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(GroceryPlusContract.GroceryLists.LIST_NAME));
            int active = cursor.getInt(cursor.getColumnIndexOrThrow(GroceryPlusContract.GroceryLists.ACTIVE));
            int type = cursor.getInt(cursor.getColumnIndexOrThrow(GroceryPlusContract.GroceryLists.TYPE));
            String createdDate = cursor.getString(cursor.getColumnIndexOrThrow(GroceryPlusContract.GroceryLists.CREATED_DATE));
            String dueDate = cursor.getString(cursor.getColumnIndexOrThrow(GroceryPlusContract.GroceryLists.DUE_DATE));
            String activeDate = cursor.getString(cursor.getColumnIndexOrThrow(GroceryPlusContract.GroceryLists.ACTIVE_DATE));
            int position = cursor.getInt(cursor.getColumnIndexOrThrow(GroceryPlusContract.GroceryLists.POSITION));

            // create the grocery list then get and add its items
            GroceryList temp = new GroceryList(id, name, active, type, createdDate, dueDate, activeDate, position);
            List<GroceryItem> tempGroceryItems = this.selectAllGroceryItems(temp.getID());
            tempGroceryItems.sort(Comparator.comparing(GroceryItem::getPosition));

            temp.setGroceryItems(tempGroceryItems);

            gLists.add(temp);
        }
        cursor.close();

        return gLists;
    }

    public boolean deleteGroceryList(GroceryList gList) {
        SQLiteDatabase db = this.getReadableDatabase();

        //delete items first
        for (GroceryItem groceryItem : gList.getGroceryItems()) {
            this.deleteGroceyItem(groceryItem.getID(), gList.getID());
        }

        // Define 'where' part of query.
        String selection = GroceryPlusContract.GroceryLists.LIST_ID + " = ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = { "" + gList.getID() };

        // Issue SQL statement.
        int deletedRows = db.delete(GroceryPlusContract.GroceryLists.TABLE_NAME, selection, selectionArgs);

        return deletedRows != 0;
    }

    public long insertNewGroceryItem(GroceryItem newGroceryItem, long gListID) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        //values.put(GroceryPlusContract.ItemHistory.ITEM_ID, newItem.getID());
        values.put(GroceryPlusContract.ItemHistory.LIST_ID, gListID);
        values.put(GroceryPlusContract.ItemHistory.ITEM_NAME, newGroceryItem.getName());
        values.put(GroceryPlusContract.ItemHistory.QUANTITY, newGroceryItem.getQuantity());
        values.put(GroceryPlusContract.ItemHistory.POSITION, newGroceryItem.getPosition());

        int foundVal = newGroceryItem.isFound() ? 1 : 0;
        values.put(GroceryPlusContract.ItemHistory.FOUND, foundVal);

        values.put(GroceryPlusContract.ItemHistory.EXPIRY_DATE, ToolBox.dateToDbString(newGroceryItem.getExpiryDate()));
        values.put(GroceryPlusContract.ItemHistory.RESTOCK_DATE, ToolBox.dateToDbString(newGroceryItem.getRestockDate()));
        values.put(GroceryPlusContract.ItemHistory.FOUND_DATE, ToolBox.dateToDbString(newGroceryItem.getFoundDate()));

        // Insert the new row, returning the primary key value of the new row
        long newItemID = db.insert(GroceryPlusContract.ItemHistory.TABLE_NAME, null, values);

        return newItemID;
    }

    public boolean deleteGroceyItem(long itemID, long listID) {
        // delete from history table
        SQLiteDatabase db = this.getReadableDatabase();

        // Define 'where' part of query.
        String selection = GroceryPlusContract.ItemHistory.ITEM_ID + " = ? ";
        selection += "AND ";
        selection += GroceryPlusContract.ItemHistory.LIST_ID + " = ? ";

        // Specify arguments in placeholder order.
        String[] selectionArgs = { "" + itemID, "" + listID };

        // Issue SQL statement.
        int deletedRows = db.delete(GroceryPlusContract.ItemHistory.TABLE_NAME, selection, selectionArgs);

        return deletedRows != 0;
    }

    public List<GroceryItem> selectAllGroceryItems(long gListID) {
        //get all items from itemshistory eith list id
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = GroceryPlusContract.ItemHistory.LIST_ID + " = ? ";
        String[] selectionArgs = { "" + gListID };

        Cursor cursor = db.query(
                GroceryPlusContract.ItemHistory.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        List<GroceryItem> itemsList = new ArrayList<>();

        while(cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(GroceryPlusContract.ItemHistory.ITEM_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(GroceryPlusContract.ItemHistory.ITEM_NAME));
            int found = cursor.getInt(cursor.getColumnIndexOrThrow(GroceryPlusContract.ItemHistory.FOUND));
            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(GroceryPlusContract.ItemHistory.QUANTITY));
            String foundDate = cursor.getString(cursor.getColumnIndexOrThrow(GroceryPlusContract.ItemHistory.FOUND_DATE));
            String expiryDate = cursor.getString(cursor.getColumnIndexOrThrow(GroceryPlusContract.ItemHistory.EXPIRY_DATE));
            String restockDate = cursor.getString(cursor.getColumnIndexOrThrow(GroceryPlusContract.ItemHistory.RESTOCK_DATE));
            int position = cursor.getInt(cursor.getColumnIndexOrThrow(GroceryPlusContract.ItemHistory.POSITION));

            GroceryItem temp = new GroceryItem(id, name, found, quantity, foundDate, expiryDate, restockDate, position);

            itemsList.add(temp);
        }

        cursor.close();

        return itemsList;
    }

    public Boolean updateGroceryItem(GroceryItem gItem) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(GroceryPlusContract.ItemHistory.ITEM_ID, gItem.getID());
        values.put(GroceryPlusContract.ItemHistory.ITEM_NAME, gItem.getName());
        values.put(GroceryPlusContract.ItemHistory.FOUND, gItem.isFound() ? 1 : 0);
        values.put(GroceryPlusContract.ItemHistory.QUANTITY, gItem.getQuantity());
        values.put(GroceryPlusContract.ItemHistory.FOUND_DATE, ToolBox.dateToDbString(gItem.getFoundDate()));
        values.put(GroceryPlusContract.ItemHistory.EXPIRY_DATE, ToolBox.dateToDbString(gItem.getExpiryDate()));
        values.put(GroceryPlusContract.ItemHistory.RESTOCK_DATE, ToolBox.dateToDbString(gItem.getRestockDate()));
        values.put(GroceryPlusContract.ItemHistory.POSITION, gItem.getPosition());

        // Which row to update, based on the title
        String selection = GroceryPlusContract.ItemHistory.ITEM_ID + " = ?";
        String[] selectionArgs = { "" + gItem.getID() };

        int count = db.update(
                GroceryPlusContract.ItemHistory.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        return count != 0;
    }

    //BETTER VERSION
    public void updateGroceryItems(GroceryList gList) {
        // delete all from db
        for (GroceryItem groceryItem : gList.getGroceryItems()) {
            this.updateGroceryItem(groceryItem);
        }
    }

    // VERY POOR IMPLEMENTATION
    public void updateGroceryItemsOld(GroceryList gList) {
        // delete all from db
        for (GroceryItem groceryItem : gList.getGroceryItems()) {
            this.deleteGroceyItem(groceryItem.getID(), gList.getID());
        }

        //insert all back in
        for (GroceryItem groceryItem : gList.getGroceryItems()) {
            this.insertNewGroceryItem(groceryItem, gList.getID());
        }
    }
}
