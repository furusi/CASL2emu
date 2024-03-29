package jp.ac.fukuoka_u.tl.casl2emu.android;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;

import jp.ac.fukuoka_u.tl.casl2emu.R;

/**
 * Created by furusho on 2016/09/26.
 */

public class Casl2Ftp extends ContextWrapper {
    FTPClient myFTPClient;
        public Casl2Ftp(Context base) {
            super(base);
        }

        public boolean putData(InetSocketAddress remoteserver, String userid, String passwd,
                               boolean passive, String remotefile, String localFile,int kadaiNum) {
            boolean isLogin = false;
            if(myFTPClient==null) myFTPClient = new FTPClient();
            Date uploaddate = getDate();
            Handler handler = new Handler(Looper.getMainLooper());
            String s_date =
                    android.text.format.DateFormat.format("yyyyMMddkkmmss",uploaddate).toString();

            try (FileInputStream fileInputStream = new FileInputStream(new File(localFile))){
                isLogin = ftpLogin(remoteserver, userid, passwd);
                //転送モード
                if (passive) {
                    myFTPClient.enterLocalPassiveMode(); //パッシブモード
                } else {
                    myFTPClient.enterLocalActiveMode();  //アクティブモード
                }//ファイル送信
                myFTPClient.setDataTimeout(15000);
                myFTPClient.setSoTimeout(15000);

                //FileInputStream fileInputStream = this.openFileInput(localFile);

                myFTPClient.storeFile("~/"+s_date+remotefile, fileInputStream);
                int reply = myFTPClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    showToastMessage(handler,"FTPサーバからの応答が不正です．");

                }else{
                    Date date = getDate();
                    SharedPreferences.Editor editor = PreferenceManager.
                            getDefaultSharedPreferences(getApplicationContext()).edit();
                    editor.putString(userid+"-"+kadaiNum,
                            DateFormat.format("yyyy年MM月dd日kk時mm分",date).toString());
                    editor.apply();
                }

            } catch (Exception e){
                showToastMessage(handler,e.getMessage());
                return false;
            }finally {
                disconnect();
            }
            Intent auIntent = new Intent(getString(R.string.assginment_upload_complete));
            getApplicationContext().sendBroadcast(auIntent);
            return true;
        }

    private void disconnect() {
        if(myFTPClient != null){
            try {
                myFTPClient.logout();
                myFTPClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                myFTPClient = null;
            }
        }

    }

    private void showToastMessage(Handler handler, final String str) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),str,Toast.LENGTH_LONG).show();
            }
        });
    }

    public Date getDate() {
        Date date = null;
        NTPUDPClient ntpudpClient = null;
        try {
            ntpudpClient = new NTPUDPClient();
            TimeInfo timeInfo;
            ntpudpClient.open();
            timeInfo = ntpudpClient.getTime(InetAddress.getByName("ntp.nict.jp"));
            date = new Date(timeInfo.getReturnTime());
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(ntpudpClient!=null)
            ntpudpClient.close();
        }
        return date;
    }

    public boolean appLogin(InetSocketAddress  remoteserver, String userid, String passwd) throws Exception {
            //IDがtlguestだったらtrue,loginできてももちろんtrue．最終的にftpclientはnullにする．
        boolean ret = ftpLogin(remoteserver, userid, passwd);
        disconnect();
        return userid.equals("TLGUEST") || ret;
    }

    public boolean ftpLogin(InetSocketAddress  remoteserver, String userid, String passwd) throws Exception {
        int reply;

        if(myFTPClient==null)
            myFTPClient=new FTPClient();
        myFTPClient.setConnectTimeout(5000);
        if(userid.equals("")){
            return false;
        }
        //接続
        myFTPClient.connect(remoteserver.getHostName(),remoteserver.getPort());
        reply = myFTPClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            throw new Exception("Connect Status:" + String.valueOf(reply));
        }
        //ログイン
        if (!myFTPClient.login(userid, passwd)) {
            throw new Exception("Invalid user/password");
        }

        Date date = getDate();
        if(date !=null){
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
            editor.putString("LastLoginDate", DateFormat.format("yyyyMMdd",date).toString());
            editor.apply();
            return true;
        }

        //myFTPClient.disconnect();
        return false;
    }
}
