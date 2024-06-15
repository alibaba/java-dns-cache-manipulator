# 🎓 Developer Guide

<p align="center">
<a href="https://github.com/alibaba/java-dns-cache-manipulator/actions/workflows/ci.yaml"><img src="https://img.shields.io/github/actions/workflow/status/alibaba/java-dns-cache-manipulator/ci.yaml?branch=main&logo=github&logoColor=white&label=fast ci" alt="Github Workflow Build Status"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/actions/workflows/strong_ci.yaml"><img src="https://img.shields.io/github/actions/workflow/status/alibaba/java-dns-cache-manipulator/strong_ci.yaml?branch=main&logo=github&logoColor=white&label=strong ci" alt="Github Workflow Build Status"></a>
<a href="https://codecov.io/gh/alibaba/java-dns-cache-manipulator/branch/main"><img src="https://img.shields.io/codecov/c/github/alibaba/java-dns-cache-manipulator/main?logo=codecov&logoColor=white" alt="Coverage Status"></a>
<a href="https://codeclimate.com/github/alibaba/java-dns-cache-manipulator"><img src="https://img.shields.io/codeclimate/maintainability/alibaba/java-dns-cache-manipulator?logo=code-climate" alt="Maintainability"></a>
<a href="https://openjdk.java.net/"><img src="https://img.shields.io/badge/Java-8+-339933?logo=openjdk&logoColor=white" alt="Java support"></a>
<a href="https://www.apache.org/licenses/LICENSE-2.0.html"><img src="https://img.shields.io/github/license/alibaba/java-dns-cache-manipulator?color=4D7A97&logo=apache" alt="License"></a>
<a href="https://search.maven.org/artifact/com.alibaba/dns-cache-manipulator"><img src="https://img.shields.io/maven-central/v/com.alibaba/dns-cache-manipulator?color=2d545e&logo=apache-maven&logoColor=white" alt="Maven Central"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/releases"><img src="https://img.shields.io/github/release/alibaba/java-dns-cache-manipulator.svg" alt="GitHub release"></a>
<a href="https://alibaba.github.io/java-dns-cache-manipulator/apidocs/"><img src="https://img.shields.io/github/release/alibaba/java-dns-cache-manipulator?label=javadoc&color=339933&logo=microsoft-academic&logoColor=white" alt="Javadocs"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/stargazers"><img src="https://img.shields.io/github/stars/alibaba/java-dns-cache-manipulator?style=flat" alt="GitHub Stars"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/fork"><img src="https://img.shields.io/github/forks/alibaba/java-dns-cache-manipulator?style=flat" alt="GitHub Forks"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/issues"><img src="https://img.shields.io/github/issues/alibaba/java-dns-cache-manipulator" alt="GitHub issues"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/network/dependents"><img src="https://badgen.net/github/dependents-repo/alibaba/java-dns-cache-manipulator?label=user%20repos" alt="user repos"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/graphs/contributors"><img src="https://img.shields.io/github/contributors/alibaba/java-dns-cache-manipulator" alt="GitHub Contributors"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator"><img src="https://img.shields.io/github/repo-size/alibaba/java-dns-cache-manipulator" alt="GitHub repo size"></a>
<a href="https://gitpod.io/#https://github.com/alibaba/java-dns-cache-manipulator"><img src="https://img.shields.io/badge/Gitpod-ready to code-339933?label=gitpod&logo=gitpod&logoColor=white" alt="gitpod: Ready to Code"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/releases/download/v1.8.1/dcm-1.8.1.tar.gz"><img src="https://img.shields.io/github/downloads/alibaba/java-dns-cache-manipulator/v1.8.1/dcm-1.8.1.tar.gz.svg?logoColor=white&logo=DocuSign" alt="GitHub release download - dcm.tar.gz)"></a>
</p>

## How to set the DNS Cache of `JVM` safely

The DNS Cache of the JVM is maintained in the private field of the InetAddress class and is set by reflection. For specific implementation, see


- [`InetAddressCacheUtilCommons.java`](../library/src/main/java/com/alibaba/dcm/internal/InetAddressCacheUtilCommons.java)
- [`InetAddressCacheUtilForOld.java`](../library/src/main/java/com/alibaba/dcm/internal/InetAddressCacheUtilForOld.java)
- [`InetAddressCacheUtilForNew.java`](../library/src/main/java/com/alibaba/dcm/internal/InetAddressCacheUtilForNew.java)

Pay attention to the thread safety of setting the DNS Cache of JVM
The DNS Cache of the JVM is obviously shared globally, so the setting is guaranteed to be thread-safe and there is no concurrency problem.

Taking JDK 8 as an example, by looking at the implementation of the InetAddress class, it can be determined that thread safety is ensured by the synchronized block with the addressCache field as the lock.

The key code is as follows:

```java
/*
 * Cache the given hostname and addresses.
 */
private static void cacheAddresses(String hostname,
                                   InetAddress[] addresses,
                                   boolean success) {
    hostname = hostname.toLowerCase();
    synchronized (addressCache) {
        cacheInitIfNeeded();
        if (success) {
            addressCache.put(hostname, addresses);
        } else {
            negativeCache.put(hostname, addresses);
        }
    }
}
```

In the [`InetAddressCacheUtilForOld`](../library/src/main/java/com/alibaba/dcm/internal/InetAddressCacheUtilForOld.java) class, the read and write to the DNS Cache also consistently adds a synchronized block with `addressCache` field as the lock to ensure thread safety.

## Need test different `JDK` versions

The implementation of this library uses the non-public API of the `JDK`, and different `JDK` implementations will be different, that is, compatible logic is required, and different versions of `JDK` are tested to ensure functionality.

The LTS `JDK` versions(8/11/17) and recent versions are tested , other `JDK` versions should work properly.

## 📚 Related Resources

- The source code of the class `InetAddress`:
    - `JDK 6` [`InetAddress`](https://hg.openjdk.java.net/jdk6/jdk6/jdk/file/8deef18bb749/src/share/classes/java/net/InetAddress.java#l739)
    - `JDK 7` [`InetAddress`](https://hg.openjdk.java.net/jdk7u/jdk7u/jdk/file/4dd5e486620d/src/share/classes/java/net/InetAddress.java#l742)
    - `JDK 8` [`InetAddress`](https://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/45e4e636b757/src/share/classes/java/net/InetAddress.java#l748)
    - `JDK 9` [`InetAddress`](https://hg.openjdk.java.net/jdk9/jdk9/jdk/file/65464a307408/src/java.base/share/classes/java/net/InetAddress.java#l783)
    - `JDK 11` [`InetAddress`](https://hg.openjdk.java.net/jdk/jdk11/file/1ddf9a99e4ad/src/java.base/share/classes/java/net/InetAddress.java#l787)
- [`JVM Networking Properties` - `java docs`](http://docs.oracle.com/javase/8/docs/technotes/guides/net/properties.html)
- [Domain Name System - wikipedia](http://en.wikipedia.org/wiki/Domain_Name_System)
- `Java DNS` FAQ
    - [`Java DNS cache` viewer - stackoverflow](http://stackoverflow.com/questions/1835421/java-dns-cache-viewer)
    - [Disable `DNS caching`](http://www.rgagnon.com/javadetails/java-0445.html)
    - [FileOutput Node - Java DNS caching pitfall - quick clarification and tips](https://www.ibm.com/developerworks/community/blogs/aimsupport/entry/fileoutput_node_dns_caching_pitfall?lang=en)
- The [`javahost`](https://github.com/tanhaichao/javahost) project of [`@tanhaichao`](https://github.com/tanhaichao) (Thanks for your work!)
    - [the documentation](http://leopard.io/modules/javahost) of the project.
    - The solution of how to set up Java DNS Cache comes from this project. When I first encountered the host binding problem in the continuous integration project, I also used the project to solve it 👍
