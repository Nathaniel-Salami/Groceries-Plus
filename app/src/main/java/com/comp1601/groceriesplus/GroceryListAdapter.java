package com.comp1601.groceriesplus;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GroceryListAdapter extends RecyclerView.Adapter<GroceryListAdapter.ViewHolder> {

    private Context mContext;
    //private List<GroceryList> gListArrayList = new ArrayList<>();
    private GroceryPlusDbHelper db;
    private GroceryModel groceryModel;

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mListNameTextView;
        public TextView mDueDateTextView;
        private ImageButton mDeleteListButton;

        private View parentView;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.parentView = view;
            this.mListNameTextView = view.findViewById(R.id.listNameTextView);
            this.mDueDateTextView = view.findViewById(R.id.dueDateTextView);
            this.mDeleteListButton = view.findViewById(R.id.deleteListButton);
        }
    }

    public GroceryListAdapter(@NonNull Context context, @NonNull GroceryModel gModel) {
        mContext = context;
        groceryModel = gModel;

        db = new GroceryPlusDbHelper(mContext);
    }

    /*@Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.adapter_grocery_list, parent, false)
        );
    }*/

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_grocery_list, viewGroup, false);

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final GroceryList gList = groceryModel.getGroceryList(position);

        //basic setters
        if (gList.getDueDate() != null) {
            holder.mDueDateTextView.setText(ToolBox.dateToUiString(gList.getDueDate()));
        }

        if (!gList.getName().isEmpty()) {
            holder.mListNameTextView.setText(gList.getName());
        }

        // handlers
        holder.mDeleteListButton.setOnClickListener(view -> {
            db.deleteGroceryList(gList);
            groceryModel.removeGroceryList(position);

            this.notifyItemRemoved(position);
            this.notifyDataSetChanged();
        });

        // whole list item click event
        holder.parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go to item list activity

                Intent intent = new Intent(mContext, GroceryListActivity.class);

                intent.putExtra(ToolBox.LIST_EXTRA, gList);

                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.groceryModel.getAllGroceryLists().size();
    }
}
