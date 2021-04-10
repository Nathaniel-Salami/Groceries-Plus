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
    //private LinearLayout mDueDateRow;
    private EditText mDueDateEditText;
    //private TextView mDueDateText;

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

        mAddItemButton = findViewById(R.id.addItemButton);
        mListNameEditText = findViewById(R.id.listNameEditText);
        mListActiveSwitch = findViewById(R.id.activeSwitch);

        mDueDateButton = findViewById(R.id.dueDateButton);
        mDueDateEditText = findViewById(R.id.dueDateEditText);


        //basic setters
        mListNameEditText.setText(gList.getName());

        mListActiveSwitch.setChecked(gList.isActive());

        String dueDate = gList.getDueDate() != null ?
                ToolBox.dateToUiString(gList.getDueDate()) : "";
        mDueDateEditText.setText(dueDate);

        //recycler view launch
        mRecyclerView.setAdapter(mGroceryItemAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(mRecyclerView);

        //auto add item to empty glist
        if (gList.getGroceryItems().isEmpty() && gList.isActive()) {
            GroceryItem newGroceryItem = new GroceryItem();
            addGroceryItem(newGroceryItem);
        }

        // handlers
        mAddItemButton.setOnClickListener(view -> {
            if (gList.isActive()) {
                GroceryItem newGroceryItem = new GroceryItem();
                addGroceryItem(newGroceryItem);
            }
            else {
                Toast.makeText(this, getString(R.string.inactiveListWarning), Toast.LENGTH_SHORT).show();
            }
        });

        mListActiveSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            gList.setActive(mListActiveSwitch.isChecked());
        });
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
