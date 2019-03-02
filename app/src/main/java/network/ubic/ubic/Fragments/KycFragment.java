package network.ubic.ubic.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.ubic.ubic.AsyncTasks.GetBalance;
import network.ubic.ubic.AsyncTasks.GetTransactionFees;
import network.ubic.ubic.AsyncTasks.OnGetBalanceCompleted;
import network.ubic.ubic.AsyncTasks.OnGetTransactionFeesCompleted;
import network.ubic.ubic.AsyncTasks.OnSendTransactionCompleted;
import network.ubic.ubic.AsyncTasks.SendTransaction;
import network.ubic.ubic.ChallengeParser;
import network.ubic.ubic.Currencies;
import network.ubic.ubic.Interfaces.QrCodeCallbackInterface;
import network.ubic.ubic.MainActivity;
import network.ubic.ubic.PrivateKeyStore;
import network.ubic.ubic.R;

/**
 * Activities that contain this fragment must implement the
 * {@link KycFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link KycFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KycFragment extends Fragment implements
        QrCodeCallbackInterface {

    private OnFragmentInteractionListener mListener;
    private View view;

    public KycFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SendFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static KycFragment newInstance(String param1, String param2) {
        KycFragment fragment = new KycFragment();
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
        view = inflater.inflate(R.layout.fragment_kyc, container, false);

        view.findViewById(R.id.scanQrCodeButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity)getActivity()).startQrCodeScan(KycFragment.this);
                    }
                }
        );

        view.findViewById(R.id.verifyKYCButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String challenge = ((TextInputEditText)KycFragment.this.view.findViewById(R.id.challenge_input)).getText().toString();
                        ((MainActivity)getActivity()).goToNavKYCPassport(challenge);
                    }
                }
        );

        view.findViewById(R.id.send_main_layout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        view.findViewById(R.id.challenge_input).clearFocus();
                    }
                }
        );


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
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

    public void qrCodeResult(String qrcodeContent) {
        ChallengeParser challengeParser = new ChallengeParser(qrcodeContent);
        if(challengeParser.validateChallenge()) {
            ((TextView) this.view.findViewById(R.id.challenge_input)).setText(qrcodeContent);
        } else {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.error_qr_code))
                    .setMessage(getResources().getString(R.string.error_unsupported))
                    .setNegativeButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int id) {
                            dialog.cancel();
                        }
                    })
                    .setCancelable(true).create().show();
        }
    }

}
