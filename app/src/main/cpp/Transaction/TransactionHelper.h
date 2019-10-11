
#ifndef TX_TRANSACTIONHELPER_H
#define TX_TRANSACTIONHELPER_H

#include <vector>
#include <list>
#include "TxIn.h"
#include "TxOut.h"
#include "Transaction.h"
#include "../Scripts/DeactivateCertificateScript.h"

class TransactionHelper {
public:
    static std::vector<unsigned char> getDeactivateCertificateScriptId(DeactivateCertificateScript deactivateCertificateScript);
    static std::vector<unsigned char> getTxId(Transaction* tx);
    static std::vector<unsigned char> getTxHash(Transaction* tx);
    static uint32_t getTxSize(Transaction* tx);
    static std::vector<unsigned char> getPassportHash(Transaction* tx);
};


#endif //TX_TRANSACTIONHELPER_H
