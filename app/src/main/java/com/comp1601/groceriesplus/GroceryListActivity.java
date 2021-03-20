package com.comp1601.groceriesplus;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
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
    public SwitchMaterial mListActiveSwitch;

    private ImageButton mDueDateButton;
    private LinearLayout mDueDateRow;
    private TextView mDueDateText;

    private ItemTouchHelper mItemTouchHelper;

    public GroceryList gList;
    public GroceryModel model;

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
            int position = viewHolder.getAdapterPosition();
            handleRemoveGItem(position);
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
            gList = (GroceryList) mIntent.getSerializableExtra(ToolBox.GLIST_EXTRA);
            model = (GroceryModel) mIntent.getSerializableExtra(ToolBox.MODEL_EXTRA);
        }
        else {
            gList = new GroceryList(ListType.GENERATED);
            model = null;
        }

        //initialize recycler view
        mRecyclerView = findViewById(R.id.itemRecyclerView);
        mGroceryItemAdapter = new GroceryItemAdapter(this, gList);
        mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

        mAddItemButton = findViewById(R.id.addItemButton);
        mListNameEditText = findViewById(R.id.listNameEditText);
        mListActiveSwitch = findViewById(R.id.activeSwitch);

        mDueDateButton = findViewById(R.id.dueDateButton);
        mDueDateRow = findViewById(R.id.dueDateRow);
        mDueDateText = findViewById(R.id.dueDateText);


        //basic setters
        mListNameEditText.setText(gList.getName());

        String dueDate = gList.getDueDate() != null ?
                ToolBox.dateToUiString(gList.getDueDate()) : "";
        mDueDateText.setText(dueDate);

        mAddItemButton.setEnabled(gList.isActive());

        mListActiveSwitch.setChecked(gList.isActive());

        //recycler view launch
        mRecyclerView.setAdapter(mGroceryItemAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mItemTouchHelper.attachToRecyclerView(gList.isActive() ? mRecyclerView : null);

        //handle if glist is active
        handleGListActive();

        //auto add item to empty glist
        if (gList.getGroceryItems().isEmpty() && gList.isActive()) {
            GroceryItem newGroceryItem = new GroceryItem();
            addGroceryItem(newGroceryItem);
        }

        // handlers
        mAddItemButton.setOnClickListener(view -> {
            GroceryItem newGroceryItem = new GroceryItem();
            addGroceryItem(newGroceryItem);
        });

        mDueDateRow.setOnClickListener(v -> {
            handleDueDatePicker();
        });

        mListActiveSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            gList.setActive(mListActiveSwitch.isChecked());

            handleGListActive();
        });

        final Calendar myCalendar = Calendar.getInstance();

        if (gList.getDueDate() != null) {
            myCalendar.setTime(gList.getDueDate());
        }
    }

    private void handleGListActive() {
        mAddItemButton.setEnabled(gList.isActive());
        mDueDateRow.setEnabled(gList.isActive());
        mDueDateText.setEnabled(gList.isActive());
        mListNameEditText.setEnabled(gList.isActive());

        //findViewById(R.id.grocery_generator_button).setEnabled(gList.isActive());

        //reload recycler view
        mGroceryItemAdapter = new GroceryItemAdapter(this, gList);
        mRecyclerView.setAdapter(mGroceryItemAdapter);
        mItemTouchHelper.attachToRecyclerView(gList.isActive() ? mRecyclerView : null);
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
        mGroceryItemAdapter.notifyItemInserted(mGroceryItemAdapter.getItemCount() - 1);
        mGroceryItemAdapter.notifyDataSetChanged();

        //load up new recycler view
        //loadRecyclerView();

        //scroll to bottom where new item should be
        mRecyclerView.scrollToPosition(gList.getTotalItemCount() - 1);
    }

    public void updateGListDB() {
        Log.i("TEST", "updateGListDB");
        Log.i("TEST", gList.toString());

        gList.setName(mListNameEditText.getText().toString());
        gList.setDueDate(ToolBox.uiStringToDate(mDueDateText.getText().toString()));
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
        //loadRecyclerView();
    }

    public void handleDueDatePicker() {
        final Calendar myCalendar = Calendar.getInstance();

        if (gList.getDueDate() != null)
            myCalendar.setTime(gList.getDueDate());

        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            //format date
            String pickedDate = ToolBox.dateToUiString(myCalendar.getTime());
            mDueDateText.setText(pickedDate);

            gList.setDueDate(myCalendar.getTime());
        };

        DatePickerDialog dateDialog = new DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));

        dateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.clearDate), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    mDueDateText.setText("");
                    gList.setDueDate(null);
                }
            }
        });

        dateDialog.show();
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
            //Toast.makeText(this, "Generating Items . . .", Toast.LENGTH_SHORT).show();
            if (!gList.isActive()) {
                Toast.makeText(this, getString(R.string.inactiveListWarning), Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }
            if (model != null) {
                List<GroceryItem> generatedItems = model.generateGItems();

                if (generatedItems == null) {
                    Toast.makeText(this, getString(R.string.noInactiveListWarning), Toast.LENGTH_SHORT).show();
                }
                else if (generatedItems.isEmpty()) {
                    Toast.makeText(this, getString(R.string.noGenerateItemsWarning), Toast.LENGTH_SHORT).show();
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
