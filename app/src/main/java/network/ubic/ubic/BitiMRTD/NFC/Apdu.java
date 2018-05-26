package network.ubic.ubic.BitiMRTD.NFC;

import network.ubic.ubic.BitiMRTD.Tools.Tools;

public class Apdu {

    protected Tools tools;
    protected boolean apduWithLe = true;

    public Apdu()
    {
        this.tools = new Tools();
    }

    public byte[] buildApduCommand(byte cla, byte instruction, byte parameter1, byte parameter2, byte[] command, byte minExpectedLength)
    {
        byte[] aCla = {cla};
        byte[] aInstruction = {instruction};
        byte[] aParameter1 = {parameter1};
        byte[] aParameter2 = {parameter2};
        byte[] aCommandLength = {(byte)command.length};
        byte[] aminExpectedLength = {minExpectedLength};
        byte[] apduCommand = this.tools.concatByteArrays(aCla, aInstruction);

        apduCommand = this.tools.concatByteArrays(apduCommand, aParameter1);
        apduCommand = this.tools.concatByteArrays(apduCommand, aParameter2);
        apduCommand = this.tools.concatByteArrays(apduCommand, aCommandLength);
        apduCommand = this.tools.concatByteArrays(apduCommand, command);

        if(this.apduWithLe) {
            apduCommand = this.tools.concatByteArrays(apduCommand, aminExpectedLength);
        }

        System.out.println("APDU command : ".concat(this.tools.bytesToString(apduCommand)));

        return apduCommand;
    }

    public byte[] buildApduCommand(byte cla, byte instruction, byte parameter1, byte parameter2, byte[] command)
    {
        byte[] aCla = {cla};
        byte[] aInstruction = {instruction};
        byte[] aParameter1 = {parameter1};
        byte[] aParameter2 = {parameter2};
        byte[] aCommandLength = {(byte)command.length};
        byte[] apduCommand = this.tools.concatByteArrays(aCla, aInstruction);

        apduCommand = this.tools.concatByteArrays(apduCommand, aParameter1);
        apduCommand = this.tools.concatByteArrays(apduCommand, aParameter2);
        apduCommand = this.tools.concatByteArrays(apduCommand, aCommandLength);
        apduCommand = this.tools.concatByteArrays(apduCommand, command);

        System.out.println("APDU command : ".concat(this.tools.bytesToString(apduCommand)));

        return apduCommand;
    }

    public void setApduWithLe(boolean value)
    {
        this.apduWithLe = value;
    }

}
