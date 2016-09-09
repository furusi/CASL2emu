package com.example.furusho.casl2emu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.media.AudioTrack;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.furusho.casl2emu.databinding.ActivityOutputScreenBinding;


public class OutputScreen extends AppCompatActivity {


    OutputBuffer outputBuffer;
    Casl2Emulator emulator = Casl2Emulator.getInstance(getApplicationContext());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output_screen);


        ActivityOutputScreenBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_output_screen);
        //binding.output.setText("Casl2emu is LEADY");
        outputBuffer = OutputBuffer.getInstance();
        //outputBuffer.setData("CASL2Emu is ready!!!!");
        binding.setOutputbuffer(outputBuffer);
        outputBuffer.setCasl2PaintView(getApplicationContext());
        addContentView(outputBuffer.getCasl2PaintView(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        binding.runbuttonoutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emulator.run();
            }
        });
        binding.stepbuttonoutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emulator.stepOver();
            }
        });
        binding.waitbuttonoutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emulator.waitEmu();
            }
        });
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
