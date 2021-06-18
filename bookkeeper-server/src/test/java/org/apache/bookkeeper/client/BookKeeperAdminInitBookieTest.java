package org.apache.bookkeeper.client;

import org.apache.bookkeeper.bookie.BookieImpl;
import org.apache.bookkeeper.bookie.storage.ldb.DbLedgerStorage;
import org.apache.bookkeeper.common.allocator.PoolingPolicy;
import org.apache.bookkeeper.conf.BookKeeperClusterTestCase;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.meta.zk.ZKMetadataDriverBase;
import org.apache.bookkeeper.test.ZooKeeperCluster;
import org.apache.bookkeeper.test.ZooKeeperClusterUtil;
import org.apache.bookkeeper.util.BookKeeperConstants;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;


/**
 * Test the bookkeeperAdmin method "initNewCluster()"
 */
@RunWith(value = Parameterized.class)
public class BookKeeperAdminInitBookieTest extends BookKeeperClusterTestCase{

    boolean expectedResult;
    private static final int numOfBookies = 2;
    private boolean useServerConfiguration;
    private int bookieindex = 0;
    private ServerConfiguration sv = null;

    @Before
    public void setUp() throws Exception {

        super.setUp();

        if (useServerConfiguration) sv = confByIndex(bookieindex);


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

        killBookie(bookieindex);



    }




    //Parametri in input
    @Parameterized.Parameters
    public static Collection<?> getParameters(){
        return Arrays.asList(new Object[][] {
                //expected result, useServerConfiguratio
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

        String bookieId = BookieImpl.getBookieId(sv).toString();
        String bookieCookiePath =
                ZKMetadataDriverBase.resolveZkLedgersRootPath(sv)
                        + "/" + BookKeeperConstants.COOKIE_NODE
                        + "/" + bookieId;
        zkc.delete(bookieCookiePath, -1);

        if (!BookKeeperAdmin.initBookie(sv)) killBookie(bookieindex);


        boolean result = BookKeeperAdmin.initBookie(sv);

        Assert.assertEquals(expectedResult, result);



    }



    private void doServerConfig(ServerConfiguration serverConfiguration) {
        serverConfiguration.setJournalFlushWhenQueueEmpty(true);
        serverConfiguration.setAllowEphemeralPorts(true);
        serverConfiguration.setJournalFormatVersionToWrite(5);
        serverConfiguration.setBookiePort(0);
        serverConfiguration.setGcWaitTime(1000);
        serverConfiguration.setDiskUsageWarnThreshold(0.99f);
        serverConfiguration.setDiskUsageThreshold(0.999f);
        serverConfiguration.setProperty(DbLedgerStorage.READ_AHEAD_CACHE_MAX_SIZE_MB, 4);
        serverConfiguration.setAllocatorPoolingPolicy(PoolingPolicy.UnpooledHeap);
        serverConfiguration.setProperty(DbLedgerStorage.WRITE_CACHE_MAX_SIZE_MB, 4);

    }

}
