#include <jni.h>
#include <string>
#include <iostream>
#include "Wallet.h"
#include "Tools/Log.h"
#include "PassportReader/PKCS7/PKCS7Parser.h"
#include "CertStore/Cert.h"
#include "Transaction/TransactionHelper.h"
#include "Base64.h"

extern "C" JNIEXPORT jstring

JNICALL
Java_network_ubic_ubic_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


extern "C" JNIEXPORT jstring

JNICALL
Java_network_ubic_ubic_AsyncTasks_ReceiveFragmentPopulate_getAddress(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray seed) {

    std::cout << "C++:" << std::endl;

    Wallet& wallet = Wallet::Instance();

    int len = env->GetArrayLength (seed);
    std::cout << "Length:" << len << std::endl;
    unsigned char* seedUC = new unsigned char[len];
    env->GetByteArrayRegion (seed, 0, len, reinterpret_cast<jbyte*>(seedUC));

    std::vector<unsigned char> seedVector = std::vector<unsigned char>(seedUC, seedUC + 20);
    wallet.setSeed(seedVector);


    std::cout << "seedUC" << std::endl;
    wallet.initWallet();
    std::cout << "initWallet" << std::endl;
    Address address = wallet.getRandomAddressFromWallet();

    return env->NewStringUTF(wallet.readableAddressFromAddress(address).c_str());

    //return env->NewStringUTF((const char*)seedUC);
}

extern "C" JNIEXPORT jstring

JNICALL
Java_network_ubic_ubic_Fragments_ReadingPassportFragment_getPassportTransaction(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray sod) {

    int len = env->GetArrayLength (sod);
    std::cout << "Length:" << len << std::endl;
    unsigned char* sodUC = new unsigned char[len];
    env->GetByteArrayRegion (sod, 0, len, reinterpret_cast<jbyte*>(sodUC));

    Wallet& wallet = Wallet::Instance();
    wallet.initWallet();
    PKCS7Parser* pkcs7Parser = new PKCS7Parser((char*)sodUC, len);

    if(pkcs7Parser->hasError()) {
        std::cout << "Pkcs7Parser has an error";
        std::cout << "{\"success\": false, \"error\" : \"Pkcs7Parser has an error\"}";

        return env->NewStringUTF("");
    }

    Cert* pkcsCert = new Cert();
    Address randomWalletAddress = wallet.getRandomAddressFromWallet();
    pkcsCert->setX509(pkcs7Parser->getDscCertificate());

    Transaction* registerPassportTx = new Transaction();
    TxIn* pTxIn = new TxIn();
    UScript* pIScript = new UScript();
    pIScript->setScript(std::vector<unsigned char>());
    pIScript->setScriptType(SCRIPT_REGISTER_PASSPORT);
    pTxIn->setInAddress(pkcsCert->getId());
    pTxIn->setScript(*pIScript);
    pTxIn->setNonce(0);
    pTxIn->setAmount(*(new UAmount()));
    registerPassportTx->addTxIn(*pTxIn);

    TxOut* pTxOut = new TxOut();
    pTxOut->setAmount(*(new UAmount()));
    pTxOut->setScript(randomWalletAddress.getScript());
    registerPassportTx->addTxOut(*pTxOut);
    registerPassportTx->setNetwork(NET_CURRENT);

    std::vector<unsigned char> txId = TransactionHelper::getTxId(registerPassportTx);

    if(pkcs7Parser->isRSA()) {
        NtpRskSignatureRequestObject *ntpRskSignatureRequestObject = pkcs7Parser->getNtpRsk();

        // @TODO perhaps add padding to txId
        ntpRskSignatureRequestObject->setNm(ECCtools::vectorToBn(txId));

        NtpRskSignatureVerificationObject *ntpEskSignatureVerificationObject = NtpRsk::signWithNtpRsk(
                ntpRskSignatureRequestObject
        );

        CDataStream sntpRsk(SER_DISK, 1);
        sntpRsk << *ntpEskSignatureVerificationObject;

        Log(LOG_LEVEL_INFO) << "generated NtpRsk: " << sntpRsk;
        pIScript->setScript((unsigned char *) sntpRsk.data(), (uint16_t) sntpRsk.size());

        pTxIn->setScript(*pIScript);

    } else {

        NtpEskSignatureRequestObject *ntpEskSignatureRequestObject = pkcs7Parser->getNtpEsk();
        ntpEskSignatureRequestObject->setNewMessageHash(txId);

        Log(LOG_LEVEL_INFO) << "P-UID, Passport unique identifier (signed hash):: "
                            << ntpEskSignatureRequestObject->getMessageHash();

        std::string dscId = pkcsCert->getIdAsHexString();
        Log(LOG_LEVEL_INFO) << "dscId: " << dscId;
        Log(LOG_LEVEL_INFO) << "subject: "
                            << X509_NAME_oneline(X509_get_subject_name(pkcs7Parser->getDscCertificate()), 0, 0);


        NtpEskSignatureVerificationObject *ntpEskSignatureVerificationObject = NtpEsk::signWithNtpEsk(
                ntpEskSignatureRequestObject);

        CDataStream sntpEsk(SER_DISK, 1);
        sntpEsk << *ntpEskSignatureVerificationObject;
        Log(LOG_LEVEL_INFO) << "generated NtpEsk: " << sntpEsk;
        pIScript->setScript((unsigned char *) sntpEsk.data(), (uint16_t) sntpEsk.size());

        pTxIn->setScript(*pIScript);
    }
    std::vector<TxIn> pTxIns;
    pTxIns.push_back(*pTxIn);
    registerPassportTx->setTxIns(pTxIns);

    CDataStream spTx(SER_DISK, 1);
    spTx << *registerPassportTx;

    return env->NewStringUTF(base64_encode((unsigned char*)spTx.data(), spTx.size()).c_str());
}


extern "C" JNIEXPORT jstring

JNICALL
Java_network_ubic_ubic_Fragments_ReadingPassportFragment_getPassportTransaction(
        JNIEnv *env,
        jobject /* this */,
        char * readableAddress,
        int currency,
        int64_t amount,
        int64_t fee) {

    Wallet &wallet = Wallet::Instance();

    std::vector<TxOut> txOuts;

    TxOut txOut;
    std::vector<unsigned char> vectorAddress = wallet.readableAddressToVectorAddress(readableAddress);
    Address address;
    CDataStream s(SER_DISK, 1);
    s.write((char *) vectorAddress.data(), vectorAddress.size());
    s >> address;

    txOut.setScript(address.getScript());

    txOut.setAmount(uAmountAggregated);
    txOuts.push_back(txOut);

    Transaction tx;
    tx.setTxIns();
    tx.setTxOuts(txOuts);

    CDataStream s2(SER_DISK, 1);
    s2 << tx;
    std::string tx64 = base64_encode((unsigned char*)s2.str().data(), (uint32_t)s2.str().size());
    return env->NewStringUTF(tx64.c_str());
}