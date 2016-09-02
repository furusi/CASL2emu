package com.example.furusho.casl2emu;

import android.app.ProgressDialog;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by furusho on 2016/09/02.
 */

public class DataSendTask extends SimpleTaskLoader implements DialogInterface.OnCancelListener{
    private Context myContext;
    private ProgressDialog myProgressDialog;
    private FTPClient myFTPClient;
    private String[] params;

    public DataSendTask(Context context,String... params) {
        super(context);
        myContext = context;
        this.params=params;
    }
    /**
     * @param params String... 0:FTPサーバーアドレス 1:FTPサーバーポート 2:サーバーフォルダ 3:ログインユーザID
     *               4:ログインパスワード 5:パッシブモード使用"0" or "1" 6:送信ファイル名
    */
    public void setParams(String... params){
        this.params=params;
    }



    /**
     * Subclasses must implement this to take care of loading their data,
     * as per {@link #startLoading()}.  This is not called by clients directly,
     * but as a result of a call to {@link #startLoading()}.
     */
    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        myProgressDialog = new ProgressDialog(myContext);
        myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        myProgressDialog.setCancelable(true);
        myProgressDialog.setOnCancelListener(this);
        myProgressDialog.setTitle("データ送信");
        myProgressDialog.setMessage("アップロード中");
        myProgressDialog.show();
    }


    @Override
    public Object loadInBackground() {
            String remoteserver = params[0];                 //FTPサーバーアドレス
            int remoteport = Integer.parseInt(params[1]);    //FTPサーバーポート
            String remotefile = params[2];                   //サーバーフォルダ
            String userid = params[3];                       //ログインユーザID
            String passwd = params[4];                       //ログインパスワード
            boolean passive = Boolean.valueOf(params[5]);    //パッシブモード使用
            String localFile = params[6];
            //ＦＴＰファイル送信
            FTP ftp = new FTP(myContext);
            String result = ftp.putData(remoteserver, remoteport, userid, passwd, passive, remotefile, localFile);
            ftp = null;

            return result;
    }

    /**
     * This method will be invoked when the dialog is canceled.
     *
     * @param dialog The dialog that was canceled will be passed into the
     *               method.
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        cancelLoad();

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
                FileInputStream fileInputStream = this.openFileInput(localFile);
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
