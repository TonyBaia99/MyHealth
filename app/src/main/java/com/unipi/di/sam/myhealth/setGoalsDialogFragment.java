package com.unipi.di.sam.myhealth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class setGoalsDialogFragment extends DialogFragment implements View.OnClickListener {
    private myViewModel myViewModel;
    private EditText stepsGoal_input, durationGoal_input, caloriesGoal_input, hoursOfSleepGoal_input, minutesOfSleepGoal_input, glassOfWaterGoal_input;
    private Button save;

    public setGoalsDialogFragment(com.unipi.di.sam.myhealth.myViewModel myViewModel) {
        this.myViewModel= myViewModel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.activity_imposta_obiettivi, container, false);
        getDialog().setTitle(getResources().getString(R.string.imposta_obiettivi));

        stepsGoal_input= v.findViewById(R.id.edit_steps);
        durationGoal_input= v.findViewById(R.id.edit_duration);
        caloriesGoal_input= v.findViewById(R.id.edit_cal);
        hoursOfSleepGoal_input= v.findViewById(R.id.edit_sleep_ore);
        minutesOfSleepGoal_input= v.findViewById(R.id.edit_sleep_min);
        glassOfWaterGoal_input= v.findViewById(R.id.edit_glasses);

        stepsGoal_input.setHint(String.valueOf(myViewModel.getAttività_giornalieraMutableLiveData().getValue().stepsGoal));
        durationGoal_input.setHint(String.valueOf(myViewModel.getAttività_giornalieraMutableLiveData().getValue().activityDurationGoal));
        caloriesGoal_input.setHint(String.valueOf(myViewModel.getAttività_giornalieraMutableLiveData().getValue().CaloriesGoal));
        hoursOfSleepGoal_input.setHint(String.valueOf(myViewModel.getSleepMutableLiveData().getValue().hoursOfSleepGoal));
        minutesOfSleepGoal_input.setHint(String.valueOf(myViewModel.getSleepMutableLiveData().getValue().minutesOfSleepGoal));
        glassOfWaterGoal_input.setHint(String.valueOf(myViewModel.getGlassesOfWaterMutableLiveData().getValue().glassesGoal));

        save= v.findViewById(R.id.save_goals);
        save.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        String new_stepsGoal, new_DurationGoal, newCaloriesGoal, new_hoursOfSleepGoal, new_minutesOfSleepGoal, new_glassOfWaterGoal;

        /* Sto recuperando le nuove stringhe scritte nei vari editText */
        new_stepsGoal= stepsGoal_input.getText().toString().trim();
        new_DurationGoal= durationGoal_input.getText().toString().trim();
        newCaloriesGoal= caloriesGoal_input.getText().toString().trim();
        new_hoursOfSleepGoal= hoursOfSleepGoal_input.getText().toString().trim();
        new_minutesOfSleepGoal= minutesOfSleepGoal_input.getText().toString().trim();
        new_glassOfWaterGoal= glassOfWaterGoal_input.getText().toString().trim();


        /*
        In questa serie di if controllo che effettivamente siano stati inseriti dei nuovi obiettivi
        negli EditText, così da evitare crush dell'app. Se un nuovo valore obiettivo è stato inserito
        notifico l'adapter in modo tale che la VIew relativa alla posizione indicata venga ridisegnata
        conseguenza.
         */
        if(!new_stepsGoal.equals("")) {
            MainActivity.Attività_Giornaliera tmp= myViewModel.getAttività_giornalieraMutableLiveData().getValue();
            MainActivity.stepCounter tmp1= myViewModel.getStepCounterMutableLiveData().getValue();

            myViewModel.setAttività_giornalieraMutableLiveData(new MainActivity.Attività_Giornaliera(tmp.currentSteps, Integer.parseInt(new_stepsGoal),
                    tmp.activityDuration, tmp.activityDurationGoal, tmp.currentCalories, tmp.CaloriesGoal));

            myViewModel.setStepCounterMutableLiveData(new MainActivity.stepCounter(tmp1.currentSteps, Integer.parseInt(new_stepsGoal)));
        }

        if(!new_DurationGoal.equals("")) {
            MainActivity.Attività_Giornaliera tmp= myViewModel.getAttività_giornalieraMutableLiveData().getValue();
            myViewModel.setAttività_giornalieraMutableLiveData(new MainActivity.Attività_Giornaliera(tmp.currentSteps, tmp.stepsGoal,
                    tmp.activityDuration, Integer.parseInt(new_DurationGoal), tmp.currentCalories, tmp.CaloriesGoal));
        }

        if(!newCaloriesGoal.equals("")) {
            MainActivity.Attività_Giornaliera tmp= myViewModel.getAttività_giornalieraMutableLiveData().getValue();
            myViewModel.setAttività_giornalieraMutableLiveData(new MainActivity.Attività_Giornaliera(tmp.currentSteps, tmp.stepsGoal,
                    tmp.activityDuration, tmp.activityDurationGoal, tmp.currentCalories, Integer.parseInt(newCaloriesGoal)));

            /*((MainActivity.Attività_Giornaliera) this.dataForAdapter.get(0)).CaloriesGoal= Integer.parseInt(newCaloriesGoal);
            this.home_rVadapter.notifyItemChanged(0);*/
        }

        if(!new_hoursOfSleepGoal.equals("") && !new_minutesOfSleepGoal.equals("")) {
            MainActivity.sleep tmp= myViewModel.getSleepMutableLiveData().getValue();
            myViewModel.setSleepMutableLiveData(new MainActivity.sleep(tmp.hoursOfSleep, Integer.parseInt(new_hoursOfSleepGoal),
                    tmp.minutesOfSleep, Integer.parseInt(new_minutesOfSleepGoal), tmp.startSleep, tmp.endSleep));
        }

        if(!new_glassOfWaterGoal.equals("")) {
            MainActivity.glassesOfWater tmp= myViewModel.getGlassesOfWaterMutableLiveData().getValue();
            myViewModel.setGlassesOfWaterMutableLiveData(new MainActivity.glassesOfWater(tmp.currentGlasses, Integer.parseInt(new_glassOfWaterGoal)));
        }
    }
}
