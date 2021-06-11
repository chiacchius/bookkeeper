package org.apache.bookkeeper.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.bookkeeper.util.BookKeeperConstants.AVAILABLE_NODE;
import static org.apache.bookkeeper.util.BookKeeperConstants.READONLY;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import com.google.common.net.InetAddresses;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.bookkeeper.bookie.BookieImpl;
import org.apache.bookkeeper.client.BookKeeper.DigestType;
import org.apache.bookkeeper.client.api.LedgerMetadata;
import org.apache.bookkeeper.common.component.ComponentStarter;
import org.apache.bookkeeper.common.component.Lifecycle;
import org.apache.bookkeeper.common.component.LifecycleComponent;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.conf.TestBKConfiguration;
import org.apache.bookkeeper.discover.BookieServiceInfo;
import org.apache.bookkeeper.meta.UnderreplicatedLedger;
import org.apache.bookkeeper.meta.ZkLedgerUnderreplicationManager;
import org.apache.bookkeeper.meta.zk.ZKMetadataDriverBase;
import org.apache.bookkeeper.net.BookieId;
import org.apache.bookkeeper.net.BookieSocketAddress;
import org.apache.bookkeeper.proto.BookieServer;
import org.apache.bookkeeper.replication.ReplicationException.UnavailableException;
import org.apache.bookkeeper.server.Main;
import org.apache.bookkeeper.server.conf.BookieConfiguration;
import org.apache.bookkeeper.util.AvailabilityOfEntriesOfLedger;
import org.apache.bookkeeper.util.BookKeeperConstants;
import org.apache.bookkeeper.util.PortManager;
import org.apache.commons.io.FileUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the bookkeeperAdmin method "nukeExistingCluster()"
 */

public class BookKeeperAdminNukeExistingClusterTest{






}
