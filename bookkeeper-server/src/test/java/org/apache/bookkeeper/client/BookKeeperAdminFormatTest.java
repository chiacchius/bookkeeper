package org.apache.bookkeeper.client;

import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.test.BookKeeperClusterTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@RunWith(value = Parameterized.class)
public class BookKeeperAdminFormatTest extends BookKeeperClusterTestCase
{
    private BookKeeper.DigestType digestType = BookKeeper.DigestType.CRC32;
    private static final int numOfBookies = 2;

    private boolean hasValidServConf;
    private boolean isInteractive;
    private boolean isInteractiveYes;
    private boolean force;
    private boolean expectedResult;

    public BookKeeperAdminFormatTest(boolean hasValidServConf, boolean isInteractive, boolean isInteractiveYes, boolean force, boolean expectedResult) {

        super(numOfBookies);


        this.hasValidServConf = hasValidServConf;
        this.isInteractive = isInteractive;
        this.isInteractiveYes = isInteractiveYes;
        this.force = force;
        this.expectedResult = expectedResult;




    }

    //Parametri in input
    @Parameterized.Parameters
    public static Collection<?> getParameters(){
        return Arrays.asList(new Object[][] {

                //serverConfig, isInteractive, isInteractiveYes, force, expectedresult
                {true, true, true, true, true},
                {true, true, false, false, false},
                {true, true, false, false, false},
                {false, true, false, false, false},
                {true, false, false, true, true},
                {true, false, false, true, true},
        });
    }

    @Before
    public void setUp() throws Exception {
        try {

            super.setUp();
        }catch (Exception e){ e.printStackTrace(); }

        try {
            ClientConfiguration conf = new ClientConfiguration();
            conf.setMetadataServiceUri(zkUtil.getMetadataServiceUri());

            /*
             * Creazione di un servizio BookKeeper e l'aggiunta di 2 ledgers
             */
            int numOfLedgers = 2;
            try (BookKeeper bkc = new BookKeeper(conf)) {
                Set<Long> ledgerIds = new HashSet<>();
                for (int n = 0; n < numOfLedgers; n++) {
                    try (LedgerHandle lh = bkc.createLedger(numOfBookies, numOfBookies, digestType, "L".getBytes())) {
                        ledgerIds.add(lh.getId());
                        lh.addEntry("000".getBytes());
                    }
                }
            }
            catch( Exception e1){ e1.printStackTrace();}




        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @After
    public void tearDown() throws Exception{
        super.tearDown();
    }


    @Test
    public void format() {



        if (isInteractive) {
            if (isInteractiveYes) System.setIn(new ByteArrayInputStream("y\n".getBytes(), 0, 2));
            else System.setIn(new ByteArrayInputStream("n\n".getBytes(), 0, 2));
        }

        boolean result;
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
