package com.comp1601.groceriesplus;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;
import java.util.List;

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
    public GroceryModel model;

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
            gList = (GroceryList) mIntent.getSerializableExtra(ToolBox.GLIST_EXTRA);
            model = (GroceryModel) mIntent.getSerializableExtra(ToolBox.MODEL_EXTRA);
        }
        else {
            gList = new GroceryList(ListType.GENERATED);
            model = null;
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

        //auto add item to empty glist
        if (gList.getGroceryItems().isEmpty()) {
            GroceryItem newGroceryItem = new GroceryItem();
            addGroceryItem(newGroceryItem);
        }

        // handlers
        mAddItemButton.setOnClickListener(view -> {
            GroceryItem newGroceryItem = new GroceryItem();
            addGroceryItem(newGroceryItem);
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

    private void addGroceryItem(GroceryItem newGroceryItem) {
        //clears focus from anything
        mListNameEditText.requestFocus();
        mListNameEditText.clearFocus();

        //update glist in db with what we currently have
        updateGListDB();

        //create new item and add it to the glist object and db
        //GroceryItem newGroceryItem = new GroceryItem();
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

    public void handleRemoveGItem(int position) {
        db.deleteGroceyItem(gList.getItem(position).getID(), gList.getID());
        gList.removeItem(position);

        //regular notify
        mGroceryItemAdapter.notifyDataSetChanged();

        //reload recycler view
        loadRecyclerView();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.grocery_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.grocery_generator_button) {
            Toast.makeText(this, "Generating Items . . .", Toast.LENGTH_SHORT).show();
            if (!gList.isActive()) {
                Toast.makeText(this, "This is an inactive List", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }
            if (model != null) {
                List<GroceryItem> generatedItems = model.generateGItems();

                if (generatedItems == null) {
                    Toast.makeText(this, "No Inactive Lists Available", Toast.LENGTH_SHORT).show();
                }
                else if (generatedItems.isEmpty()) {
                    Toast.makeText(this, "No Expired Items Available", Toast.LENGTH_SHORT).show();
                }
                else {
                    for (GroceryItem gi : generatedItems) {
                        addGroceryItem(gi);
                    }
                }
            }
            else {
                Toast.makeText(this, "Error Generating Items!!!", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
