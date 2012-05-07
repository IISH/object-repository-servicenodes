package org.objectrepository.dao;

import com.mongodb.Mongo;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton for the MongoDB driver instance.
 *
 * @author: Lucien van Wouw <lwo@iisg.nl>
 */
public class MongoDBSingleton {

    private Mongo mongo = null;

    public MongoDBSingleton() {
    }

    public MongoDBSingleton(String[] hosts) {
        MongoOptions options = new MongoOptions();
        setMongo(hosts, options);
    }

    public MongoDBSingleton(String[] hosts, int connectionsPerHost, int writeConcern) {
        MongoOptions options = new MongoOptions();
        options.w = writeConcern;
        options.connectionsPerHost = connectionsPerHost;
        setMongo(hosts, options);
    }

    public MongoDBSingleton(String[] hosts, MongoOptions options) {
        setMongo(hosts, options);
    }

    private synchronized Mongo setMongo(String[] serverAddresses, MongoOptions options) {

        if (mongo == null) {
            if (serverAddresses.length == 0) {
                serverAddresses = new String[]{"localhost"};
            }
            List<ServerAddress> replSet = new ArrayList<ServerAddress>(serverAddresses.length);
            for (String url : serverAddresses) {
                String[] split = url.split(":", 2);
                try {
                    ServerAddress host = (split.length == 2)
                            ? new ServerAddress(split[0], Integer.parseInt(split[1]))
                            : new ServerAddress(url);
                    replSet.add(host);
                } catch (UnknownHostException e) {
                    System.err.println(e);
                    e.printStackTrace(System.err);
                }
            }
            mongo = (replSet.size() == 1)
                    ? new Mongo(replSet.get(0), options) // Connects to single server
                    : new Mongo(replSet, options);   // Connects to replica set
        }
        return mongo;
    }

    public Mongo getInstance() {
        return mongo;
    }

    public static Mongo newInstance(String[] hosts) {

        MongoDBSingleton instance = new MongoDBSingleton(hosts);
        return instance.getInstance();
    }
}