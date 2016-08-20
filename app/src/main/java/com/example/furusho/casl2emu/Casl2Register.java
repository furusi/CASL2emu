package com.example.furusho.casl2emu;

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
    private char pc;
    @Bindable
    private char sp;
    @Bindable
    private char fr[] = new char[3];


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
        gr[position]= data;
        notifyPropertyChanged(BR.gr);
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
        this.sp = sp;
        notifyPropertyChanged(BR.sp);
    }

    public char[] getFr() {
        return fr;
    }

    public void setFr(char[] fr) {
        this.fr = fr;
        notifyPropertyChanged(BR.fr);
    }
    public void setFr(char data,int position) {
        this.fr[position] = data;
        notifyPropertyChanged(BR.fr);
    }

}
