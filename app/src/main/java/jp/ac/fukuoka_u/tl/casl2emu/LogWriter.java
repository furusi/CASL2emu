package jp.ac.fukuoka_u.tl.casl2emu;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import org.apache.commons.io.output.WriterOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by furusho on 2016/11/09.
 */

public class LogWriter {


    Context context;

    public LogWriter(Context context) {
        this.context = context;
    }

    FileWriter fileWriter;
   /*1日毎に分ける

    */
    //保存回数を出力.ファイル名も
    public void recordLogData(String data){

        SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(context);
        String dirname = Environment.getExternalStorageDirectory().getPath()+
                "Log/"+
                sharedPreferences.getString("userid","null");
        File dir = new File(dirname);
        if(!dir.exists()){
            dir.mkdirs();
        }
        //ファイル名の決定
        //日付を取得してファイル名へ
        File file = new File(dirname,save_filename);
        try {
            fileWriter = new FileWriter(file,true);
            fileWriter.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
