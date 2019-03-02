
#include "FS.h"
#include <regex>

std::vector<unsigned char> FS::concatPaths(std::vector<unsigned char> path1, std::vector<unsigned char> path2) {
    path1.insert(path1.end(), path2.begin(), path2.end());
    return path1;
}

std::vector<unsigned char> FS::concatPaths(std::vector<unsigned char> path1, const char* path2) {
    std::vector<unsigned char> np(path2, path2 + strlen(path2));
    path1.insert(path1.end(), np.begin(), np.end());
    return path1;
}

std::vector<unsigned char> FS::concatPaths(const char* path1, const char* path2) {
    std::vector<unsigned char> bp(path1, path1 + strlen(path1));
    std::vector<unsigned char> np(path2, path2 + strlen(path2));

    return FS::concatPaths(bp, np);
}

void FS::charPathFromVectorPath(char* pData, std::vector<unsigned char> vectorPath) {
    memcpy(pData, (char*)vectorPath.data(), vectorPath.size());
    memcpy(pData + vectorPath.size(), "\0", 1);
}
