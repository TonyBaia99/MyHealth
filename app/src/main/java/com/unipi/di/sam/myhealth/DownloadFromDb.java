package com.unipi.di.sam.myhealth;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class DownloadFromDb extends Activity implements Runnable{
    private myDataBase myDataBase;
    private myViewModel myViewModel;
    private String data;

    public DownloadFromDb(myDataBase myDataBase, myViewModel myViewModel, String data) {
        this.myDataBase= myDataBase;
        this.myViewModel= myViewModel;
        this.data=data;
    }

    @Override
    public void run() {
        SQLiteDatabase db= myDataBase.getReadableDatabase();

        /*
        Scarico dal DB le ultime informazioni riguardo l'attività Giornaliera
         */
        String[] colums= new String[]{"Passi","Passi_Goal", "Durata_Attività","DurataAttività_Goal","Calorie","Calorie_Goal", "Data"};
        Cursor c= db.query("Attività_Giornaliera", colums, "Data=?",new String[]{data},null,null,null);
        c.moveToLast();
        MainActivity.Attività_Giornaliera attività_giornaliera= new MainActivity.Attività_Giornaliera(c.getInt(0), c.getInt(1), c.getInt(2), c.getInt(3), c.getInt(4), c.getInt(5));
        MainActivity.stepCounter stepCounter= new MainActivity.stepCounter(c.getInt(0), c.getInt(1));
        Log.d("Scaricamento da DB", attività_giornaliera.toString());


        colums= new String[]{"Ore_di_sonno","Ore_di_sonno_Goal","Minuti_di_sonno","Minuti_di_sonno_Goal","Inizio","Fine", "Data"};
        c= db.query("Ore_di_Sonno", colums, "Data=?",new String[]{data},null,null,null);
        c.moveToLast();
        MainActivity.sleep sleep= new MainActivity.sleep(c.getInt(0), c.getInt(1), c.getInt(2), c.getInt(3), c.getInt(4), c.getInt(5));
        Log.d("Scaricamento da DB", sleep.toString());


        ArrayList<MainActivity.trainingRecap> list= new ArrayList<>();
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy/MM/dd kk:mm", Locale.ITALY);
        colums= new String[]{"Tipologia_Allenamento","Duarata_allenamento","Calorie","Distanza_percorsa","Orario_allenamento"};
        c= db.query("Cronologia_allenamenti", colums, null,null,null,null,null);
        c.moveToLast();
        c.moveToPrevious();
        try {
            Log.d("Scaricamento da DB", "Provo a vedere se ci sono allenamenti recenti");
            MainActivity.trainingRecap trainingRecap1= new MainActivity.trainingRecap(c.getString(0), c.getInt(1), c.getInt(2), c.getInt(3), sdf.parse(c.getString(4)));
            Log.d("Scaricamento da DB", trainingRecap1.toString());
            list.add(0,trainingRecap1);
            c.moveToNext();
            MainActivity.trainingRecap trainingRecap2= new MainActivity.trainingRecap(c.getString(0), c.getInt(1), c.getInt(2), c.getInt(3), sdf.parse(c.getString(4)));
            Log.d("Scaricamento da DB", trainingRecap2.toString());
            list.add(1,trainingRecap2);
        } catch (ParseException e) {
            Log.d("Scaricamento da DB", e.toString());
        }
        MainActivity.trainingHistory trainingHistory= new MainActivity.trainingHistory(list);


        colums= new String[]{"bicchieri_correnti","bicchieri_goal", "Data"};
        c= db.query("Bicchieri_Dacqua", colums, "Data=?",new String[]{data},null,null,null);
        c.moveToLast();
        MainActivity.glassesOfWater glassesOfWater= new MainActivity.glassesOfWater(c.getInt(0), c.getInt(1));
        Log.d("Scaricamento da DB", glassesOfWater.toString());


        colums= new String[]{"Peso","Altezza","BMI", "Data"};
        c= db.query("Composizione_Corpo", colums, "Data=?",new String[]{data},null,null,null);
        c.moveToLast();
        MainActivity.bodyComposition bodyComposition= new MainActivity.bodyComposition(c.getInt(0), c.getInt(1), c.getInt(2));
        Log.d("Scaricamento da DB", bodyComposition.toString());

        c.close();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myViewModel.setAttività_giornalieraMutableLiveData(attività_giornaliera);
                myViewModel.setStepCounterMutableLiveData(stepCounter);
                myViewModel.setSleepMutableLiveData(sleep);
                myViewModel.setTrainingHistoryMutableLiveData(trainingHistory);
                myViewModel.setGlassesOfWaterMutableLiveData(glassesOfWater);
                myViewModel.setBodyCompositionMutableLiveData(bodyComposition);
            }
        });
    }
}
