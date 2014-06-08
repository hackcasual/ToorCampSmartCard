package io.hackcasual;

import javacard.framework.*;
import javacard.security.CryptoException;
import javacard.security.MessageDigest;

public class HelloWorld extends Applet {
	private final static byte[] hello = { 0x48, 0x65, 0x6c, 0x6c, 0x6f } ;
	private final static byte[] empty = { 0x00, 0x00, 0x00, 0x00, 0x00 } ;
	
	public void process(APDU apdu) throws ISOException {
		byte[] buf = apdu.getBuffer();
		byte[] mdbuf = new byte[MessageDigest.LENGTH_SHA_512];
		
		
		
		switch(buf[ISO7816.OFFSET_INS]) {
		case (byte)0xA4:
			Util.arrayCopy(hello, (short)0, buf, ISO7816.OFFSET_CDATA, (short)5);
			apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)5);
			break;
		default:
			Util.arrayCopy(empty, (short)0, buf, ISO7816.OFFSET_CDATA, (short)5);
			buf[ISO7816.OFFSET_CDATA] = buf[ISO7816.OFFSET_INS];
			try {
				MessageDigest md = MessageDigest.getInstance(MessageDigest.ALG_SHA_256, false);
				md.doFinal(hello, (short)0, (short)5, buf, ISO7816.OFFSET_CDATA);
			} catch (RuntimeException cx) {
				//buf[ISO7816.OFFSET_CDATA + 1] = (byte)(cx.getReason() >> 8);
				//buf[ISO7816.OFFSET_CDATA + 2] = (byte)(cx.getReason());
				buf[ISO7816.OFFSET_CDATA + 1] = (byte)0x56;
				if (cx instanceof NullPointerException) {
					buf[ISO7816.OFFSET_CDATA + 2] = (byte)0x78;
				}
				if (cx instanceof CryptoException) {
					buf[ISO7816.OFFSET_CDATA + 2] = (byte)0x90;
				}				
			}
			apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)5);
			break;//ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}

	}
	
	public static void install(byte[] buffer, short offset, byte length)
	{
		HelloWorld app = new HelloWorld();
		
		app.register();
	}
}
