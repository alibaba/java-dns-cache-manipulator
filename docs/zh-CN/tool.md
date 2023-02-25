Java Dns Cache Manipulator Tool
=================================

<p align="center">
<a href="https://ci.appveyor.com/project/oldratlee/java-dns-cache-manipulator"><img src="https://img.shields.io/appveyor/ci/oldratlee/java-dns-cache-manipulator/main?logo=appveyor&logoColor=white" alt="Build Status"></a>
<a href="https://coveralls.io/github/alibaba/java-dns-cache-manipulator?branch=main"><img src="https://img.shields.io/coveralls/github/alibaba/java-dns-cache-manipulator/main?logo=coveralls&logoColor=white" alt="Coveralls branch"></a>
<a href="https://codeclimate.com/github/alibaba/java-dns-cache-manipulator/maintainability"><img src="https://api.codeclimate.com/v1/badges/80e64dc9160cf6f62080/maintainability" alt="Maintainability"></a>
<a href="https://openjdk.java.net/"><img src="https://img.shields.io/badge/Java-6+-green?logo=openjdk&logoColor=white" alt="JDK support"></a>
<a href="https://www.apache.org/licenses/LICENSE-2.0.html"><img src="https://img.shields.io/github/license/alibaba/java-dns-cache-manipulator?color=4D7A97" alt="License"></a>
<a href="https://alibaba.github.io/java-dns-cache-manipulator/apidocs/"><img src="https://img.shields.io/github/release/alibaba/java-dns-cache-manipulator?label=javadoc&color=3d7c47&logo=microsoft-academic&logoColor=white" alt="Javadocs"></a>
<a href="https://search.maven.org/artifact/com.alibaba/dns-cache-manipulator"><img src="https://img.shields.io/maven-central/v/com.alibaba/dns-cache-manipulator?color=2d545e&logo=apache-maven&logoColor=white" alt="Maven Central"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/releases"><img src="https://img.shields.io/github/release/alibaba/java-dns-cache-manipulator.svg" alt="GitHub release"></a>
<a href="https://gitter.im/alibaba/java-dns-cache-manipulator?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge"><img src="https://img.shields.io/gitter/room/alibaba/java-dns-cache-manipulator?color=46BC99&logo=gitter&logoColor=white" alt="Chat at gitter.im"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/stargazers"><img src="https://img.shields.io/github/stars/alibaba/java-dns-cache-manipulator" alt="GitHub Stars"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/fork"><img src="https://img.shields.io/github/forks/alibaba/java-dns-cache-manipulator" alt="GitHub Forks"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/network/dependents"><img src="https://badgen.net/github/dependents-repo/alibaba/java-dns-cache-manipulator?label=user%20repos" alt="user repos"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/issues"><img src="https://img.shields.io/github/issues/alibaba/java-dns-cache-manipulator" alt="GitHub issues"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/graphs/contributors"><img src="https://img.shields.io/github/contributors/alibaba/java-dns-cache-manipulator" alt="GitHub Contributors"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator"><img src="https://img.shields.io/github/repo-size/alibaba/java-dns-cache-manipulator" alt="GitHub repo size"></a>
<a href="https://github.com/alibaba/java-dns-cache-manipulator/releases/download/v1.8.1/dcm-1.8.1.tar.gz"><img src="https://img.shields.io/github/downloads/alibaba/java-dns-cache-manipulator/v1.8.1/dcm-1.8.1.tar.gz.svg?logoColor=white&logo=DocuSign" alt="GitHub release download - dcm.tar.gz)"></a>
</p>

:point_right: 设置/查看 运行中`JVM`进程的`DNS Cache`，支持`IPv6`。

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [:wrench: 功能](#wrench-%E5%8A%9F%E8%83%BD)
- [:busts_in_silhouette: User Guide](#busts_in_silhouette-user-guide)
    - [下载](#%E4%B8%8B%E8%BD%BD)
    - [设置/重置`DNS`](#%E8%AE%BE%E7%BD%AE%E9%87%8D%E7%BD%AEdns)
    - [查看`DNS Cache`内容](#%E6%9F%A5%E7%9C%8Bdns-cache%E5%86%85%E5%AE%B9)
    - [删除/清空`DNS Cache`](#%E5%88%A0%E9%99%A4%E6%B8%85%E7%A9%BAdns-cache)
    - [设置/查看`JVM`缺省的`DNS`的缓存时间](#%E8%AE%BE%E7%BD%AE%E6%9F%A5%E7%9C%8Bjvm%E7%BC%BA%E7%9C%81%E7%9A%84dns%E7%9A%84%E7%BC%93%E5%AD%98%E6%97%B6%E9%97%B4)
- [:books: 相关资料](#books-%E7%9B%B8%E5%85%B3%E8%B5%84%E6%96%99)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

:wrench: 功能
=================================

- 设置/重置`DNS`
- 查看`DNS Cache`内容
- 删除一条`DNS Cache`（即重新`Lookup DNS`）
- 清空`DNS Cache`（即所有的域名重新`Lookup DNS`）
- 设置/查看`JVM`缺省的`DNS`的缓存时间

:busts_in_silhouette: User Guide
=================================

下载
----------

[![GitHub release download - dcm.tar.gz)](https://img.shields.io/github/downloads/alibaba/java-dns-cache-manipulator/v1.8.1/dcm-1.8.1.tar.gz.svg?logoColor=white&logo=DocuSign)](https://github.com/alibaba/java-dns-cache-manipulator/releases/download/v1.8.1/dcm-1.8.1.tar.gz) 下载文件`dcm-x.y.z.tar.gz`。

解压后，运行`bin`目录下的`dcm`。

```bash
$ dcm -h
usage: Options
 -h,--help             show help
 -p,--pid <arg>        java process id to attach
```

设置/重置`DNS`
---------------

```bash
# 对进程ID是12345的Java进程，设置域名 foo.com IP为 1.1.1.1
$ dcm -p 12345 set foo.com 1.1.1.1
# 对进程ID是12345的Java进程，设置域名 bar.com IP为 2.2.2.2 3.3.3.3(可以多个IP)
$ dcm -p 12345 set bar.com 2.2.2.2 3.3.3.3
```

查看`DNS Cache`内容
---------------

查看单条

```bash
# 对进程ID是12345的Java进程，获取域名 aliyun.com 的DNS条目信息
$ dcm -p 12345 get aliyun.com
aliyun.com 220.181.57.217,180.149.132.47,123.125.114.144 2015-06-05T18:56:09.635+0800
# 输出格式是 域名 IP列表（可能有多个IP） 失效时间
```

查看全部

```bash
$ dcm -p 12345 list
Dns cache:
    bar.com 2.2.2.2,3.3.3.3 292278994-08-17T15:12:55.807+0800
    aliyun.com 220.181.57.217,180.149.132.47,123.125.114.144 2015-06-05T19:00:30.514+0800
    foo.com 1.1.1.1 292278994-08-17T15:12:55.807+0800
Dns negative cache:
# 输出包含Cache 和 Negative Cache的条目。条目缩进了4个空格。
# 上面的示例中，Negative Cache为空。
```

删除/清空`DNS Cache`
---------------

```bash
# 删除一条DNS
$ dcm -p 12345 rm aliyun.com
# 清除所有DNS Cache
$ dcm -p 12345 clear
```

设置/查看`JVM`缺省的`DNS`的缓存时间
---------------

```bash
# 查看缓存时间，单位秒。-1表示永远缓存，0表示不缓存
$ dcm -p 12345 getPolicy
30
# 设置缓存时间
$ dcm --pid 12345 setPolicy 5
# 查看未命中条目的缓存时间，单位秒。-1表示永远缓存，0表示不缓存
$ dcm -p 12345 getNegativePolicy
10
# 设置未命中条目的缓存时间
$ dcm -p 12345 setNegativePolicy 0
```

:books: 相关资料
=================================

* [Java Agent规范](http://docs.oracle.com/javase/7/docs/api/java/lang/instrument/package-summary.html)
* [Java SE 6 新特性: Instrumentation 新功能](http://www.ibm.com/developerworks/cn/java/j-lo-jse61/)
* [Creation, dynamic loading and instrumentation with javaagents](http://dhruba.name/2010/02/07/creation-dynamic-loading-and-instrumentation-with-javaagents/)
* [JavaAgent加载机制分析](http://nijiaben.iteye.com/blog/1847212)
