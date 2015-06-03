Java Dns Cache Manipulator Tool
=================================

[![Build Status](https://travis-ci.org/alibaba/java-dns-cache-manipulator.svg?branch=master)](https://travis-ci.org/alibaba/java-dns-cache-manipulator)
[![Windows Build Status](https://img.shields.io/appveyor/ci/oldratlee/java-dns-cache-manipulator/master.svg?label=windows%20build)](https://ci.appveyor.com/project/oldratlee/java-dns-cache-manipulator)
[![Coverage Status](https://coveralls.io/repos/alibaba/java-dns-cache-manipulator/badge.svg?branch=master)](https://coveralls.io/r/alibaba/java-dns-cache-manipulator?branch=master)  
[![GitHub release](https://img.shields.io/github/release/alibaba/java-dns-cache-manipulator.svg)](https://github.com/alibaba/java-dns-cache-manipulator/releases)
[![Dependency Status](https://www.versioneye.com/user/projects/553a2f981d2989f7ee0000a7/badge.svg?style=flat)](https://www.versioneye.com/user/projects/553a2f981d2989f7ee0000a7)  
[![GitHub issues](https://img.shields.io/github/issues/alibaba/java-dns-cache-manipulator.svg)](https://github.com/alibaba/java-dns-cache-manipulator/issues)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

:point_right: 修改/查看 运行中`JVM`进程的`DNS Cache`。

:wrench: 功能
=================================

- 设置/重置`DNS`
- 查看`DNS Cache`内容
- 删除一条`DNS Cache`（即重新`Lookup DNS`）
- 清空`DNS Cache`（即所有的域名重新`Lookup DNS`）
- 修改/查看`JVM`缺省的`DNS`的缓存时间

:busts_in_silhouette: User Guide
=================================

下载
----------

在[项目Release页面](https://github.com/alibaba/java-dns-cache-manipulator/releases)下载文件`dcm-tool-x.x.x.tar.gz`。

解压后，运行`bin`目录下的`dcm`脚本（`Shell脚本`）。

```bash
$ dcm -h
usage: Options
 -h,--help             show help
 -p,--pid <arg>        java process id to attach
```

设置/重置`DNS`
---------------

```bash
# 对进程ID是12345的Java进程，设置域名 baidu.com IP为 1.1.1.1
$ dcm -p 12345 set baidu.com 1.1.1.1
set DONE.
```

查看`DNS Cache`内容
---------------

查看单条

```bash
# 对进程ID是12345的Java进程，获取域名 baidu.com 的DNS条目信息
$ dcm -p 12345 get baidu.com
DnsCacheEntry{host='baidu.com', ips=[1.1.1.1], expiration=292278994-08-17 15:12:55.807+0800}
get DONE.
```

查看全部

```bash
$ dcm -p 12345 list
DnsCache{cache=[DnsCacheEntry{host='bar.com', ips=[1.1.1.1], expiration=292278994-08-17 15:12:55.807+0800}, DnsCacheEntry{host='foo.com', ips=[1.1.1.1], expiration=292278994-08-17 15:12:55.807+0800}, DnsCacheEntry{host='baidu.com', ips=[180.149.132.47, 123.125.114.144, 220.181.57.217], expiration=2015-06-03 17:49:42.077+0800}], negativeCache=[]}
list DONE.
```

清空`DNS Cache`
---------------

```bash
$ dcm -p 12345 clear
clear DONE.
```

修改/查看`JVM`缺省的`DNS`的缓存时间
---------------

```bash
# 查看缓存时间，单位秒。-1表示永远缓存，0表示不缓存
$ dcm -p 12345 getPolicy
30
getPolicy DONE.

# 设置缓存时间
$ dcm --pid 12345 setPolicy 5
setPolicy DONE.

# 查看未命中条目的缓存时间，单位秒。-1表示永远缓存，0表示不缓存
$ dcm -p 12345 getNegativePolicy
10
getNegativePolicy DONE.

# 修改未命中条目的缓存时间
$ dcm -p 12345 setNegativePolicy 0
setNegativePolicy DONE.
```

:books: 相关资料
=================================

* [Java Agent规范](http://docs.oracle.com/javase/7/docs/api/java/lang/instrument/package-summary.html)
* [Java SE 6 新特性: Instrumentation 新功能](http://www.ibm.com/developerworks/cn/java/j-lo-jse61/)
* [Creation, dynamic loading and instrumentation with javaagents](http://dhruba.name/2010/02/07/creation-dynamic-loading-and-instrumentation-with-javaagents/)
* [JavaAgent加载机制分析](http://nijiaben.iteye.com/blog/1847212)
