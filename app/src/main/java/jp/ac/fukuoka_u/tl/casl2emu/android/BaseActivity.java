package jp.ac.fukuoka_u.tl.casl2emu.android;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.ac.fukuoka_u.tl.casl2emu.Casl2Emulator;
import jp.ac.fukuoka_u.tl.casl2emu.Casl2Register;
import jp.ac.fukuoka_u.tl.casl2emu.R;


public class BaseActivity extends AppCompatActivity {

    protected static final int REQUEST_WRITE_STORAGE = 112;
    private boolean activityVisible=false;
    Casl2Register register;
    Casl2EmulatorAndroid emulator;
    ArrayList<String> stringArrayList;
    ArrayAdapter<String> arrayAdapter;
    BroadcastReceiver receiver;
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        register = Casl2Register.getInstance();
        emulator = (Casl2EmulatorAndroid) Casl2Emulator.getInstance("jp.ac.fukuoka_u.tl.casl2emu.android.Casl2EmulatorAndroid");
        emulator.initializeInstanceAndroid(getApplicationContext());

        handler = new Handler(Looper.getMainLooper());
        IntentFilter inputFilter,uploadFilter;
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(getString(R.string.action_svc_input))) {
                    if (activityVisible) {


                        if (emulator.isInterruptflag()) {
                            emulator.waitEmu();
                        }
                        final char position = intent.getCharExtra(context.getString(R.string.memory_position), (char) 0x0000);
                        final char input_length = intent.getCharExtra(context.getString(R.string.input_length), (char) 0x0001);
                        final int valueType = intent.getIntExtra(getString(R.string.ValueType), 0);

                        final Casl2EditText editView = new Casl2EditText(BaseActivity.this, 1);
                        switch (valueType) {
                            case 0xFF00:
                                editView.setInputType(context, 4);
                                break;
                            case 0xFF01:
                                editView.setInputType(context, 3);
                                break;
                        }
                        new AlertDialog.Builder(BaseActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .setView(R.layout.input_text_dialog)
                                .setTitle("SVC:数値を入力してください")
                                //setViewにてビューを設定します。
                                .setView(editView)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //入力した文字をトースト出力する
                                        String upperedString = editView.getText().toString().toUpperCase();
                                        String patternstr = "";
                                        switch (valueType) {
                                            case 0xFF00://符号付き
                                            case 0xFF01://符号無し
                                                patternstr = "-?\\d*";
                                                break;
                                            case 0xFF02:
                                                patternstr = getString(R.string.svc_input_pattern);

                                        }
                                        Pattern pattern = Pattern.compile(patternstr);
                                        Matcher matcher = pattern.matcher(upperedString);
                                        if (matcher.matches()) {
                                            //Toast.makeText(ContextDisplayScreen.this, upperedString, Toast.LENGTH_LONG).show();
                                            char[] input = Casl2EditText.getHexChars(upperedString, " ");
                                            char[] chars = new char[input_length];
                                            switch (valueType) {
                                                case 0xFF00://符号付き
                                                    short s = Short.parseShort(upperedString);
                                                    register.setGr((char) s, position);
                                                    break;

                                                case 0xFF01://符号無し
                                                    input[0] = (char) Integer.parseInt(upperedString);
                                                    register.setGr(input[0], position);
                                                    break;
                                                case 0xFF02:
                                                    if (chars.length >= input.length) {
                                                        Arrays.fill(chars, (char) 0x0);
                                                        System.arraycopy(input, 0, chars, 0, input.length);
                                                    }
                                                    refreshMemory(chars, position);
                                                    break;
                                            }
                                            if (emulator.isInterruptflag()) {
                                                emulator.setRunflag(true);
                                                emulator.setInterruptflag(false);
                                                emulator.run(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(getString(R.string.intervalkey), 1000));
                                            }

                                        } else {
                                            Toast.makeText(BaseActivity.this, "適切な文字列を入力してください", Toast.LENGTH_LONG).show();
                                        }

                                    }
                                })
                                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                })
                                .show();
                    }
                }
                if(intent.getAction().equals(getString(R.string.assginment_upload_complete))) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            new MaterialDialog.Builder(BaseActivity.this).
                                    title("アップロード成功").content("課題のアップロードに成功しました。")
                                    .positiveText("OK")
                                    .positiveColor(0)
                                    .cancelable(false)
                                    .show();
                        }
                    });
                }
            }
        };

        inputFilter = new IntentFilter(getString(R.string.action_svc_input));
        uploadFilter = new IntentFilter(getString(R.string.assginment_upload_complete));

        registerReceiver(receiver,inputFilter);
        registerReceiver(receiver,uploadFilter);
    }

    protected void refreshMemory(char[] data, char position) {
        emulator.refreshMemory(data,position);
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        activityVisible=true;
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        activityVisible=false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    protected void makeDir(String dirname) {
        File dir = new File(dirname);
        boolean hasPermission = (ContextCompat.checkSelfPermission(BaseActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(BaseActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }
        if(!dir.exists()){
            dir.mkdirs();
        }
    }

    protected void logging(String name,String value){
        LogSerializable  log;
            log = new LogSerializable(name,value);
        startService(new Intent(getApplicationContext(),Casl2LogWriter.class)
                .putExtra("log",log));
    }
}
