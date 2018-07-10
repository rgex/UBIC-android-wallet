package network.ubic.ubic.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import network.ubic.ubic.AsyncTasks.GetBalance;
import network.ubic.ubic.AsyncTasks.OnGetBalanceCompleted;
import network.ubic.ubic.Currencies;
import network.ubic.ubic.MainActivity;
import network.ubic.ubic.PrivateKeyStore;
import network.ubic.ubic.R;

/**
 * Activities that contain this fragment must implement the
 * {@link BalanceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BalanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BalanceFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener, OnGetBalanceCompleted
{
    private View view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private OnFragmentInteractionListener mListener;
    private List<Integer> currenciesInWallet;

    public BalanceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BalanceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BalanceFragment newInstance() {
        BalanceFragment fragment = new BalanceFragment();
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
        view = inflater.inflate(R.layout.fragment_balance, container, false);

        currenciesInWallet = new ArrayList<Integer>();

        swipeRefreshLayout =((SwipeRefreshLayout)view.findViewById(R.id.balance_SwipeRefreshLayout));
        swipeRefreshLayout.setOnRefreshListener(this);

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

    @Override
    public void onRefresh() {
        System.out.println("onRefresh called");

        PrivateKeyStore privateKeyStore = new PrivateKeyStore();
        new GetBalance(this, privateKeyStore.getPrivateKey(this.getContext())).execute();
    }

    public void OnGetBalanceCompleted(
            HashMap<Integer, BigInteger> balanceMap,
            HashMap<String, HashMap<Integer, BigInteger>> transactions,
            boolean isReceivingUBI,
            boolean isEmptyAddress
            ) {

        if(balanceMap == null || balanceMap.isEmpty() || isEmptyAddress) {
            view.findViewById(R.id.emptyBalanceText).setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
            return;
        } else {
            view.findViewById(R.id.emptyBalanceText).setVisibility(View.GONE);
        }

        ListView balanceListView = view.findViewById(R.id.balance_list_view);
        List<String> balanceList = new ArrayList<String>();

        Currencies currencies = new Currencies();

        //System.out.println(key + " : " + value);
        for (HashMap.Entry<Integer, BigInteger> entry : balanceMap.entrySet())
        {
            if(!currenciesInWallet.contains(entry.getKey())) {
                currenciesInWallet.add(entry.getKey());
            }
            balanceList.add(currencies.getCurrency(Integer.valueOf(entry.getKey())) + " : " + (entry.getValue().divide(BigInteger.valueOf(1000000))));
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                balanceList
        );

        balanceListView.setAdapter(arrayAdapter);

        ((MainActivity)getActivity()).setCurrenciesInWallet(currenciesInWallet);

        ListView lastTransactionListView = view.findViewById(R.id.last_transaction_list_view);
        List<String> transactionList = new ArrayList<String>();

        System.out.println("transactions.size():" + transactions.size());
        for (HashMap.Entry<String, HashMap<Integer, BigInteger>> entry : transactions.entrySet())
        {
            String entryStr;
            entryStr = entry.getKey().substring(0,8) + " : ";
            for (HashMap.Entry<Integer, BigInteger> entry2 : entry.getValue().entrySet())
            {
                entryStr += currencies.getCurrency(entry2.getKey()) + " : " +  (entry2.getValue().divide(BigInteger.valueOf(1000000))) + "\n";
            }
            transactionList.add(entryStr);
        }

        arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                transactionList
        );

        lastTransactionListView.setAdapter(arrayAdapter);

        swipeRefreshLayout.setRefreshing(false);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}