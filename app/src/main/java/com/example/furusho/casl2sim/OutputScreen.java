package com.example.furusho.casl2sim;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.common.base.Joiner;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OutputScreen extends AppCompatActivity {

    @BindView(R.id.output) TextView textView;

    String[] code;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output_screen);
        code = this.getIntent().getStringArrayExtra(new String("sourcecode"));
        ButterKnife.bind(this);

        Commetii cm = new Commetii(code);
    }

    @Override
    protected void onStart() {
        super.onStart();
        textView.setText(Joiner.on("\n").skipNulls().join(code));
    }
}
