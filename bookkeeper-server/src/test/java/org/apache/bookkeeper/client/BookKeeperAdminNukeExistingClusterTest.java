package org.apache.bookkeeper.client;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.*;

import org.apache.bookkeeper.conf.ServerConfiguration;

import org.apache.bookkeeper.meta.zk.ZKMetadataDriverBase;

import org.apache.bookkeeper.test.ZooKeeperCluster;
import org.apache.bookkeeper.test.ZooKeeperClusterUtil;
import org.apache.bookkeeper.util.BookKeeperConstants;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


/**
 * Test the bookkeeperAdmin method "nukeExistingCluster()"
 */
@RunWith(value = Parameterized.class)
public class BookKeeperAdminNukeExistingClusterTest{

    private boolean expectedResult;
    private ZooKeeperCluster zkc;
    private Cluster cluster;


    @Before
    public void setUp() throws Exception {


        /**** initialize a cluster before nuke it ****/
        try {
            zkc= new ZooKeeperClusterUtil(7); // 7 zkNodes to avoid a IllegalArgumentException
            zkc.startCluster();

            if (cluster.getServerConfiguration()!=null){
                cluster.getServerConfiguration().setMetadataServiceUri(zkc.getMetadataServiceUri());
                BookKeeperAdmin.initNewCluster(cluster.getServerConfiguration()); //initialize cluster

                byte[] data = zkc.getZooKeeperClient().getData(
                        ZKMetadataDriverBase.resolveZkLedgersRootPath(cluster.getServerConfiguration()) + "/" + BookKeeperConstants.INSTANCEID,
                        false, null);
                String instanceId = new String(data, UTF_8);

                cluster.setInstanceId(instanceId);

            }
            System.out.println("cluster initilized");


        }
        catch (Exception e) {
            System.out.println("Failure in cluster initialization"); e.printStackTrace();
        }




    }

    @After
    public void tearDown() throws Exception {

        System.out.println("Starting tear down");
        zkc.getZooKeeperClient().close();
        zkc.killCluster();
        System.out.println("Tear down completed. Cluster killed");



    }

    @Parameterized.Parameters
    public static Collection<?> getParameters(){
        return Arrays.asList(new Object[][] {
                {new Cluster(null, "/ledgers",  "7657328", true, true, true), false},
                {new Cluster(new ServerConfiguration(), "/ledgers", "7657328", true, true, true), true},

        });
    }

    public BookKeeperAdminNukeExistingClusterTest(Cluster cluster, boolean expectedResult) {
        this.cluster = cluster;
        this.expectedResult = expectedResult;

        System.out.println("test initialized");


    }


    @Test
    public void nukeExistingCluster() {

        boolean result;
        System.out.println("test started");
        try {
            result = BookKeeperAdmin.nukeExistingCluster(cluster.getServerConfiguration(), this.cluster.getLedgersRootPath(), cluster.getInstanceId(), cluster.isForce());
        }
        catch(Exception e){
            e.printStackTrace();
            result = false;
        }
        System.out.println("test finished");

        Assert.assertEquals(expectedResult, result);



    }



}
