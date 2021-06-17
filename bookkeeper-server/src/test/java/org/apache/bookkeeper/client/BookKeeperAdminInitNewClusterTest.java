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
public class BookKeeperAdminInitNewClusterTest {

    private Cluster cluster;
    private ZooKeeperCluster zk;
    private boolean expectedResult;

    @Before
    public void setUp() throws Exception {

        System.out.println("Inizializzazione");

        //Il cluster di metadati è gestito con ZooKeeper, instanziamo quindi un cluster Zookeeper
        try {

            zk = new ZooKeeperClusterUtil(5);
            //zk.startCluster();


            System.out.println("Cluster Zookeeper inizializzato e running");

            //E' necessario specificare lo URI dove vengono salvati i ledgers
            if(cluster.getServerConfiguration() != null) cluster.getServerConfiguration().setMetadataServiceUri(zk.getMetadataServiceUri(cluster.getLedgersRootPath()));
            //Inizializazione del nuovo cluster

            System.out.println("Cluster inizializzato");
        }
        catch (Exception e) { System.out.println("Failure nell'inizializzazione del cluster"); e.printStackTrace();}


    }

    @After
    public void tearDown() throws Exception {

        System.out.println("Effettuo il teardown dell'enviroment");
        try{
            zk.getZooKeeperClient().close();
            zk.killCluster();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("Teardown di Zookeeper effettuato");


    }




    //Parametri in input
    @Parameterized.Parameters
    public static Collection<?> getParameters(){
        return Arrays.asList(new Object[][] {
                {new Cluster(new ServerConfiguration(), false, ""), false},
                {new Cluster(new ServerConfiguration(), true, "/testledgers"), true},
                {new Cluster(new ServerConfiguration(), false,"."), false},
                {new Cluster(new ServerConfiguration(), true,"/testledgers"), false},
                {new Cluster(null, false,"/testledgers"), false}

        });
    }

    public BookKeeperAdminInitNewClusterTest(Cluster cluster, boolean expectedResult) {
        this.cluster = cluster;
        this.expectedResult = expectedResult;

        System.out.println("Test inizializzato");


    }

    @Test
    public void initNewCluster() {

        boolean result;

        try {
            result = BookKeeperAdmin.initNewCluster(cluster.getServerConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }

        Assert.assertEquals(result, expectedResult);


    }



}
