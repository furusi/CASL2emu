package com.example.furusho.casl2emu;

import java.util.ArrayList;

/**
 * Created by furusho on 16/08/24.
 */

public class OutputBuffer {
    private String data="";
    private static OutputBuffer instance = new OutputBuffer();


    public OutputBuffer() {

    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public static OutputBuffer getInstance(){
        return instance;

    }
}
