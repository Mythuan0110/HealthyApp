package com.example.verifit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

public class ActionActivity extends AppCompatActivity {
    ArrayList<Action> foodArrayList;
    DaoAction daoAction;
    ActionAdapter actionAdapter;
    Toolbar toolbar;
    TextView titletoolbar;
    RecyclerView rcvfood;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listfood );
        rcvfood = findViewById(R.id.rcvfood);
        Intent getdata = getIntent();
        String matl = getdata.getStringExtra("matl");
        daoAction = new DaoAction( ActionActivity.this);
        foodArrayList = new ArrayList<>();
        actionAdapter = new ActionAdapter(foodArrayList, ActionActivity.this);
        GridLayoutManager idLayoutManager = new GridLayoutManager( ActionActivity.this,1);
        rcvfood.setLayoutManager(idLayoutManager);
        rcvfood.setHasFixedSize(true);
        rcvfood.setAdapter(actionAdapter);

        daoAction.getAll(new Actioncallback() {
            @Override
            public void onSuccess(ArrayList<Action> lists) {
                foodArrayList.clear();
                for (int i =0;i<lists.size();i++){
                    if (lists.get(i).getMatheloai().equalsIgnoreCase(matl)){
                        foodArrayList.add(lists.get(i));
                        actionAdapter.notifyDataSetChanged();
                    }
                }

            }
            @Override
            public void onError(String message) {

            }
        });
    }


}