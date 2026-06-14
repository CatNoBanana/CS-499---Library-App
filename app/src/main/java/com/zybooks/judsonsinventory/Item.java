/*
* Item
* Simple data model containing the ID, name, and quantity
* (a few aspects of this class are outdated and need to be fixed)
 */

package com.zybooks.judsonsinventory;

public class Item {

    //variables needed
    private int mId;
    private String mItemName;
    private int mAmount;
    public Item() {}


    public Item(int id, String item, int amount) {
        mId = id;
        mItemName = item;
        mAmount = amount;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public String getItemName() {
        return mItemName;
    }

    public void setItemName(String item) {
        this.mItemName = item;
    }

    public int getAmount() {
        return mAmount;
    }

    public void setAmount(int amount) {
        this.mAmount = amount;
    }
}
