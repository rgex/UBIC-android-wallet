
#ifndef TX_FS_H
#define TX_FS_H

#include <vector>
#include "../streams.h"
#include "../Tools/Log.h"
#include "../ChainParams.h"

class FS {
public:
    static std::vector<unsigned char> concatPaths(std::vector<unsigned char> path1, std::vector<unsigned char> path2);
    static std::vector<unsigned char> concatPaths(std::vector<unsigned char> path1, const char* path2);
    static std::vector<unsigned char> concatPaths(const char* path1, const char* path2);
    static void charPathFromVectorPath(char* pData, std::vector<unsigned char> vectorPath);
};


#endif //TX_FS_H
