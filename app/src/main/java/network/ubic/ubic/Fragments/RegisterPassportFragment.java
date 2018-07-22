package network.ubic.ubic.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import network.ubic.ubic.MainActivity;
import network.ubic.ubic.PassportStore;
import network.ubic.ubic.R;

/**
 * Activities that contain this fragment must implement the
 * {@link RegisterPassportFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RegisterPassportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterPassportFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    private View view;

    public int selectedDateField;
    public String dateOfBirth = "000000";
    public int[] dateOfBirthIntArray = {15,6,1980};
    public String dateOfExpiration = "000000";
    public int[] dateOfExpirationIntArray = {15,6,2020};
    public String passportNumber;

    private OnFragmentInteractionListener mListener;

    public RegisterPassportFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RegisterPassportFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterPassportFragment newInstance(String param1, String param2) {
        RegisterPassportFragment fragment = new RegisterPassportFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void displayDatePickerDialog()
    {

        view.findViewById(R.id.DateOfBirth).clearFocus();
        view.findViewById(R.id.DateOfExpiration).clearFocus();
        view.findViewById(R.id.PassportNbr).clearFocus();

        Calendar calendar = Calendar.getInstance();

        //date of expiration
        int calendarYear = this.dateOfExpirationIntArray[2];
        int calendarMonth = this.dateOfExpirationIntArray[1];
        int calendarDay = this.dateOfExpirationIntArray[0];

        if(this.selectedDateField == 1) { // date of birth
            calendarYear = this.dateOfBirthIntArray[2];
            calendarMonth = this.dateOfBirthIntArray[1];
            calendarDay = this.dateOfBirthIntArray[0];
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                this,
                calendarYear,
                calendarMonth,
                calendarDay
        );

        datePickerDialog.getDatePicker().setCalendarViewShown(false);
        datePickerDialog.getDatePicker().setSpinnersShown(true);
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
    {
        if (this.selectedDateField == 1) {
            this.setDateToTextView("dob", year, monthOfYear, dayOfMonth);
        }
        if (this.selectedDateField == 2) {
            this.setDateToTextView("doe", year, monthOfYear, dayOfMonth);
        }
    }

    public void setDateToTextView(String fieldName, int year, int monthOfYear, int dayOfMonth)
    {
        try {
            monthOfYear += 1;

            String displayDate = String.valueOf(dayOfMonth)
                    .concat("/")
                    .concat(String.valueOf(monthOfYear))
                    .concat("/")
                    .concat(String.valueOf(year));

            Date selectedDate = (new SimpleDateFormat("dd/MM/yyyy")).parse(displayDate);
            String passportDate = (new SimpleDateFormat("yyMMdd")).format(selectedDate);

            displayDate = SimpleDateFormat.getDateInstance().format(selectedDate);

            if (fieldName.equals("dob")) {
                ((EditText) view.findViewById(R.id.DateOfBirth)).setText(displayDate);
                this.dateOfBirth = passportDate;

                this.dateOfBirthIntArray[2] = year;
                this.dateOfBirthIntArray[1] = monthOfYear - 1;
                this.dateOfBirthIntArray[0] = dayOfMonth;
            }
            if (fieldName.equals("doe")) {
                ((EditText) view.findViewById(R.id.DateOfExpiration)).setText(displayDate);
                this.dateOfExpiration = passportDate;

                this.dateOfExpirationIntArray[2] = year;
                this.dateOfExpirationIntArray[1] = monthOfYear - 1;
                this.dateOfExpirationIntArray[0] = dayOfMonth;
            }
        }
        catch (Exception e) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

        if (mNfcAdapter == null || !mNfcAdapter.isEnabled()) {
            System.out.println("failed to get NFC adapter, NFC disabled?");

            new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.error_error))
                    .setMessage(getResources().getString(R.string.error_nfc_is_disabled))
                    .setCancelable(true)
                    .setPositiveButton("enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                                startActivity(intent);
                            } catch (Exception e) {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(getResources().getString(R.string.success))
                                        .setMessage(getResources().getString(R.string.go_to_nfc_settings))
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
                    }).create().show();
        }

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_register_passport, container, false);

        ((EditText)view.findViewById(R.id.DateOfBirth)).setText("");
        ((EditText)view.findViewById(R.id.DateOfExpiration)).setText("");

        ((EditText)view.findViewById(R.id.PassportNbr)).setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        view.findViewById(R.id.RegisterPassportMainLayout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        view.findViewById(R.id.PassportNbr).clearFocus();
                        view.findViewById(R.id.DateOfBirth).clearFocus();
                        view.findViewById(R.id.DateOfExpiration).clearFocus();
                    }
                }
        );

        ((EditText)view.findViewById(R.id.DateOfBirth)).setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        if (hasFocus) {
                            RegisterPassportFragment.this.selectedDateField = 1;
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            RegisterPassportFragment.this.displayDatePickerDialog();
                        }
                    }
                }
        );

        ((EditText)view.findViewById(R.id.DateOfExpiration)).setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        if (hasFocus) {
                            RegisterPassportFragment.this.selectedDateField = 2;
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            RegisterPassportFragment.this.displayDatePickerDialog();
                        }
                    }
                }
        );


        view.findViewById(R.id.ReadNfcBtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view2) {

                        passportNumber = ((EditText) view.findViewById(R.id.PassportNbr)).getText().toString();

                        PassportStore passportStore = new PassportStore(getContext());
                        passportStore.setDateOfBirth(dateOfBirth);
                        passportStore.setDateOfExpiry(dateOfExpiration);
                        passportStore.setPassportNumber(passportNumber);
                        passportStore.persist();

                        try {
                            ((MainActivity)getActivity()).goToNavWaitForNfc(RegisterPassportFragment.this.passportNumber, RegisterPassportFragment.this.dateOfBirth, RegisterPassportFragment.this.dateOfExpiration);
                        }
                        catch(Exception e) {
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
