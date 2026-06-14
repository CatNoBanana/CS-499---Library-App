/*
* MediaItem
* Data model representing a media entry in the library
 */

package com.zybooks.judsonsinventory;

public class MediaItem {

    //variables needed
    private int mId;
    private String mTitle;
    private String mCategory;
    private int mYear;
    private float mRating;
    private String mNotes;

    public MediaItem() {}

    public MediaItem(int id, String title, String category, int year, float rating, String notes) {
        mId = id;
        mTitle = title;
        mCategory = category;
        mYear = year;
        mRating = rating;
        mNotes = notes;
    }

    //ID
    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }


    //Title
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }


    //Category
    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        mCategory = category;
    }


    //Year
    public int getYear() {
        return mYear;
    }

    public void setYear(int year) {
        mYear = year;
    }


    //Rating
    public float getRating() {
        return mRating;
    }

    public void setRating(float rating) {
        mRating = rating;
    }


    //Notes
    public String getNotes() {
        return mNotes;
    }

    public void setNotes(String notes) {
        mNotes = notes;
    }





}
