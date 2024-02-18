package com.unipi.di.sam.myhealth;

import static com.unipi.di.sam.myhealth.MainActivity.ACTION_SHOW_TRACKING_FRAGMENT;
import static com.unipi.di.sam.myhealth.MainActivity.NOTIFICATION_CHANNEL_ID;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class TrackingService extends LifecycleService {
    /**
     * Variabile booleana che mi permette di capire se il tracking è attivo oppure no
     */
    public static MutableLiveData<Boolean> isTracking= new MutableLiveData<Boolean>();

    /**
    * Questo elemento è una lista di liste di coordinate <Latitudine, Longitudine>
    * Praticamente è una lista di percorsi, dove ogni percorso è una lista di coordinare.
     */
    public static MutableLiveData<ArrayList<ArrayList<LatLng>>> percorsoAllenamento= new MutableLiveData<ArrayList<ArrayList<LatLng>>>();

    public static MutableLiveData<Integer> speed= new MutableLiveData<Integer>();

    public static MutableLiveData<Long> timeRunInMilliSeconds= new MutableLiveData<Long>();

    private FusedLocationProviderClient fusedLocationProviderClient;

    private boolean isTimerEnable=false;
    //Tempo tra il click su start e il click su Pausa
    private long lapTime= 0L;
    //Tempo totale della corsa, cioè tutti i lapTime insieme
    private long timeRun= 0L;
    //Momento in cui avviamo il Timer
    private long timeStarted= 0L;
    private long lastSecondTimeStamp= 0L;

    /**
     * Metodo che utlizziamo per tenere il Timer della nostra corsa
     */
    private void startTimer() {
        isTimerEnable=true;
        timeStarted= System.currentTimeMillis();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(isTracking.getValue()==true) {
                    //Differenza di tempo tra adesso e timeStarted
                    lapTime= System.currentTimeMillis() - timeStarted;

                    //post the new lapTime
                    timeRunInMilliSeconds.postValue(timeRun + lapTime);

                    //Log.d("Allenamento", timeRunInMilliSeconds.getValue().toString());

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                timeRun+= lapTime;
            }
        }).start();
    }

    public TrackingService() {}

    /**
     * Metodo che inizializza i due MutableLiveData che si occupano di tenere le informazioni riguardo
     * al tracking del percorso
     */
    private void setInitialValues() {
        isTracking.setValue(false);
        percorsoAllenamento.setValue(new ArrayList<>());
        timeRunInMilliSeconds.setValue(0L);
    }

    /**
     * Metodo che inizializza la lista di percorsi se null, oppure aggiungere un nuovo percorso
     * al termine della lista di percorsi nel caso in cui la lista generale sia non null.
     */
    private void addTrattoPercorso() {
        ArrayList<ArrayList<LatLng>> tmp= percorsoAllenamento.getValue();

        if(tmp!=null) {
            tmp.add(new ArrayList<>());
            percorsoAllenamento.postValue(tmp);
        }
        else {
            ArrayList<ArrayList<LatLng>> tmp1= new ArrayList<>();
            tmp1.add(new ArrayList<>());
            percorsoAllenamento.postValue(tmp1);
        }
    }

    /**
     * Metodo che data una coordinata la salva come ultima coordinata nell'ultimo parte di percorso
     * registrato sulla corrispettiva lista.
     * @param location
     */
    private void addCoordinateToLastPercorso(Location location) {
        if(location!=null) {
            ArrayList<ArrayList<LatLng>> list= percorsoAllenamento.getValue();
            if(list!=null) {
                ArrayList<LatLng> list1= list.get(list.size()-1);
                list1.add(new LatLng(location.getLatitude(), location.getLongitude()));
                percorsoAllenamento.postValue(list);
            }
        }
    }

    /**
     *
     */
    LocationCallback locationCallback= new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            if(isTracking.getValue()!= null) {
                if(isTracking.getValue()==true) {
                    if(locationResult!=null) {
                        if(locationResult.getLocations()!=null) {
                            for(Location location: locationResult.getLocations()) {
                                addCoordinateToLastPercorso(location);
                                //Log.d("Nuova Location", location.getLatitude()+" "+location.getLongitude());

                               speed.setValue((int) location.getSpeed());
                            }
                        }
                    }
                }
            }
        }
    };

    /**
     *
     * @param isTracking
     */
    private void updateLocationtracking(boolean isTracking) {
        if(isTracking==true) {
            //Controllo se ho ancora i permessi necessari
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "È necessario che i permessi sulla Posizione precedentemente richiesti" +
                        "e già accettati dall'utente, vengano riaccettati, perchè un questo momento la funzionalità di " +
                        "monitoramento dell'allenamento potrebbe non funzionare correttamente", Toast.LENGTH_LONG).show();
            }

            LocationRequest request= new LocationRequest.Builder(5000L)
                    .setMinUpdateIntervalMillis(2000L)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY).build();

            fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        }
        else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        super.onBind(intent);
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        switch(intent.getAction()) {
            case "Avvia":{
                Log.d("TrackingService", "Tracking service avviato per registrare l'allenamento");
                isTracking.setValue(true);
                startTimer();
            } break;

            case "Riprendi": {
                Log.d("TrackingService", "Tracking service ripresa allenamento");
                isTracking.setValue(true);
                startTimer();
            } break;

            case "Pausa": {
                Log.d("TrackingService", "Tracking service messo in pausa");
                isTracking.setValue(false);
                addTrattoPercorso();
                this.isTimerEnable= false;
            } break;

            case "Fine": {
                Log.d("TrackingService", "Tracking service stoppato per porre fine al tracking dell'allenamento");
                isTracking.setValue(false);
                this.isTimerEnable= false;
                stopSelf();
            } break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setInitialValues();
        addTrattoPercorso();

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        isTracking.observe(this, item -> {
            updateLocationtracking(item);
        });

        Intent i = new Intent(this, MainActivity.class);
        i.setAction(ACTION_SHOW_TRACKING_FRAGMENT);
        PendingIntent pi= PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT
                | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.running_icon)
                .setContentTitle("Rilevamento allenamento")
                .setContentText("00:00")
                .setContentIntent(pi)
                .build();

        startForeground(3, notification);
    }
}