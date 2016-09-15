package com.example.furusho.casl2emu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.furusho.casl2emu.databinding.ActivityOutputScreenBinding;


public class OutputScreen extends BaseActivity {


    OutputBuffer outputBuffer;
    Casl2Emulator emulator;
    Casl2PaintView paintView;
    BroadcastReceiver receiver ;
    IntentFilter filter;
    RelativeLayout relativeLayout;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output_screen);
        emulator = Casl2Emulator.getInstance(getApplicationContext());
        outputBuffer = OutputBuffer.getInstance();
        outputBuffer.setCasl2PaintView(getApplicationContext());
        paintView = outputBuffer.getCasl2PaintView();
        handler = new Handler();
        relativeLayout = (RelativeLayout) findViewById(R.id.out_relativelayout);
        final ActivityOutputScreenBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_output_screen);
        final RelativeLayout layout = binding.outRelativelayout;
        //binding.output.setText("Casl2emu is LEADY");
        //outputBuffer.setData("CASL2Emu is ready!!!!");
        binding.setOutputbuffer(outputBuffer);
        outputBuffer.setCasl2PaintView(getApplicationContext());
        addContentView(paintView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        binding.runbuttonoutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emulator.run(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(getString(R.string.intervalkey),1000));
            }
        });
        binding.stepbuttonoutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emulator.stepOver();
                //paintView.invalidate();
            }
        });
        binding.waitbuttonoutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emulator.waitEmu();
            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(getString(R.string.action_view_refresh))){
                    binding.output.setText(outputBuffer.getData());
                    refresh();
                }else{
                   //ボタンを表示する
                }
            }
        };
        filter = new IntentFilter(getString(R.string.action_view_refresh));
        registerReceiver(receiver,filter);
        registerReceiver(receiver,new IntentFilter(getString(R.string.action_async_input)));

        binding.asyncbutton1.setVisibility(Button.INVISIBLE);
        binding.asyncbutton2.setVisibility(Button.INVISIBLE);
        binding.asyncbutton3.setVisibility(Button.INVISIBLE);
        binding.asyncbutton4.setVisibility(Button.INVISIBLE);
    }

    private void refresh(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                RelativeLayout layout= (RelativeLayout)findViewById(R.id.out_relativelayout);
                layout.invalidate();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //textView.setText(Joiner.on("\n").skipNulls().join(code));
    }

}
