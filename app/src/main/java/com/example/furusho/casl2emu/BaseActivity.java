package com.example.furusho.casl2emu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseActivity extends AppCompatActivity {

    private boolean activityVisible=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        /**
         *
         */



        IntentFilter filter;
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(activityVisible){


                    final HexEditText editView = new HexEditText(BaseActivity.this,1);
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
                                    Pattern pattern = Pattern.compile(getString(R.string.memory_row_pattern));
                                    Matcher matcher = pattern.matcher(upperedString);
                                    if (matcher.matches()) {
                                        //Toast.makeText(ContextDisplayScreen.this, upperedString, Toast.LENGTH_LONG).show();
                                        char[] chars = getHexChars(upperedString," ");
                                        memory.setMemoryArray(chars, position*4);
                                        stringArrayList.remove(position);
                                        arrayAdapter.insert(String.format(Locale.US ,"%04X %04X %04X %04X",
                                                chars[0] & 0xFFFF, chars[1] & 0xFFFF, chars[2] & 0xFFFF, chars[3] & 0xFFFF),position);
                                        arrayAdapter.notifyDataSetChanged();

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
}
