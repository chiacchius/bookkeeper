package org.apache.bookkeeper.client.ledgerHandleTest;

import org.apache.bookkeeper.client.*;
import org.apache.bookkeeper.conf.BookKeeperClusterTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;


/**
 * Test the LedgerHandle method "asyncReadEntries()"
 */
@RunWith(value = Parameterized.class)
public class LedgerHandleAsyncReadEntriesTest extends BookKeeperClusterTestCase implements AsyncCallback.ReadCallback, AsyncCallback.AddCallback{

    private boolean expectedResult;

    private static LedgerHandle lh;
    private long firstEntry;
    private long lastEntry;
    private byte[] data;

    private static int entries;
    private AsyncCallback.AddCallback cb;

    private static SyncObject sync;       //Object ctx


    private boolean isCbValid; //if cb is valid or is null



    public LedgerHandleAsyncReadEntriesTest(boolean expectedRes, long firstEntry, long lastEntry, boolean isCbValid, SyncObject sync) {
        super(3);

        this.expectedResult = expectedRes;
        this.firstEntry = firstEntry;
        this.lastEntry = lastEntry;
        this.isCbValid = isCbValid;
        this.sync = sync;

    }

    @Before
    public void setup() {

        // ledger creation
        sync = new SyncObject();
        byte[] ledgerPassword = "pwd".getBytes();
        final BookKeeper.DigestType digestType = BookKeeper.DigestType.CRC32;

        try {
            lh = bkc.createLedger(digestType, ledgerPassword);
        } catch (BKException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Ledger created with ID: " + lh.getId());

        data = new byte[]{'m', 'a', 't', 't', 'e', 'o'};

        lh.asyncAddEntry(data, 0, data.length, this, sync);
        lh.asyncAddEntry(data, 0, 3, this, sync);
        lh.asyncAddEntry(data, 1, 4, this, sync);
        lh.asyncAddEntry(data, 0, 2, this, sync);

        entries=4;

        // wait all writings
        synchronized (sync) {
            while (sync.getCounter() < entries) {
                try {
                    sync.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    @Override
    public void addComplete(int rc, LedgerHandle lh, long entryId, Object ctx) {
        SyncObject sync = (SyncObject) ctx;
        sync.setReturnCode(rc);
        synchronized (sync) {
            sync.setCounter(sync.getCounter()+1);
            sync.notify(); //notify sync if a write was completed
        }

    }

    @Override
    public void readComplete(int rc, LedgerHandle lh, Enumeration<LedgerEntry> seq, Object ctx) {
        SyncObject sync = (SyncObject) ctx;
        sync.setLedgerEntries(seq);
        sync.setReturnCode(rc);
        synchronized (sync) {
            sync.setValue(true);
            sync.notify();
        }
    }


    @After
    public void tearDown() throws Exception {
        lh.close();
        super.tearDown();
    }

    @Parameterized.Parameters
    public static Collection<?> getTestParameters() {
        return Arrays.asList(new Object[][]{

                //FALSE if :
                //1. firstEntry<0 || firstEntry > lastEntry
                //2. lastEntry > lastAddConfirmed (last add pushed)


                //boolean expectedRes, long firstEntry, long lastEntry, boolean isCbValid, AsyncHelper.SyncObj sync


                {true, 0, entries, true, sync},
                {false, 0, 50, true, sync},
                {true, 0, 0, true, sync},
                {false, -50, -2, true, sync},
                {true, 0, entries, true, null},
                {false, 1, -1, true, sync}


        });
    }

    @Test
    public void asyncReadEntries()  {

        boolean result;

        result = true;

        if (isCbValid) {
            lh.asyncReadEntries(firstEntry, lastEntry, this, sync);

        } else {
            lh.asyncReadEntries(firstEntry, lastEntry, null, sync);
        }


        // wait until the last entry
        synchronized (sync) {
            while (!sync.isValue()) {
                try {
                    sync.wait();
                } catch (InterruptedException e) {
                    result = false;
                    Assert.assertEquals(expectedResult, result);
                    e.printStackTrace();
                }
            }

            if ( (sync.getReturnCode() ==  BKException.Code.IncorrectParameterException ) ||
                    (sync.getReturnCode() == BKException.Code.ReadException) ){

                result = false;
            }

            Assert.assertEquals(expectedResult, result);

        }


        try {
            lh.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BKException e) {
            e.printStackTrace();
        }

    }
}
