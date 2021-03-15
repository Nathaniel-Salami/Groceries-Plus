package com.comp1601.groceriesplus;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GroceryListActivity extends AppCompatActivity {

    public RecyclerView mRecyclerView;
    public ItemAdapter mItemAdapter;
    public EditText mListNameEditText;
    public EditText mDueDateEditText;
    public SwitchMaterial mListActiveSwitch;

    public GroceryList gList;

    public GroceryPlusDbHelper db;

    private FloatingActionButton mAddItemButton;

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
        mItemAdapter = new ItemAdapter(this, gList);
        mRecyclerView.setAdapter(mItemAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


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

    private void addNewItem() {
        //Fixes weird bugs when clicking add button while a recycler view edit text has focus
        mListNameEditText.requestFocus();
        mListNameEditText.clearFocus();

        gList = mItemAdapter.getGroceryList();

        GroceryItem newGroceryItem = new GroceryItem();

        long id = db.insertNewGroceryItem(newGroceryItem, gList.getID());
        newGroceryItem.setID(id);

        gList.addItem(newGroceryItem);

        mItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        // update grocery list model object
        gList.setName(mListNameEditText.getText().toString());
        gList.setActive(mListActiveSwitch.isChecked());

        Log.i("TEST", gList.toString());
        db.updateGroceryList(gList);

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
