package com.example.furusho.casl2emu;

import java.util.Locale;

/**
 * Created by furusho on 2016/07/09.
 */
public class Casl2Emulator extends EmulatorCore {
    private static Casl2Emulator instance = new Casl2Emulator();
    Casl2Memory memory = Casl2Memory.getInstance();
    Casl2Register register = Casl2Register.getInstance();
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

        if(compareOPCode(mem1, 00)){//NOP
            //データに基づいて処理する
            wordCount=1;
            register.setPc((char)(cpc+wordCount));
        }else if(compareOPCode(mem1, 10)){//LD
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            char jikkouaddr = getDataAtJikkouAddress(tmp);
            int gr_position= getGrNumber(tmp);
            short ld = (short) memory.getMemory(jikkouaddr);
            register.setGr((char) ld,gr_position);
            fr[0]=0;//LDのOFは必ず0
            setRegisterAfterClaculationShort(cpc,wordCount,tmp,ld);
        }else if(compareOPCode(mem1, 11)){//ST
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //実行アドレスを取得
            char setaddr = getJikkouAddress(tmp);
            int gr_position= getGrNumber(tmp);
            memory.setMemory(register.getGr()[gr_position],setaddr);
            register.setPc((char)(cpc+wordCount));

        }else if(compareOPCode(mem1, 12)){//LAD
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            char jikkouaddr = getDataAtJikkouAddress(tmp);
            int gr_position= getGrNumber(tmp);
            register.setGr(jikkouaddr,gr_position);
            register.setPc((char)(cpc+wordCount));
        }else if(compareOPCode(mem1, 14)){//LD
            wordCount=1;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);

            int r1_position = getGrNumber(tmp);
            int r2_position = getGr2Number(tmp);
            char ld = register.getGr()[r2_position];
            //計算結果はrに入る
            register.setGr(ld,r1_position);
            fr[0]=0;//LDのOFは必ず0
            setRegisterAfterClaculationChar(cpc,wordCount,tmp,ld);
        } else if(compareOPCode(mem1, 20)){//ADDA
            //データに基づいて処理する
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //実行アドレスを取得
            char jikkou = getJikkouAddress(tmp);
            //加算数を取得
            short adder = (short) memory.getMemory(jikkou);
            //grの中身を取得
            short r = (short) register.getGr()[getGrNumber(tmp)];
            adda(cpc, wordCount, tmp, r, adder);
        } else if(compareOPCode(mem1, 21)){//SUBA
            //データに基づいて処理する
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char jikkou = getJikkouAddress(tmp);
            //減算数を取得
            short subber = (short) memory.getMemory(jikkou);
            //grの中身を取得
            short r = (short) register.getGr()[getGrNumber(tmp)];
            suba(cpc, wordCount, tmp, r, subber);
        } else if(compareOPCode(mem1, 22)){//ADDL
            //データに基づいて処理する
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //実行アドレスを取得
            char jikkou = getJikkouAddress(tmp);
            //grの中身を取得
            char r = register.getGr()[getGrNumber(tmp)];
            //加算数を取得
            char adder = memory.getMemory(jikkou);
            addl(cpc, wordCount, tmp, r, adder);
        } else if(compareOPCode(mem1, 23)){//SUBL
            //データに基づいて処理する
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //実行アドレスを取得
            char jikkou = getJikkouAddress(tmp);
            //grの中身を取得
            char r = register.getGr()[getGrNumber(tmp)];
            //加算数を取得
            char subber = memory.getMemory(jikkou);
            subl(cpc, wordCount, tmp, r, subber);
        }else if(compareOPCode(mem1, 24)){//ADDA
            //データに基づいて処理する
            wordCount=1;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            short r1 = (short) register.getGr()[getGrNumber(tmp)];
            short r2 = (short) register.getGr()[getGr2Number(tmp)];
            adda(cpc, wordCount, tmp, r1, r2);
        }else if(compareOPCode(mem1, 25)){//SUBA
            //データに基づいて処理する
            wordCount=1;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            short r1 = (short) register.getGr()[getGrNumber(tmp)];
            short r2 = (short) register.getGr()[getGr2Number(tmp)];
            suba(cpc, wordCount, tmp, r1, r2);
        }else if(compareOPCode(mem1, 26)){//ADDL
            //データに基づいて処理する
            wordCount=1;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char r1 = register.getGr()[getGrNumber(tmp)];
            char r2 = register.getGr()[getGr2Number(tmp)];
            addl(cpc,wordCount,tmp,r1,r2);
        }else if(compareOPCode(mem1, 27)){//SUBL
            //データに基づいて処理する
            wordCount=1;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char r1 = register.getGr()[getGrNumber(tmp)];
            char r2 = register.getGr()[getGr2Number(tmp)];
            subl(cpc, wordCount, tmp, r1, r2);
        } else if(compareOPCode(mem1, 30)){//and
            //データに基づいて処理する
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char jikkou = getJikkouAddress(tmp);
            //grの中身を取得
            char r = register.getGr()[getGrNumber(tmp)];
            //加算数を取得
            char ander = memory.getMemory(jikkou);
            char ans = (char) (r&ander);
            setRegisterAfterClaculationChar(cpc,wordCount,tmp,ans);
        } else if(compareOPCode(mem1, 31)){//or
            //データに基づいて処理する
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char jikkou = getJikkouAddress(tmp);
            //grの中身を取得
            char r = register.getGr()[getGrNumber(tmp)];
            //加算数を取得
            char orer = memory.getMemory(jikkou);
            char ans = (char) (r|orer);
            setRegisterAfterClaculationChar(cpc,wordCount,tmp,ans);
        } else if(compareOPCode(mem1, 32)){//xor
            //データに基づいて処理する
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char jikkou = getJikkouAddress(tmp);
            //grの中身を取得
            char r = register.getGr()[getGrNumber(tmp)];
            //加算数を取得
            char xorer = memory.getMemory(jikkou);
            char ans = (char) (r^xorer);
            setRegisterAfterClaculationChar(cpc,wordCount,tmp,ans);
        }else if(compareOPCode(mem1, 34)){//and
            //データに基づいて処理する
            wordCount=1;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char r1 = register.getGr()[getGrNumber(tmp)];
            char r2 = register.getGr()[getGr2Number(tmp)];
            char ans = (char) (r1&r2);
            setRegisterAfterClaculationChar(cpc,wordCount,tmp,ans);
        }else if(compareOPCode(mem1, 35)){//or
            //データに基づいて処理する
            wordCount=1;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char r1 = register.getGr()[getGrNumber(tmp)];
            char r2 = register.getGr()[getGr2Number(tmp)];
            char ans = (char) (r1|r2);
            setRegisterAfterClaculationChar(cpc,wordCount,tmp,ans);
        }else if(compareOPCode(mem1, 36)){//xor
            //データに基づいて処理する
            wordCount=1;
            char[] tmp = new char[wordCount];
            for(int i=0;i<wordCount;i++){
                tmp[i] = memory.getMemory(register.getPc()+i);
            }
            //xの中身を取得
            char r1 = register.getGr()[getGrNumber(tmp)];
            char r2 = register.getGr()[getGr2Number(tmp)];
            char ans = (char) (r1^r2);
            setRegisterAfterClaculationChar(cpc,wordCount,tmp,ans);
        } else if(compareOPCode(mem1, 40)){//CPA
            //データに基づいて処理する
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //実行アドレスを取得
            char jikkou = getJikkouAddress(tmp);
            //加算数を取得
            short cpa = (short) memory.getMemory(jikkou);
            //grの中身を取得
            short r = (short) register.getGr()[getGrNumber(tmp)];
            fr[0]=0;
            getCompareResultA(r, cpa);
            //pcが更新される
            register.setPc((char)(cpc+wordCount));
        } else if(compareOPCode(mem1, 41)){//CPL
            //データに基づいて処理する
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char jikkou = getJikkouAddress(tmp);
            //grの中身を取得
            char r = register.getGr()[getGrNumber(tmp)];
            //加算数を取得
            char cpl = memory.getMemory(jikkou);
            fr[0]=0;
            getCompareResultL(r, cpl);
            //pcが更新される
            register.setPc((char)(cpc+wordCount));
        } else if(compareOPCode(mem1, 44)){//CPA
            //データに基づいて処理する
            wordCount=1;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            short r1 = (short) register.getGr()[getGrNumber(tmp)];
            short r2 = (short) register.getGr()[getGr2Number(tmp)];
            fr[0]=0;
            getCompareResultA(r1, r2);
            //pcが更新される
            register.setPc((char)(cpc+wordCount));
        } else if(compareOPCode(mem1, 45)){//CPL
            //データに基づいて処理する
            wordCount=1;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char r1 = register.getGr()[getGrNumber(tmp)];
            char r2 = register.getGr()[getGr2Number(tmp)];
            fr[0]=0;
            getCompareResultL(r1, r2);
            //pcが更新される
            register.setPc((char)(cpc+wordCount));
        }else if(compareOPCode(mem1, 50)){//SLA
            //データに基づいて処理する
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //実行アドレスを取得
            char jikkou = getJikkouAddress(tmp);
            //加算数を取得
            short sla = (short) memory.getMemory(jikkou);
            //grの中身を取得
            short r = (short) register.getGr()[getGrNumber(tmp)];
            //rの記号を保持
            int r_before = r;
            //計算結果はrに入る
            short ans;
            ans= (short) checkShortRange((int)r<<sla);

            if(r_before * ans<0){//符号が変わっていれば元に戻す
                ans= (short) (ans^0x8000);
            }

            //OFは最後に送り出されたビットの値
            fr[0]= (char) ((r_before>>(15-sla))&0x0001);
            setRegisterAfterClaculationShort(cpc,wordCount,tmp,ans);
        }else if(compareOPCode(mem1, 51)){//SRA
            //データに基づいて処理する
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //実行アドレスを取得
            char jikkou = getJikkouAddress(tmp);
            //加算数を取得
            short sla = (short) memory.getMemory(jikkou);
            //grの中身を取得
            short r = (short) register.getGr()[getGrNumber(tmp)];
            //rの記号を保持
            int r_before = r;
            //計算結果はrに入る
            short ans;
            ans= (short) checkShortRange((int)r>>>sla);
            //OFは最後に送り出されたビットの値
            fr[0]= (char) ((r_before>>(sla-1))&0x0001);

            //pcが更新される
            setRegisterAfterClaculationShort(cpc,wordCount,tmp,ans);
        }else if(compareOPCode(mem1, 52)){//SLA
            //データに基づいて処理する
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //実行アドレスを取得
            char jikkou = getJikkouAddress(tmp);
            //加算数を取得
            char sla = memory.getMemory(jikkou);
            //grの中身を取得
            char r = register.getGr()[getGrNumber(tmp)];
            //rの記号を保持
            int r_before = r;
            //計算結果はrに入る
            char ans;
            ans= (char) checkCharRange((int)r<<sla);

            //OFは最後に送り出されたビットの値
            fr[0]= (char) ((r_before>>(15-sla))&0x0001);
            setRegisterAfterClaculationChar(cpc,wordCount,tmp,ans);
        }else if(compareOPCode(mem1, 53)){//SRA
            //データに基づいて処理する
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //実行アドレスを取得
            char jikkou = getJikkouAddress(tmp);
            //加算数を取得
            char sla = memory.getMemory(jikkou);
            //grの中身を取得
            char r = register.getGr()[getGrNumber(tmp)];
            //rの記号を保持
            int r_before = r;
            //計算結果はrに入る
            char ans;
            ans= (char) checkCharRange((int)r>>sla);

            //OFは最後に送り出されたビットの値
            fr[0]= (char) ((r_before>>(sla-1))&0x0001);

            //pcが更新される
            setRegisterAfterClaculationChar(cpc,wordCount,tmp,ans);
        }else if(compareOPCode(mem1, 61)){//JMI
            //データに基づいて処理する
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //実行アドレスを取得
            char jikkou = getJikkouAddress(tmp);

            //SFが1であれば実行アドレスをPCに代入
            if(fr[1]==1){
                register.setPc(jikkou);
            }else {//0ならば次へ進む
                register.setPc((char)(cpc+wordCount));
            }
        }else if(compareOPCode(mem1, 62)) {//JNZ
            //データに基づいて処理する
            wordCount = 2;
            char[] tmp = memory.getMemoryArray(register.getPc(), wordCount);
            //実行アドレスを取得
            char jikkou = getJikkouAddress(tmp);

            //ZFが0であれば実行アドレスをPCに代入
            if (fr[2] == 0) {
                register.setPc(jikkou);
            } else {//1ならば次へ進む
                register.setPc((char) (cpc + wordCount));
            }
        }else if(compareOPCode(mem1, 63)) {//JZE
            //データに基づいて処理する
            wordCount = 2;
            char[] tmp = memory.getMemoryArray(register.getPc(), wordCount);
            //実行アドレスを取得
            char jikkou = getJikkouAddress(tmp);

            //ZFが1であれば実行アドレスをPCに代入
            if (fr[2] == 1) {
                register.setPc(jikkou);
            } else {//0ならば次へ進む
                register.setPc((char) (cpc + wordCount));
            }
        }else if(compareOPCode(mem1, 64)) {//JUMP
            //データに基づいて処理する
            wordCount = 2;
            char[] tmp = memory.getMemoryArray(register.getPc(), wordCount);
            //実行アドレスを取得
            char jikkou = getJikkouAddress(tmp);

            //無条件で飛ぶ
            register.setPc(jikkou);
        }else if(compareOPCode(mem1, 65)) {//JPL
            //データに基づいて処理する
            wordCount = 2;
            char[] tmp = memory.getMemoryArray(register.getPc(), wordCount);
            //実行アドレスを取得
            char jikkou = getJikkouAddress(tmp);

            //SFZFがともに1であれば実行アドレスをPCに代入
            if (fr[1]==1&&fr[2] == 1) {
                register.setPc(jikkou);
            } else {//0ならば次へ進む
                register.setPc((char) (cpc + wordCount));
            }
        }else if(compareOPCode(mem1, 66)) {//JOV
            //データに基づいて処理する
            wordCount = 2;
            char[] tmp = memory.getMemoryArray(register.getPc(), wordCount);
            //実行アドレスを取得
            char jikkou = getJikkouAddress(tmp);

            //OFが1であれば実行アドレスをPCに代入
            if (fr[0] == 1) {
                register.setPc(jikkou);
            } else {//0ならば次へ進む
                register.setPc((char) (cpc + wordCount));
            }
        }else if(compareOPCode(mem1, 70)) {//PUSH
            //データに基づいて処理する
            wordCount = 2;
            char[] tmp = memory.getMemoryArray(register.getPc(), wordCount);
            //実行アドレスを取得
            char jikkou = getJikkouAddress(tmp);

            //SPが指す値を1ひいてSPに入れる
            char ans = memory.getMemory(register.getSp());
            register.setSp((char) (ans-1));
            //SPの指すアドレスへ実行アドレスを入れる
            memory.setMemory(jikkou,register.getSp());
            register.setPc((char) (cpc + wordCount));
        }else if(compareOPCode(mem1, 71)) {//POP
            //データに基づいて処理する
            wordCount = 1;
            char[] tmp = memory.getMemoryArray(register.getPc(), wordCount);
            //spの指すアドレスを取得
            char spaddr = register.getSp();
            //そのアドレスが指す値をgrへ格納
            register.setGr(memory.getMemory(spaddr),getGr2Number(tmp));
            //spに1を加算して格納
            register.setSp((char) (spaddr+1));

            register.setPc((char) (cpc + wordCount));
        }else if(compareOPCode(mem1, 80)) {//CALL
            //データに基づいて処理する
            wordCount = 2;
            char[] tmp = memory.getMemoryArray(register.getPc(), wordCount);
            //実行アドレスを取得
            char jikkou = getJikkouAddress(tmp);

            //SPが指す値を1ひいてSPに入れる
            char ans = memory.getMemory(register.getSp());
            register.setSp((char) (ans-1));
            //SPの指すアドレスへPCを入れる
            memory.setMemory(register.getPc(),register.getSp());
            //PCへ実行アドレスを入れる
            register.setPc(jikkou);
        }else if(compareOPCode(mem1, 81)) {//RET
            //データに基づいて処理する
            wordCount = 1;
            char[] tmp = memory.getMemoryArray(register.getPc(), wordCount);
            //spの指すアドレスを取得
            char spaddr = register.getSp();
            //そのアドレスが指す値をPCへ格納
            register.setPc(memory.getMemory(spaddr));
            //spに1を加算して格納
            register.setSp((char) (spaddr+1));

        }else if(compareOPCodeS(mem1, "F0")) {//SVC
            //データに基づいて処理する
            wordCount = 2;
            char[] tmp = memory.getMemoryArray(register.getPc(), wordCount);
            //spの指すアドレスを取得
            char spaddr = register.getSp();

            register.setPc((char) (cpc + wordCount));
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

    private char getDataAtJikkouAddress(char[] tmp) {
        int i = tmp[0]%4096;
        char sihyou_nakami = (char) (register.getGr()[i]);
        char addr = memory.getMemory(tmp[1]);
        return (char) (addr+sihyou_nakami);
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
    }
    public void waitEmu(){

    }
    public void registerSVC (int num, String func)  /* SVC num が実行されたときに呼び出す関数 func を設定 */ {
    }
    public void unregisterSVC (int num) {
    }

}
