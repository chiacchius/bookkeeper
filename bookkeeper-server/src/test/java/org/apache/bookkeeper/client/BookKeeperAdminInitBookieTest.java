package org.apache.bookkeeper.client;

import org.apache.bookkeeper.bookie.BookieException;
import org.apache.bookkeeper.bookie.BookieImpl;
import org.apache.bookkeeper.bookie.storage.ldb.DbLedgerStorage;
import org.apache.bookkeeper.common.allocator.PoolingPolicy;
import org.apache.bookkeeper.conf.BookKeeperClusterTestCase;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.discover.RegistrationManager;
import org.apache.bookkeeper.meta.zk.ZKMetadataDriverBase;
import org.apache.bookkeeper.net.BookieId;
import org.apache.bookkeeper.util.BookKeeperConstants;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import static org.apache.bookkeeper.meta.MetadataDrivers.runFunctionWithRegistrationManager;


/**
 * Test the bookkeeperAdmin method "initNewCluster()"
 */
@RunWith(value = Parameterized.class)
public class BookKeeperAdminInitBookieTest extends BookKeeperClusterTestCase{

    boolean expectedResult;
    private static final int numOfBookies = 2;
    private final boolean useServerConfiguration;
    private final int bookieindex = 0;
    private ServerConfiguration sv = null;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        //kill a potential existing bookie
        if (useServerConfiguration) sv = confByIndex(bookieindex);

        System.out.println("starting setUp");
        try {
            killBookie(bookieindex);
        }catch (IndexOutOfBoundsException e){
            System.out.println("there is no bookie to kill");
        }

        File[] journalDirs = sv.getJournalDirs();
        for (File journalDir : journalDirs) {
            FileUtils.deleteDirectory(journalDir);
        }

        File[] ledgerDirs = sv.getLedgerDirs();
        for (File ledgerDir : ledgerDirs) {
            FileUtils.deleteDirectory(ledgerDir);
        }


        File[] indexDirs = sv.getIndexDirs();
        if (indexDirs != null) {
            for (File indexDir : indexDirs) {
                FileUtils.deleteDirectory(indexDir);
            }
        }








    }

    @After
    public void tearDown() throws Exception {

        //kill the created bookie
        killBookie(bookieindex);



    }




    //Parametri in input
    @Parameterized.Parameters
    public static Collection<?> getParameters(){
        return Arrays.asList(new Object[][] {
                //expected result, useServerConfiguration
                {true, true}
                //{true, data, 2, data.length-2, true, sync},
        });
    }

    public BookKeeperAdminInitBookieTest(boolean expectedResult, boolean useServerConfiguration) {
        super(numOfBookies);
        this.useServerConfiguration = useServerConfiguration;
        this.expectedResult = expectedResult;

    }

    @Test
    public void initBookie() throws Exception {
        System.out.println("starting test");

        String bookieId = BookieImpl.getBookieId(sv).toString();
        String bookieCookiePath =
                ZKMetadataDriverBase.resolveZkLedgersRootPath(sv)
                        + "/" + BookKeeperConstants.COOKIE_NODE
                        + "/" + bookieId;
        zkc.delete(bookieCookiePath, -1);

        boolean result = BookKeeperAdmin.initBookie(sv);

        Assert.assertEquals(expectedResult, result);
        System.out.println("test finished");


    }

}
