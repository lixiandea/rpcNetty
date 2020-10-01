package com.lixiande.common.zookeeper;

import com.lixiande.common.config.Constant;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

import java.util.List;
/**
* @program: CuratorClient
*
* @description: zk curator client for service management
*
* @author: LiXiande
*
* @create: 16:57 2020/9/30
**/
public class CuratorClient {
    private CuratorFramework client;

    /**
     * init client
     * @param connectString: host:port
     * @param namespace: root path for this server
     * @param sessionTimeout : session timeout
     * @param connectionTimeout: connection timeout
     */
    public CuratorClient(String connectString, String namespace, int sessionTimeout, int connectionTimeout){
        client = CuratorFrameworkFactory.builder().namespace(namespace)
                .connectString(connectString).connectionTimeoutMs(connectionTimeout)
                .retryPolicy(new ExponentialBackoffRetry(1000,10))
                .build();
        client.start();
    }
    public CuratorClient(String connectString, int timeout) {
        this(connectString, Constant.ZK_NAMESPACE, timeout, timeout);
    }
    public CuratorClient(String connectString) {
        this(connectString, Constant.ZK_NAMESPACE, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
    }

    public CuratorFramework getClient() {
        return client;
    }

    public void addConnectionStateListner(ConnectionStateListener listener){
        client.getConnectionStateListenable().addListener(listener);
    }

    /**
     * create path and store RpcProtocol
     * @param path : zk path
     * @param data : RpcProtocol with  byte[] format
     * @throws Exception
     */
    public void createPathData(String path, byte[] data) throws Exception {
        // create temporary zk node
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                .forPath(path,data);
    }

    public void updatePathData(String path, byte[] data) throws Exception {
        client.setData().forPath(path,data);
    }
    public void deletePath(String path) throws Exception {
        client.delete().forPath(path);
    }
    public void watchNode(String path, Watcher watcher) throws Exception {
        client.getData().usingWatcher(watcher).forPath(path);
    }
    public byte[] getData(String path) throws Exception {
        return client.getData().forPath(path);
    }

    public List<String> getChildren(String path) throws Exception {
        return client.getChildren().forPath(path);
    }

    public void watchTreeNode(String path, TreeCacheListener listener) {
        TreeCache treeCache = new TreeCache(client, path);
        treeCache.getListenable().addListener(listener);
    }

    public void watchPathChildrenNode(String path, PathChildrenCacheListener listener) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, true);
        //BUILD_INITIAL_CACHE 代表使用同步的方式进行缓存初始化。
        pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        pathChildrenCache.getListenable().addListener(listener);
    }

    public void close() {
        client.close();
    }




}
