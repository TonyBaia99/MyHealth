package com.unipi.di.sam.myhealth;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TrainingFragment extends Fragment implements View.OnClickListener {
    private TextView durata, distance, velocità, velocitàMedia, calorie;
    private Button avvia, pausa, riprendi, fine;

    private boolean isTracking= false;
    private ArrayList<ArrayList<LatLng>> percorsoAllenamento= new ArrayList<>(new ArrayList<>());

    private long curTimeInMillis= 0L;

    private GoogleMap map;
    private myDataBase myDataBase;

    private double totDistance=0.00;
    private String durataAllenamento;
    private String calorieAllenamento;
    private String distanzaAllenamento;


    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map= googleMap;
            addAllCordinateToPercorso();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        myDataBase= new myDataBase(getActivity());

        Log.d("LifeCycle MyApp", "Siamo nel metodo onCreate di TrainingFragment");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v= inflater.inflate(R.layout.fragment_training, container, false);

        durata= v.findViewById(R.id.durata);
        distance= v.findViewById(R.id.distanceValue);
        velocità= v.findViewById(R.id.speedValue);
        velocitàMedia= v.findViewById(R.id.avgSpeedValue);
        calorie= v.findViewById(R.id.caloreisValue);

        avvia= v.findViewById(R.id.avvia);
        avvia.setOnClickListener(this);

        pausa= v.findViewById(R.id.pausa);
        pausa.setOnClickListener(this);

        riprendi= v.findViewById(R.id.riprendi);
        riprendi.setOnClickListener(this);

        fine= v.findViewById(R.id.fine);
        fine.setOnClickListener(this);

        Log.d("LifeCycle MyApp", "Siamo nel metodo onCreateView di TrainingFragment");

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.myMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        subscribeToObserver();

        if(isTracking==true) {
            avvia.setVisibility(View.GONE);
            pausa.setVisibility(View.VISIBLE);
        }

        Log.d("LifeCycle MyApp", "Siamo nel metodo onViewCreated di TrainingFragment");
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_tr_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.music : {
                Intent i= Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC);
                startActivity(i);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.avvia: {
                v.setVisibility(View.GONE);
                pausa.setVisibility(View.VISIBLE);

                this.isTracking= true;
                sendCommandToService("Avvia");
            } break;

            case R.id.pausa: {
                v.setVisibility(View.GONE);
                riprendi.setVisibility(View.VISIBLE);
                fine.setVisibility(View.VISIBLE);

                this.isTracking= false;
                sendCommandToService("Pausa");
            } break;

            case R.id.riprendi: {
                v.setVisibility(View.GONE);
                fine.setVisibility(View.GONE);
                pausa.setVisibility(View.VISIBLE);

                this.isTracking=true;
                sendCommandToService("Riprendi");
            } break;

            case R.id.fine: {
                v.setVisibility(View.GONE);
                riprendi.setVisibility(View.GONE);
                avvia.setVisibility(View.VISIBLE);

                this.isTracking=false;
                generateDurataAndDIstanceAllenamento();
                sendCommandToService("Fine");

                saveTrainingInDB();
                resetStatus();
            } break;
        }
    }

    /**
     * Metodo che causa il rieseguimento del Service ogni singola volta che questo metodo
     * viene chiamato, ma che ci permette in primis di consegnare l'intent con il comando
     * che ci interessa.
     * @param command
     */
    private void sendCommandToService(String command) {
        Intent i = new Intent(requireContext(), TrackingService.class);
        i.setAction(command);
        requireContext().startService(i);
    }

    /**
     * Metodo che utilizziamo per salvare i dati dell'allenamento appena terminato nel DB
     * TODO
     */
    private void saveTrainingInDB(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("Update new Allenamento", "Thread che aggiungerà il nuovo allenamento sta partendo");
                LocalDateTime localDateTime= new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                int year= localDateTime.getYear();
                int month= localDateTime.getMonthValue();
                int day= localDateTime.getDayOfMonth();
                int hour= localDateTime.getHour();
                int minute= localDateTime.getMinute();
                String data= year+"/"+month+"/"+day+" "+hour+":"+minute;
                SimpleDateFormat sdf= new SimpleDateFormat("yyyy/MM/dd HH:mm");
                Calendar c= Calendar.getInstance();
                try {
                    c.setTime(sdf.parse(data));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                data= sdf.format(c.getTime());

                myDataBase.add_allenamento("Corsa",
                        Integer.parseInt(durataAllenamento),
                        Integer.parseInt(calorieAllenamento),
                        Integer.parseInt(distanzaAllenamento),
                        data);

                Log.d("Update new Allenamento", "Nuovo allenamento registrati in DB: "+"Corsa, "
                        +durataAllenamento+" "+calorieAllenamento+" "+distanzaAllenamento);
            }
        }).start();
    }

    /**
     * Metodo utilizzato per raccogliere i dati dell'allenamento appena terminato, così che
     * possano essere caricati sul DB
     */
    private void generateDurataAndDIstanceAllenamento() {
        String[] tmp_durata = ((String)this.durata.getText()).split(":");
        int minute =((Integer.parseInt((tmp_durata[0])) * 60) + Integer.parseInt(tmp_durata[1]));
        this.durataAllenamento = String.valueOf(minute);

        //double distance = Double.parseDouble((String) this.distance.getText());
        double distance = round(this.totDistance, 2);
        distance = distance * 1000;
        this.distanzaAllenamento = String.valueOf((int) distance);

        this.calorieAllenamento= (String) this.calorie.getText();
    }

    /**
     * Metodo che utilizziamo per resettare le varie variabili utilizzate per tenere traccia
     * dell'allenamento.
     */
    private void resetStatus() {
        this.durata.setText("00:00:00");
        this.velocità.setText("0");
        this.distance.setText("0");
        this.calorie.setText("0");

        curTimeInMillis= 0L;

        this.totDistance= 0.00;

        this.map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(	43.724591, 10.382981), 10f));
        this.map.clear();
    }

    /**
     * Metodo utilizzato per registrare degli observer che verranno eseguiti ogni volta che cambia il valore
     * dell'elemento che l'observer sta tenendo d'occhio
     */
    private void subscribeToObserver() {
        TrackingService.percorsoAllenamento.observe(getViewLifecycleOwner(), item -> {
            this.percorsoAllenamento= item;
            addCoordinateToPercorso();
            moveCameraToUser();
        });

        TrackingService.speed.observe(getViewLifecycleOwner(), item -> {
            this.velocità.setText(String.valueOf(3.6* item));
        });

        TrackingService.timeRunInMilliSeconds.observe(getViewLifecycleOwner(), item -> {
            curTimeInMillis= item;
            String formattedTime= getFormattedTimer(curTimeInMillis);
            this.durata.setText(formattedTime);
        });
    }

    /**
     * Metodo che utilizziamo per far muovere la telecamere sulla mappa in modo tale che segua la posizione dell'utente
     */
    private void moveCameraToUser() {
        if(!percorsoAllenamento.isEmpty() && !percorsoAllenamento.get(percorsoAllenamento.size()-1).isEmpty()) {
            ArrayList<LatLng> lastPartePercorso= percorsoAllenamento.get(percorsoAllenamento.size()-1);
            this.map.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPartePercorso.get(lastPartePercorso.size()-1), 20f));
        }
    }

    /**
     * Metodo che ridisegna l'intero percorso fatto fino ad ora dall'utente, quando necessario.
     */
    private void addAllCordinateToPercorso() {
        for(ArrayList<LatLng> list: percorsoAllenamento) {
            PolylineOptions polylineOptions= new PolylineOptions()
                    .color(getResources().getColor(R.color.green, null))
                    .width(12f)
                    .addAll(list);

            this.map.addPolyline(polylineOptions);
        }
    }

    /**
     * Metodo che disegna sulla mappa, un collegamento tra l'ultima coordianta registrata e la penultima
     * che era giò prensente, così da far vedere il percorso fatto dall'utente sulla mappa.
     */
    private void addCoordinateToPercorso() {
        if(!percorsoAllenamento.isEmpty() && (percorsoAllenamento.get(percorsoAllenamento.size()-1).size() > 1)) {
            ArrayList<LatLng> lastPartePercorso= percorsoAllenamento.get(percorsoAllenamento.size()-1);
            LatLng penunltimaCoordinata= lastPartePercorso.get(lastPartePercorso.size() -2);
            LatLng ultimaCoordinata= lastPartePercorso.get(lastPartePercorso.size() -1);

            PolylineOptions polylineOptions= new PolylineOptions()
                    .color(getResources().getColor(R.color.green, null))
                    .width(12f)
                    .add(penunltimaCoordinata)
                    .add(ultimaCoordinata);

            //Con questo comando sto designando una retta tra la penultima e l'ultima cordinata
            //registrata
            this.map.addPolyline(polylineOptions);

            DecimalFormat df = new DecimalFormat("0.00");
            totDistance+= getDistanceBetweenPointsNew(penunltimaCoordinata, ultimaCoordinata);
            Log.d("DistancePercorso", String.valueOf(totDistance));

            double newDistance= round(totDistance, 2);
            this.distance.setText(df.format(newDistance));
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    /**
     * Metodo che utilizzo per calcolare la distanza tra due punti indicati con Latituidine e Longitudine
     */
    public static double getDistanceBetweenPointsNew(LatLng pos1, LatLng pos2) {
        double theta = pos1.longitude - pos2.longitude;
        double distance = 60 * 1.1515 * (180/Math.PI) * Math.acos(
                Math.sin(pos1.latitude * (Math.PI/180)) * Math.sin(pos2.latitude * (Math.PI/180)) +
                        Math.cos(pos1.latitude * (Math.PI/180)) * Math.cos(pos2.latitude * (Math.PI/180)) * Math.cos(theta * (Math.PI/180)));
        Double newDistance= distance * 1.609344;
        return newDistance;
    }

    /**
     *
     * @param ms
     * @return
     */
    private String getFormattedTimer(Long ms) {
        long milliseconds= ms;
        long hours= TimeUnit.MILLISECONDS.toHours(ms);
        milliseconds-= TimeUnit.HOURS.toMillis(hours);

        long minutes= TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        milliseconds-= TimeUnit.MINUTES.toMillis(minutes);

        long secondi= TimeUnit.SECONDS.convert(milliseconds, TimeUnit.MILLISECONDS);

        StringBuilder ore_minuti_secondi= new StringBuilder();
        if(hours < 10) {
            ore_minuti_secondi.append("0").append(hours).append(":");
        }
        else ore_minuti_secondi.append("").append(hours).append(":");
        if(minutes<10) {
            ore_minuti_secondi.append("0").append(minutes).append(":");
        }
        else ore_minuti_secondi.append("").append(minutes).append(":");
        if(secondi<10) {
            ore_minuti_secondi.append("0").append(secondi);
        }
        else ore_minuti_secondi.append("").append(secondi);

        return ore_minuti_secondi.toString();
    }
}

