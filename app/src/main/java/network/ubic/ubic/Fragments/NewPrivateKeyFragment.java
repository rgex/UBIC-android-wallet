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

import org.spongycastle.util.encoders.Hex;

import network.ubic.ubic.MainActivity;
import network.ubic.ubic.PrivateKeyStore;
import network.ubic.ubic.R;

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link NewPrivateKeyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewPrivateKeyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewPrivateKeyFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private View view;

    public NewPrivateKeyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MyUBIFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewPrivateKeyFragment newInstance(String param1, String param2) {
        NewPrivateKeyFragment fragment = new NewPrivateKeyFragment();
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
        this.view = inflater.inflate(R.layout.fragment_new_private_key, container, false);

        view.findViewById(R.id.private_key_layout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        view.findViewById(R.id.private_key_input).clearFocus();
                    }
                }
        );
        this.view.findViewById(R.id.importPrivateKeyButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        long availableMb = NewPrivateKeyFragment.this.getContext().getExternalFilesDir(null).getUsableSpace() / (1024 * 1024);

                        if(availableMb < 50) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getResources().getString(R.string.error))
                                    .setMessage(getResources().getString(R.string.not_enough_space_for_import))
                                    .setNegativeButton(getResources().getString(R.string.ok),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int id) {
                                                    dialog.cancel();
                                                }
                                            })
                                    .setCancelable(true).create().show();
                            return;
                        }

                        String privateKeyToImport = "";
                        try {
                            privateKeyToImport = ((TextInputEditText) NewPrivateKeyFragment.this.view.findViewById(R.id.private_key_input)).getText().toString().replace(" ", "");
                        } catch (Exception e) {
                            System.out.println("privateKeyToImport from input failed");
                            e.printStackTrace();
                        }

                        PrivateKeyStore privateKeyStore = new PrivateKeyStore();

                        byte[] privKeyBytes = {0x00};
                        try {
                            privKeyBytes = Hex.decode(privateKeyToImport);
                        } catch (Exception e) {
                            System.out.println("privateKeyToImport decode failed");
                            e.printStackTrace();
                        }

                        System.out.println("privKeyBytes.length: " + privKeyBytes.length);

                        if(privKeyBytes.length != 20) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getResources().getString(R.string.error))
                                    .setMessage(getResources().getString(R.string.cannot_import_invalid_privatekey))
                                    .setNegativeButton(getResources().getString(R.string.ok),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int id) {
                                                    dialog.cancel();
                                                }
                                            })
                                    .setCancelable(true).create().show();
                            return;
                        }

                        if(privateKeyStore.addPrivateKey(getActivity().getBaseContext(), privKeyBytes)) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getResources().getString(R.string.success))
                                    .setMessage(getResources().getString(R.string.private_key_was_imported))
                                    .setNegativeButton(getResources().getString(R.string.ok),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int id) {
                                                    dialog.cancel();
                                                }
                                            })
                                    .setCancelable(true).create().show();
                            try {
                                ((MainActivity) NewPrivateKeyFragment.this.getActivity()).goToNavPrivateKey();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            return;
                        } else {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getResources().getString(R.string.error))
                                    .setMessage(getResources().getString(R.string.cannot_import_privatekey_unknown_error))
                                    .setNegativeButton(getResources().getString(R.string.ok),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int id) {
                                                    dialog.cancel();
                                                }
                                            })
                                    .setCancelable(true).create().show();
                            return;
                        }
                    }
                }
        );

        this.view.findViewById(R.id.generateNewPrivateKeyButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println("Clicked generate new private key");
                        PrivateKeyStore privateKeyStore = new PrivateKeyStore();
                        try {
                            byte[] generatedKey = privateKeyStore.generateNewKey();
                            String stringKey = new String(Hex.encode(generatedKey), "UTF-8");
                            ((TextInputEditText) NewPrivateKeyFragment.this.view.findViewById(R.id.private_key_input)).setText(stringKey);
                        } catch (Exception e) {
                            System.out.println("failed to generate new private key for import");
                            e.printStackTrace();
                        }
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

}
