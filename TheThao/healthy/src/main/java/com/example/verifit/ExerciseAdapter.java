package com.example.verifit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;


public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.MyViewHolder> implements Filterable {

    Context ct;
    ArrayList<Exercise> Exercises;
    ArrayList<Exercise> Exercises_Full;

    public ExerciseAdapter(Context ct, ArrayList<Exercise> Exercises)
    {
        this.ct = ct;
        this.Exercises = new ArrayList<>(Exercises);
        this.Exercises_Full = new ArrayList<>(Exercises);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(this.ct);
        View view = inflater.inflate(R.layout.exercise_row,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        holder.tv_exercise_name.setText(Exercises.get(position).getName());
        holder.tv_exercise_bodypart.setText(Exercises.get(position).getBodyPart());

        holder.cardview_exercise_1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent in = new Intent(ct,AddExerciseActivity.class);
                in.putExtra("exercise",holder.tv_exercise_name.getText().toString());
                ct.startActivity(in);
            }
        });



    }


    public void deleteExercise(String exercise_name)
    {
        for (Iterator<Exercise> exerciseIterator = this.Exercises.iterator(); exerciseIterator.hasNext(); )
        {
            Exercise current_exercise = exerciseIterator.next();

            if(current_exercise.getName().equals(exercise_name))
            {
                exerciseIterator.remove();
                notifyDataSetChanged();
            }
        }
    }


    @Override
    public int getItemCount()
    {
        return this.Exercises.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence)
        {
            ArrayList<Exercise> Exercises_Filtered = new ArrayList<Exercise>();

            if(charSequence == null || charSequence.length() == 0)
            {
                Exercises_Filtered.addAll(Exercises_Full);
            }
            else
            {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for(int i = 0; i < Exercises.size(); i++)
                {
                    if(Exercises.get(i).getName().toLowerCase().contains(filterPattern))
                    {
                        Exercises_Filtered.add(Exercises.get(i));
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = Exercises_Filtered;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults)
        {
            Exercises.clear();
            Exercises.addAll((ArrayList)filterResults.values);
            notifyDataSetChanged();
        }
    };

    public class MyViewHolder extends  RecyclerView.ViewHolder implements View.OnLongClickListener, PopupMenu.OnMenuItemClickListener, AdapterView.OnItemSelectedListener {
        TextView tv_exercise_name;
        TextView tv_exercise_bodypart;
        CardView cardview_exercise_1;
        String exercise_name;
        String new_exercise_category;
        int current_exercise_category_position;

        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            tv_exercise_name = itemView.findViewById(R.id.tv_date);
            tv_exercise_bodypart = itemView.findViewById(R.id.exercise_bodypart);
            cardview_exercise_1 = itemView.findViewById(R.id.cardview_exercise_1);

            cardview_exercise_1.setOnLongClickListener(this);
        }


        @Override
        public boolean onLongClick(View view)
        {
            showPopupMenu(view);
            return true;
        }


        private void showPopupMenu(View view)
        {
            PopupMenu popupMenu = new PopupMenu(view.getContext(),view);
            popupMenu.inflate(R.menu.exercises_activity_floating_context_menu);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.show();
        }


        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            if(item.getItemId() == R.id.edit)
            {
                int position = getAdapterPosition();
                System.out.println("Edit");

                LayoutInflater inflater = LayoutInflater.from(ct);
                View view = inflater.inflate(R.layout.edit_exercise_dialog,null);
                AlertDialog alertDialog = new AlertDialog.Builder(ct).setView(view).create();

                Button bt_save = view.findViewById(R.id.bt_save);
                Button bt_cancel = view.findViewById(R.id.bt_cancel);
                EditText et_exercise_name = view.findViewById(R.id.et_exercise_name);
                Spinner spinner = view.findViewById(R.id.spinner);


                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ct,R.array.Categories, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(this);


                String[] listValue;
                listValue = ct.getResources().getStringArray(R.array.Categories);

                // Find Current Category position
                for(int i = 0; i < listValue.length; i++)
                {
                    if(listValue[i].equals(Exercises.get(position).getBodyPart()))
                    {
                        current_exercise_category_position = i;
                        System.out.println(listValue[i]);
                    }
                }


                exercise_name = Exercises.get(position).getName();
                et_exercise_name.setText(exercise_name);
                spinner.setSelection(current_exercise_category_position);


                bt_cancel.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        alertDialog.dismiss();
                    }
                });


                bt_save.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {

                        String new_exercise_name = et_exercise_name.getText().toString();


                        if(new_exercise_category != null && !new_exercise_category.isEmpty() && new_exercise_category.length() > 0 && new_exercise_name != null && !new_exercise_name.isEmpty() && new_exercise_name.length() > 0)
                        {
                            System.out.println(new_exercise_category + " " + et_exercise_name.getText().toString());
                            MainActivity.editExercise(exercise_name,new_exercise_name,new_exercise_category);
                            MainActivity.saveWorkoutData(ct);
                            MainActivity.saveKnownExerciseData(ct);
                            notifyDataSetChanged();
                            alertDialog.dismiss();
                        }

                        // Tell user to stop fucking around
                        else
                        {
                            Toast.makeText(ct,"Please choose an apropriate name", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                alertDialog.show();
                return true;
            }


            else if(item.getItemId() == R.id.delete)
            {
                int position = getAdapterPosition();

                // Prepare to show exercise dialog box
                LayoutInflater inflater = LayoutInflater.from(ct);
                View view = inflater.inflate(R.layout.delete_exercise_dialog,null);
                AlertDialog alertDialog = new AlertDialog.Builder(ct).setView(view).create();


                Button bt_yes = view.findViewById(R.id.bt_yes3);
                Button bt_no = view.findViewById(R.id.bt_no3);


                bt_no.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        alertDialog.dismiss();
                    }
                });

                bt_yes.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        // Delete Exercise from MainActivity Data Structures
                        MainActivity.deleteExercise(Exercises.get(position).getName());

                        // Delete Exercise from Adapter's local data structure
                        deleteExercise(Exercises.get(position).getName());

                        // Save Results
                        MainActivity.saveKnownExerciseData(ct);
                        MainActivity.saveWorkoutData(ct);

                        alertDialog.dismiss();
                    }
                });


                alertDialog.show();
                return true;
            }


            return false;
        }

        // Spinner On Item Selected Methods
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
        {
            new_exercise_category = adapterView.getItemAtPosition(i).toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView)
        {
            new_exercise_category = "";
        }
    }
}
