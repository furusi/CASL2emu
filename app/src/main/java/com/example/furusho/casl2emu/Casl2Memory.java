package com.example.furusho.casl2emu;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by furusho on 2016/07/09.
 */
public class Casl2Memory {

    static Casl2Memory instance = new Casl2Memory();
    ArrayList<String> memory;

    private Casl2Memory(){
        memory = new ArrayList<>();
    }

    public ArrayList<String> getMemoryRow() {
        return memory;
    }
    public String getMemoryRow(int i) {
        return memory.get(i);
    }

    public void setMemory(ArrayList<String> memory) {
        this.memory = memory;
    }
    public void setMemory(String memory,int position) {
       this.memory.set(position,memory);
    }

    static Casl2Memory getInstance(){
       return instance;
    }

    public int getMemory(int c) {
        int rowNum = (int)c/8;
        int columnNum = (int)c%8;
        String str = this.getMemoryRow(rowNum);
        String[] arr = str.split(" ");
        Log.d("aaaaa","tmpの中身は"+ Integer.parseInt(arr[columnNum],16) +"だよ");
        return Integer.parseInt(arr[columnNum],16);
    }
}
