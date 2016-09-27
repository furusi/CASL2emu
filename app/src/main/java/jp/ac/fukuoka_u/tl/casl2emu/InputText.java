package jp.ac.fukuoka_u.tl.casl2emu;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

public class InputText extends BaseObservable{
    public String inputText;
    public String rowNum;

    public void setInputText(String inputText) {
        this.inputText = inputText;
        notifyPropertyChanged(BR.inputText);
    }

    @Bindable
    public String getInputText() {
        return inputText;
    }

    @Bindable
    public void setRowNum(String str){
       rowNum = str;
    }

}
