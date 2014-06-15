package io.hackcasual;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

public class KeyWrapper extends Applet {
	private static byte[] PIN_DEFAULT = { 0x31, 0x32, 0x33, 0x34, 0x35, 0x36 };

	private static final byte _0 = 0;

	private OwnerPIN pin;
	private Key key;

	public void process(APDU apdu) throws ISOException {
		byte[] buf = apdu.getBuffer();
		byte[] encBuf = new byte[16];
		
		switch(buf[ISO7816.OFFSET_INS]) {
		// Called on AID select
		case (byte)0xA4:
			break;
		// Decryption
		case (byte)0x11:
			if (pin.isValidated()) {

				Cipher ciph = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
				ciph.init(key, Cipher.MODE_DECRYPT);

				ciph.doFinal(buf, ISO7816.OFFSET_CDATA, buf[ISO7816.OFFSET_LC], encBuf, _0);
				Util.arrayCopy(encBuf, _0, buf, ISO7816.OFFSET_CDATA, (short)16);

				apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)16);
				break;
			} else {
				ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED );
			}
		// Encryption
		case (byte)0x12:
			if (pin.isValidated()) {

				Cipher ciph = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
				ciph.init(key, Cipher.MODE_ENCRYPT);

				ciph.doFinal(buf, ISO7816.OFFSET_CDATA, buf[ISO7816.OFFSET_LC], encBuf, _0);
				Util.arrayCopy(encBuf, _0, buf, ISO7816.OFFSET_CDATA, (short)16);

				apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)16);
				break;
			} else {
				ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED );
			}
		// Pin Unlock
		case (byte)0x01:
			if (!pin.check(buf, ISO7816.OFFSET_CDATA, buf[ISO7816.OFFSET_LC])) {
				ISOException.throwIt((short) (0x63C0 | pin.getTriesRemaining()));
			}
			break;

        // Pin Change
        case (byte)0x02:
            if (pin.isValidated()) {
                pin.update(buf, ISO7816.OFFSET_CDATA, buf[ISO7816.OFFSET_LC]);
            } else {
                ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED );
            }
            break;

		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}

	}
	
	public static void install(byte[] buffer, short offset, byte length)
	{
		KeyWrapper app = new KeyWrapper();
		
		app.register();
	}

	public KeyWrapper() {
		pin = new OwnerPIN((byte)3, (byte)16);

		pin.update(PIN_DEFAULT, _0, (byte)PIN_DEFAULT.length);

		key = KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);

		byte[] keyBuf = new byte[16];

		RandomData.getInstance(RandomData.ALG_SECURE_RANDOM).generateData(keyBuf, _0, (byte)16);

		((AESKey)key).setKey(keyBuf, _0);
	}
}
