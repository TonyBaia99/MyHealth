package com.unipi.di.sam.myhealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DailyActivity_Activity extends AppCompatActivity {
    public MyCustomBarChart myCustomBarChart;
    public RadioGroup radioButton;
    private String data;

    private myDataBase myDataBase;

    private ArrayList<MainActivity.Attività_Giornaliera> dataSet;
    private ArrayList<String> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_activity);

        myDataBase = new myDataBase(getApplicationContext());

        myCustomBarChart = findViewById(R.id.myCustomBarChart);
        radioButton= findViewById(R.id.radioButton);

        dataSet = new ArrayList<>();
        dataList = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                MainActivity.Attività_Giornaliera tmp = null;
                SQLiteDatabase db = myDataBase.getReadableDatabase();

                String[] colums = new String[]{"Passi", "Passi_Goal", "Durata_Attività", "DurataAttività_Goal", "Calorie", "Calorie_Goal", "Data"};
                Cursor c = db.query("Attività_Giornaliera", colums, null, null, null, null, null);
                c.moveToLast();
                for (int i = 0; i < 7; i++) {
                    String newData = null;
                    try {
                        newData = generatePreviousDay(i);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    dataList.add(i, newData);

                    tmp = new MainActivity.Attività_Giornaliera(c.getInt(0), c.getInt(1), c.getInt(2), c.getInt(3), c.getInt(4), c.getInt(5));
                    Log.d("CustomActivity", tmp.toString());
                    dataSet.add(i, tmp);
                    Log.d("CustomActivity", dataSet.get(i).toString());
                    c.moveToPrevious();
                }
                c.close();
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            File file= saveImage();
            if(file!=null) {
                shareFile(file);
            }
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Metodo che recuperare la data corrente, e ritorna una stringa che rapprensenta la data di
     * "i" giorni prima.
     * @param i
     * @return
     * @throws ParseException
     */
    private  String generatePreviousDay(int i) throws ParseException {
        LocalDateTime localDateTime= new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int year= localDateTime.getYear();
        int month= localDateTime.getMonthValue();
        int day= localDateTime.getDayOfMonth();
        data= year+"/"+month+"/"+day;

        SimpleDateFormat sdf= new SimpleDateFormat("yyyy/MM/dd");
        Calendar c= Calendar.getInstance();
        c.setTime(sdf.parse(data));
        c.add(Calendar.DATE, -i);

        return sdf.format(c.getTime());
    }

    /**
     * Metodo nella quale in base al button premuto impostiamo i giusto valori dal vedere nella
     * ViewCustom.
     * @param view
     */
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.stepsButton: {
                if(checked) {
                    myCustomBarChart.setMaxValY(dataSet.get(0).stepsGoal);

                    ArrayList<Integer> list= new ArrayList<>();
                    for(int i=0; i<7; i++){
                        list.add(i, dataSet.get(i).currentSteps);
                    }
                    myCustomBarChart.setDataSet(list, dataList);

                    myCustomBarChart.setTypeDataSet("steps");
                }
            } break;

            case R.id.activityDurationButton: {
                if(checked) {
                    myCustomBarChart.setMaxValY(dataSet.get(0).activityDurationGoal);

                    ArrayList<Integer> list= new ArrayList<>();
                    for(int i=0; i<7; i++){
                        list.add(i, dataSet.get(i).activityDuration);
                    }
                    myCustomBarChart.setDataSet(list, dataList);

                    myCustomBarChart.setTypeDataSet("activityDuration");
                }
            } break;

            case  R.id.caloriesButton: {
                if(checked) {
                    myCustomBarChart.setMaxValY(dataSet.get(0).CaloriesGoal);

                    ArrayList<Integer> list= new ArrayList<>();
                    for(int i=0; i<7; i++){
                        list.add(i, dataSet.get(i).currentCalories);
                    }
                    myCustomBarChart.setDataSet(list, dataList);

                    myCustomBarChart.setTypeDataSet("calories");
                }
            } break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi= getMenuInflater();
        mi.inflate(R.menu.daily_activity_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.shareButton: {
                File file= saveImage();
                if(file!=null) {
                    shareFile(file);
                }
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Metodo che tramite un intent mi permette di condiviere un file.
     * @param file
     */
    private void shareFile(File file) {
        Uri uri;

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            uri= FileProvider.getUriForFile(this, getPackageName()+".provider", file);
        }
        else {
            uri= Uri.fromFile(file);
        }

        Intent i= new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.setType("image/*");
        i.putExtra(Intent.EXTRA_SUBJECT, "Screenshot");
        i.putExtra(Intent.EXTRA_STREAM, uri);

        startActivity(Intent.createChooser(i, "Share files"));
    }

    /**
     *
     * @return
     */
    private File saveImage() {
        if(!checkPermission()) {
            return null;
        }

        try {
            String path= null;
            path = Environment.getExternalStorageDirectory().toString() +"/DCIM/Screenshots";
            File fileDir= new File(path);
            Log.d("ScreenShot", fileDir.toString());
            if(!fileDir.exists()){
                boolean result= fileDir.mkdir();
                Log.d("ScreenShot", String.valueOf(result));
            }

            String mPath= path+ "/ScreenShot_"+new Date().getTime()+".jpeg";
            Bitmap bitmap= screenShot();

            File file= new File(mPath);
            Log.d("ScreenShot", file.toString());
            FileOutputStream Fout= new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, Fout);
            Fout.flush();
            Fout.close();

            return file;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Metodo che fa lo screenShot di una particolare View indicata dall'id.
     * @return
     */
    private Bitmap screenShot() {
        View v= findViewById(R.id.myRelativeLayout);
        Bitmap bitmap= Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas= new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

    /**
     * Metodo utilizzato per controllare di avere i permessi necessari per accedere alla memoria e scrivere in essa.
     * Permessi necessari per attivare la funzionalità di condivisione dei dati mostrati dalla ViewCustom.
     * @return
     */
    private boolean checkPermission() {
        int permission= ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            return false;
        }

        return true;
    }
}