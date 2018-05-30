package network.ubic.ubic.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import network.ubic.ubic.BitiAndroid.TagProvider;
import network.ubic.ubic.MainActivity;
import network.ubic.ubic.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WaitForNfcFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WaitForNfcFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WaitForNfcFragment extends Fragment {
    private static final String ARG_PARAM_PASSPORT_NUMBER = "param1";
    private static final String ARG_PARAM_DATE_OF_BIRTH = "param2";
    private static final String ARG_PARAM_DATE_OF_EXPIRATION = "param2";

    private String passportNumber;
    private String dateOfBirth;
    private String dateOfExpiration;

    private OnFragmentInteractionListener mListener;

    public WaitForNfcFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WaitForNfcFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WaitForNfcFragment newInstance(String passportNumber, String dateOfBirth, String dateOfExpiration) {
        WaitForNfcFragment fragment = new WaitForNfcFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_PASSPORT_NUMBER, passportNumber);
        args.putString(ARG_PARAM_DATE_OF_BIRTH, dateOfBirth);
        args.putString(ARG_PARAM_DATE_OF_EXPIRATION, dateOfExpiration);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            passportNumber = getArguments().getString(ARG_PARAM_PASSPORT_NUMBER);
            dateOfBirth = getArguments().getString(ARG_PARAM_DATE_OF_BIRTH);
            dateOfExpiration = getArguments().getString(ARG_PARAM_DATE_OF_EXPIRATION);
        }

        if(TagProvider.isTagReady()) {
            this.readPassport();
        }
    }

    public void readPassport()
    {
        ((MainActivity)getActivity()).goToNavReadingPassport(passportNumber, dateOfBirth, dateOfExpiration);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wait_for_nfc, container, false);
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
    }
}
