package com.example.furusho.casl2emu;

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

    public ArrayList<String> getMemory() {
        return memory;
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
}
