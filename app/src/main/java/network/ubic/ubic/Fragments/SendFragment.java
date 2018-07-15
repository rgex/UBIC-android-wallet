package network.ubic.ubic.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
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
import network.ubic.ubic.Currencies;
import network.ubic.ubic.Interfaces.QrCodeCallbackInterface;
import network.ubic.ubic.MainActivity;
import network.ubic.ubic.PrivateKeyStore;
import network.ubic.ubic.R;

/**
 * Activities that contain this fragment must implement the
 * {@link SendFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SendFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SendFragment extends Fragment implements
        QrCodeCallbackInterface,
        OnGetTransactionFeesCompleted,
        OnGetBalanceCompleted,
        OnSendTransactionCompleted {

    Currencies currencies;
    private OnFragmentInteractionListener mListener;
    private View view;
    private HashMap<Integer, BigInteger> feesMap = new HashMap<Integer, BigInteger>();
    private int nonce = 0;

    public native String getTransaction(byte[]  seed,
                                        String readableAddress,
                                        int currency,
                                        long amount,
                                        long fee,
                                        int nonce);

    public SendFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SendFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SendFragment newInstance(String param1, String param2) {
        SendFragment fragment = new SendFragment();
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
        view = inflater.inflate(R.layout.fragment_send, container, false);

        view.findViewById(R.id.scanQrCodeButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity)getActivity()).startQrCodeScan(SendFragment.this);
                    }
                }
        );

        currencies = new Currencies();

        view.findViewById(R.id.sendTxButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String selectedItem = ((Spinner)SendFragment.this.view.findViewById(R.id.currency_spinner)).getSelectedItem().toString();

                        if((selectedItem == null) || selectedItem.isEmpty()) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getResources().getString(R.string.error_error))
                                    .setMessage(getResources().getString(R.string.error_no_selected_currency))
                                    .setNegativeButton("Ok",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int id) {
                                                    dialog.cancel();
                                                }
                                            })
                                    .setCancelable(true).create().show();

                            return;
                        }

                        int currencyId = currencies.getCurrency(selectedItem);
                        System.out.println("currencyId:" + currencyId);

                        BigInteger fee = feesMap.get(currencyId);

                        if(fee == null) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getResources().getString(R.string.error_error))
                                    .setMessage(getResources().getString(R.string.error_cannot_get_fees))
                                    .setNegativeButton("Ok",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int id) {
                                                    dialog.cancel();
                                                }
                                            })
                                    .setCancelable(true).create().show();

                            return;
                        }

                        if(((TextView)SendFragment.this.view.findViewById(R.id.send_address)).getText().toString().isEmpty()) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getResources().getString(R.string.error_error))
                                    .setMessage(getResources().getString(R.string.error_empty_address))
                                    .setNegativeButton("Ok",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int id) {
                                                    dialog.cancel();
                                                }
                                            })
                                    .setCancelable(true).create().show();

                            return;
                        }

                        double amount = 0;

                        try {
                            amount = Double.parseDouble(((TextView)SendFragment.this.view.findViewById(R.id.send_amount)).getText().toString());
                        } catch (Exception e) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getResources().getString(R.string.error_error))
                                    .setMessage(getResources().getString(R.string.error_invalid_amount))
                                    .setNegativeButton("Ok",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int id) {
                                                    dialog.cancel();
                                                }
                                            })
                                    .setCancelable(true).create().show();

                            return;
                        }

                        String transaction64 = getTransaction(
                                (new PrivateKeyStore()).getPrivateKey(getContext()),
                                ((TextView)SendFragment.this.view.findViewById(R.id.send_address)).getText().toString(),
                                currencyId,
                                (long)(amount * 1000000),
                                fee.longValue(),
                                nonce
                        );
                        System.out.println("transaction64: " + transaction64);

                        SendFragment.this.view.findViewById(R.id.sendTransactionProgressBar).setVisibility(View.VISIBLE);
                        (new SendTransaction(SendFragment.this, transaction64)).execute();
                    }
                }
        );

        view.findViewById(R.id.send_main_layout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        view.findViewById(R.id.send_address).clearFocus();
                        view.findViewById(R.id.send_amount).clearFocus();
                    }
                }
        );

        view.findViewById(R.id.send_layout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                        view.findViewById(R.id.send_address).clearFocus();
                        view.findViewById(R.id.send_amount).clearFocus();
                    }
                }
        );

        List<Integer> currenciesInWallet = ((MainActivity)getActivity()).getCurrenciesInWallet();

        if(currenciesInWallet.isEmpty()) {
            view.findViewById(R.id.no_funds_error).setVisibility(View.VISIBLE);
        } else {

            List<String> spinnerArray =  new ArrayList<String>();
            for (int currencyID : currenciesInWallet) {
                spinnerArray.add(currencies.getCurrency(currencyID));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    getActivity(), android.R.layout.simple_spinner_item, spinnerArray
            );

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner sItems = (Spinner) view.findViewById(R.id.currency_spinner);
            sItems.setAdapter(adapter);
        }

        PrivateKeyStore privateKeyStore = new PrivateKeyStore();
        new GetBalance(this, privateKeyStore.getPrivateKey(this.getContext())).execute();
        (new GetTransactionFees(this)).execute();

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
        if(qrcodeContent.substring(0, 1).equals("\01")) {
            ((TextView) this.view.findViewById(R.id.send_address)).setText(qrcodeContent.substring(1, qrcodeContent.length()));
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

    public void OnGetBalanceCompleted(
            HashMap<Integer, BigInteger> balanceMap,
            HashMap<String, HashMap<Integer, BigInteger>> transactions,
            boolean isReceivingUBI,
            boolean isEmptyAddress,
            int nonce
    ) {
        this.nonce = nonce;
    }

    public void OnGetTransactionFeesCompleted(HashMap<Integer, BigInteger> feesMap) {

        System.out.println("OnGetTransactionFeesCompleted");

        for(Map.Entry<Integer, BigInteger> fee : feesMap.entrySet()) {
            System.out.println("key:" + fee.getKey());
            System.out.println("value:" + fee.getValue());
        }

        this.feesMap = feesMap;
    }

    public void onSendTransactionCompleted(int responseCode, String response) {
        System.out.println("onSendTransactionCompleted code: " + responseCode + " reponse: " + response);

        try {
            JSONObject jsonObj = new JSONObject(response);
            boolean success = jsonObj.getBoolean("success");

            if(success) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.success))
                        .setMessage(getResources().getString(R.string.transaction_was_send))
                        .setNegativeButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                })
                        .setCancelable(true).create().show();
            } else {
                String errorMessage;

                if(jsonObj.getString("error").isEmpty()) {
                    errorMessage = jsonObj.getString("error").toString();
                } else {
                    errorMessage = getResources().getString(R.string.error_unknown_error);
                }

                new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.error_error))
                        .setMessage(errorMessage)
                        .setNegativeButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                })
                        .setCancelable(true).create().show();
            }
        } catch (Exception e) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.error_error))
                    .setMessage(getResources().getString(R.string.error_invalid_server_response))
                    .setNegativeButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.cancel();
                                }
                            })
                    .setCancelable(true).create().show();
        }

        view.findViewById(R.id.sendTransactionProgressBar).setVisibility(View.GONE);
    }
}
