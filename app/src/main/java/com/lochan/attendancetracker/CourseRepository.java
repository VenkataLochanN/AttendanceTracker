package com.lochan.attendancetracker;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CourseRepository {

    private static final String PREFS_NAME = "MyCoursePreferences"; // Name of your SharedPreferences file
    private static final String KEY_COURSE_NAMES = "course_names_set"; // Key for storing the set of course names

    private SharedPreferences sharedPreferences;

    // Constructor: Initializes SharedPreferences
    public CourseRepository(Context context) {
        // Get SharedPreferences instance. MODE_PRIVATE means only this app can access these preferences.
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Saves the list of course names to SharedPreferences.
     * @param courseNames The list of course names to save.
     */
    public void saveCourseNames(ArrayList<String> courseNames) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // SharedPreferences can store a Set<String> directly.
        // Convert ArrayList to HashSet to ensure uniqueness and for SharedPreferences compatibility.
        Set<String> courseSet = new HashSet<>(courseNames);
        editor.putStringSet(KEY_COURSE_NAMES, courseSet);
        editor.apply(); // Asynchronously saves the changes. Use commit() for synchronous save.
    }

    /**
     * Loads the list of course names from SharedPreferences.
     * @return An ArrayList of course names. Returns an empty list if no courses are saved.
     */
    public ArrayList<String> loadCourseNames() {
        // Retrieve the Set<String>. If the key doesn't exist, return a new empty HashSet.
        Set<String> courseSet = sharedPreferences.getStringSet(KEY_COURSE_NAMES, new HashSet<>());
        // Convert the Set back to an ArrayList for use with the ArrayAdapter.
        return new ArrayList<>(courseSet);
    }

    /**
     * Clears all saved course names from SharedPreferences.
     * (Optional - if you need a way to reset the data)
     */
    public void clearAllCourses() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_COURSE_NAMES); // Remove the specific key
        // or editor.clear(); // To remove all keys from this SharedPreferences file
        editor.apply();
    }
}
