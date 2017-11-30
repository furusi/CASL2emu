package jp.ac.fukuoka_u.tl.casl2emu.android;

import android.content.Context;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by furusho on 2016/08/17.
 */

public class ListDisplayTask extends SimpleTaskLoader {
    private char[] chars;
    private int position;
    public ListDisplayTask(Context context , char[] cs, int i) {
        super(context);
        chars=cs;
        position=i;
    }

    @Override
    public ArrayList<String> loadInBackground() {
        ArrayList<String> stringArrayList = new ArrayList<String>();
        for(int i = position; i< chars.length; i++){
            stringArrayList.add(String.format(Locale.US ,"%04X", chars[i] & 0xFFFF));
        }
        return stringArrayList;
    }


    public ListDisplayTask(Context context) {
        super(context);
    }

}
