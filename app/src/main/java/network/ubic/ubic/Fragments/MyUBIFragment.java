package network.ubic.ubic.Fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import network.ubic.ubic.AsyncTasks.GetBalance;
import network.ubic.ubic.AsyncTasks.OnGetBalanceCompleted;
import network.ubic.ubic.MainActivity;
import network.ubic.ubic.PrivateKeyStore;
import network.ubic.ubic.R;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link MyUBIFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyUBIFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyUBIFragment extends Fragment implements OnGetBalanceCompleted {

    private OnFragmentInteractionListener mListener;
    private View view;

    public MyUBIFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MyUBIFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyUBIFragment newInstance(String param1, String param2) {
        MyUBIFragment fragment = new MyUBIFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.fragment_my_ubi, container, false);

        this.view.findViewById(R.id.registerPassportButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity)getActivity()).goToNavRegisterPassport();
                    }
                }
        );

        this.view.findViewById(R.id.registerAnotherPassportButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity)getActivity()).goToNavRegisterPassport();
                    }
                }
        );

        PrivateKeyStore privateKeyStore = new PrivateKeyStore();
        new GetBalance(this, privateKeyStore.getPrivateKey(this.getContext())).execute();

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

    public void OnGetBalanceCompleted(
            HashMap<Integer, BigInteger> balanceMap,

            Map<Integer, Pair<String, HashMap<Integer, BigInteger>>> transactions,
            Map<Integer, Pair<String, HashMap<Integer, BigInteger>>> pendingTransactions,
            boolean isReceivingUBI,
            List<Pair<String, BigInteger>> ubiExpirationDate,
            boolean isEmptyAddress,
            int nonce
    ) {

        this.view.findViewById(R.id.my_ubi_progress_bar).setVisibility(View.GONE);

        final ConnectivityManager connectivityManager = ((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE));
        if(!(connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected())) {
            this.view.findViewById(R.id.no_internet_my_ubi_layout).setVisibility(View.VISIBLE);
        } else {
            if (isReceivingUBI) {
                this.view.findViewById(R.id.is_receiving_ubi_layout).setVisibility(View.VISIBLE);
            } else {
                this.view.findViewById(R.id.no_ubi_layout).setVisibility(View.VISIBLE);
            }
        }
    }

}
