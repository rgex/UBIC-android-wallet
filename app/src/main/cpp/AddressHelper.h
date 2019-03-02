
#ifndef TX_ADDRESSHELPER_H
#define TX_ADDRESSHELPER_H

#include "UAmount.h"
#include "Crypto/Hash160.h"

class AddressHelper {
public:

    static std::vector<unsigned char> addressLinkFromScript(UScript script) {
        //if it is already an address
        if(script.getScriptType() == SCRIPT_LINK) {
            return script.getScript();
        }
        CDataStream s(SER_DISK, 1);
        s << script;
        std::vector<unsigned char> r(s.data(), s.data() + s.size());

        return Hash160::hash160(r);
    }

};

#endif //TX_ADDRESSHELPER_H
