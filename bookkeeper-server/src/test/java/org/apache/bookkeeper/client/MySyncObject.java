package org.apache.bookkeeper.client;

import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

public class MySyncObject {

    long lastConfirmed;
    volatile int counter;
    boolean value;
    AtomicInteger rc = new AtomicInteger(BKException.Code.OK);
    Enumeration<LedgerEntry> ls = null;

    public MySyncObject() {
        counter = 0;
        lastConfirmed = LedgerHandle.INVALID_ENTRY_ID;
        value = false;
    }

    void setReturnCode(int rc) {
        this.rc.compareAndSet(BKException.Code.OK, rc);
    }

    int getReturnCode() {
        return rc.get();
    }

    void setLedgerEntries(Enumeration<LedgerEntry> ls) {
        this.ls = ls;
    }

    Enumeration<LedgerEntry> getLedgerEntries() {
        return ls;
    }
}
