package network.ubic.ubic;

import java.util.ArrayList;

public class Currencies {

    private ArrayList<String> currencies;

    public Currencies() {
        currencies = new ArrayList<String>();
        currencies.add(0, "");
        currencies.add(1, "UCH");
        currencies.add(2, "UDE");
        currencies.add(3, "UAT");
        currencies.add(4, "UUK");
        currencies.add(5, "UIE");
        currencies.add(6, "UUS");
        currencies.add(7, "UAU");
        currencies.add(8, "UCN");
        currencies.add(9, "USE");
        currencies.add(10, "UFR");
        currencies.add(11, "UCA");
        currencies.add(12, "UJP");
        currencies.add(13, "UTH");
        currencies.add(14, "UNZ");
        currencies.add(15, "UAE");
        currencies.add(16, "UFI");
        currencies.add(17, "ULU");
        currencies.add(18, "USG");
        currencies.add(19, "UHU");
        currencies.add(20, "UCZ");
        currencies.add(21, "UMY");
        currencies.add(22, "UUA");
        currencies.add(23, "UEE");
        currencies.add(24, "UMC");
        currencies.add(25, "ULI");
        currencies.add(26, "UIS");
        currencies.add(27, "UHK");
        currencies.add(28, "UES");
        currencies.add(29, "URU");
        currencies.add(30, "UIL");
        currencies.add(31, "UPT");
        currencies.add(32, "UDK");
        currencies.add(33, "UTR");
        currencies.add(34, "URO");
        currencies.add(35, "UPL");
        currencies.add(36, "UNL");
    }

    public ArrayList<String> getCurrencies() {
        return currencies;
    }

    public String getCurrency(int currencyId) {
        System.out.println("currencyId: " + currencyId);
        return currencies.get(currencyId);
    }

    public int getCurrency(String currencyId) {
        for(int i= 0; i < currencies.size(); i++) {
            if(currencies.get(i) == currencyId) {
                return i;
            }
        }
        return -1;
    }
}
