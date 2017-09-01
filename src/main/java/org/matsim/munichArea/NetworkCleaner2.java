package org.matsim.munichArea;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.api.internal.NetworkRunnable;

/**
 * Created by carlloga on 17-05-17.
 */


public final class NetworkCleaner2 implements NetworkRunnable {
    private static final Logger log = Logger.getLogger(NetworkCleaner2.class);

    public NetworkCleaner2() {
    }

    private Map<Id<Node>, Node> findCluster(Node startNode, Network network) {
        Map<Node, NetworkCleaner2.DoubleFlagRole> nodeRoles = new HashMap(network.getNodes().size());
        ArrayList<Node> pendingForward = new ArrayList();
        ArrayList<Node> pendingBackward = new ArrayList();
        TreeMap<Id<Node>, Node> clusterNodes = new TreeMap();
        clusterNodes.put(startNode.getId(), startNode);
        NetworkCleaner2.DoubleFlagRole r = getDoubleFlag(startNode, nodeRoles);
        r.forwardFlag = true;
        r.backwardFlag = true;
        pendingForward.add(startNode);
        pendingBackward.add(startNode);

        int idx;
        Node currNode;
        Iterator var10;
        Link link;
        Node node;
        while(pendingForward.size() > 0) {
            idx = pendingForward.size() - 1;
            currNode = (Node)pendingForward.remove(idx);
            var10 = currNode.getOutLinks().values().iterator();

            while(var10.hasNext()) {
                link = (Link)var10.next();
                node = link.getToNode();
                r = getDoubleFlag(node, nodeRoles);
                if(!r.forwardFlag) {
                    r.forwardFlag = true;
                    pendingForward.add(node);
                }
            }
        }

        while(pendingBackward.size() > 0) {
            idx = pendingBackward.size() - 1;
            currNode = (Node)pendingBackward.remove(idx);
            var10 = currNode.getInLinks().values().iterator();

            while(var10.hasNext()) {
                link = (Link)var10.next();
                node = link.getFromNode();
                r = getDoubleFlag(node, nodeRoles);
                if(!r.backwardFlag) {
                    r.backwardFlag = true;
                    pendingBackward.add(node);
                    if(r.forwardFlag) {
                        clusterNodes.put(node.getId(), node);
                    }
                }
            }
        }

        return clusterNodes;
    }

    public Map<Id<Node>, Node> searchBiggestCluster(Network network) {
        Map<Id<Node>, Node> visitedNodes = new TreeMap();
        Map<Id<Node>, Node> biggestCluster = new TreeMap();
        log.info("running " + this.getClass().getName() + " algorithm...");
        log.info("  checking " + network.getNodes().size() + " nodes and " + network.getLinks().size() + " links for dead-ends...");
        boolean stillSearching = true;
        Iterator iter = network.getNodes().values().iterator();

        while(iter.hasNext() && stillSearching) {
            Node startNode = (Node)iter.next();
            if(!visitedNodes.containsKey(startNode.getId())) {
                Map<Id<Node>, Node> cluster = this.findCluster(startNode, network);
                visitedNodes.putAll(cluster);
                if(cluster.size() > ((Map)biggestCluster).size()) {
                    biggestCluster = cluster;
                    if(cluster.size() >= network.getNodes().size() - visitedNodes.size()) {
                        stillSearching = false;
                    }
                }
            }
        }

        log.info("    The biggest cluster consists of " + ((Map)biggestCluster).size() + " nodes.");
        log.info("  done.");
        return (Map)biggestCluster;
    }

    public static void reduceToBiggestCluster(Network network, Map<Id<Node>, Node> biggestCluster) {
        List<Node> allNodes2 = new ArrayList(network.getNodes().values());
        Iterator var3 = allNodes2.iterator();

        while(var3.hasNext()) {
            Node node = (Node)var3.next();
            //System.out.print(node.getId().toString().contains("ptbus")+"\n");

            //if(!biggestCluster.containsKey(node.getId())) {
             if(node.getId().toString().contains("ptbus")){
                network.removeNode(node.getId());
            }
        }

        log.info("  resulting network contains " + network.getNodes().size() + " nodes and " + network.getLinks().size() + " links.");
        log.info("done.");
    }

    public void run(Network network) {
        Map<Id<Node>, Node> biggestCluster = this.searchBiggestCluster(network);
        reduceToBiggestCluster(network, biggestCluster);
    }

    private static NetworkCleaner2.DoubleFlagRole getDoubleFlag(Node n, Map<Node, NetworkCleaner2.DoubleFlagRole> nodeRoles) {
        NetworkCleaner2.DoubleFlagRole r = (NetworkCleaner2.DoubleFlagRole)nodeRoles.get(n);
        if(null == r) {
            r = new NetworkCleaner2.DoubleFlagRole();
            nodeRoles.put(n, r);
        }

        return r;
    }

    static class DoubleFlagRole {
        boolean forwardFlag = false;
        boolean backwardFlag = false;

        DoubleFlagRole() {
        }
    }
}
