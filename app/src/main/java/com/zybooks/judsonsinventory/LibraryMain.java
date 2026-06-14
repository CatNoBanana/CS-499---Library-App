/*
* LibraryMain
* Main activity displayed after login
* Hosts the navigation graph, bottom navigation bar, and floating action button
 */

package com.zybooks.judsonsinventory;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class LibraryMain extends AppCompatActivity {

    private int mUserId;
    private NavHostFragment mNavHostFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get userId passed from the login screen
        mUserId = getIntent().getIntExtra("USER_ID", -1);


        setContentView(R.layout.activity_library_main);

        if (mUserId == -1) {
            Toast.makeText(this, "Error: No user logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //get the nav controller from the host fragment
        mNavHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = mNavHostFragment.getNavController();

        //wire the bottom nav to the nav controller - handles tab switching automatically
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        //wire FAB to only show on category tabs, not library or profile
        FloatingActionButton fab = findViewById(R.id.fab_add);


        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();

            if (id == R.id.nav_books || id == R.id.nav_movies || id == R.id.nav_games) {
                fab.show();
            }
            else {
                fab.hide();
            }

            //reload library fragment when it's selected
            if (id == R.id.nav_library) {
                Fragment current = mNavHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
                if (current instanceof LibraryFragment) {
                    ((LibraryFragment) current).refreshData();
                }
            }
        });


        fab.setOnClickListener(v -> {
            Fragment current = mNavHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();

            if (current instanceof CategoryFragment) {
                ((CategoryFragment) current).showAddDialog();
            }
        });
    }


    //any fragment can call this to get the logged-in user's ID
    public int getUserId() {
        return mUserId;
    }
}