package com.comp1601.groceriesplus;

import android.app.DatePickerDialog;
import android.content.Context;
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

        private TextView mExpiryDateText;
        private TextView mRestockDateText;

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

            this.mExpiryDateText = view.findViewById(R.id.expiryDateText);
            this.mRestockDateText = view.findViewById(R.id.restockDateText);
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
        holder.mExpiryDateText.setText(expDate);

        String resDate = groceryItem.getRestockDate() != null ?
                ToolBox.dateToUiString(groceryItem.getRestockDate()) : "";
        holder.mRestockDateText.setText(resDate);

        //handlers
        holder.mExpireRow.setOnClickListener(v -> {
            handleDatePicker(mContext, holder.mExpiryDateText, gList.getItem(holder.getAdapterPosition()), AdapterInputType.EXPIRY);
        });
        holder.mRestockRow.setOnClickListener(v -> {
            handleDatePicker(mContext, holder.mRestockDateText, gList.getItem(holder.getAdapterPosition()), AdapterInputType.RESTOCK);
        });

        holder.mItemNameEditText.addTextChangedListener(
            makeTextWatcher(gList.getItem(holder.getAdapterPosition()), holder.mItemNameEditText, AdapterInputType.NAME)
        );

        holder.mQuantityEditText.addTextChangedListener(
            makeTextWatcher(gList.getItem(holder.getAdapterPosition()), holder.mQuantityEditText, AdapterInputType.QUANTITY)
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

    public void handleDatePicker(Context context, TextView textView, GroceryItem groceryItem, AdapterInputType inputType) {
        final Calendar myCalendar = Calendar.getInstance();

        if (groceryItem.getExpiryDate() != null && inputType == AdapterInputType.EXPIRY) {
            myCalendar.setTime(groceryItem.getExpiryDate());
        }
        else if (groceryItem.getRestockDate() != null && inputType == AdapterInputType.RESTOCK) {
            myCalendar.setTime(groceryItem.getRestockDate());
        }

        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            //format date
            String pickedDate = ToolBox.dateToUiString(myCalendar.getTime());
            textView.setText(pickedDate);

            if (inputType == AdapterInputType.EXPIRY) {
                //pickedDate = mContext.getString(R.string.expireText) + " " + pickedDate;
                //textView.setText(pickedDate);
                groceryItem.setExpiryDate(myCalendar.getTime());
            }
            else if (inputType == AdapterInputType.RESTOCK) {
                //pickedDate = mContext.getString(R.string.restockText) + " " + pickedDate;
                //textView.setText(pickedDate);
                groceryItem.setRestockDate(myCalendar.getTime());
            }

        };

        new DatePickerDialog(context, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
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
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            public void afterTextChanged(Editable s) {

            }
        };
    }
}
