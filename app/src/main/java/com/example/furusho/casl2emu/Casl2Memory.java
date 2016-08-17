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
        for(int i = 0; i <=count;i++){
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
    public void setMemoryArray(char[] data,int position) {
        for(int i = 0; i <data.length;i++){
            memory[i+position]=data[i];
        }
        notifyPropertyChanged(BR.casl2Memory);
    }

    static Casl2Memory getInstance(){
       return instance;
    }

    public ArrayList<String> getMemoryList(){
        char[] a = getMemory();
        ArrayList<String> stringArrayList = new ArrayList<String>();
        for(int i=0;i<a.length/8;i++){
            stringArrayList.add(String.format(Locale.US ,"%02X %02X %02X %02X %02X %02X %02X %02X", a[8*i] & 0xFFFF, a[8*i+1] & 0xFFFF, a[8*i+2] & 0xFFFF, a[8*i+3] & 0xFFFF, a[8*i+4] & 0xFFFF, a[8*i+5] & 0xFFFF, a[8*i+6] & 0xFFFF, a[8*i+7] & 0xFFFF));
        }
        return stringArrayList;
    }

}
