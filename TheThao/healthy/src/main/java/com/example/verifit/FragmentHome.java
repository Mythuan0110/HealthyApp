package com.example.verifit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class FragmentHome extends Fragment {

    RecyclerView rcvhome,rcvmonan;
    ArrayList<Categories> datacategories;
    DaoCategories daoCategories;
    TextView txtslogan;
    CategoryAdapter categoryAdapter;
    FoodAdapter foodAdapter;
    FoodAdapter actionAdapter;
    ArrayList<Food> foodArrayList;
    DaoFood daoFood;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragmenthome,container,false);
        rcvmonan = view.findViewById(R.id.rcvmonan);
        rcvhome = view.findViewById(R.id.rcvhome);
        txtslogan = view.findViewById(R.id.txtslogan);
        daoCategories = new DaoCategories(getActivity());
        datacategories = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(datacategories,getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        rcvhome.setLayoutManager(linearLayoutManager);
        rcvhome.setHasFixedSize(true);
        rcvhome.setAdapter(categoryAdapter);


        Date currentTime = Calendar.getInstance().getTime();
        if (currentTime.getTime() < 10) {
            txtslogan.setText("Start your day with delicious food !");
            foodArrayList = new ArrayList<>();
            foodAdapter = new FoodAdapter(foodArrayList,getActivity());
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),2);
            rcvmonan.setLayoutManager(gridLayoutManager);
            rcvmonan.setHasFixedSize(true);
            rcvmonan.setAdapter(foodAdapter);
            daoFood = new DaoFood(getActivity());
            daoFood.getAll(new Foodcallback() {
                @Override
                public void onSuccess(ArrayList<Food> lists) {
                    foodArrayList.clear();
                    for (int i =0;i<lists.size();i++){
                        if (lists.get(i).getStatus().equalsIgnoreCase("Sáng")){
                            foodArrayList.add(lists.get(i));
                            foodAdapter.notifyDataSetChanged();
                        }
                    }

                }
                @Override
                public void onError(String message) {

                }
            });
        } else if (currentTime.getTime() >= 10 && currentTime.getTime() <= 15 ){
            txtslogan.setText("Recharge for lunch !");
            foodArrayList = new ArrayList<>();
            foodAdapter = new FoodAdapter(foodArrayList,getActivity());
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),2);
            rcvmonan.setLayoutManager(gridLayoutManager);
            rcvmonan.setHasFixedSize(true);
            rcvmonan.setAdapter(foodAdapter);
            daoFood = new DaoFood(getActivity());
            daoFood.getAll(new Foodcallback() {
                @Override
                public void onSuccess(ArrayList<Food> lists) {
                    foodArrayList.clear();
                    for (int i =0;i<lists.size();i++){
                        if (lists.get(i).getStatus().equalsIgnoreCase("Trưa")){
                            foodArrayList.add(lists.get(i));
                            foodAdapter.notifyDataSetChanged();
                        }
                    }

                }
                @Override
                public void onError(String message) {

                }
            });
        }else if (currentTime.getTime() > 15){
            txtslogan.setText("Afternoon with delicious food !");
            foodArrayList = new ArrayList<>();
            foodAdapter = new FoodAdapter(foodArrayList,getActivity());
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),2);
            rcvmonan.setLayoutManager(gridLayoutManager);
            rcvmonan.setHasFixedSize(true);
            rcvmonan.setAdapter(foodAdapter);
            daoFood = new DaoFood(getActivity());
            daoFood.getAll(new Foodcallback() {
                @Override
                public void onSuccess(ArrayList<Food> lists) {
                    foodArrayList.clear();
                    for (int i =0;i<lists.size();i++){
                        if (lists.get(i).getStatus().equalsIgnoreCase("Chiều")){
                            foodArrayList.add(lists.get(i));
                            foodAdapter.notifyDataSetChanged();
                        }
                    }

                }
                @Override
                public void onError(String message) {

                }
            });
        }




        daoCategories.getAll(new Categoriescallback() {
            @Override
            public void onSuccess(ArrayList<Categories> lists) {
                datacategories.clear();
                datacategories.addAll(lists);
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String message) {

            }
        });

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
//        foodAdapter.notifyDataSetChanged();
//        categoryAdapter.notifyDataSetChanged();
    }
}
