package network.ubic.ubic.BitiMRTD.Reader;
;
import android.util.Log;
import network.ubic.ubic.BitiAndroid.TagProvider;
import network.ubic.ubic.BitiMRTD.NFC.Apdu;
import network.ubic.ubic.BitiMRTD.NFC.Doc9303Apdu;
import network.ubic.ubic.BitiMRTD.Tools.Crypto;
import network.ubic.ubic.BitiMRTD.Tools.Tools;

import java.lang.ref.WeakReference;
import java.util.Arrays;

public abstract class AbstractReader
{

    protected byte[] selectBacChallenge = {
            (byte) 0x00, // CLA Class
            (byte) 0x84, // INS Instruction
            (byte) 0x00, // P1  Parameter 1
            (byte) 0x00, // P2  Parameter 2
            (byte) 0x08 // Length
    };

    protected byte[] sessionEncKey = null;
    protected byte[] sessionMacKey = null;
    protected byte[] sequenceCounter = null;
    protected Tools tools;
    protected Apdu apdu;
    protected Crypto crypto;
    protected Doc9303Apdu doc9303Apdu;
    protected BacInfo bacInfo = null;
    protected WeakReference<Object> progressListener;
    protected int readingAttempsFails = 0;
    protected int maxBlockSize = 215;
    protected byte mutualAuthLe = (byte)0x28;

    public void incrementSequenceCounter()
    {
        this.sequenceCounter = this.tools.incrementBytesArray(this.sequenceCounter);
    }

    public AbstractReader()
    {
        this.tools = new Tools();
        this.apdu = new Apdu();
        this.crypto = new Crypto();
        this.doc9303Apdu = new Doc9303Apdu();

        try {
            byte[] result = TagProvider.transceive(this.doc9303Apdu.getAID());
            System.out.println("Select AID : ".concat(this.tools.bytesToString(result)));
        }
        catch(Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    public void setProgressListener(WeakReference<Object> progressListener)
    {
        this.progressListener = progressListener;
    }

    public void setMaxBlockSize(int maxBlockSize)
    {
        this.maxBlockSize = maxBlockSize;
    }

    public void setApduWithLe(boolean value)
    {
        this.apdu.setApduWithLe(value);
    }

    public void setMutualAuthLe(byte mutualAuthLe)
    {
        this.mutualAuthLe = mutualAuthLe;
    }

    public abstract byte[] encrypt(byte[] key, byte[] payload);

    public abstract byte[] decrypt(byte[] key, byte[] payload);

    public abstract byte[] calculateMac(byte[] macKey, byte[] message, boolean addPad);

    public abstract byte[] calculateMac(byte[] macKey, byte[] message);

    public abstract byte[] calculateSequenceCounter(byte[] payload);

    public abstract byte[] padData(byte[] data);

    /**
     * return encryption key
     * @return
     */
    public abstract byte[] calculateEncryptionKey(byte[] seed);

    /**
     * Gets the 8 bytes BAC challenge from passport
     * @return
     */
    public byte[] getBacChallenge()
    {
        try {

            byte[] result = TagProvider.transceive(selectBacChallenge);
            if(result == null || result.length < 10) {
                System.out.println("Expected at least 10 bytes response, got : ".concat(this.tools.bytesToString(result)));
                return null;
            }

            if(result[8] != (byte)0x90 || result[9] != (byte)0x00) {
                System.out.println("Expected SWR 1-2 to be 0x9000");
                //return null;
            }
            System.out.println("Got challenge : ".concat(this.tools.bytesToString(result)));
            return Arrays.copyOfRange(result, 0, 8);
        }
        catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            return null;
        }
    }

    public boolean initSession()
    {
        return this.initSession(20000); // + 20 seconds
    }

    /**
     * Step D.3
     * @return boolean
     */
    public boolean initSession(int mutualAuthTimeout)
    {

        if(this.bacInfo == null) {
            System.out.println("bac info needs to be set");
            return false;
        }

        byte[] challenge = this.getBacChallenge();
        byte[] random8 = this.crypto.generateRandomBytes(8);
        byte[] kIFD = this.crypto.generateRandomBytes(16);
        byte[] s = this.tools.concatByteArrays(random8, challenge);
        s = this.tools.concatByteArrays(s, kIFD);
        System.out.println("S : ".concat(this.tools.bytesToString(s)));

        byte[] seed = this.crypto.calculateSeed(
                this.bacInfo.getPassportNbr(),
                this.bacInfo.getDateOfBirth(),
                this.bacInfo.getDateOfExpiry()
        );

        byte[] encryptionKey = this.calculateEncryptionKey(seed);
        byte[] macKey = this.crypto.calculateMacKey(seed);
        byte[] eifd = this.encrypt(encryptionKey, s);
        System.out.println("eifd : ".concat(this.tools.bytesToString(eifd)));
        byte[] desMac = this.calculateMac(macKey, eifd);
        byte[] cmdData = this.tools.concatByteArrays(eifd, desMac);

        byte[] cmd = this.apdu.buildApduCommand(
                (byte) 0x00,
                (byte) 0x82,
                (byte) 0x00,
                (byte) 0x00,
                cmdData,
                this.mutualAuthLe
        );

        long before = System.currentTimeMillis();
        int previousTimeout = TagProvider.getTimeout();
        TagProvider.setTimeout(previousTimeout + mutualAuthTimeout);
        try {
            System.out.println("Max tranceivalable length: ".concat(String.valueOf(TagProvider.getMaxTransceiveLength())));

            byte[] result = TagProvider.transceive(cmd);
            System.out.println("Succeeded in: ".concat(String.valueOf((System.currentTimeMillis() - before))));
            System.out.println("SESSION RESULT: ".concat(this.tools.bytesToString(result)));

            TagProvider.setTimeout(previousTimeout);

            if(result == null || result.length < 42) {

                //@TODO retry with le: 0xff, 0x00 ...

                System.out.println("BAC FAILED!");
                return false;
            }

            byte[] payload = Arrays.copyOfRange(result, 0, 32);
            byte[] decryptedPayload = this.decrypt(encryptionKey, payload);

            System.out.println("Decrypted payload: ".concat(this.tools.bytesToString(decryptedPayload)));

            byte[] sessionSeed = this.tools.doXor(kIFD, Arrays.copyOfRange(decryptedPayload, 16, 32));

            System.out.println("Session seed: ".concat(this.tools.bytesToString(sessionSeed)));

            this.sessionEncKey = this.calculateEncryptionKey(sessionSeed);
            this.sessionMacKey = this.crypto.calculateMacKey(sessionSeed);
            this.sequenceCounter = this.calculateSequenceCounter(decryptedPayload);

            return true;
        }
        catch (Exception e) {
            System.out.println("Failed in: ".concat(String.valueOf((System.currentTimeMillis() - before))));
            if(e.getMessage() != null) {
                System.out.println(e.getMessage());
            }
            System.out.println(Log.getStackTraceString(e));
            return false;
        }
    }

    public void setBacInfo(BacInfo bacInfo)
    {
        this.bacInfo = bacInfo;
    }

    private boolean selectFile(byte[] file)
    {
        if(this.sessionEncKey == null || this.sessionMacKey == null) {
            System.out.println("Session key is empty");
            return false;
        }

        // Step 1. select FILE
        System.out.println("Step 1, select FILE");

        byte[] cmdHeader = {(byte)0x0C, (byte)0xA4, (byte)0x02, (byte)0x0C};

        cmdHeader = this.padData(cmdHeader);

        byte[] paddedQuery = this.padData(file);

        System.out.println("paddedQuery : ".concat(this.tools.bytesToString(paddedQuery)));

        byte[] encryptedQuery = this.encrypt(this.sessionEncKey, paddedQuery);
        byte[] do87 = this.doc9303Apdu.buildDO87(encryptedQuery);

        System.out.println("do87 : ".concat(this.tools.bytesToString(do87)));

        byte[] m = this.tools.concatByteArrays(cmdHeader, do87);

        this.incrementSequenceCounter();

        byte[] n = this.padData(
                this.tools.concatByteArrays(
                        this.sequenceCounter,
                        m
                )
        );


        byte[] cc = this.calculateMac(this.sessionMacKey, n, false);
        byte[] do8e = this.doc9303Apdu.buildDO8E(cc);
        byte[] protectedApdu = this.apdu.buildApduCommand(
                (byte) 0x0C,
                (byte) 0xA4,
                (byte) 0x02,
                (byte) 0x0C,
                this.tools.concatByteArrays(do87, do8e),
                (byte) 0x00
        );

        try {

            byte[] rapdu = TagProvider.transceive(protectedApdu);
            System.out.println("rapdu : ".concat(this.tools.bytesToString(rapdu)));
            //@TODO: verify rapdu

            this.incrementSequenceCounter();

            return true;

        }
        catch (Exception e) {
            if(e.getMessage() != null) {
                System.out.println(e.getMessage());
            }
            System.out.println(Log.getStackTraceString(e));
        }

        return false;
    }

    public byte[] readFile(byte[] file)
    {
        if(this.progressListener != null && this.progressListener.get() instanceof ProgressListenerInterface) {
            if (((ProgressListenerInterface) this.progressListener.get()).isCanceled()) {
                return null;
            }
        }
        return this.readFileStep1(file);
    }

    private byte[] readFileStep1(byte[] file)
    {
        if(this.sessionEncKey == null || this.sessionMacKey == null) {
            System.out.println("Session key is empty");
            return null;
        }

        if(this.selectFile(file)) {
            return this.readFileStep2(file);
        }

        return null;
    }

    private byte[] readFileStep2(byte[] file)
    {
        // Step 2. read first 4 bytes of FILE
        System.out.println("Step 2, read first 4 bytes of FILE");

        byte[] cmdHeader = {(byte)0x0C, (byte)0xB0, (byte)0x00, (byte)0x00};

        cmdHeader = this.padData(cmdHeader);

        byte[] do97 = this.doc9303Apdu.buildDO97(4);

        System.out.println("do97 : ".concat(this.tools.bytesToString(do97)));

        byte[] m = this.tools.concatByteArrays(cmdHeader, do97);

        this.incrementSequenceCounter();

        byte[] n = this.tools.concatByteArrays(this.sequenceCounter, m);
        n = this.padData(n);

        byte[] cc = this.calculateMac(this.sessionMacKey, n, false);
        byte[] do8e = this.doc9303Apdu.buildDO8E(cc);
        byte[] protectedApdu = this.apdu.buildApduCommand(
                (byte) 0x0C,
                (byte) 0xB0,
                (byte) 0x00,
                (byte) 0x00,
                this.tools.concatByteArrays(
                        do97,
                        do8e
                ),
                (byte) 0x00
        );

        try {
            byte[] rapdu = TagProvider.transceive(protectedApdu);

            this.incrementSequenceCounter();

            if(rapdu != null && rapdu.length >= 5) {
                if(rapdu[0] != (byte) 0x87 || rapdu[2] != (byte) 0x01) {
                    System.out.println("Expected DO87 response");
                    return null;
                }

                int responseLength = (int)rapdu[1];

                System.out.println("Response length : ".concat(String.valueOf(responseLength)));

                byte[] encryptedPayload = Arrays.copyOfRange(rapdu, 3, 2 + responseLength);

                byte[] decryptedPayload = this.decrypt(this.sessionEncKey, encryptedPayload);

                byte[] unpadedResult = this.tools.unpadData(decryptedPayload);

                byte[] fileContent = this.readFileStep3(file, unpadedResult);

                if(fileContent == null) {
                    return null;
                }

                //return fileContent;
                return  this.tools.concatByteArrays(unpadedResult, fileContent);
            }
            else {
                System.out.println("Response size too small");
                return null;
            }

        }
        catch(Exception e) {
            if(e.getMessage() != null) {
                System.out.println(e.getMessage());
            }
            System.out.println(Log.getStackTraceString(e));
        }

        return null;
    }

    private byte[] readFileStep3(byte[] file, byte[] fileHeader)
    {
        // Step 3. read entire FILE
        System.out.println("Step 3, read entire FILE");

        int cursor = 4;
        int fileLength = this.tools.getLengthFromFileHeader(fileHeader);

        System.out.println("File length : ".concat(String.valueOf(fileLength)));

        byte[] fileContent = {};

        if(fileLength < 128) {
            fileLength += 2;
        }
        else if(fileLength < 256) {
            fileLength += 3;
        }
        else if(fileLength < 65536) {
            fileLength += 4;
        }

        while(cursor < fileLength) {
            int le = this.maxBlockSize;
            if((cursor + le) > fileLength) {
                le = fileLength - cursor;
            }

            byte[] fileChunk = this.readFileStep3(file, cursor, le);

            if(fileChunk == null) {

                if(this.progressListener != null && this.progressListener.get() instanceof ProgressListenerInterface) {
                    if (((ProgressListenerInterface) this.progressListener.get()).isCanceled()) {
                        return null;
                    }
                }

                if(this.readingAttempsFails >= 1) {
                    return null;
                }

                System.out.println("Seems that we lost the connection, trying to reinitialize the session");
                this.initSession(TagProvider.getTimeout());
                this.selectFile(file);
                this.readingAttempsFails++;
            }
            else {
                cursor += le;

                fileContent = this.tools.concatByteArrays(
                        fileContent,
                        fileChunk
                );

                this.readingAttempsFails = 0;
            }

            if(this.progressListener != null && this.progressListener.get() instanceof ProgressListenerInterface) {
                if(((ProgressListenerInterface)this.progressListener.get()).isCanceled()) {
                    return null;
                }
                ((ProgressListenerInterface)this.progressListener.get()).updateProgress(Math.round(100*cursor/fileLength));
            }
        }

        return fileContent;
    }

    private byte[] readFileStep3(byte[] file, int cursor, int le)
    {
        // Step 3. read entire FILE
        System.out.println("Step 3, read entire FILE, cursor at : ".concat(String.valueOf(cursor)));
        System.out.println("le : ".concat(String.valueOf(le)));

        // @TODO this code is similar to the one in STEP 2 and should be placed in a same method

        byte[] bytesCursor =  this.tools.calculate2bytesInt(cursor);
        byte[] cmdHeader = {(byte)0x0C, (byte)0xB0, bytesCursor[0], bytesCursor[1]};

        cmdHeader = this.padData(cmdHeader);

        byte[] do97 = this.doc9303Apdu.buildDO97(le);
        byte[] m = this.tools.concatByteArrays(cmdHeader, do97);

        this.incrementSequenceCounter();

        byte[] n = this.tools.concatByteArrays(this.sequenceCounter, m);
        n = this.padData(n);

        byte[] cc = this.calculateMac(this.sessionMacKey, n, false);
        byte[] do8e = this.doc9303Apdu.buildDO8E(cc);
        byte[] protectedApdu = this.apdu.buildApduCommand(
                (byte) 0x0C,
                (byte) 0xB0,
                bytesCursor[0],
                bytesCursor[1],
                this.tools.concatByteArrays(
                        do97,
                        do8e
                ),
                (byte) 0x00
        );

        try {
            byte[] rapdu = TagProvider.transceive(protectedApdu);
            this.incrementSequenceCounter();
            if(rapdu == null || rapdu.length < 4) {
                System.out.println("Error, expected at least 4 bytes, got : ".concat(this.tools.bytesToString(rapdu)));
                return null;
            }

            int responseLength = this.tools.getLengthFromFileHeader(Arrays.copyOfRange(rapdu, 0, 4));

            System.out.println("Response length : ".concat(String.valueOf(responseLength)));

            int startToReadFrom = 3;

            if(responseLength <= 0x7F) {
                startToReadFrom = 3;
            } else if(responseLength <= 0xFF) {
                startToReadFrom = 4;
            }
            else if(responseLength <= 0xFFFF) {
                startToReadFrom = 5;
            }
            else {
                System.out.println("Too big");
                return null;
            }

            byte[] encryptedPayload = Arrays.copyOfRange(rapdu, startToReadFrom, startToReadFrom - 1 + responseLength);
            byte[] decryptedPayload = this.decrypt(this.sessionEncKey, encryptedPayload);
            byte[] unpadedResult = this.tools.unpadData(decryptedPayload);

            return unpadedResult;

        }
        catch(Exception e) {
            if(e.getMessage() != null) {
                System.out.println(e.getMessage());
            }
            System.out.println(Log.getStackTraceString(e));

            if(TagProvider.getTagIsLost())
            {
                if(this.progressListener != null && this.progressListener.get() instanceof ProgressListenerInterface) {
                    ((ProgressListenerInterface)this.progressListener.get()).cancel();
                }
            }
        }

        return null;
    }

}
