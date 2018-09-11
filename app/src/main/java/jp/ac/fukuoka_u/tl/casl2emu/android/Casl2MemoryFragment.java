package jp.ac.fukuoka_u.tl.casl2emu.android;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.ArrayList;

import jp.ac.fukuoka_u.tl.casl2emu.Casl2Emulator;
import jp.ac.fukuoka_u.tl.casl2emu.R;
import jp.ac.fukuoka_u.tl.casl2emu.databinding.FragmentCasl2MemoryBinding;

import static android.R.layout.simple_list_item_1;

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


    public Casl2MemoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Casl2MemoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Casl2MemoryFragment newInstance(String param1, String param2) {
        Casl2MemoryFragment fragment = new Casl2MemoryFragment();
        Bundle args = new Bundle();
        /*args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);*/
        fragment.setArguments(args);
        return fragment;
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
        View view = inflater.inflate(R.layout.fragment_casl2_memory, container, false);
        final FragmentCasl2MemoryBinding binding = DataBindingUtil.inflate(inflater,R.layout.fragment_casl2_memory,container,false);
        memoryView = view.findViewById(R.id.memory_grid2);
        memoryView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        emulator = Casl2EmulatorAndroid.getInstance("jp.ac.fukuoka_u.tl.casl2emu.android.Casl2EmulatorAndroid");

        setMemoryAdapter(showMemory(),0);
        return view;
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
            ArrayList<String> stringArrayList = (ArrayList<String>) data;
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
}
