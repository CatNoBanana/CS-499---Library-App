/*
* MainActivity
* Handles user login and launches the main library screen
* Validates entered credentials against the database and passes the logged-in user ID
 */

package com.zybooks.judsonsinventory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    //variables needed
    private DatabaseManager db;
    private EditText usernameEditText;
    private EditText passwordEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set login menu as the first view
        setContentView(R.layout.login_menu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_menu), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //initialize database
        db = new DatabaseManager(this);

        //link editTexts
        usernameEditText = findViewById(R.id.login_username_edit_text);
        passwordEditText = findViewById(R.id.login_password_edit_text);

        //link buttons
        Button loginButton = findViewById(R.id.login_login_button);
        Button createAccountButton = findViewById(R.id.login_create_account_button);

        //login button control
        loginButton.setOnClickListener(v -> login());

        //create account button control
        createAccountButton.setOnClickListener(v -> createAccount());
    }


    //function used to login. takes username and password entered on the main login screen in the
    //editText boxes. lets user know if information is invalid
    private void login() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        //if blank username or password, a toast will request them
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter a username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        //check if username and password given are in the database
        int userId = db.loginUser(username, password);

        //if the userId is found, the view and code move to inventoryMain, passing along userId
        if (userId != -1) {
            Intent intent = new Intent(MainActivity.this, LibraryMain.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
            finish();
        }

        //if userId is not found, a toast will let user know it's invalid
        else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }


    //allows user to create an account with the given username and password
    private void createAccount() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        //if blank username or password, a toast will request them
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter a username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        //if username already exists, a toast will let the user know and request new one
        if (db.userExists(username)) {
            Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        //checks if username and password were created
        boolean created = db.createUser(username, password);

        //if so, the view and code move to inventoryMain, passing along new userId
        if (created) {
            int userId = db.loginUser(username, password);
            Intent intent = new Intent(MainActivity.this, LibraryMain.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
            finish();
        }

        //else it will provide a toast letting user know account didn't create successfully
        else {
            Toast.makeText(this, "Account creation failed", Toast.LENGTH_SHORT).show();
        }
    }
}