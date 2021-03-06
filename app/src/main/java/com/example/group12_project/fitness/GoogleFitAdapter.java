package com.example.group12_project.fitness;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


import android.support.annotation.NonNull;
import android.util.Log;

import com.example.group12_project.DataReader;
import com.example.group12_project.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;

public class GoogleFitAdapter implements FitnessService {

    // for fit api permission d
    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE
            = System.identityHashCode(this) & 0xFFFF;

    // for logging
    private final String TAG = "GoogleFitAdapter";

    private DataReader dailyReader;

    // to link the activity with with adapter
    private MainActivity activity;

    // constructor for GoogleFitAdapter
    public GoogleFitAdapter(MainActivity activity) {
        this.dailyReader = new DataReader(activity,1,2);
        this.activity = activity;
    }

    // we need this for google fit
    @Override
    public int getRequestCode() {
        return GOOGLE_FIT_PERMISSIONS_REQUEST_CODE;
    }

    // setup the fitness tracking options
    public void setup() {

        // declaring the Fit API data types and access required
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();

        // check if the user has previously granted the necessary data access
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity),
                fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    activity,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(activity),
                    fitnessOptions
            );
        } else {
            update_daily_steps();
            startRecording();
        }
    }

    // subscribe step count for Google to record
    private void startRecording() {

        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity);

        // check if already signed in
        if (lastSignedInAccount == null) {
            return;
        }

        // subscribe
        Fitness.getRecordingClient(activity, lastSignedInAccount)
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully subscribed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem subscribing.");
                    }
                });

        Fitness.getRecordingClient(activity, lastSignedInAccount)
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully subscribed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem subscribing.");
                    }
                });
    }


    public void dataSetup() {
        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity);


        // check if already signed in
        if (lastSignedInAccount == null) {
            return;
        }
        //long DAY_IN_MS = 1000 * 60 * 60 * 24
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        // Create a data source
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(activity)
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setStreamName(TAG + " - step count")
                .setType(DataSource.TYPE_RAW)
                .build();

        //Dummy number for testing
        int stepCountDelta = 1000;
        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint = dataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_STEPS).setInt(stepCountDelta);
        dataSet.add(dataPoint);


        Task<Void> response = Fitness.getHistoryClient(activity, lastSignedInAccount)
                .insertData(dataSet);

        response.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> write) {
                if (write.isSuccessful()) {
                    Log.i(TAG, "Insert History Task Worked");
                } else {
                    Log.i(TAG, "Insert History Task Failed");
                    Exception e = write.getException();
                }
            }
        });
    }


    //Skeleton code from google api for outputting results of a HistoryClient task


    public void dataReader() {

        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity);

        // check if already signed in
        if (lastSignedInAccount == null) {
            return;
        }

        //Specifying the timeframe for retrieval
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = getDateInstance();
        Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(TAG, "Range End: " + dateFormat.format(endTime));

        //Creating the readrequest for our desired time and data type
        DataReader reader = new DataReader(activity, startTime, endTime);
        List<DataSet> data = reader.getData();
        reader.dumpDataSets();
    }

    // get the update of daily step count
    public void update_daily_steps() {
       // dataReader();
        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity);

        // check if already signed in
        if (lastSignedInAccount == null) {
            return;
        }

        long total = dailyReader.getDailyData();
        Log.d(TAG, "Total steps: " + total);
        activity.setStepCount(total);
        // Stores today's step count using sharedPreferences
        Calendar newCal = Calendar.getInstance();
        activity.storeDailyStepCount(newCal.get(Calendar.DAY_OF_WEEK), total);
        // And add today's step count to total
        activity.storeTotalStepCount(newCal.get(Calendar.DAY_OF_WEEK), total);

    }
}
