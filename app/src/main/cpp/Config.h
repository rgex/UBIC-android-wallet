
#ifndef TX_CONFIG_H
#define TX_CONFIG_H

#include <string>

class Config {
public:
    static Config& Instance() {
        static Config instance;
        return instance;
    }

    uint32_t getNumberOfAdresses();
};


#endif //TX_CONFIG_H
