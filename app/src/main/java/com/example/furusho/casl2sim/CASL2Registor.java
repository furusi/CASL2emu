package com.example.furusho.casl2sim;

import java.util.ArrayList;

/**
 * Created by furus on 2016/08/15.
 */

public class CASL2Registor {
    static CASL2Registor instance = new CASL2Registor();
    ArrayList<String> memory;

    public static CASL2Registor getInstance() {
        return instance;
    }
}
