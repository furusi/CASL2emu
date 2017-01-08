package jp.ac.fukuoka_u.tl.casl2emu;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

/**
 * Created by furus on 2016/08/15.
 */

public class Casl2Register extends BaseObservable{
    static Casl2Register instance = new Casl2Register();
    @Bindable
    public char gr[] = new char[8];
    @Bindable
    private char pc=0x0000;
    @Bindable
    private char sp=0xFEFF;
    @Bindable
    private char fr[] = new char[3];


    static public void initializeInstance() {
        instance = new Casl2Register();
    }
    public static Casl2Register getInstance() {
        return instance;
    }


    @Bindable
    public char[] getGr() {
        return gr;
    }

    public void setGr(char[] gr) {
        this.gr = gr;
        notifyPropertyChanged(BR.gr);
    }
    public void setGr(char data,int position){
        if(position<gr.length&&position>=0){
            gr[position]= data;
            notifyPropertyChanged(BR.gr);
        }
    }

    public char getPc() {
        return pc;
    }

    public void setPc(char pc) {
        this.pc = pc;
        notifyPropertyChanged(BR.pc);
    }

    public char getSp() {
        return sp;
    }

    public void setSp(char sp) {
        if(sp<0xFF00){
            this.sp = sp;
        }
        notifyPropertyChanged(BR.sp);
    }

    public char[] getFr() {
        return fr;
    }

    public void setFr(char[] fr) {
        this.fr[0] = fr[0];
        this.fr[1] = fr[1];
        this.fr[2] = fr[2];
        notifyPropertyChanged(BR.fr);
    }
    public void setFr(char data,int position) {
        this.fr[position] = data;
        notifyPropertyChanged(BR.fr);
    }
    public void setDatafromBinary(byte[] loaddata){

        for (int i = 0; i < 8; i++) {
            this.setGr((char) ((char)(loaddata[2 * i]<<8)+loaddata[2 * i + 1]), i);
        }
        this.setPc((char) ((char)(loaddata[8 * 2]<<8)+loaddata[8 * 2 + 1]));
        this.setSp((char) ((char)(loaddata[9 * 2]<<8)+loaddata[9 * 2 + 1]));
        for (int i = 0; i < 3; i++) {
            this.setFr((char) ((char) (loaddata[2 * (10 + i)] << 8) + loaddata[2 * (10 + i) + 1]), i);
        }
    }

}
