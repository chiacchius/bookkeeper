package org.apache.bookkeeper.client.ledgerHandleTest;


import org.apache.bookkeeper.client.*;
import org.apache.bookkeeper.conf.BookKeeperClusterTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.TimeoutException;


/**
 * Test the LedgerHandle method "asyncAddEntry()"
 */
@RunWith(value = Parameterized.class)
public class LedgerHandleAsyncAddEntryTest extends BookKeeperClusterTestCase implements AsyncCallback.AddCallback {

    private boolean expectedResult;

    private static LedgerHandle lh;
    private byte[] data;
    private int offset;
    private int lenght;
    private AsyncCallback.AddCallback cb;

    private static SyncObject sync;       //Object ctx


    private boolean isCbValid; //if cb is valid or is null

    public LedgerHandleAsyncAddEntryTest(boolean expectedRes, byte[] data, int offset, int lenght, boolean isCbValid, SyncObject sync) {
        super(3);

        this.expectedResult =expectedRes;
        this.data=data;
        this.offset=offset;
        this.lenght=lenght;
        this.isCbValid=isCbValid;
        this.sync=sync;

    }

    @Before
    public void setup(){
        // ledger creation

        System.out.println("starting setUp");

        sync = new SyncObject();
        byte[] ledgerPassword = "pwd".getBytes();
        final BookKeeper.DigestType digestType = BookKeeper.DigestType.CRC32;

        try {
            lh = bkc.createLedger(digestType, ledgerPassword);
        } catch (BKException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Ledger created with ID: " + lh.getId());

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


    @Parameterized.Parameters
    public static Collection<?> getTestParameters() {
        byte[] data = {'t','e','s','t','i','n','g'};
        return Arrays.asList(new Object[][] {

                /* FALSE if :
                1. offset <0
                2. lenght <0
                3. offset + lenght > len(data)
                 */



                //boolean expectedRes, byte[] data, int offset, int lenght, boolean isCbValid, SyncObject sync
                {true, data, 0, data.length, true, sync},
                {false, null, 0, data.length, true, sync},
                {false, data, -1, data.length, true, sync},
                {false, data, 2, -1, true, sync},
                {false, data, 0, data.length+1, true, sync},
                //{false, data, 0, data.length, false, sync}, //it fail because of TimeoutException
                {true, data, 0, data.length, true, null},

        });
    }

    @After
    public void tearDown() throws Exception{

        System.out.println("starting tearDown");

        lh.close();
        super.tearDown();
        System.out.println("tearDown finished");

    }




    @Test
    public void asyncAddEntry(){

        boolean result;
        System.out.println("starting test");

        try {
            result = true;

            if (isCbValid) {
                lh.asyncAddEntry(data, offset, lenght, this, sync);

            }
            else{
                //cb = null if it isn't valid
                lh.asyncAddEntry(data, offset, lenght, null, sync);

            }


        } catch (ArrayIndexOutOfBoundsException e) {
            result= false;
            e.printStackTrace();

        } catch (NullPointerException e){
            result= false;
            e.printStackTrace();
        }catch (IllegalArgumentException e){
            result=false;
            e.printStackTrace();
        }/*catch (TimeoutException e){
            result=false;
            e.printStackTrace();
        }*/

        // wait for all entries to be added -> writing completed
        if (expectedResult){
            synchronized (sync) {
                while (sync.getCounter() < 1) {

                    try {
                        sync.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (sync.getReturnCode()!=BKException.Code.OK){
                    result = false;
                }
            }
        }

        Assert.assertEquals(expectedResult,result);

        //check if in the entry there is what i wrote before

        if (expectedResult){

            int numEntries = 1;
            String actualEntry, expectedEntry;

            actualEntry=null;
            expectedEntry=null;

            Enumeration<LedgerEntry> entries = null;

            try {
                entries = lh.readEntries(0, numEntries - 1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BKException e) {
                e.printStackTrace();
            }

            while (entries.hasMoreElements()) {


                byte[] expectedTemp = null;
                byte[] entryTemp = entries.nextElement().getEntry();

                expectedTemp = Arrays.copyOfRange(data, 0, data.length);

                try {
                    actualEntry = new String(entryTemp, "UTF-8") ;
                    expectedEntry = new String(expectedTemp, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                System.out.println("\n----- entry: " + actualEntry+"   expected: "+expectedEntry);
                Assert.assertEquals(actualEntry, expectedEntry);

            }


        }
        System.out.println("test finished");

    }









}
