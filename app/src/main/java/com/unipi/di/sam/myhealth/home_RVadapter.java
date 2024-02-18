package com.unipi.di.sam.myhealth;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class home_RVadapter extends RecyclerView.Adapter {
    /*
    In questa lista ci sono i dati con cui andremo a popolare le varie View.
    Il contenuto di questa lista è divisono nelle posizioni fissate:
        0-> il contenuto che riguarda le posizioni 0 (dailyActivity)  della recycerView
        1-> il contenuto che riguarda le posizioni 1 (stepsCounter) della recyclerView
        2-> il contenuto che riguarda la posizione 2 (sleep) della recyclerView
        3-> il contenuto che riguarda la posizione 3 (trainingHistory) della recyclerView
        4-> il contenuto che riguarda la posizione 4 (Glass of Water) della recyclerView
        5-> il contenuto che riguarda la posizione 5 (Body Composition) della recyclerView
     */
    private ArrayList<Object> dataForView;

    private final RecyclerViewInterface recyclerViewInterface;

    public home_RVadapter(ArrayList<Object> dataForView, RecyclerViewInterface recyclerViewInterface) {
        this.dataForView= dataForView;
        this.recyclerViewInterface= recyclerViewInterface;
    }

    /*
    ViewHolder che tiene dentro le info riguardanti le attività giornaliere
     */
    public static class dailyActivity_ViewHolder extends RecyclerView.ViewHolder {
        TextView steps, activity_time, calories;
        ProgressBar steps_pb, activityTime_pb, calories_pb;

        public dailyActivity_ViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            steps= itemView.findViewById(R.id.steps_text);
            activity_time= itemView.findViewById(R.id.activity_time_text);
            calories= itemView.findViewById(R.id.calories_text);

            steps_pb= itemView.findViewById(R.id.steps_pb);
            activityTime_pb= itemView.findViewById(R.id.activity_time_pb);
            calories_pb= itemView.findViewById(R.id.calories_pb);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recyclerViewInterface !=null) {
                        int position= getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    /*
    ViewHolder utilizzato per gestire la view in cui vengono contati solamente i passi.
     */
    public static class steps_ViewHolder extends RecyclerView.ViewHolder {
        TextView current_steps, stepsGoal, percentage_pb;
        ProgressBar steps_pb;

        public steps_ViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            current_steps= itemView.findViewById(R.id.currentSteps);
            stepsGoal= itemView.findViewById(R.id.goal);
            percentage_pb= itemView.findViewById(R.id.percentage_pbSteps);

            steps_pb= itemView.findViewById(R.id.steps_pb2);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recyclerViewInterface !=null) {
                        int position= getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    /*
    ViewHolder nella quale andiamo a contenere le info riguardanti il sonno.
     */
    public static class sleep_ViewHolder extends RecyclerView.ViewHolder {
        TextView hoursOfSleep, minutesOfSleep, startSleep, endSleep;

        public sleep_ViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            hoursOfSleep= itemView.findViewById(R.id.hours_of_sleep);
            minutesOfSleep= itemView.findViewById(R.id.minutes_of_sleep);
            startSleep= itemView.findViewById(R.id.start_sleep);
            endSleep= itemView.findViewById(R.id.end_sleep);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recyclerViewInterface !=null) {
                        int position= getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    /*
    ViewHolder per le historyTraining
     */
    public static class trainingHistory_ViewHolder extends RecyclerView.ViewHolder {
        View recap1, recap2;

        public trainingHistory_ViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            ViewStub tr1, tr2;
            tr1= itemView.findViewById(R.id.tr1);
            tr2= itemView.findViewById(R.id.tr2);

            recap1= tr1.inflate();
            recap2= tr2.inflate();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recyclerViewInterface !=null) {
                        int position= getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    /*
    ViewHolder per quando non ci sono allenamenti recenti da mostrare
     */
    public static class noTrainingHistory_ViewHolder extends RecyclerView.ViewHolder {

        public noTrainingHistory_ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /*
    ViewHolder nella quale andiamo ad inserire le info riguardati un allenamento in particolare
     */
    public static class trainingRecap_ViewHolder extends RecyclerView.ViewHolder {
        TextView typeOfTraining, trainingDuration, caloriesTr, distance, trainingTime;

        public trainingRecap_ViewHolder(@NonNull View itemView) {
            super(itemView);

            typeOfTraining= itemView.findViewById(R.id.type_of_training);
            trainingDuration= itemView.findViewById(R.id.training_duration);
            caloriesTr= itemView.findViewById(R.id.calories_tr);
            distance= itemView.findViewById(R.id.distance);
            trainingTime= itemView.findViewById(R.id.training_time);
        }
    }

    /*

     */
    public static class glassesOfWater_ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ProgressBar glasses_pb;
        TextView current_glasses, goal_glasses;
        FloatingActionButton plus, minus;
        MainActivity.glassesOfWater glasses;

        public glassesOfWater_ViewHolder(@NonNull View itemView, MainActivity.glassesOfWater glasses, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            this.glasses=glasses;

            glasses_pb= itemView.findViewById(R.id.glasses_of_water_pb);
            current_glasses= itemView.findViewById(R.id.current_glasses);
            goal_glasses= itemView.findViewById(R.id.glasses_goal);
            plus= itemView.findViewById(R.id.button_plus);
            plus.setOnClickListener(this);
            minus= itemView.findViewById(R.id.button_minus);
            minus.setOnClickListener(this);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recyclerViewInterface !=null) {
                        int position= getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_plus: {
                    if(glasses.currentGlasses>=0) {
                        minus.setEnabled(true);
                    }
                    glasses.currentGlasses++;
                    Log.d("Glass_of_water +1", String.valueOf(glasses.currentGlasses));
                    current_glasses.setText(String.valueOf(glasses.currentGlasses));
                    glasses_pb.setProgress(glasses.currentGlasses);
                } break;

                case R.id.button_minus: {
                    if(glasses.currentGlasses==0) {
                        minus.setEnabled(false);
                    }
                    else {
                        glasses.currentGlasses--;
                        Log.d("Glass_of_water -1", String.valueOf(glasses.currentGlasses));
                        current_glasses.setText(String.valueOf(glasses.currentGlasses));
                        glasses_pb.setProgress(glasses.currentGlasses);
                    }

                } break;
            }
        }
    }

    /*
    ViewHolder che manitiene le informazioni riguardati la composizione corporea.
     */
    public static class bodyComposition_ViewHolder extends RecyclerView.ViewHolder {
        TextView weight, bmi, height;

        public bodyComposition_ViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            weight= itemView.findViewById(R.id.weight);
            bmi= itemView.findViewById(R.id.bmi_text);
            height= itemView.findViewById(R.id.height);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recyclerViewInterface !=null) {
                        int position= getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater= LayoutInflater.from(parent.getContext());
        View view;

        switch (viewType){
            case 0: {
                view= inflater.inflate(R.layout.daily_activity, parent, false);
                return new dailyActivity_ViewHolder(view, recyclerViewInterface);
            }

            case 1: {
                view= inflater.inflate(R.layout.steps, parent, false);
                return new steps_ViewHolder(view, recyclerViewInterface);
            }

            case 2: {
                view= inflater.inflate(R.layout.sleep, parent, false);
                return new sleep_ViewHolder(view, recyclerViewInterface);
            }

            case 3: {
                /*
                In questo caso è un codice provvisorio per provare ad utilizzare tutti e due i layout
                cioè quello quando ci sono degli allenamenti recenti e quello quando non
                ce ne sono.
                Questo controllo andrà sistemato.
                 */
                if(dataForView.get(3)!=null){
                    view= inflater.inflate(R.layout.training_history, parent, false);
                    return new trainingHistory_ViewHolder(view, recyclerViewInterface);
                }
                else{
                    view= inflater.inflate(R.layout.no_training_history, parent, false);
                    return new noTrainingHistory_ViewHolder(view);
                }

            }

            case 4: {
                view= inflater.inflate(R.layout.glass_of_water, parent, false);
                return new glassesOfWater_ViewHolder(view, (MainActivity.glassesOfWater) dataForView.get(4), recyclerViewInterface);
            }

            case 5: {
                view= inflater.inflate(R.layout.body_composition, parent, false);
                return new bodyComposition_ViewHolder(view, recyclerViewInterface);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //In questo momento non modifico i vari dati nelle view quindi per ora non faccio nulla
        /*
        All'interno di questo metodo molto probabilemente metterò uno switch che in base alla
        posizione opera sulla rispettiva view con i dati raccolta da ancora non so dove.
         */
        switch (position) {
            case 0: {
                dailyActivity_ViewHolder dailyActivity_viewHolder= (dailyActivity_ViewHolder) holder;

                MainActivity.Attività_Giornaliera attività_giornaliera= (MainActivity.Attività_Giornaliera) this.dataForView.get(0);
                dailyActivity_viewHolder.steps.setText(String.valueOf(attività_giornaliera.currentSteps));
                dailyActivity_viewHolder.activity_time.setText(String.valueOf(attività_giornaliera.activityDuration));
                dailyActivity_viewHolder.calories.setText(String.valueOf(attività_giornaliera.currentCalories));

                dailyActivity_viewHolder.steps_pb.setMax(attività_giornaliera.stepsGoal);
                dailyActivity_viewHolder.steps_pb.setProgress(attività_giornaliera.currentSteps);

                dailyActivity_viewHolder.activityTime_pb.setMax(attività_giornaliera.activityDurationGoal);
                dailyActivity_viewHolder.activityTime_pb.setProgress(attività_giornaliera.activityDuration);

                dailyActivity_viewHolder.calories_pb.setMax(attività_giornaliera.CaloriesGoal);
                dailyActivity_viewHolder.calories_pb.setProgress(attività_giornaliera.currentCalories);
            } break;

            case 1: {
                steps_ViewHolder steps_viewHolder= (steps_ViewHolder) holder;
                MainActivity.stepCounter stepCounter= (MainActivity.stepCounter) this.dataForView.get(1);

                steps_viewHolder.current_steps.setText(String.valueOf(stepCounter.currentSteps));
                steps_viewHolder.stepsGoal.setText(String.valueOf(stepCounter.stepsGoal));
                String currentPercentage= ((stepCounter.currentSteps*100)/ stepCounter.stepsGoal)+"%";
                steps_viewHolder.percentage_pb.setText(currentPercentage);
                steps_viewHolder.steps_pb.setProgress((stepCounter.currentSteps*100)/ stepCounter.stepsGoal);
            } break;

            case 2: {
                sleep_ViewHolder sleep_viewHolder= (sleep_ViewHolder) holder;
                MainActivity.sleep sleep= (MainActivity.sleep) this.dataForView.get(2);

                sleep_viewHolder.hoursOfSleep.setText(String.valueOf(sleep.hoursOfSleep));
                sleep_viewHolder.minutesOfSleep.setText(String.valueOf(sleep.minutesOfSleep));
                sleep_viewHolder.startSleep.setText(String.valueOf(sleep.startSleep));
                sleep_viewHolder.endSleep.setText(String.valueOf(sleep.endSleep));
            } break;

            case 3: {
                if(dataForView.get(3)!=null){
                    trainingHistory_ViewHolder trainingHistory_viewHolder= (trainingHistory_ViewHolder) holder;
                    MainActivity.trainingHistory trainingHistory= (MainActivity.trainingHistory) this.dataForView.get(3);

                    TextView typeOfTraining, trainingDuration, caloriesTr, distance, trainingTime;

                    typeOfTraining= trainingHistory_viewHolder.recap1.findViewById(R.id.type_of_training);
                    trainingDuration= trainingHistory_viewHolder.recap1.findViewById(R.id.training_duration);
                    caloriesTr= trainingHistory_viewHolder.recap1.findViewById(R.id.calories_tr);
                    distance= trainingHistory_viewHolder.recap1.findViewById(R.id.distance);
                    trainingTime= trainingHistory_viewHolder.recap1.findViewById(R.id.training_time);

                    MainActivity.trainingRecap trainingRecap= trainingHistory.trainingRecaplist.get(0);
                    typeOfTraining.setText(trainingRecap.typeOfTraining);
                    trainingDuration.setText(String.valueOf(trainingRecap.trainingDuration));
                    caloriesTr.setText(String.valueOf(trainingRecap.calories));
                    distance.setText(String.valueOf(trainingRecap.distace / 1000));
                    trainingTime.setText(formatTime(trainingRecap.trainingTime));
                    //******************************************************************************

                    TextView typeOfTraining2, trainingDuration2, caloriesTr2, distance2, trainingTime2;

                    typeOfTraining2= trainingHistory_viewHolder.recap2.findViewById(R.id.type_of_training);
                    trainingDuration2= trainingHistory_viewHolder.recap2.findViewById(R.id.training_duration);
                    caloriesTr2= trainingHistory_viewHolder.recap2.findViewById(R.id.calories_tr);
                    distance2= trainingHistory_viewHolder.recap2.findViewById(R.id.distance);
                    trainingTime2= trainingHistory_viewHolder.recap2.findViewById(R.id.training_time);

                    MainActivity.trainingRecap trainingRecap2= trainingHistory.trainingRecaplist.get(1);
                    typeOfTraining2.setText(trainingRecap2.typeOfTraining);
                    trainingDuration2.setText(String.valueOf(trainingRecap2.trainingDuration));
                    caloriesTr2.setText(String.valueOf(trainingRecap2.calories));
                    distance2.setText(String.valueOf(trainingRecap2.distace/1000));
                    trainingTime2.setText(formatTime(trainingRecap2.trainingTime));
                }
            } break;

            case 4: {
                glassesOfWater_ViewHolder glassesOfWater_viewHolder= (glassesOfWater_ViewHolder) holder;
                MainActivity.glassesOfWater glassesOfWater= (MainActivity.glassesOfWater) this.dataForView.get(4);

                glassesOfWater_viewHolder.current_glasses.setText(String.valueOf(glassesOfWater.currentGlasses));
                glassesOfWater_viewHolder.goal_glasses.setText(String.valueOf(glassesOfWater.glassesGoal));

                glassesOfWater_viewHolder.glasses_pb.setProgress(glassesOfWater.currentGlasses);
                glassesOfWater_viewHolder.glasses_pb.setMax(glassesOfWater.glassesGoal);

                if(Integer.parseInt((String) glassesOfWater_viewHolder.current_glasses.getText())==0) {
                    glassesOfWater_viewHolder.minus.setEnabled(false);
                }
                else glassesOfWater_viewHolder.minus.setEnabled(true);
            } break;

            case 5: {
                bodyComposition_ViewHolder bodyComposition_viewHolder= (bodyComposition_ViewHolder) holder;
                MainActivity.bodyComposition bodyComposition= (MainActivity.bodyComposition) this.dataForView.get(5);

                bodyComposition_viewHolder.height.setText(String.valueOf(bodyComposition.height));
                bodyComposition_viewHolder.weight.setText(String.valueOf(bodyComposition.weight));
                bodyComposition_viewHolder.bmi.setText(String.valueOf(bodyComposition.bmi));
            } break;
        }
    }

    @NonNull
    private String formatTime(@NonNull Date trainingTime) {
        String time= trainingTime.toString();
        String[] time1 = time.split(" ");
        StringBuilder realTime= new StringBuilder();
        realTime.append(time1[3].split(":")[0])
                .append(":").append(time1[3].split(":")[1])
                .append(" ")
                .append(time1[5])
                .append("/")
                .append(time1[1])
                .append("/")
                .append(time1[2]);
        //Log.d("Data allenamento", realTime.toString());
        return realTime.toString();
    }

    @Override
    public int getItemCount() {
        //La lista ha dimensione fissa a 6 elementi e non cambia.
        /*
        Valutare se ho tempo per dare anche la possibilità di decidere quali elementi della
        lista mostrare e quali no.
         */
        return 6;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /*
            Nella RecyclerView gestita da questo adapter, l'ordine delle View è sempre lo stesso.
             */
    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0: {
                //In prima posizione c'è la VIew descritta dal file di layout daily_activity.xml
                return 0;
            }

            case 1: {
                //In seconda posizione c'è la VIew descritta dal file di layout steps.xml
                return 1;
            }

            case 2: {
                //In terza posizione c'è la VIew descritta dal file di layout sleep.xml
                return 2;
            }

            case 3: {
                /*
                In quarta posizione c'è la VIew descritta dal file di layout training_history.xml
                oppure da no_training_history.xml, in base al fatto che ci siano allenamenti
                recenti oppure no.
                 */
                return 3;
            }

            case 4: {
                //In quita posizione c'è la VIew descritta dal file di layout glasses_of_water.xml
                return 4;
            }

            case 5: {
                //In quita posizione c'è la VIew descritta dal file di layout body_composition.xml
                return 5;
            }

            default:
                throw new IllegalStateException("Unexpected value: " + position);
        }
    }
}
