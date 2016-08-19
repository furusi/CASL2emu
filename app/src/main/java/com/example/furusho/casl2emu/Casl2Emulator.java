package com.example.furusho.casl2emu;

import android.os.Handler;

import java.util.Locale;


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
        //pcの命令をみて読み込むデータ数が決まる。
        int wordCount=0;
        char[] tmp;
        int gr_position;
        char cans;
        short sans;
        short sr1;
        short sr2;
        char cr1;
        char cr2;
        short smember;
        char jikkou;
        char cmember;
        char spaddr;
        int r_before;
        char cdata;
        short sdata;

        switch (mem1 & 0xff00) {
            case 0x0000: // NOP
            //データに基づいて処理する
                wordCount=1;
                register.setPc((char)(cpc+wordCount));
                break;
            case 0x1000: // ST
                wordCount=2;
                tmp = memory.getMemoryArray(register.getPc(),wordCount);
                cr2 = getJikkouAddress(tmp);
                gr_position = getGrNumber(tmp);
                //TODO charとshortの変換が必要
                sdata = (short) memory.getMemory(cr2);
                register.setGr((char) sdata,gr_position);
                fr[0]=0;//LDのOFは必ず0
                setRegisterAfterClaculationShort(cpc,wordCount,tmp,sdata);
                break;
            case 0x1100://ST
            wordCount=2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                char setaddr = getJikkouAddress(tmp);
                gr_position = getGrNumber(tmp);
                memory.setMemory(register.getGr()[gr_position],setaddr);
                register.setPc((char)(cpc+wordCount));
                break;
            case 0x1200://LAD
            wordCount=2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                cr2 = getJikkouAddress(tmp);
                gr_position = getGrNumber(tmp);
                register.setGr(cr2,gr_position);
                register.setPc((char)(cpc+wordCount));
                break;
            case 0x1400://LD
            wordCount=1;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);

                int r1_position = getGrNumber(tmp);
                int r2_position = getGr2Number(tmp);
                cdata = register.getGr()[r2_position];
                //計算結果はrに入る
                register.setGr(cdata,r1_position);
                fr[0]=0;//LDのOFは必ず0
                setRegisterAfterClaculationChar(cpc,wordCount,tmp,cdata);
                break;
            case 0x2000://ADDA
                //データに基づいて処理する
                wordCount=2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                cr2 = getJikkouAddress(tmp);
                //加算数を取得
                smember = (short) memory.getMemory(cr2);
                //grの中身を取得
                sr1 = (short) register.getGr()[getGrNumber(tmp)];
                adda(cpc, wordCount, tmp, sr1, smember);
                break;
            case 0x2100://SUBA
                //データに基づいて処理する
                wordCount=2;
                    tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                cr2 = getJikkouAddress(tmp);
                //減算数を取得
                smember = (short) memory.getMemory(cr2);
                //grの中身を取得
                sr1 = (short) register.getGr()[getGrNumber(tmp)];
                suba(cpc, wordCount, tmp, sr1, smember);
                break;
            case 0x2200://ADDL
                //データに基づいて処理する
                wordCount=2;
                    tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                    cr2 = getJikkouAddress(tmp);
                //grの中身を取得
                cr1 = register.getGr()[getGrNumber(tmp)];
                //加算数を取得
                cmember = memory.getMemory(cr2);
                addl(cpc, wordCount, tmp, cr1, cmember);
                break;
            case 0x2300://SUBL
                //データに基づいて処理する
                wordCount=2;
                    tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(tmp);
                //grの中身を取得
                cr1 = register.getGr()[getGrNumber(tmp)];
                //加算数を取得
                cmember = memory.getMemory(jikkou);
                subl(cpc, wordCount, tmp, cr1, cmember);
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
                cr1 = register.getGr()[getGrNumber(tmp)];
                cr2 = register.getGr()[getGr2Number(tmp)];
                addl(cpc,wordCount,tmp,cr1,cr2);
                break;
            case 0x2700://SUBL
            //データに基づいて処理する
                wordCount=1;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                cr1 = register.getGr()[getGrNumber(tmp)];
                cr2 = register.getGr()[getGr2Number(tmp)];
                subl(cpc, wordCount, tmp, cr1, cr2);
                break;
            case 0x3000://and
            //データに基づいて処理する
                wordCount=2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                jikkou = getJikkouAddress(tmp);
                //grの中身を取得
                cr1 = register.getGr()[getGrNumber(tmp)];
                //加算数を取得
                cmember = memory.getMemory(jikkou);
                cans = (char) (cr1 & cmember);
                setRegisterAfterClaculationChar(cpc,wordCount,tmp,cans);
                break;
            case 0x3100://or
                //データに基づいて処理する
                wordCount=2;
                    tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                    jikkou = getJikkouAddress(tmp);
                //grの中身を取得
                    cr1 = register.getGr()[getGrNumber(tmp)];
                //加算数を取得
                    cmember = memory.getMemory(jikkou);
                cans = (char) (cr1|cmember);
                setRegisterAfterClaculationChar(cpc,wordCount,tmp,cans);
                break;
            case 0x3200://xor
                //データに基づいて処理する
                wordCount=2;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                jikkou = getJikkouAddress(tmp);
                //grの中身を取得
                cr1 = register.getGr()[getGrNumber(tmp)];
                //加算数を取得
                cmember = memory.getMemory(jikkou);
                char ans = (char) (cr1^cmember);
                setRegisterAfterClaculationChar(cpc,wordCount,tmp,ans);
                break;
            case 0x3400://and
                //データに基づいて処理する
                wordCount=1;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                cr1 = register.getGr()[getGrNumber(tmp)];
                cr2 = register.getGr()[getGr2Number(tmp)];
                cans = (char) (cr1 & cr2);
                setRegisterAfterClaculationChar(cpc,wordCount,tmp,cans);
                break;
            case 0x3500://or
                //データに基づいて処理する
                wordCount=1;
                tmp = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                cr1 = register.getGr()[getGrNumber(tmp)];
                cr2 = register.getGr()[getGr2Number(tmp)];
                cans = (char) (cr1 | cr2);
                setRegisterAfterClaculationChar(cpc,wordCount,tmp,cans);
                break;
            case 0x3600://xor
                //データに基づいて処理する
                wordCount=1;
                tmp = new char[wordCount];
                for(int i=0;i<wordCount;i++){
                    tmp[i] = memory.getMemory(register.getPc()+i);
                }
                //xの中身を取得
                cr1 = register.getGr()[getGrNumber(tmp)];
                cr2 = register.getGr()[getGr2Number(tmp)];
                cans = (char) (cr1 ^ cr2);
                setRegisterAfterClaculationChar(cpc,wordCount,tmp,cans);
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
                cr1 = register.getGr()[getGrNumber(tmp)];
                //加算数を取得
                cmember = memory.getMemory(jikkou);
                fr[0]=0;
                getCompareResultL(cr1, cmember);
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
                cr1 = register.getGr()[getGrNumber(tmp)];
                cr2 = register.getGr()[getGr2Number(tmp)];
                fr[0]=0;
                getCompareResultL(cr1, cr2);
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
                setRegisterAfterClaculationShort(cpc,wordCount,tmp,sans);
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
                setRegisterAfterClaculationShort(cpc,wordCount,tmp,sans);
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
                cr1 = register.getGr()[getGrNumber(tmp)];
                //rの記号を保持
                r_before = cr1;
                //計算結果はrに入る
                cans= (char) checkCharRange((int)cr1<<cmember);

                //OFは最後に送り出されたビットの値
                fr[0]= (char) ((r_before>>(15-cmember))&0x0001);
                setRegisterAfterClaculationChar(cpc,wordCount,tmp,cans);
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
                cr1 = register.getGr()[getGrNumber(tmp)];
                //rの記号を保持
                r_before = cr1;
                //計算結果はrに入る
                cans= (char) checkCharRange((int)cr1>>cmember);

                //OFは最後に送り出されたビットの値
                fr[0]= (char) ((r_before>>(cmember-1))&0x0001);

                //pcが更新される
                setRegisterAfterClaculationChar(cpc,wordCount,tmp,cans);
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
                cans = register.getSp();
                register.setSp((char) (cans-1));
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
                register.setGr(memory.getMemory(spaddr),getGr2Number(tmp));
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
                cans = register.getSp();
                register.setSp((char) (cans-1));
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
                //spの指すアドレスを取得
                spaddr = register.getSp();

                register.setPc((char) (cpc + wordCount));
                break;
        }

    }

    private void subl(char cpc, int wordCount, char[] tmp, int r, int subber) {
        char ans=0;
        ans = (char) checkCharRange(r - subber);
        setRegisterAfterClaculationChar(cpc, wordCount, tmp, ans);
    }

    private void addl(char cpc, int wordCount, char[] tmp, int r, int adder) {
        char ans=0;
        ans = (char) checkCharRange(r + adder);
        setRegisterAfterClaculationChar(cpc, wordCount, tmp, ans);
    }

    private void suba(char cpc, int wordCount, char[] tmp, int r , int subber) {
        short ans=0;
        ans = (short) checkShortRange(r - subber);
        setRegisterAfterClaculationShort(cpc, wordCount, tmp, ans);
    }

    private void adda(char cpc, int wordCount, char[] tmp, int r1, int r2) {
        short ans=0;
        ans = (short) checkShortRange(r1 + r2);

        setRegisterAfterClaculationShort(cpc, wordCount, tmp, ans);
    }

    private void setRegisterAfterClaculationShort(char cpc, int wordCount, char[] tmp, short ans) {
        checkShortSfZf(ans);
        //計算結果はrに入る
        register.setGr((char)ans,getGrNumber(tmp));
        register.setFr(fr);
        //pcが更新される
        register.setPc((char)(cpc+wordCount));
    }
    private void setRegisterAfterClaculationChar(char cpc, int wordCount, char[] tmp, char ans) {
        checkCharSfZf(ans);
        //計算結果はrに入る
        register.setGr(ans,getGrNumber(tmp));
        register.setFr(fr);
        //pcが更新される
        register.setPc((char)(cpc+wordCount));
    }


    private void checkShortSfZf(short ld) {
        if(ld<0){
            fr[1]=1;
        }else if(ld==0){
            fr[2]=1;
        }
    }
    private void checkCharSfZf(char ld) {
        char d = (char) ( ld/(256*128));
        if(d==1){
            fr[1]=1;
        }else if(ld==0){
            fr[2]=1;
        }
    }

    private void getCompareResultL(char r, char jikkou) {
        if(r>jikkou){
            fr[1] = 0;
            fr[2] = 0;
        }else if (r == jikkou){
            fr[1] = 0;
            fr[2] = 1;
        }else {
            fr[1] = 1;
            fr[2] = 0;
        }
    }

    private void getCompareResultA(short r, short jikkou) {
        if(r>jikkou){
           fr[1] = 0;
           fr[2] = 0;
        }else if (r == jikkou){
            fr[1] = 0;
            fr[2] = 1;
        }else {
            fr[1] = 1;
            fr[2] = 0;
        }
    }

    private char getJikkouAddress(char[] tmp) {
        int sihyou = getGr2Number(tmp);
        char sihyou_nakami = (char) (register.getGr()[sihyou]);
        return (char) (tmp[1]+sihyou_nakami);
    }

    private boolean compareOPCode(char mem1, int opcode) {
        char i = (char) ((char)mem1>>8);
        return i == Integer.decode( "0x" + opcode );
    }
    private boolean compareOPCodeS(char mem1, String opcode) {
        char i = (char) ((char)mem1>>8);
        return i == Integer.decode( "0x" + opcode );
    }
    private int getGrNumber(char[] data){
        String s = String.format(Locale.US,"%04X",data[0] & 0xFFFF);
        return Character.digit(s.charAt(2),10);
    }
    private int getGr2Number(char[] data){
        String s = String.format(Locale.US,"%04X",data[0]& 0xFFFF);
        return Character.digit(s.charAt(3),10);
    }
    private int checkByte(char data, int position){
        String s = String.format(Locale.US,"%04X",data);
        return s.charAt(position);
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

        handler.removeCallbacksAndMessages(null);
        handler = null;
    }
    public void registerSVC (int num, String func)  /* SVC num が実行されたときに呼び出す関数 func を設定 */ {
    }
    public void unregisterSVC (int num) {
    }

}
