package jp.ac.fukuoka_u.tl.casl2emu;


import java.util.HashMap;

import jp.ac.fukuoka_u.tl.casl2emu.android.OutputBuffer;

/**
 * Created by furusho on 2016/07/09.
 */
public class Casl2Emulator {
    private static HashMap _classnameToInstance = new HashMap();
    private static Object _lock = new Object();
    protected Casl2Memory memory = Casl2Memory.getInstance();
    protected static OutputBuffer outputBuffer = OutputBuffer.getInstance();
    protected Casl2Register register = Casl2Register.getInstance();
    char[] fr = new char[3];
    boolean interruptflag =false;
    protected boolean runflag = false;

    protected Casl2Emulator() {
        synchronized (_lock){
            String classname = this.getClass().getName();
            if(_classnameToInstance.get(classname)!=null){
                throw new RuntimeException("Already created: " + classname);
            }
            _classnameToInstance.put(classname,this);
        }
    }


    /*
    サブクラスでnewする
     */
    static public void initializeInstance(String classname){
        _classnameToInstance.remove(classname);

    }

   public static Casl2Emulator getInstance(String classname) {
       synchronized (_lock){
           Casl2Emulator obj=(Casl2Emulator)_classnameToInstance.get(classname);

           if(obj==null){
              try{
                  Class cls = Class.forName(classname);
                  obj = (Casl2Emulator)cls.newInstance();
              } catch (ClassNotFoundException e) {
                  e.printStackTrace();
              } catch (InstantiationException e) {
                  e.printStackTrace();
              } catch (IllegalAccessException e) {
                  e.printStackTrace();
              }
           }
           return obj;
       }
   }


    public boolean isInterruptflag() {
        return interruptflag;
    }

    public boolean isRunflag() {
        return runflag;
    }

    public void setRunflag(boolean runflag) {
        this.runflag = runflag;
    }

    public void setInterruptflag(boolean interruptflag) {
        runflag = false;
        this.interruptflag = interruptflag;
    }

    public void stepOver(){
        //pcの指すメモリの中身をを見る
        char cpc = register.getPc(); char mem1 = memory.getMemory(cpc);
        fr[0]=0; fr[1]=0; fr[2]=0;
        //pcの命令をみて読み込むデータ数が決まる。
        int wordCount=0;
        char[] instArray;
        int r1_position;
        short sans;
        int ians;
        short[] sr = {0,0};
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
                instArray = memory.getMemoryArray(register.getPc(),wordCount);
                r2 = getJikkouAddress(instArray);
                r1_position = getGrNumber(instArray);
                data = memory.getMemory(r2);
                register.setGr(data,r1_position);
                fr[0]=0;//LDのOFは必ず0
                setRegisterAfterClaculation(cpc,wordCount,instArray,data);
                break;
            case 0x1100://ST
            wordCount=2;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                char setaddr = getJikkouAddress(instArray);
                r1_position = getGrNumber(instArray);
                memory.setMemory(register.getGr()[r1_position],setaddr);
                register.setPc((char)(cpc+wordCount));
                break;
            case 0x1200://LAD
            wordCount=2;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                r2 = getJikkouAddress(instArray);
                r1_position = getGrNumber(instArray);
                register.setGr(r2,r1_position);
                register.setPc((char)(cpc+wordCount));
                break;
            case 0x1400://LD
            wordCount=1;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);

                r1_position = getGrNumber(instArray);
                int r2_position = getGr2Number(instArray);
                data = register.getGr()[r2_position];
                //計算結果はrに入る
                register.setGr(data,r1_position);
                fr[0]=0;//LDのOFは必ず0
                setRegisterAfterClaculation(cpc,wordCount,instArray,data);
                break;
            case 0x2000://ADDA
                //データに基づいて処理する
                wordCount=2;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                r2 = getJikkouAddress(instArray);
                //加算数を取得
                smember = (short) memory.getMemory(r2);
                //grの中身を取得
                sr[0] = (short) register.getGr()[getGrNumber(instArray)];
                adda(cpc, wordCount, instArray, sr[0], smember);
                break;
            case 0x2100://SUBA
                //データに基づいて処理する
                wordCount=2;
                    instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                r2 = getJikkouAddress(instArray);
                //減算数を取得
                smember = (short) memory.getMemory(r2);
                //grの中身を取得
                sr[0] = (short) register.getGr()[getGrNumber(instArray)];
                suba(cpc, wordCount, instArray, sr[0], smember);
                break;
            case 0x2200://ADDL
                //データに基づいて処理する
                wordCount=2;
                    instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                    r2 = getJikkouAddress(instArray);
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                //加算数を取得
                cmember = memory.getMemory(r2);
                addl(cpc, wordCount, instArray, r1, cmember);
                break;
            case 0x2300://SUBL
                //データに基づいて処理する
                wordCount=2;
                    instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(instArray);
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                //加算数を取得
                cmember = memory.getMemory(jikkou);
                subl(cpc, wordCount, instArray, r1, cmember);
                break;
            case 0x2400://ADDA
                //データに基づいて処理する
                wordCount=1;
                    instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                sr[0] = (short) register.getGr()[getGrNumber(instArray)];
                sr[1] = (short) register.getGr()[getGr2Number(instArray)];
                adda(cpc, wordCount, instArray, sr[0], sr[1]);
                break;
            case 0x2500://SUBA
                //データに基づいて処理する
                wordCount=1;
                    instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                    sr[0] = (short) register.getGr()[getGrNumber(instArray)];
                    sr[1] = (short) register.getGr()[getGr2Number(instArray)];
                suba(cpc, wordCount, instArray, sr[0], sr[1]);
                break;
            case 0x2600://ADDL
            //データに基づいて処理する
                wordCount=1;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
            //xの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                r2 = register.getGr()[getGr2Number(instArray)];
                addl(cpc,wordCount,instArray,r1,r2);
                break;
            case 0x2700://SUBL
            //データに基づいて処理する
                wordCount=1;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                r2 = register.getGr()[getGr2Number(instArray)];
                subl(cpc, wordCount, instArray, r1, r2);
                break;
            case 0x3000://and
            //データに基づいて処理する
                wordCount=2;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                jikkou = getJikkouAddress(instArray);
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                //加算数を取得
                cmember = memory.getMemory(jikkou);
                data = (char) (r1 & cmember);
                setRegisterAfterClaculation(cpc,wordCount,instArray,data);
                break;
            case 0x3100://or
                //データに基づいて処理する
                wordCount=2;
                    instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                    jikkou = getJikkouAddress(instArray);
                //grの中身を取得
                    r1 = register.getGr()[getGrNumber(instArray)];
                //加算数を取得
                    cmember = memory.getMemory(jikkou);
                data = (char) (r1|cmember);
                setRegisterAfterClaculation(cpc,wordCount,instArray,data);
                break;
            case 0x3200://xor
                //データに基づいて処理する
                wordCount=2;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                jikkou = getJikkouAddress(instArray);
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                //加算数を取得
                cmember = memory.getMemory(jikkou);
                data = (char) (r1 ^ cmember);
                setRegisterAfterClaculation(cpc,wordCount,instArray,data);
                break;
            case 0x3400://and
                //データに基づいて処理する
                wordCount=1;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                r2 = register.getGr()[getGr2Number(instArray)];
                data = (char) (r1 & r2);
                setRegisterAfterClaculation(cpc,wordCount,instArray,data);
                break;
            case 0x3500://or
                //データに基づいて処理する
                wordCount=1;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                r2 = register.getGr()[getGr2Number(instArray)];
                data = (char) (r1 | r2);
                setRegisterAfterClaculation(cpc,wordCount,instArray,data);
                break;
            case 0x3600://xor
                //データに基づいて処理する
                wordCount=1;
                instArray = new char[wordCount];
                for(int i=0;i<wordCount;i++){
                    instArray[i] = memory.getMemory(register.getPc()+i);
                }
                //xの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                r2 = register.getGr()[getGr2Number(instArray)];
                data = (char) (r1 ^ r2);
                setRegisterAfterClaculation(cpc,wordCount,instArray,data);
                break;
            case 0x4000://CPA
                //データに基づいて処理する
                wordCount=2;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(instArray);
                //加算数を取得
                smember = (short) memory.getMemory(jikkou);
                //grの中身を取得
                sr[0] = (short) register.getGr()[getGrNumber(instArray)];
                fr[0]=0;
                getCompareResultA(sr[0], smember);
                //pcが更新される
                register.setPc((char)(cpc+wordCount));
                break;
            case 0x4100://CPL
                //データに基づいて処理する
                wordCount=2;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                jikkou = getJikkouAddress(instArray);
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
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
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                sr[0] = (short) register.getGr()[getGrNumber(instArray)];
                sr[1] = (short) register.getGr()[getGr2Number(instArray)];
                fr[0]=0;
                getCompareResultA(sr[0], sr[1]);
                //pcが更新される
                register.setPc((char)(cpc+wordCount));
                break;
            case 0x4500://CPL
                //データに基づいて処理する
                wordCount=1;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //xの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                r2 = register.getGr()[getGr2Number(instArray)];
                fr[0]=0;
                getCompareResultL(r1, r2);
                //pcが更新される
                register.setPc((char)(cpc+wordCount));
                break;
            case 0x5000://SLA
                //データに基づいて処理する
                wordCount=2;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(instArray);
                //加算数を取得
                smember = (short) jikkou;
                //grの中身を取得
                sr[0] = (short) register.getGr()[getGrNumber(instArray)];
                //rの記号を保持
                r_before = sr[0];
                //計算結果はrに入る
                sans= (short) checkShortRange((int)sr[0]<<smember);

                if(r_before * sans<0){//符号が変わっていれば元に戻す
                    sans= (short) (sans^0x8000);
                }

                //OFは最後に送り出されたビットの値
                fr[0]= (char) ((r_before>>(15-smember))&0x0001);
                setRegisterAfterClaculation(cpc,wordCount,instArray, (char) sans);
                break;
            case 0x5100://SRA
                //データに基づいて処理する
                wordCount=2;
                    instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(instArray);
                //加算数を取得
                smember = (short) jikkou;
                //grの中身を取得
                sr[0] = (short) register.getGr()[getGrNumber(instArray)];
                //rの記号を保持
                r_before = sr[0];
                //計算結果はrに入る
                sans= (short) checkShortRange((int)sr[0]>>>smember);
                //OFは最後に送り出されたビットの値
                fr[0]= (char) ((r_before>>(smember-1))&0x0001);

                //pcが更新される
                setRegisterAfterClaculation(cpc,wordCount,instArray, (char) sans);
                break;
            case 0x5200://SLL
                //データに基づいて処理する
                wordCount=2;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(instArray);
                //加算数を取得
                cmember = jikkou;
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                //rの記号を保持
                r_before = r1;
                //計算結果はrに入る
                data= (char) checkCharRange((int)r1<<cmember);

                //OFは最後に送り出されたビットの値
                fr[0]= (char) ((r_before>>(15-cmember))&0x0001);
                setRegisterAfterClaculation(cpc,wordCount,instArray,data);
                break;
            case 0x5300://SRL
                //データに基づいて処理する
                wordCount=2;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(instArray);
                //加算数を取得
                cmember = jikkou;
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                //rの記号を保持
                r_before = r1;
                //計算結果はrに入る
                data= (char) checkCharRange((int)r1>>cmember);

                //OFは最後に送り出されたビットの値
                fr[0]= (char) ((r_before>>(cmember-1))&0x0001);

                //pcが更新される
                setRegisterAfterClaculation(cpc,wordCount,instArray,data);
                break;
            case 0x6100://JMI
                //データに基づいて処理する
                wordCount=2;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(instArray);

                //SFが1であれば実行アドレスをPCに代入
                if(register.getFr()[1]==1){
                    register.setPc(jikkou);
                }else {//0ならば次へ進む
                    register.setPc((char)(cpc+wordCount));
                }
                break;
            case 0x6200://JNZ
                //データに基づいて処理する
                wordCount = 2;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(instArray);

                //ZFが0であれば実行アドレスをPCに代入
                if (register.getFr()[2] == 0) {
                    register.setPc(jikkou);
                } else {//1ならば次へ進む
                    register.setPc((char) (cpc + wordCount));
                }
                break;
            case 0x6300://JZE
                //データに基づいて処理する
                wordCount = 2;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(instArray);

                //ZFが1であれば実行アドレスをPCに代入
                if (register.getFr()[2] == 1) {
                    register.setPc(jikkou);
                } else {//0ならば次へ進む
                    register.setPc((char) (cpc + wordCount));
                }
                break;
            case 0x6400://JUMP
                //データに基づいて処理する
                wordCount = 2;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(instArray);

                //無条件で飛ぶ
                register.setPc(jikkou);
                break;
            case 0x6500://JPL
                //データに基づいて処理する
                wordCount = 2;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(instArray);

                //SFZFがともに1であれば実行アドレスをPCに代入
                if (register.getFr()[1]==0&&register.getFr()[2] == 0) {
                    register.setPc(jikkou);
                } else {//0ならば次へ進む
                    register.setPc((char) (cpc + wordCount));
                }
                break;
            case 0x6600://JOV
                //データに基づいて処理する
                wordCount = 2;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(instArray);

                //OFが1であれば実行アドレスをPCに代入
                if (register.getFr()[0] == 1) {
                    register.setPc(jikkou);
                } else {//0ならば次へ進む
                    register.setPc((char) (cpc + wordCount));
                }
                break;
            case 0x7000://PUSH
                //データに基づいて処理する
                wordCount = 2;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                //実行アドレスを取得
                jikkou = getJikkouAddress(instArray);

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
                if(register.getSp()<0xFEFF) {
                    instArray = memory.getMemoryArray(register.getPc(), wordCount);
                    //spの指すアドレスを取得
                    spaddr = register.getSp();
                    //そのアドレスが指す値をgrへ格納
                    register.setGr(memory.getMemory(spaddr), getGrNumber(instArray));
                    //spに1を加算して格納
                    register.setSp((char) (spaddr + 1));

                    register.setPc((char) (cpc + wordCount));
                }
                break;
            case 0x8000://CALL
                //データに基づいて処理する
                wordCount = 2;
                instArray = memory.getMemoryArray(register.getPc(), wordCount);
                register.setPc((char) (cpc + wordCount));
                //実行アドレスを取得
                jikkou = getJikkouAddress(instArray);

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
                //spの指すアドレスを取得
                spaddr = register.getSp();
                if(spaddr == 0xFEFF){
                    waitEmu();
                    showText("RET命令によって停止しました。");
                }else {
                    //そのアドレスが指す値をPCへ格納
                    register.setPc(memory.getMemory(spaddr));
                    //spに1を加算して格納
                    register.setSp((char) (spaddr+1));
                }

                break;
            case 0xF000://SVC
                opSVC(cpc, sr);

                break;
            default:
                //Toast("不正な命令です。");
        }

        //TODO:ステップ実行回数の記録
    }

    protected void showText(String txt) {
    }

    public void opSVC(char cpc, short[] sr) {

    }

    protected char[] getFloatArray(float r) {
        char sign=0;
        if (r < 0){
            sign = 0xF;
            r= Math.abs(r);
        }
        short r_sisu=0;

        float abs_r =Math.abs(r);
        if(abs_r<0.1) {//絶対値が0.1以上1未満になるまで乗除を繰り返す
            for (short i = 0; i < 37; i++) {
                if (Math.abs(r) >= 0.1) {
                    r_sisu = (short) (i*-1);
                    break;
                }else{
                    r = r * 10;

                }
            }
        }else if (abs_r>=1){
            for (short i = 0; i < 37; i++) {
                if (Math.abs(r) < 1){
                    r_sisu = i;
                    break;
                }else {
                    r = r / 10;
                }
            }
        }

        char[] r_array = new char[3];
        char[] _r={48,48,48,48,48,48,48,48,48};
        char[] cs = String.valueOf(r).toCharArray();
        for(int i=0;i<cs.length;i++){
            _r[i]=cs[i];
        }
        //'0'=48,'1'=49   '1'-48=1
        r_array[0]= (char) ((sign <<12) + ((_r[0]-48) <<8) +((_r[2]-48)<<4)+((_r[3]-48)));
        r_array[1]= (char) (((_r[4]-48) <<12) + ((_r[5]-48) <<8) +((_r[6]-48)<<4)+((_r[7]-48)));
        r_array[2]= (char) r_sisu;
        return r_array;
    }

    protected double getFloat(char c, char[] a_kasu) {
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
        double flt = (double) ((_array[0]*0.1+_array[1]*0.01+_array[2]*0.001+_array[3]*0.0001+_array[4]*0.00001+_array[5]*0.000001+_array[6]*0.0000001)*sign*(Math.pow(10,a_sisu)));
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
        register.setFr(fr);
    }

    private void getCompareResultL(char r, char jikkou) {
        if(r>jikkou){
            fr[1] = 0; fr[2] = 0;
        }else if (r == jikkou){
            fr[1] = 0; fr[2] = 1;
        }else {
            fr[1] = 1; fr[2] = 0;
        }
        register.setFr(fr);
    }

    private void getCompareResultA(short r, short jikkou) {
        if(r>jikkou){
            fr[1] = 0; fr[2] = 0;
        }else if (r == jikkou){
            fr[1] = 0; fr[2] = 1;
        }else {
            fr[1] = 1; fr[2] = 0;
        }
        register.setFr(fr);

    }

    protected char getJikkouAddress(char[] cordstr) {
        int sihyou = getGr2Number(cordstr);
        char sihyou_nakami=0;
        if (sihyou != 0) {
            sihyou_nakami = register.getGr()[sihyou];
        }
        return (char) ((int)cordstr[1]+(int)sihyou_nakami);
    }

    private int getGrNumber(char[] cordstr){
        int r1data =(cordstr[0]>>4) & 0x000F;
        if(r1data >= 0 && r1data < 8){
            return r1data;
        }else {
            return 0;
        }
    }
    private int getGr2Number(char[] cordstr){
        int r2data = cordstr[0] & 0x000F;
        if(r2data >= 0 && r2data < 8){
            return r2data;
        }else {
            return 0;
        }
    }
    protected long checkShortRange(int value){
        if(value > Short.MAX_VALUE||value < Short.MIN_VALUE)
            fr[0]=1;
        return value;
    }
    protected long checkCharRange(int value){
        if(value > Character.MAX_VALUE||value < Character.MIN_VALUE)
            fr[0]=1;
        return value;
    }
    protected double checkFloatRange(double value){
        if(value > Float.MAX_VALUE||value < Float.MIN_VALUE)
            fr[0]=1;
        return value;
    }


    public void run(final int interval){
    }

    public void waitEmu(){
    }

    public char getMemory(int position) {
        return memory.getMemory(position);
    }

    public void setMemoryArray(char[] chars, int i) {
       memory.setMemoryArray(chars,i);
    }

    public void deleteMemoryArray(char[] zero, int i) {
       memory.deleteMemoryArray(zero,i);
    }

    public void insertMemoryArray(char[] zero, int i) {
        memory.insertMemoryArray(zero, i);
    }

    public char[] getMemory() {
        return memory.getMemory();

    }

    public void setMemory(char[] tmp) {
        memory.setMemory(tmp);
    }

    public void setDatafromBinary(byte[] loaddata) {
        memory.setDatafromBinary(loaddata);
    }

    public void setMemory(char c, char address) {
        memory.setMemory(c,address);
    }

    public void refreshMemory(char[] data, char position) {
        memory.refreshMemory(data,position);
    }
}
