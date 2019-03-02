package network.ubic.ubic.AsyncTasks;

import java.math.BigInteger;
import java.util.HashMap;

public interface OnGetTransactionFeesCompleted {
    void OnGetTransactionFeesCompleted(HashMap<Integer, BigInteger> feesMap);
}
