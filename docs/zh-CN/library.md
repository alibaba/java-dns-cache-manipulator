Java Dns Cache Manipulator(DCM) Library
=======================================

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
<a href="https://github.com/alibaba/java-dns-cache-manipulator/stargazers"><img src="https://img.shields.io/github/stars/alibaba/java-dns-cache-manipulator" alt="GitHub Stars"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/fork"><img src="https://img.shields.io/github/forks/alibaba/java-dns-cache-manipulator" alt="GitHub Forks"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/issues"><img src="https://img.shields.io/github/issues/alibaba/java-dns-cache-manipulator" alt="GitHub issues"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/network/dependents"><img src="https://badgen.net/github/dependents-repo/alibaba/java-dns-cache-manipulator?label=user%20repos" alt="user repos"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/graphs/contributors"><img src="https://img.shields.io/github/contributors/alibaba/java-dns-cache-manipulator" alt="GitHub Contributors"></a>
<a href="https://gitpod.io/#https://github.com/alibaba/java-dns-cache-manipulator"><img src="https://img.shields.io/badge/Gitpod-ready to code-339933?label=gitpod&logo=gitpod&logoColor=white" alt="gitpod: Ready to Code"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator"><img src="https://img.shields.io/github/repo-size/alibaba/java-dns-cache-manipulator" alt="GitHub repo size"></a>
</p>

:point_right: 用编码的方式设置/查看`JVM`的`DNS`（实际上设置的是`DNS Cache`），支持`JDK 6+`，支持`IPv6`。

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [:wrench: 功能](#wrench-%E5%8A%9F%E8%83%BD)
- [:art: 需求场景](#art-%E9%9C%80%E6%B1%82%E5%9C%BA%E6%99%AF)
- [:busts_in_silhouette: User Guide](#busts_in_silhouette-user-guide)
    - [直接设置](#%E7%9B%B4%E6%8E%A5%E8%AE%BE%E7%BD%AE)
    - [通过`dns-cache.properties`文件批量配置](#%E9%80%9A%E8%BF%87dns-cacheproperties%E6%96%87%E4%BB%B6%E6%89%B9%E9%87%8F%E9%85%8D%E7%BD%AE)
    - [清空`JVM DNS Cache`](#%E6%B8%85%E7%A9%BAjvm-dns-cache)
    - [删除一条`DNS Cache`](#%E5%88%A0%E9%99%A4%E4%B8%80%E6%9D%A1dns-cache)
    - [查看`JVM DNS Cache`](#%E6%9F%A5%E7%9C%8Bjvm-dns-cache)
    - [设置/查看`JVM`缺省的`DNS`的缓存时间](#%E8%AE%BE%E7%BD%AE%E6%9F%A5%E7%9C%8Bjvm%E7%BC%BA%E7%9C%81%E7%9A%84dns%E7%9A%84%E7%BC%93%E5%AD%98%E6%97%B6%E9%97%B4)
    - [使用注意](#%E4%BD%BF%E7%94%A8%E6%B3%A8%E6%84%8F)
    - [更多详细功能](#%E6%9B%B4%E5%A4%9A%E8%AF%A6%E7%BB%86%E5%8A%9F%E8%83%BD)
- [:electric_plug: Java API Docs](#electric_plug-java-api-docs)
- [:cookie: 依赖](#cookie-%E4%BE%9D%E8%B5%96)
- [:eyeglasses: 经过测试的`JDK`](#eyeglasses-%E7%BB%8F%E8%BF%87%E6%B5%8B%E8%AF%95%E7%9A%84jdk)
- [:mortar_board: Developer Guide](#mortar_board-developer-guide)
    - [如何设置`JVM`的`DNS Cache`](#%E5%A6%82%E4%BD%95%E8%AE%BE%E7%BD%AEjvm%E7%9A%84dns-cache)
    - [注意设置`JVM`的`DNS Cache`的线程安全问题](#%E6%B3%A8%E6%84%8F%E8%AE%BE%E7%BD%AEjvm%E7%9A%84dns-cache%E7%9A%84%E7%BA%BF%E7%A8%8B%E5%AE%89%E5%85%A8%E9%97%AE%E9%A2%98)
    - [需要测试不同版本`JDK`](#%E9%9C%80%E8%A6%81%E6%B5%8B%E8%AF%95%E4%B8%8D%E5%90%8C%E7%89%88%E6%9C%ACjdk)
- [:books: 相关资料](#books-%E7%9B%B8%E5%85%B3%E8%B5%84%E6%96%99)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

:wrench: 功能
=====================================

- 设置/重置`DNS`（不会再去`Lookup DNS`）
    - 可以设置单条
    - 或是通过`Properties`文件批量设置
- 清空`DNS Cache`（即所有的域名重新`Lookup DNS`）
- 删除一条`DNS Cache`（即重新`Lookup DNS`）
- 查看`DNS Cache`内容
- 设置/查看`JVM`缺省的`DNS`的缓存时间

:art: 需求场景
=====================================

1. 一些库中写死了连接域名，需要通过设置`host`文件绑定才能做测试。结果是：
    - 自动持续集成的机器上一般同学是没有权限去设置`host`文件的，导致项目不能持续集成。  
        实际上是因为这点，催生这个库的需求。 :persevere::gun:
    - 单元测试需要每个开发都在开发机上做`host`绑定，增加了依赖的配置操作且繁琐重复。
1. 一些功能需要域名作为输入参数，如使用`HTTP`请求的网关 或是 有域名检查限制的`Web`应用。  
    这种情况下，让需要让一个域名连接到测试机器的`IP`上，或是 使用一个还不存在的域名但又不想或不能去配置`DNS`。
1. 在性能测试时，
    - 不去做网络的`DNS Lookup`（`DNS`解析消耗），这样使得压测更加关注服务器响应，压测更充分反应出实现代码的性能。
    - 可以动态设置`DNS`缓存，无需修改`host`文件和`http`链接等不灵活的方式。
    - 一个`JVM`进程可以对应一套域名绑定，相互之间不影响，可以实现多场景，多域名绑定的需求压测。
1. 打开`Java`中的`SecurityManager`时（如在`Web`容器`Tomcat`中的`Web`应用），`Java`的`DNS`缺省是不会失效的。
    如果域名绑定的`IP`变了，可以通过这个库重置`DNS`。
    - 通过[`Java Dns Cache Manipulator Tool`](../tool)设置运行中`JVM DNS Cache`。  
        **无需** 应用包含了`Java Dns Cache Manipulator Library`依赖（即`Jar`）。
    - 或通过执行入口调用`Java Dns Cache Manipulator Library`的方法，比如远程调用或是[`jvm-ssh-groovy-shell`](https://github.com/palominolabs/jvm-ssh-groovy-shell)。  
        ***需要*** 应用已经包含了`Java Dns Cache Manipulator Library`依赖（即`Jar`）。

:busts_in_silhouette: User Guide
=====================================

通过类[`DnsCacheManipulator`](../../library/src/main/java/com/alibaba/dcm/DnsCacheManipulator.java)设置`DNS`。

直接设置
----------------------------------

```java
DnsCacheManipulator.setDnsCache("www.hello.com", "192.168.1.1");
DnsCacheManipulator.setDnsCache("www.world.com", "1234:5678:0:0:0:0:0:200e"); // 支持IPv6地址

// 上面设置全局生效，之后Java中的所有的域名解析逻辑都会是上面设定的IP。
// 下面用一个简单获取域名对应的IP，来演示一下：

String ip = InetAddress.getByName("www.hello.com").getHostAddress();
// ip = "192.168.1.1"
String ipv6 = InetAddress.getByName("www.world.com").getHostAddress();
// ipv6 = "1234:5678:0:0:0:0:0:200e"


// 可以设置多个IP
DnsCacheManipulator.setDnsCache("www.hello-world.com", "192.168.2.1", "192.168.2.2");

String ipHw = InetAddress.getByName("www.hello-world.com").getHostAddress();
// ipHw = 192.168.2.1 ，读到第一个IP
InetAddress[] allIps = InetAddress.getAllByName("www.hello-world.com");
// 上面读到设置的多个IP

// 设置失效时间，单元毫秒
DnsCacheManipulator.setDnsCache(3600 * 1000, "www.hello-hell.com", "192.168.1.1", "192.168.1.2");
```

通过`dns-cache.properties`文件批量配置
----------------------------------

在代码测试中，会期望把域名绑定写在配置文件。

使用方式如下：

在`ClassPath`上，提供文件`dns-cache.properties`：

```bash
# 配置格式：
# <host> = <ip>
www.hello-world.com=192.168.1.1
# 支持设置多个IP，用逗号分隔
www.foo.com=192.168.1.2,192.168.1.3
# 支持IPv6
www.bar.com=1234:5678:0:0:0:0:0:200e
```

> 注：  
> `dns-cache.properties`是缺省文件名，可以通过`JVM`的`-D`选项`dcm.config.filename`修改使用的配置文件名，如  
> `-Ddcm.config.filename=my-dns-cache.properties`。

然后通过下面的一行代码完成批量设置：

```java
DnsCacheManipulator.loadDnsCacheConfig();
```

在单元测试中，往往会写在测试类的`setUp`方法中，如：

```java
@BeforeClass
public static void beforeClass() throws Exception {
    DnsCacheManipulator.loadDnsCacheConfig();
}
```

清空`JVM DNS Cache`
----------------------------------

```java
DnsCacheManipulator.clearDnsCache();
```

删除一条`DNS Cache`
----------------------------------

即重新`Lookup DNS`。

```java
DnsCacheManipulator.removeDnsCache("aliyun.com");
```

查看`JVM DNS Cache`
----------------------------------

```java
DnsCache dnsCache = DnsCacheManipulator.getWholeDnsCache()
System.out.println(dnsCache);
```

设置/查看`JVM`缺省的`DNS`的缓存时间
----------------------------------

```java
// 查看缓存时间，单位秒。-1表示永远缓存，0表示不缓存
int cachePolicy = DnsCacheManipulator.getDnsCachePolicy();
// 设置缓存时间
DnsCacheManipulator.setDnsCachePolicy(2);

// 查看未命中条目的缓存时间
DnsCacheManipulator.getDnsNegativeCachePolicy()
// 设置未命中条目的缓存时间
DnsCacheManipulator.setDnsNegativeCachePolicy(0);
```

使用注意
----------------------------------

- 域名不区分大小写，域名会统一转成小写，再进入`DNS Cache`。  
    其中一个引发的现象是，`DNS`查询结果的域名会和输入的域名大小写不同，如果输入的域名有大写字母。
- 对于已经完成解析保存了`IP`的逻辑，设置`JVM DNS`缓存，不会生效！可以重新创建 连接或`Client`解决。  
如对于`HttpClient`:

```java
HttpClient client = new HttpClient();
GetMethod m1 = new GetMethod("https://www.aliyun.com");
client.executeMethod(m1);
String content = m1.getResponseBodyAsString();

// 设置DNS，绑定到自己的机器
DnsCacheManipulator.setDnsCache("www.aliyun.com", "192.168.1.1");

// 重新执行m1，仍然是老结果
client.executeMethod(m1);
String content = m1.getResponseBodyAsString();

// 重新创建GetMethod，才能得到自己机器上的结果
GetMethod m2 = new GetMethod("https://www.aliyun.com");
client.executeMethod(m2);
content = m2.getResponseBodyAsString();
```

更多详细功能
----------------------------------

参见类[`DnsCacheManipulator`](../../library/src/main/java/com/alibaba/dcm/DnsCacheManipulator.java)的文档说明。

:electric_plug: Java API Docs
=====================================

`Java API`文档地址： <http://alibaba.github.io/java-dns-cache-manipulator/apidocs>

:cookie: 依赖
=====================================

`Maven`示例：

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>dns-cache-manipulator</artifactId>
    <version>1.8.1</version>
</dependency>
```

可以在[search.maven.org](https://search.maven.org/artifact/com.alibaba/dns-cache-manipulator)查看最新的版本。

:eyeglasses: 经过测试的`JDK`
==================================

JDK               | 系统                   | On        | 备注
---               | ---                    | ---       | ----
openjdk6 64-Bit   | Linux                  | travis-ci |
oraclejdk7 64-Bit | Linux                  | travis-ci |
openjdk7 64-Bit   | Linux                  | travis-ci |
oraclejdk8 64-Bit | Linux                  | travis-ci |
applejdk6 64-Bit  | Mac                    | 个人Mac   | jdk6由Apple[提供](https://java.com/zh_CN/download/faq/java_mac.xml)，[下载地址](https://support.apple.com/kb/DL1572?locale=zh_CN)。
oraclejdk7 64-Bit | Mac                    | 个人Mac   | 从jdk7开始，Mac jdk直接在`Oracle`下载。
oraclejdk8 64-Bit | Mac                    | 个人Mac   |
oraclejdk6 64-Bit | windows server 2012 r2 | appveyor  |
oraclejdk6 32-Bit | windows server 2012 r2 | appveyor  |
oraclejdk7 64-Bit | windows server 2012 r2 | appveyor  |
oraclejdk7 32-Bit | windows server 2012 r2 | appveyor  |
oraclejdk8 64-Bit | windows server 2012 r2 | appveyor  |
oraclejdk8 32-Bit | windows server 2012 r2 | appveyor  |

PS：  
感谢 [travis-ci](https://travis-ci.org/) 和 [appveyor](https://ci.appveyor.com) 免费提供了持续集成环境。

:mortar_board: Developer Guide
=====================================

如何设置`JVM`的`DNS Cache`
----------------------------------

`JVM`的`DNS Cache`维护在类`InetAddress`的私有字段中，通过反射来设置，具体实现参见

- [`InetAddressCacheUtilCommons.java`](../../library/src/main/java/com/alibaba/dcm/internal/InetAddressCacheUtilCommons.java)
- [`InetAddressCacheUtilForOld.java`](../../library/src/main/java/com/alibaba/dcm/internal/InetAddressCacheUtilForOld.java)
- [`InetAddressCacheUtilForNew.java`](../../library/src/main/java/com/alibaba/dcm/internal/InetAddressCacheUtilForNew.java)

注意设置`JVM`的`DNS Cache`的线程安全问题
----------------------------------

`JVM`的`DNS Cache`显然是全局共用的，所以设置保证是线程安全的没有并发问题。

以`JDK 8`为例，通过查看类`InetAddress`的实现可以确定：通过以`addressCache`字段为锁的`synchronized`块来保证线程安全。

其中关键代码如下：

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

[`InetAddressCacheUtilForOld`](../../library/src/main/java/com/alibaba/dcm/internal/InetAddressCacheUtilForOld.java)类中对`DNS Cache`的读写也一致地加了以`addressCache`为锁的`synchronized`块，以保证线程安全。

需要测试不同版本`JDK`
----------------------------------

本库实现使用了`JDK`的非公开`API`，不同`JDK`实现会不一样，即需要有兼容逻辑，并对不同版本`JDK`进行测试，以保证功能。

目前测试包含`JDK`版本参见【经过测试的`JDK`】一节。

:books: 相关资料
=====================================

- [tanhaichao](https://github.com/tanhaichao)的[`javahost`项目](https://github.com/tanhaichao/javahost)，
    该项目的[使用文档](http://leopard.io/modules/javahost)。  
    本项目如何设置`Java DNS Cache`的解法来自该项目。刚开始在持续集成项目中碰到`host`绑定的问题时，也是使用该项目来解决的 :+1:
- 类`InetAddress`的源代码：
    - `JDK 6`的[`InetAddress`](https://hg.openjdk.java.net/jdk6/jdk6/jdk/file/8deef18bb749/src/share/classes/java/net/InetAddress.java#l739)
    - `JDK 7`的[`InetAddress`](https://hg.openjdk.java.net/jdk7u/jdk7u/jdk/file/4dd5e486620d/src/share/classes/java/net/InetAddress.java#l742)
    - `JDK 8`的[`InetAddress`](https://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/45e4e636b757/src/share/classes/java/net/InetAddress.java#l748)
    - `JDK 9`的[`InetAddress`](https://hg.openjdk.java.net/jdk9/jdk9/jdk/file/65464a307408/src/java.base/share/classes/java/net/InetAddress.java#l783)
    - `JDK 11`的[`InetAddress`](https://hg.openjdk.java.net/jdk/jdk11/file/1ddf9a99e4ad/src/java.base/share/classes/java/net/InetAddress.java#l787)
- [`JVM Networking Properties` - `java docs`](http://docs.oracle.com/javase/8/docs/technotes/guides/net/properties.html)
- [`java dns`解析缓存之源码解析](http://rongmayisheng.com/?p=1006)，写得很完整，源码解析。给出值得注意的结论：
    - 打开`Java`中的`SecurityManager`，`DNS`缓存将不会失效。
    - 否则，可访问的`DNS`解析缺省缓存30秒，不可访问的`DNS`解析缺省缓存10秒。
- [关于`jvm dns cache`(域名缓存时间)](https://nigelzeng.iteye.com/blog/1704052)，给出“对于多条A记录是采用什么策略返回`IP`”的结论：
    - 在缓存有效期内，取到的`IP`永远是缓存中全部A记录的第一条，并没有轮循之类的策略。
    - 缓存失效之后重新进行DNS解析，因为每次域名解析返回的A记录顺序会发生变化(`dig www.google.com`测试可见)，所以缓存中的数据顺序也变了，取到的`IP`也变化。
- [通过`JAVA`反射修改`JDK 1.6`当中`DNS`缓存内容](http://www.tuicool.com/articles/auYzui)，给出了设置`DNS`缓存在性能测试下使用的场景。
- [java InetAddress 的dns cache问题](http://www.blogjava.net/jjwwhmm/archive/2008/07/09/213685.html)，
说明`HttpClient`需要重新创建`GetMethod`/`PostMethod`对象以使设置`DNS`生效问题。
- [Domain Name System - wikipedia](http://en.wikipedia.org/wiki/Domain_Name_System)
- `Java DNS` FAQ
    - [`Java DNS cache` viewer - stackoverflow](http://stackoverflow.com/questions/1835421/java-dns-cache-viewer)
    - [Disable `DNS caching`](http://www.rgagnon.com/javadetails/java-0445.html)
    - [FileOutput Node - Java DNS caching pitfall - quick clarification and tips](https://www.ibm.com/developerworks/community/blogs/aimsupport/entry/fileoutput_node_dns_caching_pitfall?lang=en)
