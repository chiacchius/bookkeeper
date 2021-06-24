package org.apache.bookkeeper.client;

import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncObject {

    private long lastConfirmed;
    private volatile int counter;

    private boolean value;
    AtomicInteger rc = new AtomicInteger(BKException.Code.OK);
    Enumeration<LedgerEntry> ls = null;

    public SyncObject() {
        counter = 0;
        lastConfirmed = LedgerHandle.INVALID_ENTRY_ID;
        value = false;
    }

    public void setReturnCode(int rc) {
        this.rc.compareAndSet(BKException.Code.OK, rc);
    }

    public int getReturnCode() {
        return rc.get();
    }

    public void setLedgerEntries(Enumeration<LedgerEntry> ls) {
        this.ls = ls;
    }

    Enumeration<LedgerEntry> getLedgerEntries() {
        return ls;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }
}
