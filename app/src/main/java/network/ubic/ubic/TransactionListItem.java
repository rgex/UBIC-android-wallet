package network.ubic.ubic;

public class TransactionListItem {
    private int transactionSign; // 0 = -, 1 = +
    private String transactionAmount;
    private String transactionDate;
    private String transactionType = "";

    public int getTransactionSign() {
        return transactionSign;
    }

    public void setTransactionSign(int transactionSign) {
        this.transactionSign = transactionSign;
    }

    public String getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
}
