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
        for(int i = 0; i <count;i++){
            tmp[i]=memory[i+start];
        }
        return tmp;
    }
    public char getMemory(int position) {
        return memory[position];
    }

    public void setMemory(char[] data) {
        Arrays.fill(memory,'\0');
        for(int i = 0; i <data.length;i++){
            memory[i]=data[i];
        }
    }
    public void deleteMemoryArray(char[] data, int position){

        //コピーの必要な部分はpositionから65535-dataまで
        for(int i=position;i<65535-data.length;i++){
            memory[i]=memory[i+data.length];
        }
        setMemoryArray(data,65535-data.length);
    }
    public void insertMemoryArray(char[] data, int position){

        //コピーの必要な部分はpositionから65535-dataまで
        for(int i=65535-data.length;i>=position;i--){
           memory[i+data.length]=memory[i];
        }
        setMemoryArray(data,position);
    }
    public void setMemoryArray(char[] data, int position) {
        for(int i = 0; i <data.length;i++){
            memory[i+position]=data[i];
        }
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


}
