#include <jni.h>
#include <string>
#include <iostream>
#include "Wallet.h"

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