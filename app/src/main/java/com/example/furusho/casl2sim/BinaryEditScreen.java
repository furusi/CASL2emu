package com.example.furusho.casl2sim;

import android.databinding.DataBindingUtil;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.inputmethod.InputBinding;
import android.widget.EditText;
import android.widget.TextView;

import com.example.furusho.casl2sim.databinding.ActivityBinaryEditScreenBinding;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;

public class BinaryEditScreen extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityBinaryEditScreenBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_binary_edit_screen);
        InputText it = new InputText();
        binding.setInputText(it);
    }
}
