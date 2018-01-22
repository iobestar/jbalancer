package io.github.jbalancer.proxy;

import org.mapdb.DB;
import org.mapdb.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

@Component
public class YamlNodesStorage {

    private final ConcurrentMap<String,String> storage;

    @Autowired
    public YamlNodesStorage(DB db) {
        this.storage =  db.hashMap("yamlNodes", Serializer.STRING, Serializer.STRING).createOrOpen();
    }

    public Set<String> getBalancerIds() {
        return Collections.unmodifiableSet(storage.keySet());
    }

    public void save(String balancerId, String yamlNodes) {
        storage.put(balancerId, yamlNodes);
    }

    public String getYamlNodes(String balancerId) {
        return storage.get(balancerId);
    }
}
