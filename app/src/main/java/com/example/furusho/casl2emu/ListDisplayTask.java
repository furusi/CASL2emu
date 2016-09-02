package com.example.furusho.casl2emu;

import android.content.AsyncTaskLoader;
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
        for(int i = position; i< chars.length/4; i++){
            stringArrayList.add(String.format(Locale.US ,"%04X %04X %04X %04X",
                chars[4*i] & 0xFFFF, chars[4*i+1] & 0xFFFF, chars[4*i+2] & 0xFFFF, chars[4*i+3] &
                0xFFFF));
        }
        return stringArrayList;
    }


    public ListDisplayTask(Context context) {
        super(context);
    }

}
