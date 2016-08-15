package com.example.furusho.casl2sim;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by furus on 2016/08/15.
 */

public class CustomArrayAdapter extends ArrayAdapter<String> {

    Context context;
    int resourceId;
    Typeface tf;
    ArrayList<String> arry;


    public CustomArrayAdapter(Context context, int resource, ArrayList<String> arry, Typeface tf) {
        super(context, resource, arry);
        this.resourceId= resource;
        this.context=context;
        this.arry = arry;
        this.tf =Typeface.create(tf,Typeface.NORMAL);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view= super.getView(position, convertView, parent);
        TextView textView = (TextView)view;
        textView.setTypeface(tf);
        return textView;
    }
}
