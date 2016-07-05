package com.example.furusho.casl2sim;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by furusho on 2016/07/05.
 */
public class Commetii {
    int address;

    int gr0;
    int gr1;
    int gr2;
    int gr3;
    int gr4;
    int gr5;
    int gr6;
    int gr7;

    int sp;
    int pr;
    //flag register
    int of;
    int sf;
    int zf;

    final List<String> sourceCode;

    public Commetii(String[] text) {
        sourceCode = Lists.newArrayList(text);

        //stringから一行ずつ取り出す
        //行頭がaddaならば
        //二つの文字を足し、settextする。

        //out 1ならば
        //gr0を出力する
    }

    //LAD GR2,1,GR2
    void cm2OUT(int output,int outputLength){
        System.out.println(output+"%d\n");
    }


}
