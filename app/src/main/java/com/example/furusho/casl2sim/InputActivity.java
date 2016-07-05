package com.example.furusho.casl2sim;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import android.content.Intent;
import android.widget.TextView;

import com.google.common.collect.Lists;

import java.util.List;


public class InputActivity extends AppCompatActivity {

    @BindView(R.id.runbutton) Button runButton;
    @BindView(R.id.editText) TextView editedTextView;

    @OnClick(R.id.runbutton) void onClickRunButton(Button b){
        String[] text= editedTextView.getText().toString().split("\n");
        //List source = Lists.newArrayList(text);
        Intent intent =new Intent(this.getApplication(),OutputScreen.class);
        intent.putExtra("sourcecode",text);
        this.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.furusho.casl2sim.R.layout.activity_main);
        ButterKnife.bind(this);
    }


}
