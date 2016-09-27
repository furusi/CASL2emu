package jp.ac.fukuoka_u.tl.casl2emu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseActivity extends AppCompatActivity {

    private boolean activityVisible=false;
    Casl2Memory memory;
    Casl2Emulator emulator;
    ArrayList<String> stringArrayList;
    ArrayAdapter<String> arrayAdapter;
    BroadcastReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        /**
         *
         */
        memory = Casl2Memory.getInstance();
        emulator = Casl2Emulator.getInstance(getApplicationContext());



        IntentFilter filter;
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(activityVisible){


                    if(emulator.isInterruptflag()){
                        emulator.waitEmu();
                    }
                    final char position = intent.getCharExtra(context.getString(R.string.memory_position), (char) 0x0000);
                    final char input_length = intent.getCharExtra(context.getString(R.string.input_length), (char) 0x0001);

                    final Casl2EditText editView = new Casl2EditText(BaseActivity.this,1);
                    new AlertDialog.Builder(BaseActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setView(R.layout.input_text_dialog)
                            .setTitle("SVC:数値を入力してください")
                            //setViewにてビューを設定します。
                            .setView(editView)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //入力した文字をトースト出力する
                                    String upperedString =editView.getText().toString().toUpperCase();
                                    Pattern pattern = Pattern.compile(getString(R.string.svc_input_pattern));
                                    Matcher matcher = pattern.matcher(upperedString);
                                    if (matcher.matches()) {
                                        //Toast.makeText(ContextDisplayScreen.this, upperedString, Toast.LENGTH_LONG).show();
                                        char[] input = Casl2EditText.getHexChars(upperedString," ");
                                        char[] chars = new char[input_length];
                                        if(chars.length>=input.length) {
                                            Arrays.fill(chars, (char) 0x0);
                                            for (int i = 0; i < input.length; i++) {

                                                chars[i] = input[i];
                                            }
                                            refreshMemory(chars, position);
                                            emulator.run(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(getString(R.string.intervalkey), 1000));
                                        }

                                    }else {
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
        };
        filter = new IntentFilter(getString(R.string.action_svc_input));
        registerReceiver(receiver,filter);
    }

    protected void refreshMemory(char[] data, char position) {
        memory.setMemoryArray(data, position);
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
}
