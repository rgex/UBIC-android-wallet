package network.ubic.ubic.BitiMRTD.Parser;
import network.ubic.ubic.BitiMRTD.Tools.Tools;

import java.util.Arrays;

public class DG1Parser {

    private String mrz;
    private String documentCode;
    private String issuingStateCode;
    private String documentNumber;
    private String documentNumberCheckDigit;
    private String gender;
    private String givenNames;
    private String surname;
    private String nationalityCode;
    private String dateOfBirth;
    private String dateOfBirthCheckDigit;
    private String dateOfExpiry;
    private String dateOfExpiryCheckDigit;
    private Tools tools;

    public DG1Parser(byte[] mrz)
    {

        this.tools = new Tools();
        this.mrz = Arrays.toString(mrz);
        this.clean();

        TagParser tagParser = new TagParser(mrz);

        TagParser tag5F1F = tagParser.geTag("61").geTag("5F1F");

        this.buildTD1(tag5F1F.getBytes());

        if(this.isCorrect()) {
            System.out.println("Detected TD1 format");
        }
        else {
            this.buildTD2(tag5F1F.getBytes());
            if (this.isCorrect()) {
                System.out.println("Detected TD2 format");
            }
            else {
                this.clean();

                //TD3 Format (German format):
                this.buildTD3(tag5F1F.getBytes());

                if (this.isCorrect()) {
                    System.out.println("Detected TD3 format");
                } else {
                    System.out.println("Couldn't find TD format");
                }
            }
        }
    }

    /**
     * Returns true if
     * @return boolean
     */
    public boolean isCorrect()
    {
        if(this.getDocumentCode() == null || this.getDocumentCode().equals("")) {
            System.out.println("DG1 object verification failed at document code");
            return false;
        }

        if(this.getIssuingStateCode() == null || this.getIssuingStateCode().equals("")) {
            System.out.println("DG1 object verification failed at issuing state code");
            return false;
        }

        if(this.getDocumentNumber() == null || this.getDocumentNumber().equals("")) {
            System.out.println("DG1 object verification failed at document number");
            return false;
        }

        if(this.getDocumentNumberCheckDigit() == null || this.getDocumentNumberCheckDigit().equals("")) {
            System.out.println("DG1 object verification failed at document number check digit");
            return false;
        }

        if (this.getGender() == null || this.getGender().equals("")) {
            System.out.println("DG1 object verification failed at gender");
            return false;
        }

        if(this.getGivenNames() == null || this.getGivenNames().equals("")) {
            System.out.println("DG1 object verification failed at given names");
            return false;
        }

        if(this.getSurname() == null || this.getSurname().equals("")) {
            System.out.println("DG1 object verification failed at surname");
            return false;
        }

        if(this.getNationalityCode() == null || this.getNationalityCode().equals("")) {
            System.out.println("DG1 object verification failed at nationality code");
            return false;
        }

        if(this.getDateOfBirth() == null || this.getDateOfBirth().equals("")) {
            System.out.println("DG1 object verification failed at date of birth");
            return false;
        }

        if(this.getDateOfBirthCheckDigit() == null || this.getDateOfBirthCheckDigit().equals("")) {
            System.out.println("DG1 object verification failed at date of birth check digit");
            return false;
        }

        if(this.getDateOfExpiry() == null || this.getDateOfExpiry().equals("")) {
            System.out.println("DG1 object verification failed at date of expiry");
            return false;
        }

        if(this.getDateOfExpiryCheckDigit() == null || this.getDateOfExpiryCheckDigit().equals("")) {
            System.out.println("DG1 object verification failed at date of expiry check digit");
            return false;
        }

        if(!String.valueOf(this.tools.calculateMrzCheckDigit(this.getDocumentNumber())).equals(this.getDocumentNumberCheckDigit())) {
            System.out.println("DG1 object verification failed to verify document number check digit");
            return false;
        }

        if(!String.valueOf(this.tools.calculateMrzCheckDigit(this.getDateOfBirth())).equals(this.getDateOfBirthCheckDigit())) {
            System.out.println("DG1 object verification failed to verify date of birth check digit");
            return false;
        }

        if(!String.valueOf(this.tools.calculateMrzCheckDigit(this.getDateOfExpiry())).equals(this.getDateOfExpiryCheckDigit())) {
            System.out.println("DG1 object verification failed to verify date of expiry check digit");
            return false;
        }

        return true;
    }

    public void clean()
    {
        this.setDocumentCode("");
        this.setIssuingStateCode("");
        this.setDocumentNumber("");
        this.setGender("");
        this.setGivenNames("");
        this.setSurname("");
        this.setNationalityCode("");
        this.setDateOfBirth("");
        this.setDateOfExpiry("");
    }

    private String readSection(byte[] zone, int cursor, int length)
    {
        if(zone.length < cursor + length) {
            System.out.println("Tried to read out of range section");
            return null;
        }
        byte[] scope = Arrays.copyOfRange(zone, cursor, cursor + length);
        String result = new String(scope);
        result = result.replace("<", "");
        System.out.println("result : ".concat(result));
        return result;
    }

    private String[] parseName(byte[] zone)
    {
        String stringZone = new String(zone);
        String[] nameArray = stringZone.split("<<");
        if(nameArray.length < 2) {
            return null;
        }
        else {
            return nameArray;
        }
    }

    private String parseGivenNames(byte[] zone)
    {
        String[] name = this.parseName(zone);
        if(name == null) {
            return null;
        }
        return name[1].replace("<", " ");
    }

    private String parseSurname(byte[] zone)
    {
        String[] name = this.parseName(zone);
        if(name == null) {
            return null;
        }
        return name[0].replace("<", " ");
    }

    private void buildTD1(byte[] mrz)
    {
        if(mrz.length < 93) {
            System.out.println("Skip TD1 build, mrz is too short");
            return;
        }

        int cursor = 0;

        this.documentCode = this.readSection(mrz, cursor, 2);
        cursor += 2;

        this.issuingStateCode = this.readSection(mrz, cursor, 3);
        cursor += 3;

        this.documentNumber = this.readSection(mrz, cursor, 9);
        cursor += 9;

        this.documentNumberCheckDigit = this.readSection(mrz, cursor, 1);
        cursor += 1;

        cursor += 15; // skip optional data

        this.dateOfBirth = this.readSection(mrz, cursor, 6);
        cursor += 6;

        this.dateOfBirthCheckDigit = this.readSection(mrz, cursor, 1);
        cursor += 1;

        this.gender = this.readSection(mrz, cursor, 1);
        cursor += 1;

        this.dateOfExpiry = this.readSection(mrz, cursor, 6);
        cursor += 6;

        this.dateOfExpiryCheckDigit = this.readSection(mrz, cursor, 1);
        cursor += 1;

        this.nationalityCode = this.readSection(mrz, cursor, 3);
        cursor += 3;

        cursor += 11; // skip optional data

        cursor += 1; // skip composite check digit

        this.surname = this.parseSurname(Arrays.copyOfRange(mrz, cursor, cursor + 30));
        this.givenNames = this.parseGivenNames(Arrays.copyOfRange(mrz, cursor, cursor + 30));
        cursor += 30;
    }

    private void buildTD2(byte[] mrz)
    {
        if(mrz.length < 67) {
            System.out.println("Skip TD2 build, mrz is too short");
            return;
        }

        int cursor = 0;

        this.documentCode = this.readSection(mrz, cursor, 2);
        cursor += 2;

        this.issuingStateCode = this.readSection(mrz, cursor, 3);
        cursor += 3;

        this.surname = this.parseSurname(Arrays.copyOfRange(mrz, cursor, cursor + 31));
        this.givenNames = this.parseGivenNames(Arrays.copyOfRange(mrz, cursor, cursor + 31));
        cursor += 31;

        this.documentNumber = this.readSection(mrz, cursor, 9);
        cursor += 9;

        this.documentNumberCheckDigit = this.readSection(mrz, cursor, 1);
        cursor += 1;

        this.nationalityCode = this.readSection(mrz, cursor, 3);
        cursor += 3;

        this.dateOfBirth = this.readSection(mrz, cursor, 6);
        cursor += 6;

        this.dateOfBirthCheckDigit = this.readSection(mrz, cursor, 1);
        cursor += 1;

        this.gender = this.readSection(mrz, cursor, 1);
        cursor += 1;

        this.dateOfExpiry = this.readSection(mrz, cursor, 6);
        cursor += 6;

        this.dateOfExpiryCheckDigit = this.readSection(mrz, cursor, 1);
        cursor += 1;

    }

    private void buildTD3(byte[] mrz)
    {
        if(mrz.length < 75) {
            System.out.println("Skip TD3 build, mrz is too short");
            return;
        }

        int cursor = 0;

        this.documentCode = this.readSection(mrz, cursor, 2);
        cursor += 2;

        this.issuingStateCode = this.readSection(mrz, cursor, 3);
        cursor += 3;

        this.surname = this.parseSurname(Arrays.copyOfRange(mrz, cursor, cursor + 39));
        this.givenNames = this.parseGivenNames(Arrays.copyOfRange(mrz, cursor, cursor + 39));
        cursor += 39;

        this.documentNumber = this.readSection(mrz, cursor, 9);
        cursor += 9;

        this.documentNumberCheckDigit = this.readSection(mrz, cursor, 1);
        cursor += 1;

        this.nationalityCode = this.readSection(mrz, cursor, 3);
        cursor += 3;

        this.dateOfBirth = this.readSection(mrz, cursor, 6);
        cursor += 6;

        this.dateOfBirthCheckDigit = this.readSection(mrz, cursor, 1);
        cursor += 1;

        this.gender = this.readSection(mrz, cursor, 1);
        cursor += 1;

        this.dateOfExpiry = this.readSection(mrz, cursor, 6);
        cursor += 6;

        this.dateOfExpiryCheckDigit = this.readSection(mrz, cursor, 1);
        cursor += 1;

    }

    public String getMRZ() {
        return new String(this.mrz);
    }

    public void setMRZ(String mrz) {
        this.mrz = mrz;
    }

    public String getDocumentCode() {
        return this.documentCode;
    }

    public void setDocumentCode(String documentCode) {
        this.documentCode = documentCode;
    }

    public String getIssuingStateCode() {
        return this.issuingStateCode;
    }

    public void setIssuingStateCode(String issuingStateCode) {
        this.issuingStateCode = issuingStateCode;
    }

    public String getDocumentNumber() {
        return this.documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentNumberCheckDigit() {
        return this.documentNumberCheckDigit;
    }

    public void setDocumentNumberCheckDigit(String documentNumberCheckDigit) {
        this.documentNumberCheckDigit = documentNumberCheckDigit;
    }

    public String getGender() {
        return this.gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGivenNames() {
        return this.givenNames;
    }

    public void setGivenNames(String firstName) {
        this.givenNames = firstName;
    }

    public String getSurname() {
        return this.surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getNationalityCode() {
        return this.nationalityCode;
    }

    public void setNationalityCode(String nationalityCode) {
        this.nationalityCode = nationalityCode;
    }

    public String getDateOfBirth() {
        return this.dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDateOfExpiry() {
        return this.dateOfExpiry;
    }

    public void setDateOfExpiry(String dateOfExpiry) {
        this.dateOfExpiry = dateOfExpiry;
    }


    public String getDateOfBirthCheckDigit() {
        return this.dateOfBirthCheckDigit;
    }

    public void setDateOfBirthCheckDigit(String dateOfBirthCheckDigit) {
        this.dateOfBirthCheckDigit = dateOfBirthCheckDigit;
    }

    public String getDateOfExpiryCheckDigit() {
        return this.dateOfExpiryCheckDigit;
    }

    public void setDateOfExpiryCheckDigit(String dateOfExpiryCheckDigit) {
        this.dateOfExpiryCheckDigit = dateOfExpiryCheckDigit;
    }

}
