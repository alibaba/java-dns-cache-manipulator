Java Dns Cache Manipulator(DCM)
=========================

[![Build Status](https://travis-ci.org/alibaba/java-dns-cache-manipulator.svg?branch=master)](https://travis-ci.org/alibaba/java-dns-cache-manipulator) [![Coverage Status](https://coveralls.io/repos/alibaba/java-dns-cache-manipulator/badge.svg?branch=master)](https://coveralls.io/r/alibaba/java-dns-cache-manipulator?branch=master) 
[![GitHub issues](https://img.shields.io/github/issues/alibaba/java-dns-cache-manipulator.svg)](https://github.com/alibaba/java-dns-cache-manipulator/issues) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.alibaba/dns-cache-manipulator/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.alibaba/dns-cache-manipulator/) [![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

:point_right: 通过代码直接设置`Java`的`DNS`（实际上设置的是`DNS Cache`），支持`JDK 6+`。

:wrench: 功能
----------------------------

- 设置/重置`DNS`（不会再去`Lookup DNS`）
    - 可以设置单条
    - 或是通过`Properties`文件批量设置
- 查看`DNS Cache`内容
- 删除一条`DNS Cache`（即重新`Lookup DNS`）
- 清空`DNS Cache`（即所有的域名重新`Lookup DNS`）

:art: 需求场景
----------------------

1. 一些库中写死了连接域名，需要通过修改`host`文件绑定才能做测试。结果是：
    - 自动持续集成的机器上一般同学是没有权限去修改`host`文件的，导致项目不能持续集成。  
        实际上是因为这点，催生这个库的需求。 :persevere::gun:
    - 单元测试需要每个开发都在开发机上做绑定，增加了依赖的配置操作且繁琐重复。
2. `Java`的`DNS`缺省是不会失效的。  
    如果域名绑定的`IP`变了，可以通过这个库重置`DNS`，作为一个临时的手段（***强烈不推荐***）。  
    当然往往进行要先有能执行入口，比如远程调用或是[`jvm-ssh-groovy-shell`](https://github.com/palominolabs/jvm-ssh-groovy-shell)。

:busts_in_silhouette: User Guide
=====================================

通过类[`DnsCacheManipulator`](src/main/java/com/alibaba/dcm/DnsCacheManipulator.java)设置`DNS`。

### 直接设置

```java
DnsCacheManipulator.setDnsCache("www.hello-world.com", "192.168.10.113");

// 之后Java代码中使用到域名都会解析成上面指定的IP。
// 下面是一个简单获取域名对应的IP，演示一下：

String ip = InetAddress.getByName("www.hello-world.com").getHostAddress();
// ip = "192.168.10.113"
```

### 通过`dns-cache.properties`文件批量配置

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
public void beforeClass() throws Exception {
    DnsCacheManipulator.loadDnsCacheConfig();
}
```

更多详细功能参见类[`DnsCacheManipulator`](src/main/java/com/alibaba/dcm/DnsCacheManipulator.java)的文档说明。

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
    <version>1.0.0</version>
</dependency>
```

可以在[search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.alibaba%22%20AND%20a%3A%22dns-cache-manipulator%22)查看可用的版本。

:mortar_board: Developer Guide
=====================================

### 如何修改`JVM`的`DNS Cache`

`JVM`的`DNS Cache`维护在类`InetAddress`的`addressCache`私有字段中，通过反射来修改，
具体参见[`InetAddressCacheUtil`](src/main/java/com/alibaba/dcm/internal/InetAddressCacheUtil.java)。

### 注意修改`JVM`的`DNS Cache`的线程安全问题

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

### 相关资料

- [tanhaichao](https://github.com/tanhaichao)的[`javahost`项目](https://github.com/tanhaichao/javahost)，
    该项目的[使用文档](http://leopard.io/modules/javahost)。  
    本项目如何设置`Java DNS Cache`的解法来自该项目。刚开始在持续集成项目中碰到`host`绑定的问题时，也是使用该项目来解决的 :+1:
- 类`InetAddress`的源代码：
    - `JDK 6`的[`InetAddress`](http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/6-b27/java/net/InetAddress.java#InetAddress.CacheEntry)
    - `JDK 7`的[`InetAddress`](http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/7-b147/java/net/InetAddress.java#InetAddress.CacheEntry)
    - `JDK 8`的[`InetAddress`](http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/8-b132/java/net/InetAddress.java#InetAddress.CacheEntry)
