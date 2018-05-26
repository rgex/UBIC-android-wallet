package network.ubic.ubic;

import java.math.BigInteger;
import java.util.HashMap;

public interface OnGetBalanceCompleted {

    void OnGetBalanceCompleted(HashMap<Integer, BigInteger> balanceMap);
}
