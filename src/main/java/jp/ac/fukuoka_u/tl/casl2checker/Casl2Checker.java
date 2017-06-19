package jp.ac.fukuoka_u.tl.casl2checker;

import jp.ac.fukuoka_u.tl.casl2emu.Casl2Emulator;
import jp.ac.fukuoka_u.tl.casl2emu.Casl2Memory;
import jp.ac.fukuoka_u.tl.casl2emu.Casl2Register;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by furusho on 2017/01/04.
 */

public class Casl2Checker {

    private static int inputcounter=0;
    private static int exnum=0;
    private static int nopconter = 0;
    private static int ex5_rand =-1;
    private static final char[] kirakiraboshi={
            0, 0, 4, 4, 5, 5, 4,//休符
            3, 3, 2, 2, 1, 1, 0,//休符
            4, 4, 3, 3, 2, 2, 1,//休符
            4, 4, 3, 3, 2, 2, 1,//休符
            0, 0, 4, 4, 5, 5, 4,//休符
            3, 3, 2, 2, 1, 1, 0

    };

    static final String[] filename={
            "ex01.bin",
            "ex02.bin",
            "ex03.bin",
            "ex04.bin",
            "ex05.bin",
            "ex06.bin"
    };
    public Casl2Checker() {
    }
    public static void main(String[] args){
        Casl2EmulatorImpl emulator=null;
        Casl2Memory memory=Casl2Memory.getInstance();
        Casl2Register register = Casl2Register.getInstance();

        try {
            emulator = (Casl2EmulatorImpl) Casl2Emulator.getInstance("jp.ac.fukuoka_u.tl.casl2checker.Casl2EmulatorImpl");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println(e.getMessage()+"Casl2Emulatorが実装されていません");
            System.exit(1);
        }
        System.out.println(System.getProperty("user.dir"));
        String loadfilename="";
        try{
            exnum=Integer.parseInt(args[args.length-1]);
        }catch (NumberFormatException e){
            System.out.println(e.getMessage()+"1-6の値を入れてください");
            System.exit(1);

        }catch (ArrayIndexOutOfBoundsException e){
            System.out.println(e.getMessage()+"課題のファイル名を入れてください");
            System.exit(1);
        }
        if(exnum>0&&exnum<7) {
            loadfilename = filename[exnum - 1];
        }else {
            System.out.println("1-6の値を入れてください");
            System.exit(1);
        }
        FileInputStream fileInputStream = null;
        byte[] loaddata = new byte[131098];
        try {
            fileInputStream = new FileInputStream(loadfilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {

            fileInputStream.read(loaddata);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        register.setDatafromBinary(loaddata);
        memory.setDatafromBinary(loaddata);
        //初期化
        register.setPc((char) 0);
        register.setSp((char) 0xfeff);
        switch(exnum){
            case 1:
                register.setGr(new char[]{0,0,0,0,0,0,0,0});
                break;

            case 2:
                memory.setMemory((char)0,(char)0x1008);
                break;
            case 3:
                register.setGr(new char[]{0,0,0,0,0,0,0,0});
                break;

        }

        //memory.setMemoryArray(new char[]{0x1270, 0x0100, 0x1260, 0x0101, 0x8100}, 0x0000);
        mainloop:while (!((memory.getMemory(register.getPc()) == 0x8100) && (register.getSp()==0xfeff))){
            if(memory.getMemory(register.getPc())==0){
               nopconter++;
            }else{
                nopconter=0;
            }
            if(nopconter==100){
                System.out.println("NOPが100回続いたため終了");
                break mainloop;
            }
            emulator.stepOver();
            switch(exnum){
                case 1:
                    if(register.getGr()[0]==0x0000&&register.getGr()[1]==0x0001&&register.getGr()[2]==0x0002&&
                            register.getGr()[3]==0x0003&&register.getGr()[4]==0x0004&&register.getGr()[4]==0x0004&&
                            register.getGr()[5]==0x0005&&register.getGr()[6]==0x0006&&register.getGr()[7]==0x0007
                            )
                    {

                        System.out.println("ex01 passed!!!");
                        break mainloop;
                    }
                    break;

                case 2:
                    if(memory.getMemory(0x1008)!=0 ) {

                        char[] data = memory.getMemoryArray(0x1000,8);
                        char sum=0;
                        for(char tmp:data){
                           sum = (char) (sum+tmp);
                        }
                        if(sum==memory.getMemory(0x1008)){
                            System.out.println("ex02 passed!!!");
                            break mainloop;
                        }
                    }
                    break;

                case 3:
                    if((register.getGr()[0]|register.getGr()[1]|register.getGr()[2]|register.getGr()[3]|register.getGr()[4]
                            |register.getGr()[5])!=0){
                        System.out.println("GR6,GR7以外のレジスタを利用したため失敗");
                        break mainloop;
                    }else if(register.getGr()[6]==0xeed2){
                        System.out.println("ex03 passed!!");
                        break mainloop;
                    }
                    break;
                case 4://きらきらぼし
                    if(emulator.getSoundindex()==42){
                        if( Arrays.equals(emulator.getsound(),kirakiraboshi) ){
                            System.out.println("きらきらぼしの演奏成功");
                            break mainloop;
                        }else {
                            System.out.println("きらきらぼしではない！");
                            break mainloop;
                        }

                    }


                    break;
                case 5://数当てゲーム
                    /*
                    乱数生成→数値入力→外れてたら音A合ってたら音B
                    乱数ってなに？
                    出力イメージ
                    入力値：０
                    出力文字列：TOO LOW
                    音：ド
                    入力値：99
                    出力文字列：TOO HIGH
                    音：ド
                    正解：50
                    入力値：50
                    出力文字列：BINGO
                    音：ドレミレド
                     */

                    if(emulator.inputflag){//svcのインプット命令が来たら
                       //0-99の値を渡す。
                        outputSound(emulator);
                        switch (inputcounter){
                            case 0:
                                register.setGr((char) 0,7);
                                System.out.println("入力値：0");
                                inputcounter=1;
                                break;
                            case 1:
                                register.setGr((char) 99,7);
                                System.out.println("入力値：99");
                                inputcounter=2;
                                break;
                            case 2:
                                register.setGr((char)ex5_rand,7);
                                System.out.println("入力値："+ex5_rand);
                                inputcounter=3;
                                break;

                        }
                        emulator.inputflag=false;
                        //値入力時に音を表示する。
                    }
                    int r1=-1;
                    int r2=-1;
                    char cmpOP = (char) (memory.getMemory(register.getPc())&0xFF00);
                    if((cmpOP==0x4000||cmpOP==0x4100||cmpOP==0x4400||cmpOP==0x4500)&&inputcounter>0) {//CPL r1,r2なら
                        if ((cmpOP == 0x4400 || cmpOP == 0x4500) && inputcounter > 0) {//CPL r1,r2なら
                            //1~98の値が入ってる配列indexを記憶
                            r1 = register.getGr()[(memory.getMemory(register.getPc()) & 0x00F0) >> 4];
                            r2 = register.getGr()[memory.getMemory(register.getPc()) & 0x000F];
                        } else if ((cmpOP == 0x4000 || cmpOP == 0x4100) && inputcounter > 0) {//CPL r1,r2なら
                            r1 = register.getGr()[(memory.getMemory(register.getPc()) & 0x00F0) >> 4];
                            r2 = memory.getMemory(emulator.getEffectiveAddress());
                        }
                        if (ex5_rand == -1) {
                            if (r1 > 0 && r1 < 99) {//r1の中身が1~98
                                ex5_rand = r1;
                            } else if (r2 > 0 && r2 < 99) {
                                ex5_rand = r2;
                            } else {
                                System.out.println("乱数は0,99なので最初からやり直し");
                                register.setPc((char) 0x0000);
                                inputcounter = 0;
                            }
                        } else if (inputcounter == 2) {//2度目のCPLにおいて
                            if (ex5_rand == r1 || ex5_rand == r2) {
                                System.out.println("*****乱数：" + ex5_rand + "*****");
                                //乱数が１つに定められている。
                            } else {
                                System.out.println("乱数が一意でない。終了");
                                break mainloop;
                            }
                        }
                    }

                    break;
                case 6://八角形
                    if(emulator.getOctangleindex()==8){
                        //8角形チェック
                        char [][] obj = emulator.getOctangle();
                        ArrayList<LineData> octangledata = new ArrayList<>();
                        for(int i=0;i<obj.length;i++){
                            octangledata.add(new LineData(obj[i]));
                        }
                        char start[]={octangledata.get(0).value[0],octangledata.get(0).value[1]};
                        while(octangledata.size()>1){
                            //最初の始点を終点に持つ線が残ればいい
                            pointcheck:for(int i=1;i<octangledata.size();i++){
                                if(octangledata.get(i).value[0]==octangledata.get(0).value[2]&&
                                        octangledata.get(i).value[1]==octangledata.get(0).value[3]){

                                    char[] line=octangledata.get(i).value;
                                    System.out.println(//辺の長さを出力
                                            Math.sqrt(Math.pow(Math.abs(line[0]-line[2]),2.0)+
                                                      Math.pow(Math.abs(line[1]-line[3]),2.0)
                                            )
                                    );
                                    octangledata.add(0,octangledata.get(i));
                                    octangledata.remove(1);
                                    octangledata.remove(i);
                                    //obj=new char[][]{obj[i],Arrays.copyOfRange(obj,0,i-1),Arrays.copyOfRange(obj,i+1,obj.length)};
                                    break pointcheck;
                                }
                            }
                        }
                        char end[]={octangledata.get(0).value[2],octangledata.get(0).value[3]};
                        if(start[0]==end[0]&&start[1]==end[1]){
                            System.out.println("8角形が描けている");
                        }else {
                            System.out.println("8角形が描けてない");
                        }
                        break mainloop;
                    }
                    break;
            }
            /*
            if((memory.getMemory(register.getPc()) == 0x8100) && (register.getSp()==0xfeff)){
                System.out.println("retで正常終了");
                break mainloop;
            }

             */
        }
        if(exnum==5){
            outputSound(emulator);
        }
    }

    private static void outputSound(Casl2EmulatorImpl emulator) {
        if(emulator.getSoundindex()>0){
            //何の音が鳴るか出力
            ArrayList<String> soundstring = new ArrayList<>();
            for(int i:Arrays.copyOfRange(emulator.getsound(),0,emulator.getSoundindex())){
                switch (i){
                    case 0:
                        soundstring.add("ド");
                        break;
                    case 1:
                        soundstring.add("レ");
                        break;
                    case 2:
                        soundstring.add("ミ");
                        break;
                    case 3:
                        soundstring.add("ファ");
                        break;
                    case 4:
                        soundstring.add("ソ");
                        break;
                    case 5:
                        soundstring.add("ラ");
                        break;
                    case 6:
                        soundstring.add("シ");
                        break;
                    default:
                }
            }
            System.out.println("演奏される音："+soundstring);
            //出力が終わったらバッファをクリア
            emulator.initializesound();
        }
    }
}
