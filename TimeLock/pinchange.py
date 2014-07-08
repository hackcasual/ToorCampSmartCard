#!/usr/bin/python

from smartcard.CardType import AnyCardType
from smartcard.CardRequest import CardRequest
from smartcard.CardConnectionObserver import ConsoleCardConnectionObserver

from smartcard.sw.ErrorCheckingChain import ErrorCheckingChain
from smartcard.sw.ISO7816_4ErrorChecker import ISO7816_4ErrorChecker
from smartcard.sw.ISO7816_8ErrorChecker import ISO7816_8ErrorChecker
from smartcard.sw.SWExceptions import SWException, WarningProcessingException

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

def enter_pin(pin):
    return add_length(as_list(apdu_ins(0, 1, 0, 0) + bytearray(pin)))

def update_pin(pin):
    return add_length(as_list(apdu_ins(0, 2, 0, 0) + bytearray(pin)))

def encrypt(data):
    return add_length(as_list(apdu_ins(0, 0x12, 0, 0) + bytearray(data)))

def decrypt(data):
    return add_length(as_list(apdu_ins(0, 0x11, 0, 0) + bytearray(data)))


try:
    select_keywrapper = [0x00, 0xA4, 0x04, 0x00, 0x08, 0xDA, 0xAB, 0xBA, 0xAD, 0x00, 0x00, 0x00, 0x02]

    response, sw1, sw2 = cardservice.connection.transmit( select_keywrapper )

    response, sw1, sw2 = cardservice.connection.transmit( enter_pin('123456') )
    response, sw1, sw2 = cardservice.connection.transmit( update_pin('654321') )

    print response



except SWException, e:
    print str(e)