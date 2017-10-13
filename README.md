# JBalancer
Simple Java solution for performing client side load balancing. 

[![Build Status](https://travis-ci.org/iobestar/jbalancer.svg?branch=master)](https://travis-ci.org/iobestar/jbalancer)

## Quick start

See integration test for [JBalancer](https://github.com/iobestar/jbalancer/blob/master/src/test/java/io/github/jbalancer/JBalancerIT.java)

```java
import io.github.jbalancer.Balancer;
import io.github.jbalancer.JBalancer;
import io.github.jbalancer.node.Node;
import io.github.jbalancer.strategy.RoundRobinStrategy;

import java.net.URI;
import java.util.Arrays;

public class QuickStart {

    public static void main(String[] args) throws InterruptedException {

        JBalancer jBalancer = new JBalancer.Builder().build();
        Runtime.getRuntime().addShutdownHook(new Thread(jBalancer::destroy));

        Balancer balancer = jBalancer.create("cluster1", new RoundRobinStrategy(), balancerId -> Arrays.asList(
                new Node(URI.create("https://jbalancer1.io:443"), URI.create("tcp://github.com:443")),
                new Node(URI.create("https://jbalancer2.io:443"), URI.create("tcp://github.com:443"))
        ));

        while (true) {
            System.out.println(balancer.getBalanced().getConnection().toString());
            Thread.sleep(1000);
        }
    }
}
```

## Running the tests

```mvn clean test``` for running JUnit tests

```mvn clen verify``` for running complete integration tests

## Installing

Will be available on [Maven Central](https://search.maven.org)

## Versioning

We use [SemVer](http://semver.org/) for versioning.

## Authors

* **Ivica Obestar**

See also the list of [contributors](https://github.com/iobestar/jbalancer/contributors) who participated in this project.

## License

This project is licensed under the Apache 2.0 License.
