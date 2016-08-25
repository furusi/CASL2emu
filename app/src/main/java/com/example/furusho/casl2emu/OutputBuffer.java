package com.example.furusho.casl2emu;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
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
    private HashMap<Integer,Object> drawObjectArray ;


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


    public HashMap<Integer, Object> getDrawObjectArray() {
        return drawObjectArray;
    }

    public void setDrawObjectArray(HashMap<Integer, Object> drawObjectArray) {
        this.drawObjectArray = drawObjectArray;
    }
    public void addDrawObjectArray(int type,Object o){

        drawObjectArray.put(type,o);
    }
}

