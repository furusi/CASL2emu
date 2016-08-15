package com.example.furusho.casl2sim;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.util.ArrayList;

/**
 * Created by furus on 2016/08/15.
 */

public class CASL2Register extends BaseObservable{
    static CASL2Register instance = new CASL2Register();
    @Bindable
    public String gr[] = new String[8];
    private char pc;
    private char sp;
    private char fr[] = new char[3];


    public static CASL2Register getInstance() {
        return instance;
    }


    @Bindable
    public String[] getGr() {
        return gr;
    }

    public void setGr(String[] gr) {
        this.gr = gr;
        notifyPropertyChanged(BR.register);
    }

    public char getPc() {
        return pc;
    }

    public void setPc(char pc) {
        this.pc = pc;
    }

    public char getSp() {
        return sp;
    }

    public void setSp(char sp) {
        this.sp = sp;
    }

    public char[] getFr() {
        return fr;
    }

    public void setFr(char[] fr) {
        this.fr = fr;
    }
}
