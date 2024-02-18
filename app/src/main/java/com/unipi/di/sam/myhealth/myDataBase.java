package com.unipi.di.sam.myhealth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


import androidx.annotation.Nullable;


public class myDataBase extends SQLiteOpenHelper {
    private Context context;

    private static final String DATABASE_NAME= "my_database.db";
    private static final int DATABASE_VERSION= 1;

    private static final String COLUMN_ID= "_id";

    private static final String TABLE1_NAME= "Attività_Giornaliera";
    private static final String TABLE1_COLUMN1= "Passi";
    private static final String TABLE1_COLUMN2= "Passi_Goal";
    private static final String TABLE1_COLUMN3= "Durata_Attività";
    private static final String TABLE1_COLUMN4= "DurataAttività_Goal";
    private static final String TABLE1_COLUMN5= "Calorie";
    private static final String TABLE1_COLUMN6= "Calorie_Goal";
    private static final String TABLE1_COLUMN7= "Data";

    private static final String TABLE2_NAME= "Ore_di_Sonno";
    private static final String TABLE2_COLUMN1= "Ore_di_sonno";
    private static final String TABLE2_COLUMN2= "Ore_di_sonno_Goal";
    private static final String TABLE2_COLUMN3= "Minuti_di_sonno";
    private static final String TABLE2_COLUMN4= "Minuti_di_sonno_Goal";
    private static final String TABLE2_COLUMN5= "Inizio";
    private static final String TABLE2_COLUMN6= "Fine";
    private static final String TABLE2_COLUMN7= "Data";

    private static final String TABLE3_NAME= "Cronologia_allenamenti";
    private static final String TABLE3_COLUMN1= "Tipologia_Allenamento";
    private static final String TABLE3_COLUMN2= "Duarata_allenamento";
    private static final String TABLE3_COLUMN3= "Calorie";
    private static final String TABLE3_COLUMN4= "Distanza_percorsa";
    private static final String TABLE3_COLUMN5= "Orario_allenamento";

    private static final String TABLE4_NAME= "Bicchieri_Dacqua";
    private static final String TABLE4_COLUMN1= "bicchieri_correnti";
    private static final String TABLE4_COLUMN2= "bicchieri_goal";
    private static final String TABLE4_COLUMN3 = "Data";

    private static final String TABLE5_NAME= "Composizione_Corpo";
    private static final String TABLE5_COLUMN1= "Peso";
    private static final String TABLE5_COLUMN2= "Altezza";
    private static final String TABLE5_COLUMN3= "BMI";
    private static final String TABLE5_COLUMN4 = "Data";


    public myDataBase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query1= "CREATE TABLE " + TABLE1_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TABLE1_COLUMN1 + " INTEGER, " +
                TABLE1_COLUMN2 + " INTEGER, " +
                TABLE1_COLUMN3 + " INTEGER, " +
                TABLE1_COLUMN4 + " INTEGER, " +
                TABLE1_COLUMN5 + " INTEGER, " +
                TABLE1_COLUMN6 + " INTEGER, " +
                TABLE1_COLUMN7 + " STRING);";

        String query2= "CREATE TABLE "+ TABLE2_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TABLE2_COLUMN1 + " INTEGER, " +
                TABLE2_COLUMN2 + " INTEGER, " +
                TABLE2_COLUMN3 + " INTEGER, " +
                TABLE2_COLUMN4 + " INTEGER, " +
                TABLE2_COLUMN5 + " INTEGER, " +
                TABLE2_COLUMN6 + " INTEGER, " +
                TABLE2_COLUMN7 + " STRING);";

        String query3= "CREATE TABLE " + TABLE3_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TABLE3_COLUMN1 + " TEXT, " +
                TABLE3_COLUMN2 + " INTEGER, " +
                TABLE3_COLUMN3 + " INTEGER, " +
                TABLE3_COLUMN4 + " INTEGER, " +
                TABLE3_COLUMN5 + " STRING);";

        String query4 = "CREATE TABLE " + TABLE4_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TABLE4_COLUMN1 + " INTEGER, " +
                TABLE4_COLUMN2 + " INTEGER, " +
                TABLE4_COLUMN3 + " STRING);";

        String query5= "CREATE TABLE " + TABLE5_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TABLE5_COLUMN1 + " INTEGER, " +
                TABLE5_COLUMN2 + " INTEGER, " +
                TABLE5_COLUMN3 + " FLOAT, " +
                TABLE5_COLUMN4 + " STRING);";

        db.execSQL(query1);
        db.execSQL(query2);
        db.execSQL(query3);
        db.execSQL(query4);
        db.execSQL(query5);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE1_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE2_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE3_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE4_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE5_NAME);

        this.onCreate(db);
    }

    /**
     * Metodo usato esculsivamente una volta per aggiungere nel DB dei dummy data, usiti per mostrare un esempio
     * di utilizzo della ViewCustom presente in una particolare funzionalità dell'applicazione
     * @param passi
     * @param passi_goal
     * @param durataAttività
     * @param durataAttività_goal
     * @param calorie
     * @param calorie_goal
     * @param data
     * @throws ParseException
     */
    public void initialFillDBAttivitàGiornaliera(int passi, int passi_goal, int durataAttività, int durataAttività_goal,
                                                 int calorie, int calorie_goal, String data) throws ParseException {
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv= new ContentValues();

        cv.put(TABLE1_COLUMN1, passi);
        cv.put(TABLE1_COLUMN2, passi_goal);
        cv.put(TABLE1_COLUMN3, durataAttività);
        cv.put(TABLE1_COLUMN4, durataAttività_goal);
        cv.put(TABLE1_COLUMN5, calorie);
        cv.put(TABLE1_COLUMN6, calorie_goal);
        cv.put(TABLE1_COLUMN7, data);

        for(int i=6; i>-1;i--){
            cv.put(TABLE1_COLUMN1, i*passi);
            cv.put(TABLE1_COLUMN2, passi_goal);
            cv.put(TABLE1_COLUMN3, i*durataAttività);
            cv.put(TABLE1_COLUMN4, durataAttività_goal);
            cv.put(TABLE1_COLUMN5, i*calorie);
            cv.put(TABLE1_COLUMN6, calorie_goal);
            cv.put(TABLE1_COLUMN7, generatePreviousDay(i));

            long result= db.insert(TABLE1_NAME, null, cv);
            if(result==-1){
                Log.d("Inserimento riga in db Attività Gionaliera", "Inserimento fallito: "+ cv);
            }
            else {
                Log.d("Inserimento riga in db Attività Gionaliera", "Inserimento riuscito: "+ cv);
            }
        }
    }

    private  String generatePreviousDay(int i) throws ParseException {
        LocalDateTime localDateTime= new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int year= localDateTime.getYear();
        int month= localDateTime.getMonthValue();
        int day= localDateTime.getDayOfMonth();
        String data= year+"/"+month+"/"+day;

        SimpleDateFormat sdf= new SimpleDateFormat("yyyy/MM/dd");
        Calendar c= Calendar.getInstance();
        c.setTime(sdf.parse(data));
        c.add(Calendar.DATE, -i);

        return sdf.format(c.getTime());
    }

    public void add_attivitàGiornaliera(int passi, int passi_goal, int durataAttività, int durataAttività_goal,
                                        int calorie, int calorie_goal, String data) {

        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv= new ContentValues();

        cv.put(TABLE1_COLUMN1, passi);
        cv.put(TABLE1_COLUMN2, passi_goal);
        cv.put(TABLE1_COLUMN3, durataAttività);
        cv.put(TABLE1_COLUMN4, durataAttività_goal);
        cv.put(TABLE1_COLUMN5, calorie);
        cv.put(TABLE1_COLUMN6, calorie_goal);
        cv.put(TABLE1_COLUMN7, data);

        long result= db.insert(TABLE1_NAME, null, cv);

        if(result==-1){
            Log.d("Inserimento riga in db Attività Gionaliera", "Inserimento fallito: "+ cv);
        }
        else {
            Log.d("Inserimento riga in db Attività Gionaliera", "Inserimento riuscito: "+ cv);
        }
    }

    public void update_AttivitàGiornaliera(MainActivity.Attività_Giornaliera attività_giornaliera, String  data) {
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv= new ContentValues();

        cv.put(TABLE1_COLUMN1, attività_giornaliera.currentSteps);
        cv.put(TABLE1_COLUMN2, attività_giornaliera.stepsGoal);
        cv.put(TABLE1_COLUMN3, attività_giornaliera.activityDuration);
        cv.put(TABLE1_COLUMN4, attività_giornaliera.activityDurationGoal);
        cv.put(TABLE1_COLUMN5, attività_giornaliera.currentCalories);
        cv.put(TABLE1_COLUMN6, attività_giornaliera.CaloriesGoal);
        cv.put(TABLE1_COLUMN7, data);

        long result= db.update(TABLE1_NAME, cv, "Data=?", new String[] {data});

        if(result==-1){
            Log.d("Aggiornamento riga in db Attività Gionaliera", "Aggiornamento fallito: "+ cv);
        }
        else {
            Log.d("Aggiornamento riga in db Attività Gionaliera", "Aggiornamento riuscito: "+ cv);
        }
    }

    /**
     * Metodo itilizzato per recuperare dalla Tabella "Attività Giornaliera" la riga corrispondente alla dara passata come argomento
     * @param data
     * @return
     */
    public MainActivity.Attività_Giornaliera getAttivitàGiornaliera(String data) {
        MainActivity.Attività_Giornaliera tmp = null;
        SQLiteDatabase db= this.getReadableDatabase();

        String[] colums= new String[]{"Passi","Passi_Goal", "Durata_Attività","DurataAttività_Goal","Calorie","Calorie_Goal"};
        Cursor c= db.query("Attività_Giornaliera", colums, "Data=?",new String[]{data},null,null,null);
        c.moveToLast();
        tmp= new MainActivity.Attività_Giornaliera(c.getInt(0), c.getInt(1), c.getInt(2) ,c.getInt(3) ,c.getInt(4), c.getInt(5));
        c.close();
        return tmp;
    }

    /**
     * Metodo utilizzato per aggiungere una nuova riga nel DB, e questa righa viene riempita con gli argomenti
     * che vengono passati al metodo.
     * @param ore_di_sonno
     * @param ore_di_sonno_goal
     * @param minuti_di_sonno
     * @param minuti_di_sonno_goal
     * @param inizio
     * @param fine
     * @param data
     */
    public void add_sleep(int ore_di_sonno, int ore_di_sonno_goal, int minuti_di_sonno, int minuti_di_sonno_goal, int inizio, int fine, String data) {
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv= new ContentValues();

        cv.put(TABLE2_COLUMN1, ore_di_sonno);
        cv.put(TABLE2_COLUMN2, ore_di_sonno_goal);
        cv.put(TABLE2_COLUMN3, minuti_di_sonno);
        cv.put(TABLE2_COLUMN4, minuti_di_sonno_goal);
        cv.put(TABLE2_COLUMN5, inizio);
        cv.put(TABLE2_COLUMN6, fine);
        cv.put(TABLE2_COLUMN7, data);

        long result= db.insert(TABLE2_NAME, null, cv);

        if(result==-1) {
            Log.d("Inserimento riga in db Ore di sono", "Inserimento fallito: "+ cv);
        }
        else {
            Log.d("Inserimento riga in db Ore di sonno", "Inserimento riuscito: "+ cv);
        }
    }

    /**
     * Metodo che utilizziamo per aggiornare la riga corrispondente alla "data" nella tabella Sleep
     * con l'altro argomento passato al metodo
     * @param sleep
     * @param data
     */
    public void update_sleep(MainActivity.sleep sleep, String data) {
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv= new ContentValues();

        cv.put(TABLE2_COLUMN1, sleep.hoursOfSleep);
        cv.put(TABLE2_COLUMN2, sleep.hoursOfSleepGoal);
        cv.put(TABLE2_COLUMN3, sleep.minutesOfSleep);
        cv.put(TABLE2_COLUMN4, sleep.minutesOfSleepGoal);
        cv.put(TABLE2_COLUMN5, sleep.startSleep);
        cv.put(TABLE2_COLUMN6, sleep.endSleep);
        cv.put(TABLE2_COLUMN7, data);

        long result= db.update(TABLE2_NAME, cv, "Data=?", new String[] {data});

        if(result==-1) {
            Log.d("Aggiornamento riga in db Ore di sono", "Aggiornamento fallito: "+ cv);
        }
        else {
            Log.d("Aggiornamento riga in db Ore di sonno", "Aggiornamento riuscito: "+ cv);
        }
    }

    /**
     * Metodo itilizzato per recuperare dalla Tabella "sleep" la riga corrispondente alla dara passata come argomento
     * @param data
     * @return
     */
    public MainActivity.sleep getSleep(String data) {
        MainActivity.sleep tmp = null;
        SQLiteDatabase db= this.getReadableDatabase();

        String[] colums= new String[]{"Ore_di_sonno","Ore_di_sonno_Goal","Minuti_di_sonno","Minuti_di_sonno_Goal","Inizio","Fine"};
        Cursor c= db.query("Ore_di_Sonno", colums, "Data=?",new String[]{data},null,null,null);
        c.moveToLast();

        tmp= new MainActivity.sleep(c.getInt(0), c.getInt(1), c.getInt(2), c.getInt(3), c.getInt(4), c.getInt(5));
        return tmp;
    }

    /**
     * Metodo utilizzato per aggiungere una nuova riga al DB, corrispondente ad un allenamento.
     * @param tipologia
     * @param durata
     * @param calorie
     * @param distanza
     * @param data
     */
    public void add_allenamento(String tipologia, int durata, int calorie, int distanza, String data) {
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv= new ContentValues();

        cv.put(TABLE3_COLUMN1, tipologia);
        cv.put(TABLE3_COLUMN2, durata);
        cv.put(TABLE3_COLUMN3, calorie);
        cv.put(TABLE3_COLUMN4, distanza);
        cv.put(TABLE3_COLUMN5, data);

        long result= db.insert(TABLE3_NAME, null, cv);

        if(result==-1) {
            Log.d("Inserimento riga in db Cronologia Allenamenti", "Inserimento fallito: "+ cv);
        }
        else {
            Log.d("Inserimento riga in db Cronologia allenamenti", "Inserimento riuscito: "+ cv);
        }
    }

    /**
     * Metodo utilizzato per aggiungere una nuova riga nel DB, e questa righa viene riempita con gli argomenti
     * che vengono passati al metodo.
     * @param bicchieri_correnti
     * @param bicchieri_obiettivo
     * @param data
     */
    public void add_bicchieriDacqua(int bicchieri_correnti, int bicchieri_obiettivo, String data) {
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv= new ContentValues();

        cv.put(TABLE4_COLUMN1, bicchieri_correnti);
        cv.put(TABLE4_COLUMN2, bicchieri_obiettivo);
        cv.put(TABLE4_COLUMN3, data);

        long result= db.insert(TABLE4_NAME, null, cv);

        if(result==-1) {
            Log.d("Inserimento riga in db Bicchieri D'Acqua", "Inserimento fallito: "+ cv);
        }
        else {
            Log.d("Inserimento riga in db Bicchieri D'Acqua", "Inserimento riuscito: "+ cv);
        }
    }

    /**
     * Metodo che utilizziamo per aggiornare la riga corrispondente alla "data" nella tabella BicchieriD'Aqua
     * con l'altro argomento passto al metodo
     * @param glassesOfWater
     * @param data
     */
    public void update_bicchieriDacqua(MainActivity.glassesOfWater glassesOfWater, String data) {
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv= new ContentValues();

        cv.put(TABLE4_COLUMN1, glassesOfWater.currentGlasses);
        cv.put(TABLE4_COLUMN2, glassesOfWater.glassesGoal);
        cv.put(TABLE4_COLUMN3, data);

        long result= db.update(TABLE4_NAME, cv, "Data=?", new String[] {data});

        if(result==-1) {
            Log.d("Aggiornamento riga in db Bicchieri D'Acqua", "Aggiornamento fallito: "+ cv);
        }
        else {
            Log.d("Aggiornamento riga in db Bicchieri D'Acqua", "Aggiornamento riuscito: "+ cv);
        }
    }

    /**
     * Metodo utilizzato per aggiungere una nuova riga nel DB, e questa righa viene riempita con gli argomenti
     * che vengono passati al metodo.
     * @param peso
     * @param altezza
     * @param bmi
     * @param data
     */
    public void add_ComposizioneCorpo(int peso, int altezza, float bmi, String data) {
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv= new ContentValues();

        cv.put(TABLE5_COLUMN1, peso);
        cv.put(TABLE5_COLUMN2, altezza);
        cv.put(TABLE5_COLUMN3, bmi);
        cv.put(TABLE5_COLUMN4, data);

        long result= db.insert(TABLE5_NAME, null, cv);

        if(result==-1) {
            Log.d("Inserimento riga in db Composizione Corpo", "Inserimento fallito: "+ cv);
        }
        else {
            Log.d("Inserimento riga in db Composizione Corpo", "Inserimento riuscito: "+ cv);
        }
    }

    /**
     * Metodo che utilizziamo per aggiornare la riga corrispondente alla "data" nella tabella Composizione Corpo
     * con l'altro argomento passto al metodo
     * @param bodyComposition
     * @param data
     */
    public void update_ComposizioneCorpo(MainActivity.bodyComposition bodyComposition, String data) {
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv= new ContentValues();

        cv.put(TABLE5_COLUMN1, bodyComposition.weight);
        cv.put(TABLE5_COLUMN2, bodyComposition.height);
        cv.put(TABLE5_COLUMN3, bodyComposition.bmi);
        cv.put(TABLE5_COLUMN4, data);

        long result= db.update(TABLE5_NAME, cv, "Data=?", new String[] {data});

        if(result==-1) {
            Log.d("Aggiornamento riga in db Composizione Corpo", "Aggiornamento fallito: "+ cv);
        }
        else {
            Log.d("Aggiornamento riga in db Composizione Corpo", "Aggiornamento riuscito: "+ cv);
        }
    }

    /**
     * Metodo che mi permette di recuperare gli ultimi obiettivi fissati, dal DB. Per ultimi si intende sempre l'ultima
     * riga di una tabella, sfruttando il fatto che le righe nel DB sono ordinate sempre per data (ogni operazione sul DB
     * è fatta in modo da mantanere questa proprietà).
     * @return
     */
    public HashMap<String, Integer> getLastTargets() {
        SQLiteDatabase db= this.getWritableDatabase();
        int Passi_Goal, DurataAttività_Goal, Calorie_Goal, Ore_di_sonno_Goal, Minuti_di_sonno_Goal, bicchieri_goal;

        HashMap<String, Integer> goals= new HashMap<>();

        String[] colums= new String[]{"Passi_Goal", "DurataAttività_Goal","Calorie_Goal"};
        Cursor c= db.query("Attività_Giornaliera", colums, null, null,null,null,null);
        c.moveToLast();
        goals.put("Passi_Goal", c.getInt(0));
        goals.put("DurataAttività_Goal", c.getInt(1));
        goals.put("Calorie_Goal", c.getInt(2));

        colums= new String[]{"Ore_di_sonno_Goal", "Minuti_di_sonno_Goal"};
        c= db.query("Ore_di_Sonno", colums, null, null,null,null,null);
        c.moveToLast();
        goals.put("Ore_di_sonno_Goal", c.getInt(0));
        goals.put("Minuti_di_sonno_Goal", c.getInt(1));

        colums= new String[]{"bicchieri_goal"};
        c= db.query("Bicchieri_Dacqua", colums, null, null,null,null,null);
        c.moveToLast();
        goals.put("bicchieri_goal", c.getInt(0));

        return goals;
    }
}
