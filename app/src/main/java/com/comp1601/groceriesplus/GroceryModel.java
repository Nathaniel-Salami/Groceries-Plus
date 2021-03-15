package com.comp1601.groceriesplus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class GroceryModel {
    private List<GroceryList> gListArrayList;

    public GroceryModel() {
        gListArrayList = new ArrayList<>();
    }

    public void addGroceryList(GroceryList newList) {
        gListArrayList.add(0, newList); //adding to top causes ui issues

        for (int i = 1; i < gListArrayList.size(); i++) {
            //int newPos = gListArrayList.get(i).getPosition() + 1;
            gListArrayList.get(i).setPosition(i);
        }
        //gListArrayList.add(newList);
    }

    public GroceryList getGroceryList(int position) {
        return gListArrayList.get(position);
    }

    public void removeGroceryList(int position) {
        gListArrayList.remove(position);
    }

    public List<GroceryList> getAllGroceryLists() {
        return gListArrayList;
    }

    public List<GroceryList> getActiveGroceryLists(boolean active) {
        List<GroceryList> groceryLists = new ArrayList<>();

        for (GroceryList gl: gListArrayList) {
            if (gl.isActive() == active) {
                groceryLists.add(gl);
            }
        }

        return gListArrayList;
    }

    public void setGListArrayList(List<GroceryList> gListArrayList) {
        this.gListArrayList = gListArrayList;

        this.gListArrayList.sort(Comparator.comparing(GroceryList::getPosition));
    }

    private GroceryList generateGList() {
        GroceryList newGList = new GroceryList(ListType.GENERATED);

        //loop through all inactive lists
        for (GroceryList gl : getActiveGroceryLists(false)) {
            //if the list was made inactive less than GLIST_LIMIT ago

            //loop through their items
            for (GroceryItem gi : gl.getGroceryItems()) {
                //add expired/restock due items to newList

                //add items that will expire in GITEM_LIMIT to new list
                if (gi.needRestock() || gi.isExpired()) {
                    newGList.addItem(gi);
                }
            }
        }

        return newGList;
    }
}
