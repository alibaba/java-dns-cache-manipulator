Java Dns Cache Manipulator Tool
=================================

[![Build Status](https://travis-ci.org/alibaba/java-dns-cache-manipulator.svg?branch=v1.5.0)](https://travis-ci.org/alibaba/java-dns-cache-manipulator)
[![Windows Build Status](https://img.shields.io/appveyor/ci/oldratlee/java-dns-cache-manipulator/v1.5.0.svg?label=windows%20build)](https://ci.appveyor.com/project/oldratlee/java-dns-cache-manipulator)
[![Coverage Status](https://coveralls.io/repos/alibaba/java-dns-cache-manipulator/badge.svg?branch=v1.5.0)](https://coveralls.io/r/alibaba/java-dns-cache-manipulator?branch=v1.5.0)  
[![GitHub release](https://img.shields.io/github/release/alibaba/java-dns-cache-manipulator.svg)](https://github.com/alibaba/java-dns-cache-manipulator/releases)
[![Dependency Status](https://www.versioneye.com/user/projects/553a2f981d2989f7ee0000a7/badge.svg?style=flat)](https://www.versioneye.com/user/projects/553a2f981d2989f7ee0000a7)  
[![GitHub issues](https://img.shields.io/github/issues/alibaba/java-dns-cache-manipulator.svg)](https://github.com/alibaba/java-dns-cache-manipulator/issues)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

:point_right: 修改/查看 运行中`JVM`进程的`DNS Cache`，支持`IPv6`。

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
# 对进程ID是12345的Java进程，设置域名 foo.com IP为 1.1.1.1
$ dcm -p 12345 set foo.com 1.1.1.1
# 对进程ID是12345的Java进程，设置域名 bar.com IP为 2.2.2.2 3.3.3.3(可以多个IP)
$ dcm -p 12345 set bar.com 2.2.2.2 3.3.3.3
```

查看`DNS Cache`内容
---------------

查看单条

```bash
# 对进程ID是12345的Java进程，获取域名 baidu.com 的DNS条目信息
$ dcm -p 12345 get baidu.com
baidu.com 220.181.57.217,180.149.132.47,123.125.114.144 2015-06-05T18:56:09.635+0800
# 输出格式是 域名 IP列表（可能有多个IP） ​失效时间
```

查看全部

```bash
$ dcm -p 12345 list
Dns cache:
    bar.com 2.2.2.2,3.3.3.3 292278994-08-17T15:12:55.807+0800
    baidu.com 220.181.57.217,180.149.132.47,123.125.114.144 2015-06-05T19:00:30.514+0800
    foo.com 1.1.1.1 292278994-08-17T15:12:55.807+0800
Dns negative cache:
# 输出包含Cache 和 Negative Cache的条目。条目缩进了4个空格。
# 上面的示例中，Negative Cache为空。
```

删除/清空`DNS Cache`
---------------

```bash
# 删除一条DNS
$ dcm -p 12345 rm baidu.com
# 清除所有DNS Cache
$ dcm -p 12345 clear
```

修改/查看`JVM`缺省的`DNS`的缓存时间
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
# 修改未命中条目的缓存时间
$ dcm -p 12345 setNegativePolicy 0
```

:books: 相关资料
=================================

* [Java Agent规范](http://docs.oracle.com/javase/7/docs/api/java/lang/instrument/package-summary.html)
* [Java SE 6 新特性: Instrumentation 新功能](http://www.ibm.com/developerworks/cn/java/j-lo-jse61/)
* [Creation, dynamic loading and instrumentation with javaagents](http://dhruba.name/2010/02/07/creation-dynamic-loading-and-instrumentation-with-javaagents/)
* [JavaAgent加载机制分析](http://nijiaben.iteye.com/blog/1847212)
