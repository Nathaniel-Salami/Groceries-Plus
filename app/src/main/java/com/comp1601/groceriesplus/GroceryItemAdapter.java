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
import android.widget.Toast;

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
        private EditText mExpiryDateEditText;
        private EditText mRestockDateEditText;

        private View parentView;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.parentView = view;

            this.mFoundCheckBox = view.findViewById(R.id.foundCheckBox);
            this.mItemNameEditText = view.findViewById(R.id.itemNameEditText);
            this.mDeleteItemButton = view.findViewById(R.id.deleteListButton);
            this.mExpiryDateEditText = view.findViewById(R.id.dueDateTextView);
            this.mRestockDateEditText = view.findViewById(R.id.restockDateEditText);
            this.mQuantityEditText = view.findViewById(R.id.itemQuantityEditText);
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

        //basic setters
        holder.mFoundCheckBox.setChecked(groceryItem.isFound());
        holder.mItemNameEditText.setText(groceryItem.getName());

        String quantityText = "";
        quantityText += groceryItem.getQuantity() == 0 ? "" : groceryItem.getQuantity();
        holder.mQuantityEditText.setText(quantityText);


        holder.mExpiryDateEditText.setText(ToolBox.dateToUiString(groceryItem.getExpiryDate()));
        holder.mRestockDateEditText.setText(ToolBox.dateToUiString(groceryItem.getRestockDate()));


        //handlers
        holder.mExpiryDateEditText.setOnFocusChangeListener((view, b) -> {
            handleDatePicker(mContext, holder.mExpiryDateEditText, gList.getItem(holder.getAdapterPosition()), true);
        });
        holder.mExpiryDateEditText.addTextChangedListener(
            makeTextWatcher(gList.getItem(holder.getAdapterPosition()), holder.mExpiryDateEditText, AdapterInputType.EXPIRY)
        );

        holder.mRestockDateEditText.setOnFocusChangeListener((view, b) -> {
            handleDatePicker(mContext, holder.mRestockDateEditText, gList.getItem(holder.getAdapterPosition()), false);
        });
        holder.mRestockDateEditText.addTextChangedListener(
            makeTextWatcher(gList.getItem(holder.getAdapterPosition()), holder.mRestockDateEditText, AdapterInputType.RESTOCK)
        );

        holder.mItemNameEditText.addTextChangedListener(
            makeTextWatcher(gList.getItem(holder.getAdapterPosition()), holder.mItemNameEditText, AdapterInputType.NAME)
        );

        holder.mQuantityEditText.addTextChangedListener(
            makeTextWatcher(gList.getItem(holder.getAdapterPosition()), holder.mQuantityEditText, AdapterInputType.QUANTITY)
        );

        holder.mDeleteItemButton.setOnClickListener(view -> {
            //Toast.makeText(mContext,"Delete Item",Toast.LENGTH_SHORT).show();

            db.deleteGroceyItem(groceryItem.getID(), gList.getID());
            gList.removeItem(position);

            //((GroceryListActivity) mContext).gList.removeItem(holder.getAdapterPosition());

            this.notifyItemRemoved(holder.getAdapterPosition());
            this.notifyDataSetChanged();
        });

        holder.mFoundCheckBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            gList.getItem(holder.getAdapterPosition()).setFound(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return this.gList.getTotalItemCount();
    }

    public void handleDatePicker(Context context, EditText dateEditText, GroceryItem groceryItem, boolean isExpiry) {
        final Calendar myCalendar = Calendar.getInstance();

        if (groceryItem.getExpiryDate() != null && isExpiry) {
            myCalendar.setTime(groceryItem.getExpiryDate());
        }
        else if (groceryItem.getRestockDate() != null && !isExpiry) {
            myCalendar.setTime(groceryItem.getRestockDate());
        }

        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            //format date
            String date1 = ToolBox.dateToUiString(myCalendar.getTime());
            dateEditText.setText(date1);

            if (isExpiry) {
                groceryItem.setExpiryDate(myCalendar.getTime());
            }
            else {
                groceryItem.setRestockDate(myCalendar.getTime());
            }

        };

        dateEditText.setOnClickListener(view -> {
            new DatePickerDialog(context, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    public TextWatcher makeTextWatcher(GroceryItem groceryItem, EditText editText, AdapterInputType inputType) {
        return new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = editText.getText().toString();
                // set oid value now
                if (inputType == AdapterInputType.EXPIRY) {
                    groceryItem.setExpiryDate(ToolBox.uiStringToDate(text));
                }
                else if (inputType == AdapterInputType.RESTOCK) {
                    groceryItem.setRestockDate(ToolBox.uiStringToDate(text));
                }
                else if (inputType == AdapterInputType.NAME) {
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
