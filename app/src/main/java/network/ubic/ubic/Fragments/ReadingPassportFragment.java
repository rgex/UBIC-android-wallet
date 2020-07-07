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
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;

import net.sf.scuba.smartcards.CardFileInputStream;
import net.sf.scuba.smartcards.CardService;

import org.jmrtd.BACKey;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.CardAccessFile;
import org.jmrtd.lds.CardSecurityFile;
import org.jmrtd.lds.PACEInfo;
import org.jmrtd.lds.SODFile;
import org.jmrtd.lds.SecurityInfo;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Collection;

import network.ubic.ubic.AsyncTasks.OnSendTransactionCompleted;
import network.ubic.ubic.AsyncTasks.SendTransaction;
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
import network.ubic.ubic.ChallengeParser;
import network.ubic.ubic.MainActivity;
import network.ubic.ubic.PassportStore;
import network.ubic.ubic.PrivateKeyStore;
import network.ubic.ubic.R;
import static org.jmrtd.PassportService.DEFAULT_MAX_BLOCKSIZE;
import static org.jmrtd.PassportService.NORMAL_MAX_TRANCEIVE_LENGTH;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReadingPassportFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReadingPassportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReadingPassportFragment extends Fragment implements OnSendTransactionCompleted {

    private String passportNumber;
    private String dateOfBirth;
    private String dateOfExpiration;
    private String challenge;
    private boolean isActivityRunning;

    private OnFragmentInteractionListener mListener;

    View view;
    private byte[] sod;
    private byte[] dg1;
    private byte[] dg2;
    public static FragmentActivity activity;

    private AsyncReader asyncReader;

    private ProgressBar mrtdProgressBar;
    private TextView percentageDisplay;

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
        System.out.println("oncreate ReadingPassportFragment");
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
                this.dateOfExpiration,
                this.challenge
        );
        asyncReader.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        System.out.println("onCreateView ReadingPassportFragment");

        this.view = inflater.inflate(R.layout.fragment_reading_passport, container, false);

        this.view.findViewById(R.id.ReadingPassportLayout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
        );

        PassportStore passportStore = new PassportStore(getContext());

        this.passportNumber = passportStore.getPassportNumber();
        this.dateOfBirth = passportStore.getDateOfBirth();
        this.dateOfExpiration = passportStore.getDateOfExpiry();
        this.challenge = passportStore.getChallenge();

        this.mrtdProgressBar = this.view.findViewById(R.id.mrtdProgressBar);

        System.out.println("this.passportNumber: " + this.passportNumber);
        System.out.println("this.dateOfBirth: " + this.dateOfBirth);
        System.out.println("this.dateOfExpiration: " + this.dateOfExpiration);
        System.out.println("this.challenge: " + this.challenge);

        this.readNfc();

        return this.view;
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

    public native String getPassportTransaction(byte[]  seed, byte[] sod);

    public native String getKycTransaction(
            byte[] jSeed,
            byte[] jSod,
            byte[] jDg1,
            byte[] jDg2,
            int jMode,
            String jChallenge
    );

    public void createAndSendKYCTransaction() {
        //this.setMrtdProgressBarPercentage(100);
        ChallengeParser challengeParser = new ChallengeParser(this.challenge);

        System.out.println("this.sod: " + this.sod);
        System.out.println("this.dg1: " + this.dg1);
        System.out.println("this.dg2: " + this.dg2);
        String kycTx64 = getKycTransaction(
                (new PrivateKeyStore()).getPrivateKey(getContext()),
                this.sod,
                this.dg1,
                this.dg2,
                challengeParser.getAuthenticationMode(),
                this.challenge
        );

        System.out.println("kycTx64: " + kycTx64);

        this.view.findViewById(R.id.mrtdProgressBar).setVisibility(View.GONE);
        this.view.findViewById(R.id.uploadPassportTransactionProgressBar).setVisibility(View.VISIBLE);
        ((TextView)this.view.findViewById(R.id.readingPassportTextView)).setText(R.string.uploading_kyc_transaction);

        SendTransaction sendTransaction = new SendTransaction(ReadingPassportFragment.this, kycTx64);
        sendTransaction.setOverwriteUrl(challengeParser.getUrl());
        sendTransaction.execute();
    }

    public void createAndSendRegisterPassportTransaction() {
        //this.setMrtdProgressBarPercentage(100);


        TagParser tagParser = new TagParser(this.sod);
        byte[] tag77 = tagParser.geTag("77").getBytes();

        String passportTx64 = getPassportTransaction(
                (new PrivateKeyStore()).getPrivateKey(getContext()),
                tag77
        );
        System.out.println("passportTx64: " + passportTx64);

        this.view.findViewById(R.id.mrtdProgressBar).setVisibility(View.GONE);
        this.view.findViewById(R.id.uploadPassportTransactionProgressBar).setVisibility(View.VISIBLE);
        ((TextView)this.view.findViewById(R.id.readingPassportTextView)).setText(R.string.uploading_passport_transaction);

        (new SendTransaction(ReadingPassportFragment.this, passportTx64)).execute();
    }

    public void showError(final String message) {
        ReadingPassportFragment.activity = getActivity();

        if(ReadingPassportFragment.activity != null) {
            ReadingPassportFragment.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ReadingPassportFragment.this.isActivityRunning && ReadingPassportFragment.activity != null) {
                        new AlertDialog.Builder(ReadingPassportFragment.activity)
                                .setTitle(getResources().getString(R.string.error_error))
                                .setMessage(message)
                                .setCancelable(false)
                                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //@TODO
                                    }
                                }).create().show();
                    }
                }
            });
        }

    }


    public void setMrtdProgressBarPercentage(int progress) {
        this.mrtdProgressBar.setProgress(progress);
        final int finalProgress = progress;

        /*
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                percentageDisplay.setText(finalProgress + " %");
            }
        });*/

    }

    public void setSOD(byte[] sod) {
        this.sod = sod;
    }

    public void setDG1(byte[] dg1) {
        this.dg1 = dg1;
    }

    public void setDG2(byte[] dg2) {
        this.dg2 = dg2;
    }

    private class AsyncReader extends AsyncTask<Void, Integer, Boolean> implements ProgressListenerInterface {

        private boolean success = false;
        private WeakReference<ReadingPassportFragment> readingPassportActivity;
        private String passportNumber;
        private String dateOfBirth;
        private String dateOfExpiration;
        private String challenge;
        private boolean isCanceled = false;

        public AsyncReader(
                ReadingPassportFragment readingPassportActivity,
                String passportNumber,
                String dateOfBirth,
                String dateOfExpiration,
                String challenge
        ) {

            this.passportNumber = passportNumber;
            this.dateOfBirth = dateOfBirth;
            this.dateOfExpiration = dateOfExpiration;
            this.challenge = challenge;
            this.isCanceled = false;

            this.link(readingPassportActivity);
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {


            try {

                if (TagProvider.getTag() != null) {
                    // we first try with our own reading software as it is the most reliable
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
                        byte[] dg1;
                        byte[] dg2;
                        int authMode = (new ChallengeParser(challenge)).getAuthenticationMode();
                        System.out.println("challenge: " + challenge);
                        System.out.println("authMode: " + authMode);

                        if(authMode == 2) {
                            dg2 = mrtd.readFile(MrtdConstants.FID_DG2);
                            this.readingPassportActivity.get().setDG2(dg2);
                        }

                        if(authMode == 1 || authMode == 2) {
                            dg1 = mrtd.readFile(MrtdConstants.FID_DG1);
                            this.readingPassportActivity.get().setDG1(dg1);
                        }

                        this.readingPassportActivity.get().setSOD(efsod);

                        if (sod != null) {
                            this.success = true;
                        }

                    } else {
                        // Then we try with JMRTD which also supports PACE
                        BACKeySpec bacKey = new BACKey(passportNumber, dateOfBirth, dateOfExpiration);

                        CardService cardService = CardService.getInstance(TagProvider.getTag());
                        cardService.open();

                        PassportService service = new PassportService(cardService, NORMAL_MAX_TRANCEIVE_LENGTH, DEFAULT_MAX_BLOCKSIZE, true, false);
                        service.open();

                        boolean paceSucceeded = false;
                        try {
                            CardAccessFile cardSecurityFile = new CardAccessFile(service.getInputStream(PassportService.EF_CARD_ACCESS));
                            Collection<SecurityInfo> securityInfoCollection = cardSecurityFile.getSecurityInfos();
                            for (SecurityInfo securityInfo : securityInfoCollection) {
                                if (securityInfo instanceof PACEInfo) {
                                    PACEInfo paceInfo = (PACEInfo) securityInfo;
                                    service.doPACE(bacKey, paceInfo.getObjectIdentifier(), PACEInfo.toParameterSpec(paceInfo.getParameterId()), null);
                                    paceSucceeded = true;
                                    System.out.println("PACE succeeded");
                                } else {
                                    System.out.println("PACE failed");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        service.sendSelectApplet(paceSucceeded);

                        if (!paceSucceeded) {
                            try {
                                service.getInputStream(PassportService.EF_COM).read();
                            } catch (Exception e) {
                                service.doBAC(bacKey);
                            }
                        }

                        CardFileInputStream sodStream = service.getInputStream(PassportService.EF_SOD);
                        //SODFile sodFile = new SODFile(sodStream);

                        //sodStream.read();
                        int sodLength = sodStream.getLength();
                        sod = new byte[sodLength];
                        sodStream.read(sod);

                        this.readingPassportActivity.get().setSOD(sod);

                        if (sod != null) {
                            this.success = true;
                        } else {
                            return false;
                        }
                    }
                } else {
                    System.out.println("Couldn't get Tag from intent");
                    this.readingPassportActivity.get().showError(getResources().getString(R.string.error_lost_connexion));
                    try {
                        ((MainActivity) getActivity()).goToNavWaitForNfc();
                    } catch (Exception e) {

                    }
                    return false;
                }
            } catch (Exception e) {
                System.out.println("Exception");
                if(e.getMessage() != null) {
                    System.out.println(e.getMessage());
                }
                System.out.println(Log.getStackTraceString(e));
                this.readingPassportActivity.get().showError(getResources().getString(R.string.error_nfc_exception));
                return false;
            }

            if(!this.success) {
                this.readingPassportActivity.get().showError(getResources().getString(R.string.error_nfc_exception));
            }

            //this.readingPassportActivity.get().setMrtdProgressBarPercentage(95);

            return true;
        }

        public void link(ReadingPassportFragment readingPassportActivity) {
            this.readingPassportActivity = new WeakReference<ReadingPassportFragment>(readingPassportActivity);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (this.success) {
                if(challenge.isEmpty()) {
                    this.readingPassportActivity.get().createAndSendRegisterPassportTransaction();
                } else {
                    this.readingPassportActivity.get().createAndSendKYCTransaction();
                }
            } else {
                if(challenge.isEmpty()) {
                    ((MainActivity) getActivity()).goToNavRegisterPassport();
                } else {
                    ((MainActivity) getActivity()).goToNavKYCPassport(challenge);
                }
            }
        }

        public void updateProgress(int progress) {
            //this.readingPassportActivity.get().setMrtdProgressBarPercentage(progress);
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


    public void onSendTransactionCompleted(int responseCode, String response) {
        System.out.println("onSendTransactionCompleted code: " + responseCode + " reponse: " + response);

        try {
            JSONObject jsonObj = new JSONObject(response);
            boolean success = jsonObj.getBoolean("success");

            if(success) {
                ((TextView)this.view.findViewById(R.id.readingPassportTextView)).setText(R.string.uploading_passport_transaction_done);
            } else {
                String errorMessage;

                ((TextView)this.view.findViewById(R.id.readingPassportTextView)).setText(R.string.error_error);

                if(!jsonObj.getString("error").isEmpty()) {
                    errorMessage = jsonObj.getString("error").toString();
                } else {
                    errorMessage = getResources().getString(R.string.error_passport_unknown_error);
                }

                new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.error_error))
                        .setMessage(errorMessage)
                        .setNegativeButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                })
                        .setCancelable(true).create().show();
            }
        } catch (Exception e) {
            e.printStackTrace();

            ((TextView)this.view.findViewById(R.id.readingPassportTextView)).setText(R.string.error_error);

            new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.error_error))
                    .setMessage(getResources().getString(R.string.error_passport_invalid_server_response))
                    .setNegativeButton(getResources().getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.cancel();
                                }
                            })
                    .setCancelable(true).create().show();
        }

        view.findViewById(R.id.uploadPassportTransactionProgressBar).setVisibility(View.GONE);
    }
}
