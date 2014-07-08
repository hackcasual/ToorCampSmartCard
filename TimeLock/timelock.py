#!/usr/bin/python

from smartcard.CardType import AnyCardType
from smartcard.CardRequest import CardRequest
from smartcard.CardConnectionObserver import ConsoleCardConnectionObserver

from smartcard.sw.ErrorCheckingChain import ErrorCheckingChain
from smartcard.sw.ISO7816_4ErrorChecker import ISO7816_4ErrorChecker
from smartcard.sw.ISO7816_8ErrorChecker import ISO7816_8ErrorChecker
from smartcard.sw.SWExceptions import SWException, WarningProcessingException

import hashlib

# request any card
cardtype = AnyCardType()
cardrequest = CardRequest( timeout=10, cardType=cardtype )
cardservice = cardrequest.waitforcard()

# our error checking chain
errorchain=[]
errorchain=[ ErrorCheckingChain( errorchain, ISO7816_8ErrorChecker() ),
             ErrorCheckingChain( errorchain, ISO7816_4ErrorChecker() ) ]
cardservice.connection.setErrorCheckingChain( errorchain )

# a console tracer
observer=ConsoleCardConnectionObserver()
cardservice.connection.addObserver( observer )

# send a few apdus; exceptions will occur upon errors
cardservice.connection.connect()

def as_list(ba):
    return [x for x in ba]

def add_length(apdu):
    apdu[4] = len(apdu) - 5
    return apdu


def apdu_ins(clazz, instruction, param1, param2):
    return bytearray([clazz, instruction, param1, param2, 0])


def hash(data):
    return add_length(as_list(apdu_ins(0, 0x11, 0, 0) + bytearray(data)))

header_hex = ("01000000" +
     "81cd02ab7e569e8bcd9317e2fe99f2de44d49ab2b8851ba4a308000000000000" +
     "e320b6c2fffc8d750423db8b1eb942ae710e951ed797f7affc8892b0f1fc122b" +
     "c7f5d74d" +
     "f2b9441a" +
     "42a14695")

header_bin = header_hex.decode('hex')

try:
    select_timelock = [0x00, 0xA4, 0x04, 0x00, 0x08,
                       0xDA, 0xAB, 0xBA, 0xAD, 0x00, 0x03, 0x00, 0x03]

    response, sw1, sw2 = cardservice.connection.transmit(select_timelock)


    for z in range(0,100):
        response, sw1, sw2 = cardservice.connection.transmit(hash(header_bin))
        response = ''.join(['%02x'%x for x in response])
        print z


    print response



except SWException, e:
    print str(e)