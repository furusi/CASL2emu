package jp.ac.fukuoka_u.tl.casl2emu.android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Locale;

import jp.ac.fukuoka_u.tl.casl2emu.Casl2Emulator;
import jp.ac.fukuoka_u.tl.casl2emu.R;

import static android.R.layout.simple_list_item_1;
import static android.support.constraint.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Casl2MemoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Casl2MemoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Casl2MemoryFragment extends Fragment implements LoaderManager.LoaderCallbacks{

    private ArrayAdapter<String> arrayAdapter;
    private OnFragmentInteractionListener mListener;
    private Casl2Emulator emulator;
    private GridView memoryView;
    private int posFocus = -1;
    ArrayList<String> stringArrayList;
    private View lastFocusedView;
    private int inputCount = 0;


    public Casl2MemoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Casl2MemoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Casl2MemoryFragment newInstance() {

        return new Casl2MemoryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        emulator = Casl2EmulatorAndroid.getInstance("jp.ac.fukuoka_u.tl.casl2emu.android.Casl2EmulatorAndroid");

        View view = inflater.inflate(R.layout.fragment_casl2_memory, container, false);
        memoryView = view.findViewById(R.id.memory_grid2);
        memoryView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

        setMemoryAdapter(showMemory(),0);
        AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "setOnItemClickListener() - pos:" + String.valueOf(i));

                changeFocus(view, i);
            }
        };
        memoryView.setOnItemClickListener(onItemClickListener);
        return view;
    }

    private void changeFocus(View view, int i) {
        inputCount = 0;
        if(lastFocusedView!=null){//選択していたセルの色を戻す
            lastFocusedView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        }
        view.setBackgroundColor(Color.GREEN);
        lastFocusedView = view;
        posFocus = i;
    }

    private char[] showMemory() {
        return emulator.getMemory();
    }

    private void setMemoryAdapter(char[] chars, int position) {
        Bundle bundle=new Bundle();
        bundle.putCharArray("cs",chars);
        bundle.putInt("position",position);
        getLoaderManager().initLoader(0,bundle,this).forceLoad();
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

    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {
        char[]cs = new char[0];
        int i = 0;
        if(args!=null){
            cs = args.getCharArray("cs");
            i = args.getInt("position");
        }
        return new ListDisplayTask(getActivity(),cs,i);
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        if(arrayAdapter == null) {
            stringArrayList = (ArrayList<String>) data;
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
    public void onLoaderReset(@NonNull Loader loader) {

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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public  void onKeycodeSent(String str){
        updateMemory(posFocus,str);
    }

    private void updateMemory(int pos, String str) {
        Log.d(TAG,"アクティビティからイベントが来た");
        //emulator.setMemoryArray(new char[]{'1','2','3','4'},0);

        if(pos>-1) {
            char c = str.charAt(0);
            if (c < 60) {
                c -= 48;
            } else {
                c -= 55;
            }
            emulator.updateMemory(c, pos);
            refreshMemoryPane(pos, 0);
            inputCount++;
            if(inputCount % 4 == 0){
                changeFocus(memoryView.getChildAt(posFocus+1),posFocus+1);
            }

        }

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
}
