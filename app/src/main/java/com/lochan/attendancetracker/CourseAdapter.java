package com.lochan.attendancetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
// Import ProgressBar
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class CourseAdapter extends ArrayAdapter<String> {

    private Context mContext;
    public ArrayList<String> mCourseNames;
    private SharedPreferences mSharedPreferences;
    private static final String PREFS_NAME = "AttendancePrefs";
    private static final String KEY_ATTENDED_PREFIX = "attended_";
    private static final String KEY_TOTAL_PREFIX = "total_";
    private CourseRepository courseRepository;

    public CourseAdapter(@NonNull Context context, ArrayList<String> courseNames) {
        super(context, 0, courseNames);
        mContext = context;
        mCourseNames = courseNames;
        mSharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        courseRepository = new CourseRepository(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        ViewHolder holder; // ViewHolder pattern

        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.courseNameTextView = listItem.findViewById(R.id.coursename);
            holder.skiptxt = listItem.findViewById(R.id.skiptext);
            holder.attendancelayout = listItem.findViewById(R.id.attendance_layout);
            holder.numberTextView = listItem.findViewById(R.id.numbertxt);
            holder.progressBar = listItem.findViewById(R.id.attendanceProgressBar); // Find ProgressBar
            listItem.setTag(holder);
        } else {
            holder = (ViewHolder) listItem.getTag();
        }

        String currentCourse = mCourseNames.get(position);
        holder.courseNameTextView.setText(currentCourse);

        // Load attendance and update UI
        int attended = getAttendedClasses(currentCourse);
        int total = getTotalClasses(currentCourse);
        updateAttendanceUI(holder, attended, total);


        holder.attendancelayout.setOnClickListener(v -> {
            // Pass the ViewHolder to update its views directly
            showUpdateAttendanceDialog(currentCourse, holder);
        });

        // Add LongClickListener for deletion to the course name TextView
        holder.attendancelayout.setOnLongClickListener(view -> {
            // Ensure position is still valid when long-clicked
            if (position < mCourseNames.size()) {
                final String courseToDelete = mCourseNames.get(position);
                showDeleteConfirmationDialog(courseToDelete, position);
            }
            return true; // Consume the long click
        });

        // --- Listeners for Increment/Decrement Buttons ---
        holder.incbtn = listItem.findViewById(R.id.addbtn);
        holder.decbtn = listItem.findViewById(R.id.delbtn);

        if (holder.decbtn != null) {
            holder.decbtn.setOnClickListener(v -> {
                if (position < mCourseNames.size()) { // Ensure position is still valid
                    int currentAttended = getAttendedClasses(currentCourse);
                    int currentTotal = getTotalClasses(currentCourse); // Get total to avoid going below 0 or other logic
                    if (currentAttended > 0) {
                        currentTotal++;
                        saveAttendance(currentCourse, currentAttended, currentTotal);
                        updateAttendanceUI(holder, currentAttended, currentTotal);
                        Toast.makeText(mContext, "Absent for " + currentCourse, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, "Attended classes cannot be less than 0", Toast.LENGTH_SHORT).show();
                    }
                }                });
        } else {
            // Log an error or handle the case where the button is not found
            Log.e("CourseAdapter", "Button not found in layout!");
        }

        holder.incbtn.setOnClickListener(v -> {
            if (position < mCourseNames.size()) { // Ensure position is still valid
                int currentAttended = getAttendedClasses(currentCourse);
                int currentTotal = getTotalClasses(currentCourse);
                if (currentTotal == 0 && currentAttended == 0) {
                     currentTotal = 1;
                     currentAttended++;
                     saveAttendance(currentCourse, currentAttended, currentTotal);
                     updateAttendanceUI(holder, currentAttended, currentTotal);
                    Toast.makeText(mContext, "Present for " + currentCourse, Toast.LENGTH_SHORT).show();
                } else if (currentAttended < currentTotal) {
                    currentAttended++;
                    currentTotal++;
                    saveAttendance(currentCourse, currentAttended, currentTotal);
                    updateAttendanceUI(holder, currentAttended, currentTotal);
                    Toast.makeText(mContext, "Present for " + currentCourse, Toast.LENGTH_SHORT).show();
                } else if (currentAttended >= currentTotal && currentTotal > 0) {
                    Toast.makeText(mContext, "Attended classes cannot exceed total classes (" + currentTotal + ")", Toast.LENGTH_SHORT).show();
                } else { // total is 0, but attended might be > 0 (data inconsistency) or user trying to increment
                    Toast.makeText(mContext, "Please set up by tapping on the item.", Toast.LENGTH_LONG).show();
                }
            }
        });

        return listItem;
    }

    // ViewHolder class for better performance
    private static class ViewHolder {
        public View attendancelayout;
        Button decbtn;
        Button incbtn;
        TextView skiptxt;
        TextView courseNameTextView;
        TextView numberTextView;
        ProgressBar progressBar;
    }

    // Helper method to update UI elements related to attendance
    private void updateAttendanceUI(ViewHolder holder, int attended, int total) {
        if (total > 0) {
            double percent = (double) attended / (double) total;
            double math = percent * 100;
            String roundedString = String.format("%.2f", math); // Result: "123.46"

            double doubleValue = 0;
            try {
                doubleValue = Double.parseDouble(roundedString);
            } catch (NumberFormatException e) {
                System.err.println("Invalid number format: " + e.getMessage());
            }

            holder.numberTextView.setText(doubleValue + "%");
            holder.progressBar.setMax(total);
            holder.progressBar.setProgress(attended);
            holder.progressBar.setVisibility(View.VISIBLE);

            int green = ContextCompat.getColor(getContext(), R.color.green);
            int red = ContextCompat.getColor(getContext(), R.color.red);
            int yellow = ContextCompat.getColor(getContext(), R.color.yellow);

            if(doubleValue >= 80){
                holder.progressBar.setProgressTintList(ColorStateList.valueOf(green));
                holder.numberTextView.setTextColor(green);
            } else if(doubleValue <= 70){
                holder.progressBar.setProgressTintList(ColorStateList.valueOf(red));
                holder.numberTextView.setTextColor(red);
            } else if (doubleValue > 70 && doubleValue < 80) {
                holder.progressBar.setProgressTintList(ColorStateList.valueOf(yellow));
                holder.numberTextView.setTextColor(yellow);
            }

            double skipclass = ((100 * (double) attended) - (75 * (double) total)) / 75;
            double needclass = ((100 * (double) attended) - (75 * (double) total)) / 25;

            if(doubleValue > 75 && Math.floor(skipclass) > 0){
                holder.skiptxt.setText("Great! You can skip " + Math.floor(skipclass) + " session(s)");
                holder.skiptxt.setTextColor(green);
            } else if (doubleValue >= 75 && Math.floor(skipclass) <= 0){
                holder.skiptxt.setText("You are on track, do not skip!");
                holder.skiptxt.setTextColor(yellow);
            } else if (doubleValue < 75) {
                holder.skiptxt.setText("You need to attend " + Math.ceil(Math.abs(needclass)) + " session(s) to cover");
                holder.skiptxt.setTextColor(red);
            }
        } else {
            int flamingo = ContextCompat.getColor(getContext(), R.color.flamingo);

            holder.numberTextView.setText("0/0");
            holder.skiptxt.setText("Click to add details");
            holder.skiptxt.setTextColor(flamingo);
            holder.progressBar.setMax(100); // Default max if no total
            holder.progressBar.setProgress(0);
            // Optionally hide the progress bar if no data or total is 0
            // holder.progressBar.setVisibility(View.GONE); // Or View.INVISIBLE
        }
    }


    private void showUpdateAttendanceDialog(String courseName, ViewHolder holderToUpdate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.myDialogTheme);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View dialogView = inflater.inflate(R.layout.dialog_update_attendance, null);
        builder.setView(dialogView);

        final EditText editTextAttendedClasses = dialogView.findViewById(R.id.editTextAttendedClasses);
        final EditText editTextTotalClasses = dialogView.findViewById(R.id.editTextTotalClasses);

        editTextAttendedClasses.setText(String.valueOf(getAttendedClasses(courseName)));
        editTextTotalClasses.setText(String.valueOf(getTotalClasses(courseName)));

//        builder.setTitle("Update Attendance for '" + courseName + "'");

        builder.setPositiveButton("Update", (dialog, which) -> {
            String attendedStr = editTextAttendedClasses.getText().toString().trim();
            String totalStr = editTextTotalClasses.getText().toString().trim();

            if (TextUtils.isEmpty(attendedStr) || TextUtils.isEmpty(totalStr)) {
                Toast.makeText(mContext, "Please enter both attended and total classes", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int attendedClasses = Integer.parseInt(attendedStr);
                int totalClasses = Integer.parseInt(totalStr);

                if (attendedClasses < 0 || totalClasses < 0) {
                    Toast.makeText(mContext, "Values cannot be negative", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (attendedClasses > totalClasses) {
                    Toast.makeText(mContext, "Attended classes cannot exceed total classes", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveAttendance(courseName, attendedClasses, totalClasses);

                // Update the UI elements using the ViewHolder
                updateAttendanceUI(holderToUpdate, attendedClasses, totalClasses);

                Toast.makeText(mContext, "Attendance updated for " + courseName, Toast.LENGTH_LONG).show();

            } catch (NumberFormatException e) {
                Toast.makeText(mContext, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        int mantle = ContextCompat.getColor(getContext(), R.color.mantle);
        int surface1 = ContextCompat.getColor(getContext(), R.color.surface1);


        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_border);
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(mantle);
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(surface1);

    }

    // New method for delete confirmation dialog
    private void showDeleteConfirmationDialog(final String courseName, final int position) {
        AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext, R.style.myDialogTheme2);
        builder2.setTitle("Confirm Deletion");
        builder2.setMessage("Are you sure you want to remove '" + courseName + "' and its attendance data?");
        builder2.setPositiveButton("Remove", (dialog, which) -> {
                    removeCourseAndData(courseName, position);
                });
        builder2.setNegativeButton("Cancel", null);
        builder2.setIcon(android.R.drawable.ic_dialog_alert); // Standard alert icon

        int mantle = ContextCompat.getColor(getContext(), R.color.mantle);
        int surface1 = ContextCompat.getColor(getContext(), R.color.surface1);


        AlertDialog alertDialog2 = builder2.create();
        alertDialog2.getWindow().setBackgroundDrawableResource(R.drawable.rounded_border);
        alertDialog2.show();

        alertDialog2.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(mantle);
        alertDialog2.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(surface1);
    }

    // New method to handle actual removal from data source and SharedPreferences
    private void removeCourseAndData(String courseName, int position) {
        if (position < mCourseNames.size() && mCourseNames.get(position).equals(courseName)) {
            // 1. Remove from the adapter's list
            mCourseNames.remove(courseName);

            // 2. Notify the adapter that the data set has changed
            notifyDataSetChanged(); // This will refresh the ListView

            // 3. Remove from CourseRepository (which handles saving the course name list)
            courseRepository.saveCourseNames(mCourseNames);

            // 4. Remove attendance data from SharedPreferences
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.remove(KEY_ATTENDED_PREFIX + courseName);
            editor.remove(KEY_TOTAL_PREFIX + courseName);
            editor.apply();

            Toast.makeText(mContext, "'" + courseName + "' removed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "Error removing course. Please refresh.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAttendance(String courseName, int attendedClasses, int totalClasses) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(KEY_ATTENDED_PREFIX + courseName, attendedClasses);
        editor.putInt(KEY_TOTAL_PREFIX + courseName, totalClasses);
        editor.apply();
    }

    private int getAttendedClasses(String courseName) {
        return mSharedPreferences.getInt(KEY_ATTENDED_PREFIX + courseName, 0);
    }

    private int getTotalClasses(String courseName) {
        return mSharedPreferences.getInt(KEY_TOTAL_PREFIX + courseName, 0);
    }
}