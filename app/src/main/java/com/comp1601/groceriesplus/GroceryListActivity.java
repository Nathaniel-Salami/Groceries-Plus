package com.comp1601.groceriesplus;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GroceryListActivity extends AppCompatActivity {

    public RecyclerView mRecyclerView;
    public GroceryItemAdapter mGroceryItemAdapter;
    public EditText mListNameEditText;
    public EditText mDueDateEditText;
    public SwitchMaterial mListActiveSwitch;

    public GroceryList gList;

    public GroceryPlusDbHelper db;

    private FloatingActionButton mAddItemButton;

    //for swipe to delete in recycler view
    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            //Toast.makeText(ListActivity.this, "on Move", Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            //Toast.makeText(ListActivity.this, "on Swiped ", Toast.LENGTH_SHORT).show();
            //Remove swiped item from list and notify the RecyclerView
            int position = viewHolder.getAdapterPosition();
            db.deleteGroceyItem(gList.getItem(position).getID(), gList.getID());
            gList.removeItem(position);

            //regular notify
            mGroceryItemAdapter.notifyDataSetChanged();

            //reload recycler view
            loadRecyclerView();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);

        db = new GroceryPlusDbHelper(this);

        // load list from intent
        Intent mIntent = getIntent();
        if(mIntent != null) {
            gList = (GroceryList) mIntent.getSerializableExtra(ToolBox.LIST_EXTRA);
        }
        else {
            gList = new GroceryList(ListType.GENERATED);
        }

        mRecyclerView = findViewById(R.id.itemRecyclerView);
        mAddItemButton = findViewById(R.id.addItemButton);
        mListNameEditText = findViewById(R.id.listNameEditText);
        mDueDateEditText = findViewById(R.id.dueDateTextView);
        mListActiveSwitch = findViewById(R.id.activeSwitch);

        //basic setters
        mListNameEditText.setText(gList.getName());
        mListActiveSwitch.setChecked(gList.isActive());
        mDueDateEditText.setText(ToolBox.dateToUiString(gList.getDueDate()));

        //recycler view launch
        loadRecyclerView();
        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(mRecyclerView);

        //auto add item to empty glist
        if (gList.getGroceryItems().isEmpty()) {
            addNewItem();
        }

        // handlers
        mAddItemButton.setOnClickListener(view -> {
            addNewItem();
        });

        mDueDateEditText.setOnFocusChangeListener((view, b) -> {
            //ToolBox.handleDatePicker(this, mDueDateEditText, gList.getDueDate());
            handleDueDatePicker();
        });
        mDueDateEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            public void afterTextChanged(Editable s) {
                // set oid value now
                gList.setDueDate(ToolBox.uiStringToDate(mDueDateEditText.getText().toString()));
            }
        });
    }

    private void loadRecyclerView() {
        mGroceryItemAdapter = new GroceryItemAdapter(this, gList);
        mRecyclerView.setAdapter(mGroceryItemAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void addNewItem() {
        //clears focus from anything
        mListNameEditText.requestFocus();
        mListNameEditText.clearFocus();

        //update glist in db with what we currently have
        updateGListDB();

        //create new item and add it to the glist object and db
        GroceryItem newGroceryItem = new GroceryItem();
        gList.addItem(newGroceryItem);
        db.insertNewGroceryItem(newGroceryItem, gList.getID());

        // regular notify
        //mGroceryItemAdapter.notifyItemInserted(mGroceryItemAdapter.getItemCount() - 1);
        //mGroceryItemAdapter.notifyDataSetChanged();

        //load up new recycler view
        loadRecyclerView();

        //scroll to bottom where new item should be
        mRecyclerView.scrollToPosition(gList.getTotalItemCount() - 1);
    }

    public void updateGListDB() {
        Log.i("TEST", "updateGListDB");
        Log.i("TEST", gList.toString());

        gList.setName(mListNameEditText.getText().toString());
        gList.setDueDate(ToolBox.uiStringToDate(mDueDateEditText.getText().toString()));
        gList.setActive(mListActiveSwitch.isChecked());

        db.updateGroceryList(gList);
    }

    @Override
    public void onBackPressed() {
        // update grocery list model object
        updateGListDB();

        super.onBackPressed();
    }

    public void handleDueDatePicker() {
        final Calendar myCalendar = Calendar.getInstance();

        if (gList.getDueDate() != null) {
            myCalendar.setTime(gList.getDueDate());
        }

        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            //format date
            String date1 = ToolBox.dateToUiString(myCalendar.getTime());
            mDueDateEditText.setText(date1);

            gList.setDueDate(myCalendar.getTime());
        };

        mDueDateEditText.setOnClickListener(view -> {
            new DatePickerDialog(this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }
}
