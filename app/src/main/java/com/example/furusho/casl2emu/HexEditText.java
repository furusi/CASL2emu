package com.example.furusho.casl2emu;

import android.content.Context;
import android.graphics.Typeface;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

/**
 * Created by furusho on 16/09/14.
 */


class HexEditText extends EditText {
    public HexEditText(Context context, int i) {
        super(context);
        this.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        switch (i){
            case 1:
                this.setKeyListener(DigitsKeyListener.getInstance(context.getString(R.string.a_to_f_0_to_9)));
                break;
            case 2:
                this.setKeyListener(DigitsKeyListener.getInstance(context.getString(R.string.zero_or_one)));
                break;
        }
        this.setTypeface(Typeface.MONOSPACE);
    }
    public static char[] getHexChars(String s, String separeter) {
        String[] stmp = s.split(separeter);
        char[] tmp= new char[stmp.length];
        for(int i=0;i<stmp.length;i++){
            tmp[i] = (char)Integer.parseInt(stmp[i],16);
        }
        return tmp;
    }
}
