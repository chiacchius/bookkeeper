package org.apache.bookkeeper.client;


import com.google.common.util.concurrent.UncheckedExecutionException;
import org.apache.bookkeeper.bookie.BookieImpl;

import org.apache.bookkeeper.conf.BookKeeperClusterTestCase;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.meta.zk.ZKMetadataDriverBase;
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




@RunWith(value = Parameterized.class)
public class BookKeeperAdminInitBookieTest extends BookKeeperClusterTestCase{

    private boolean expectedResult;

    private final boolean deleteDirs;


    private static final int numOfBookies = 2;
    private final boolean useServerConfiguration;
    private final int bookieindex = 0;
    private ServerConfiguration sv = null;

    @Before
    public void setUp() throws Exception {
        try {
            super.setUp();
        }catch (Exception e){
            e.printStackTrace();
        }
        //kill a potential existing bookie
        if (useServerConfiguration) sv = confByIndex(bookieindex);

        System.out.println("starting setUp");
        try {
            killBookie(bookieindex);
        }catch (IndexOutOfBoundsException e){
            System.out.println("there is no bookie to kill");
        }
        if (sv!=null && this.deleteDirs) {

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








    }

    @After
    public void tearDown() throws Exception {

        //kill the created bookie
        killBookie(bookieindex);
        super.tearDown();



    }




    //input parameters
    @Parameterized.Parameters
    public static Collection<?> getParameters(){
        return Arrays.asList(new Object[][] {
                //expected result, useServerConfiguration, deleteDirs
                {true, true, true},
                {false, false, true},
                {false, true, false},

        });
    }

    public BookKeeperAdminInitBookieTest(boolean expectedResult, boolean useServerConfiguration, boolean deleteDirs) {
        super(numOfBookies);
        this.useServerConfiguration = useServerConfiguration;
        this.expectedResult = expectedResult;
        this.deleteDirs = deleteDirs;

    }

    @Test
    public void initBookie() throws Exception {
        System.out.println("starting test");
        boolean result = false;

        if (sv!=null) {
            String bookieId = BookieImpl.getBookieId(sv).toString();
            String bookieCookiePath =
                    ZKMetadataDriverBase.resolveZkLedgersRootPath(sv)
                            + "/" + BookKeeperConstants.COOKIE_NODE
                            + "/" + bookieId;
            zkc.delete(bookieCookiePath, -1);
        try {
                 result = BookKeeperAdmin.initBookie(sv);
            } catch (Exception e) {
                result =false;

            }


        }


        Assert.assertEquals(expectedResult, result);
        System.out.println("test finished");


    }

}
