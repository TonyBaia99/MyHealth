package com.unipi.di.sam.myhealth;

import android.util.Log;

public class UpdateDataBase implements Runnable {
    private myDataBase myDataBase;
    private myViewModel myViewModel;
    private String data;

    public UpdateDataBase(myDataBase myDataBase, myViewModel myViewModel, String data) {
        this.myDataBase= myDataBase;
        this.myViewModel= myViewModel;
        this.data= data;
    }

    @Override
    public void run() {
        MainActivity.Attività_Giornaliera attività_giornaliera= myViewModel.getAttività_giornalieraMutableLiveData().getValue();
        assert attività_giornaliera != null;
        myDataBase.update_AttivitàGiornaliera(attività_giornaliera, data);

        MainActivity.sleep sleep= myViewModel.getSleepMutableLiveData().getValue();
        assert sleep != null;
        myDataBase.update_sleep(sleep, data);

        MainActivity.glassesOfWater glassesOfWater= myViewModel.getGlassesOfWaterMutableLiveData().getValue();
        assert glassesOfWater != null;
        myDataBase.update_bicchieriDacqua(glassesOfWater, data);

        MainActivity.bodyComposition bodyComposition= myViewModel.getBodyCompositionMutableLiveData().getValue();
        assert bodyComposition != null;
        myDataBase.update_ComposizioneCorpo(bodyComposition, data);

        Log.d("UpdateDB", "Update del DB portato a termine");
    }
}
