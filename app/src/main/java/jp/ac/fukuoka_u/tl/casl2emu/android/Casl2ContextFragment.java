package jp.ac.fukuoka_u.tl.casl2emu.android;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.ac.fukuoka_u.tl.casl2emu.Casl2Emulator;
import jp.ac.fukuoka_u.tl.casl2emu.Casl2Register;
import jp.ac.fukuoka_u.tl.casl2emu.R;
import jp.ac.fukuoka_u.tl.casl2emu.databinding.FragmentCasl2ContextBinding;

import static android.R.layout.simple_list_item_1;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Casl2ContextFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Casl2ContextFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Casl2ContextFragment extends Fragment implements LoaderCallbacks,Runnable{


    private OnFragmentInteractionListener mListener;

    private GridView memoryView;
    private Casl2Register register;
    private ArrayList<String> stringArrayList;
    private Casl2Emulator emulator;
    private ArrayAdapter<String> arrayAdapter;
    private BroadcastReceiver receiver;
    private boolean activityVisible=false;
    private final AdapterView.OnItemClickListener showTextEditDialog = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String msg = String.valueOf(memoryView.getItemAtPosition(position));
            showTextDialog(msg,position);
        }
    };

    public Casl2ContextFragment() {
        // Required empty public constructor
    }


    public static Casl2ContextFragment newInstance() {
        Casl2ContextFragment fragment = new Casl2ContextFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final FragmentCasl2ContextBinding binding =
                DataBindingUtil.inflate(inflater,R.layout.fragment_casl2_context,container,false);

        register = Casl2Register.getInstance();
        emulator = Casl2Emulator.getInstance("jp.ac.fukuoka_u.tl.casl2emu.android.Casl2EmulatorAndroid");



        binding.setCasl2Register(register);
        binding.gr0.setOnClickListener(showWordDialog(binding,0));
        binding.gr0.setOnLongClickListener(jumpAddress(binding,0));
        binding.gr1.setOnClickListener(showWordDialog(binding,1));
        binding.gr1.setOnLongClickListener(jumpAddress(binding,1));
        binding.gr2.setOnClickListener(showWordDialog(binding,2));
        binding.gr2.setOnLongClickListener(jumpAddress(binding,2));
        binding.gr3.setOnClickListener(showWordDialog(binding,3));
        binding.gr3.setOnLongClickListener(jumpAddress(binding,3));
        binding.gr4.setOnClickListener(showWordDialog(binding,4));
        binding.gr4.setOnLongClickListener(jumpAddress(binding,4));
        binding.gr5.setOnClickListener(showWordDialog(binding,5));
        binding.gr5.setOnLongClickListener(jumpAddress(binding,5));
        binding.gr6.setOnClickListener(showWordDialog(binding,6));
        binding.gr6.setOnLongClickListener(jumpAddress(binding,6));
        binding.gr7.setOnClickListener(showWordDialog(binding,7));
        binding.gr7.setOnLongClickListener(jumpAddress(binding,7));
        binding.pc.setOnClickListener(showWordDialog(binding,8));
        binding.pc.setOnLongClickListener(jumpAddress(binding,8));
        binding.sp.setOnClickListener(showWordDialog(binding,9));
        binding.sp.setOnLongClickListener(jumpAddress(binding,9));
        binding.of.setOnClickListener(showWordDialog(binding,10));
        binding.sf.setOnClickListener(showWordDialog(binding,11));
        binding.zf.setOnClickListener(showWordDialog(binding,12));

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
                    final int valueType = intent.getIntExtra(getString(R.string.ValueType),0);

                    final Casl2EditText editView = new Casl2EditText(getActivity(),1);
                    switch (valueType){
                        case 0xFF00:
                            editView.setInputType(context,4);
                            break;
                        case 0xFF01:
                            editView.setInputType(context,3);
                            break;
                    }
                    new AlertDialog.Builder(getActivity())
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setView(R.layout.input_text_dialog)
                            .setTitle("SVC:数値を入力してください")
                            //setViewにてビューを設定します。
                            .setView(editView)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //入力した文字をトースト出力する
                                    String upperedString =editView.getText().toString().toUpperCase();
                                    String patternstr="";
                                    switch(valueType){
                                        case 0xFF00://符号付き
                                        case 0xFF01://符号無し
                                            patternstr = "-?\\d*";
                                            break;
                                        case 0xFF02:
                                            patternstr = getString(R.string.svc_input_pattern);

                                    }
                                    Pattern pattern = Pattern.compile(patternstr);
                                    Matcher matcher = pattern.matcher(upperedString);
                                    if (matcher.matches()) {
                                        //Toast.makeText(ContextDisplayScreen.this, upperedString, Toast.LENGTH_LONG).show();
                                        char[] input = Casl2EditText.getHexChars(upperedString," ");
                                        char[] chars = new char[input_length];
                                        switch(valueType){
                                            case 0xFF00://符号付き
                                                short s = Short.parseShort(upperedString);
                                                if (s >= Short.MIN_VALUE&&s<=Short.MAX_VALUE) {
                                                    register.setGr((char) s, position);
                                                }
                                                break;

                                            case 0xFF01://符号無し
                                                input[0] = (char) Integer.parseInt(upperedString);
                                                if (input[0] >= Character.MIN_VALUE&&input[0]<=Character.MAX_VALUE){
                                                    register.setGr(input[0],position);
                                                }
                                                break;
                                            case 0xFF02:
                                                if(chars.length>=input.length) {
                                                    Arrays.fill(chars, (char) 0x0);
                                                    for (int i = 0; i < input.length; i++) {

                                                        chars[i] = input[i];
                                                    }
                                                }
                                                refreshMemory(chars, position);
                                                break;
                                        }
                                        if(emulator.isInterruptflag()){
                                            emulator.setRunflag(true);
                                            emulator.setInterruptflag(false);
                                            emulator.run(PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(getString(R.string.intervalkey), 1000));
                                        }

                                    }else {
                                        Toast.makeText(getActivity(), "適切な文字列を入力してください", Toast.LENGTH_LONG).show();
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
        getActivity().registerReceiver(receiver,filter);

        return inflater.inflate(R.layout.fragment_casl2_context, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<ArrayList<String>> onCreateLoader(int id, Bundle args) {
        char[]cs = new char[0];
        int i = 0;
        if(args!=null){
            cs = args.getCharArray("cs");
            i = args.getInt("position");
        }
        return new ListDisplayTask(getActivity(),cs,i);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {

        if(arrayAdapter == null) {
            stringArrayList = (ArrayList<String>)data;
            arrayAdapter = new CustomArrayAdapter(memoryView.getContext(), simple_list_item_1,
                    stringArrayList, Typeface.MONOSPACE);
            memoryView.setAdapter(arrayAdapter);
        }else {
            arrayAdapter.clear();
            arrayAdapter.addAll((ArrayList<String>)data);
            arrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    public void run() {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private View.OnLongClickListener jumpAddress(final FragmentCasl2ContextBinding binding, final int id) {

        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                switch (id){
                    case 0:
                        memoryView.setSelection(Integer.parseInt(String.valueOf(binding.gr0.getText()), 16) / 4);
                        break;
                    case 1:
                        memoryView.setSelection(Integer.parseInt(String.valueOf(binding.gr1.getText()), 16) / 4);
                        break;
                    case 2:
                        memoryView.setSelection(Integer.parseInt(String.valueOf(binding.gr2.getText()), 16) / 4);
                        break;
                    case 3:
                        memoryView.setSelection(Integer.parseInt(String.valueOf(binding.gr3.getText()), 16) / 4);
                        break;
                    case 4:
                        memoryView.setSelection(Integer.parseInt(String.valueOf(binding.gr4.getText()), 16) / 4);
                        break;
                    case 5:
                        memoryView.setSelection(Integer.parseInt(String.valueOf(binding.gr5.getText()), 16) / 4);
                        break;
                    case 6:
                        memoryView.setSelection(Integer.parseInt(String.valueOf(binding.gr6.getText()), 16) / 4);
                        break;
                    case 7:
                        memoryView.setSelection(Integer.parseInt(String.valueOf(binding.gr7.getText()), 16) / 4);
                        break;
                    case 8:
                        memoryView.setSelection(Integer.parseInt(String.valueOf(binding.pc.getText()), 16) / 4);
                        break;
                    case 9:
                        memoryView.setSelection(Integer.parseInt(String.valueOf(binding.sp.getText()), 16) / 4);
                        break;
                }
                return true;
            }
        };
    }

    private View.OnClickListener showWordDialog(final FragmentCasl2ContextBinding binding, final int id) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Casl2EditText casl2EditText;
                final InputMethodManager inputMethodManager =  (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if(id<10){
                    casl2EditText = new Casl2EditText(getActivity(),1);
                }else {
                    casl2EditText = new Casl2EditText(getActivity(),2);
                }
                TextView textview = (TextView) v;
                casl2EditText.setText(textview.getText());
                casl2EditText.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                            //キーボードを閉じる
                            inputMethodManager.hideSoftInputFromWindow(casl2EditText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

                            return true;
                        }
                        return false;
                    }
                });
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(R.layout.input_text_dialog)
                        .setTitle("レジスタを編集")
                        //setViewにてビューを設定します。
                        .setView(casl2EditText)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //入力した文字をトースト出力する
                                String upperedString = casl2EditText.getText().toString().toUpperCase();
                                Pattern pattern;
                                if(id<10) {
                                    pattern = Pattern.compile(getString(R.string.word_pattern_wo_space));
                                }else {
                                    pattern = Pattern.compile(getString(R.string.boolean_pattern));
                                }
                                Matcher matcher = pattern.matcher(upperedString);
                                if (matcher.matches()) {
                                    //Toast.makeText(ContextDisplayScreen.this, upperedString, Toast.LENGTH_LONG).show();
                                    switch (id){
                                        case 0:
                                            binding.gr0.setText(upperedString);
                                            register.setGr((char)Integer.parseInt(upperedString,16),0);
                                            break;
                                        case 1:
                                            binding.gr1.setText(upperedString);
                                            register.setGr((char)Integer.parseInt(upperedString,16),1);
                                            break;
                                        case 2:
                                            binding.gr2.setText(upperedString);
                                            register.setGr((char)Integer.parseInt(upperedString,16),2);
                                            break;
                                        case 3:
                                            binding.gr3.setText(upperedString);
                                            register.setGr((char)Integer.parseInt(upperedString,16),3);
                                            break;
                                        case 4:
                                            binding.gr4.setText(upperedString);
                                            register.setGr((char)Integer.parseInt(upperedString,16),4);
                                            break;
                                        case 5:
                                            binding.gr5.setText(upperedString);
                                            register.setGr((char)Integer.parseInt(upperedString,16),5);
                                            break;
                                        case 6:
                                            binding.gr6.setText(upperedString);
                                            register.setGr((char)Integer.parseInt(upperedString,16),6);
                                            break;
                                        case 7:
                                            binding.gr7.setText(upperedString);
                                            register.setGr((char)Integer.parseInt(upperedString,16),7);
                                            break;
                                        case 8:
                                            binding.pc.setText(upperedString);
                                            register.setPc((char)Integer.parseInt(upperedString,16));
                                            break;
                                        case 9:
                                            binding.sp.setText(upperedString);
                                            register.setSp((char) Integer.parseInt(upperedString,16));
                                            break;
                                        case 10:
                                            binding.of.setText(upperedString);
                                            register.setFr((char) Integer.parseInt(upperedString,16),0 );
                                            break;
                                        case 11:
                                            binding.sf.setText(upperedString);
                                            register.setFr((char) Integer.parseInt(upperedString,16),1 );
                                            break;
                                        case 12:
                                            binding.zf.setText(upperedString);
                                            register.setFr((char) Integer.parseInt(upperedString,16),2 );
                                            break;
                                    }
                                } else {
                                    Toast.makeText(getActivity(), "適切な文字列を入力してください", Toast.LENGTH_LONG).show();
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

    private void showTextDialog(String text, final int rownum) {
        final Casl2EditText editView = new Casl2EditText(getActivity(),1);
        final InputMethodManager inputMethodManager =  (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        editView.setText(text);
        editView.setMaxLines(1);
        editView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    //キーボードを閉じる
                    inputMethodManager.hideSoftInputFromWindow(editView.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

                    return true;
                }
                return false;
            }
        });
        new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(R.layout.input_text_dialog)
                .setTitle("メモリを編集: "+String.format(Locale.US,"0x%04X",rownum & 0xFFFF))
                //setViewにてビューを設定します。
                .setView(editView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //入力した文字をトースト出力する
                        String upperedString =editView.getText().toString().toUpperCase();
                        Pattern pattern = Pattern.compile(getString(R.string.memory_row_pattern_1));
                        Matcher matcher = pattern.matcher(upperedString);
                        if (matcher.matches()) {
                            char[] chars = Casl2EditText.getHexChars(upperedString," ");
                            emulator.setMemoryArray(chars, rownum);
                            refreshMemoryPane(rownum,0);

                        }else {
                            Toast.makeText(getActivity(), "適切な文字列を入力してください", Toast.LENGTH_LONG).show();
                        }

                    }
                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    private void refreshMemoryPane(int rownum,int refreshMode) {
        switch(refreshMode){
            case 0://通常の更新
                stringArrayList.remove(rownum);
                arrayAdapter.insert(String.format(Locale.US ,getString(R.string.HEX_REGEX_1), emulator.getMemory(rownum) & 0xFFFF),rownum);
                break;
            case 1://挿入の更新
                arrayAdapter.insert(String.format(Locale.US ,getString(R.string.HEX_REGEX_4),
                        emulator.getMemory(4*rownum) & 0xFFFF, emulator.getMemory(4*rownum+1) & 0xFFFF,
                        emulator.getMemory(4*rownum+2) & 0xFFFF, emulator.getMemory(4*rownum+3) & 0xFFFF),rownum);
                stringArrayList.remove(arrayAdapter.getCount()-1);
                break;
            case 2://削除の更新
                stringArrayList.remove(rownum);
                arrayAdapter.insert(String.format(Locale.US ,getString(R.string.HEX_REGEX_4),
                        emulator.getMemory(0xFFFC) & 0xFFFF, emulator.getMemory(0xFFFD) & 0xFFFF,
                        emulator.getMemory(0xFFFE) & 0xFFFF, emulator.getMemory(0xFFFF) & 0xFFFF),arrayAdapter.getCount()-1);
                break;
        }

        arrayAdapter.notifyDataSetChanged();
    }

    protected void refreshMemory(char[] data, char position) {
        emulator.refreshMemory(data,position);
    }
    private void startListTask(char[]cs,int i){
        Bundle bundle=new Bundle();
        bundle.putCharArray("cs",cs);
        bundle.putInt("position",i);
        getActivity().getLoaderManager().initLoader(0,bundle,this).forceLoad();
    }
}
