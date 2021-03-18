package com.comp1601.groceriesplus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class GroceryList implements Serializable {

    private long ID;
    private int position; //position in

    private String name;
    private ListType listType;
    private boolean active;

    private Date createdDate;
    private Date dueDate;
    private Date activeDate; //date this list was made inactive

    private List<GroceryItem> groceryItems;

    GroceryList(ListType type) {
        this(0, "", 1, type.getIntValue(), "", "", "", 0);
        createdDate = new Date();
    }

    public GroceryList(long id, String name, int active, int type, String createdDate, String dueDate, String activeDate, int position) {
        this.ID = id;
        this.name = name;
        this.active = active == 1;
        this.listType = ListType.getType(type);

        this.position = position;

        this.createdDate = ToolBox.dbStringToDate(createdDate);
        this.dueDate = ToolBox.dbStringToDate(dueDate);
        this.activeDate = ToolBox.dbStringToDate(activeDate);

        this.groceryItems = new ArrayList<>();
    }

    public void addItem(GroceryItem groceryItem) {
        int newPos = 0;
        for (int i = 0; i < groceryItems.size(); i++) {
            groceryItems.get(i).setPosition(i);
            newPos = i;
        }
        groceryItem.setPosition(newPos+1);
        groceryItems.add(groceryItem);
    }

    public GroceryItem getItem(int index) {
        return groceryItems.get(index);
    }

    public void removeItem(int index) {
        groceryItems.remove(index);
    }

    public int getTotalItemCount() {
        return groceryItems.size();
    }

    public int getFoundItemCount(boolean found) {
        int count = 0;
        for (GroceryItem groceryItem : groceryItems) {
            count += (groceryItem.isFound() == found) ? 1 : 0;
        }

        return count;
    }

    public void setItemFound(int index, boolean found) {
        groceryItems.get(index).setFound(found);
        groceryItems.get(index).setFoundDate(new Date());
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ListType getListType() {
        return listType;
    }

    public void setListType(ListType listType) {
        this.listType = listType;
    }

    // 1: True, 0: False
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (this.active != active) {
            this.activeDate = active ? null : new Date();
        }
        this.active = active;
    }

    public List<GroceryItem> getGroceryItems() {
        return groceryItems;
    }

    public void setGroceryItems(List<GroceryItem> groceryItems) {
        this.groceryItems = groceryItems;
        this.groceryItems.sort(Comparator.comparing(GroceryItem::getPosition));
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public Date getActiveDate() {
        return activeDate;
    }

    public void setActiveDate(Date activeDate) {
        this.activeDate = activeDate;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        String out = "|\n\nList: " + name;
        out += " ID: " + ID +
                ", type: " + listType +
                ", active: " + active +
                ", cD: " + ToolBox.dateToUiString(createdDate) +
                ", dD: " + ToolBox.dateToUiString(dueDate);

        for (int i = 0; i < groceryItems.size(); i++) {
            out += "\n   " + i + ") " + groceryItems.get(i);
        }

        out += "\n\n|";
        return out;
    }
}
