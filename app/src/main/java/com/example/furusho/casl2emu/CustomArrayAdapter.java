package com.example.furusho.casl2emu;

import android.content.Context;
import android.databinding.ObservableArrayList;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Observable;

/**
 * Created by furus on 2016/08/15.
 */

public class CustomArrayAdapter extends ArrayAdapter<String> {

    Context context;
    int resourceId;
    Typeface tf;
    ArrayList<String> arry;
    LayoutInflater layoutInflater =null;


    public CustomArrayAdapter(Context context, int resource, ArrayList<String> arry, Typeface tf) {
        super(context, resource, arry);
        this.resourceId= resource;
        this.context=context;
        this.arry = arry;
        this.tf =Typeface.create(tf,Typeface.NORMAL);
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView= layoutInflater.inflate(R.layout.column_row,parent,false);
        //View view= super.getView(position, convertView, parent);
        //1TextView textView = (TextView)convertView.findViewById(R.id.rowbody);
        ((TextView)convertView.findViewById(R.id.rowbody)).setTypeface(tf);
        ((TextView)convertView.findViewById(R.id.rowbody)).setText(arry.get(position));
        ((TextView)convertView.findViewById(R.id.rownumber)).setText(String.format(Locale.US,"0x%04X",position*4&0xFFFF));

        return convertView;
    }
}
