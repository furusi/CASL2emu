package com.example.furusho.casl2sim;

/**
 * Created by furusho on 2016/07/09.
 */
public class CASL2Emulator extends EmulatorCore {
    public void registerSVC (int num, String func)  /* SVC num が実行されたときに呼び出す関数 func を設定 */ {
    }
    public void unregisterSVC (int num) {
    }
    public int getXX (){ /* レジスタXXを読み出す */
        return 1;
    }
    public void setXX (int val){ /* レジスタXXを設定する */

    }
    public void stepOver(){

    }
    public void run(){

    }
    public void waitEmu(){

    }

}
