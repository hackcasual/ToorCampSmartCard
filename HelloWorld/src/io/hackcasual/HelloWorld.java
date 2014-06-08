package io.hackcasual;

import javacard.framework.*;
import javacard.security.CryptoException;
import javacard.security.MessageDigest;

public class HelloWorld extends Applet {
	private final static byte[] hello = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd', '!' } ;
	
	public void process(APDU apdu) throws ISOException {
		byte[] buf = apdu.getBuffer();
		byte[] mdbuf = new byte[MessageDigest.LENGTH_SHA_512];
		
		
		
		switch(buf[ISO7816.OFFSET_INS]) {
		// Called on AID select
		case (byte)0xA4:
			break;
		// Our function
		case (byte)0x01:
			MessageDigest md = MessageDigest.getInstance(MessageDigest.ALG_SHA_256, false);
			md.doFinal(hello, (short)0, (short)hello.length, buf, ISO7816.OFFSET_CDATA);
			apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)MessageDigest.LENGTH_SHA_256);
			break;
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}

	}
	
	public static void install(byte[] buffer, short offset, byte length)
	{
		HelloWorld app = new HelloWorld();
		
		app.register();
	}
}
