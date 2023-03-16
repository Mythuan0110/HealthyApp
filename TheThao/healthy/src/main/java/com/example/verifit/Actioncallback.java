package com.example.verifit;

import java.util.ArrayList;

public interface Actioncallback {
    void onSuccess(ArrayList<Action> lists);
    void onError(String message);
}
