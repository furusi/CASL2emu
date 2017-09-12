package jp.ac.fukuoka_u.tl.casl2emu.android;

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
import android.widget.TextView;

import jp.ac.fukuoka_u.tl.casl2emu.R;
import jp.ac.fukuoka_u.tl.casl2emu.databinding.ActivityOutputScreenBinding;


public class OutputScreen extends BaseActivity {


    OutputBuffer outputBuffer;
    //Casl2Emulator emulator;
    Casl2PaintView paintView;
    IntentFilter filter;
    RelativeLayout relativeLayout;
    Handler handler;
    BroadcastReceiver opsreceiver;

    @SuppressWarnings("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output_screen);
        outputBuffer = OutputBuffer.getInstance();
        outputBuffer.setCasl2PaintView(getApplicationContext());
        paintView = outputBuffer.getCasl2PaintView();
        handler = new Handler();
        relativeLayout = (RelativeLayout) findViewById(R.id.out_relativelayout);
        final ActivityOutputScreenBinding binding =
                DataBindingUtil.setContentView(this,R.layout.activity_output_screen);
        //binding.output.setText("Casl2emu is LEADY");
        //outputBuffer.setData("CASL2Emu is ready!!!!");
        binding.setOutputbuffer(outputBuffer);
        outputBuffer.setCasl2PaintView(getApplicationContext());
        addContentView(paintView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        binding.runbuttonoutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emulator.run(PreferenceManager.getDefaultSharedPreferences(
                        getApplicationContext()).getInt(getString(R.string.intervalkey),1000));
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
        final Intent intent = new Intent(getString(R.string.action_memory_refresh));
        binding.asyncbutton1.setVisibility(Button.GONE);
        binding.asyncbutton1.setVisibility(outputBuffer.getButtonconfig(0).getVisibility());
        binding.asyncbutton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                char address = outputBuffer.getButtonconfig(0).getInputAddress();
                emulator.setMemory((char) 1,address);
                intent.putExtra(getString(R.string.BUTTON_INPUT_ADDRESS),address);
                getApplicationContext().sendBroadcast(intent);
            }
        });
        binding.asyncbutton2.setVisibility(Button.GONE);
        binding.asyncbutton2.setVisibility(outputBuffer.getButtonconfig(1).getVisibility());
        binding.asyncbutton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                char address = outputBuffer.getButtonconfig(1).getInputAddress();
                emulator.setMemory((char) 1,address);
                intent.putExtra(getString(R.string.BUTTON_INPUT_ADDRESS),address);
                getApplicationContext().sendBroadcast(intent);
            }
        });
        binding.asyncbutton3.setVisibility(Button.GONE);
        binding.asyncbutton3.setVisibility(outputBuffer.getButtonconfig(2).getVisibility());
        binding.asyncbutton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                char address = outputBuffer.getButtonconfig(2).getInputAddress();
                emulator.setMemory((char) 1,address);
                intent.putExtra(getString(R.string.BUTTON_INPUT_ADDRESS),address);
                getApplicationContext().sendBroadcast(intent);
            }
        });
        binding.asyncbutton4.setVisibility(Button.GONE);
        binding.asyncbutton4.setVisibility(outputBuffer.getButtonconfig(3).getVisibility());
        binding.asyncbutton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                char address = outputBuffer.getButtonconfig(3).getInputAddress();
                emulator.setMemory((char) 1,address);
                intent.putExtra(getString(R.string.BUTTON_INPUT_ADDRESS),address);
                getApplicationContext().sendBroadcast(intent);
            }
        });

    }




    @SuppressWarnings("WrongConstant")
    @Override
    protected void onResume() {
        super.onResume();

        opsreceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(getString(jp.ac.fukuoka_u.tl.casl2emu.R.string.action_view_refresh))){
                    TextView output = (TextView)findViewById(R.id.output);
                    output.setText(outputBuffer.getData());
                    output.invalidate();
                }
            }
        };
        filter = new IntentFilter(getString(R.string.action_view_refresh));
        registerReceiver(opsreceiver,filter);

    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(opsreceiver);
    }


}
