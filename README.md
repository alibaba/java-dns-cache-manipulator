# <div align="center"><a href="#dummy"><img src="docs/logo-red.png" alt="🌏 Java Dns Cache Manipulator(DCM)"></a></div>

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
<a href="https://github.com/alibaba/java-dns-cache-manipulator/releases/download/v1.8.2/dcm-1.8.2.tar.gz"><img src="https://img.shields.io/github/downloads/alibaba/java-dns-cache-manipulator/v1.8.2/dcm-1.8.2.tar.gz.svg?logoColor=white&logo=DocuSign" alt="GitHub release download - dcm.tar.gz)"></a>
</p>

📖 English Documentation | 📖 [中文文档](docs/zh-CN/README.md)

----------------------------------------

Java Dns Cache Manipulator(`DCM`) contains 2 subprojects:

- [**`DCM` Library**](library)  
  A tiny 0-dependency thread-safe lib for setting/viewing dns programmatically without touching host file, make unit/integration test portable. Support `Java 8~21`, support `IPv6`.
- [**`DCM` Tool**](tool)  
  A tiny tool for setting/viewing dns of running JVM processes.

> [!NOTE]
> Start from `DCM 1.7+` upgrade to `Java 8` 🚀  
> If you need `Java 6` support, use version `1.6.x` <a href="https://central.sonatype.com/artifact/com.alibaba/dns-cache-manipulator/versions"><img src="https://img.shields.io/maven-central/v/com.alibaba/dns-cache-manipulator?versionPrefix=1.6.&color=lightgrey&logo=apache-maven&logoColor=white" alt="Maven Central"></a>

----------------------------------------

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [Java Dns Cache Manipulator(`DCM`) Library](#java-dns-cache-manipulatordcm-library)
    - [🔧 Features](#-features)
    - [🎨 Requirement Scenario](#-requirement-scenario)
    - [👥 User Guide](#-user-guide)
        - [Set directly](#set-directly)
        - [Batch configuration through the `dns-cache.properties` file](#batch-configuration-through-the-dns-cacheproperties-file)
        - [View JVM DNS cache](#view-jvm-dns-cache)
        - [Remove a DNS cache](#remove-a-dns-cache)
        - [Clear JVM DNS cache](#clear-jvm-dns-cache)
        - [Set/View the default DNS cache time of JVM](#setview-the-default-dns-cache-time-of-jvm)
        - [Precautions for use](#precautions-for-use)
            - [JVM settings for Java 16+](#jvm-settings-for-java-16)
            - [Domain name case](#domain-name-case)
            - [Domain resolution cache](#domain-resolution-cache)
        - [More detailed functions](#more-detailed-functions)
    - [🔌 Java API Docs](#-java-api-docs)
    - [🍪 Dependency](#-dependency)
    - [🗿 More Documentation](#-more-documentation)
    - [📚 Related Resources](#-related-resources)
- [Java Dns Cache Manipulator Tool](#java-dns-cache-manipulator-tool)
    - [🔧 Features](#-features-1)
    - [👥 User Guide](#-user-guide-1)
        - [Download](#download)
        - [Set/reset a DNS cache entry](#setreset-a-dns-cache-entry)
        - [View DNS cache entry content](#view-dns-cache-entry-content)
        - [Remove a DNS Cache](#remove-a-dns-cache)
        - [Clear DNS Cache](#clear-dns-cache)
        - [Set/View DNS cache time of `JVM`](#setview-dns-cache-time-of-jvm)
    - [📚 Related information](#-related-information)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

----------------------------------------

# Java Dns Cache Manipulator(`DCM`) Library

## 🔧 Features

- Set/reset a DNS cache entry (won't lookup DNS afterwards)
    - set a single `DNS record`
    - or batch setting through a `Properties` file
- View DNS cache entry content (positive dns cache and/or negative dns cache)
- Remove a DNS cache entry (i.e. lookup DNS again)
- Clear the DNS Cache (re-Lookup DNS for all domain names)
- Set/View DNS cache time of `JVM` (positive dns cache and negative dns cache)

## 🎨 Requirement Scenario

1. The domain name is hard-coded in some libraries, and have to modify the `host file` binding to do the test(e.g. unit test, integration test). Turn out:
    - Generally, developers do not have the permission to modify the `host file` on the continuous integration machine, which leads to the continuous integration fail.
        - In fact, because of this, the demand for this library was born. 😣 🔫
        - Unit testing requires each developer to do some host binding on the development machine, which increases configuration operations and is tedious and repetitive.
2. Some functions require domain names instead of IPs as input parameters, such as HTTP gateways or web applications with domain name restrictions.
    - In this case, you need a domain name to connect to the IP of the test machine; Or need use a test domain name that does not exist yet, but you do not want to or can not configure the DNS.
3. In the performance test,
    - want to skip DNS lookup through network (bypass the DNS resolution consumption), so that stress testing pays more attention to server response, and stress testing can fully reflect the performance of the core implementation code.
    - DNS cache can be set dynamically instead of inflexible ways such as modifying host files and http links.
    - A `JVM` process can have a set of domain name binding without affecting other JVM, be able to run stress testing with multi-scenario and multi-domain binding.
4. When opening the `SecurityManager` in `Java` (such as a web application in the Web container `Tomcat`), `Java`'s DNS will not be expired by default. If the IP bound to the domain name changes, you can reset the DNS through this library.
    - Set the running JVM DNS Cache through the `DCM` Tool.
      application **need not** contain `DCM` Library dependency (i.e. `Jar`).
    - Or call the method of `DCM` Library through the execution entry, such as remote call or [`jvm-ssh-groovy-shell`](https://github.com/palominolabs/jvm-ssh-groovy-shell).
      The application **need** contain `DCM` Library dependency (ie `Jar`).

## 👥 User Guide

Set/View DNS through the class [`DnsCacheManipulator`](library/src/main/java/com/alibaba/dcm/DnsCacheManipulator.java).

### Set directly

```java
DnsCacheManipulator.setDnsCache("hello.com", "192.168.1.1");
// support IPv6
DnsCacheManipulator.setDnsCache("world.com", "1234:5678:0:0:0:0:0:200e");

// The above settings take effect globally, 
// and then all the domain name resolution logic in Java will be the IP set above.
// Let's use a simple method to get the IP of the domain name to demonstrate:

String ip = InetAddress.getByName("hello.com").getHostAddress();
// ip = "192.168.1.1"
String ipv6 = InetAddress.getByName("world.com").getHostAddress();
// ipv6 = "1234:5678:0:0:0:0:0:200e"


// set multiple IPs
DnsCacheManipulator.setDnsCache("hello-world.com", "192.168.2.1", "192.168.2.2");

String ipHw = InetAddress.getByName("hello-world.com").getHostAddress();
// ipHw = 192.168.2.1, read the first IP
InetAddress[] allIps = InetAddress.getAllByName("hello-world.com");
// Read the multiple IP setting


// Set the expiration time, unit is milliseconds
DnsCacheManipulator.setDnsCache(3600 * 1000, "hello-hell.com", "192.168.1.1", "192.168.1.2");
```

### Batch configuration through the `dns-cache.properties` file

In testing, it is expected that the domain name binding will be written in a configuration file.

The usage is as follows:

provide the file `dns-cache.properties` on ClassPath:

```bash
# Configuration format:
# <host> = <ip>
hello-world.com=192.168.1.1
# Support setting multiple IPs, separated by commas
foo.com=192.168.1.2,192.168.1.3
# Support IPv6
bar.com=1234:5678:0:0:0:0:0:200e
```

Then complete the batch setting with the following code:

```java
DnsCacheManipulator.loadDnsCacheConfig();
```

> NOTE:
>
> The default configuration file name is `dns-cache.properties`.
> and the configuration file name used can be modified through the `-D` option `dcm.config.filename` of the `JVM`: `-Ddcm.config.filename=my-dns-cache.properties`.

```java
// or load the batch setting from the specified file name explicitly
DnsCacheManipulator.loadDnsCacheConfig("my-dns-cache.properties");
```

In unit testing, it is often written in the `setUp` method of the test class, such as:

```java
@BeforeClass
public static void beforeClass() throws Exception {
    DnsCacheManipulator.loadDnsCacheConfig();
}
```

### View JVM DNS cache

```java
// Get a dns cache entry by host
DnsCacheEntry entry = DnsCacheManipulator.getDnsCache("bing.com");

// get whole dns cache info DnsCache including cache and negative cache.
DnsCache dnsCache = DnsCacheManipulator.getWholeDnsCache();

// get positive dns cache entries
//   same as DnsCacheManipulator.getWholeDnsCache().getCache()
List<DnsCacheEntry> positiveEntries = DnsCacheManipulator.getWholeDnsCache();

// get dns negative cache entries
//   same as DnsCacheManipulator.getWholeDnsCache().getNegativeCache()
List<DnsCacheEntry> positiveEntries = DnsCacheManipulator.getWholeDnsCache();
```

### Remove a DNS cache

aka. relookup DNS later.

```java
DnsCacheManipulator.removeDnsCache("bing.com");
```

### Clear JVM DNS cache

```java
DnsCacheManipulator.clearDnsCache();
```

### Set/View the default DNS cache time of JVM

```java
// View the cache time, in seconds. -1 means cache forever, 0 means never cache
int cachePolicy = DnsCacheManipulator.getDnsCachePolicy();
// Set the cache time
DnsCacheManipulator.setDnsCachePolicy(2);

// View the cache time of missed entries(negative entries)
DnsCacheManipulator.getDnsNegativeCachePolicy();
// Set the cache time of missed entries
DnsCacheManipulator.setDnsNegativeCachePolicy(0);
```

### Precautions for use

#### JVM settings for Java 16+

With the release of Java 16 the access control of the new Jigsaw module system is starting to be enforced by the JVM. If you use `DCM` under Java 16+, add below Java options:

```sh
--add-opens java.base/java.net=ALL-UNNAMED
--add-opens java.base/sun.net=ALL-UNNAMED
```

If you use `maven`(e.g. running test), add below config:

```xml
<profiles>
    <profile>
        <id>add-java-open-options-for-jdk16+</id>
        <activation>
            <jdk>[16,)</jdk>
        </activation>
        <properties>
            <argLine>
                --add-opens java.base/java.net=ALL-UNNAMED
                --add-opens java.base/sun.net=ALL-UNNAMED
            </argLine>
        </properties>
    </profile>
</profiles>
```

#### Domain name case

The domain name is not case-sensitive.

The domain name may be converted to lower case uniformly before entering the `JVM` DNS Cache.

One of the causes is that the case of the domain name in the DNS query result will be different from the case of the entered domain name, if the entered domain name has uppercase letters.

#### Domain resolution cache

- For the logic that has been resolved and saved the IP, setting the JVM DNS cache will not take effect! This can be resolved by re-creating the connection or the client.

For `HttpClient`:

```java
HttpClient client = new HttpClient();
GetMethod m1 = new GetMethod("https://bing.com");
client.executeMethod(m1);
String content = m1.getResponseBodyAsString();

// Set up DNS and bind to your own machine
DnsCacheManipulator.setDnsCache("bing.com", "192.168.1.1");

// Re-execute m1, still the old result
client.executeMethod(m1);
String content = m1.getResponseBodyAsString();

// Re-create GetMethod to get the results on your own machine
GetMethod m2 = new GetMethod("https://bing.com");
client.executeMethod(m2);
content = m2.getResponseBodyAsString();
```

### More detailed functions

See the documentation for the class [`DnsCacheManipulator`](library/src/main/java/com/alibaba/dcm/DnsCacheManipulator.java).

## 🔌 Java API Docs

Java API document: <http://alibaba.github.io/java-dns-cache-manipulator/apidocs>

## 🍪 Dependency

`Maven` example:

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>dns-cache-manipulator</artifactId>
    <version>1.8.2</version>
</dependency>
```

You can view the latest version at [search.maven.org](https://search.maven.org/artifact/com.alibaba/dns-cache-manipulator).

## 🗿 More Documentation

- [🎓 Developer Guide](docs/developer-guide.md)

## 📚 Related Resources

- Article [Source code analysis of java dns parsing cache / `java dns`解析缓存之源码解析](http://rongmayisheng.com/?p=1006) is very complete and have source code analysis, give noteworthy conclusions:
    - Open the SecurityManager in Java, the DNS cache will not be invalidated.
    - Otherwise, the accessible DNS resolution will be cached for 30 seconds by default, and the inaccessible DNS resolution will be cached for 10 seconds by default.
- [Regarding the jvm dns cache (domain name cache time) / 关于`jvm dns cache`(域名缓存时间)](https://nigelzeng.iteye.com/blog/1704052), the conclusion of "what strategy is used to return IP for multiple A records" is given:
    - During the validity period of the cache, the obtained IP is always the first A records in the cache, and there is no such strategy as round-robin.
    - After the cache is invalidated, perform DNS resolution again. Because the order of the A records returned by the domain name resolution will change (visible in the dig google.com test), the order of the data in the cache has also changed, and the obtained IP will also change.
- [Modify the content of DNS cache in JDK 1.6 through JAVA reflection / 通过`JAVA`反射修改`JDK 1.6`当中`DNS`缓存内容](https://tuicool.com/articles/auYzui), give the scene of setting DNS cache in performance test.
- [The dns cache problem of java InetAddress / java InetAddress 的 dns cache 问题](http://blogjava.net/jjwwhmm/archive/2008/07/09/213685.html) indicates that `HttpClient` needs to recreate the `GetMethod`/`PostMethod` object to make the DNS setting take effect.

# Java Dns Cache Manipulator Tool

## 🔧 Features

- Set/reset a DNS cache entry
- View DNS cache entry content
- Remove a DNS Cache
- Clear DNS Cache
- Set/View DNS cache time of `JVM`

## 👥 User Guide

### Download

[![GitHub release download - dcm.tar.gz)](https://img.shields.io/github/downloads/alibaba/java-dns-cache-manipulator/v1.8.2/dcm-1.8.2.tar.gz.svg?logoColor=white&logo=DocuSign)](https://github.com/alibaba/java-dns-cache-manipulator/releases/download/v1.8.2/dcm-1.8.2.tar.gz) download the file `dcm-x.y.z.tar.gz`.

After decompression, run `dcm` in the `bin` directory.

```bash
$ dcm -h
usage: Options
 -h,--help             show help
 -p,--pid <arg>        java process id to attach
```

### Set/reset a DNS cache entry

```bash
# For the Java process whose process ID is 12345
# set the domain name foo.com IP to 1.1.1.1
$ dcm -p 12345 set foo.com 1.1.1.1
# For the Java process whose process ID is 12345
# set the domain name bar.com IP to 2.2.2.2 3.3.3.3 (multiple IPs are possible)
$ dcm -p 12345 set bar.com 2.2.2.2 3.3.3.3
```

### View DNS cache entry content

View single entry:

```bash
# For the Java process whose process ID is 12345,
# obtain the DNS entry information of the domain name bing.com
$ dcm -p 12345 get bing.com
bing.com 220.181.57.217,180.149.132.47,123.125.114.144 2015-06-05T18:56:09.635+0800
# The output format:
#   "the domain name" "IP list (there may be multiple IPs)" "expiration time"
```

View all DNS cache:

```bash
$ dcm -p 12345 list
Dns cache:
    bar.com 2.2.2.2,3.3.3.3 292278994-08-17T15:12:55.807+0800
    bing.com 220.181.57.217,180.149.132.47,123.125.114.144 2015-06-05T19:00:30.514+0800
    foo.com 1.1.1.1 292278994-08-17T15:12:55.807+0800
Dns negative cache:
# Output entries containing Cache and Negative Cache.
# The entry is indented 4 spaces.
# In the above example, Negative Cache is empty.
```

### Remove a DNS Cache

```bash
# Remove a DNS
$ dcm -p 12345 rm bing.com
```

### Clear DNS Cache

```bash
# Clear all DNS Cache
$ dcm -p 12345 clear
```

### Set/View DNS cache time of `JVM`

```bash
# View the cache time, in seconds.
# -1 means cache forever, 0 means no cache
$ dcm -p 12345 getPolicy
30
# Set cache time
$ dcm --pid 12345 setPolicy 5
# View the cache time of missed entries, in seconds.
# -1 means cache forever, 0 means no cache
$ dcm -p 12345 getNegativePolicy
10
# Set the cache time of missed entries
$ dcm -p 12345 setNegativePolicy 0
```

### Load Cache Entries from File

```bash
$ dcm -p 12345 load /foo/bar/my-cache.properties
```

### Manipulate cache with a command line option at JVM startup

To manipulate dns entries at jvm startup, add the following jvm-options:

```bash
# Set individual entry
java -javaagent:<path to jar>="set foo.com 1.1.1.1" --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net=ALL-UNNAMED ...
# Load entries from file
java -javaagent:<path to jar>="load my-cache.properties" --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net=ALL-UNNAMED ...
```

## 📚 Related information

* [Java Agent Specification](http://docs.oracle.com/javase/7/docs/api/java/lang/instrument/package-summary.html)
* [New Features of Java SE 6: New Features of Instrumentation](http://www.ibm.com/developerworks/cn/java/j-lo-jse61/)
* [Creation, dynamic loading and instrumentation with javaagents](http://dhruba.name/2010/02/07/creation-dynamic-loading-and-instrumentation-with-javaagents/)
* [Analysis of JavaAgent loading mechanism](http://nijiaben.iteye.com/blog/1847212)
