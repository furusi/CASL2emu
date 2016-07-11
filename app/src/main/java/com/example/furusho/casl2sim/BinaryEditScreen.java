package com.example.furusho.casl2sim;

import android.databinding.DataBindingUtil;
import android.renderscript.ScriptGroup;
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
            Layout layout = ((TextView) v).getLayout();
            int x = (int)event.getX();
            int y = (int)event.getY();
            if(layout!=null){
                int line =layout.getLineForVertical(y);
                int offset =layout.getOffsetForHorizontal(line, x);
                showToast(offset);
            }
            return true;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityBinaryEditScreenBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_binary_edit_screen);
        binding.setBinaryEditScreen(this);

        it = new InputText();
        String bintext = Pattern.compile("(..)").matcher(it.getInputText()).replaceAll("$0 ");
        it.setInputText(bintext);
        binding.setInputText(it);
    }

    public View.OnTouchListener getShowToastListener(){
        return showTouchListener;
    }

    //private void showToast() {
    //    Toast.makeText(BinaryEditScreen.this,"is touched!!",Toast.LENGTH_SHORT).show();
    //}
    private void showToast(int offset) {
        Toast.makeText(BinaryEditScreen.this,offset+" is touched!!",Toast.LENGTH_SHORT).show();
    }
}
