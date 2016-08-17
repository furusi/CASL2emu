package com.example.furusho.casl2emu;

import android.util.Log;

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
        int wordCount=0; int datacount=0;
        if(isSameString(mem1, "10")){
            wordCount=2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount*2);
            char[] fr = register.getFr();
            //データに基づいて処理する
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        } else if(isSameString(mem1, "20")){//ADDA
            //データに基づいて処理する
            wordCount=2;
            datacount=wordCount*2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount*2);
            //xの中身を取得
            short jikkou = (short) getJikkouAddress(tmp);
            short r = (short) register.getGr()[tmp[1]];
            short ans=0;
            try {
                ans = (short) (r+jikkou);
            }catch (ArithmeticException e){

            }
            //計算結果はrに入る
            register.setGr((char)ans,tmp[1]);
            //pcが更新される
            register.setPc((char)(cpc+datacount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        } else if(isSameString(mem1, "21")){//SUBA
            //データに基づいて処理する
            wordCount=2;
            datacount=wordCount*2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount*2);
            //xの中身を取得
            char jikkou = getJikkouAddress(tmp);
            char r =register.getGr()[tmp[1]];
            char ans = (char) (r-jikkou);
            //計算結果はrに入る
            register.setGr((char)ans,tmp[1]);
            //pcが更新される
            register.setPc((char)(cpc-datacount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        } else if(isSameString(mem1, "22")){//ADDL
            //データに基づいて処理する
            wordCount=2;
            datacount=wordCount*2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount*2);
            //xの中身を取得
            char jikkou = getJikkouAddress(tmp);
            char r =register.getGr()[tmp[1]];
            char ans = (char) (r+jikkou);
            //計算結果はrに入る
            register.setGr((char)ans,tmp[1]);
            //pcが更新される
            register.setPc((char)(cpc+datacount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        } else if(isSameString(mem1, "23")){//SUBL
            //データに基づいて処理する
            wordCount=2; datacount=wordCount*2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount*2);
            //xの中身を取得
            char jikkou = getJikkouAddress(tmp);
            char r =register.getGr()[tmp[1]];
            char ans = (char) (r-jikkou);
            //計算結果はrに入る
            register.setGr((char)ans,tmp[1]);
            //pcが更新される
            register.setPc((char)(cpc+datacount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        }else if(isSameString(mem1, "24")){//ADDA
            //データに基づいて処理する
            wordCount=1; datacount=wordCount*2;
            char[] tmp = new char[wordCount*2];
            for(int i=0;i<wordCount*2;i++){
                tmp[i] = memory.getMemory(register.getPc()+i);
            }
            //xの中身を取得
            char r1 = register.getGr()[tmp[1]/16]; char r2 = register.getGr()[tmp[1]%16];
            char ans = (char) (r1+r2);
            //計算結果はrに入る
            register.setGr((char)ans,tmp[1]/16);
            //pcが更新される
            register.setPc((char)(cpc+datacount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        }else if(isSameString(mem1, "25")){//SUBA
            //データに基づいて処理する
            wordCount=1; datacount=wordCount*2;
            char[] tmp = new char[wordCount*2];
            for(int i=0;i<wordCount*2;i++){
                tmp[i] = memory.getMemory(register.getPc()+i);
            }
            //xの中身を取得
            short r1 = (short) register.getGr()[tmp[1]/16]; short r2 = (short) register.getGr()[tmp[1]%16];
            short ans = (short) (r1-r2);
            //計算結果はrに入る
            register.setGr((char) ans,tmp[1]/16);
            //pcが更新される
            register.setPc((char)(cpc+datacount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        }else if(isSameString(mem1, "26")){//ADDL
            //データに基づいて処理する
            wordCount=1; datacount=wordCount*2;
            char[] tmp = new char[wordCount*2];
            for(int i=0;i<wordCount*2;i++){
                tmp[i] = memory.getMemory(register.getPc()+i);
            }
            //xの中身を取得
            char r1 = register.getGr()[tmp[1]/16];
            char r2 = register.getGr()[tmp[1]%16];
            char ans = (char) (r1+r2);
            //計算結果はrに入る
            register.setGr((char)ans,tmp[1]/16);
            //pcが更新される
            register.setPc((char)(cpc+datacount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        }else if(isSameString(mem1, "27")){//SUBL
            //データに基づいて処理する
            wordCount=1; datacount=wordCount*2;
            char[] tmp = new char[wordCount*2];
            for(int i=0;i<wordCount*2;i++){
                tmp[i] = memory.getMemory(register.getPc()+i);
            }
            //xの中身を取得
            char r1 = register.getGr()[tmp[1]/16];
            char r2 = register.getGr()[tmp[1]%16];
            char ans = (char) (r1-r2);
            //計算結果はrに入る
            register.setGr((char)ans,tmp[1]/16);
            //pcが更新される
            register.setPc((char)(cpc+datacount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        } else if(isSameString(mem1, "30")){//and
            //データに基づいて処理する
            wordCount=2;
            datacount=wordCount*2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount*2);
            //xの中身を取得
            char jikkou = getJikkouAddress(tmp);
            char r =register.getGr()[tmp[1]];
            char ans = (char) (r&jikkou);
            //計算結果はrに入る
            register.setGr(ans,tmp[1]);
            //pcが更新される
            register.setPc((char)(cpc+datacount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        } else if(isSameString(mem1, "31")){//or
            //データに基づいて処理する
            wordCount=2;
            datacount=wordCount*2;
            char[] tmp = memory.getMemoryArray(register.getPc(),wordCount*2);
            //xの中身を取得
            char jikkou = getJikkouAddress(tmp);
            char r =register.getGr()[tmp[1]];
            char ans = (char) (r&jikkou);
            //計算結果はrに入る
            register.setGr(ans,tmp[1]);
            //pcが更新される
            register.setPc((char)(cpc|datacount));
            Log.d("aaaaa","tmpの中身は"+ tmp[1] +"だよ");
        }

        //処理の結果を受けてpc以外のレジスタとメモリが書き換わる
        //処理の結果を受けてpcが書き換わる
    }

    private char getJikkouAddress(char[] tmp) {
        char sihyou_nakami = register.getGr()[tmp[2]];
        char jikkouadr = (char) (tmp[3]+sihyou_nakami);
        return memory.getMemory(jikkouadr);
    }

    private boolean isSameString(int mem1, String cs) {
        return Integer.toHexString(mem1).contentEquals(cs);
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
