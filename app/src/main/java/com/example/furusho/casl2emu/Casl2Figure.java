package com.example.furusho.casl2emu;

import android.graphics.Color;

/**
 * Created by furusho on 16/08/26.
 */

public class Casl2Figure {
    int type;
    Object prop;
    int color;

    public Casl2Figure(int type, Object prop,int color) {
        this.type = type;
        this.prop = prop;
        this.color = color;
    }

    public int getType() {

        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getProp() {

        return prop;
    }

    public int getColor() {
        return color;
    }

    public void setProp(Object prop) {
        this.prop = prop;
    }

}
