package org.apache.bookkeeper.client.bookkeeperAdminTest;

import org.apache.bookkeeper.client.BookKeeper;
import org.apache.bookkeeper.client.BookKeeperAdmin;
import org.apache.bookkeeper.client.LedgerHandle;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.conf.BookKeeperClusterTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collection;

/**
 * Test the BookkeeperAdmin method "format()"
 */
@RunWith(value = Parameterized.class)
public class BookKeeperAdminFormatTest extends BookKeeperClusterTestCase
{
    private boolean expectedResult;

    private final BookKeeper.DigestType digestType = BookKeeper.DigestType.CRC32;
    private static final int numOfBookies = 2;

    private final boolean hasValidServConf;
    private final boolean isInteractive;
    private final boolean isInteractiveYes;
    private final boolean force;


    public BookKeeperAdminFormatTest(boolean expectedResult, boolean hasValidServConf, boolean isInteractive, boolean isInteractiveYes, boolean force) {

        super(numOfBookies);
        this.expectedResult = expectedResult;

        this.hasValidServConf = hasValidServConf;
        this.isInteractive = isInteractive;
        this.isInteractiveYes = isInteractiveYes;
        this.force = force;





    }

    //input parameters
    @Parameterized.Parameters
    public static Collection<?> getParameters(){
        return Arrays.asList(new Object[][] {

                // expectedresult, serverConfig, isInteractive, isInteractiveYes, force
                { true, true, true, true, true},
                { false, true, true, false, false},
                {false, true, true, false, false},
                {false, false, true, false, false},
                {true, true, false, false, true},
                {true, true, false, false, true},
        });
    }

    @Before
    public void setUp(){
        try {

            super.setUp();
        }catch (Exception e){ e.printStackTrace(); }

        try {
            ClientConfiguration conf = new ClientConfiguration();
            conf.setMetadataServiceUri(zkUtil.getMetadataServiceUri());


            //Creation of a BookKeeper service and the addition of 2 ledgers

            int numOfLedgers = 2;
            try (BookKeeper bkc = new BookKeeper(conf)) {
                for (int n = 0; n < numOfLedgers; n++) {
                    try (LedgerHandle lh = bkc.createLedger(numOfBookies, numOfBookies, digestType, "L".getBytes())) {
                        lh.addEntry("000".getBytes());
                    }
                }
            }
            catch( Exception e){
                e.printStackTrace();
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    @After
    public void tearDown() throws Exception{
        super.tearDown();
    }


    @Test
    public void format() {

        boolean result;

        if (isInteractive) {
            if (isInteractiveYes) System.setIn(new ByteArrayInputStream("y\n".getBytes(), 0, 2));
            else System.setIn(new ByteArrayInputStream("n\n".getBytes(), 0, 2));
        }


        try{
            if(hasValidServConf) result = BookKeeperAdmin.format(baseConf, isInteractive, force);
            else result = BookKeeperAdmin.format(null, isInteractive, force);
            System.out.println("OK");

        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }

        Assert.assertEquals(result, expectedResult);


    }



}
