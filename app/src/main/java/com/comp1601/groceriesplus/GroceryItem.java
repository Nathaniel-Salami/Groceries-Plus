package com.comp1601.groceriesplus;

import java.io.Serializable;
import java.util.Date;

public class GroceryItem implements Serializable,Comparable<GroceryItem> {

    private long ID;

    private String name;
    private int quantity;
    private int position;

    private boolean found;
    private Date foundDate;

    private Date expiryDate;
    private Date restockDate;

    public GroceryItem() {
        this(0, "", 0, 0, "", "", "", 0);
    }

    public GroceryItem(long id, String name, int found, int quantity, String foundDate, String expiryDate, String restockDate, int position) {
        this.ID = id;
        this.name = name;
        this.found = found == 1;
        this.quantity = quantity;
        this.foundDate = ToolBox.dbStringToDate(foundDate);
        this.expiryDate = ToolBox.dbStringToDate(expiryDate);
        this.restockDate = ToolBox.dbStringToDate(restockDate);
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean f) {
        this.found = f;
        this.foundDate = (found) ? new Date() : null;
    }

    public Date getFoundDate() {
        return foundDate;
    }

    public void setFoundDate(Date foundDate) {
        this.foundDate = foundDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Date getRestockDate() {
        return restockDate;
    }

    public void setRestockDate(Date restockDate) {
        this.restockDate = restockDate;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isExpired() {
        if (expiryDate != null) {
            return expiryDate.before(new Date());
        }
        return false;
    }

    public boolean needRestock() {
        if (restockDate != null) {
            return restockDate.before(new Date());
        }
        return false;
    }

    public void reset() {
        expiryDate = null;
        restockDate = null;
        foundDate = null;

        found = false;

    }

    @Override
    public int compareTo(GroceryItem groceryItem) {
        if (!this.found && groceryItem.found) {
            return -1;
        }
        else if (this.found && !groceryItem.found) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Item: " + name +
                ", ID: " + ID +
                ", q: " + quantity +
                ", eD: " + ToolBox.dateToUiString(expiryDate) +
                ", rD: " + ToolBox.dateToUiString(restockDate) +
                ", f: " + found +
                ", fD: " + ToolBox.dateToUiString(foundDate);
    }
}
