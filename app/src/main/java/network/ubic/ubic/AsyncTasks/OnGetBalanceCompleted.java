package network.ubic.ubic.AsyncTasks;

import java.math.BigInteger;
import java.util.HashMap;

public interface OnGetBalanceCompleted {

    void OnGetBalanceCompleted(HashMap<Integer, BigInteger> balanceMap, HashMap<String, HashMap<Integer, BigInteger>> transactions, boolean isReceivingUBI);
}