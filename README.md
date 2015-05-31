Java Dns Cache Manipulator(DCM)
=========================

[![Build Status](https://travis-ci.org/alibaba/java-dns-cache-manipulator.svg?branch=master)](https://travis-ci.org/alibaba/java-dns-cache-manipulator)
[![Windows Build Status](https://img.shields.io/appveyor/ci/oldratlee/java-dns-cache-manipulator/master.svg?label=windows%20build)](https://ci.appveyor.com/project/oldratlee/java-dns-cache-manipulator)
[![Coverage Status](https://coveralls.io/repos/alibaba/java-dns-cache-manipulator/badge.svg?branch=master)](https://coveralls.io/r/alibaba/java-dns-cache-manipulator?branch=master)  
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.alibaba/dns-cache-manipulator/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.alibaba/dns-cache-manipulator/)
[![GitHub release](https://img.shields.io/github/release/alibaba/java-dns-cache-manipulator.svg)](https://github.com/alibaba/java-dns-cache-manipulator/releases)
[![Dependency Status](https://www.versioneye.com/user/projects/553a2f981d2989f7ee0000a7/badge.svg?style=flat)](https://www.versioneye.com/user/projects/553a2f981d2989f7ee0000a7)  
[![GitHub issues](https://img.shields.io/github/issues/alibaba/java-dns-cache-manipulator.svg)](https://github.com/alibaba/java-dns-cache-manipulator/issues)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

:point_right: 通过代码直接设置`Java`的`DNS`（实际上设置的是`DNS Cache`），支持`JDK 6+`。

:wrench: 功能
=====================================

- 设置/重置`DNS`（不会再去`Lookup DNS`）
    - 可以设置单条
    - 或是通过`Properties`文件批量设置
- 查看`DNS Cache`内容
- 删除一条`DNS Cache`（即重新`Lookup DNS`）
- 清空`DNS Cache`（即所有的域名重新`Lookup DNS`）
- 修改/查看`JVM`缺省的`DNS`的缓存时间

:art: 需求场景
=====================================

1. 一些库中写死了连接域名，需要通过修改`host`文件绑定才能做测试。结果是：
    - 自动持续集成的机器上一般同学是没有权限去修改`host`文件的，导致项目不能持续集成。  
        实际上是因为这点，催生这个库的需求。 :persevere::gun:
    - 单元测试需要每个开发都在开发机上做`host`绑定，增加了依赖的配置操作且繁琐重复。
1. 一些功能需要域名作为输入参数，如使用`HTTP`请求的网关 或是 有域名检查限制的`Web`应用。  
    这种情况下，让需要让一个域名连接到测试机器的`IP`上，或是 使用一个还不存在的域名但又不想或不能去配置`DNS`。
1. 在性能测试时，
    - 不去做网络的`DNS Lookup`（`DNS`解析消耗），这样使得压测更加关注服务器响应，压测更充分反应出实现代码的性能。
    - 可以动态修改`DNS`缓存，无需修改`host`文件和`http`链接等不灵活的方式。
    - 一个`JVM`进程可以对应一套域名绑定，相互之间不影响，可以实现多场景，多域名绑定的需求压测。
1. 打开`Java`中的`SecurityManager`时（如在`Web`容器`Tomcat`中的`Web`应用），`Java`的`DNS`缺省是不会失效的。
    如果域名绑定的`IP`变了，可以通过这个库重置`DNS`，作为一个临时的手段（***强烈不推荐***）。  
    当然往往进行要先有能执行入口，比如远程调用或是[`jvm-ssh-groovy-shell`](https://github.com/palominolabs/jvm-ssh-groovy-shell)。

:busts_in_silhouette: User Guide
=====================================

通过类[`DnsCacheManipulator`](src/main/java/com/alibaba/dcm/DnsCacheManipulator.java)设置`DNS`。

直接设置
----------------------------------

```java
DnsCacheManipulator.setDnsCache("www.hello-world.com", "192.168.10.113");

// 之后Java代码中使用到域名都会解析成上面指定的IP。
// 下面是一个简单获取域名对应的IP，演示一下：

String ip = InetAddress.getByName("www.hello-world.com").getHostAddress();
// ip = "192.168.10.113"
```

通过`dns-cache.properties`文件批量配置
----------------------------------

在代码测试中，会期望把域名绑定写在配置文件。

使用方式如下：

在`ClassPath`上，提供文件`dns-cache.properties`：

```bash
# 配置格式：
# <host> = <ip>
www.hello-world.com=192.168.10.113
www.foo.com=192.168.10.2
```

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

查看`JVM DNS Cache`
----------------------------------

```java
DnsCache dnsCache = DnsCacheManipulator.getWholeDnsCache()
System.out.println(dnsCache);
```

修改/查看`JVM`缺省的`DNS`的缓存时间
----------------------------------

```java
// 查看缓存时间，单位秒。-1表示永远缓存，0表示不缓存
int cachePolicy = DnsCacheManipulator.getDnsCachePolicy();
// 查看缓存时间
DnsCacheManipulator.setDnsCachePolicy(2);

// 查看未命中条目的缓存时间
DnsCacheManipulator.getDnsNegativeCachePolicy()
// 修改未命中条目的缓存时间
DnsCacheManipulator.setDnsNegativeCachePolicy(0);
```

使用注意
----------------------------------

对于已经完成解析保存了`IP`的逻辑，修改`JVM DNS`缓存，不会生效！可以重新创建 连接或`Client`解决。

如对于`HttpClient`:

```java
HttpClient client = new HttpClient();
GetMethod m1 = new GetMethod("http://www.baidu.com");
client.executeMethod(m1);
String content = m1.getResponseBodyAsString();

// 修改DNS，绑定到自己的机器
DnsCacheManipulator.setDnsCache("www.baidu.com", "192.168.10.2");

// 重新执行m1，仍然是老结果
client.executeMethod(m1);
String content = m1.getResponseBodyAsString();

// 重新创建GetMethod，才能得到自己机器上的结果
GetMethod m2 = new GetMethod("http://www.baidu.com");
client.executeMethod(m2);
content = m2.getResponseBodyAsString();
```

更多详细功能
----------------------------------

参见类[`DnsCacheManipulator`](src/main/java/com/alibaba/dcm/DnsCacheManipulator.java)的文档说明。

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
    <version>1.2.0</version>
</dependency>
```

可以在[search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.alibaba%22%20AND%20a%3A%22dns-cache-manipulator%22)查看可用的版本。

:mortar_board: Developer Guide
=====================================

如何修改`JVM`的`DNS Cache`
----------------------------------

`JVM`的`DNS Cache`维护在类`InetAddress`的`addressCache`私有字段中，通过反射来修改，
具体参见[`InetAddressCacheUtil`](src/main/java/com/alibaba/dcm/internal/InetAddressCacheUtil.java)。

注意修改`JVM`的`DNS Cache`的线程安全问题
----------------------------------

`JVM`的`DNS Cache`显然是全局共用的，所以修改需要同步以保证没有并发问题。

通过查看类`InetAddress`的实现可以确定：通过以`addressCache`字段为锁的`synchronized`块来保证线程安全。

其中关键代码（`JDK 7`）如下：

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

[`InetAddressCacheUtil`](src/main/java/com/alibaba/dcm/internal/InetAddressCacheUtil.java)类中对`DNS Cache`的读写也一致地加了以`addressCache`为锁的`synchronized`块，以保证线程安全。

:books: 相关资料
=====================================

- [tanhaichao](https://github.com/tanhaichao)的[`javahost`项目](https://github.com/tanhaichao/javahost)，
    该项目的[使用文档](http://leopard.io/modules/javahost)。  
    本项目如何设置`Java DNS Cache`的解法来自该项目。刚开始在持续集成项目中碰到`host`绑定的问题时，也是使用该项目来解决的 :+1:
- 类`InetAddress`的源代码：
    - `JDK 6`的[`InetAddress`](http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/6-b27/java/net/InetAddress.java#InetAddress.CacheEntry)
    - `JDK 7`的[`InetAddress`](http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/7-b147/java/net/InetAddress.java#InetAddress.CacheEntry)
    - `JDK 8`的[`InetAddress`](http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/8-b132/java/net/InetAddress.java#InetAddress.CacheEntry)
- [`JVM Networking Properties` - `java docs`](http://docs.oracle.com/javase/8/docs/technotes/guides/net/properties.html)
- [`java dns`解析缓存之源码解析](http://rongmayisheng.com/post/java-dns%E7%BC%93%E5%AD%98%E6%BA%90%E7%A0%81%E8%A7%A3%E6%9E%90)，写得很完整，源码解析。给出值得注意的结论：
    - 打开`Java`中的`SecurityManager`，`DNS`缓存将不会生效。
    - 否则，可访问的`DNS`解析缺省缓存30秒，不可访问的`DNS`解析缺省缓存10秒。
- [关于`jvm dns cache`(域名缓存时间)](http://www.piao2010.com/%E5%85%B3%E4%BA%8Ejvm-dns-cache-%E5%9F%9F%E5%90%8D%E7%BC%93%E5%AD%98%E6%97%B6%E9%97%B4)，给出“对于多条A记录是采用什么策略返回`IP`”的结论：  
    - 在缓存有效期内，取到的`IP`永远是缓存中全部A记录的第一条，并没有轮循之类的策略。
    - 缓存失效之后重新进行DNS解析，因为每次域名解析返回的A记录顺序会发生变化(`dig www.google.com`测试可见)，所以缓存中的数据顺序也变了，取到的`IP`也变化。
- [通过`JAVA`反射修改`JDK 1.6`当中`DNS`缓存内容](http://www.tuicool.com/articles/auYzui)，给出了修改`DNS`缓存在性能测试下使用的场景。
- [java InetAddress 的dns cache问题](http://www.blogjava.net/jjwwhmm/archive/2008/07/09/213685.html)，
说明`HttpClient`需要重新创建`GetMethod`/`PostMethod`对象以使设置`DNS`生效问题。
- [Domain Name System - wikipedia](http://en.wikipedia.org/wiki/Domain_Name_System)
- `Java DNS` FAQ
    - [`Java DNS cache` viewer - stackoverflow](http://stackoverflow.com/questions/1835421/java-dns-cache-viewer)
    - [Disable `DNS caching`](http://www.rgagnon.com/javadetails/java-0445.html)
    - [FileOutput Node - Java DNS caching pitfall - quick clarification and tips](https://www.ibm.com/developerworks/community/blogs/aimsupport/entry/fileoutput_node_dns_caching_pitfall?lang=en)
