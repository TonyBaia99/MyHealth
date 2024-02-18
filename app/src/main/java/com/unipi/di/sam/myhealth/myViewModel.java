package com.unipi.di.sam.myhealth;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class myViewModel extends ViewModel {
    private MutableLiveData<MainActivity.Attività_Giornaliera> attività_giornalieraMutableLiveData;
    public void setAttività_giornalieraMutableLiveData(MainActivity.Attività_Giornaliera attività_giornaliera) {
        if(attività_giornalieraMutableLiveData==null) {
            attività_giornalieraMutableLiveData= new MutableLiveData<>();
        }
        this.attività_giornalieraMutableLiveData.setValue(attività_giornaliera);
    }
    public MutableLiveData<MainActivity.Attività_Giornaliera> getAttività_giornalieraMutableLiveData() {
        if(attività_giornalieraMutableLiveData==null) {
            attività_giornalieraMutableLiveData= new MutableLiveData<>();
        }
        return attività_giornalieraMutableLiveData;
    }

    private MutableLiveData<MainActivity.stepCounter> stepCounterMutableLiveData;
    public void setStepCounterMutableLiveData(MainActivity.stepCounter stepCounter) {
        if(stepCounterMutableLiveData == null) {
            stepCounterMutableLiveData= new MutableLiveData<>();
        }
        this.stepCounterMutableLiveData.setValue(stepCounter);
    }
    public MutableLiveData<MainActivity.stepCounter> getStepCounterMutableLiveData() {
        if(stepCounterMutableLiveData ==null) {
            stepCounterMutableLiveData= new MutableLiveData<>();
        }
        return stepCounterMutableLiveData;
    }

    private MutableLiveData<MainActivity.sleep> sleepMutableLiveData;
    public void setSleepMutableLiveData(MainActivity.sleep sleep) {
        if(sleepMutableLiveData == null) {
            sleepMutableLiveData= new MutableLiveData<>();
        }
        this.sleepMutableLiveData.setValue(sleep);
    }
    public MutableLiveData<MainActivity.sleep> getSleepMutableLiveData() {
        if(sleepMutableLiveData == null) {
            sleepMutableLiveData= new MutableLiveData<>();
        }
        return sleepMutableLiveData;
    }

    private MutableLiveData<MainActivity.trainingHistory> trainingHistoryMutableLiveData;
    public void setTrainingHistoryMutableLiveData(MainActivity.trainingHistory trainingHistory) {
        if(trainingHistoryMutableLiveData == null) {
            trainingHistoryMutableLiveData= new MutableLiveData<>();
        }
        this.trainingHistoryMutableLiveData.setValue(trainingHistory);
    }
    public MutableLiveData<MainActivity.trainingHistory> getTrainingHistoryMutableLiveData() {
        if(trainingHistoryMutableLiveData == null) {
            trainingHistoryMutableLiveData= new MutableLiveData<>();
        }
        return trainingHistoryMutableLiveData;
    }

    private MutableLiveData<MainActivity.glassesOfWater> glassesOfWaterMutableLiveData;
    public void setGlassesOfWaterMutableLiveData(MainActivity.glassesOfWater glassesOfWater) {
        if(glassesOfWaterMutableLiveData == null) {
            glassesOfWaterMutableLiveData= new MutableLiveData<>();
        }
        this.glassesOfWaterMutableLiveData.setValue(glassesOfWater);
    }
    public MutableLiveData<MainActivity.glassesOfWater> getGlassesOfWaterMutableLiveData() {
        if(glassesOfWaterMutableLiveData == null) {
            glassesOfWaterMutableLiveData= new MutableLiveData<>();
        }
        return glassesOfWaterMutableLiveData;
    }

    private MutableLiveData<MainActivity.bodyComposition> bodyCompositionMutableLiveData;
    public void setBodyCompositionMutableLiveData(MainActivity.bodyComposition bodyComposition) {
        if(bodyCompositionMutableLiveData == null) {
            bodyCompositionMutableLiveData = new MutableLiveData<>();
        }
        this.bodyCompositionMutableLiveData.setValue(bodyComposition);
    }
    public MutableLiveData<MainActivity.bodyComposition> getBodyCompositionMutableLiveData() {
        if(bodyCompositionMutableLiveData == null) {
            bodyCompositionMutableLiveData = new MutableLiveData<>();
        }
        return bodyCompositionMutableLiveData;
    }
}
