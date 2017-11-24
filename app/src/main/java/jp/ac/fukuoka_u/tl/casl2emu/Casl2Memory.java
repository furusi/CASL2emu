package jp.ac.fukuoka_u.tl.casl2emu;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.util.Arrays;

/**
 * Created by furusho on 2016/07/09.
 */
public class Casl2Memory extends BaseObservable{

    static Casl2Memory instance = new Casl2Memory();
    @Bindable
    char[] memory = new char[65536];

    private Casl2Memory(){
        Arrays.fill(memory,'0');
    }


    static public void initializeInstance() {
        instance = new Casl2Memory();
    }

    static Casl2Memory getInstance(){
        return instance;
    }

    public char[] getMemory() {
        return memory;
    }
    public char[] getMemoryArray(int start, int count) {
        char[]tmp=new char[count];
        System.arraycopy(memory, 0 + start, tmp, 0, count);
        return tmp;
    }
    public char getMemory(int position) {
        return memory[position];
    }

    public void setMemory(char[] data) {
        Arrays.fill(memory,'\0');
        System.arraycopy(data, 0, memory, 0, data.length);
    }
    public void deleteMemoryArray(char[] data, int position){

        //コピーの必要な部分はpositionから65535-dataまで
        System.arraycopy(memory, position + data.length, memory, position, 65535 - data.length - position);
        setMemoryArray(data,65535-data.length);
    }
    public void insertMemoryArray(char[] data, int position){

        //コピーの必要な部分はpositionから65535-dataまで
        System.arraycopy(memory, position, memory, position + data.length, 65535 - data.length + 1 - position);
        setMemoryArray(data,position);
    }
    public void setMemoryArray(char[] data, int position) {
        System.arraycopy(data, 0, memory, 0 + position, data.length);
        notifyPropertyChanged(BR.casl2Memory);
    }
    public void setMemory(char data, int position) {
        memory[position]=data;
        notifyPropertyChanged(BR.casl2Memory);
    }
    public void initializeMemory(){
        Arrays.fill(this.memory,(char)0);
        notifyPropertyChanged(BR.casl2Memory);
    }
    public void setMemoryWithoutNotifying(char data, int position) {
        memory[position]=data;
    }
    public void setDatafromBinary(byte[] loaddata){

        for (int i = 0; i < 65536; i++) {
            this.setMemoryWithoutNotifying(convertBytetoChar(loaddata,2 * (13 + i)), i);
        }
    }

    protected char convertBytetoChar(byte[] loaddata, int position) {
        return (char) ((char)(loaddata[position]<<8)+(char)(loaddata[position + 1]&0x00FF));
    }
    public void refreshMemory(char[] data, char position) {
        setMemoryArray(data, position);
    }


}
