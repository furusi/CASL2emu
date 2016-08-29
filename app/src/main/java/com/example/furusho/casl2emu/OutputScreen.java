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


public class OutputScreen extends AppCompatActivity implements Runnable{


    OutputBuffer outputBuffer;
    private AudioTrack audioTrack;

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

        audioTrack= outputBuffer.getSoundGenerator().getAudioTrack();
        Thread thread = new Thread(OutputScreen.this);
        thread.start();


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

      @Override
  public void run() {

    // 再生中なら一旦止める
    if(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
      audioTrack.stop();
      audioTrack.reloadStaticData();
    }
    // 再生開始
    audioTrack.play();

    // スコアデータを書き込む
    for(SoundDto dto : outputBuffer.getSoundList()) {
      audioTrack.write(dto.getSound(), 0, dto.getSound().length);
    }
    // 再生停止
    audioTrack.stop();
  }

}
