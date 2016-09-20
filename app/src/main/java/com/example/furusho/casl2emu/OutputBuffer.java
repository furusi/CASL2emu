package com.example.furusho.casl2emu;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.Button;

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
    private ArrayList<Casl2AsyncInputConfig> buttonconfig = new ArrayList<Casl2AsyncInputConfig>();
    private OutputBuffer() {

        for(int i=0;i<4;i++){
            buttonconfig.add(new Casl2AsyncInputConfig(i));
        }

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

    public Casl2AsyncInputConfig getButtonconfig(int num) {
        return buttonconfig.get(num);
    }

    public void setButtonconfig(int buttonnum, int visibility, char position ) {
        buttonconfig.get(buttonnum).setVisibility(visibility).setInputAddress(position);
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
    public void addDrawObjectArray(int type,Object object,int i,float width){

        Casl2Figure figure =new Casl2Figure();
        figure.setType(type);
        figure.setProp(object);

        int color;
        switch (i){
            case 1:
                color=Color.RED;
                break;
            case 2:
                color=Color.GREEN;
                break;
            case 3:
                color=Color.BLUE;
                break;
            case 4:
                color=Color.YELLOW;
                break;
            case 5:
                color=Color.BLACK;
                break;
            default:
                color=Color.WHITE;
        }
        figure.setColor(color);
        figure.setWidth(width);
        drawObjectArray.add(figure);
        casl2PaintView.setDrawObjectArray(drawObjectArray);

    }
}

