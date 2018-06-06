package network.ubic.ubic.Fragments;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.lang.ref.WeakReference;

import network.ubic.ubic.BitiAndroid.AbstractNfcActivity;
import network.ubic.ubic.BitiAndroid.TagProvider;
import network.ubic.ubic.BitiMRTD.Constants.MrtdConstants;
import network.ubic.ubic.BitiMRTD.Parser.DG1Parser;
import network.ubic.ubic.BitiMRTD.Parser.TagParser;
import network.ubic.ubic.BitiMRTD.Reader.AbstractReader;
import network.ubic.ubic.BitiMRTD.Reader.BacInfo;
import network.ubic.ubic.BitiMRTD.Reader.DESedeReader;
import network.ubic.ubic.BitiMRTD.Reader.ProgressListenerInterface;
import network.ubic.ubic.BitiMRTD.Tools.Tools;
import network.ubic.ubic.PassportStore;
import network.ubic.ubic.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReadingPassportFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReadingPassportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReadingPassportFragment extends Fragment {

    private String passportNumber;
    private String dateOfBirth;
    private String dateOfExpiration;
    private boolean isActivityRunning;

    private OnFragmentInteractionListener mListener;

    private byte[] sod;

    private AsyncReader asyncReader;

    private ProgressBar mrtdProgressBar;

    protected NfcAdapter mNfcAdapter;
    protected PendingIntent pendingIntent;

    public ReadingPassportFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ReadingPassportFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReadingPassportFragment newInstance() {
        ReadingPassportFragment fragment = new ReadingPassportFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.isActivityRunning = true;

        mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

        if (mNfcAdapter == null || !mNfcAdapter.isEnabled()) {
            System.out.println("failed to get NFC adapter, NFC disabled?");

            new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.error_error))
                    .setMessage(getResources().getString(R.string.error_nfc_is_disabled))
                    .setCancelable(true)
                    .setPositiveButton("enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                            startActivity(intent);
                        }
                    }).create().show();
        }
        else {
            pendingIntent = PendingIntent.getActivity(
                    getActivity(), 0, new Intent(getActivity(), getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        }
    }

    protected void readNfc() {
        this.asyncReader = new AsyncReader(
                this,
                this.passportNumber,
                this.dateOfBirth,
                this.dateOfExpiration
        );
        asyncReader.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reading_passport, container, false);

        PassportStore passportStore = new PassportStore(getContext());

        this.passportNumber = passportStore.getPassportNumber();
        this.dateOfBirth = passportStore.getDateOfBirth();
        this.dateOfExpiration = passportStore.getDateOfExpiry();

        this.mrtdProgressBar = view.findViewById(R.id.mrtdProgressBar);

        System.out.println("this.passportNumber: " + this.passportNumber);
        System.out.println("this.dateOfBirth: " + this.dateOfBirth);
        System.out.println("this.dateOfExpiration: " + this.dateOfExpiration);

        this.readNfc();

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

    public native String getPassportTransaction(byte[]  seed);

    public void createRegisterPassportTransaction() {
        this.setMrtdProgressBarPercentage(100);


        TagParser tagParser = new TagParser(this.sod);
        byte[] tag77 = tagParser.geTag("77").getBytes();

        String passportTx64 = getPassportTransaction(tag77);
        System.out.println("passportTx64: " + passportTx64);
    }

    public void showError(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(ReadingPassportFragment.this.isActivityRunning) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.error_error))
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //@TODO
                                }
                            }).create().show();
                }
            }
        });

    }


    public void setMrtdProgressBarPercentage(int progress) {
        this.mrtdProgressBar.setProgress(progress);
    }

    public void setSOD(byte[] sod) {
        this.sod = sod;
    }

    private class AsyncReader extends AsyncTask<Void, Integer, Boolean> implements ProgressListenerInterface {

        private boolean success = false;
        private WeakReference<ReadingPassportFragment> readingPassportActivity;
        private String passportNumber;
        private String dateOfBirth;
        private String dateOfExpiration;
        private boolean isCanceled = false;

        public AsyncReader(
                ReadingPassportFragment readingPassportActivity,
                String passportNumber,
                String dateOfBirth,
                String dateOfExpiration
        ) {

            this.passportNumber = passportNumber;
            this.dateOfBirth = dateOfBirth;
            this.dateOfExpiration = dateOfExpiration;
            this.isCanceled = false;

            this.link(readingPassportActivity);
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {


            try {
                if (TagProvider.getTag() != null) {

                    System.out.println("GOT TAG");

                    BacInfo bacInfo = new BacInfo();

                    bacInfo.setPassportNbr(this.passportNumber);
                    bacInfo.setDateOfBirth(this.dateOfBirth);
                    bacInfo.setDateOfExpiry(this.dateOfExpiration);

                    AbstractReader mrtd = new DESedeReader();
                    mrtd.setBacInfo(bacInfo);


                    if (mrtd.initSession()) {

                        mrtd.setProgressListener(new WeakReference<Object>(this));

                        this.readingPassportActivity.get().setMrtdProgressBarPercentage(5);

                        byte[] efsod = mrtd.readFile(MrtdConstants.FID_EF_SOD);
                        this.readingPassportActivity.get().setSOD(efsod);

                        if (sod != null) {
                            this.success = true;
                        }

                    } else {
                        System.out.println("Failed to init session");
                        this.readingPassportActivity.get().showError(getResources().getString(R.string.error_mutual_authentication_failed));
                        TagProvider.closeTag();
                        return false;
                    }
                } else {
                    System.out.println("Couldn't get Tag from intent");
                    this.readingPassportActivity.get().showError(getResources().getString(R.string.error_lost_connexion));
                    return false;
                }
            } catch (Exception e) {
                System.out.println("Exception");
                this.readingPassportActivity.get().showError(getResources().getString(R.string.error_nfc_exception));
                return false;
            }

            if(!this.success) {
                this.readingPassportActivity.get().showError(getResources().getString(R.string.error_nfc_exception));
            }

            this.readingPassportActivity.get().setMrtdProgressBarPercentage(95);

            return true;
        }

        public void link(ReadingPassportFragment readingPassportActivity) {
            this.readingPassportActivity = new WeakReference<ReadingPassportFragment>(readingPassportActivity);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (this.success) {
                this.readingPassportActivity.get().createRegisterPassportTransaction();
                //@TODO
            }
        }

        public void updateProgress(int progress) {
            this.readingPassportActivity.get().setMrtdProgressBarPercentage(progress);
        }

        public void cancel() {
            this.isCanceled = true;
        }

        @Override
        protected void onCancelled() {
            this.isCanceled = true;
            super.onCancelled();
        }

        @Override
        public boolean isCanceled() {
            return this.isCanceled;
        }
    }
}
