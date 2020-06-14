package network.ubic.ubic.Fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.DateUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import network.ubic.ubic.AsyncTasks.GetBalance;
import network.ubic.ubic.AsyncTasks.OnGetBalanceCompleted;
import network.ubic.ubic.BalanceListAdapter;
import network.ubic.ubic.BalanceListItem;
import network.ubic.ubic.Currencies;
import network.ubic.ubic.MainActivity;
import network.ubic.ubic.PrivateKeyStore;
import network.ubic.ubic.R;
import network.ubic.ubic.TransactionListAdapter;
import network.ubic.ubic.TransactionListItem;

/**
 * Activities that contain this fragment must implement the
 * {@link BalanceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BalanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BalanceFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        OnGetBalanceCompleted
{
    private View view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private OnFragmentInteractionListener mListener;
    private List<Integer> currenciesInWallet;

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 20*1000;

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

        this.view.findViewById(R.id.balance_register_passport_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity)getActivity()).goToNavRegisterPassport();
                    }
                }
        );

        view.findViewById(R.id.balance_layout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
        );

        PrivateKeyStore privateKeyStore = new PrivateKeyStore();
        new GetBalance(this, privateKeyStore.getPrivateKey(this.getContext())).execute();
        return view;
    }

    @Override
    public void onResume() {
        final Context context = this.getContext();
        final OnGetBalanceCompleted onGetBalanceCompletedListener = this;

        handler.postDelayed( runnable = new Runnable() {
            public void run() {
                PrivateKeyStore privateKeyStore = new PrivateKeyStore();
                new GetBalance(onGetBalanceCompletedListener, privateKeyStore.getPrivateKey(context)).execute();

                handler.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
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
            Map<Integer, Pair<String, HashMap<Integer, BigInteger>>> transactions,
            Map<Integer, Pair<String, HashMap<Integer, BigInteger>>> pendingTransactions,
            boolean isReceivingUBI,
            List<Pair<String, BigInteger>> ubiExpirationDate,
            boolean isEmptyAddress,
            int nonce
            ) {

        view.findViewById(R.id.loading_balance_layout).setVisibility(View.GONE);

        try {
            final ConnectivityManager connectivityManager = ((ConnectivityManager) this.getContext().getSystemService(Context.CONNECTIVITY_SERVICE));
            if (!(connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected())) {
                view.findViewById(R.id.no_internet_balance_layout).setVisibility(View.VISIBLE);

                return;
            }
        } catch (Exception ignored) {
            // getSystemService failed
        }

        if (balanceMap == null || balanceMap.isEmpty() || isEmptyAddress) {
            view.findViewById(R.id.balance_is_empty_layout).setVisibility(View.VISIBLE);
            view.findViewById(R.id.transaction_layout).setVisibility(View.GONE);

            view.findViewById(R.id.balance_register_passport_button).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.empty_balance_text_view)).setText(getResources().getString(R.string.empty_balance_message));

            if (pendingTransactions.size() > 0) {
                for(HashMap.Entry<Integer, Pair<String, HashMap<Integer, BigInteger>>> pendingTransaction  : pendingTransactions.entrySet()) {
                    if(pendingTransaction.getValue().first.equals("registerPassport")) {
                        ((TextView)view.findViewById(R.id.empty_balance_text_view)).setText(getResources().getString(R.string.empty_balance_passport_pending));
                        view.findViewById(R.id.balance_register_passport_button).setVisibility(View.GONE);
                    }
                }
            }
            if(isReceivingUBI) {
                ((TextView)view.findViewById(R.id.empty_balance_text_view)).setText(getResources().getString(R.string.empty_balance_passport_pending));
                view.findViewById(R.id.balance_register_passport_button).setVisibility(View.GONE);
            }
            swipeRefreshLayout.setRefreshing(false);
            return;
        } else {
            view.findViewById(R.id.balance_is_empty_layout).setVisibility(View.GONE);
            view.findViewById(R.id.transaction_layout).setVisibility(View.VISIBLE);
            view.findViewById(R.id.balance_layout).setVisibility(View.VISIBLE);
        }

        ListView balanceListView = view.findViewById(R.id.balance_list_view);
        ArrayList<BalanceListItem> balanceList = new ArrayList<>();

        Currencies currencies = new Currencies();

        //System.out.println(key + " : " + value);
        for (HashMap.Entry<Integer, BigInteger> entry : balanceMap.entrySet()) {
            if (!currenciesInWallet.contains(entry.getKey())) {
                currenciesInWallet.add(entry.getKey());
            }
            BalanceListItem balanceListItem = new BalanceListItem();
            BigDecimal entryDec = new BigDecimal(entry.getValue().abs());
            balanceListItem.setBalanceAmount((entryDec.divide(BigDecimal.valueOf(1000000), 2, BigDecimal.ROUND_DOWN) + " " + currencies.getCurrency(entry.getKey())));
            balanceListItem.setCurrencyCode(currencies.getCurrency(Integer.valueOf(entry.getKey())));
            balanceList.add(balanceListItem);
        }

        BalanceListAdapter balanceListAdapter = new BalanceListAdapter(
                getActivity(),
                R.layout.balance_list_item,
                balanceList
        );

        balanceListView.setAdapter(balanceListAdapter);
        setListViewHeightBasedOnChildren(balanceListView);

        ((MainActivity) getActivity()).setCurrenciesInWallet(currenciesInWallet);


        // Pending Transactions
        ListView pendingTransactionListView = view.findViewById(R.id.pending_transaction_list_view);
        ArrayList<TransactionListItem> pendingTransactionList = new ArrayList();

        System.out.println("pending transactions.size():" + pendingTransactions.size());

        if(pendingTransactions.size() == 0) {
            view.findViewById(R.id.pendingTransactionTextView).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.pendingTransactionTextView).setVisibility(View.VISIBLE);
        }

        NavigableMap<Integer, Pair<String, HashMap<Integer, BigInteger>>> reveresedPendingTransactions = ((TreeMap)pendingTransactions).descendingMap();

        for (HashMap.Entry<Integer, Pair<String, HashMap<Integer, BigInteger>>> entry : reveresedPendingTransactions.entrySet()) {
            TransactionListItem entryItem = new TransactionListItem();
            for (HashMap.Entry<Integer, BigInteger> entry2 : entry.getValue().second.entrySet()) {
                BigDecimal entryDec = new BigDecimal(entry2.getValue().abs());
                entryItem.setTransactionSign(entry2.getValue().signum());
                entryItem.setTransactionAmount((entryDec.divide(BigDecimal.valueOf(1000000), 2, BigDecimal.ROUND_DOWN) + " " + currencies.getCurrency(entry2.getKey())));
            }
            long now = System.currentTimeMillis();
            entryItem.setTransactionDate(DateUtils.getRelativeTimeSpanString((long)entry.getKey() * 1000, now, DateUtils.DAY_IN_MILLIS).toString());
            entryItem.setTransactionType(entry.getValue().first);
            pendingTransactionList.add(entryItem);
        }

        TransactionListAdapter pendingTransactionListAdapter = new TransactionListAdapter(
                getActivity(),
                R.layout.transaction_list_item,
                pendingTransactionList
        );

        pendingTransactionListView.setAdapter(pendingTransactionListAdapter);
        setListViewHeightBasedOnChildren(pendingTransactionListView);

        // Last Transactions
        ListView lastTransactionListView = view.findViewById(R.id.last_transaction_list_view);
        ArrayList<TransactionListItem> transactionList = new ArrayList();

        System.out.println("transactions.size():" + transactions.size());
        NavigableMap<Integer, Pair<String, HashMap<Integer, BigInteger>>> reveresedTransactions = ((TreeMap)transactions).descendingMap();

        for (HashMap.Entry<Integer, Pair<String, HashMap<Integer, BigInteger>>> entry : reveresedTransactions.entrySet()) {
            TransactionListItem entryItem = new TransactionListItem();
            for (HashMap.Entry<Integer, BigInteger> entry2 : entry.getValue().second.entrySet()) {
                BigDecimal entryDec = new BigDecimal(entry2.getValue().abs());
                entryItem.setTransactionSign(entry2.getValue().signum());
                entryItem.setTransactionAmount((entryDec.divide(BigDecimal.valueOf(1000000), 2, BigDecimal.ROUND_DOWN) + " " + currencies.getCurrency(entry2.getKey())));
            }
            long now = System.currentTimeMillis();
            entryItem.setTransactionDate(DateUtils.getRelativeTimeSpanString((long)entry.getKey() * 1000, now, DateUtils.DAY_IN_MILLIS).toString());
            entryItem.setTransactionType(entry.getValue().first);
            transactionList.add(entryItem);
        }

        TransactionListAdapter transactionListAdapter = new TransactionListAdapter(
                getActivity(),
                R.layout.transaction_list_item,
                transactionList
        );

        lastTransactionListView.setAdapter(transactionListAdapter);
        setListViewHeightBasedOnChildren(lastTransactionListView);

        swipeRefreshLayout.setRefreshing(false);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

}