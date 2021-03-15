package com.comp1601.groceriesplus;

import android.provider.BaseColumns;

public final class GroceryPlusContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private GroceryPlusContract() {}

    /* Inner class that defines the table contents */
    public static class GroceryLists implements BaseColumns {
        public static final String TABLE_NAME = "grocery_lists";
        public static final String LIST_ID = "list_id";
        public static final String CREATED_DATE = "created_date";
        public static final String DUE_DATE = "due_date";
        public static final String ACTIVE_DATE = "active_date";
        public static final String LIST_NAME = "list_name";
        public static final String TYPE = "list_type";
        public static final String ACTIVE = "is_active";
        public static final String POSITION = "list_position";
    }

    /*public static class GroceryItems implements BaseColumns {
        public static final String TABLE_NAME = "grocery_items";
        public static final String ITEM_ID = "item_id";
        public static final String ITEM_NAME = "item_name";
    }*/

    public static class ItemHistory implements BaseColumns {
        public static final String TABLE_NAME = "item_history";
        public static final String ITEM_ID = "item_id";
        public static final String ITEM_NAME = "item_name";
        public static final String LIST_ID = "list_id";
        public static final String FOUND = "is_found";
        public static final String FOUND_DATE = "found_date";
        public static final String QUANTITY = "quantity";
        public static final String EXPIRY_DATE = "expiry_date";
        public static final String RESTOCK_DATE = "restock_date";
        public static final String POSITION = "item_position";
    }

    public static final String SQL_CREATE_LIST_TABLE =
            "CREATE TABLE " + GroceryLists.TABLE_NAME + " (" +
                    GroceryLists.LIST_ID + " INTEGER PRIMARY KEY, " +
                    GroceryLists.CREATED_DATE + " TEXT, " +
                    GroceryLists.DUE_DATE + " TEXT, " +
                    GroceryLists.ACTIVE_DATE + " TEXT, " +
                    GroceryLists.LIST_NAME + " TEXT, " +
                    GroceryLists.TYPE + " INTEGER, " +
                    GroceryLists.POSITION + " INTEGER, " +
                    GroceryLists.ACTIVE + " INTEGER)"; // 1: True, 0: False

    /*public static final String SQL_CREATE_ITEM_TABLE =
            "CREATE TABLE " + GroceryItems.TABLE_NAME + " (" +
                    GroceryItems.ITEM_ID + " INTEGER PRIMARY KEY," +
                    GroceryItems.ITEM_NAME + " TEXT)";*/

    public static final String SQL_CREATE_ITEM_HISTORY_TABLE =
            "CREATE TABLE " + ItemHistory.TABLE_NAME + " (" +
                    ItemHistory.ITEM_ID + " INTEGER PRIMARY KEY, " +
                    ItemHistory.LIST_ID + " INTEGER, " +
                    ItemHistory.ITEM_NAME + " TEXT, " +
                    ItemHistory.FOUND + " INTEGER, " + // 1: True, 0: False
                    ItemHistory.FOUND_DATE + " TEXT, " +
                    ItemHistory.QUANTITY + " INTEGER, " +
                    ItemHistory.EXPIRY_DATE + " TEXT, " +
                    ItemHistory.POSITION + " INTEGER, " +
                    ItemHistory.RESTOCK_DATE + " TEXT)";

    public static final String SQL_DELETE_LIST_TABLE =
            "DROP TABLE IF EXISTS " + GroceryLists.TABLE_NAME;

    /*public static final String SQL_DELETE_ITEM_TABLE =
            "DROP TABLE IF EXISTS " + GroceryItems.TABLE_NAME;*/

    public static final String SQL_DELETE_ITEM_HISTORY_TABLE =
            "DROP TABLE IF EXISTS " + ItemHistory.TABLE_NAME;

}
