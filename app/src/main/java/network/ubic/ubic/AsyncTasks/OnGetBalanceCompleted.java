package network.ubic.ubic.AsyncTasks;

import android.util.Pair;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface OnGetBalanceCompleted {

    void OnGetBalanceCompleted(
            HashMap<Integer, BigInteger> balanceMap,
            Map<Integer, Pair<String, HashMap<Integer, BigInteger>>> transactions,
            Map<Integer, Pair<String, HashMap<Integer, BigInteger>>> pendingTransactions,
            boolean isReceivingUBI,
            List<Pair<String, BigInteger>> ubiExpirationDate,
            boolean isEmptyAddress,
            int nonce
    );
}