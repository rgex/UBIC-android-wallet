package network.ubic.ubic.AsyncTasks;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public interface OnGetBalanceCompleted {

    void OnGetBalanceCompleted(
            HashMap<Integer, BigInteger> balanceMap,
            Map<Integer, HashMap<Integer, BigInteger>> transactions,
            boolean isReceivingUBI,
            boolean isEmptyAddress,
            int nonce
    );
}