package jp.ac.fukuoka_u.tl.casl2emu.android;


/**
 * Created by furusho on 16/08/26.
 */

public class Casl2Figure {
    int type;
    Object prop;
    int color;
    float width;

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Casl2Figure() {

    }

    public Casl2Figure(int type, Object prop, int color) {
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
