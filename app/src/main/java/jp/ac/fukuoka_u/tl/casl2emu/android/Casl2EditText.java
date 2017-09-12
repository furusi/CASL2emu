package jp.ac.fukuoka_u.tl.casl2emu.android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.method.DigitsKeyListener;

import jp.ac.fukuoka_u.tl.casl2emu.R;

/**
 * Created by furusho on 16/09/14.
 */


public class Casl2EditText extends android.support.v7.widget.AppCompatEditText {
    public Casl2EditText(Context context, int i) {
        super(context);
        this.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        setInputType(context, i);
        this.setTypeface(Typeface.MONOSPACE);
        this.setTextColor(Color.BLACK);
    }

    public void setInputType(Context context, int i) {
        switch (i){
            case 1:
                this.setKeyListener(DigitsKeyListener.getInstance(context.getString(R.string.a_to_f_0_to_9)));
                break;
            case 2:
                this.setKeyListener(DigitsKeyListener.getInstance(context.getString(R.string.zero_or_one)));
                break;
            case 3:
                this.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
                break;
            case 4:
                this.setKeyListener(DigitsKeyListener.getInstance("0123456789-"));
                break;
        }
    }

    public static char[] getHexChars(String s, String separeter) {
        String[] stmp = s.split(separeter);
        char[] tmp= new char[stmp.length];
        for(int i=0;i<stmp.length;i++){
            tmp[i] = (char)Integer.parseInt(stmp[i],16);
        }
        return tmp;
    }
    public static char getChar(String s) throws NumberFormatException{
        char result;
        result = Character.forDigit(Integer.parseInt(s,16),16);

        return result;

    }
    public static short getShort(String s) throws NumberFormatException{
        short result;
        result = Short.parseShort(s,16);

        return result;

    }
}
