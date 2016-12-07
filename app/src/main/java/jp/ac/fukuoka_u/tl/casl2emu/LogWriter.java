package jp.ac.fukuoka_u.tl.casl2emu;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;

import org.apache.commons.io.output.WriterOutputStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by furusho on 2016/11/09.
 */

public class LogWriter implements AutoCloseable{


    Context context;
    Casl2Ftp ftp;

    public LogWriter(Context context) {
        this.context = context;
        ftp = new Casl2Ftp(context);
    }


   /*1日毎に分ける

    */
    //保存回数を出力.ファイル名も
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void recordLogData(String data){

        SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(context);
        String userid = sharedPreferences.getString("userid","null");
        String dirname = Environment.getExternalStorageDirectory().getPath()+
                "/Log/"+ userid;
        File dir = new File(dirname);
        if(!dir.exists()){
            dir.mkdirs();
        }
        //ファイル名の決定
        //日付を取得してファイル名へ
        final SimpleDateFormat df = new SimpleDateFormat("yyyyMMddkkmmss");
        final Date date = new Date(System.currentTimeMillis());
        String s_date = android.text.format.DateFormat.format("yyyyMMddkkmmss",date).toString();
        if(date!=null){
            String save_filename = DateFormat.format("yyyyMMdd",date).toString()+".log";
            File file = new File(dirname,userid+save_filename);
            try(FileWriter fileWriter = new FileWriter(file,true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                bufferedWriter.write(s_date+","+data+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Closes the object and release any system resources it holds.
     */
    @Override
    public void close() throws Exception {

    }
}
