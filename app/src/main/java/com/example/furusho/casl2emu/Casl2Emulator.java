package com.example.furusho.casl2emu;

import android.app.AlarmManager;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by furusho on 2016/07/09.
 */
public class Casl2Emulator extends EmulatorCore {
    private static Casl2Emulator instance = new Casl2Emulator();
    Casl2Memory memory = Casl2Memory.getInstance();
    Casl2Register register = Casl2Register.getInstance();
    Handler handler;
    char[] fr = new char[3];

    private Casl2Emulator() {
    }

   static public Casl2Emulator getInstance(){
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
                OutputBuffer outputBuffer = OutputBuffer.getInstance();
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                jikkou = getJikkouAddress(tmp);
                char memory_position;
                char count;
                char[] subarray;
                //spの指すアドレスを取得
                switch(jikkou){
                    case 0xFF00://OUT
                        //r7を文字数(wordの数ではない)、r6を先頭アドレスとする。
                        memory_position = register.getGr()[6];
                        count = register.getGr()[7];
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
                    case 0xFF01://描画
                        //先頭アドレス:gr7
                        memory_position = register.getGr()[7];
                        int color;
                        subarray = Arrays.copyOfRange(memory.getMemory(),memory_position,memory_position+6);
                        switch (subarray[0]){//種類別の処理
                            case 1://circle
                                float cx = (short)subarray[1];
                                float cy = (short)subarray[2];
                                float radius = (short)subarray[3];
                                float[] circleprop = {cx,cy,radius};
                                color = subarray[4];
                                outputBuffer.addDrawObjectArray(1,circleprop,color);
                                break;
                            case 2://rectangle
                                int left = (short)subarray[1];
                                int top = (short)subarray[2];
                                int right = (short)subarray[3];
                                int bottom = (short)subarray[4];
                                Rect rect = new Rect(left,top,right,bottom);
                                color = subarray[5];
                                outputBuffer.addDrawObjectArray(2,rect,color);
                                break;

                        }
                    case 0xFF02://音を鳴らす
                        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM,ToneGenerator.MAX_VOLUME);
                        toneGenerator.startTone(ToneGenerator.TONE_DTMF_0,2000);
                }
                //FF00 FABCで文字出力できるようにする
                //実行アドレスに

                register.setPc((char) (cpc + wordCount));
                break;
        }

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
    public void registerSVC (int num, String func)  /* SVC num が実行されたときに呼び出す関数 func を設定 */ {
    }
    public void unregisterSVC (int num) {
    }

}
