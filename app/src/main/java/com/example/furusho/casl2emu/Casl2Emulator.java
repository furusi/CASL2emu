package com.example.furusho.casl2emu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.JetPlayer;
import android.os.Handler;

import org.apache.commons.lang.math.RandomUtils;

import java.util.Arrays;
import java.util.Random;


/**
 * Created by furusho on 2016/07/09.
 */
public class Casl2Emulator extends EmulatorCore {
    private static Casl2Emulator instance = new Casl2Emulator();
    Casl2Memory memory = Casl2Memory.getInstance();
    static OutputBuffer outputBuffer = OutputBuffer.getInstance();
    Casl2Register register = Casl2Register.getInstance();
    Handler handler;
    char[] fr = new char[3];
    private static JetPlayer jetPlayer=JetPlayer.getJetPlayer();
    private static Context context;
    static Intent broadcastIntent= new Intent();

    private Casl2Emulator() {
    }

   static public Casl2Emulator getInstance(Context context1){
       if(context==null) {
           context = context1;
           jetPlayer.loadJetFile(context.getResources().openRawResourceFd(R.raw.doremifa));
           broadcastIntent.setAction(context.getString(R.string.action_view_invalidate));
           outputBuffer.setCasl2PaintView(context);
       }
      return instance;
   }

    public int getXX (){ /* レジスタXXを読み出す */
        return 1;
    }
    public void setXX (int val){ /* レジスタXXを設定する */

    }
    public void stepOver(){
        //pcの指すメモリの中身をを見る
        char cpc = register.getPc(); char mem1 = memory.getMemory(cpc);
        fr[0]=0; fr[1]=0; fr[2]=0;
        //pcの命令をみて読み込むデータ数が決まる。
        int wordCount=0;
        char[] tmp;
        int r1_position;
        short sans;
        short sr1;
        short sr2;
        char r1;
        char r2;
        short smember;
        char jikkou;
        char cmember;
        char spaddr;
        int r_before;
        char data;

        switch (mem1 & 0xff00) {
            case 0x0000: // NOP
            //データに基づいて処理する
                wordCount=1;
                register.setPc((char)(cpc+wordCount));
                break;
            case 0x1000: // LD
                wordCount=2;
                tmp = memory.getMemoryArray(register.getPc(),wordCount);
                r2 = getJikkouAddress(tmp);
                r1_position = getGrNumber(tmp);
                data = memory.getMemory(r2);
                register.setGr(data,r1_position);
                fr[0]=0;//LDのOFは必ず0
                setRegisterAfterClaculation(cpc,wordCount,tmp,data);
                break;
            case 0x1100://ST
            wordCount=2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                char setaddr = getJikkouAddress(tmp);
                r1_position = getGrNumber(tmp);
                memory.setMemory(register.getGr()[r1_position],setaddr);
                register.setPc((char)(cpc+wordCount));
                break;
            case 0x1200://LAD
            wordCount=2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                r2 = getJikkouAddress(tmp);
                r1_position = getGrNumber(tmp);
                register.setGr(r2,r1_position);
                register.setPc((char)(cpc+wordCount));
                break;
            case 0x1400://LD
            wordCount=1;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);

                r1_position = getGrNumber(tmp);
                int r2_position = getGr2Number(tmp);
                data = register.getGr()[r2_position];
                //計算結果はrに入る
                register.setGr(data,r1_position);
                fr[0]=0;//LDのOFは必ず0
                setRegisterAfterClaculation(cpc,wordCount,tmp,data);
                break;
            case 0x2000://ADDA
                //データに基づいて処理する
                wordCount=2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                r2 = getJikkouAddress(tmp);
                //加算数を取得
                smember = (short) memory.getMemory(r2);
                //grの中身を取得
                sr1 = (short) register.getGr()[getGrNumber(tmp)];
                adda(cpc, wordCount, tmp, sr1, smember);
                break;
            case 0x2100://SUBA
                //データに基づいて処理する
                wordCount=2;
                    tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                r2 = getJikkouAddress(tmp);
                //減算数を取得
                smember = (short) memory.getMemory(r2);
                //grの中身を取得
                sr1 = (short) register.getGr()[getGrNumber(tmp)];
                suba(cpc, wordCount, tmp, sr1, smember);
                break;
            case 0x2200://ADDL
                //データに基づいて処理する
                wordCount=2;
                    tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                    r2 = getJikkouAddress(tmp);
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(tmp)];
                //加算数を取得
                cmember = memory.getMemory(r2);
                addl(cpc, wordCount, tmp, r1, cmember);
                break;
            case 0x2300://SUBL
                //データに基づいて処理する
                wordCount=2;
                    tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(tmp);
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(tmp)];
                //加算数を取得
                cmember = memory.getMemory(jikkou);
                subl(cpc, wordCount, tmp, r1, cmember);
                break;
            case 0x2400://ADDA
                //データに基づいて処理する
                wordCount=1;
                    tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                sr1 = (short) register.getGr()[getGrNumber(tmp)];
                sr2 = (short) register.getGr()[getGr2Number(tmp)];
                adda(cpc, wordCount, tmp, sr1, sr2);
                break;
            case 0x2500://SUBA
                //データに基づいて処理する
                wordCount=1;
                    tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                    sr1 = (short) register.getGr()[getGrNumber(tmp)];
                    sr2 = (short) register.getGr()[getGr2Number(tmp)];
                suba(cpc, wordCount, tmp, sr1, sr2);
                break;
            case 0x2600://ADDL
            //データに基づいて処理する
                wordCount=1;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
            //xの中身を取得
                r1 = register.getGr()[getGrNumber(tmp)];
                r2 = register.getGr()[getGr2Number(tmp)];
                addl(cpc,wordCount,tmp,r1,r2);
                break;
            case 0x2700://SUBL
            //データに基づいて処理する
                wordCount=1;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                r1 = register.getGr()[getGrNumber(tmp)];
                r2 = register.getGr()[getGr2Number(tmp)];
                subl(cpc, wordCount, tmp, r1, r2);
                break;
            case 0x3000://and
            //データに基づいて処理する
                wordCount=2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                jikkou = getJikkouAddress(tmp);
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(tmp)];
                //加算数を取得
                cmember = memory.getMemory(jikkou);
                data = (char) (r1 & cmember);
                setRegisterAfterClaculation(cpc,wordCount,tmp,data);
                break;
            case 0x3100://or
                //データに基づいて処理する
                wordCount=2;
                    tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                    jikkou = getJikkouAddress(tmp);
                //grの中身を取得
                    r1 = register.getGr()[getGrNumber(tmp)];
                //加算数を取得
                    cmember = memory.getMemory(jikkou);
                data = (char) (r1|cmember);
                setRegisterAfterClaculation(cpc,wordCount,tmp,data);
                break;
            case 0x3200://xor
                //データに基づいて処理する
                wordCount=2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                jikkou = getJikkouAddress(tmp);
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(tmp)];
                //加算数を取得
                cmember = memory.getMemory(jikkou);
                data = (char) (r1 ^ cmember);
                setRegisterAfterClaculation(cpc,wordCount,tmp,data);
                break;
            case 0x3400://and
                //データに基づいて処理する
                wordCount=1;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                r1 = register.getGr()[getGrNumber(tmp)];
                r2 = register.getGr()[getGr2Number(tmp)];
                data = (char) (r1 & r2);
                setRegisterAfterClaculation(cpc,wordCount,tmp,data);
                break;
            case 0x3500://or
                //データに基づいて処理する
                wordCount=1;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                r1 = register.getGr()[getGrNumber(tmp)];
                r2 = register.getGr()[getGr2Number(tmp)];
                data = (char) (r1 | r2);
                setRegisterAfterClaculation(cpc,wordCount,tmp,data);
                break;
            case 0x3600://xor
                //データに基づいて処理する
                wordCount=1;
                tmp = new char[wordCount];
                for(int i=0;i<wordCount;i++){
                    tmp[i] = memory.getMemory(register.getPc()+i);
                }
                //xの中身を取得
                r1 = register.getGr()[getGrNumber(tmp)];
                r2 = register.getGr()[getGr2Number(tmp)];
                data = (char) (r1 ^ r2);
                setRegisterAfterClaculation(cpc,wordCount,tmp,data);
                break;
            case 0x4000://CPA
                //データに基づいて処理する
                wordCount=2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(tmp);
                //加算数を取得
                smember = (short) memory.getMemory(jikkou);
                //grの中身を取得
                sr1 = (short) register.getGr()[getGrNumber(tmp)];
                fr[0]=0;
                getCompareResultA(sr1, smember);
                //pcが更新される
                register.setPc((char)(cpc+wordCount));
                break;
            case 0x4100://CPL
                //データに基づいて処理する
                wordCount=2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                jikkou = getJikkouAddress(tmp);
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(tmp)];
                //加算数を取得
                cmember = memory.getMemory(jikkou);
                fr[0]=0;
                getCompareResultL(r1, cmember);
                //pcが更新される
                register.setPc((char)(cpc+wordCount));
                break;
            case 0x4400://CPA
                //データに基づいて処理する
                wordCount=1;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                sr1 = (short) register.getGr()[getGrNumber(tmp)];
                sr2 = (short) register.getGr()[getGr2Number(tmp)];
                fr[0]=0;
                getCompareResultA(sr1, sr2);
                //pcが更新される
                register.setPc((char)(cpc+wordCount));
                break;
            case 0x4500://CPL
                //データに基づいて処理する
                wordCount=1;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                r1 = register.getGr()[getGrNumber(tmp)];
                r2 = register.getGr()[getGr2Number(tmp)];
                fr[0]=0;
                getCompareResultL(r1, r2);
                //pcが更新される
                register.setPc((char)(cpc+wordCount));
                break;
            case 0x5000://SLA
                //データに基づいて処理する
                wordCount=2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(tmp);
                //加算数を取得
                smember = (short) memory.getMemory(jikkou);
                //grの中身を取得
                sr1 = (short) register.getGr()[getGrNumber(tmp)];
                //rの記号を保持
                r_before = sr1;
                //計算結果はrに入る
                sans= (short) checkShortRange((int)sr1<<smember);

                if(r_before * sans<0){//符号が変わっていれば元に戻す
                    sans= (short) (sans^0x8000);
                }

                //OFは最後に送り出されたビットの値
                fr[0]= (char) ((r_before>>(15-smember))&0x0001);
                setRegisterAfterClaculation(cpc,wordCount,tmp, (char) sans);
                break;
            case 0x5100://SRA
                //データに基づいて処理する
                wordCount=2;
                    tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(tmp);
                //加算数を取得
                smember = (short) memory.getMemory(jikkou);
                //grの中身を取得
                sr1 = (short) register.getGr()[getGrNumber(tmp)];
                //rの記号を保持
                r_before = sr1;
                //計算結果はrに入る
                sans= (short) checkShortRange((int)sr1>>>smember);
                //OFは最後に送り出されたビットの値
                fr[0]= (char) ((r_before>>(smember-1))&0x0001);

                //pcが更新される
                setRegisterAfterClaculation(cpc,wordCount,tmp, (char) sans);
                break;
            case 0x5200://SLA
                //データに基づいて処理する
                wordCount=2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(tmp);
                //加算数を取得
                cmember = memory.getMemory(jikkou);
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(tmp)];
                //rの記号を保持
                r_before = r1;
                //計算結果はrに入る
                data= (char) checkCharRange((int)r1<<cmember);

                //OFは最後に送り出されたビットの値
                fr[0]= (char) ((r_before>>(15-cmember))&0x0001);
                setRegisterAfterClaculation(cpc,wordCount,tmp,data);
                break;
            case 0x5300://SRA
                //データに基づいて処理する
                wordCount=2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(tmp);
                //加算数を取得
                cmember = memory.getMemory(jikkou);
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(tmp)];
                //rの記号を保持
                r_before = r1;
                //計算結果はrに入る
                data= (char) checkCharRange((int)r1>>cmember);

                //OFは最後に送り出されたビットの値
                fr[0]= (char) ((r_before>>(cmember-1))&0x0001);

                //pcが更新される
                setRegisterAfterClaculation(cpc,wordCount,tmp,data);
                break;
            case 0x6100://JMI
                //データに基づいて処理する
                wordCount=2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(tmp);

                //SFが1であれば実行アドレスをPCに代入
                if(fr[1]==1){
                    register.setPc(jikkou);
                }else {//0ならば次へ進む
                    register.setPc((char)(cpc+wordCount));
                }
                break;
            case 0x6200://JNZ
                //データに基づいて処理する
                wordCount = 2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(tmp);

                //ZFが0であれば実行アドレスをPCに代入
                if (fr[2] == 0) {
                    register.setPc(jikkou);
                } else {//1ならば次へ進む
                    register.setPc((char) (cpc + wordCount));
                }
                break;
            case 0x6300://JZE
                //データに基づいて処理する
                wordCount = 2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(tmp);

                //ZFが1であれば実行アドレスをPCに代入
                if (fr[2] == 1) {
                    register.setPc(jikkou);
                } else {//0ならば次へ進む
                    register.setPc((char) (cpc + wordCount));
                }
                break;
            case 0x6400://JUMP
                //データに基づいて処理する
                wordCount = 2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(tmp);

                //無条件で飛ぶ
                register.setPc(jikkou);
                break;
            case 0x6500://JPL
                //データに基づいて処理する
                wordCount = 2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(tmp);

                //SFZFがともに1であれば実行アドレスをPCに代入
                if (fr[1]==1&&fr[2] == 1) {
                    register.setPc(jikkou);
                } else {//0ならば次へ進む
                    register.setPc((char) (cpc + wordCount));
                }
                break;
            case 0x6600://JOV
                //データに基づいて処理する
                wordCount = 2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(tmp);

                //OFが1であれば実行アドレスをPCに代入
                if (fr[0] == 1) {
                    register.setPc(jikkou);
                } else {//0ならば次へ進む
                    register.setPc((char) (cpc + wordCount));
                }
                break;
            case 0x7000://PUSH
                //データに基づいて処理する
                wordCount = 2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(tmp);

                //SPが指す値を1ひいてSPに入れる
                data = register.getSp();
                register.setSp((char) (data-1));
                //SPの指すアドレスへ実行アドレスを入れる
                memory.setMemory(jikkou,register.getSp());
                register.setPc((char) (cpc + wordCount));
                break;
            case 0x7100://POP
                //データに基づいて処理する
                wordCount = 1;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //spの指すアドレスを取得
                spaddr = register.getSp();
                //そのアドレスが指す値をgrへ格納
                register.setGr(memory.getMemory(spaddr),getGrNumber(tmp));
                //spに1を加算して格納
                register.setSp((char) (spaddr+1));

                register.setPc((char) (cpc + wordCount));
                break;
            case 0x8000://CALL
                //データに基づいて処理する
                wordCount = 2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(tmp);

                //SPが指す値を1ひいてSPに入れる
                data = register.getSp();
                register.setSp((char) (data-1));
                //SPの指すアドレスへPCを入れる
                memory.setMemory(register.getPc(),register.getSp());
                //PCへ実行アドレスを入れる
                register.setPc(jikkou);
                break;
            case 0x8100://RET
                //データに基づいて処理する
                wordCount = 1;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //spの指すアドレスを取得
                spaddr = register.getSp();
                //そのアドレスが指す値をPCへ格納
                register.setPc(memory.getMemory(spaddr));
                //spに1を加算して格納
                register.setSp((char) (spaddr+1));

                break;
            case 0xF000://SVC
                //データに基づいて処理する
                wordCount = 2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                jikkou = getJikkouAddress(tmp);
                char memory_position;
                char count;
                char[] subarray;
                //spの指すアドレスを取得
                switch(jikkou){
                    case 0xFF00://OUT
                        //r7を文字数(wordの数ではない)、r6を先頭アドレスとする。
                        memory_position = register.getGr()[7];
                        count = register.getGr()[6];
                        //文字数分のデータを読み取りStringに変換。
                        subarray = Arrays.copyOfRange(memory.getMemory(),memory_position,memory_position+count);
                        byte[] bytes = new byte[subarray.length*2];
                        for(int i =0;i<subarray.length;i++){

                            byte[] _bytes = new byte[2];
                            _bytes[0] = (byte) (subarray[i]>>8);
                            _bytes[1] = (byte) (subarray[i]&0x00FF);
                            for(int j=0;j<2;j++){
                                if((_bytes[j]>=0x20&&_bytes[j]<=0x7E)||_bytes[j]==0x0a){
                                   bytes[2*i+j]=_bytes[j];
                                }
                            }
                        }
                        outputBuffer.addData(new String(bytes));
                        break;
                    case 0xFF02://描画
                        //先頭アドレス:gr7
                        memory_position = register.getGr()[7];
                        int color;
                        int width;
                        subarray = Arrays.copyOfRange(memory.getMemory(),memory_position,memory_position+7);
                        switch (subarray[0]){//種類別の処理
                            case 1://circle
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
                            case 3://line
                                float sx=(short)subarray[1];
                                float sy=(short)subarray[2];
                                float ex=(short)subarray[3];
                                float ey=(short)subarray[4];
                                float[]lp = {sx,sy,ex,ey};
                                color = subarray[5];
                                width = subarray[6];
                                outputBuffer.addDrawObjectArray(3,lp,color,width);
                                break;
                            case 4://point
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
                    case 0xFF04://音を鳴らす
                        //先頭アドレス:gr7 データ数:gr6
                        memory_position = register.getGr()[7];
                        count = register.getGr()[6];
                        subarray = Arrays.copyOfRange(memory.getMemory(),memory_position,memory_position+count);
                        jetPlayer.loadJetFile(context.getResources().openRawResourceFd(R.raw.doremifa));
                        jetPlayer.clearQueue();
                        for(int i =0;i<subarray.length;i++){
                            //outputBuffer.getSoundList().add(new SoundDto(generateSound(outputBuffer.getSoundGenerator(),subarray[2*i], subarray[2*i+1]), subarray[2*i+1]));

                            jetPlayer.queueJetSegment(subarray[i], -1, 0, 0, 0, (byte) 0);
                            jetPlayer.play();
                        }
                        break;
                    case 0xFF06://浮動小数点数演算
                        //先頭アドレス:gr7
                        //有効桁数7桁 指数部-37~37
                        //仮数部は4*7=28ビットで表す(2word)符号は-の時8。指数部は1word使う。
                        //演算の種類gr6
                        memory_position = register.getGr()[7];
                        char op = register.getGr()[6];
                        subarray = Arrays.copyOfRange(memory.getMemory(),memory_position,memory_position+6);
                        char[] a_kasu = Arrays.copyOfRange(subarray,0,2);
                        double a = getFloat(subarray[2], a_kasu);
                        char[] b_kasu = Arrays.copyOfRange(subarray,3,5);
                        double b = getFloat(subarray[5], b_kasu);
                        char r_position = (char) (memory_position+7);
                        float r;
                        switch (op){
                            case 1://足し算
                                r=(float)checkFloatRange(a+b);
                                break;
                            case 2://引き算
                                r=(float)checkFloatRange(a-b);
                                break;
                            case 3://掛け算
                                r=(float)checkFloatRange(a*b);
                                break;
                            case 4://割り算
                                r=(float)checkFloatRange(a/b);
                                break;
                            case 5://べき乗
                                r=(float)checkFloatRange(Math.pow(a,b));
                                break;
                            case 6://正弦
                                r=(float)checkFloatRange(Math.sin(a));
                                break;
                            case 7://余弦
                                r=(float)checkFloatRange(Math.cos(a));
                                break;
                            case 8://正接
                                r=(float)checkFloatRange(Math.tan(a));
                                break;
                            default:
                                r=(float)0;
                        }

                        char sign=0;
                        if (r < 0){
                            sign = 8;
                            r= Math.abs(r);
                        }
                        short r_sisu=0;

                        float abs_r =Math.abs(r);
                       if(abs_r<1) {
                           for (short i = 0; i < 37; i++) {
                               if (Math.abs(r) >= 1) {
                                   r_sisu = (short) (i*-1);
                                   break;
                               }else{
                                   r = r * 10;

                               }
                           }
                       }else if (abs_r>=10){
                           for (short i = 0; i < 37; i++) {
                               if (Math.abs(r) < 10){
                                   r_sisu = i;
                                   break;
                               }else {
                                   r = r / 10;
                               }
                           }
                       }

                        char[] r_array = new char[3];
                        char[] _r={48,48,48,48,48,48,48,48};
                        char[] cs = String.valueOf(r).toCharArray();
                        for(int i=0;i<cs.length;i++){
                            _r[i]=cs[i];
                        }
                        //'0'=48,'1'=49   '1'-48=1
                        r_array[0]= (char) ((sign <<12) + ((_r[0]-48) <<8) +((_r[2]-48)<<4)+((_r[3]-48)));
                        r_array[1]= (char) (((_r[4]-48) <<12) + ((_r[5]-48) <<8) +((_r[6]-48)<<4)+((_r[7]-48)));
                        r_array[2]= (char) r_sisu;
                        memory.setMemoryArray(r_array,r_position);
                        break;
                    case 0xFF08://浮動小数点数変換
                        //先頭アドレス:gr7
                        //変換後代入先アドレス:gr6
                        //仮数部は4*7=28ビットで表す(2word)。符号は-の時F。指数部は1word使う。
                        //演算の種類gr6
                        memory_position = register.getGr()[7];
                        char tr_positon = register.getGr()[6];
                        subarray = Arrays.copyOfRange(memory.getMemory(),memory_position,memory_position+6);
                        char[] a_kasu1 = Arrays.copyOfRange(subarray,0,2);
                        double a1 = getFloat(subarray[2], a_kasu1);
                        memory.setMemory((char) (a1/1),tr_positon);
                        break;
                    case 0xFF0A://rand
                        memory_position = register.getGr()[7];
                        char random_max = register.getGr()[6];
                        char random_min = register.getGr()[5];
                        if(random_max<random_min) {
                            Random random = new Random(System.currentTimeMillis());
                            short randnum = (short) (random.nextInt(random_max - random_min + 1) + random_min);
                            memory.setMemory((char) randnum, memory_position);
                        }
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
                    case 0xFF0E://input
                        memory_position = register.getGr()[7];

                }
                //FF00 FABCで文字出力できるようにする
                //実行アドレスに

                register.setPc((char) (cpc + wordCount));
                break;
            default:
        }
        context.sendBroadcast(broadcastIntent);

    }

    private double getFloat(char c, char[] a_kasu) {
        int[] _array = new int[7];
        int sign=1;
        if((a_kasu[0] >> 12) == 0xF){
           sign = -1;
        }
        _array[0]= (a_kasu[0]&0x0F00)>>8;
        _array[1]= (a_kasu[0]&0x00F0)>>4;
        _array[2]= a_kasu[0]&0x000F;
        _array[3]= (a_kasu[1]&0xF000)>>12;
        _array[4]= (a_kasu[1]&0x0F00)>>8;
        _array[5]= (a_kasu[1]&0x00F0)>>4;
        _array[6]= a_kasu[1]&0x000F;
        short a_sisu = (short)c;
        double flt = (double) ((_array[0]+_array[1]*0.1+_array[2]*0.01+_array[3]*0.001+_array[4]*0.0001+_array[5]*0.00001+_array[6]*0.000001)*sign*(Math.pow(10,a_sisu)));
        return flt;
    }

    private void subl(char cpc, int wordCount, char[] tmp, int r, int member) {
        char ans=0;
        ans = (char) checkCharRange(r - member);
        setRegisterAfterClaculation(cpc, wordCount, tmp, ans);
    }

    private void addl(char cpc, int wordCount, char[] tmp, int r, int member) {
        char ans=0;
        ans = (char) checkCharRange(r + member);
        setRegisterAfterClaculation(cpc, wordCount, tmp, ans);
    }

    private void suba(char cpc, int wordCount, char[] tmp, int r , int member) {
        short ans=0;
        ans = (short) checkShortRange(r - member);
        setRegisterAfterClaculation(cpc, wordCount, tmp, (char) ans);
    }

    private void adda(char cpc, int wordCount, char[] tmp, int r1, int r2) {
        short ans=0;
        ans = (short) checkShortRange(r1 + r2);

        setRegisterAfterClaculation(cpc, wordCount, tmp, (char) ans);
    }

    private void setRegisterAfterClaculation(char cpc, int wordCount, char[] tmp, char ans) {
        checkSfZf(ans);
        //計算結果はrに入る
        register.setGr(ans,getGrNumber(tmp));
        register.setFr(fr);
        //pcが更新される
        register.setPc((char)(cpc+wordCount));
    }


    private void checkSfZf(char ld) {
        int d =   (ld>>15);
        if(d==1){
            fr[1]=1;
        }else if(ld==0x0000){
            fr[2]=1;
        }
    }

    private void getCompareResultL(char r, char jikkou) {
        if(r>jikkou){
            fr[1] = 0; fr[2] = 0;
        }else if (r == jikkou){
            fr[1] = 0; fr[2] = 1;
        }else {
            fr[1] = 1; fr[2] = 0;
        }
    }

    private void getCompareResultA(short r, short jikkou) {
        if(r>jikkou){
            fr[1] = 0; fr[2] = 0;
        }else if (r == jikkou){
            fr[1] = 0; fr[2] = 1;
        }else {
            fr[1] = 1; fr[2] = 0;
        }
    }

    private char getJikkouAddress(char[] tmp) {
        int sihyou = getGr2Number(tmp);
        char sihyou_nakami = register.getGr()[sihyou];
        return (char) ((int)tmp[1]+(int)sihyou_nakami);
    }

    private int getGrNumber(char[] data){
        return (data[0]>>4) & 0x000F;
    }
    private int getGr2Number(char[] data){
        return data[0] & 0x000F;
    }
    private long checkShortRange(int value){
        if(value > Short.MAX_VALUE||value < Short.MIN_VALUE)
            fr[0]=1;
        return value;
    }
    private long checkCharRange(int value){
        if(value > Character.MAX_VALUE||value < Character.MIN_VALUE)
            fr[0]=1;
        return value;
    }
    private double checkFloatRange(double value){
        if(value > Float.MAX_VALUE||value < Float.MIN_VALUE)
            fr[0]=1;
        return value;
    }


    public void run(){
        if(handler==null){

            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    stepOver();
                    handler.postDelayed(this,1000);
                }
            }, 1000);
        }
    }

    public void waitEmu(){

        if(handler!=null){
        handler.removeCallbacksAndMessages(null);
        handler = null;
        }
    }

      public byte[] generateSound(Casl2SoundGenerator gen, int freq, int length) {
    return gen.getSound(freq, length);
  }

  /**
   * 無音データを作成する
   * @param gen Generator
   * @param length 無音データの長さ
   * @return 無音データ
   */
  public byte[] generateEmptySound(Casl2SoundGenerator gen, int length) {
    return gen.getEmptySound(length);
  }
    private char[] getHexChars(String s,String separeter) {
        String[] stmp = s.split(separeter);
        char[] tmp= new char[stmp.length];
        for(int i=0;i<stmp.length;i++){
            tmp[i] = (char)Integer.parseInt(stmp[i],16);
        }
        return tmp;
    }

}
