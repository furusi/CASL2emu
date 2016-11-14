package jp.ac.fukuoka_u.tl.casl2emu;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by furusho on 2016/09/02.
 */

public class DataSendTask extends IntentService{
    private Context myContext;
    private ProgressDialog myProgressDialog;
    private FTPClient myFTPClient;
    private ArrayList<String> params;


    public DataSendTask() {
        super("DataSendTask");
    }


    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               Context#startService(Intent)}.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        params=intent.getStringArrayListExtra("data");

        myContext = getApplicationContext();
        String remoteserver = params.get(0);                 //FTPサーバーアドレス
        int remoteport = Integer.parseInt(params.get(1));    //FTPサーバーポート
        String remotefile = String.valueOf(
                intent.getCharSequenceExtra("kadaifilename"));                   //サーバーフォルダ
        String userid = params.get(3);                       //ログインユーザID
        String passwd = params.get(4);                       //ログインパスワード
        boolean passive = Boolean.valueOf(params.get(5));    //パッシブモード使用
        String localFile = params.get(6);
        //ＦＴＰファイル送信
        Casl2Ftp ftp = new Casl2Ftp(myContext);
        String result = ftp.putData(remoteserver, remoteport, userid, passwd, passive, remotefile, localFile);
        ftp = null;

    }


}
