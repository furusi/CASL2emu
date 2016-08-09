package com.example.furusho.casl2sim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.common.base.Joiner;


public class OutputScreen extends AppCompatActivity {


    String[] code;
    Commetii cm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output_screen);
        code = this.getIntent().getStringArrayExtra(new String("sourcecode"));
        OutputReceiver myReceiver = new OutputReceiver();

        IntentFilter mIF = new IntentFilter();
        mIF.addAction("com.example.furusho.casl2sim.output");
        registerReceiver(myReceiver, mIF);
        cm = new Commetii(code,this.getApplication());
    }

    @Override
    protected void onStart() {
        super.onStart();
        //textView.setText(Joiner.on("\n").skipNulls().join(code));
    }


    public class OutputReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
        }
    }
}
