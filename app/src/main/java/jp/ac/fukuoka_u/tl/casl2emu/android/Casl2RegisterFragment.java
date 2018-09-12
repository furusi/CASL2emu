package jp.ac.fukuoka_u.tl.casl2emu.android;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.ac.fukuoka_u.tl.casl2emu.Casl2Emulator;
import jp.ac.fukuoka_u.tl.casl2emu.R;
import jp.ac.fukuoka_u.tl.casl2emu.databinding.FragmentCasl2RegisterBinding;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Casl2RegisterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Casl2RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Casl2RegisterFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Casl2Emulator emulator;
    private View registerView;
    private FragmentCasl2RegisterBinding binding;



    public Casl2RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Casl2RegisterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Casl2RegisterFragment newInstance(String param1, String param2) {
        return new Casl2RegisterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        emulator = Casl2EmulatorAndroid.getInstance("jp.ac.fukuoka_u.tl.casl2emu.android.Casl2EmulatorAndroid");
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_casl2_register,container,false);
//        View view = inflater.inflate(R.layout.fragment_casl2_register,container,false);

        // Inflate the layout for this fragment
        return binding.getRoot();
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
        void onRegisterFragmentInteraction(String str);
    }


    public void onAreaTouchListener(int i) {

    }
}
