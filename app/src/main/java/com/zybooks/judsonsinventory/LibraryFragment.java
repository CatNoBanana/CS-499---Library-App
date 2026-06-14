/*
* LibraryFragment
* Displays the user's complete media collection and summary counts
* Loads books, movies, and games from the database
 */

package com.zybooks.judsonsinventory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {

    private int mUserId;
    private DatabaseManager mDb;
    private MediaItemAdapter mAdapter;
    private List<MediaItem> mItems = new ArrayList<>();
    private View mRootView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the logged-in user's ID from the parent activity
        if (getActivity() instanceof LibraryMain) {
            LibraryMain activity = (LibraryMain) getActivity();

            mUserId = activity.getUserId();
        }

        //initialize database manager
        mDb = new DatabaseManager(requireContext());
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_library, container, false);
        return mRootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.library_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        mAdapter = new MediaItemAdapter(mItems, new MediaItemAdapter.OnItemActionListener() {
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

        //update the counts and load all items
        //loadAllItems(view);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (mRootView != null) {
            loadAllItems(mRootView);
        }
    }


    //called when library tab is selected
    public void refreshData() {
        if (mRootView != null) {
            loadAllItems(mRootView);
        }
    }


    //called when the fragment resumes so counts stay fresh
    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            loadAllItems(getView());
        }
    }


    private void loadAllItems(View view) {

        android.util.Log.d("LIBRARY_DEBUG",
                "Loading items for userId = " + mUserId);

        //load each category separately so we can show counts
        List<MediaItem> books  = mDb.getItemsByCategory(mUserId, DatabaseManager.CAT_BOOKS);
        List<MediaItem> movies = mDb.getItemsByCategory(mUserId, DatabaseManager.CAT_MOVIES);
        List<MediaItem> games  = mDb.getItemsByCategory(mUserId, DatabaseManager.CAT_GAMES);

        //update the count badges
        TextView booksCount  = view.findViewById(R.id.count_books);
        TextView moviesCount = view.findViewById(R.id.count_movies);
        TextView gamesCount  = view.findViewById(R.id.count_games);
        TextView totalCount  = view.findViewById(R.id.count_total);

        booksCount.setText(String.valueOf(books.size()));
        moviesCount.setText(String.valueOf(movies.size()));
        gamesCount.setText(String.valueOf(games.size()));

        int total = books.size() + movies.size() + games.size();
        totalCount.setText(total + " items in your library");

        //combine all into one list for the recycler
        mItems = new ArrayList<>();
        mItems.addAll(books);
        mItems.addAll(movies);
        mItems.addAll(games);

        if (mAdapter != null) {
            mAdapter.updateItems(mItems);
        }
    }


    private void showDeleteDialog(MediaItem item) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Item")
                .setMessage("Delete \"" + item.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    mDb.deleteMediaItem(item.getId());
                    if (getView() != null) loadAllItems(getView());
                    Toast.makeText(
                            getContext().getApplicationContext(),
                            "Deleted",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void showEditDialog(MediaItem item) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_item, null);

        EditText titleEdit  = dialogView.findViewById(R.id.dialog_title);
        EditText yearEdit   = dialogView.findViewById(R.id.dialog_year);
        EditText ratingEdit = dialogView.findViewById(R.id.dialog_rating);
        EditText notesEdit  = dialogView.findViewById(R.id.dialog_notes);

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
                        String y = yearEdit.getText().toString().trim();
                        if (!y.isEmpty()) year = Integer.parseInt(y);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(),
                                "Invalid year", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        String r = ratingEdit.getText().toString().trim();
                        if (!r.isEmpty()) rating = Float.parseFloat(r);
                        if (rating < 0 || rating > 5) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(),
                                "Rating must be 0-5", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String notes = notesEdit.getText().toString().trim();
                    boolean updated = mDb.updateMediaItem(
                            item.getId(), title, year, rating, notes);

                    if (updated) {
                        if (getView() != null) loadAllItems(getView());
                    } else {
                        Toast.makeText(requireContext(),
                                "Failed to update item", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}