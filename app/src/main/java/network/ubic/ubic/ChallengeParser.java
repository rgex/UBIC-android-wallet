package network.ubic.ubic;

public class ChallengeParser {

    private String challenge; // example: 3-utopia.org/kyc/challenge=123456789

    public ChallengeParser(String newChallenge) {
        this.challenge = newChallenge;
    }

    public boolean validateChallenge() {

        if(challenge.length() < 5) { // too short
            return false;
        }

        String firstChar = challenge.substring(0, 1);
        String secondChar = challenge.substring(1, 2);

        if(!(firstChar.equals("0") || firstChar.equals("1") || firstChar.equals("2") || firstChar.equals("3"))) {
            return false;
        }
        if(!secondChar.equals("-")) {
            return false;
        }

        return true;
    }

    public int getAuthenticationMode() {
        if(validateChallenge()) {
            return Integer.parseInt(challenge.substring(0, 1));
        }

        return 0;
    }

    public String getUrl() {
        return "http://" + challenge.substring(2, challenge.length());
    }
}
