package com.example.furusho.casl2sim;

import android.app.Application;
import android.content.Intent;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by furusho on 2016/07/05.
 */
public class Commetii {


    int address;

    int gr[]={0,11,22,33,44,55,66,77};
    int addr[]=new int[65536];

    int sp;
    int pr;
    //flag register
    int of;
    int sf;
    int zf;

    final List<String> sourceCode;
    String output;
    String adda = "^\\s*(ADDA)\\s+GR(\\d+)\\s+GR(\\d+)\\s*$";
    String regex_in = "^\\s*(IN)\\s+GR(\\d+)\\s+GR(\\d+)\\s?.*$";



    public Commetii(String[] text, Application application) {
        sourceCode = Lists.newArrayList(text);
        String line = new String();


        for(int i=0;i< sourceCode.size();i++) {
            line = sourceCode.get(i);

            parseCode(line, adda);
            //stringから一行ずつ取り出す
                //行頭がaddaならば
                //二つの文字を足し、settextする。
            if(output!="") {
                sendOuput(application);
                output="";
            }

        }
    }

    private void parseCode(String line, String regex) {
        if (line.matches(regex)) {
            Pattern ptn = Pattern.compile(regex);
            Matcher mch = ptn.matcher(line);
            int n=0,m=0;
            int gc=mch.groupCount();
            if(mch.find()) {
                String r1 = mch.group(2);
                String r2 = mch.group(3);
                n = Integer.parseInt(r1);
                m = Integer.parseInt(r2);
                output=Integer.toString(gr[n]+gr[m]);
            }
        }
    }

    private void sendOuput(Application application) {
        Intent outputIntent = new Intent();
        outputIntent.putExtra("output", output);
        outputIntent.setAction("com.example.furusho.casl2sim.output");
    }


    //LAD GR2,1,GR2
    void cm2OUT(int output,int outputLength, TextView t){
        System.out.println(output+"%d\n");
        //t.setText("from Commet2");
    }



}
