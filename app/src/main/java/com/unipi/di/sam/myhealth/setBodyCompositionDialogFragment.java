package com.unipi.di.sam.myhealth;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class setBodyCompositionDialogFragment extends DialogFragment implements View.OnClickListener {
    private myViewModel myViewModel;
    private EditText weight_input, height_input;
    private Button save;

    public setBodyCompositionDialogFragment(com.unipi.di.sam.myhealth.myViewModel myViewModel) {
        this.myViewModel= myViewModel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_set_body_composition, container, false);
        getDialog().setTitle("Imposta Composizione Corporea");


        height_input= v.findViewById(R.id.edit_height);
        weight_input= v.findViewById(R.id.edit_weight);

        weight_input.setHint(String.valueOf(myViewModel.getBodyCompositionMutableLiveData().getValue().weight));
        height_input.setHint(String.valueOf(myViewModel.getBodyCompositionMutableLiveData().getValue().height));


        save= v.findViewById(R.id.save_BodyComposition);
        save.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        String new_weight, new_height;

        new_height= height_input.getText().toString().trim();
        new_weight= weight_input.getText().toString().trim();

        MainActivity.bodyComposition tmp= new MainActivity.bodyComposition();

        float height_inM= Float.parseFloat(new_height) /100;

        Log.d("BodyComposition", "Altezza: "+ height_inM+
                "\nPeso: "+new_weight);

        if(!new_weight.equals("") && !new_height.equals("")) {
            float bmi= (float) (Integer.parseInt(new_weight) / (height_inM*height_inM));

            myViewModel.setBodyCompositionMutableLiveData(new MainActivity.bodyComposition(Integer.parseInt(new_weight), Integer.parseInt(new_height), bmi));
        }
    }
}
