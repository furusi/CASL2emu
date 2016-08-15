package com.example.furusho.casl2emu;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.furusho.casl2emu.databinding.ActivityBinaryEditScreenBinding;

import org.apache.commons.codec.binary.Hex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.R.layout.activity_list_item;
import static android.R.layout.simple_list_item_1;

public class ContextDisplayScreen extends BaseActivity {

    InputText it;
    ListView listView;
    Casl2Memory listItems;
    Casl2Register register;
    ArrayAdapter<String> arrayAdapter;


    private final AdapterView.OnItemClickListener showTextEditDialog = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String msg = String.valueOf(listView.getItemAtPosition(position));
            showTextDialog(msg,position);
        }

    };

    private void showTextDialog(String text, final int position) {
        final HexEditText editView = new HexEditText(ContextDisplayScreen.this);
        editView.setText(text);
        new AlertDialog.Builder(ContextDisplayScreen.this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(R.layout.input_text_dialog)
                .setTitle("テキスト入力ダイアログ")
                //setViewにてビューを設定します。
                .setView(editView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //入力した文字をトースト出力する
                        String upperedString =editView.getText().toString().toUpperCase();
                        Pattern pattern = Pattern.compile(getString(R.string.memory_row_pattern));
                        Matcher matcher = pattern.matcher(upperedString);
                        if (matcher.matches()) {
                            Toast.makeText(ContextDisplayScreen.this, upperedString, Toast.LENGTH_LONG).show();
                            listItems.setMemory(upperedString, position);
                            arrayAdapter.addAll(listItems.getMemory());
                            arrayAdapter.notifyDataSetChanged();
                        }else {
                            Toast.makeText(ContextDisplayScreen.this, "適切な文字列を入力してください", Toast.LENGTH_LONG).show();
                        }

                    }
                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    private String getWord(int offset, CharSequence line) {
        String ret="";
        if((offset+1)%3==1){//一桁目ならoffset-1をとる
            ret= String.valueOf(line.charAt(offset))+String.valueOf(line.charAt(offset+1));
        }else if((offset+1)%3==2){
            ret= String.valueOf(line.charAt(offset-1))+String.valueOf(line.charAt(offset));

        }
        return ret;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binary_edit_screen);

        listItems = Casl2Memory.getInstance();
        register = Casl2Register.getInstance();

        final ActivityBinaryEditScreenBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_binary_edit_screen);
        char[] test = new char[]{78,0,9,8,78,7,5,23};
        register.setGr(test);
        binding.setCasl2Register(register);
        binding.gr0.setOnClickListener(showWordDialog(binding,0));
        binding.gr1.setOnClickListener(showWordDialog(binding,1));
        binding.gr2.setOnClickListener(showWordDialog(binding,2));
        binding.gr3.setOnClickListener(showWordDialog(binding,3));
        binding.gr4.setOnClickListener(showWordDialog(binding,4));
        binding.gr5.setOnClickListener(showWordDialog(binding,5));
        binding.gr6.setOnClickListener(showWordDialog(binding,6));
        binding.gr7.setOnClickListener(showWordDialog(binding,7));
        arrayAdapter = new CustomArrayAdapter(this,
                simple_list_item_1,
                listItems.getMemory(),
                Typeface.MONOSPACE);
        arrayAdapter.addAll(getString(R.string.short_zerofill).split("\\n"));

        listView = (ListView)findViewById(R.id.memory_list);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(showTextEditDialog);

    }

    public AdapterView.OnItemClickListener getShowTextEditDialog(){
        return showTextEditDialog;
    }

    //private void showToast() {
    //    Toast.makeText(ContextDisplayScreen.this,"is touched!!",Toast.LENGTH_SHORT).show();
    //}
    private void showToast(String offset) {
        Toast.makeText(ContextDisplayScreen.this,offset+" is touched!!",Toast.LENGTH_SHORT).show();
    }
    private View.OnClickListener showWordDialog(final ActivityBinaryEditScreenBinding binding, final int id) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final HexEditText hexEditText = new HexEditText(ContextDisplayScreen.this);
                TextView textview = (TextView) v;
                //Log.d("dbg",textview.getAccessibilityClassName().toString());
                hexEditText.setText(textview.getText());
                new AlertDialog.Builder(ContextDisplayScreen.this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(R.layout.input_text_dialog)
                        .setTitle("テキスト入力ダイアログ")
                        //setViewにてビューを設定します。
                        .setView(hexEditText)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //入力した文字をトースト出力する
                                String upperedString = hexEditText.getText().toString().toUpperCase();
                                Pattern pattern = Pattern.compile(getString(R.string.word_pattern_wo_space));
                                Matcher matcher = pattern.matcher(upperedString);
                                if (matcher.matches()) {
                                    Toast.makeText(ContextDisplayScreen.this, upperedString, Toast.LENGTH_LONG).show();
                                    switch (id){
                                        case 0:
                                            binding.gr0.setText(upperedString);
                                            break;
                                        case 1:
                                            binding.gr1.setText(upperedString);
                                            break;
                                        case 2:
                                            binding.gr2.setText(upperedString);
                                            break;
                                        case 3:
                                            binding.gr3.setText(upperedString);
                                            break;
                                        case 4:
                                            binding.gr4.setText(upperedString);
                                            break;
                                        case 5:
                                            binding.gr5.setText(upperedString);
                                            break;
                                        case 6:
                                            binding.gr6.setText(upperedString);
                                            break;
                                        case 7:
                                            binding.gr7.setText(upperedString);
                                            break;
                                    }
                                } else {
                                    Toast.makeText(ContextDisplayScreen.this, "適切な文字列を入力してください", Toast.LENGTH_LONG).show();
                                }

                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();

            }
        };
    }
}

class HexEditText extends EditText {
    public HexEditText(Context context) {
        super(context);
        this.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        this.setKeyListener(DigitsKeyListener.getInstance("0123456789abcdefABCDEF "));
        this.setTypeface(Typeface.MONOSPACE);
    }
}

class ContextEditDialog extends AlertDialog{

    protected ContextEditDialog(final Context context, final HexEditText hexEditTextText) {
        super(context);
    }
        /*
        this.Builder(getOwnerActivity());
        this.setIcon(android.R.drawable.ic_dialog_info);
        this.setTitle("テキスト入力ダイアログ");
            //setViewにてビューを設定します。
        this.setView(hexEditTextText);
        this.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //入力した文字をトースト出力する
                String upperedString =hexEditTextText.getText().toString().toUpperCase();
                Pattern pattern = Pattern.compile(context.getString(R.string.memory_row_pattern));
                Matcher matcher = pattern.matcher(upperedString);
                if (matcher.matches()) {
                    hexEditTextText.setText(upperedString);
                }else {
                }

            }
        });
        this.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        })
        .show();
    }
*/
}
