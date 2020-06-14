
#include "TransactionHelper.h"
#include "../Serialization/streams.h"
#include "../Tools/Log.h"
#include "../Crypto/Hash256.h"
#include "../NtpEsk/NtpEskSignatureVerificationObject.h"
#include "../Crypto/VerifySignature.h"
#include "../AddressHelper.h"
#include "../Wallet.h"
#include "../NtpRsk/NtpRsk.h"

std::vector<unsigned char> TransactionHelper::getDeactivateCertificateScriptId(DeactivateCertificateScript deactivateCertificateScript) {
    deactivateCertificateScript.rootCertSignature = std::vector<unsigned char>();

    CDataStream s(SER_DISK, 1);
    s << deactivateCertificateScript;

    return std::vector<unsigned char>(s.data(), s.data() + s.size());
}

std::vector<unsigned char> TransactionHelper::getTxId(Transaction* tx) {
    Transaction txClone = *tx;
    UScript* emptyScript = new UScript();
    std::vector<unsigned char> emptyVector = std::vector<unsigned char>();
    emptyScript->setScriptType(SCRIPT_EMPTY);
    emptyScript->setScript(emptyVector);

    std::vector<TxIn> cleanedTxIns;
    std::vector<TxIn> txIns = tx->getTxIns();
    for (std::vector<TxIn>::iterator txIn = txIns.begin(); txIn != txIns.end(); ++txIn) {
        txIn->setScript(*emptyScript);
        cleanedTxIns.emplace_back(*txIn);
    }

    txClone.setTxIns(cleanedTxIns);

    CDataStream s(SER_DISK, 1);
    s << txClone;

    std::vector<unsigned char> txId = Hash256::hash256(std::vector<unsigned char>(s.data(), s.data() + s.size()));

    return txId;
}

std::vector<unsigned char> TransactionHelper::getTxHash(Transaction* tx) {
    CDataStream s(SER_DISK, 1);
    s << *tx;

    std::vector<unsigned char> txHash = Hash256::hash256(std::vector<unsigned char>(s.data(), s.data() + s.size()));

    return txHash;
}

uint32_t TransactionHelper::getTxSize(Transaction* tx) {
    CDataStream s(SER_DISK, 1);
    s << *tx;

    return (uint32_t)s.size();
}

std::vector<unsigned char> TransactionHelper::getPassportHash(Transaction* tx) {
    CDataStream srpScript(SER_DISK, 1);
    UScript script = tx->getTxIns().front().getScript();
    srpScript.write((char *) script.getScript().data(), script.getScript().size());

    if((uint32_t)script.getScript().at(0) % 2 == 0) {
        // is NtpRsk
        NtpRskSignatureVerificationObject *ntpRskSignatureVerificationObject = new NtpRskSignatureVerificationObject();

        try {
            srpScript >> *ntpRskSignatureVerificationObject;
        } catch (const std::exception& e) {
            Log(LOG_LEVEL_ERROR) << "Failed to deserialize SCRIPT_REGISTER_PASSPORT payload";
            return std::vector<unsigned char>();
        }

        return ECCtools::bnToVector(ntpRskSignatureVerificationObject->getM());
    } else {
        //@TODO fix: important
        /*
        // is NtpEsk
        NtpEskSignatureVerificationObject *ntpEskSignatureVerificationObject = new NtpEskSignatureVerificationObject();
        CertStore& certStore = CertStore::Instance();
        Cert* cert = certStore.getDscCertWithCertId(tx->getTxIns().front().getInAddress());
        EC_KEY* ecKey = EVP_PKEY_get1_EC_KEY(cert->getPubKey());
        ntpEskSignatureVerificationObject->setCurveParams(EC_KEY_get0_group(ecKey));
        try {
            srpScript >> *ntpEskSignatureVerificationObject;
        } catch (const std::exception& e) {
            Log(LOG_LEVEL_ERROR) << "Failed to deserialize SCRIPT_REGISTER_PASSPORT payload";
            return std::vector<unsigned char>();
        }
        return ntpEskSignatureVerificationObject->getMessageHash();
        */
    }
}
