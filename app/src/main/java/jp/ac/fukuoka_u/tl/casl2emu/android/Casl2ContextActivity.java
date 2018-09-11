package jp.ac.fukuoka_u.tl.casl2emu.android;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import jp.ac.fukuoka_u.tl.casl2emu.R;

public class Casl2ContextActivity extends BaseActivity implements Casl2RegisterFragment.OnFragmentInteractionListener, Casl2MemoryFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_casl2_context);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
