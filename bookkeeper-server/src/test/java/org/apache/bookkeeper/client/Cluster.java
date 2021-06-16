package org.apache.bookkeeper.client;

import org.apache.bookkeeper.bookie.storage.ldb.DbLedgerStorage;
import org.apache.bookkeeper.common.allocator.PoolingPolicy;
import org.apache.bookkeeper.conf.ServerConfiguration;

public class Cluster {



    private ServerConfiguration serverConfiguration;
    private String ledgersRootPath;
    private String instanceId;
    private boolean force;
    private boolean useInstanceId;

    public Cluster(ServerConfiguration serverConfiguration, boolean configuration, String ledgersRootPath) {

        this.serverConfiguration=serverConfiguration;
        this.ledgersRootPath = ledgersRootPath;

        if (serverConfiguration!=null && configuration){
            doServerConfig();
        }

    }

    private void doServerConfig() {
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

    public ServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    public void setServerConfiguration(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }
    public String getLedgersRootPath() {
        return ledgersRootPath;
    }

    public void setLedgersRootPath(String ledgersRootPath) {
        this.ledgersRootPath = ledgersRootPath;
    }


    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}
