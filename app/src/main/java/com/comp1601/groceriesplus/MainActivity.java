package com.comp1601.groceriesplus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    public RecyclerView mRecyclerView;

    public GroceryListAdapter mGLAdapter;
    public GroceryModel model;
    public GroceryPlusDbHelper db;

    private FloatingActionButton mAddListButton;

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
            db.deleteGroceryList(model.getGroceryList(position));
            model.removeGroceryList(position);
            mGLAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        model = new GroceryModel();
        //model = GroceryModel.makeDemo();
        db = new GroceryPlusDbHelper(this);

        //reset database
        //db.resetDatabase();

        mRecyclerView = findViewById(R.id.listRecyclerView);
        mAddListButton = findViewById(R.id.addListButton);

        model.setGListArrayList(db.selectAllGroceryList());

        //launch recycler view
        mGLAdapter = new GroceryListAdapter(this, model);
        mRecyclerView.setAdapter(mGLAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(mRecyclerView);

        // handlers
        mAddListButton.setOnClickListener(view -> {
            handleAddList();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.settings_button) {
            Toast.makeText(this, "Open Settings", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    //THIS IS MESSY, FIX THIS
    //create and new glist and launch glist activity
    private void handleAddList() {
        //add list to model
        GroceryList newList = new GroceryList(ListType.USER_CREATED);
        model.addGroceryList(newList);

        //add to db and set model id to db id
        long id = db.insertNewGroceryList(model.getGroceryList(0));
        model.getGroceryList(0).setID(id);

        // update other grocery lists in model
        for (GroceryList gl : model.getAllGroceryLists()) {
            db.updateGroceryList(gl);
        }

        //open grocery list activity
        Intent intent = new Intent(this, GroceryListActivity.class);
        intent.putExtra(ToolBox.LIST_EXTRA, model.getGroceryList(0));

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("TEST", "onResume");

        //mGLAdapter.notifyItemChanged(0);
        mGLAdapter.notifyDataSetChanged();
    }
}