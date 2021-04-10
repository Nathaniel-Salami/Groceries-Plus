package com.comp1601.groceriesplus;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GroceryItemAdapter extends RecyclerView.Adapter<GroceryItemAdapter.ViewHolder> {

    public enum AdapterInputType {
        NAME,
        EXPIRY,
        RESTOCK,
        QUANTITY;
    }


    private Context mContext;
    private GroceryList gList;
    //private List<Item> itemList = new ArrayList<>();
    private GroceryPlusDbHelper db;

    class ViewHolder extends RecyclerView.ViewHolder {
        private CheckBox mFoundCheckBox;
        private EditText mItemNameEditText;
        private ImageButton mDeleteItemButton;
        private EditText mQuantityEditText;

        private ImageButton mExpiryDateButton;
        private ImageButton mRestockDateButton;

        private LinearLayout mExpireRow;
        private LinearLayout mRestockRow;

        private EditText mExpiryDateEditText;
        private EditText mRestockDateEditText;

        private View parentView;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.parentView = view;

            this.mFoundCheckBox = view.findViewById(R.id.foundCheckBox);
            this.mItemNameEditText = view.findViewById(R.id.itemNameEditText);
            this.mDeleteItemButton = view.findViewById(R.id.deleteListButton);
            this.mQuantityEditText = view.findViewById(R.id.itemQuantityEditText);

            this.mExpiryDateButton = view.findViewById(R.id.dueDateText_List);
            this.mRestockDateButton = view.findViewById(R.id.restockDateButton);

            this.mExpireRow = view.findViewById(R.id.expireRow);
            this.mRestockRow = view.findViewById(R.id.restockRow);

            this.mExpiryDateEditText = view.findViewById(R.id.expiryDateEditText);
            this.mRestockDateEditText = view.findViewById(R.id.restockDateEditText);
        }
    }

    public GroceryList getGroceryList() {
        return gList;
    }

    public GroceryItemAdapter(@NonNull Context context, @NonNull GroceryList object) {
        mContext = context;
        gList = object;
        db = new GroceryPlusDbHelper(mContext);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
            LayoutInflater.from(mContext).inflate(R.layout.adapter_grocery_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final GroceryItem groceryItem = gList.getItem(holder.getAdapterPosition());

        //stops recyling
        holder.setIsRecyclable(false);

        // setters
        holder.mFoundCheckBox.setChecked(groceryItem.isFound());
        holder.mItemNameEditText.setText(groceryItem.getName());

        String quantityText = groceryItem.getQuantity() == 0 ? "" : "" + groceryItem.getQuantity();
        holder.mQuantityEditText.setText(quantityText);

        String expDate = groceryItem.getExpiryDate() != null ?
                ToolBox.dateToUiString(groceryItem.getExpiryDate()) : "";
        holder.mExpiryDateEditText.setText(expDate);

        String resDate = groceryItem.getRestockDate() != null ?
                ToolBox.dateToUiString(groceryItem.getRestockDate()) : "";
        holder.mRestockDateEditText.setText(resDate);


        //handlers
        holder.mItemNameEditText.addTextChangedListener(
            makeTextWatcher(gList.getItem(holder.getAdapterPosition()), holder.mItemNameEditText, AdapterInputType.NAME)
        );

        holder.mQuantityEditText.addTextChangedListener(
            makeTextWatcher(gList.getItem(holder.getAdapterPosition()), holder.mQuantityEditText, AdapterInputType.QUANTITY)
        );

        holder.mExpiryDateEditText.addTextChangedListener(
            makeTextWatcher(gList.getItem(holder.getAdapterPosition()), holder.mExpiryDateEditText, AdapterInputType.EXPIRY)
        );

        holder.mRestockDateEditText.addTextChangedListener(
            makeTextWatcher(gList.getItem(holder.getAdapterPosition()), holder.mRestockDateEditText, AdapterInputType.RESTOCK)
        );

        holder.mDeleteItemButton.setOnClickListener(view -> {
            ((GroceryListActivity) mContext).handleRemoveGItem(holder.getAdapterPosition());
        });

        holder.mFoundCheckBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            gList.getItem(holder.getAdapterPosition()).setFound(isChecked);
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return this.gList.getTotalItemCount();
    }

    public TextWatcher makeTextWatcher(GroceryItem groceryItem, EditText editText, AdapterInputType inputType) {
        return new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = editText.getText().toString();
                // set oid value now
                if (inputType == AdapterInputType.NAME) {
                    groceryItem.setName(text);
                }
                else if (inputType == AdapterInputType.QUANTITY) {
                    if (!text.equals("")) {
                        groceryItem.setQuantity(Integer.parseInt(text));
                    }
                }
                else if (inputType == AdapterInputType.EXPIRY) {
                    if (!text.equals("")) {
                        groceryItem.setExpiryDate(ToolBox.uiStringToDate(text));
                    }
                    else {
                        groceryItem.setExpiryDate(null);
                    }
                }
                else if (inputType == AdapterInputType.RESTOCK) {
                    if (!text.equals("")) {
                        groceryItem.setRestockDate(ToolBox.uiStringToDate(text));
                    }
                    else {
                        groceryItem.setRestockDate(null);
                    }
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            public void afterTextChanged(Editable s) {

            }
        };
    }
}
