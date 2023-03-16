package com.example.verifit;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.*;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener , DatePickerDialog.OnDateSetListener{

    // Data Structures
    public static Set<String> Days = new TreeSet<String>();
    public static ArrayList<WorkoutSet> Sets = new ArrayList<WorkoutSet>();
    public static ArrayList<WorkoutDay> Workout_Days = new ArrayList<WorkoutDay>();
    public static ArrayList<Exercise> KnownExercises = new ArrayList<Exercise>(); // Initialized with hardcoded exercises
    public static String date_selected; // Used for other activities to get the selected date, by default it's set to today
    public static HashMap<String,Double> VolumePRs = new HashMap<String,Double>();
    public static HashMap<String,Double> ActualOneRepMaxPRs = new HashMap<String,Double>();
    public static HashMap<String,Double> EstimatedOneRMPRs = new HashMap<String,Double>();
    public static HashMap<String,Double> MaxRepsPRs = new HashMap<String,Double>();
    public static HashMap<String,Double> MaxWeightPRs = new HashMap<String,Double>();
    public static HashMap<String,Double> LastTimeVolume = new HashMap<String,Double>(); // Holds last workout's volume for each exercise
    public ViewPager2 viewPager2; // View Pager that is used in main activity
    public static ArrayList<WorkoutDay> Infinite_Workout_Days = new ArrayList<WorkoutDay>(); // Used to populate the viewPager object in MainActivity with "infinite" days

    public static final int READ_REQUEST_CODE = 42;
    public static final int PERMISSION_REQUEST_STORAGE = 1000;
    public static String EXPORT_FILENAME = "healthy_backup";



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onCreateStuff();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCreateStuff()
    {
        initActivity();

        // Bottom Navigation Bar Intents
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);


        // Date selected is by default today
        Date date_clicked = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        date_selected = dateFormat.format(date_clicked);
    }


    // When choosing date from DatePicker
    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2)
    {
        i1++;
        String year = String.valueOf(i);
        String month;
        String day;

        month = String.format("%02d", i1);
        day = String.format("%02d", i2);

        String date_clicked = year+"-"+month+"-"+day;
        MainActivity.date_selected = date_clicked;

        // Start Intent
        Intent in = new Intent(getApplicationContext(), DayActivity.class);
        Bundle mBundle = new Bundle();


        // Send Date and start activity
        mBundle.putString("date", date_clicked);
        in.putExtras(mBundle);
        startActivity(in);

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void initActivity()
    {

        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q))
        {
            askWritePermission();
        }

        setExportBackupName();

        getSupportActionBar().setTitle("Healthy");

        Intent in = getIntent();

        String WhatToDO = null;
        WhatToDO = in.getStringExtra("doit");

        if(WhatToDO != null)
        {
            if(WhatToDO.equals("importcsv"))
            {
                fileSearch();
            }
            else if(WhatToDO.equals("exportcsv"))
            {
                // Android 11 and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                {
                    //saveFile();
                    writeFileSAF();
                }
                // Android 10 and below
                else
                {
                    writeFile();
                }

                // Or else nothing comes up
                initViewPager();
            }
        }
        // No intent
        else
        {
            // Get WorkoutDays from shared preferences
            loadWorkoutData();

            // Get Known Exercises from shared preferences
            loadKnownExercisesData();

            // After Loading Data Initialize ViewPager
            initViewPager();
        }
    }

    public boolean checkWritePermission(String permission)
    {
        int check = ContextCompat.checkSelfPermission(this,permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();

        loadWorkoutData();

        loadKnownExercisesData();

        initViewPager();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }


    public void initViewPager()
    {
        if(Infinite_Workout_Days.isEmpty())
        {
            Infinite_Workout_Days.clear();

            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.YEAR, -5);
            Date startDate = c.getTime();
            c.add(Calendar.YEAR, +10);
            Date endDate = c.getTime();

            Calendar start = Calendar.getInstance();
            start.setTime(startDate);
            Calendar end = Calendar.getInstance();
            end.setTime(endDate);

            for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime())
            {
                String date_str = new SimpleDateFormat("yyyy-MM-dd").format(date);

                WorkoutDay today = new WorkoutDay();
                today.setDate(date_str);
                Infinite_Workout_Days.add(today);
            }
        }

        viewPager2 = findViewById(R.id.viewPager2);
        viewPager2.setAdapter(new ViewPagerWorkoutDayAdapter(this,Infinite_Workout_Days));
        viewPager2.setCurrentItem((Infinite_Workout_Days.size()+1)/2);
    }

    public static void setExportBackupName()
    {
        EXPORT_FILENAME = "healthy";
        Format formatter = new SimpleDateFormat("_yyyy-MM-dd_HH:mm:ss");
        String str_date = formatter.format(new Date());
        EXPORT_FILENAME = EXPORT_FILENAME + str_date;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode)
        {
            case 30:
                if(grantResults.length > 0)
                {
                    boolean readper = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeper = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(readper && writeper)
                    {
                        Toast.makeText(getApplicationContext(), "Đã được cho phép", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Quyền bị Từ chối", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Bạn đã từ chối quyền", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    public static int getDayPosition(String Date)
    {
        for(int i = 0; i < MainActivity.Workout_Days.size(); i++)
        {
            if(MainActivity.Workout_Days.get(i).getDate().equals(Date))
            {
                return i;
            }
        }
        return -1;
    }


    public static int getExercisePosition(String Date, String exerciseName)
    {

        int day_position = getDayPosition(Date);
        if (day_position == -1)
        {
            return -1;
        }



        ArrayList<WorkoutExercise> Exercises = MainActivity.Workout_Days.get(day_position).getExercises();

        for(int i = 0; i < Exercises.size(); i++)
        {
            if(Exercises.get(i).getExercise().equals(exerciseName))
            {
                return i;
            }
        }

        return -1;
    }


    public void fileSearch()
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent,READ_REQUEST_CODE);
    }







    public void initKnownExercises()
    {
        KnownExercises.clear();
        // Some hardcoded Exercises
        KnownExercises.add(new Exercise("High-knee running","Legs"));
        KnownExercises.add(new Exercise("Walking","Legs"));
        KnownExercises.add(new Exercise("Push Up","Chest"));
        KnownExercises.add(new Exercise("Squats","Chest"));
        KnownExercises.add(new Exercise("Plank","Glutes"));
        KnownExercises.add(new Exercise("Lunges","Glutes"));
        KnownExercises.add(new Exercise("Dumbbell Goblet Squat","Biceps"));
        KnownExercises.add(new Exercise("Two Arm Dumbbell","Biceps"));
        KnownExercises.add(new Exercise("Burpees","Abs"));
        KnownExercises.add(new Exercise("Squat thrust","Abs"));
        KnownExercises.add(new Exercise("Sphinx Pose","Back"));
        KnownExercises.add(new Exercise("Hip lift","Back"));
        KnownExercises.add(new Exercise("Arm scissors","Shoulders"));
        KnownExercises.add(new Exercise("Wall Push Up","Shoulders"));


    }


    public static void calculatePersonalRecords()
    {
        MainActivity.VolumePRs.clear();
        MainActivity.ActualOneRepMaxPRs.clear();
        MainActivity.EstimatedOneRMPRs.clear();
        MainActivity.MaxRepsPRs.clear();
        MainActivity.MaxWeightPRs.clear();
        MainActivity.LastTimeVolume.clear();

        for(int i = 0; i < MainActivity.KnownExercises.size(); i++)
        {
            MainActivity.VolumePRs.put((MainActivity.KnownExercises.get(i).getName()),0.0);
            MainActivity.ActualOneRepMaxPRs.put((MainActivity.KnownExercises.get(i).getName()),0.0);
            MainActivity.EstimatedOneRMPRs.put((MainActivity.KnownExercises.get(i).getName()),0.0);
            MainActivity.MaxRepsPRs.put((MainActivity.KnownExercises.get(i).getName()),0.0);
            MainActivity.MaxWeightPRs.put((MainActivity.KnownExercises.get(i).getName()),0.0);
            MainActivity.LastTimeVolume.put((MainActivity.KnownExercises.get(i).getName()),0.0);
        }

        for(int i = 0; i < MainActivity.KnownExercises.size(); i++)
        {
            for(int j = 0; j < MainActivity.Workout_Days.size(); j++)
            {
                for(int k = 0; k < MainActivity.Workout_Days.get(j).getExercises().size(); k++)
                {
                    if(MainActivity.Workout_Days.get(j).getExercises().get(k).getExercise().equals(MainActivity.KnownExercises.get(i).getName()))
                    {
                        // Volume Personal Records
                        if(VolumePRs.get(MainActivity.KnownExercises.get(i).getName()) < (MainActivity.Workout_Days.get(j).getExercises().get(k).getVolume()))
                        {
                            MainActivity.Workout_Days.get(j).getExercises().get(k).setVolumePR(true);
                            VolumePRs.put(MainActivity.KnownExercises.get(i).getName(),MainActivity.Workout_Days.get(j).getExercises().get(k).getVolume());
                        }

                        // Actual One Repetition Maximum
                        if(ActualOneRepMaxPRs.get(MainActivity.KnownExercises.get(i).getName()) < (MainActivity.Workout_Days.get(j).getExercises().get(k).getActualOneRepMax()))
                        {
                            MainActivity.Workout_Days.get(j).getExercises().get(k).setActualOneRepMaxPR(true);
                            ActualOneRepMaxPRs.put(MainActivity.KnownExercises.get(i).getName(),MainActivity.Workout_Days.get(j).getExercises().get(k).getActualOneRepMax());
                        }

                        // Estimated One Repetition Maximum
                        if(EstimatedOneRMPRs.get(MainActivity.KnownExercises.get(i).getName()) < (MainActivity.Workout_Days.get(j).getExercises().get(k).getEstimatedOneRepMax()))
                        {
                            MainActivity.Workout_Days.get(j).getExercises().get(k).setEstimatedOneRepMaxPR(true);
                            EstimatedOneRMPRs.put(MainActivity.KnownExercises.get(i).getName(),MainActivity.Workout_Days.get(j).getExercises().get(k).getEstimatedOneRepMax());
                        }

                        // Max Repetitions Personal Records
                        if(MaxRepsPRs.get(MainActivity.KnownExercises.get(i).getName()) < (MainActivity.Workout_Days.get(j).getExercises().get(k).getMaxReps()))
                        {
                            MainActivity.Workout_Days.get(j).getExercises().get(k).setMaxRepsPR(true);
                            MaxRepsPRs.put(MainActivity.KnownExercises.get(i).getName(),MainActivity.Workout_Days.get(j).getExercises().get(k).getMaxReps());
                        }

                        // Max Weight Personal Records
                        if(MaxWeightPRs.get(MainActivity.KnownExercises.get(i).getName()) < (MainActivity.Workout_Days.get(j).getExercises().get(k).getMaxWeight()))
                        {
                            MainActivity.Workout_Days.get(j).getExercises().get(k).setMaxWeightPR(true);
                            MaxWeightPRs.put(MainActivity.KnownExercises.get(i).getName(),MainActivity.Workout_Days.get(j).getExercises().get(k).getMaxWeight());
                        }

                        // Harder Than Last Time!
                        if(LastTimeVolume.get(MainActivity.KnownExercises.get(i).getName()) < (MainActivity.Workout_Days.get(j).getExercises().get(k).getVolume()))
                        {
                            MainActivity.Workout_Days.get(j).getExercises().get(k).setHTLT(true);
                            LastTimeVolume.put(MainActivity.KnownExercises.get(i).getName(),MainActivity.Workout_Days.get(j).getExercises().get(k).getVolume());
                        }
                        // This needs to be updates since we are dealing with last time and not overall maximums
                        else
                        {
                            LastTimeVolume.put(MainActivity.KnownExercises.get(i).getName(),MainActivity.Workout_Days.get(j).getExercises().get(k).getVolume());
                        }



                    }
                }
            }
        }
    }
    public static void saveWorkoutData(Context ct)
    {
        SharedPreferences sharedPreferences = ct.getSharedPreferences("shared preferences",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(Workout_Days);
        Log.e( "String JSON", json );
        editor.putString("workouts",json);
        editor.apply();
    }

    public void loadWorkoutData()
    {
        if(Workout_Days.isEmpty())
        {
            SharedPreferences sharedPreferences = getSharedPreferences("shared preferences",MODE_PRIVATE);
            Gson gson = new Gson();
            String json = sharedPreferences.getString("workouts",null);
            Type type = new TypeToken<ArrayList<WorkoutDay>>(){}.getType();
            Workout_Days = gson.fromJson(json,type);

            if(Workout_Days == null)
            {
                Workout_Days = new ArrayList<WorkoutDay>();
            }
        }


    }

    public static void saveKnownExerciseData(Context ct)
    {
        SharedPreferences sharedPreferences = ct.getSharedPreferences("shared preferences",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(KnownExercises);
        editor.putString("known_exercises",json);
        editor.apply();
    }

    public void loadKnownExercisesData()
    {
        if(KnownExercises.isEmpty())
        {
            SharedPreferences sharedPreferences = getSharedPreferences("shared preferences",MODE_PRIVATE);
            Gson gson = new Gson();
            String json = sharedPreferences.getString("known_exercises",null);
            Type type = new TypeToken<ArrayList<Exercise>>(){}.getType();
            KnownExercises = gson.fromJson(json,type);

            if(KnownExercises == null || KnownExercises.isEmpty())
            {
                KnownExercises = new ArrayList<Exercise>();
                initKnownExercises();
            }
        }
    }



    private boolean isExternalStorageWritable()
    {
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void saveFile()
    {

        String FolderName = "healthy";

        // Test
        InputStream is = new ByteArrayInputStream("mContent".getBytes());
        OutputStream outputStream = null;
        String name =  EXPORT_FILENAME;
        String path = Environment.DIRECTORY_DOCUMENTS+File.separator+"Healthy/";
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME ,name);
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH ,path);
        Uri uri = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            uri = getContentResolver().insert( MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
        }
        if(uri!=null){
            try {
                outputStream = getContentResolver().openOutputStream(uri);

                outputStream.write("Date,Exercise,Category,Weight (kg),Reps,Comment\n".getBytes());

                for(int i = 0; i < MainActivity.Workout_Days.size(); i++)
                {
                    for(int j = 0; j < MainActivity.Workout_Days.get(i).getExercises().size(); j++)
                    {
                        String exerciseComment = MainActivity.Workout_Days.get(i).getExercises().get(j).getComment();
                        for(int k=0; k < MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().size(); k++)
                        {
                            String Date = MainActivity.Workout_Days.get(i).getExercises().get(j).getDate();
                            String exerciseName = MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().get(k).getExercise();
                            String exerciseCategory = MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().get(k).getCategory();
                            Double Weight = MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().get(k).getWeight();
                            Double Reps = MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().get(k).getReps();
                            outputStream.write((Date + "," + exerciseName+ "," + exerciseCategory + "," + Weight + "," + Reps + "," + exerciseComment + "\n").getBytes());
                        }
                    }
                }

                Toast.makeText(getApplicationContext(), "Backup saved in " + path, Toast.LENGTH_LONG).show();
                System.out.println("Backup saved in " + path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }finally {
                try {
                    is.close();
                    outputStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    public void askWritePermission()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void writeFile()
    {
        if(isExternalStorageWritable() && checkWritePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            String healthy_folder = "healthy";

            System.out.println(Environment.getExternalStorageDirectory());
            System.out.println(Environment.getExternalStoragePublicDirectory("healthy"));

            Path path = Environment.getExternalStorageDirectory().toPath();
            path = Paths.get(path + healthy_folder);

            if (Files.exists(path))
            {
                System.out.println("This path exists");
            }
            else
            {
                System.out.println("This path does not exist");

                File folder = new File(Environment.getExternalStorageDirectory() + File.separator + healthy_folder);
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdirs();
                }
                if (success) {
                    System.out.println("healthy folder has been created");
                } else {
                    System.out.println("healthy folder has not been created");
                }

            }

            File textfile = new File(Environment.getExternalStoragePublicDirectory("healthy"), EXPORT_FILENAME);
            try
            {
                FileOutputStream fos = new FileOutputStream(textfile);
                fos.write("Date,Exercise,Category,Weight (kg),Reps,Comment\n".getBytes());

                for(int i = 0; i < MainActivity.Workout_Days.size(); i++)
                {
                    for(int j = 0; j < MainActivity.Workout_Days.get(i).getExercises().size(); j++)
                    {
                        String exerciseComment = MainActivity.Workout_Days.get(i).getExercises().get(j).getComment();
                        for(int k=0; k < MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().size(); k++)
                        {
                            String Date = MainActivity.Workout_Days.get(i).getExercises().get(j).getDate();
                            String exerciseName = MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().get(k).getExercise();
                            String exerciseCategory = MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().get(k).getCategory();
                            Double Weight = MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().get(k).getWeight();
                            Double Reps = MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().get(k).getReps();
                            fos.write((Date + "," + exerciseName+ "," + exerciseCategory + "," + Weight + "," + Reps + "," + exerciseComment + "\n").getBytes());
                        }
                    }
                }
                fos.close();
                Toast.makeText(getApplicationContext(), "File Written in " + Environment.getExternalStorageDirectory(), Toast.LENGTH_SHORT).show();
            }
            catch (IOException e)
            {
                System.out.println(e.getMessage());
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "External Storage Not Writable", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void writeFileSAF()
    {
        String fileName = EXPORT_FILENAME;

        try
        {
            OutputStream outputStream;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName); // file name required to contain extestion file mime
                values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS+"/Healthy"); //DIRECTORY
                Uri extVolumeUri = MediaStore.Files.getContentUri("external");
                Uri fileUri = getApplicationContext().getContentResolver().insert(extVolumeUri, values);
                outputStream = getApplicationContext().getContentResolver().openOutputStream(fileUri);
            }
            else {
                File root = new File(Environment.getExternalStorageDirectory()+File.separator+"DIRECTORY_NAME", "images");
                File file = new File(root, fileName );
                Log.d(TAG, "saveFile: file path - " + file.getAbsolutePath());
                outputStream = new FileOutputStream(file);
            }
            outputStream.write("Date,Exercise,Category,Weight (kg),Reps,Comment\n".getBytes());

            for(int i = 0; i < MainActivity.Workout_Days.size(); i++)
            {
                for(int j = 0; j < MainActivity.Workout_Days.get(i).getExercises().size(); j++)
                {
                    String exerciseComment = MainActivity.Workout_Days.get(i).getExercises().get(j).getComment();
                    for(int k=0; k < MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().size(); k++)
                    {
                        String Date = MainActivity.Workout_Days.get(i).getExercises().get(j).getDate();
                        String exerciseName = MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().get(k).getExercise();
                        String exerciseCategory = MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().get(k).getCategory();
                        Double Weight = MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().get(k).getWeight();
                        Double Reps = MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().get(k).getReps();
                        outputStream.write((Date + "," + exerciseName+ "," + exerciseCategory + "," + Weight + "," + Reps + "," + exerciseComment + "\n").getBytes());
                    }
                }
            }
            outputStream.close();
            Toast.makeText(getApplicationContext(), "Backup saved in " + Environment.DIRECTORY_DOCUMENTS+"/Healthy" , Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            System.out.println(e.toString());
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void clearDataStructures()
    {
        this.Workout_Days.clear();
        this.KnownExercises.clear();
        this.Sets.clear();
        this.Days.clear();
        saveWorkoutData(this);
        saveKnownExerciseData(this);
    }

    public static void sortWorkoutDaysDate()
    {
        Collections.sort(MainActivity.Workout_Days, new Comparator<WorkoutDay>() {
            @Override
            public int compare(WorkoutDay workoutDay, WorkoutDay t1)
            {
                String date1 = workoutDay.getDate();
                String date2 = t1.getDate();
                Date date_object1 = new Date();
                Date date_object2 = new Date();

                try {
                    date_object1 = new SimpleDateFormat("yyyy-MM-dd").parse(date1);
                    date_object2 = new SimpleDateFormat("yyyy-MM-dd").parse(date2);
                }
                catch (Exception e)
                {
                    System.out.println(e.getMessage());
                }
                return date_object1.compareTo(date_object2);
            }
        });
    }

    public static boolean doesExerciseExist(String exercise_name)
    {
        for(int i = 0; i < KnownExercises.size(); i++)
        {
            if(KnownExercises.get(i).getName().equals(exercise_name))
            {
                return true;
            }
        }
        return false;
    }

    public static String getExerciseCategory(String Exercise)
    {
        for(int i = 0; i < KnownExercises.size(); i++)
        {
            if(KnownExercises.get(i).getName().equals(Exercise))
            {
                return KnownExercises.get(i).getBodyPart();
            }
        }
        return "";
    }

    public static void deleteExercise(String exercise_name)
    {
        for (Iterator<WorkoutDay> dayIterator = MainActivity.Workout_Days.iterator(); dayIterator.hasNext(); )
        {

            WorkoutDay currentDay = dayIterator.next();

            for(Iterator<WorkoutExercise> exerciseIterator = currentDay.getExercises().iterator(); exerciseIterator.hasNext();)
            {
                WorkoutExercise current_exercise = exerciseIterator.next();
                if(current_exercise.getExercise().equals(exercise_name))
                {
                    exerciseIterator.remove();
                }
            }

            for(Iterator<WorkoutSet> setIterator = currentDay.getSets().iterator(); setIterator.hasNext();)
            {
                WorkoutSet current_set = setIterator.next();
                if(current_set.getExercise().equals(exercise_name))
                {
                    setIterator.remove();
                }
            }


            if(currentDay.getSets().isEmpty())
            {
                dayIterator.remove();
            }
        }
        for(Iterator<Exercise> exerciseIterator = MainActivity.KnownExercises.iterator(); exerciseIterator.hasNext();)
        {
            Exercise current_exercise = exerciseIterator.next();

            if(current_exercise.getName().equals(exercise_name))
            {
                exerciseIterator.remove();
            }
        }
    }

    public static void editExercise(String exercise_name, String new_exercise_name, String new_exercise_bodypart)
    {
        for(int i = 0; i < MainActivity.KnownExercises.size(); i++)
        {
            if(KnownExercises.get(i).getName().equals(exercise_name))
            {
                KnownExercises.get(i).setName(new_exercise_name);
                KnownExercises.get(i).setBodyPart(new_exercise_bodypart);
            }
        }

        for(int i = 0; i < MainActivity.Workout_Days.size(); i++)
        {
            for(int j = 0; j < MainActivity.Workout_Days.get(i).getSets().size(); j++)
            {
                if(MainActivity.Workout_Days.get(i).getSets().get(j).getExercise().equals(exercise_name))
                {
                    MainActivity.Workout_Days.get(i).getSets().get(j).setExercise(new_exercise_name);
                    MainActivity.Workout_Days.get(i).getSets().get(j).setCategory(new_exercise_bodypart);
                }
            }

            for(int j = 0; j < MainActivity.Workout_Days.get(i).getExercises().size(); j++)
            {
                if(MainActivity.Workout_Days.get(i).getExercises().get(j).getExercise().equals(exercise_name))
                {
                    MainActivity.Workout_Days.get(i).getExercises().get(j).setExercise(new_exercise_name);

                    for(int k = 0; k < MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().size(); k++)
                    {
                        if(MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().get(k).getExercise().equals(exercise_name))
                        {
                            MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().get(k).setExercise(new_exercise_name);
                            MainActivity.Workout_Days.get(i).getExercises().get(j).getSets().get(k).setCategory(new_exercise_bodypart);
                        }
                    }


                }
            }
        }

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        if(item.getItemId() == R.id.home)
        {
            Intent in = new Intent(this,MainActivity.class);
            startActivity(in);
            overridePendingTransition(0,0);
        }
        else if(item.getItemId() == R.id.exercises)
        {
            Intent in = new Intent(this,ExercisesActivity.class);
            startActivity(in);
            overridePendingTransition(0,0);
        }
        else if(item.getItemId() == R.id.diary)
        {
            Intent in = new Intent(this,DiaryActivity.class);
            in.putExtra("date", date_selected);
            startActivity(in);
            overridePendingTransition(0,0);
        }
        else if(item.getItemId() == R.id.charts)
        {
            Intent in = new Intent(this,ChartsActivity.class);
            startActivity(in);
            overridePendingTransition(0,0);
        }
        else if(item.getItemId() == R.id.me)
        {
            Intent in = new Intent(this,MeActivity.class);
            startActivity(in);
            overridePendingTransition(0,0);
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(item.getItemId() == R.id.home)
        {
            viewPager2.setCurrentItem((Infinite_Workout_Days.size()+1)/2);
        }
        else if(item.getItemId() == R.id.settings)
        {
            Intent in = new Intent(this,SettingsActivity.class);
            startActivity(in);
        }
        return super.onOptionsItemSelected(item);
    }
}
