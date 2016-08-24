package com.example.furusho.casl2emu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class OutputScreen extends AppCompatActivity {


    String[] code;
    Commetii cm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output_screen);
        if(this.getIntent().hasExtra("sourcecode")) {
            code = this.getIntent().getStringArrayExtra("sourcecode");
            OutputReceiver myReceiver = new OutputReceiver();

            IntentFilter mIF = new IntentFilter();
            mIF.addAction("com.example.furusho.casl2emu.output");
            registerReceiver(myReceiver, mIF);
            cm = new Commetii(code, this.getApplication());
        }
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
