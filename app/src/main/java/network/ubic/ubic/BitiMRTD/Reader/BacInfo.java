package network.ubic.ubic.BitiMRTD.Reader;

public class BacInfo {

    private String passportNbr;
    private String dateOfBirth;
    private String dateOfExpiry;

    public void setPassportNbr(String passportNbr)
    {
        passportNbr = passportNbr.replace(" ", "");

        while(passportNbr.length() < 9) {
            passportNbr = passportNbr.concat("<");
        }

        this.passportNbr = passportNbr;
    }

    public String getPassportNbr()
    {
        return this.passportNbr;
    }

    /**
     * format has to be yymmdd for example 991031
     * @param dateOfBirth
     */
    public void setDateOfBirth(String dateOfBirth)
    {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDateOfBirth()
    {
        return this.dateOfBirth;
    }

    /**
     * format has to be yymmdd for example 991031
     * @param dateOfExpiry
     */
    public void setDateOfExpiry(String dateOfExpiry)
    {
        this.dateOfExpiry = dateOfExpiry;
    }

    public String getDateOfExpiry()
    {
        return this.dateOfExpiry;
    }

}
