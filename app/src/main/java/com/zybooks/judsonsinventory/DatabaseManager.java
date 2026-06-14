/*
* DatabaseManager
* Responsible for creating, updating, and
* querying the application's database tables
 */

package com.zybooks.judsonsinventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;



public class DatabaseManager extends SQLiteOpenHelper {

    //variables needed
    //database info
    private static final String DATABASE_NAME = "judsonslibrary.db";
    private static final int DATABASE_VERSION = 4;


    //users table
    public static final String TABLE_USERS = "users";
    public static final String USER_ID = "user_id";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String PASSWORD_SALT = "password_salt";


    //categories table
    public static final String TABLE_CATEGORIES = "categories";
    public static final String CATEGORY_ID = "category_id";
    public static final String CATEGORY_NAME = "category_name";


    //media items table
    public static final String TABLE_MEDIA = "media_items";
    public static final String ITEM_ID = "item_id";
    public static final String ITEM_TITLE = "title";
    public static final String ITEM_CATEGORY_ID = "category_id";
    public static final String ITEM_YEAR = "year";
    public static final String ITEM_RATING = "rating";
    public static final String ITEM_NOTES = "notes";
    public static final String ITEM_USER_ID = "user_id";


    //default categories
    public static final String CAT_BOOKS = "Books";
    public static final String CAT_MOVIES = "Movies";
    public static final String CAT_GAMES = "Games";


    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        //users table
        String createUsersTable =
                "CREATE TABLE " + TABLE_USERS + " (" +
                USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USERNAME + " TEXT UNIQUE NOT NULL, " +
                PASSWORD + " TEXT NOT NULL, " +
                PASSWORD_SALT + " TEXT NOT NULL)";

        //categories tables
        String createCategoriesTable =
                "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CATEGORY_NAME + " TEXT UNIQUE NOT NULL)";

        //media items table
        String createMediaTable =
                "CREATE TABLE " + TABLE_MEDIA + " (" +
                        ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ITEM_TITLE + " TEXT NOT NULL, " +
                        ITEM_CATEGORY_ID + " INTEGER NOT NULL, " +
                        ITEM_YEAR + " INTEGER, " +
                        ITEM_RATING + " REAL, " +
                        ITEM_NOTES + " TEXT, " +
                        ITEM_USER_ID + " INTEGER NOT NULL, " +
                        "FOREIGN KEY(" + ITEM_CATEGORY_ID + ") REFERENCES " +
                        TABLE_CATEGORIES + "(" + CATEGORY_ID + "), " +
                        "FOREIGN KEY(" + ITEM_USER_ID + ") REFERENCES " +
                        TABLE_USERS + "(" + USER_ID + "))";


        //initialize the tables
        db.execSQL(createUsersTable);
        db.execSQL(createCategoriesTable);
        db.execSQL(createMediaTable);


        //create default categories so tabs have valid foreign keys
        createSeedCategories(db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDIA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }


    //insert the three categories on creation
    private void createSeedCategories(SQLiteDatabase db) {
        String[] defaults = {CAT_BOOKS, CAT_MOVIES, CAT_GAMES};
        for (String name : defaults) {
            ContentValues values = new ContentValues();
            values.put(CATEGORY_NAME, name);
            db.insert(TABLE_CATEGORIES, null, values);
        }
    }


    //get category id for the given category name
    public int getCategoryId(String categoryName) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_CATEGORIES,
                new String[] {CATEGORY_ID},
                CATEGORY_NAME + "=?",
                new String[]{categoryName},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }

        cursor.close();
        return -1;
    }


    //Password hashing helper
    //uses SHA-256 with a randomly generated salt.
    //the salt will ensure that two users with the same password
    //will have different hashes stored in the database

    //generate a crytographic, random salt encoded as Base64
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }


    //hash a password combined with its salt using SHA-256
    //this returns a Base64-encoded hash string, or null if unavailable
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String combined = password + salt;
            byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        }
        catch (NoSuchAlgorithmException e) {
            return null;
        }
    }


    //User Authentification

    //create a new user
    public boolean createUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        //generate a unique salt
        String salt = generateSalt();

        //hash the password with the salt before storing
        String hashedPassword = hashPassword(password, salt);
        if (hashedPassword == null) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(USERNAME, username);
        values.put(PASSWORD, hashedPassword);
        values.put(PASSWORD_SALT, salt);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }


    //login, retrieves salt, rehashes input, compares against stored hash
    //returns userID if successful, -1 if failed
    public int loginUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor saltCursor = db.query(
                TABLE_USERS,
                new String[]{USER_ID, PASSWORD, PASSWORD_SALT},
                USERNAME + "=?",
                new String[]{username},
                null, null, null
        );

        if (!saltCursor.moveToFirst()) {
            saltCursor.close();
            return -1;
        }

        int userId = saltCursor.getInt(0);
        String storedHash = saltCursor.getString(1);
        String storedSalt = saltCursor.getString(2);
        saltCursor.close();

        //hash the input password with the stored salt
        String inputHash = hashPassword(password, storedSalt);

        //compares hashes, if they match, login successful
        if (inputHash != null && inputHash.equals(storedHash)) {
            return userId;
        }

        return -1;
    }


    //check if username already exists
    public boolean userExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{USER_ID},
                USERNAME + "=?",
                new String[]{username},
                null, null, null
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }


    // Media Items CRUD capabilities

    //add a new media item for a user under a given category name
    public boolean addMediaItem(int userId, String title, String categoryName,
                                int year, float rating, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();

        int categoryId = getCategoryId(categoryName);
        if (categoryId == -1) return false;

        ContentValues values = new ContentValues();
        values.put(ITEM_TITLE, title);
        values.put(ITEM_CATEGORY_ID, categoryId);
        values.put(ITEM_YEAR, year);
        values.put(ITEM_RATING, rating);
        values.put(ITEM_NOTES, notes);
        values.put(ITEM_USER_ID, userId);

        long result = db.insert(TABLE_MEDIA, null, values);
        return result != -1;
    }


    //get all media items for a user in a specific category, sorted by title
    public List<MediaItem> getItemsByCategory(int userId, String categoryName) {
        List<MediaItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        int categoryId = getCategoryId(categoryName);
        if (categoryId == -1) return items;

        //join media items with categories to get the category name back
        String query =
                "SELECT m." + ITEM_ID + ", m." + ITEM_TITLE + ", c." + CATEGORY_NAME +
                        ", m." + ITEM_YEAR + ", m." + ITEM_RATING + ", m." + ITEM_NOTES +
                        " FROM " + TABLE_MEDIA + " m" +
                        " JOIN " + TABLE_CATEGORIES + " c ON m." + ITEM_CATEGORY_ID +
                        " = c." + CATEGORY_ID +
                        " WHERE m." + ITEM_USER_ID + " = ? AND m." + ITEM_CATEGORY_ID + " = ?" +
                        " ORDER BY m." + ITEM_TITLE + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(userId),
                String.valueOf(categoryId)
        });

        while (cursor.moveToNext()) {
            MediaItem item = new MediaItem(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getFloat(4),
                    cursor.getString(5)
            );
            items.add(item);
        }

        cursor.close();
        return items;
    }


    //search items by title within a category using LIKE
    public List<MediaItem> searchItems(int userId, String categoryName, String query) {
        List<MediaItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        int categoryId = getCategoryId(categoryName);
        if (categoryId == -1) return items;

        String sql =
                "SELECT m." + ITEM_ID + ", m." + ITEM_TITLE + ", c." + CATEGORY_NAME +
                        ", m." + ITEM_YEAR + ", m." + ITEM_RATING + ", m." + ITEM_NOTES +
                        " FROM " + TABLE_MEDIA + " m" +
                        " JOIN " + TABLE_CATEGORIES + " c ON m." + ITEM_CATEGORY_ID +
                        " = c." + CATEGORY_ID +
                        " WHERE m." + ITEM_USER_ID + " = ? AND m." + ITEM_CATEGORY_ID + " = ?" +
                        " AND m." + ITEM_TITLE + " LIKE ?" +
                        " ORDER BY m." + ITEM_TITLE + " ASC";

        Cursor cursor = db.rawQuery(sql, new String[]{
                String.valueOf(userId),
                String.valueOf(categoryId),
                "%" + query + "%"
        });

        while (cursor.moveToNext()) {
            MediaItem item = new MediaItem(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getFloat(4),
                    cursor.getString(5)
            );
            items.add(item);
        }

        cursor.close();
        return items;
    }


    //update an existing media item
    public boolean updateMediaItem(int itemId, String title, int year,
                                   float rating, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ITEM_TITLE, title);
        values.put(ITEM_YEAR, year);
        values.put(ITEM_RATING, rating);
        values.put(ITEM_NOTES, notes);

        int rows = db.update(
                TABLE_MEDIA,
                values,
                ITEM_ID + "=?",
                new String[]{String.valueOf(itemId)}
        );

        return rows > 0;
    }


    //delete a media item by its ID
    public void deleteMediaItem(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(
                TABLE_MEDIA,
                ITEM_ID + "=?",
                new String[]{String.valueOf(itemId)}
        );
    }
}
