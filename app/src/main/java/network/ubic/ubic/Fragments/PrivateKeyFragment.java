package network.ubic.ubic.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import network.ubic.ubic.AsyncTasks.OnPrivateKeyFragmentPopulateCompleted;
import network.ubic.ubic.AsyncTasks.PrivateKeyPopulate;
import network.ubic.ubic.AsyncTasks.ReceiveFragmentPopulate;
import network.ubic.ubic.PrivateKeyStore;
import network.ubic.ubic.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PrivateKeyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PrivateKeyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PrivateKeyFragment extends Fragment implements OnPrivateKeyFragmentPopulateCompleted, View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300;
    private long lastClickTime = 0;
    private TextView privateKeyTextView;

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
        View view = inflater.inflate(R.layout.fragment_private_key, container, false);
        privateKeyTextView = view.findViewById(R.id.private_key_textView);

        PrivateKeyStore privateKeyStore = new PrivateKeyStore();
        PrivateKeyPopulate privateKeyPopulate = new PrivateKeyPopulate(
                this,
                privateKeyStore.getPrivateKey(this.getContext())
        );
        privateKeyPopulate.execute();
        view.findViewById(R.id.private_key_layout).setOnClickListener(this);

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

    public void onPrivateKeyFragmentPopulateCompleted(String privateKey) {
        this.privateKeyTextView.setText(privateKey);
    }

    @Override
    public void onClick(View v) {
        long clickTime = System.currentTimeMillis();
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {

            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(this.privateKeyTextView.getText());
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData
                        .newPlainText("message", this.privateKeyTextView.getText());
                clipboard.setPrimaryClip(clip);
            }

            Toast.makeText(getActivity(), "Copied to clipboard",
                    Toast.LENGTH_SHORT).show();
        }
        lastClickTime = clickTime;
    }
}
