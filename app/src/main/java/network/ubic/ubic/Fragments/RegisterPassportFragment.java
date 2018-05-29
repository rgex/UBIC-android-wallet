package network.ubic.ubic.Fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

    private OnFragmentInteractionListener mListener;

    public RegisterPassportFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
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
                    public void onClick(View view) {

                        try {

                            Intent intent = new Intent("bondi.nfcPassportReader.jan.mrtd2.WaitingForNfcActivity");

                            String passportNumber = ((EditText) view.findViewById(R.id.PassportNbr)).getText().toString();

                            intent.putExtra("passportNumber", passportNumber);
                            intent.putExtra("dateOfBirth", RegisterPassportFragment.this.dateOfBirth);
                            intent.putExtra("dateOfExpiration", RegisterPassportFragment.this.dateOfExpiration);

                            startActivity(intent);
                        }
                        catch(Exception e) {
                            //@TODO
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
