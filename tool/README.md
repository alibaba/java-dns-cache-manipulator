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

运行脚本`dcm.sh`/`dcm.bat`，执行操作。

```bash
$ ./dcm.sh -h
usage: Options
 -a,--action <arg>     action
 -h,--help             show help
 -p,--pid <arg>        java process id to attach
```

设置/重置`DNS`
---------------

```bash
$ ./dcm.sh -p 12345 set baidu.com 1.1.1.1
```

查看`DNS Cache`内容
---------------

查看单条

```bash
$ ./dcm.sh -p 12345 get baidu.com
```

查看全部

```bash
$ ./dcm.sh -p 12345 list
```

清空`DNS Cache`
---------------

```bash
$ ./dcm.sh -p 12345 clear
```

修改/查看`JVM`缺省的`DNS`的缓存时间
---------------

```bash
# 查看缓存时间，单位秒。-1表示永远缓存，0表示不缓存
$ ./dcm.sh -p 12345 getPolicy

# 设置缓存时间
$ ./dcm.sh --pid 12345 setPolicy 5

# 查看未命中条目的缓存时间，单位秒。-1表示永远缓存，0表示不缓存
$ ./dcm.sh -p 12345 getNegativePolicy

# 修改未命中条目的缓存时间
$ ./dcm.sh -p 12345 setNegativePolicy 0
```

:mortar_board: Developer Guide
=================================

TODO

:books: 相关资料
=================================

* [Java Agent规范](http://docs.oracle.com/javase/7/docs/api/java/lang/instrument/package-summary.html)
* [Java SE 6 新特性: Instrumentation 新功能](http://www.ibm.com/developerworks/cn/java/j-lo-jse61/)
* [Creation, dynamic loading and instrumentation with javaagents](http://dhruba.name/2010/02/07/creation-dynamic-loading-and-instrumentation-with-javaagents/)
* [JavaAgent加载机制分析](http://nijiaben.iteye.com/blog/1847212)
