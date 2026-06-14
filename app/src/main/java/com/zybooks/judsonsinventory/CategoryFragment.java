/*
* CategoryFragment
* Displays a single category (books, movies, or games)
* Supports adding, editing, searching, sorting, and deleting media items
*/

package com.zybooks.judsonsinventory;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryFragment extends Fragment {

    //key used to pass the category name when creating this fragment
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_USER_ID  = "user_id";
    private String mCategory;
    private int mUserId;
    private DatabaseManager mDb;
    private MediaItemAdapter mAdapter;


    //unfiltered list loaded from the database
    private List<MediaItem> mAllItems = new ArrayList<>();


    //currently displayed list
    private List<MediaItem> mDisplayedItems = new ArrayList<>();


    //hashMap index
    private Map<String, List<MediaItem>> mCategoryIndex = new HashMap<>();


    //current sort state
    private int mSortField = SortSearchManager.SORT_TITLE;
    private int mSortOrder = SortSearchManager.ORDER_ASC;


    //current search query
    private String mCurrentQuery = "";
    private SearchView mSearchView;


    //sort label strings for the UI
    private static final String[] SORT_LABELS = {
            "Sorted by: Title A\u2192Z",
            "Sorted by: Title Z\u2192A",
            "Sorted by: Year (Newest)",
            "Sorted by: Year (Oldest)",
            "Sorted by: Rating (High\u2192Low)",
            "Sorted by: Rating (Low\u2192High)"
    };


    //factory method to always create fragments this way
    /* no proper implemntation at the moment
    public static CategoryFragment newInstance(String category, int userId) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        args.putInt(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }
    */


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCategory = getArguments().getString(ARG_CATEGORY);
            mUserId   = getArguments().getInt(ARG_USER_ID, -1);
        }


        if (mUserId == -1 && getActivity() instanceof LibraryMain) {
            mUserId = ((LibraryMain) getActivity()).getUserId();
        }

        mDb = new DatabaseManager(requireContext());
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.category_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        mAdapter = new MediaItemAdapter(mDisplayedItems, new MediaItemAdapter.OnItemActionListener() {
            @Override
            public void onEditClick(MediaItem item) {
                showEditDialog(item);
            }

            @Override
            public void onDeleteClick(MediaItem item) {
                showDeleteDialog(item);
            }
        });

        recyclerView.setAdapter(mAdapter);

        //wire sort button
        ImageButton sortButton = view.findViewById(R.id.btn_sort);
        sortButton.setOnClickListener(v -> showSortMenu(v));

        //wire search view
        mSearchView = view.findViewById(R.id.search_view);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mCurrentQuery = query;
                applySort();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mCurrentQuery = newText;
                applySort();
                return true;
            }
        });

        loadItems();
    }


    //load items from the database for this category
    public void loadItems() {
        mAllItems = mDb.getItemsByCategory(mUserId, mCategory);

        mCategoryIndex = SortSearchManager.buildCategoryIndex(mAllItems);

        applySort();
        updateEmptyView();
    }


    //sort and search
    private void applySort() {
        //get base list from index
        List<MediaItem> base = SortSearchManager.getFromIndex(mCategoryIndex, mCategory);

        //if index is empty, fall back to mAllItems
        if (base.isEmpty() && !mAllItems.isEmpty()) {
            base = new ArrayList<>(mAllItems);
        }

        //merge sort by current field and direction
        List<MediaItem> sorted = SortSearchManager.mergeSort(base, mSortField, mSortOrder);

        //apply search filter if query is active
        if (!mCurrentQuery.trim().isEmpty()) {
            sorted = SortSearchManager.search(sorted, mCurrentQuery, mSortField);
        }

        //push to adapter
        mDisplayedItems = sorted;
        if (mAdapter != null) {
            mAdapter.updateItems(mDisplayedItems);
        }

        updateSortLabel();
        updateEmptyView();
    }


    //sort popup menu
    private void showSortMenu(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);

        popup.getMenu().add(0, 0, 0, "Title A\u2192Z");
        popup.getMenu().add(0, 1, 1, "Title Z\u2192A");
        popup.getMenu().add(0, 2, 2, "Year (Newest first)");
        popup.getMenu().add(0, 3, 3, "Year (Oldest first)");
        popup.getMenu().add(0, 4, 4, "Rating (High\u2192Low)");
        popup.getMenu().add(0, 5, 5, "Rating (Low\u2192High)");

        popup.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case 0: mSortField = SortSearchManager.SORT_TITLE; mSortOrder = SortSearchManager.ORDER_ASC; break;
                case 1: mSortField = SortSearchManager.SORT_TITLE; mSortOrder = SortSearchManager.ORDER_DESC; break;
                case 2: mSortField = SortSearchManager.SORT_YEAR; mSortOrder = SortSearchManager.ORDER_DESC; break;
                case 3: mSortField = SortSearchManager.SORT_YEAR; mSortOrder = SortSearchManager.ORDER_ASC; break;
                case 4: mSortField = SortSearchManager.SORT_RATING; mSortOrder = SortSearchManager.ORDER_DESC; break;
                case 5: mSortField = SortSearchManager.SORT_RATING; mSortOrder = SortSearchManager.ORDER_ASC; break;
            }

            applySort();
            return true;
        });

        popup.show();
    }


    private void updateSortLabel() {
        if (getView() == null) {
            return;
        }
        TextView label = getView().findViewById(R.id.sort_label);
        if (label == null) {
            return;
        }

        int labelIndex;
        if (mSortField == SortSearchManager.SORT_TITLE) {
            labelIndex = mSortOrder == SortSearchManager.ORDER_ASC ? 0 : 1;
        } else if (mSortField == SortSearchManager.SORT_YEAR) {
            labelIndex = mSortOrder == SortSearchManager.ORDER_DESC ? 2 : 3;
        } else {
            labelIndex = mSortOrder == SortSearchManager.ORDER_DESC ? 4 : 5;
        }

        label.setText(SORT_LABELS[labelIndex]);
    }


    private void updateEmptyView() {
        if (getView() == null) {
            return;
        }

        TextView emptyText = getView().findViewById(R.id.empty_text);
        RecyclerView recycler = getView().findViewById(R.id.category_recycler);

        if (emptyText == null || recycler == null) {
            return;
        }

        if (mDisplayedItems.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recycler.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recycler.setVisibility(View.VISIBLE);
        }
    }


    //called by LibraryMain when the FAB is tapped on this tab
    public void showAddDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_item, null);

        EditText titleEdit  = dialogView.findViewById(R.id.dialog_title);
        EditText yearEdit   = dialogView.findViewById(R.id.dialog_year);
        EditText ratingEdit = dialogView.findViewById(R.id.dialog_rating);
        EditText notesEdit  = dialogView.findViewById(R.id.dialog_notes);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add to " + mCategory)
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = titleEdit.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Title is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int year = 0;
                    float rating = 0f;

                    try {
                        String yearStr = yearEdit.getText().toString().trim();
                        if (!yearStr.isEmpty()) year = Integer.parseInt(yearStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(),
                                "Invalid year", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        String ratingStr = ratingEdit.getText().toString().trim();
                        if (!ratingStr.isEmpty()) rating = Float.parseFloat(ratingStr);
                        if (rating < 0 || rating > 5) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        Log.d("RATING_DEBUG", "Invalid rating entered");
                        return;
                    }

                    String notes = notesEdit.getText().toString().trim();

                    boolean added = mDb.addMediaItem(
                            mUserId, title, mCategory, year, rating, notes);

                    if (added) {
                        loadItems();
                    } else {
                        Toast.makeText(requireContext(),
                                "Failed to add item", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("Cancel", null).show();
    }


    //edit dialog pre-filled with the item's current values
    private void showEditDialog(MediaItem item) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_item, null);

        EditText titleEdit  = dialogView.findViewById(R.id.dialog_title);
        EditText yearEdit   = dialogView.findViewById(R.id.dialog_year);
        EditText ratingEdit = dialogView.findViewById(R.id.dialog_rating);
        EditText notesEdit  = dialogView.findViewById(R.id.dialog_notes);

        //pre-fill existing values
        titleEdit.setText(item.getTitle());
        yearEdit.setText(item.getYear() > 0 ? String.valueOf(item.getYear()) : "");
        ratingEdit.setText(item.getRating() > 0 ? String.valueOf(item.getRating()) : "");
        notesEdit.setText(item.getNotes() != null ? item.getNotes() : "");

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Item")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = titleEdit.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Title is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int year = 0;
                    float rating = 0f;

                    try {
                        String yearStr = yearEdit.getText().toString().trim();
                        if (!yearStr.isEmpty()) year = Integer.parseInt(yearStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(),
                                "Invalid year", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        String ratingStr = ratingEdit.getText().toString().trim();
                        if (!ratingStr.isEmpty()) rating = Float.parseFloat(ratingStr);
                        if (rating < 0 || rating > 5) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        Log.d("RATING_DEBUG", "Invalid rating entered");
                        return;
                    }

                    String notes = notesEdit.getText().toString().trim();

                    boolean updated = mDb.updateMediaItem(
                            item.getId(), title, year, rating, notes);

                    if (updated) {
                        loadItems();
                    } else {
                        Toast.makeText(requireContext(),
                                "Failed to update item", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    //confirm before deleting
    private void showDeleteDialog(MediaItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Item")
                .setMessage("Delete \"" + item.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    mDb.deleteMediaItem(item.getId());
                    loadItems();
                    Toast.makeText(
                            getContext().getApplicationContext(),
                            "Deleted",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}