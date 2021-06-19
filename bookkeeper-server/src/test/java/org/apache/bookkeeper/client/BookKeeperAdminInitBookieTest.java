package org.apache.bookkeeper.client;


import org.apache.bookkeeper.bookie.BookieImpl;

import org.apache.bookkeeper.conf.BookKeeperClusterTestCase;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.meta.zk.ZKMetadataDriverBase;
import org.apache.bookkeeper.net.BookieId;
import org.apache.bookkeeper.util.BookKeeperConstants;
import org.apache.bookkeeper.versioning.Version;
import org.apache.bookkeeper.versioning.Versioned;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import static org.apache.bookkeeper.meta.MetadataDrivers.runFunctionWithRegistrationManager;


@RunWith(value = Parameterized.class)
public class BookKeeperAdminInitBookieTest extends BookKeeperClusterTestCase{

    private boolean expectedResult;

    private final boolean deleteDirs;

    private boolean writeBookieData;
    private static final int numOfBookies = 2;
    private final boolean useServerConfiguration;
    private final int bookieindex = 0;
    private ServerConfiguration sv = null;
    private boolean removeBookie;

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

        if (sv!=null && this.deleteDirs) {

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
            File[] journalDirs = sv.getJournalDirs();
            for (File journalDir : journalDirs) {
                FileUtils.deleteDirectory(journalDir);
            }

        }


    }



    @After
    public void tearDown() throws Exception {

        //kill the created bookie
        try {
            killBookie(bookieindex);
        }catch (IndexOutOfBoundsException e){
            System.out.println("IndexOutOfBoundsException");
        }
        super.tearDown();



    }


    //input parameters
    @Parameterized.Parameters
    public static Collection<?> getParameters(){
        return Arrays.asList(new Object[][] {
                //expected result, useServerConfiguration, deleteDirs, writeBookieData, removeBookie
                {true, true, true, false, true},
                {false, false, true, false, true},
                {false, true, false, true, false},
                {false, true, true, true, false},
                {false, true, true, false, false},
                {false, true, true, true, true}

        });
    }

    public BookKeeperAdminInitBookieTest(boolean expectedResult, boolean useServerConfiguration, boolean deleteDirs, boolean writeBookieData, boolean removeBookie) {
        super(numOfBookies);
        this.useServerConfiguration = useServerConfiguration;
        this.expectedResult = expectedResult;
        this.deleteDirs = deleteDirs;
        this.writeBookieData = writeBookieData;
        this.removeBookie = removeBookie;

    }

    @Test
    public void initBookie() throws Exception {
        System.out.println("starting test");
        boolean result = false;

        if (sv!=null) {

            BookieId bookieId = BookieImpl.getBookieId(sv);
            if (removeBookie) {
                try {
                    killBookie(bookieindex);
                }catch (IndexOutOfBoundsException e){
                    System.out.println("there is no bookie to kill");
                }
                String bookieCookiePath =
                        ZKMetadataDriverBase.resolveZkLedgersRootPath(sv)
                                + "/" + BookKeeperConstants.COOKIE_NODE
                                + "/" + bookieId.toString();
                zkc.delete(bookieCookiePath, -1);
            }

            if (this.writeBookieData) {
                runFunctionWithRegistrationManager(sv, rm -> {
                    Versioned<byte[]> cookieData = new Versioned<>("test".getBytes(StandardCharsets.UTF_8), Version.NEW);
                    try {
                        rm.writeCookie(bookieId, cookieData);

                    } catch (Exception e) {

                    }
                    return null;
                });
            }

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
