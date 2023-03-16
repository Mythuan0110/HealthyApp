package com.example.verifit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;



public class DiaryExerciseAdapter extends RecyclerView.Adapter<DiaryExerciseAdapter.MyViewHolder> {

    Context ct;
    ArrayList<WorkoutExercise> Exercises;
    Button bt_save_comment;
    Button bt_clear_comment;
    EditText et_exercise_comment;
    String exercise_name;


    public DiaryExerciseAdapter(Context ct, ArrayList<WorkoutExercise> Exercises)
    {
        this.ct = ct;
        this.Exercises = new ArrayList<>(Exercises);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(this.ct);
        View view = inflater.inflate(R.layout.diary_exercise_row,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        String exercise_name = Exercises.get(position).getExercise();

        holder.tv_exercise_name.setText(exercise_name);
        int sets = (int)Math.round(Exercises.get(position).getTotalSets());
        holder.sets.setText(String.valueOf(sets));

        setCategoryIconTint(holder, exercise_name);


        initializeCommentButton(holder,position);


        holder.comment_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCommentDialog(position);
            }
        });


       


        holder.cardView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showExerciseDialog(position);
            }
        });
    }

    public void initializeCommentButton(MyViewHolder holder, int position)
    {
        String Comment = Exercises.get(position).getComment();


        if (Comment == null)
        {
            holder.comment_button.setVisibility(View.GONE);
        }
        else if(Comment.equals(""))
        {
            holder.comment_button.setVisibility(View.GONE);
        }
        else if(Comment.isEmpty())
        {
            holder.comment_button.setVisibility(View.GONE);
        }
        else
        {
            holder.comment_button.setVisibility(View.VISIBLE);
        }


    }

    public void showVolumePRDialog(ArrayList<String> Records)
    {
        LayoutInflater inflater = LayoutInflater.from(ct);
        View view = inflater.inflate(R.layout.personal_record_dialog,null);
        AlertDialog alertDialog = new AlertDialog.Builder(ct).setView(view).create();

        TextView tv_record = view.findViewById(R.id.tv_record);

        if(Records.size() > 1)
        {
            tv_record.setText("Multiple Records");
        }
        else
        {
            tv_record.setText("Record");
        }

        RecyclerView recyclerViewPR = view.findViewById(R.id.recyclerViewPR);
        StringAdapter stringAdapter = new StringAdapter(ct,Records);
        recyclerViewPR.setAdapter(stringAdapter);
        recyclerViewPR.setLayoutManager(new LinearLayoutManager(ct));


        alertDialog.show();
    }


    public void showCommentDialog(int position)
    {
        LayoutInflater inflater = LayoutInflater.from(ct);
        View view = inflater.inflate(R.layout.show_exercise_comment_dialog,null);
        AlertDialog alertDialog = new AlertDialog.Builder(ct).setView(view).create();

        System.out.println("Debug 1");
        TextView tv_exercise_comment = view.findViewById(R.id.tv_exercise_comment);

        System.out.println("Debug 2");

        String Comment = Exercises.get(position).getComment();


        tv_exercise_comment.setText(Exercises.get(position).getComment());

        System.out.println("Debug 3");

        alertDialog.show();
    }

    public void setCategoryIconTint(MyViewHolder holder, String exercise_name)
    {
        String exercise_category = MainActivity.getExerciseCategory(exercise_name);

        if(exercise_category.equals("Shoulders"))
        {
            holder.imageView.setColorFilter(Color.argb(255, 	0, 116, 189)); // Primary Color
        }
        else if(exercise_category.equals("Back"))
        {
            holder.imageView.setColorFilter(Color.argb(255, 40, 176, 192));
        }
        else if(exercise_category.equals("Chest"))
        {
            holder.imageView.setColorFilter(Color.argb(255, 	92, 88, 157));
        }
        else if(exercise_category.equals("Biceps"))
        {
            holder.imageView.setColorFilter(Color.argb(255, 	255, 50, 50));
        }
        else if(exercise_category.equals("Glutes"))
        {
            holder.imageView.setColorFilter(Color.argb(255,    204, 154, 0));
        }
        else if(exercise_category.equals("Legs"))
        {
            holder.imageView.setColorFilter(Color.argb(255, 	212, 	25, 97));
        }
        else if(exercise_category.equals("Abs"))
        {
            holder.imageView.setColorFilter(Color.argb(255, 	255, 153, 171));
        }
        else
        {
            holder.imageView.setColorFilter(Color.argb(255, 	52, 58, 64)); // Grey AF
        }
    }

    public void showExerciseDialog(int position) {

        LayoutInflater inflater = LayoutInflater.from(ct);
        View view = inflater.inflate(R.layout.exercise_dialog,null);
        AlertDialog alertDialog = new AlertDialog.Builder(ct).setView(view).create();

        TextView totalsets = view.findViewById(R.id.volume);
        TextView totalreps = view.findViewById(R.id.totalreps);
        TextView name = view.findViewById(R.id.tv_date);
        Button bt_close = view.findViewById(R.id.bt_close);
        Button bt_edit_exercise = view.findViewById(R.id.bt_edit_exercise);


        int sets = (int)Math.round(Exercises.get(position).getTotalSets());
        int reps = (int)Math.round(Exercises.get(position).getTotalReps());
        int max_reps = (int)Math.round(Exercises.get(position).getMaxReps());

        totalsets.setText(String.valueOf(sets));
        totalreps.setText(String.valueOf(reps));

        name.setText(Exercises.get(position).getExercise());

        exercise_name = Exercises.get(position).getExercise();

        bt_edit_exercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent in = new Intent(ct,AddExerciseActivity.class);
                in.putExtra("exercise",Exercises.get(position).getExercise());
                MainActivity.date_selected = Exercises.get(position).getDate(); // this is required by AddExerciseActivity
                System.out.println(Exercises.get(position).getExercise());
                System.out.println(MainActivity.date_selected);
                ct.startActivity(in);
            }
        });

        bt_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                // Dismiss Exercise Dialog Box
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }

    @Override
    public int getItemCount()
    {
        return this.Exercises.size();
    }

    public class MyViewHolder extends  RecyclerView.ViewHolder
    {
        TextView tv_exercise_name;
        CardView cardView;
        TextView sets;
        ImageView imageView;
        ImageButton pr_button;
        ImageButton comment_button;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_exercise_name = itemView.findViewById(R.id.day);
            cardView = itemView.findViewById(R.id.cardview_exercise);
            sets = itemView.findViewById(R.id.sets);
            imageView = itemView.findViewById(R.id.imageView);
            pr_button = itemView.findViewById(R.id.pr_button);
            comment_button = itemView.findViewById(R.id.comment_button);
        }
    }
}
