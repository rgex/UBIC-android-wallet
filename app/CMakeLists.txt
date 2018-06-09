# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp
             src/main/cpp/NtpRsk/NtpRsk.cpp
             src/main/cpp/NtpRsk/NtpRsk.h
             src/main/cpp/NtpRsk/NtpRskSignatureRequestObject.h
             src/main/cpp/NtpRsk/NtpRskSignatureVerificationObject.h

             src/main/cpp/NtpEsk/NtpEsk.cpp
             src/main/cpp/NtpEsk/NtpEsk.h
             src/main/cpp/NtpEsk/NtpEskSignatureRequestObject.h
             src/main/cpp/NtpEsk/NtpEskSignatureVerificationObject.h

            src/main/cpp/serialize.h
            src/main/cpp/streams.h
            src/main/cpp/Wallet.cpp
            src/main/cpp/Wallet.h

            src/main/cpp/Base58.cpp
            src/main/cpp/Base58.h
            src/main/cpp/Base64.cpp
            src/main/cpp/Base64.h

            src/main/cpp/Config.cpp
            src/main/cpp/Config.h

            src/main/cpp/ChainParams.h

            src/main/cpp/Address.cpp
            src/main/cpp/Address.h
            src/main/cpp/AddressHelper.h

            src/main/cpp/UAmount.h
            src/main/cpp/UScript.h

            src/main/cpp/prevector.h

            src/main/cpp/FS/FS.cpp
            src/main/cpp/FS/FS.h

            src/main/cpp/Tools/Hexdump.cpp
            src/main/cpp/Tools/Hexdump.h
            src/main/cpp/Tools/Log.h
            src/main/cpp/Tools/Log.cpp

            src/main/cpp/Transaction/Transaction.cpp
            src/main/cpp/Transaction/Transaction.h

            src/main/cpp/Transaction/TransactionHelper.cpp
            src/main/cpp/Transaction/TransactionHelper.h
            src/main/cpp/Transaction/TxIn.cpp
            src/main/cpp/Transaction/TxIn.h
            src/main/cpp/Transaction/TxOut.cpp
            src/main/cpp/Transaction/TxOut.h

            src/main/cpp/Crypto/CreateSignature.cpp
            src/main/cpp/Crypto/CreateSignature.h
            src/main/cpp/Crypto/ECCtools.cpp
            src/main/cpp/Crypto/ECCtools.h
            src/main/cpp/Crypto/Hash160.cpp
            src/main/cpp/Crypto/Hash160.h
            src/main/cpp/Crypto/Hash256.cpp
            src/main/cpp/Crypto/Hash256.h
            src/main/cpp/Crypto/PassportCrypto.cpp
            src/main/cpp/Crypto/PassportCrypto.h


            src/main/cpp/Crypto/Ripemd160.cpp
            src/main/cpp/Crypto/Ripemd160.h
            src/main/cpp/Crypto/Sha256.cpp
            src/main/cpp/Crypto/Sha256.h
            src/main/cpp/Crypto/VerifySignature.cpp
            src/main/cpp/Crypto/VerifySignature.h

            src/main/cpp/support/cleanse.cpp
            src/main/cpp/support/cleanse.h

            src/main/cpp/support/allocators/zeroafterfree.h

            src/main/cpp/CertStore/Cert.h
            src/main/cpp/CertStore/Cert.cpp

            src/main/cpp/PassportReader/PKCS7/PKCS7Parser.h
            src/main/cpp/PassportReader/PKCS7/PKCS7Parser.cpp

            src/main/cpp/Countries/Currency.h
            )

set(distribution_DIR ${CMAKE_SOURCE_DIR}/../distribution)

include_directories(${distribution_DIR}/openssl/x86/include/)

target_include_directories(native-lib PRIVATE ${distribution_DIR}/openssl/${ANDROID_ABI}/include)

add_library(libcrypto STATIC IMPORTED)

set_target_properties(libcrypto PROPERTIES IMPORTED_LOCATION ${distribution_DIR}/openssl/${ANDROID_ABI}/lib/libcrypto.a)


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib
                       libcrypto

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )