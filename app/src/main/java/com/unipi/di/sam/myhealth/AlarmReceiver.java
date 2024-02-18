package com.unipi.di.sam.myhealth;

import static com.unipi.di.sam.myhealth.MainActivity.NOTIFICATION_CHANNEL_ID;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class AlarmReceiver extends BroadcastReceiver {
    private myDataBase myDataBase;

    public AlarmReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        Notification notification= new Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.icona_launcher_round)
                .setContentText("Aggiornamento stato della tua attività benessere")
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setTimeoutAfter(1000)
                .build();
        NotificationManager nm= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(2, notification);

        new Thread(new Runnable() {
            @Override
            public void run() {
                myDataBase= new myDataBase(context);
                Log.d("Alarm giornaliero", "Allarme partito adesso cerco di resettare lo stato");

                String data=null;
                try {
                    data= generateNextDay();
                    setNewAlarm(context, data);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                int Passi_Goal, DurataAttività_Goal, Calorie_Goal, Ore_di_sonno_Goal, Minuti_di_sonno_Goal, bicchieri_goal;

                HashMap<String, Integer> goals= myDataBase.getLastTargets();
                Passi_Goal= goals.get("Passi_Goal");
                DurataAttività_Goal= goals.get("DurataAttività_Goal");
                Calorie_Goal= goals.get("Calorie_Goal");
                Ore_di_sonno_Goal= goals.get("Ore_di_sonno_Goal");
                Minuti_di_sonno_Goal= goals.get("Minuti_di_sonno_Goal");
                bicchieri_goal= goals.get("bicchieri_goal");

                myDataBase.add_attivitàGiornaliera(0, Passi_Goal,0,DurataAttività_Goal,0,Calorie_Goal, data);

                myDataBase.add_sleep(0,Ore_di_sonno_Goal, 0,Minuti_di_sonno_Goal,0,0, data);

                myDataBase.add_bicchieriDacqua(0,bicchieri_goal, data);

                myDataBase.add_ComposizioneCorpo(0, 0, 0, data);

                Log.d("Alarm giornaliero", "Stato dell'attività resettato per il giorno successivo");
            }
        }).start();
    }

    /**
     * Imposta un nuovo allarme per il giorno successivo al corrente, sempre all stessa ora.
     * @param context
     * @param data
     * @throws ParseException
     */
    private void setNewAlarm(Context context, String data) throws ParseException {
        Intent i= new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent= PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager= (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        SimpleDateFormat sdf= new SimpleDateFormat("yyyy/MM/dd");
        Calendar calendar= Calendar.getInstance();
        calendar.setTime(sdf.parse(data));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 50);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Log.d("Alarm Giornaliero", "Nuovo allarme impostato per le: "+ calendar.getTime());
    }

    /**
     * Metodo che recupera la data corrente e ne restituisce il giorno successivo
     * @return
     * @throws ParseException
     */
    private String generateNextDay() throws ParseException {
        LocalDateTime localDateTime= new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int year= localDateTime.getYear();
        int month= localDateTime.getMonthValue();
        int day= localDateTime.getDayOfMonth();
        String data= year+"/"+month+"/"+day;

        SimpleDateFormat sdf= new SimpleDateFormat("yyyy/MM/dd");
        Calendar c= Calendar.getInstance();
        c.setTime(sdf.parse(data));
        c.add(Calendar.DATE, 1);

        return sdf.format(c.getTime());
    }
}