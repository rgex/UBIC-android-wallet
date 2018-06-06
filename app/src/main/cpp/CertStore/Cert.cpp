#include <cstring>
#include <sstream>
#include <openssl/pem.h>
#include <openssl/err.h>
#include "Cert.h"
#include "../Crypto/Hash160.h"
#include "../Tools/Hexdump.h"
#include "../FS/FS.h"

bool Cert::calculateId() {
    if(this->id.size() == 0) {

        if(this->getX509() == nullptr) {
            Log(LOG_LEVEL_ERROR) << "X509 certificate is nullptr ";
            return false;
        }

        unsigned char fingerprint[32];
        unsigned int len = 32;

        X509_digest(this->getX509(), EVP_sha256(), fingerprint, &len);

        this->id = Hash160::hash160(std::vector<unsigned char>(fingerprint, fingerprint + len));

    }
    return true;
}

std::vector<unsigned char> Cert::getId() {
    if(this->calculateId()) {
        return this->id;
    }
    return std::vector<unsigned char>();
}

EVP_PKEY* Cert::getPubKey() {
    return X509_get_pubkey(this->x509);
}

std::string Cert::getIdAsHexString() {
    this->calculateId();

    std::string s = Hexdump::vectorToHexString(this->id);

    return s;
}

void Cert::setId(std::vector<unsigned char> id) {
    Cert::id = id;
}

uint8_t Cert::getCurrencyId() {
    return currencyId;
}

void Cert::setCurrencyId(uint8_t currencyId) {
    Cert::currencyId = currencyId;
}

std::vector<std::pair<uint32_t, bool> > Cert::getStatusList() {
    return statusList;
}

void Cert::setStatusList(std::vector<std::pair<uint32_t, bool> > statusList) {
    Cert::statusList = statusList;
}

void Cert::appendStatusList(std::pair<uint32_t, bool> newStatus) {
    this->statusList.push_back(newStatus);
}

bool Cert::isCertAtive() {
    if(this->statusList.empty()) {
        return false;
    }
    return this->statusList.back().second;
}

bool Cert::isMature(uint32_t blockHeight) {

    Log(LOG_LEVEL_INFO) << "maturation for blockheight:" << blockHeight;

    if(blockHeight <= CSCA_MATURATION_SUSPENSIONTIME_IN_BLOCKS || this->statusList.back().first <= CSCA_MATURATION_SUSPENSIONTIME_IN_BLOCKS) {
        return true;
    }

    if(this->statusList.empty()) {
        Log(LOG_LEVEL_INFO) << "is not mature because status list is empty";
        return false;
    }

    if(!this->statusList.back().second) {
        Log(LOG_LEVEL_INFO) << "is not mature because it is deactivated" << this->statusList.end()->second;
        return false;
    }

    if((this->statusList.back().first + CSCA_MATURATION_TIME_IN_BLOCKS) <= blockHeight) {
        return true;
    }

    Log(LOG_LEVEL_INFO) << "Height " << blockHeight << " but required " << this->statusList.back().first + CSCA_MATURATION_TIME_IN_BLOCKS;

    return false;
}

uint32_t Cert::getNonce() {
    return nonce;
}

void Cert::setNonce(uint32_t nonce) {
    Cert::nonce = nonce;
}

