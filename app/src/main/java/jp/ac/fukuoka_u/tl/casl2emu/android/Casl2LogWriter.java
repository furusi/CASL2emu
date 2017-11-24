package jp.ac.fukuoka_u.tl.casl2emu.android;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.ac.fukuoka_u.tl.casl2emu.android.Casl2Ftp;

/**
 * Created by furusho on 2016/11/09.
 */

public class Casl2LogWriter extends IntentService implements AutoCloseable{


    Casl2Ftp ftp;
    SharedPreferences sharedPreferences;
    String userid;
    String dirname;
    File dir;
    Date date;

    String save_filename = null;

    public Casl2LogWriter() {
        super("Casl2LogWriter");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ftp = new Casl2Ftp(this);
        sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(this);
        userid = sharedPreferences.getString("userid","null");
        dirname = Environment.getExternalStorageDirectory().getPath()+
                "/Log/CASL2Emu/"+ userid;
        dir = new File(dirname);
        date = new Date(System.currentTimeMillis());
        save_filename = DateFormat.format("yyyyMMdd",date).toString()+".log";
    }

    /**
     * Closes the object and release any system resources it holds.
     */
    @Override
    public void close() throws Exception {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*1日毎に分ける

     */
    //保存回数を出力.ファイル名も
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        writeLog(intent);
    }

    private void writeLog(@Nullable Intent intent)  {
        if (intent != null && intent.hasExtra("log")) {
            LogSerializable data  = (LogSerializable) intent.getSerializableExtra("log");
            JSONObject json = new JSONObject();
            if(!dir.exists()){
                dir.mkdirs();
            }
            //ファイル名の決定
            //日付を取得してファイル名へ
            date = new Date(System.currentTimeMillis());
            String s_date = DateFormat.format("yyyyMMddkkmmss",date).toString();
            try {
                json.put("date",s_date);
                json.put("data",data.data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(date!=null){
                File file = new File(dirname,userid+save_filename);
                try(FileWriter fileWriter = new FileWriter(file,true);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                    //bufferedWriter.write(s_date+","+data+"\n");
                    bufferedWriter.write(json.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
