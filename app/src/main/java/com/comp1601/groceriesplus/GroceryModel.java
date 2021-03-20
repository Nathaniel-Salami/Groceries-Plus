package com.comp1601.groceriesplus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class GroceryModel implements Serializable {
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

        return groceryLists;
    }

    public void setGListArrayList(List<GroceryList> gListArrayList) {
        this.gListArrayList = gListArrayList;

        this.gListArrayList.sort(Comparator.comparing(GroceryList::getPosition));
    }

    public void addGListArrayList(List<GroceryList> gListArrayList) {
        this.gListArrayList.addAll(gListArrayList);

        this.gListArrayList.sort(Comparator.comparing(GroceryList::getPosition));
    }

    public List<GroceryItem> generateGItems() {
        //GroceryList newGList = new GroceryList(ListType.GENERATED);
        List<GroceryItem> genrated = new ArrayList<>();
        List<GroceryList> inActiveGroceryLists = getActiveGroceryLists(false);

        if (inActiveGroceryLists.isEmpty()) return null;

        //loop through all inactive lists
        for (GroceryList gl : inActiveGroceryLists) {
            //if the list was made inactive less than GLIST_LIMIT ago

            //loop through their items
            for (GroceryItem gi : gl.getGroceryItems()) {
                //add expired/restock due items to newList
                if ((gi.needRestock() ||
                        gi.isExpired() ||
                        !gi.isFound()) &&
                        !gi.getName().isEmpty()) {
                    gi.reset();
                    genrated.add(gi);
                }
            }
        }

        return genrated;
    }

    public static GroceryModel makeDemo() {
        GroceryModel model = new GroceryModel();
        GroceryList gl0 = new GroceryList(0, "Loblaws1",0, 0,
                "01/01/2021 00:00:00",
                "01/02/2021 00:00:00",
                "02/02/2021 00:00:00",
                200);

        GroceryItem gi0 = new GroceryItem(0, "Celery", 1, 6,
                "01/02/2021 00:00:00",
                "10/02/2021 00:00:00",
                "",
                0);

        GroceryItem gi1 = new GroceryItem(1, "Carrots", 1, 8,
                "01/02/2021 00:00:00",
                "14/02/2021 00:00:00",
                "",
                1);

        GroceryItem gi2 = new GroceryItem(2, "Cake", 0, 1,
                "01/02/2021 00:00:00",
                "",
                "09/02/2021 00:00:00",
                2);

        GroceryItem gi3 = new GroceryItem(0, "Chicken", 1, 2,
                "01/02/2021 00:00:00",
                "20/02/2021 00:00:00",
                "",
                3);

        gl0.addItem(gi0);
        gl0.addItem(gi1);
        gl0.addItem(gi2);
        gl0.addItem(gi3);

        model.addGroceryList(gl0);

        return model;
    }
}
