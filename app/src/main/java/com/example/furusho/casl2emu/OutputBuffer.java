package com.example.furusho.casl2emu;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by furusho on 16/08/24.
 */

public class OutputBuffer extends BaseObservable{

    private String data="CASL2Emu is ready.\n";
    private static OutputBuffer instance = new OutputBuffer();
    private Casl2PaintView casl2PaintView;
    private ArrayList<Casl2Figure> drawObjectArray = new ArrayList<Casl2Figure>();


    private OutputBuffer() {

    }

    @Bindable
    public String getData() {
        return data;
    }

    @Bindable
    public void setData(String data) {
        this.data = data;
    }

    public void addData(String data){
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append(this.data);
        stringBuilder.append(data);
        setData(stringBuilder.toString());
    }

    public static OutputBuffer getInstance(){
        return instance;

    }

    public Casl2PaintView getCasl2PaintView() {
        return casl2PaintView;
    }

    public void setCasl2PaintView(Context context) {
        this.casl2PaintView = new Casl2PaintView(context);
    }


    public ArrayList<Casl2Figure> getDrawObjectArray() {
        return drawObjectArray;
    }

    public void setDrawObjectArray(ArrayList<Casl2Figure> drawObjectArray) {
        this.drawObjectArray = drawObjectArray;
    }
    public void addDrawObjectArray(int type,Object o,int i){

        int color;
        switch (i){
            case 1:
                color=Color.RED;
                break;
            default:
                color=Color.WHITE;
        }
        drawObjectArray.add(new Casl2Figure(type,o,color));

    }
}

