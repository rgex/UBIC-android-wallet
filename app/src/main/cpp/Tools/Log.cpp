#include "Log.h"

Log::Log(uint8_t level) {

    char cPath[1024];

    this->logLevel = level;
    switch (this->logLevel) {
        case LOG_LEVEL_ERROR: {
            std::cout << "[ERROR] ";
            break;
        }
        case LOG_LEVEL_INFO: {
            std::cout << "[INFO] ";
            break;
        }
        case LOG_LEVEL_CRITICAL_ERROR: {
            std::cout << "[CRITICAL] ";
            break;
        }
        case LOG_LEVEL_NOTICE: {
            std::cout << "[NOTICE] ";
            break;
        }
        case LOG_LEVEL_WARNING: {
            std::cout << "[WARNING] ";
            break;
        }
    }
}

Log& Log::operator<<(CDataStream obj)
{
    std::cout << Hexdump::vectorToHexString(std::vector<unsigned char>(obj.data(), obj.data() + obj.size()));
    return *this;
}

Log& Log::operator<<(std::vector<unsigned char> obj)
{
    std::cout << Hexdump::vectorToHexString(obj);
    return *this;
}

Log& Log::operator<<(const char* obj)
{
    if(obj == nullptr) {
        std::cout << "(NULL)";
        return *this;
    }
    std::cout << obj;
    return *this;
}

Log& Log::operator<<(const unsigned char* obj)
{
    if(obj == nullptr) {
        std::cout << "(NULL)";
        return *this;
    }
    std::cout << obj;
    return *this;
}


Log& Log::operator<<(std::string obj)
{
    std::cout << obj;
    return *this;
}

Log& Log::operator<<(uint32_t obj)
{
    std::cout << obj;
    return *this;
}

Log& Log::operator<<(uint64_t obj)
{
    std::cout << obj;
    return *this;
}

Log& Log::operator<<(float obj)
{
    std::cout << obj;
    return *this;
}

Log& Log::operator<<(int obj)
{
    std::cout << obj;
    return *this;
}

Log& Log::operator<<(bool obj)
{
    if(obj) {
        std::cout << "true";
    } else {
        std::cout << "false";
    }
    return *this;
}

Log& Log::operator<<(UAmount obj)
{
    for (std::map<uint8_t, CAmount>::const_iterator it(obj.map.begin()); it != obj.map.end(); ++it) {
        std::cout << "[" << (int)it->first << ":" << it->second << "]";
    }
    return *this;
}

Log& Log::operator<<(UAmount32 obj)
{
    for (std::map<uint8_t, CAmount32>::const_iterator it(obj.map.begin()); it != obj.map.end(); ++it) {
        std::cout << "[" << (int)it->first << ":" << it->second << "]";
    }
    return *this;
}
