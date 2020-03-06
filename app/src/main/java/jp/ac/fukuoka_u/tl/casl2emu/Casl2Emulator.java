package jp.ac.fukuoka_u.tl.casl2emu;


import java.util.HashMap;

import jp.ac.fukuoka_u.tl.casl2emu.android.OutputBuffer;
import static jp.ac.fukuoka_u.tl.casl2emu.Casl2Emulator.OPCode.*;

/**
 * Created by furusho on 2016/07/09.
 */
public abstract class Casl2Emulator {
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

    public enum OPCode {
        NOP((byte)0x00, 1),
        LD2((byte)0x10, 2),
        ST((byte)0x11, 2),
        LAD((byte)0x12, 2),
        LD1((byte)0x14, 1),
        ADDA2((byte)0x20, 2),
        SUBA2((byte)0x21, 2),
        ADDL2((byte)0x22, 2),
        SUBL2((byte)0x23, 2),
        ADDA1((byte)0x24, 1),
        SUBA1((byte)0x25, 1),
        ADDL1((byte)0x26, 1),
        SUBL1((byte)0x27, 1),
        AND2((byte)0x30, 2),
        OR2((byte)0x31, 2),
        XOR2((byte)0x32, 2),
        AND1((byte)0x34, 1),
        OR1((byte)0x35, 1),
        XOR1((byte)0x36, 1),
        CPA2((byte)0x40, 2),
        CPL2((byte)0x41, 2),
        CPA1((byte)0x44, 1),
        CPL1((byte)0x45, 1),
        SLA((byte)0x50, 2),
        SRA((byte)0x51, 2),
        SLL((byte)0x52, 2),
        SRL((byte)0x53, 2),
        JMI((byte)0x61, 2),
        JNZ((byte)0x62, 2),
        JZE((byte)0x63, 2),
        JUMP((byte)0x64, 2),
        JPL((byte)0x65, 2),
        JOV((byte)0x66, 2),
        PUSH((byte)0x70, 2),
        POP((byte)0x71, 1),
        CALL((byte)0x80, 2),
        RET((byte)0x81, 1),
        SVC((byte)0xF0, 2);

        private final byte opcode;
        private final int size;
        OPCode (final byte _opcode, int _size){
            this.opcode = _opcode;
            this.size = _size;
        }
        public byte getCode (){
            return opcode;
        }
        public int getsize(){
            return size;
        }

        static OPCode getEnum(byte _code){
            for(OPCode o:OPCode.values()){
                if(o.getCode() == _code){
                    return o;
                }
            }
            return null;
        }
    }

    /*
      サブクラスの名前を指定してでnewする
    */
    static public void initializeInstance(String classname){
        _classnameToInstance.remove(classname);
    }

    /*サブクラスの名前を指定してインスタンスを作成する*/
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

   /**
   中断の状態を返す
    */
    public boolean isInterruptflag() {
        return interruptflag;
    }

    /*通常実行の有効・無効を返す*/
    public boolean isRunflag() {
        return runflag;
    }

    public void setRunflag(boolean runflag) {
        this.runflag = runflag;
    }

    public void setInterruptflag(boolean interruptflag) {
        this.interruptflag = interruptflag;
    }

    /**
     * 連続実行を停止する
     */
    public void stop(){
        setRunflag(false);
        waitEmu();
    }


    public int stepOver(){
        fr[0]=0; fr[1]=0; fr[2]=0;
        //pcの命令をみて読み込むデータ数が決まる。
        int r1_position;
        short sans;
        int ians;
        short[] sr = {0,0};
        char r1;
        char r2;
        short smember;
        char effective;
        char cmember;
        char spaddr;
        int r_before;
        char data;
        OPCode o = OPCode.getEnum(getOPCode());
        int wordCount = o.getsize();
        char[] instArray = memory.getMemoryArray(register.getPc(),wordCount);

        switch (o) {
            case NOP: // NOP
                //データに基づいて処理する
                register.setPc((char)(register.getPc()+wordCount));
                break;
            case LD2: // LD
                r2 = getEffectiveAddress();
                r1_position = getGrNumber(instArray);
                data = memory.getMemory(r2);
                register.setGr(data,r1_position);
                fr[0]=0;//LDのOFは必ず0
                setRegisterAfterClaculation(register.getPc(),wordCount,instArray,data);
                break;
            case ST://ST
                //実行アドレスを取得
                char setaddr = getEffectiveAddress();
                r1_position = getGrNumber(instArray);
                memory.setMemory(register.getGr()[r1_position],setaddr);
                register.setPc((char)(register.getPc()+wordCount));
                break;
            case LAD://LAD
                                r2 = getEffectiveAddress();
                r1_position = getGrNumber(instArray);
                register.setGr(r2,r1_position);
                register.setPc((char)(register.getPc()+wordCount));
                break;
            case LD1://LD
                r1_position = getGrNumber(instArray);
                int r2_position = getGr2Number(instArray);
                data = register.getGr()[r2_position];
                //計算結果はrに入る
                register.setGr(data,r1_position);
                fr[0]=0;//LDのOFは必ず0
                setRegisterAfterClaculation(register.getPc(),wordCount,instArray,data);
                break;
            case ADDA2://ADDA
                //実行アドレスを取得
                r2 = getEffectiveAddress();
                //加算数を取得
                smember = (short) memory.getMemory(r2);
                //grの中身を取得
                sr[0] = (short) register.getGr()[getGrNumber(instArray)];
                adda(register.getPc(), wordCount, instArray, sr[0], smember);
                break;
            case SUBA2://SUBA
                //xの中身を取得
                r2 = getEffectiveAddress();
                //減算数を取得
                smember = (short) memory.getMemory(r2);
                //grの中身を取得
                sr[0] = (short) register.getGr()[getGrNumber(instArray)];
                suba(register.getPc(), wordCount, instArray, sr[0], smember);
                break;
            case ADDL2://ADDL
                //実行アドレスを取得
                    r2 = getEffectiveAddress();
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                //加算数を取得
                cmember = memory.getMemory(r2);
                addl(register.getPc(), wordCount, instArray, r1, cmember);
                break;
            case SUBL2://SUBL
                //実行アドレスを取得
                effective = getEffectiveAddress();
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                //加算数を取得
                cmember = memory.getMemory(effective);
                subl(register.getPc(), wordCount, instArray, r1, cmember);
                break;
            case ADDA1://ADDA
                //xの中身を取得
                sr[0] = (short) register.getGr()[getGrNumber(instArray)];
                sr[1] = (short) register.getGr()[getGr2Number(instArray)];
                adda(register.getPc(), wordCount, instArray, sr[0], sr[1]);
                break;
            case SUBA1://SUBA
                //xの中身を取得
                    sr[0] = (short) register.getGr()[getGrNumber(instArray)];
                    sr[1] = (short) register.getGr()[getGr2Number(instArray)];
                suba(register.getPc(), wordCount, instArray, sr[0], sr[1]);
                break;
            case ADDL1://ADDL
            //xの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                r2 = register.getGr()[getGr2Number(instArray)];
                addl(register.getPc(),wordCount,instArray,r1,r2);
                break;
            case SUBL1://SUBL
                //xの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                r2 = register.getGr()[getGr2Number(instArray)];
                subl(register.getPc(), wordCount, instArray, r1, r2);
                break;
            case AND2://and
                //xの中身を取得
                effective = getEffectiveAddress();
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                //加算数を取得
                cmember = memory.getMemory(effective);
                data = (char) (r1 & cmember);
                setRegisterAfterClaculation(register.getPc(),wordCount,instArray,data);
                break;
            case OR2://or
                //xの中身を取得
                    effective = getEffectiveAddress();
                //grの中身を取得
                    r1 = register.getGr()[getGrNumber(instArray)];
                //加算数を取得
                    cmember = memory.getMemory(effective);
                data = (char) (r1|cmember);
                setRegisterAfterClaculation(register.getPc(),wordCount,instArray,data);
                break;
            case XOR2://xor
                //xの中身を取得
                effective = getEffectiveAddress();
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                //加算数を取得
                cmember = memory.getMemory(effective);
                data = (char) (r1 ^ cmember);
                setRegisterAfterClaculation(register.getPc(),wordCount,instArray,data);
                break;
            case AND1://and
                //xの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                r2 = register.getGr()[getGr2Number(instArray)];
                data = (char) (r1 & r2);
                setRegisterAfterClaculation(register.getPc(),wordCount,instArray,data);
                break;
            case OR1://or
                //データに基づいて処理する
                //xの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                r2 = register.getGr()[getGr2Number(instArray)];
                data = (char) (r1 | r2);
                setRegisterAfterClaculation(register.getPc(),wordCount,instArray,data);
                break;
            case XOR1://xor
                //データに基づいて処理する
                for(int i=0;i<wordCount;i++){
                    instArray[i] = memory.getMemory(register.getPc()+i);
                }
                //xの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                r2 = register.getGr()[getGr2Number(instArray)];
                data = (char) (r1 ^ r2);
                setRegisterAfterClaculation(register.getPc(),wordCount,instArray,data);
                break;
            case CPA2://CPA
                //データに基づいて処理する
                //実行アドレスを取得
                effective = getEffectiveAddress();
                //加算数を取得
                smember = (short) memory.getMemory(effective);
                //grの中身を取得
                sr[0] = (short) register.getGr()[getGrNumber(instArray)];
                fr[0]=0;
                getCompareResultA(sr[0], smember);
                //pcが更新される
                register.setPc((char)(register.getPc()+wordCount));
                break;
            case CPL2://CPL
                //データに基づいて処理する
                //xの中身を取得
                effective = getEffectiveAddress();
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                //加算数を取得
                cmember = memory.getMemory(effective);
                fr[0]=0;
                getCompareResultL(r1, cmember);
                //pcが更新される
                register.setPc((char)(register.getPc()+wordCount));
                break;
            case CPA1://CPA
                //データに基づいて処理する
                //xの中身を取得
                sr[0] = (short) register.getGr()[getGrNumber(instArray)];
                sr[1] = (short) register.getGr()[getGr2Number(instArray)];
                fr[0]=0;
                getCompareResultA(sr[0], sr[1]);
                //pcが更新される
                register.setPc((char)(register.getPc()+wordCount));
                break;
            case CPL1://CPL
                //データに基づいて処理する
                //xの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                r2 = register.getGr()[getGr2Number(instArray)];
                fr[0]=0;
                getCompareResultL(r1, r2);
                //pcが更新される
                register.setPc((char)(register.getPc()+wordCount));
                break;
            case SLA://SLA
                //データに基づいて処理する
                //実行アドレスを取得
                effective = getEffectiveAddress();
                //加算数を取得
                smember = (short) effective;
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
                setRegisterAfterClaculation(register.getPc(),wordCount,instArray, (char) sans);
                break;
            case SRA://SRA
                //データに基づいて処理する
                //実行アドレスを取得
                effective = getEffectiveAddress();
                //加算数を取得
                smember = (short) effective;
                //grの中身を取得
                sr[0] = (short) register.getGr()[getGrNumber(instArray)];
                //rの記号を保持
                r_before = sr[0];
                //計算結果はrに入る
                sans= (short) checkShortRange((int)sr[0]>>>smember);
                //OFは最後に送り出されたビットの値
                fr[0]= (char) ((r_before>>(smember-1))&0x0001);

                //pcが更新される
                setRegisterAfterClaculation(register.getPc(),wordCount,instArray, (char) sans);
                break;
            case SLL://SLL
                //データに基づいて処理する
                //実行アドレスを取得
                effective = getEffectiveAddress();
                //加算数を取得
                cmember = effective;
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                //rの記号を保持
                r_before = r1;
                //計算結果はrに入る
                data= (char) checkCharRange((int)r1<<cmember);

                //OFは最後に送り出されたビットの値
                fr[0]= (char) ((r_before>>(15-cmember))&0x0001);
                setRegisterAfterClaculation(register.getPc(),wordCount,instArray,data);
                break;
            case SRL://SRL
                //データに基づいて処理する
                //実行アドレスを取得
                effective = getEffectiveAddress();
                //加算数を取得
                cmember = effective;
                //grの中身を取得
                r1 = register.getGr()[getGrNumber(instArray)];
                //rの記号を保持
                r_before = r1;
                //計算結果はrに入る
                data= (char) checkCharRange((int)r1>>cmember);

                //OFは最後に送り出されたビットの値
                fr[0]= (char) ((r_before>>(cmember-1))&0x0001);

                //pcが更新される
                setRegisterAfterClaculation(register.getPc(),wordCount,instArray,data);
                break;
            case JMI://JMI
                //データに基づいて処理する
                //実行アドレスを取得
                effective = getEffectiveAddress();

                //SFが1であれば実行アドレスをPCに代入
                if(register.getFr()[1]==1){
                    register.setPc(effective);
                }else {//0ならば次へ進む
                    register.setPc((char)(register.getPc()+wordCount));
                }
                break;
            case JNZ://JNZ
                //データに基づいて処理する
                //実行アドレスを取得
                effective = getEffectiveAddress();

                //ZFが0であれば実行アドレスをPCに代入
                if (register.getFr()[2] == 0) {
                    register.setPc(effective);
                } else {//1ならば次へ進む
                    register.setPc((char) (register.getPc() + wordCount));
                }
                break;
            case JZE://JZE
                //データに基づいて処理する
                //実行アドレスを取得
                effective = getEffectiveAddress();

                //ZFが1であれば実行アドレスをPCに代入
                if (register.getFr()[2] == 1) {
                    register.setPc(effective);
                } else {//0ならば次へ進む
                    register.setPc((char) (register.getPc() + wordCount));
                }
                break;
            case JUMP://JUMP
                //データに基づいて処理する
                //実行アドレスを取得
                effective = getEffectiveAddress();

                //無条件で飛ぶ
                register.setPc(effective);
                break;
            case JPL://JPL
                //データに基づいて処理する
                //実行アドレスを取得
                effective = getEffectiveAddress();

                //SFZFがともに1であれば実行アドレスをPCに代入
                if (register.getFr()[1]==0&&register.getFr()[2] == 0) {
                    register.setPc(effective);
                } else {//0ならば次へ進む
                    register.setPc((char) (register.getPc() + wordCount));
                }
                break;
            case JOV://JOV
                //データに基づいて処理する
                //実行アドレスを取得
                effective = getEffectiveAddress();

                //OFが1であれば実行アドレスをPCに代入
                if (register.getFr()[0] == 1) {
                    register.setPc(effective);
                } else {//0ならば次へ進む
                    register.setPc((char) (register.getPc() + wordCount));
                }
                break;
            case PUSH://PUSH
                //データに基づいて処理する
                //実行アドレスを取得
                effective = getEffectiveAddress();

                //SPが指す値を1ひいてSPに入れる
                data = register.getSp();
                register.setSp((char) (data-1));
                //SPの指すアドレスへ実行アドレスを入れる
                memory.setMemory(effective,register.getSp());
                register.setPc((char) (register.getPc() + wordCount));
                break;
            case POP://POP
                //データに基づいて処理する
                if(register.getSp()<0xFEFF) {
                    //spの指すアドレスを取得
                    spaddr = register.getSp();
                    //そのアドレスが指す値をgrへ格納
                    register.setGr(memory.getMemory(spaddr), getGrNumber(instArray));
                    //spに1を加算して格納
                    register.setSp((char) (spaddr + 1));

                    register.setPc((char) (register.getPc() + wordCount));
                }
                break;
            case CALL://CALL
                //データに基づいて処理する
                register.setPc((char) (register.getPc() + wordCount));
                //実行アドレスを取得
                effective = getEffectiveAddress();

                //SPが指す値を1ひいてSPに入れる
                data = register.getSp();
                register.setSp((char) (data-1));
                //SPの指すアドレスへPCを入れる
                memory.setMemory(register.getPc(),register.getSp());
                //PCへ実行アドレスを入れる
                register.setPc(effective);
                break;
            case RET://RET
                //データに基づいて処理する
                //spの指すアドレスを取得
                spaddr = register.getSp();
                if(spaddr == 0xFEFF){
                    waitEmu();
                    System.out.println("RET命令によって停止しました。");
                    return 1;
                }else {
                    //そのアドレスが指す値をPCへ格納
                    register.setPc(memory.getMemory(spaddr));
                    //spに1を加算して格納
                    register.setSp((char) (spaddr+1));
                }

                break;
            case SVC://SVC
                opSVC(register.getPc(), sr);

                break;
            default:
                //Toast("不正な命令です。");
        }

        return 0;
    }

    protected byte getOPCode() {
        return (byte)((memory.getMemory(register.getPc()) & 0xff00) >> 8);
    }


    public abstract void opSVC(char cpc, short[] sr);


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
        int d =   (ans>>15);
        if(d==1){
            fr[1]=1;
        }else if(ans==0x0000){
            fr[2]=1;
        }
        register.setFr(fr);
        //計算結果はrに入る
        register.setGr(ans,getGrNumber(tmp));
        register.setFr(fr);
        //pcが更新される
        register.setPc((char)(cpc+wordCount));
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
    /*実効アドレスを取得*/
    public char getEffectiveAddress() {
        char []codestr = memory.getMemoryArray(register.getPc(),2);
        int sihyou = getGr2Number(codestr);
        char sihyou_nakami=0;
        if (sihyou != 0) {
            sihyou_nakami = register.getGr()[sihyou];
        }
        return (char) ((int)codestr[1]+(int)sihyou_nakami);
    }


    /**
     * 配列先頭要素の上位8ビットの数値を返す
     * @param cordstr
     * @return
     */
    private int getGrNumber(char[] cordstr){
        int r1data =(cordstr[0]>>4) & 0x000F;
        if(r1data >= 0 && r1data < 8){
            return r1data;
        }else {
            return 0;
        }
    }

    /*配列の先頭要素の下位2バイトの数値を返す*/
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


    abstract public void run(final int interval);

    abstract public void waitEmu();

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


