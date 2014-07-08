package io.hackcasual;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

public class TimeLock extends Applet {

	private static final byte _0 = 0;
	private MessageDigest md;

	public void process(APDU apdu) throws ISOException {
		byte[] buf = apdu.getBuffer();
		
		switch(buf[ISO7816.OFFSET_INS]) {
		// Called on AID select
		case (byte)0xA4:
			break;
		// Decryption
		case (byte)0x11:
			
			md.doFinal(buf, ISO7816.OFFSET_CDATA, buf[ISO7816.OFFSET_LC], buf, ISO7816.OFFSET_CDATA);
			md.doFinal(buf, ISO7816.OFFSET_CDATA, (byte)MessageDigest.LENGTH_SHA_256, buf, ISO7816.OFFSET_CDATA);
			md.doFinal(buf, ISO7816.OFFSET_CDATA, (byte)MessageDigest.LENGTH_SHA_256, buf, ISO7816.OFFSET_CDATA);
			md.doFinal(buf, ISO7816.OFFSET_CDATA, (byte)MessageDigest.LENGTH_SHA_256, buf, ISO7816.OFFSET_CDATA);						
			apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)MessageDigest.LENGTH_SHA_256);
			break;
		}
	}
	
	public static void install(byte[] buffer, short offset, byte length)
	{
		TimeLock app = new TimeLock();
		
		app.register();
		app.md = MessageDigest.getInstance(MessageDigest.ALG_SHA_256, false);
	}

}
