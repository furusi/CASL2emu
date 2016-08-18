package com.example.furusho.casl2emu;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import java.util.Locale;

/**
 * Created by furusho on 2016/07/09.
 */
public class Casl2Emulator extends EmulatorCore {
    private static Casl2Emulator instance = new Casl2Emulator();
    Casl2Memory memory = Casl2Memory.getInstance();
    Casl2Register register = Casl2Register.getInstance();

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
            char jikkouaddr = getJikkouAddress(tmp);
            int gr_position=checkByte(tmp[0],2);
            register.setGr((char)memory.getMemory(jikkouaddr),gr_position);
            register.setPc((char)(cpc+wordCount));
        }else if(compareOPCode(mem1, 11)){//ST
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            char jikkouaddr = getJikkouAddress(tmp);
            int gr_position=checkByte(tmp[0],2);
            memory.setMemory(register.getGr()[gr_position],jikkouaddr);
            register.setPc((char)(cpc+wordCount));

        }else if(compareOPCode(mem1, 12)){//LAD
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            char jikkouaddr = getJikkouAddress(tmp);
            int gr_position=checkByte(tmp[0],2);
            register.setGr(jikkouaddr,gr_position);
            register.setPc((char)(cpc+wordCount));
        }else if(compareOPCode(mem1, 14)){//LD
            wordCount=1;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            char jikkouaddr = getJikkouAddress(tmp);
            int gr_position=checkByte(tmp[0],2);
            register.setGr((char)memory.getMemory(jikkouaddr),gr_position);

            int r1_position =checkByte(tmp[0],2);
            int r2_position =checkByte(tmp[0],3);
            char r2 = register.getGr()[r2_position];
            //計算結果はrに入る
            register.setGr(r2,r1_position);
            //pcが更新される
            register.setPc((char)(cpc+wordCount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        } else if(compareOPCode(mem1, 20)){//ADDA
            //データに基づいて処理する
            wordCount=2;
            //shortは符号付き
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            short jikkou = (short) getJikkouAddress(tmp);
            short r = (short) register.getGr()[tmp[1]];
            short ans=0;
            char[] t={0,0,0};
            try {
                ans = (short) (r+jikkou);
            }catch (ArithmeticException e){
                t[0]=1;
            }finally {
               if(ans<0) t[1]=1;

            }
            //計算結果はrに入る
            register.setGr((char)ans,tmp[1]);
            //pcが更新される
            register.setPc((char)(cpc+wordCount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        } else if(compareOPCode(mem1, 21)){//SUBA
            //データに基づいて処理する
            wordCount=2;
           //charは符号なし
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char jikkou = getJikkouAddress(tmp);
            char r =register.getGr()[tmp[1]];
            char ans = (char) (r-jikkou);
            //計算結果はrに入る
            register.setGr((char)ans,tmp[1]);
            //pcが更新される
            register.setPc((char)(cpc-wordCount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        } else if(compareOPCode(mem1, 22)){//ADDL
            //データに基づいて処理する
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char jikkou = getJikkouAddress(tmp);
            char r =register.getGr()[tmp[1]];
            char ans = (char) (r+jikkou);
            //計算結果はrに入る
            register.setGr((char)ans,tmp[1]);
            //pcが更新される
            register.setPc((char)(cpc+wordCount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        } else if(compareOPCode(mem1, 23)){//SUBL
            //データに基づいて処理する
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char jikkou = getJikkouAddress(tmp);
            char r =register.getGr()[tmp[1]];
            char ans = (char) (r-jikkou);
            //計算結果はrに入る
            register.setGr((char)ans,tmp[1]);
            //pcが更新される
            register.setPc((char)(cpc+wordCount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        }else if(compareOPCode(mem1, 24)){//ADDA
            //データに基づいて処理する
            wordCount=1;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char r1 = register.getGr()[tmp[1]/16]; char r2 = register.getGr()[tmp[1]%16];
            char ans = (char) (r1+r2);
            //計算結果はrに入る
            register.setGr((char)ans,tmp[1]/16);
            //pcが更新される
            register.setPc((char)(cpc+wordCount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        }else if(compareOPCode(mem1, 25)){//SUBA
            //データに基づいて処理する
            wordCount=1;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            short r1 = (short) register.getGr()[tmp[1]/16]; short r2 = (short) register.getGr()[tmp[1]%16];
            short ans = (short) (r1-r2);
            //計算結果はrに入る
            register.setGr((char) ans,tmp[1]/16);
            //pcが更新される
            register.setPc((char)(cpc+wordCount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        }else if(compareOPCode(mem1, 26)){//ADDL
            //データに基づいて処理する
            wordCount=1;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char r1 = register.getGr()[tmp[1]/16];
            char r2 = register.getGr()[tmp[1]%16];
            char ans = (char) (r1+r2);
            //計算結果はrに入る
            register.setGr((char)ans,tmp[1]/16);
            //pcが更新される
            register.setPc((char)(cpc+wordCount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        }else if(compareOPCode(mem1, 27)){//SUBL
            //データに基づいて処理する
            wordCount=1;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char r1 = register.getGr()[tmp[1]/16];
            char r2 = register.getGr()[tmp[1]%16];
            char ans = (char) (r1-r2);
            //計算結果はrに入る
            register.setGr((char)ans,tmp[1]/16);
            //pcが更新される
            register.setPc((char)(cpc+wordCount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        } else if(compareOPCode(mem1, 30)){//and
            //データに基づいて処理する
            wordCount=2;

            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char jikkou = getJikkouAddress(tmp);
            char r =register.getGr()[tmp[1]];
            char ans = (char) (r&jikkou);
            //計算結果はrに入る
            register.setGr(ans,tmp[1]);
            //pcが更新される
            register.setPc((char)(cpc+wordCount));
        } else if(compareOPCode(mem1, 31)){//or
            //データに基づいて処理する
            wordCount=2;

            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char jikkou = getJikkouAddress(tmp);
            char r =register.getGr()[tmp[1]];
            char ans = (char) (r&jikkou);
            //計算結果はrに入る
            register.setGr(ans,tmp[1]);
            //pcが更新される
            register.setPc((char)(cpc|wordCount));
        } else if(compareOPCode(mem1, 32)){//xor
            //データに基づいて処理する
            wordCount=2;

            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char jikkou = getJikkouAddress(tmp);
            char r =register.getGr()[tmp[1]];
            char ans = (char) (r^jikkou);
            //計算結果はrに入る
            register.setGr(ans,tmp[1]);
            //pcが更新される
            register.setPc((char)(cpc|wordCount));
        }else if(compareOPCode(mem1, 34)){//and
            //データに基づいて処理する
            wordCount=1;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char r1 = register.getGr()[tmp[1]/16];
            char r2 = register.getGr()[tmp[1]%16];
            char ans = (char) (r1&r2);
            //計算結果はrに入る
            register.setGr((char)ans,tmp[1]/16);
            //pcが更新される
            register.setPc((char)(cpc+wordCount));
        }else if(compareOPCode(mem1, 35)){//or
            //データに基づいて処理する
            wordCount=1;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char r1 = register.getGr()[tmp[1]/16];
            char r2 = register.getGr()[tmp[1]%16];
            char ans = (char) (r1|r2);
            //計算結果はrに入る
            register.setGr((char)ans,tmp[1]/16);
            //pcが更新される
            register.setPc((char)(cpc+wordCount));
        }else if(compareOPCode(mem1, 36)){//xor
            //データに基づいて処理する
            wordCount=1;
            char[] tmp = new char[wordCount];
            for(int i=0;i<wordCount;i++){
                tmp[i] = memory.getMemory(register.getPc()+i);
            }
            //xの中身を取得
            char r1 = register.getGr()[tmp[1]/16];
            char r2 = register.getGr()[tmp[1]%16];
            char ans = (char) (r1^r2);
            //計算結果はrに入る
            register.setGr((char)ans,tmp[1]/16);
            //pcが更新される
            register.setPc((char)(cpc+wordCount));
        } else if(compareOPCode(mem1, 40)){//CPA
            //データに基づいて処理する
            wordCount=2;

            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            short jikkou = (short) getJikkouAddress(tmp);
            short r = (short) register.getGr()[tmp[1]];
            char[] ans_fr = getCompareResultA(jikkou, r);
            //計算結果はrに入る
            register.setFr(ans_fr);
            //pcが更新される
            register.setPc((char)(cpc+wordCount));
        } else if(compareOPCode(mem1, 41)){//CPL
            //データに基づいて処理する
            wordCount=2;

            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char jikkou = getJikkouAddress(tmp);
            char r = register.getGr()[tmp[1]];
            char[] ans_fr = getCompareResultL(jikkou, r);
            //計算結果はrに入る
            register.setFr(ans_fr);
            //pcが更新される
            register.setPc((char)(cpc+wordCount));
        } else if(compareOPCode(mem1, 44)){//CPA
            //データに基づいて処理する
            wordCount=2;

            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            short jikkou = (short) getJikkouAddress(tmp);
            short r = (short) register.getGr()[tmp[1]];
            char[] ans_fr = getCompareResultA(jikkou, r);
            //計算結果はrに入る
            register.setFr(ans_fr);
            //pcが更新される
            register.setPc((char)(cpc|wordCount));
        } else if(compareOPCode(mem1, 45)){//CPL
            //データに基づいて処理する
            wordCount=2;

            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);
            //xの中身を取得
            char jikkou = getJikkouAddress(tmp);
            char r = register.getGr()[tmp[1]];
            char[] ans_fr = getCompareResultL(jikkou, r);
            //計算結果はrに入る
            register.setFr(ans_fr);
            //pcが更新される
            register.setPc((char)(cpc|wordCount));
        }else if(compareOPCode(mem1, 50)){//SLA
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);

            //xの中身を取得
            char jikkou = memory.getMemory(getJikkouAddress(tmp));
            int r_position=checkByte(tmp[0],2);
            short r = (short) register.getGr()[r_position];
            //rの記号を保持
            int r_before = r;
            //計算結果はrに入る
            r= (short) (r<<jikkou);
            if(r_before * r<0){//符号が変わっていれば元に戻す
                r= (short) (r*-1);
            }
            //pcが更新される

            register.setGr((char) r,r_position);
            register.setPc((char)(cpc+wordCount));
        }else if(compareOPCode(mem1, 51)){//SRA
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount);

            //xの中身を取得
            char jikkou =  memory.getMemory(getJikkouAddress(tmp));
            int r_position=checkByte(tmp[0],2);
            short r = (short) register.getGr()[r_position];
            //rの記号を保持
            int r_before = r;
            //計算結果はrに入る
            r= (short) (r>>>jikkou);
            if(r_before * r<0){//符号が変わっていれば元に戻す
                r= (short) (r*-1);
            }
            //pcが更新される

            register.setGr((char) r,r_position);
            register.setPc((char)(cpc+wordCount));
        }

        //処理の結果を受けてpc以外のレジスタとメモリが書き換わる
        //処理の結果を受けてpcが書き換わる

        //処理の結果を受けてpc以外のレジスタとメモリが書き換わる
        //処理の結果を受けてpcが書き換わる
    }

    private char[] getCompareResultL(char jikkou, char r) {
        char[] ans = {0,0,0};
        if(r>jikkou){
            ans[1] = 0;
            ans[2] = 0;
        }else if (r == jikkou){
            ans[1] = 0;
            ans[2] = 1;
        }else {
            ans[1] = 1;
            ans[2] = 0;
        }
        return ans;
    }

    private char[] getCompareResultA(short jikkou, short r) {
        char[] ans = {0,0,0};
        if(r>jikkou){
           ans[1] = 0;
           ans[2] = 0;
        }else if (r == jikkou){
            ans[1] = 0;
            ans[2] = 1;
        }else {
            ans[1] = 1;
            ans[2] = 0;
        }
        return ans;
    }

    private char getJikkouAddress(char[] tmp) {
        int i = tmp[0]%4096;
        char sihyou_nakami = (char) (register.getGr()[i]);
        char addr = memory.getMemory(tmp[1]);
        return (char) (addr+sihyou_nakami);
    }

    private boolean compareOPCode(char mem1, int opcode) {
        char i = (char) ((char)mem1>>8);
        return i == Integer.decode( "0x" + opcode );
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private int checkByte(char data, int position, char sample){
        String s = String.format(Locale.US,"%04X",data);
        int ret = Character.compare(s.charAt(position),sample);
        return ret;
    }
    private int checkByte(char data, int position){
        String s = String.format(Locale.US,"%04X",data);
        return s.charAt(position);
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
