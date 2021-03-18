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

        //grab model from db
        model.setGListArrayList(db.selectAllGroceryList());

        //launch recycler view
        loadRecyclerView();

        // handlers
        mAddListButton.setOnClickListener(view -> {
            handleAddList();
        });
    }

    public void loadRecyclerView() {
        mGLAdapter = new GroceryListAdapter(this, model);
        mRecyclerView.setAdapter(mGLAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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
        intent.putExtra(ToolBox.GLIST_EXTRA, model.getGroceryList(0));
        intent.putExtra(ToolBox.MODEL_EXTRA, model);
        startActivity(intent);
    }

    public void handleRemoveGList(int position) {
        db.deleteGroceryList(model.getGroceryList(position));
        model.removeGroceryList(position);

        //regular notify
        mGLAdapter.notifyDataSetChanged();

        //reload recycler view
        loadRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("TEST", "onResume");

        //grab model and launch recycler view
        model.setGListArrayList(db.selectAllGroceryList());
        loadRecyclerView();

        mGLAdapter.notifyDataSetChanged();
    }
}