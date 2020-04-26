package network.ubic.ubic.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NavigableMap;
import java.util.TreeMap;

import network.ubic.ubic.AsyncTasks.OnPrivateKeyFragmentPopulateCompleted;
import network.ubic.ubic.AsyncTasks.PrivateKeyPopulate;
import network.ubic.ubic.AsyncTasks.ReceiveFragmentPopulate;
import network.ubic.ubic.MainActivity;
import network.ubic.ubic.PrivateKeyListAdapter;
import network.ubic.ubic.PrivateKeyListItem;
import network.ubic.ubic.PrivateKeyStore;
import network.ubic.ubic.R;
import network.ubic.ubic.TransactionListAdapter;
import network.ubic.ubic.TransactionListItem;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PrivateKeyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PrivateKeyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PrivateKeyFragment extends Fragment implements OnPrivateKeyFragmentPopulateCompleted {

    private OnFragmentInteractionListener mListener;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300;
    private long lastClickTime = 0;
    private TextView privateKeyTextView1;
    private TextView addressTextView;
    private String privateKey;
    private View view;

    public PrivateKeyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PrivateKeyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PrivateKeyFragment newInstance(String param1, String param2) {
        PrivateKeyFragment fragment = new PrivateKeyFragment();
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
        this.view = inflater.inflate(R.layout.fragment_private_key, container, false);
        privateKeyTextView1 = view.findViewById(R.id.private_key_preview_1);
        addressTextView = view.findViewById(R.id.address_preview);

        this.view.findViewById(R.id.copyCurrentPrivateKeyToClipboard).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                            int sdk = android.os.Build.VERSION.SDK_INT;
                            if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext()
                                        .getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboard.setText(PrivateKeyFragment.this.privateKey);
                            } else {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext()
                                        .getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData
                                        .newPlainText(getResources().getString(R.string.message), PrivateKeyFragment.this.privateKey);
                                clipboard.setPrimaryClip(clip);
                            }

                            Toast.makeText(PrivateKeyFragment.this.getActivity(), PrivateKeyFragment.this.getResources().getString(R.string.copied_to_clipboard),
                                    Toast.LENGTH_SHORT).show();
                    }
                }
        );

        this.view.findViewById(R.id.addNewPrivateKeyButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity)getActivity()).goToNavNewPrivateKey();
                    }
                }
        );

        PrivateKeyStore privateKeyStore = new PrivateKeyStore();
        byte[] privateKey = privateKeyStore.getPrivateKey(this.getContext());
        PrivateKeyPopulate privateKeyPopulate = new PrivateKeyPopulate(
                this,
                privateKey,
                getAddress(privateKey)
        );
        privateKeyPopulate.execute();

        try {
            // List other private keys
            ArrayList<PrivateKeyListItem> privateKeyListItems = new ArrayList();

            JSONObject wallet = privateKeyStore.getWallet(getContext());
            JSONArray allList = wallet.getJSONArray("all");

            for (int i = 0; i < allList.length(); i++) {
                String privKey = allList.getString(i);
                PrivateKeyListItem entryItem = new PrivateKeyListItem();
                entryItem.setPrivateKey(privKey);
                entryItem.setAddress(getAddress(Hex.decode(privKey)));
                privateKeyListItems.add(entryItem);
            }

            if(allList.length() == 0) {
                view.findViewById(R.id.other_private_keys_textview).setVisibility(View.GONE);
            }

            PrivateKeyListAdapter privateKeyListAdapter = new PrivateKeyListAdapter(
                    getActivity(),
                    R.layout.private_key_list_item,
                    privateKeyListItems,
                    ((MainActivity)getActivity())
            );

            ListView privateKeyListView = view.findViewById(R.id.private_key_list_view);
            privateKeyListView.setAdapter(privateKeyListAdapter);
            setListViewHeightBasedOnChildren(privateKeyListView);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
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

    public void onPrivateKeyFragmentPopulateCompleted(String privateKey, String address) {
        System.out.println("Currently in use address: " + address);
        this.privateKeyTextView1.setText(privateKey);
        this.addressTextView.setText(address);
        this.privateKey = privateKey;
    }

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
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        System.out.println("new height: " + params.height);
        listView.setLayoutParams(params);
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public native String getAddress(byte[]  seed);
}
