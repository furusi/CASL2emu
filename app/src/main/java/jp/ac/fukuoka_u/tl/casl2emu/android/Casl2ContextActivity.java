package jp.ac.fukuoka_u.tl.casl2emu.android;

import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


import jp.ac.fukuoka_u.tl.casl2emu.R;
import jp.ac.fukuoka_u.tl.casl2emu.databinding.ActivityCasl2ContextBinding;

import static android.support.constraint.Constraints.TAG;

public class Casl2ContextActivity extends BaseActivity implements Casl2RegisterFragment.OnFragmentInteractionListener, Casl2MemoryFragment.OnFragmentInteractionListener,Casl2KeypadFragment.OnFragmentInteractionListener{

    ActivityCasl2ContextBinding binding;
    Casl2MemoryFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_casl2_context);

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    public void onFragmentInteraction(String str) {
        Log.d(TAG,str+"を受け取った");
        if(fragment==null) {
            fragment = (Casl2MemoryFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_memory);
        }
        fragment.onKeycodeSent(str);
    }
}
