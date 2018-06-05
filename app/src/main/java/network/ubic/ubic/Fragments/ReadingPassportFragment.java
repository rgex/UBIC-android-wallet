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

    private byte[] dg1;
    private byte[] dg2;
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

    public void showResult() {
        if (this.dg1 != null && this.dg2 != null) {
            Intent intent = new Intent("bondi.nfcPassportReader.jan.mrtd2.ResultDisplayActivity");
            intent.putExtra("dg1", this.dg1);
            intent.putExtra("dg2", this.dg2);
            intent.putExtra("sod", this.sod);
            this.setMrtdProgressBarPercentage(96);
            startActivity(intent);
            this.setMrtdProgressBarPercentage(100);
        } else {
            System.out.println("dg1 or/and dg2 is/are null");
        }
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

    public void setDg1(byte[] dg1) {
        this.dg1 = dg1;
    }

    public void setDg2(byte[] dg2) {
        this.dg2 = dg2;
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
        private int currentStep = 0;

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

                        this.currentStep = 1;
                        byte[] dg1 = mrtd.readFile(MrtdConstants.FID_DG1);
                        if (dg1 == null) {
                            this.readingPassportActivity.get().showError(getResources().getString(R.string.error_dg1_is_null));
                        }

                        this.readingPassportActivity.get().setMrtdProgressBarPercentage(10);

                        this.readingPassportActivity.get().setDg1(dg1);
                        DG1Parser dg1Parser = new DG1Parser(dg1);
                        Tools tools = new Tools();

                        /*
                        System.out.println("Document Code : ".concat(dg1Parser.getDocumentCode()));
                        System.out.println("Issuing state : ".concat(dg1Parser.getIssuingStateCode()));
                        System.out.println("Document Number : ".concat(dg1Parser.getDocumentNumber()));
                        System.out.println("Gender : ".concat(dg1Parser.getGender()));
                        System.out.println("Given names : ".concat(dg1Parser.getGivenNames()));
                        System.out.println("Surname : ".concat(dg1Parser.getSurname()));
                        System.out.println("Nationality : ".concat(dg1Parser.getNationalityCode()));
                        System.out.println("Date of birth : ".concat(dg1Parser.getDateOfBirth()));
                        System.out.println("Date of Expiry : ".concat(dg1Parser.getDateOfExpiry()));
                        System.out.println("File content : ".concat(tools.bytesToString(dg1)));*/

                        this.currentStep = 2;
                        byte[] dg2 = mrtd.readFile(MrtdConstants.FID_DG2);
                        if (dg2 == null) {
                            this.readingPassportActivity.get().showError(getResources().getString(R.string.error_dg2_is_null));
                        }
                        this.readingPassportActivity.get().setDg2(dg2);

                        this.currentStep = 3;
                        byte[] efsod = mrtd.readFile(MrtdConstants.FID_EF_SOD);
                        this.readingPassportActivity.get().setSOD(efsod);

                        if (dg1 != null && dg2 != null) {
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
                this.readingPassportActivity.get().showResult();
                //@TODO
            }
        }

        public void updateProgress(int progress) {
            switch (currentStep) {
                case 1:
                    this.readingPassportActivity.get().setMrtdProgressBarPercentage(Math.round(progress * 10 / 100));
                    break;
                case 2:
                    this.readingPassportActivity.get().setMrtdProgressBarPercentage(Math.round(progress * 75 / 100) + 10);
                    break;
                case 3:
                    this.readingPassportActivity.get().setMrtdProgressBarPercentage(Math.round(progress * 10 / 100) + 85);
                    break;
            }
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
