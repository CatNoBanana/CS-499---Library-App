/*
* ProfileFragment
* Displays account information and profile-related actions
 */

package com.zybooks.judsonsinventory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.List;

public class ProfileFragment extends Fragment {

    private int mUserId;
    private DatabaseManager mDb;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() instanceof LibraryMain) {
            mUserId = ((LibraryMain) getActivity()).getUserId();
        }

        mDb = new DatabaseManager(requireContext());
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //show total item counts on the profile screen
        List<MediaItem> books  = mDb.getItemsByCategory(mUserId, DatabaseManager.CAT_BOOKS);
        List<MediaItem> movies = mDb.getItemsByCategory(mUserId, DatabaseManager.CAT_MOVIES);
        List<MediaItem> games  = mDb.getItemsByCategory(mUserId, DatabaseManager.CAT_GAMES);

        TextView booksCount  = view.findViewById(R.id.profile_books_count);
        TextView moviesCount = view.findViewById(R.id.profile_movies_count);
        TextView gamesCount  = view.findViewById(R.id.profile_games_count);

        booksCount.setText(books.size() + " Books");
        moviesCount.setText(movies.size() + " Movies");
        gamesCount.setText(games.size() + " Games");

        //lets the user update their saved alert number
        view.findViewById(R.id.btn_update_phone).setOnClickListener(v -> showPhoneDialog());

        //show the currently saved number if there is one
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("app_prefs", requireActivity().MODE_PRIVATE);
        String savedNumber = prefs.getString("phone_number", null);
        TextView phoneText = view.findViewById(R.id.profile_phone_number);
        phoneText.setText(savedNumber != null ? savedNumber : "Not set");

        //logout button - goes back to login screen
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> logout());
    }


    //refresh counts when returning to this fragment
    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            List<MediaItem> books  = mDb.getItemsByCategory(mUserId, DatabaseManager.CAT_BOOKS);
            List<MediaItem> movies = mDb.getItemsByCategory(mUserId, DatabaseManager.CAT_MOVIES);
            List<MediaItem> games  = mDb.getItemsByCategory(mUserId, DatabaseManager.CAT_GAMES);

            getView().findViewById(R.id.profile_books_count);
            ((TextView) getView().findViewById(R.id.profile_books_count))
                    .setText(books.size() + " Books");
            ((TextView) getView().findViewById(R.id.profile_movies_count))
                    .setText(movies.size() + " Movies");
            ((TextView) getView().findViewById(R.id.profile_games_count))
                    .setText(games.size() + " Games");
        }
    }


    private void showPhoneDialog() {
        EditText input = new EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        input.setHint("Enter phone number");

        //pre-fill with the saved number if there is one
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("app_prefs", requireActivity().MODE_PRIVATE);
        String current = prefs.getString("phone_number", "");
        input.setText(current);

        new AlertDialog.Builder(requireContext())
                .setTitle("Alert Phone Number")
                .setMessage("This number will receive low stock alerts.")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String number = input.getText().toString().trim();
                    if (!number.isEmpty()) {
                        prefs.edit().putString("phone_number", number).apply();

                        //refresh the displayed number
                        TextView phoneText = requireView().findViewById(R.id.profile_phone_number);
                        phoneText.setText(number);

                        android.widget.Toast.makeText(requireContext(),
                                "Number saved", android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    //go back to the login screen and clear the back stack
                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}