package com.unipi.di.sam.myhealth;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements RecyclerViewInterface{

    private ArrayList<Object> dataForAdapter;
    private home_RVadapter home_rVadapter;
    private myViewModel myViewModel;


    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(Bundle bundle) {
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        initializeDataForAdapter();

        myViewModel= new ViewModelProvider(requireActivity()).get(com.unipi.di.sam.myhealth.myViewModel.class);
        setMyViewModel();

        home_rVadapter= new home_RVadapter(dataForAdapter, this);
        home_rVadapter.setHasStableIds(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView rv= view.findViewById(R.id.myHomeRecyclerView);
        rv.setHasFixedSize(true);

        rv.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rv.setAdapter(home_rVadapter);
        rv.getItemAnimator().setChangeDuration(0);

        return view;
    }

    @Override
    public void onItemClick(int position) {
        /*
        GENERARE LO SCHELETRO DELLE VARIE ACTIVITY CREATE A PARTIRE DAL CLICK CON LE SINGOLE VIEW A SCHERMO
         */
        switch (position) {
            case 0: {
                //In questo ramo gestico l'activity che viene creata quando si clicca sulla View in posizione 0
                //che è sempre la stessa ed è dailyActivityViewHolder

                Intent i= new Intent(getContext(), DailyActivity_Activity.class);
                startActivity(i);
            } break;

            case 1: {
                /**
                 * Decidere dopo se aggiungere o meno
                 */
            } break;

            case 2: {
                Intent i= new Intent(getContext(), sleep_Activity.class);
                startActivity(i);
            } break;

            case 3: {
                Intent i= new Intent(getContext(), TrainingHistory_Activity.class);
                startActivity(i);
            } break;

            case 4: {
                /**
                 * Decidere dopo se aggiungere o meno qualcosa riguardo ai bicchieri d'acqua
                 */
            } break;

            case 5: {
                FragmentManager fm= getChildFragmentManager();
                setBodyCompositionDialogFragment setBodyCompositionDialogFragment= new setBodyCompositionDialogFragment(this.myViewModel);
                setBodyCompositionDialogFragment.show(fm, "setBodyCompositionDialogFragment");
            } break;
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.imposta_obiettivi: {
                FragmentManager fm= getParentFragmentManager();
                setGoalsDialogFragment setGoalsDialogFragment = new setGoalsDialogFragment(this.myViewModel);
                setGoalsDialogFragment.show(fm,"setGoalsDialogFragment");
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Metodo utilizzato per inizializzare i dati prensenti nell'arrayList in questione.
     * ArrayList utilizzato dalla RecyclerView per mostrare i dati a schermo.
     */
    private void initializeDataForAdapter() {
        dataForAdapter= new ArrayList<>();
        dataForAdapter.add(0, new MainActivity.Attività_Giornaliera(0,10000,0,30,0,500));

        dataForAdapter.add(1, new MainActivity.stepCounter(0,10000));

        dataForAdapter.add(2,new MainActivity.sleep());

        ArrayList<MainActivity.trainingRecap> list= new ArrayList<>();
        list.add(0,new MainActivity.trainingRecap());
        list.add(1,new MainActivity.trainingRecap());
        dataForAdapter.add(3,new MainActivity.trainingHistory(list));

        dataForAdapter.add(4,new MainActivity.glassesOfWater());

        dataForAdapter.add(5,new MainActivity.bodyComposition());
    }

    /**
     * In questo metodo impostiamo gli observer sui vari elementi del ViewModel.
     * Ogni volta che uno dei valori nel ViewModel cambia, allara il corrispettivo
     * observer viene chiamato e viene eseguito il codice al suo interno, inserendo i nuovi dati
     * nell'ArrayList usato dalla RecyclerView e notificando il cambiamento di dati cosi
     * che possa ridisegnarsi di conseguenza.
     */
    private void setMyViewModel() {
        myViewModel.getAttività_giornalieraMutableLiveData().observe(this, item -> {
            this.dataForAdapter.set(0, item);
            this.home_rVadapter.notifyItemChanged(0);
        });

        myViewModel.getStepCounterMutableLiveData().observe(this, item -> {
            this.dataForAdapter.set(1, item);
            this.home_rVadapter.notifyItemChanged(1);
        });

        myViewModel.getSleepMutableLiveData().observe(this, item -> {
            this.dataForAdapter.set(2, item);
            this.home_rVadapter.notifyItemChanged(2);
        });

        myViewModel.getTrainingHistoryMutableLiveData().observe(this, item -> {
            this.dataForAdapter.set(3, item);
            this.home_rVadapter.notifyItemChanged(3);
        });

        myViewModel.getGlassesOfWaterMutableLiveData().observe(this, item -> {
            this.dataForAdapter.set(4, item);
            this.home_rVadapter.notifyItemChanged(4);
        });

        myViewModel.getBodyCompositionMutableLiveData().observe(this, item -> {
            this.dataForAdapter.set(5, item);
            this.home_rVadapter.notifyItemChanged(5);
        });
    }
}