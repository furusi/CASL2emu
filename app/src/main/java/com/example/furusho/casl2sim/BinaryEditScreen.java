package com.example.furusho.casl2sim;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputBinding;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.furusho.casl2sim.databinding.ActivityBinaryEditScreenBinding;
import com.google.common.base.Strings;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;

public class BinaryEditScreen extends AppCompatActivity {

    InputText it;

    private final View.OnClickListener showToastListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            //showToast();
        }


    };

    private final View.OnTouchListener showTouchListener = new View.OnTouchListener(){


        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction()==MotionEvent.ACTION_DOWN) {
                Layout layout = ((TextView) v).getLayout();
                String selectedWord;
                int x = (int) event.getX();
                int y = (int) event.getY();
                if (layout != null) {
                    int lineNo = layout.getLineForVertical(y);
                    int offset = layout.getOffsetForHorizontal(lineNo, x);
                    CharSequence line = layout.getText();
                    if((offset+1)%3!=0){//選択したところが空白で無ければ
                        //selectedWord = getWord(offset, line);
                        //showToast(selectedWord);
                        final EditText editView = new EditText(BinaryEditScreen.this);
                        new AlertDialog.Builder(BinaryEditScreen.this)
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .setTitle("テキスト入力ダイアログ")
                                //setViewにてビューを設定します。
                                .setView(editView)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //入力した文字をトースト出力する
                                        //Toast.makeText(BinaryEditScreen.this, editView.getText().toString(), Toast.LENGTH_LONG).show();
                                    }
                                })
                                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                })
                                .show();
                    }

                }
            }
            return true;
        }
    };

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
        ActivityBinaryEditScreenBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_binary_edit_screen);
        binding.setBinaryEditScreen(this);

        it = new InputText();
        String bintext = getString(R.string.zerofill);
        String rowNum=getString(R.string.rownumber);
        it.setInputText(bintext);
        it.setRowNum(rowNum);
        binding.setInputText(it);
    }

    public View.OnTouchListener getShowToastListener(){
        return showTouchListener;
    }

    //private void showToast() {
    //    Toast.makeText(BinaryEditScreen.this,"is touched!!",Toast.LENGTH_SHORT).show();
    //}
    private void showToast(String offset) {
        Toast.makeText(BinaryEditScreen.this,offset+" is touched!!",Toast.LENGTH_SHORT).show();
    }
}
