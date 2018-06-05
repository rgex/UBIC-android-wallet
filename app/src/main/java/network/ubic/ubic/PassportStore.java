package network.ubic.ubic;

import android.content.Context;
import android.content.SharedPreferences;

public class PassportStore {

    private Context context;
    private SharedPreferences sharedPassportPref;
    private String passportNumber;
    private String dateOfBirth;
    private String dateOfExpiry;

    public PassportStore(Context context) {
        this.context = context;
        sharedPassportPref = context.getSharedPreferences(
                context.getResources().getString(
                        R.string.passport_info_shared_pref),
                Context.MODE_PRIVATE
        );

        passportNumber = sharedPassportPref.getString(
                context.getResources().getString(R.string.passport_number_shared_pref),
                ""
        );

        dateOfBirth = sharedPassportPref.getString(
                context.getResources().getString(R.string.passport_date_of_birth_shared_pref),
                ""
        );

        dateOfExpiry = sharedPassportPref.getString(
                context.getResources().getString(R.string.passport_date_of_expiry_shared_pref),
                ""
        );
    }

    public void persist() {

        SharedPreferences.Editor editor = sharedPassportPref.edit();

        editor.putString(
                context.getResources().getString(R.string.passport_number_shared_pref),
                passportNumber
        );

        editor.putString(
                context.getResources().getString(R.string.passport_date_of_birth_shared_pref),
                dateOfBirth
        );

        editor.putString(
                context.getResources().getString(R.string.passport_date_of_expiry_shared_pref),
                dateOfExpiry
        );

        editor.commit();
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDateOfExpiry() {
        return dateOfExpiry;
    }

    public void setDateOfExpiry(String dateOfExpiry) {
        this.dateOfExpiry = dateOfExpiry;
    }
}
