package com.example.furusho.casl2emu;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

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
        String remotefile = params.get(2);                   //サーバーフォルダ
        String userid = params.get(3);                       //ログインユーザID
        String passwd = params.get(4);                       //ログインパスワード
        boolean passive = Boolean.valueOf(params.get(5));    //パッシブモード使用
        String localFile = params.get(6);
        //ＦＴＰファイル送信
        FTP ftp = new FTP(myContext);
        String result = ftp.putData(remoteserver, remoteport, userid, passwd, passive, remotefile, localFile);
        ftp = null;

    }


    // インナークラス　ＦＴＰクライアント commons net使用
    private class FTP extends ContextWrapper {
        public FTP(Context base) {
            super(base);
        }

        private String putData(String remoteserver, int remoteport,
                               String userid, String passwd, boolean passive, String remotefile, String localFile) {
            int reply = 0;
            boolean isLogin = false;
            myFTPClient = new FTPClient();

            try {
                myFTPClient.setConnectTimeout(5000);
                //接続
                myFTPClient.connect(remoteserver, remoteport);
                reply = myFTPClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    throw new Exception("Connect Status:" + String.valueOf(reply));
                }
                //ログイン
                if (!myFTPClient.login(userid, passwd)) {
                    throw new Exception("Invalid user/password");
                }
                isLogin = true;
                //転送モード
                if (passive) {
                    myFTPClient.enterLocalPassiveMode(); //パッシブモード
                } else {
                    myFTPClient.enterLocalActiveMode();  //アクティブモード
                }//ファイル送信
                myFTPClient.setDataTimeout(15000);
                myFTPClient.setSoTimeout(15000);
                //FileInputStream fileInputStream = this.openFileInput(localFile);
                FileInputStream fileInputStream = new FileInputStream(new File(Environment.getExternalStorageDirectory().getPath()+getString(R.string.app_directory_name),"data.cl2"));
                myFTPClient.storeFile(remotefile, fileInputStream);
                reply = myFTPClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    throw new Exception("Send Status:" + String.valueOf(reply));
                }
                fileInputStream.close();
                fileInputStream = null;
                //ログアウト
                myFTPClient.logout();
                isLogin = false;
                //切断
                myFTPClient.disconnect();
            } catch (Exception e) {
                return e.getMessage();
            } finally {
                if (isLogin) {
                    try {
                        myFTPClient.logout();
                    } catch (IOException e) {
                    }
                }
                if (myFTPClient.isConnected()) {
                    try {
                        myFTPClient.disconnect();
                    } catch (IOException e) {
                    }
                }
                myFTPClient = null;
            }
            return null;
        }
    }
}
