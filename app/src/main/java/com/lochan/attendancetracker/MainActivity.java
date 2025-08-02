package com.lochan.attendancetracker;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> dataList;
    // ArrayAdapter<String> adapter; // Remove this
    CourseAdapter adapter; // Use your custom adapter
    ListView listView;
    CourseRepository courseRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        courseRepository = new CourseRepository(this);
        dataList = courseRepository.loadCourseNames();
        if (dataList == null) {
            dataList = new ArrayList<>();
        }

        listView = findViewById(R.id.listview);
        // adapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.coursename, dataList); // Remove this
        adapter = new CourseAdapter(this, dataList); // Initialize your custom adapter
        listView.setAdapter(adapter);

        Button acb = findViewById(R.id.addcoursebtn);
        if (acb != null) {
            acb.setOnClickListener(v -> showAddCourseDialog());
        }

//        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                final String selectedCourse = dataList.get(position);
//                new AlertDialog.Builder(MainActivity.this)
//                        .setTitle("Confirm Action")
//                        .setMessage("Do you want to remove '" + selectedCourse + "'?")
//                        .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                removeCourse(selectedCourse);
//                            }
//                        })
//                        .setNegativeButton("Cancel", null)
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .show();
//                return true;
//            }
//        });
    }

    private void showAddCourseDialog() {
        AlertDialog.Builder builder3 = new AlertDialog.Builder(this, R.style.myDialogTheme3);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_course, null);
        builder3.setView(dialogView);

        final EditText editTextCourseName = dialogView.findViewById(R.id.editTextCourseName);
        builder3.setTitle("Add New Course");

        builder3.setPositiveButton("Add", (dialog, which) -> {
            String courseName = editTextCourseName.getText().toString().trim();
            if (!courseName.isEmpty()) {
                if (!dataList.contains(courseName)) {
                    dataList.add(courseName);
                    adapter.notifyDataSetChanged();
                    courseRepository.saveCourseNames(dataList);
                    Toast.makeText(MainActivity.this, "Course Added: " + courseName, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Course name already exists.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Course name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder3.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        int mantle = ContextCompat.getColor(this, R.color.mantle);
        int surface1 = ContextCompat.getColor(this, R.color.surface1);


        AlertDialog alertDialog3 = builder3.create();
        alertDialog3.getWindow().setBackgroundDrawableResource(R.drawable.rounded_border);
        alertDialog3.show();

        alertDialog3.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(mantle);
        alertDialog3.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(surface1);
    }

    private void removeCourse(String courseName) {
        if (dataList.remove(courseName)) {
            adapter.notifyDataSetChanged();
            courseRepository.saveCourseNames(dataList);
            Toast.makeText(this, courseName + " removed", Toast.LENGTH_SHORT).show();
        }
    }
}
