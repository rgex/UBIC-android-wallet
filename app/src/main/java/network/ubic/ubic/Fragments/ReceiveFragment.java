package network.ubic.ubic.Fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import network.ubic.ubic.AsyncTasks.OnReceiveFragmentPopulateCompleted;
import network.ubic.ubic.AsyncTasks.ReceiveFragmentPopulate;
import network.ubic.ubic.PrivateKeyStore;
import network.ubic.ubic.R;

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link ReceiveFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReceiveFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReceiveFragment extends Fragment implements OnReceiveFragmentPopulateCompleted, View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private OnFragmentInteractionListener mListener;
    private TextView receiveAddressTextView;
    private ImageView imageView;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300;
    private long lastClickTime = 0;

    public ReceiveFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ReceiveFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReceiveFragment newInstance() {
        ReceiveFragment fragment = new ReceiveFragment();
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
        View view = inflater.inflate(R.layout.fragment_receive, container, false);
        receiveAddressTextView = view.findViewById(R.id.receive_address_textView);
        imageView = view.findViewById(R.id.qrView);

        PrivateKeyStore privateKeyStore = new PrivateKeyStore();
        ReceiveFragmentPopulate receiveFragmentPopulate = new ReceiveFragmentPopulate(
                this,
                privateKeyStore.getPrivateKey(this.getContext())
        );
        receiveFragmentPopulate.execute();
        view.findViewById(R.id.receive_layout).setOnClickListener(this);

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

    public void onReceiveFragmentPopulateCompleted(Bitmap qrCode, String address) {
        System.out.println("onReceiveFragmentPopulateCompleted");
        receiveAddressTextView.setText(address);

        try {
            imageView.setImageBitmap(qrCode);
        } catch (Exception e) {

        }
    }

    @Override
    public void onClick(View v) {
        long clickTime = System.currentTimeMillis();
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {

            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(receiveAddressTextView.getText());
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData
                        .newPlainText("message", receiveAddressTextView.getText());
                clipboard.setPrimaryClip(clip);
            }

            Toast.makeText(getActivity(), "Copied to clipboard",
                    Toast.LENGTH_SHORT).show();
        }
        lastClickTime = clickTime;
    }

}
