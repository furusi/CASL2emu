package jp.ac.fukuoka_u.tl.casl2emu.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.JetPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Random;

import jp.ac.fukuoka_u.tl.casl2emu.Casl2Emulator;
import jp.ac.fukuoka_u.tl.casl2emu.R;

/**
 * Android用のエミュレータクラス
 */

public class Casl2EmulatorAndroid extends Casl2Emulator {
    private Handler handler=null;
    private static JetPlayer jetPlayer=JetPlayer.getJetPlayer();
    private Context context=null;
    private static SoundPool soundPool = null;


    private static Intent broadcastIntent= new Intent();



    void initializeInstanceAndroid(Context context1) {
            context = context1;
            jetPlayer.loadJetFile(context.getResources().openRawResourceFd(R.raw.doremifa));
            broadcastIntent.setAction(context.getString(R.string.action_view_refresh));
            outputBuffer.setCasl2PaintView(context);
            initializeSoundPool();
    }

    private static void initializeSoundPool() {
        AudioAttributes attr = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        soundPool = new SoundPool.Builder().setAudioAttributes(attr)
                .setMaxStreams(6)
                .build();
    }


    @Override
    public void opSVC(char cpc, short[] sr) {
                int wordCount;
        char[] instArray;
        char jikkou;
        short smember;
        int ians;
        char r1;
        char cmember;//データに基づいて処理する
        wordCount = 2;
        jikkou = getEffectiveAddress();
        char memory_position;
        char count;
        char[] subarray;
        Intent inputintent = new Intent(context.getString(R.string.action_svc_input));
        //spの指すアドレスを取得
        switch(jikkou){
            case 0xFF00://符号付き値input
                if(isRunflag()){
                    setRunflag(false);
                    setInterruptflag(true);
                }
                memory_position = 7;
                inputintent.putExtra(context.getString(R.string.memory_position),memory_position);
                inputintent.putExtra(context.getString(R.string.ValueType),0xFF00);
                context.sendBroadcast(inputintent);
                break;
            case 0xFF01://符号なし値input
                if(isRunflag()){
                    setRunflag(false);
                    setInterruptflag(true);
                }
                memory_position = 7;
                inputintent.putExtra(context.getString(R.string.memory_position),memory_position);
                inputintent.putExtra(context.getString(R.string.ValueType),0xFF01);
                context.sendBroadcast(inputintent);
                break;
            case 0xFF02://input
                if(isRunflag()){
                    setRunflag(false);
                    setInterruptflag(true);
                }
                memory_position = register.getGr()[7];
                char length = register.getGr()[6];
                inputintent.putExtra(context.getString(R.string.memory_position),memory_position);
                inputintent.putExtra(context.getString(R.string.input_length),length);
                inputintent.putExtra(context.getString(R.string.ValueType),0xFF02);
                context.sendBroadcast(inputintent);
                break;

            case 0xFF03://OUT
                //r7を先頭アドレス、r6を文字数(wordの数ではない)とする。
                memory_position = register.getGr()[7];
                count = register.getGr()[6];
                //文字数分のデータを読み取りStringに変換。
                subarray = Arrays.copyOfRange(memory.getMemory(),memory_position,memory_position+count);
                byte[] chardataStr = new byte[subarray.length];
                byte chardata;
                for(int i =0;i<subarray.length;i++){

                    chardata = (byte) (subarray[i]&0x00FF);
                    if((chardata>=0x20&&chardata<=0x7E)||chardata==0x0a){
                       chardataStr[i]=chardata;
                    }
                }
                outputBuffer.addData(new String(chardataStr));
                break;
            case 0xFF10://算術乗算
                /**
                 * GR6:掛けられる数
                 * GR7:掛ける数
                 */
                sr[0] = (short) register.getGr()[7];
                smember = (short) register.getGr()[6];
                ians = (int) checkShortRange(sr[0]*smember);
                register.setGr((char) ((ians&0xFFFF0000)>>16),6);
                register.setGr((char) ((ians&0x0000FFFF)),7);
                break;
            case 0xFF11://論理乗算
                /**
                 * GR6:掛けられる数
                 * GR7:掛ける数
                 */
                r1 = register.getGr()[7];
                cmember = register.getGr()[6];
                ians = (int) checkCharRange(r1*cmember);
                register.setGr((char) ((ians&0xFFFF0000)>>16),6);
                register.setGr((char) ((ians&0x00FFFF)),7);
                break;
            case 0xFF12://算術除算
                /**
                 * GR6:割られる数
                 * GR7:割る数
                 */
                sr[0] = (short) register.getGr()[7];
                smember = (short) register.getGr()[6];
                ians = (short) checkShortRange(smember/sr[0]);
                int amari = smember%sr[0];
                register.setGr((char) ians,6);
                register.setGr((char) amari,7);
                break;
            case 0xFF13://論理除算
                /**
                 * GR6:割られる数
                 * GR7:割る数
                 */
                r1 = register.getGr()[7];
                cmember = register.getGr()[6];

                ians = (char) checkCharRange(cmember/r1);
                char amari1 = (char) (cmember%r1);
                register.setGr((char) ians,6);
                register.setGr(amari1,7);
                break;
            case 0xFF30://描画
                //先頭アドレス:gr7
                memory_position = register.getGr()[7];
                int color;
                int width;
                subarray = Arrays.copyOfRange(memory.getMemory(),memory_position,memory_position+7);
                switch (subarray[0]){//種類別の処理
                    case 3://circle
                        float cx = (short)subarray[1];
                        float cy = (short)subarray[2];
                        float radius = (short)subarray[3];
                        float[] circleprop = {cx,cy,radius};
                        color = subarray[4];
                        outputBuffer.addDrawObjectArray(1,circleprop,color,1);
                        break;
                    case 2://rectangle
                        int left = (short)subarray[1];
                        int top = (short)subarray[2];
                        int right = (short)subarray[3];
                        int bottom = (short)subarray[4];
                        Rect rect = new Rect(left,top,right,bottom);
                        color = subarray[5];
                        outputBuffer.addDrawObjectArray(2,rect,color,1);
                        break;
                    case 1://line
                        float sx=(short)subarray[1];
                        float sy=(short)subarray[2];
                        float ex=(short)subarray[3];
                        float ey=(short)subarray[4];
                        float[]lp = {sx,sy,ex,ey};
                        color = subarray[5];
                        width = subarray[6];
                        outputBuffer.addDrawObjectArray(3,lp,color,width);
                        break;
                    case 0://point
                        float x=(short)subarray[1];
                        float y=(short)subarray[2];
                        float[]pp = {x,y};
                        color = subarray[3];
                        width = subarray[4];
                        outputBuffer.addDrawObjectArray(4,pp,color,width);
                        break;
                    default:

                }
                break;
            case 0xFF40://音を鳴らす
                //先頭アドレス:gr7 データ数:gr6
                memory_position = register.getGr()[7];
                int sndId = context.getResources().getIdentifier(String.format("s%03d",(int)memory_position),"raw",context.getPackageName());
                try {
                final int soundOne = soundPool.load(context,sndId,1);
                soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                        soundPool.play(soundOne, 1.0f,1.0f,0,0,1);
                    }
                });

                }catch (Exception e) {
                }
                /*
                jetPlayer.loadJetFile(context.getResources().openRawResourceFd(R.raw.doremifa));
                jetPlayer.clearQueue();
                if(ontei<7){
                    jetPlayer.queueJetSegment(ontei, -1, 0, 0, 0, (byte) 0);
                    jetPlayer.play();
                }
                */
                break;
            case 0xFF20://浮動小数点数演算
                //先頭アドレス:gr7
                //有効桁数7桁 指数部-37~37
                //仮数部は4*7=28ビットで表す(2word)符号は-の時8。指数部は1word使う。
                //演算の種類gr6
                memory_position = register.getGr()[7];
                char op = register.getGr()[6];
                subarray = Arrays.copyOfRange(memory.getMemory(),memory_position,memory_position+6);
                char[] a_kasu = Arrays.copyOfRange(subarray,0,2);
                double a = getFloatFromCommet(subarray[2], a_kasu);
                char[] b_kasu = Arrays.copyOfRange(subarray,3,5);
                double b = getFloatFromCommet(subarray[5], b_kasu);
                char r_position = (char) (memory_position+6);
                float r;
                switch (op){
                    case 0x0://足し算
                        r=(float)checkFloatRange(a+b);
                        break;
                    case 0x1://引き算
                        r=(float)checkFloatRange(a-b);
                        break;
                    case 0x2://掛け算
                        r=(float)checkFloatRange(a*b);
                        break;
                    case 0x3://割り算
                        r=(float)checkFloatRange(a/b);
                        break;
                    case 0x4://べき乗
                        r=(float)checkFloatRange(Math.pow(a,b));
                        break;
                    case 0x5://正弦
                        r=(float)checkFloatRange(Math.sin(Math.toRadians(a)));
                        break;
                    case 0x6://余弦
                        r=(float)checkFloatRange(Math.cos(Math.toRadians(a)));
                        break;
                    case 0x7://正接
                        r=(float)checkFloatRange(Math.tan(Math.toRadians(a)));
                        break;
                    default:
                        r=(float)0;
                }

                char[] r_array = getFloatArray(r);
                memory.setMemoryArray(r_array,r_position);
                break;
            case 0xFF21:
                memory_position = register.getGr()[7];
                r1 = register.getGr()[6];
                memory.setMemoryArray(getFloatArray(r1),memory_position);
                break;
            case 0xFF22://浮動小数点数から16進数への変換
                //先頭アドレス:gr7
                //変換後代入先アドレス:gr6
                //仮数部は4*7=28ビットで表す(2word)。符号は-の時FXXX。指数部は1word使う。
                //メモリアドレスgr6
                memory_position = register.getGr()[7];
                char tr_positon = register.getGr()[6];
                subarray = Arrays.copyOfRange(memory.getMemory(),memory_position,memory_position+6);
                char[] a_kasu1 = Arrays.copyOfRange(subarray,0,2);
                float a1 = (float) getFloatFromCommet(subarray[2], a_kasu1);
                register.setGr((char) (a1/1),6);
                break;
            case 0xFF14://rand
                short random_max = Short.MAX_VALUE;
                short random_min = Short.MIN_VALUE;
                Random random = new Random(System.currentTimeMillis());
                short randnum = (short) (random.nextInt(random_max - random_min + 1) + random_min);
                register.setGr((char)randnum,7);
                break;
            case 0xFF0C://timer
                memory_position = register.getGr()[7];
                char sleeptime = memory.getMemory(memory_position);
                try {
                    //Thread.sleep(sleeptime);
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case 0xFF70://非同期入力（ボタン）
                /**
                 * gr7:入力を受け取るアドレス
                 * gr6:1なら表示、0なら非表示
                 * gr5:1~4で表示するボタンを選択
                 * 押したら+1する。すべてのボタンで共通。
                 * 押したらボタンに応じて1,2,3,4をアドレスに代入する。
                 */
                memory_position = register.getGr()[7];
                int visibility;
                visibility = register.getGr()[6]>0 ? Button.VISIBLE : Button.INVISIBLE;
                int buttonnum = register.getGr()[5];
                outputBuffer.setButtonconfig(buttonnum-1,visibility,memory_position);
                break;
            case 0xFFFE://プログラム終了
                break;

        }
        //FF00 FABCで文字出力できるようにする
        //実行アドレスに

        register.setPc((char) (cpc + wordCount));
    }

    @Override
    public void waitEmu() {

        if(handler!=null){
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    @Override
    public int stepOver() {
        int opCode = getOPCode();
        // TODO: 2017/11/20 2バイト分記録する（2バイト使う命令のみ)
        context.startService(new Intent(context,Casl2LogWriter.class)
                .putExtra("log",new LogSerializable(
                        "StepOver",String.format("%04X",opCode)
                )));
        int r = super.stepOver();
        context.sendBroadcast(broadcastIntent);
        return r;
    }

    @Override
    protected void showText(String txt) {
        super.showText(txt);
        Toast.makeText(context,txt,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void run(final int interval) {
        if(handler==null){

            runflag = true;
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    stepOver();

                    if(handler!=null) {
                        handler.postDelayed(this, interval);
                    }
                }
            }, interval);
        }
    }
}
