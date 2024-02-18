package com.unipi.di.sam.myhealth;

import static com.unipi.di.sam.myhealth.MainActivity.NOTIFICATION_CHANNEL_ID;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class MyService extends Service implements SensorEventListener {
    private int currentSteps=0;
    private int currentDuration=0;
    private int currentCalories=0;
    private String data;

    private SensorManager sensorManager;
    private Sensor sensorStepCounter;
    private boolean isCounterSensorPresent;
    private float activityDurationInSeconds=0;
    private int caloriesCounter=1;

    private HashMap<String, updateFromServiceKeeper> list;

    public MyService() {}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Notification notification= new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.icona_launcher_round)
                .setContentText("Monitoraggio dell'attività fisica in funzione")
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .build();
        startForeground(1, notification);

        LocalDateTime localDateTime= new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int year= localDateTime.getYear();
        int month= localDateTime.getMonthValue();
        int day= localDateTime.getDayOfMonth();
        data= year+"/"+month+"/"+day;

        list= new HashMap<>();

        list.put(data, new updateFromServiceKeeper(data,0,0,0,0));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        sensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null){
            sensorStepCounter= sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            isCounterSensorPresent= true;
        }
        else {
            Toast.makeText(this, "Funzione di conta passi non supportata dalla Smartphone", Toast.LENGTH_LONG).show();
            isCounterSensorPresent= false;
        }

        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            sensorManager.registerListener(this, sensorStepCounter, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d("myService: Sensore StepCounter", "Sensore StepCounter registrato");
        }
        else {
            Toast.makeText(this, "Funzione di conta passi non supportata dalla Smartphone", Toast.LENGTH_LONG).show();
            isCounterSensorPresent= false;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            sensorManager.unregisterListener(this, sensorStepCounter);
            Log.d("myService: Sensore StepCounter", "Sensore StepCounter deregistrato");
        }
        else {
            Toast.makeText(this, "Funzione di conta passi non supportata dalla Smartphone", Toast.LENGTH_LONG).show();
            isCounterSensorPresent= false;
        }

        ArrayList<updateFromServiceKeeper> tmp= new ArrayList<>();

        for(updateFromServiceKeeper updateFromServiceKeeper: list.values()) {
            tmp.add(updateFromServiceKeeper);
        }

        Intent i= new Intent("UpdateFromService");
        i.putParcelableArrayListExtra("AggiornamentoDaService", tmp);
        sendBroadcast(i);

        Log.d("myService: Terminazione Service", "Il numero di passi fatto mentre l'app era chiusa è: "+currentSteps+"\nLa durata dell'attività è: "+ currentDuration+"\nmentre le calorie correnti sono: "+currentCalories);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        LocalDateTime localDateTime= new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int year= localDateTime.getYear();
        int month= localDateTime.getMonthValue();
        int day= localDateTime.getDayOfMonth();
        String data1= year+"/"+month+"/"+day;

        if(event.sensor == sensorStepCounter) {
            currentSteps++;

            activityDurationInSeconds+= 0.63;
            if(activityDurationInSeconds >= 60) {
                currentDuration++;
                activityDurationInSeconds=0;
            }

            if(currentSteps == caloriesCounter*20) {
                caloriesCounter++;
                currentCalories++;
                /*
                Mi sono reso conto che potrei anche usarne uno solo tra i due.
                 */
            }


            if(!list.containsKey(data1)){
                list.put(data1, new updateFromServiceKeeper(data1, 0,0,0,1));

                list.put(data, new updateFromServiceKeeper(data, currentSteps, currentDuration, currentCalories, caloriesCounter));
                currentSteps=0;
                currentDuration=0;
                currentCalories=0;
                caloriesCounter=1;

                data=data1;
            }
            else {
                list.put(data, new updateFromServiceKeeper(data, currentSteps, currentDuration, currentCalories, caloriesCounter));
            }
            Log.d("myService: Sensore StepCounter", "Il nuovo numero di passi corrente è: "+ currentSteps +"\nLa durata è: "+currentDuration+ "\nLe calorie: "+currentCalories);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public static class updateFromServiceKeeper implements Parcelable {
        private String data;
        private int currentSteps;
        private int currentDuration;
        private int currentCalories;
        private int caloriesCounter;

        public updateFromServiceKeeper(String data, int currentSteps, int currentDuration, int currentCalories, int caloriesCounter) {
            this.data= data;
            this.currentSteps= currentSteps;
            this.currentDuration= currentDuration;
            this.currentCalories= currentCalories;
            this.caloriesCounter= caloriesCounter;
        }

        public String getData() {
            return data;
        }

        public int getCurrentSteps() {
            return currentSteps;
        }

        public int getCurrentDuration() {
            return currentDuration;
        }

        public int getCurrentCalories() {
            return currentCalories;
        }

        public int getCaloriesCounter() {
            return caloriesCounter;
        }

        protected updateFromServiceKeeper(Parcel in) {
            data = in.readString();
            currentSteps = in.readInt();
            currentDuration = in.readInt();
            currentCalories = in.readInt();
            caloriesCounter = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(data);
            dest.writeInt(currentSteps);
            dest.writeInt(currentDuration);
            dest.writeInt(currentCalories);
            dest.writeInt(caloriesCounter);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<updateFromServiceKeeper> CREATOR = new Creator<updateFromServiceKeeper>() {
            @Override
            public updateFromServiceKeeper createFromParcel(Parcel in) {
                return new updateFromServiceKeeper(in);
            }

            @Override
            public updateFromServiceKeeper[] newArray(int size) {
                return new updateFromServiceKeeper[size];
            }
        };
    }
}