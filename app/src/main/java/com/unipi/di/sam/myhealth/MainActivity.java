package com.unipi.di.sam.myhealth;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int PHYISCAL_ACTIVITY = 0;
    private static final int LOCATION_PERMISSION = 3;
    private static final int LOCATION_BACKGROUND_PERMISSION = 4;

    public static final String NOTIFICATION_CHANNEL_ID= "MyHealthChannel";
    public static final String NOTIFICATION_CHANNEL_NAME= "MyHealth";
    public static final String ACTION_SHOW_TRACKING_FRAGMENT= "ACTION_SHOW_TRACKING_FRAGMENT";

    private BottomNavigationView bottomNavigationView;
    private NotificationManager nm;

    //Riferimento a database app e MyViewModel nella quale tengo i dati di tutto quello che accade
    public myDataBase myDataBase;
    private myViewModel myViewModel;

    private HomeFragment homeFragment;
    private TrainingFragment trainingFragment;

    //Variabili globali necessarie per lo stepCounting
    private DataUpdateReceiver dataUpdateReceiver;
    private SensorManager sensorManager;
    private Sensor sensorStepCounter;
    private boolean isCounterSensorPresent;
    private float activityDurationInSeconds=0;
    private int caloriesCounter=1;

    //Variabili udate per l'aggiornamento gironaliero dello stato
    private AlarmManager alarmManager;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LifeCycle myApp", "Siamo nel metodo onCreate");

        setContentView(R.layout.activity_main);

        nm= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        /*
        Pezzo di codice che richiede il permesso di utilizzare il contapassi.
         */
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED){
            Log.d("Richiesta dei permessi", "Il permesso 'ACTIVITY_RECOGNITION' è già stato accordato");
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestActivityRecognitionPermission();
            }
        }

        /*
         * Recupero il sensore per contare i passi, e nel caso in cui non fosse disponibile
         * faccio apparire un Toast dicendo quale sia il problema
         */
        sensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null){
            sensorStepCounter= sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            isCounterSensorPresent= true;
        }
        else {
            //Toast.makeText(this, "Funzione di conta passi non supportata dalla Smartphone", Toast.LENGTH_LONG).show();
            isCounterSensorPresent= false;
        }

        /*
        Pezzo di codice che richiede il permesso di utilizzare il gli allarmi
         */
        alarmManager= (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                Log.d("Richiesta dei permessi", "Il permesso 'SCHEDULE_EXACT_ALARM' è già stato accordato");
            } else {
                requestScheduleExactAlarmPermission();
            }
        }

        /*
        Controllo se l'aaplicazione viene eseguita per la prima volta oppure no.
        Nel caso in cui è stata avviata per la prima volta, metto nel DB delle righe di base da utilizzare
        per riempire le View nella pagina Home.
         */
        sharedPreferences= getSharedPreferences("myPreferences",0);
        myDataBase= new myDataBase(MainActivity.this);
        if(itsFirstTime()) {
            try {
                fill_DB();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            try {
                setAlarm();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Log.d("FirstTime", "Questa è la prima volta che l'app parte");

            sharedPreferences.edit().putBoolean("AutoTrDetection", false).apply();
        }
        else {
            Log.d("FirstTime", "Questa NON è la prima volta che l'app viene eseguita");
        }

        /*
         * Inizializzo il ViewModel in cui verranno inseriti i dati mostrati nelle View Della pagina Home
         */
        myViewModel= new ViewModelProvider(this).get(com.unipi.di.sam.myhealth.myViewModel.class);
        initializeMyViewModel();

        bottomNavigationView= findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(onItemSelectedListener);

        homeFragment= new HomeFragment();
        trainingFragment= new TrainingFragment();

        /*
        Controllo se l'applicazione è stata avviata dalla notifica relativa ad un allenamento
        in corso. In quel caso navigo immediatamente verso il fragment specifico.
         */
        if(getIntent().getAction().equals(ACTION_SHOW_TRACKING_FRAGMENT)) {
            replaceFragment(trainingFragment);
        }
        else {
            replaceFragment(homeFragment);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(intent.getAction().equals(ACTION_SHOW_TRACKING_FRAGMENT)) {
            replaceFragment(trainingFragment);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("LifeCycle myApp", "Siamo nel metodo onStart");

        /*
         * Attivo il Thread che scarica dal DB i dati iniziali con cui andremo a riempire le View
         */
        LocalDateTime localDateTime= new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int year= localDateTime.getYear();
        int month= localDateTime.getMonthValue();
        int day= localDateTime.getDayOfMonth();
        String data= year+"/"+month+"/"+day;
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy/MM/dd");
        Calendar c= Calendar.getInstance();
        try {
            c.setTime(sdf.parse(data));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        data= sdf.format(c.getTime());
        Runnable downLoadFromDb= new DownloadFromDb(myDataBase, myViewModel, data);
        Thread t= new Thread(downLoadFromDb);
        t.start();


        if(isCounterSensorPresent && sharedPreferences.getBoolean("AutoTrDetection", true)) {
            if (dataUpdateReceiver == null) dataUpdateReceiver = new DataUpdateReceiver();
            IntentFilter intentFilter = new IntentFilter("UpdateFromService");
            registerReceiver(dataUpdateReceiver, intentFilter);
        }

        if(isMyServiceRunning(MyService.class) && sharedPreferences.getBoolean("AutoTrDetection", true)) {
            Log.d("myService", "Il service era in esecuzione adesso lo fermo");
            stopService(new Intent(this, MyService.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LifeCycle myApp", "Siamo nel metodo onResume");

        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            sensorManager.registerListener(this, sensorStepCounter, SensorManager.SENSOR_DELAY_UI);
            Log.d("Sensore StepCounter", "Sensore StepCounter registrato");
        }
        else {
            //Toast.makeText(this, "Funzione di conta passi non supportata dalla Smartphone", Toast.LENGTH_LONG).show();
            isCounterSensorPresent= false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("LifeCycle myApp", "Siamo nel metodo onPause");

        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            sensorManager.unregisterListener(this, sensorStepCounter);
            Log.d("Sensore StepCounter", "Sensore StepCounter deregistrato");
        }
        else {
            //Toast.makeText(this, "Funzione di conta passi non supportata dalla Smartphone", Toast.LENGTH_LONG).show();
            isCounterSensorPresent= false;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("LifeCycle myApp", "Siamo nel metodo onStop");

        LocalDateTime localDateTime= new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int year= localDateTime.getYear();
        int month= localDateTime.getMonthValue();
        int day= localDateTime.getDayOfMonth();
        String data= year+"/"+month+"/"+day;
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy/MM/dd");
        Calendar c= Calendar.getInstance();
        try {
            c.setTime(sdf.parse(data));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        data= sdf.format(c.getTime());
        Runnable updateDB= new UpdateDataBase(myDataBase, myViewModel, data);
        Thread t= new Thread(updateDB);
        t.start();

        if(isCounterSensorPresent && sharedPreferences.getBoolean("AutoTrDetection", true)) {
            if(dataUpdateReceiver!=null) unregisterReceiver(dataUpdateReceiver);
            Intent i= new Intent(this, MyService.class);
            startForegroundService(i);
        }
    }

    /**
     * Crea un Channel per le notifiche che verrà utilizzato dall'applicazione
     */
    private void createNotificationChannel() {
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
        nm.createNotificationChannel(channel);
    }

    /**
     * BroadCastRecevier utilizzato per Aggiornare i dati dopo il service ha inviato un Intent con i
     * dati raccolti mentre l'app non era visibile all'utente
     */
    private class DataUpdateReceiver extends BroadcastReceiver {
        ArrayList<MyService.updateFromServiceKeeper> list;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("UpdateFromService")) {

               LocalDateTime localDateTime= new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
               int year= localDateTime.getYear();
               int month= localDateTime.getMonthValue();
               int day= localDateTime.getDayOfMonth();
               String data1= year+"/"+month+"/"+day;
               list= intent.getParcelableArrayListExtra("AggiornamentoDaService");

               for(MyService.updateFromServiceKeeper tmp: list) {
                   if(tmp.getData().equals(data1)) {
                       Attività_Giornaliera attività_giornaliera= new Attività_Giornaliera(myViewModel.getAttività_giornalieraMutableLiveData().getValue());
                       attività_giornaliera.currentSteps+= tmp.getCurrentSteps();
                       attività_giornaliera.activityDuration+= tmp.getCurrentDuration();
                       attività_giornaliera.currentCalories+= tmp.getCurrentCalories();
                       setCaloriesCounter(tmp.getCaloriesCounter());

                       stepCounter stepCounter= new stepCounter(myViewModel.getStepCounterMutableLiveData().getValue());
                       stepCounter.currentSteps+= tmp.getCurrentSteps();

                       myViewModel.setAttività_giornalieraMutableLiveData(attività_giornaliera);
                       myViewModel.setStepCounterMutableLiveData(stepCounter);
                   }
                   else {
                       new Thread(new Runnable() {
                           @Override
                           public void run() {
                               Attività_Giornaliera attività_giornaliera= myDataBase.getAttivitàGiornaliera(tmp.getData());

                               attività_giornaliera.currentSteps+= tmp.getCurrentSteps();
                               attività_giornaliera.activityDuration+= tmp.getCurrentDuration();
                               attività_giornaliera.currentCalories+= tmp.getCurrentCalories();

                               myDataBase.update_AttivitàGiornaliera(attività_giornaliera, tmp.getData());
                           }
                       }).start();
                   }
               }
            }
        }
    }

    public void setCaloriesCounter(int caloriesCounter) {
        this.caloriesCounter = caloriesCounter;
    }

    /**
     * Questo metodo mi permette di controllare se il mio service specificato dalla classe passata
     * come argomento è in esecuzione oppure no
     * @param serviceClass
     * @return
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Metodo che mi aiuta a vedere se l'app viene eseguita per la prima volta oppure no.
     * @return
     */
    private boolean itsFirstTime() {
        if (sharedPreferences.getBoolean("firstTime", true)) {
            sharedPreferences.edit().putBoolean("firstTime", false).apply();
            return true;
        } else {
            return false;
        }
    }

    //*********************************************************************************************
    /**
     * Metodo tramite un Dialog chiede di accettare dei permessi necessari per la funzione di contapassi
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requestActivityRecognitionPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACTIVITY_RECOGNITION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permesso necessario ContaPassi")
                    .setMessage("Questo permesso è necessario per avviare la funzione di contapassi")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, PHYISCAL_ACTIVITY);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, PHYISCAL_ACTIVITY);
        }
    }

    /**
     * Metodo tramite un Dialog chiede di accettare dei permessi necessari per la funzione di contapassi
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void requestScheduleExactAlarmPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SCHEDULE_EXACT_ALARM)) {
            new AlertDialog.Builder(this)
                    .setTitle("Richiesta permesso 'SCHEDULE_EXACT_ALARM'")
                    .setMessage("Permetti a questa app di impostare allarmi e pianificare altre azioni. Questa app potrebbe" +
                            "essere utilizzata quando tu non stati usando il tuo telefono, quindi potrebbe usare più batteria." +
                            "Se questo permesso fosse disattivato, l'app potrebbe non funzionare normalmente, e i suoi allarmi" +
                            "non funzioneranno come pianificato.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }
        else {
            Intent i= new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(i);
        }
    }

    /**
     * Metodo utilizzato per richiedere i permessi riguardo l'avere la posizione dell'utente
     * in Foregorund.
     */
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION);
    }

    /**
     * Metodo utilizzato per richiedere all'utente i permessi per poter tracciare la posizione dell'utente
     * anche in background.
     */
    private void requestBackgroundLocationPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, LOCATION_BACKGROUND_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PHYISCAL_ACTIVITY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Richiesta dei Permessi", "Permesso PHYISCAL_ACTIVITY accordato");
                }
                else {
                    Log.d("Richiesta dei Permessi", "Permesso PHYISCAL_ACTIVITY non accordato");
                }
            } break;

            case LOCATION_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Richiesta Permessi", "Permessi LOCATION_PERMISSION accordati");

                    /*
                    Una volta che i permessi per la location in Foregorund sono stati accettati, vado
                    a chiedere anche quelli per recuperare la posizione dell'utente in background.
                     */
                    requestBackgroundLocationPermission();
                }
                else {
                    Log.d("Richiesta Permessi", "Permessi LOCATION_PERMISSION non accordati, ma vanno necessariamente " +
                            "richiesti");
                    requestLocationPermission();
                }
            } break;

            case LOCATION_BACKGROUND_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Richiesta Permessi", "Permessi LOCATION_BACKGROUND_PERMISSION accordati");
                }
                else {
                    Log.d("Richiesta Permessi", "Permessi LOCATION_BACKGROUND_PERMISSION non accordato, ma vanno necessariamente " +
                            "richiesti");
                }
            }
        }
    }
    //***********************************************************************************************

    //**************************************METODI PER I SENSORI************************************
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor == sensorStepCounter) {
            stepCounter new_stepCounter= new stepCounter(myViewModel.getStepCounterMutableLiveData().getValue());
            new_stepCounter.currentSteps++;

            Attività_Giornaliera attività_giornaliera= new Attività_Giornaliera(myViewModel.getAttività_giornalieraMutableLiveData().getValue());
            attività_giornaliera.currentSteps++;

            activityDurationInSeconds+= 0.63;
            if(activityDurationInSeconds >= 60) {
                attività_giornaliera.activityDuration++;
                activityDurationInSeconds=0;
            }

            if(attività_giornaliera.currentSteps == caloriesCounter*20) {
                caloriesCounter++;
                attività_giornaliera.currentCalories++;
            }

            Log.d("Sensore StepCounter", "Il nuovo numero di passi corrente è:"+new_stepCounter.currentSteps);

            myViewModel.setAttività_giornalieraMutableLiveData(attività_giornaliera);
            myViewModel.setStepCounterMutableLiveData(new_stepCounter);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    //**********************************************************************************************

    //****************************************METODI CHE GESTISCONO IL MENU PRINCIPALE**************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi= getMenuInflater();
        mi.inflate(R.menu.general_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem checkbox= menu.findItem(R.id.AutoTrDetection);

        if(sharedPreferences.getBoolean("AutoTrDetection",false)) {
            checkbox.setChecked(true);
        }
        else{
            checkbox.setChecked(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.impostazioni: {
                /*
                Ramo in cui permetteremo all'utente di specificare quale sia l'accounta Google da collegale a luo profilo.
                E varie altre impostazioni. Vedere APP Samsung Health per esempi da copiare.
                 */
                return true;
            }

            case R.id.AutoTrDetection: {
                /*
                Item del menu utilizzato per dare la possibilità all'utente di attivare o disattivare
                a piacimento la modalità di rilevamento automatico dell'attività tramite foregraoundService.
                Il valore messo nelle sharedPreferences verrà utilizzato per decidere se attivare o meno il service.
                 */
                if(!item.isChecked() && !sharedPreferences.getBoolean("AutoTrDetection", false)) {
                    item.setChecked(true);
                    sharedPreferences.edit().putBoolean("AutoTrDetection", true).apply();
                }
                else {
                    if(item.isChecked() && sharedPreferences.getBoolean("AutoTrDetection", true)) {
                        item.setChecked(false);
                        sharedPreferences.edit().putBoolean("AutoTrDetection", false).apply();
                    }
                }
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //**********************************************************************************************

    /**
     * Metodo in cui viene avviato uno dei 3 fragment principali al tocco del corrispettivo
     * pulsante nella barra di navigazione
     */
    private final NavigationBarView.OnItemSelectedListener onItemSelectedListener= item -> {
        switch (item.getItemId()){
            case R.id.home: {
                replaceFragment(homeFragment);
            } break;

            case R.id.allenamento: {
                /*
                Pezzo di codice utilizzato per richiedere i permessi sulla posizione
                 */
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    Log.d("Richiesta dei permessi", "Il permesso 'ACCESS_FINE_LOCATION' e 'ACCESS_BACKGROUND_LOCATION' sono già stati accordati");
                }
                else {
                    requestLocationPermission();
                }

                replaceFragment(trainingFragment);
            } break;

            case R.id.profilo: {

            } break;

            default:
                throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }
        return true;
    };

    /**
     * Metodo utilizzato per spostarsi da Fragment a Fragment
     * @param fragment
     */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fm= getSupportFragmentManager();
        FragmentTransaction fragmentTransaction= fm.beginTransaction();

        fragmentTransaction.replace(R.id.myfragmentContainerView, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }

    //********************************************************************************************
    /*
    Varie classi di utility utilizzate qua e la nel codice per vari motivi
     */
    static class Attività_Giornaliera {
        int currentSteps, stepsGoal, activityDuration, activityDurationGoal,
                currentCalories, CaloriesGoal;

        public Attività_Giornaliera() {

        }

        public Attività_Giornaliera (int currentSteps, int stepsGoal, int activityDuration,
                                     int activityDurationGoal, int currentCalories, int CaloriesGoal){
            this.currentSteps= currentSteps;
            this.stepsGoal= stepsGoal;
            this.activityDuration= activityDuration;
            this.activityDurationGoal= activityDurationGoal;
            this.currentCalories= currentCalories;
            this.CaloriesGoal= CaloriesGoal;
        }

        public Attività_Giornaliera(Attività_Giornaliera value) {
            this.currentSteps= value.currentSteps;
            this.stepsGoal= value.stepsGoal;
            this.activityDuration= value.activityDuration;
            this.activityDurationGoal= value.activityDurationGoal;
            this.currentCalories= value.currentCalories;
            this.CaloriesGoal= value.CaloriesGoal;
        }

        @Override
        public String toString() {
            return "Attività_Giornaliera{" +
                    "currentSteps=" + currentSteps +
                    ", stepsGoal=" + stepsGoal +
                    ", activityDuration=" + activityDuration +
                    ", activityDurationGoal=" + activityDurationGoal +
                    ", currentCalories=" + currentCalories +
                    ", CaloriesGoal=" + CaloriesGoal +
                    '}';
        }

        public void setCurrentSteps(int currentSteps) {
            this.currentSteps = currentSteps;
        }

        public void setStepsGoal(int stepsGoal) {
            this.stepsGoal = stepsGoal;
        }

        public void setActivityDuration(int activityDuration) {
            this.activityDuration = activityDuration;
        }

        public void setActivityDurationGoal(int activityDurationGoal) {
            this.activityDurationGoal = activityDurationGoal;
        }

        public void setCurrentCalories(int currentCalories) {
            this.currentCalories = currentCalories;
        }

        public void setCaloriesGoal(int caloriesGoal) {
            CaloriesGoal = caloriesGoal;
        }
    }

    static class stepCounter {
        int currentSteps, stepsGoal;

        public stepCounter(){}

        public stepCounter(int currentSteps, int stepsGoal) {
            this.currentSteps= currentSteps;
            this.stepsGoal= stepsGoal;
        }

        public stepCounter(stepCounter value) {
            this.currentSteps= value.currentSteps;
            this.stepsGoal= value.stepsGoal;
        }

        @NonNull
        @Override
        public String toString() {
            return "stepCounter{" +
                    "currentSteps=" + currentSteps +
                    ", stepsGoal=" + stepsGoal +
                    '}';
        }
    }

    static class sleep {
        int hoursOfSleep, hoursOfSleepGoal, minutesOfSleep, minutesOfSleepGoal, startSleep, endSleep;

        public sleep(int hoursOfSleep, int hoursOfSleepGoal, int minutesOfSleep,
                     int minutesOfSleepGoal, int startSleep, int endSleep) {
            this.hoursOfSleep= hoursOfSleep;
            this.hoursOfSleepGoal= hoursOfSleepGoal;
            this.minutesOfSleep= minutesOfSleep;
            this.minutesOfSleepGoal= minutesOfSleepGoal;
            this.startSleep= startSleep;
            this.endSleep= endSleep;
        }

        public sleep() {}

        @NonNull
        @Override
        public String toString() {
            return "sleep{" +
                    "hoursOfSleep=" + hoursOfSleep +
                    ", hoursOfSleepGoal=" + hoursOfSleepGoal +
                    ", minutesOfSleep=" + minutesOfSleep +
                    ", minutesOfSleepGoal=" + minutesOfSleepGoal +
                    ", startSleep=" + startSleep +
                    ", endSleep=" + endSleep +
                    '}';
        }
    }

    static class trainingHistory {
        List<trainingRecap> trainingRecaplist;

        public trainingHistory(List<trainingRecap> trainingRecaplist) {
            this.trainingRecaplist= trainingRecaplist;
        }

        public trainingHistory() {}

        public void setTrainingRecaplist(List<trainingRecap> trainingRecaplist) {
            this.trainingRecaplist = trainingRecaplist;
        }
    }

    static class trainingRecap {
        String typeOfTraining;
        int trainingDuration, calories, distace;
        Date trainingTime;

        public trainingRecap(String typeOfTraining, int trainingDuration, int calories, int distace,
                             Date trainingTime) {
            this.typeOfTraining= typeOfTraining;
            this.trainingDuration= trainingDuration;
            this.calories= calories;
            this.distace= distace;
            this.trainingTime= trainingTime;
        }

        public trainingRecap() {

        }

        @NonNull
        @Override
        public String toString() {
            return "trainingRecap{" +
                    "typeOfTraining='" + typeOfTraining + '\'' +
                    ", trainingDuration=" + trainingDuration +
                    ", calories=" + calories +
                    ", distace=" + distace +
                    ", trainingTime=" + trainingTime +
                    '}';
        }
    }

    static class glassesOfWater {
        int currentGlasses, glassesGoal;

        public glassesOfWater(int currentGlasses, int glassesGoal) {
            this.currentGlasses= currentGlasses;
            this.glassesGoal= glassesGoal;
        }

        public glassesOfWater() {

        }

        @NonNull
        @Override
        public String toString() {
            return "glassesOfWater{" +
                    "currentGlasses=" + currentGlasses +
                    ", glassesGoal=" + glassesGoal +
                    '}';
        }
    }

    static class bodyComposition {
        int weight, height;
        float bmi;

        public bodyComposition(int weight, int height, float bmi) {
            this.weight= weight;
            this.height= height;
            this.bmi= bmi;
        }

        public bodyComposition() {

        }

        @NonNull
        @Override
        public String toString() {
            return "bodyComposition{" +
                    "weight=" + weight +
                    ", height=" + height +
                    ", bmi=" + bmi +
                    '}';
        }
    }
    //**********************************************************************************************

    private void initializeMyViewModel() {
        myViewModel.setAttività_giornalieraMutableLiveData(new Attività_Giornaliera(0,10000,0,30,0,500));
        myViewModel.setStepCounterMutableLiveData(new stepCounter(0,10000));
        myViewModel.setSleepMutableLiveData(new sleep(0,0,0,0,0,0));

        ArrayList<MainActivity.trainingRecap> list= new ArrayList<>();
        list.add(0,new MainActivity.trainingRecap("",0,0,0,new Date()));
        list.add(1,new MainActivity.trainingRecap("",0,0,0,new Date()));
        myViewModel.setTrainingHistoryMutableLiveData(new trainingHistory(list));

        myViewModel.setGlassesOfWaterMutableLiveData(new glassesOfWater(0,10));
        myViewModel.setBodyCompositionMutableLiveData(new bodyComposition(0,0,0));
    }

    /**
     * Metodo per mettere qualche dato all'interdo del DB la prima volta in cui
     * l'applicazione viene eseguita.
     *
     * So benissimo che questo metodo dovrebbe essere eseguito all'interno di un thread non UI, ma avevo bisogno
     * che nel db ci fossero delle righe già pronte, fino a 7 giorni prima della prima esecuzione dell'app.
     * Ho preferito non metterle vuote per far vedere meglio l'effetto dell VIewCustom.
     */
    private void fill_DB() throws ParseException {
        LocalDateTime localDateTime= new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int year= localDateTime.getYear();
        int month= localDateTime.getMonthValue();
        int day= localDateTime.getDayOfMonth();
        String data= year+"/"+month+"/"+day;

        int hour= localDateTime.getHour();
        int minute= localDateTime.getMinute();
        String dataTR= data+" "+hour+":"+minute;

        SimpleDateFormat sdf= new SimpleDateFormat("yyyy/MM/dd");
        Calendar c= Calendar.getInstance();
        c.setTime(sdf.parse(data));
        data= sdf.format(c.getTime());

        try {
            myDataBase.initialFillDBAttivitàGiornaliera(1000,10000, 4, 30, 3, 300, data);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        myDataBase.add_sleep(0,8, 0,30,0,0, data);

        myDataBase.add_bicchieriDacqua(0,10, data);

        myDataBase.add_ComposizioneCorpo(0, 0, 0, data);


        sdf= new SimpleDateFormat("yyyy/MM/dd HH:mm");
        c= Calendar.getInstance();
        c.setTime(sdf.parse(dataTR));
        dataTR= sdf.format(c.getTime());
        myDataBase.add_allenamento("Corsa", 0, 0, 0, dataTR);
        myDataBase.add_allenamento("Corsa", 30, 700, 4500, dataTR);
        myDataBase.add_allenamento("Camminata", 60, 300, 4500, dataTR);
        myDataBase.add_allenamento("Corsa", 45, 100, 6000, dataTR);
        myDataBase.add_allenamento("Corsa", 25, 900, 3000, dataTR);
        myDataBase.add_allenamento("Corsa", 120, 1500, 10000, dataTR);
    }

    /**
     * Metodo per impostare un allarme che attiverà ogni giorno alle 24 un thread che andrà a
     * resettare lo stato dell'attività
     */
    private void setAlarm() throws ParseException {
        LocalDateTime localDateTime= new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int year= localDateTime.getYear();
        int month= localDateTime.getMonthValue();
        int day= localDateTime.getDayOfMonth();
        String data= year+"/"+month+"/"+day;
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy/MM/dd");
        Calendar calendar= Calendar.getInstance();
        calendar.setTime(sdf.parse(data));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 50);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Intent intent= new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent= PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);


        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Log.d("Alarm giornaliero", "MainActivity: Allarme impostato per le:"+ calendar.getTime());
    }
}