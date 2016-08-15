package com.example.furusho.casl2sim;

import java.util.ArrayList;

/**
 * Created by furusho on 2016/07/09.
 */
public class CASL2Memory {

    static CASL2Memory instance = new CASL2Memory();
    ArrayList<String> memory;

    private CASL2Memory(){
        memory = new ArrayList<>();
    }

    public ArrayList<String> getMemory() {
        return memory;
    }

    public void setMemory(ArrayList<String> memory) {
        this.memory = memory;
    }
    public void setMemory(String memory,int position) {
       this.memory.set(position,memory);
    }

    static CASL2Memory getInstance(){
       return instance;
    }
}
