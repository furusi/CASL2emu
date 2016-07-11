package com.example.furusho.casl2sim;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.text.Layout;

/**
 * Created by furusho on 2016/07/07.
 */
public class InputText extends BaseObservable{
    public String inputText = new String("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");

    public void setInputText(String inputText) {
        this.inputText = inputText;
        notifyPropertyChanged(BR.inputText);
    }

    @Bindable
    public String getInputText() {
        return inputText;
    }

}
