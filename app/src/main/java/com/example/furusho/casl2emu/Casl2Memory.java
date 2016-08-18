package com.example.furusho.casl2emu;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Observable;

/**
 * Created by furusho on 2016/07/09.
 */
public class Casl2Memory extends BaseObservable{

    static Casl2Memory instance = new Casl2Memory();
    @Bindable
    char[] memory = new char[65536];

    private Casl2Memory(){
        Arrays.fill(memory,'0');
    }

    public char[] getMemory() {
        return memory;
    }
    public char[] getMemoryArray(int start, int count) {
        char[]tmp=new char[count];
        for(int i = 0; i <count;i++){
            tmp[i]=memory[i+start];
        }
        return tmp;
    }
    public char getMemory(int position) {
        return memory[position];
    }

    public void setMemory(char[] data) {
        Arrays.fill(memory,'\0');
        for(int i = 0; i <data.length;i++){
            memory[i]=data[i];
        }
    }
    public void setMemoryArray(char[] data, int position) {
        for(int i = 0; i <data.length;i++){
            memory[i+position]=data[i];
        }
        notifyPropertyChanged(BR.casl2Memory);
    }
    public void setMemory(char data, int position) {
            memory[position]=data;
        notifyPropertyChanged(BR.casl2Memory);
    }

    static Casl2Memory getInstance(){
       return instance;
    }

}
