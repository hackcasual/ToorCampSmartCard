#!/usr/bin/python

import time

from smartcard.CardMonitoring import CardMonitor, CardObserver
from smartcard.util import *

# a simple card observer that prints inserted/removed cards
class printobserver( CardObserver ):
    """A simple card observer that is notified
    when cards are inserted/removed from the system and
    prints the list of cards
    """
    def update( self, observable, (addedcards, removedcards) ):
        for card in addedcards:
            print "+Inserted: ", toHexString( card.atr )
        for card in removedcards:
            print "-Removed: ", toHexString( card.atr )

try:
    print "Insert or remove a smartcard in the system."
    print "This program will exit in 10 seconds"
    print ""
    cardmonitor = CardMonitor()
    cardobserver = printobserver()
    cardmonitor.addObserver( cardobserver )
    time.sleep(10)
    
except Exception as e:
    print e