# Java 25 兼容性修复：InetAddressCachePolicy 字段变更

## 问题描述

项目测试在 Java 21 上通过，但在 Java 25 上失败。错误信息为：

```
NoSuchFieldException: set
```

## 问题排查

### 1. 对比 Java 21 和 Java 25 的 InetAddressCachePolicy 类结构

使用 `javap -p` 命令查看两个版本的类结构：

**Java 21:**
```bash
$ JAVA_HOME=/Users/jerry/.sdkman/candidates/java/21.0.10-tem/bin/java -p sun.net.InetAddressCachePolicy
```

输出：
```
private static boolean propertySet;
private static boolean propertyNegativeSet;
```

**Java 25:**
```bash
$ JAVA_HOME=/Users/jerry/.sdkman/candidates/java/25.0.2-tem/bin/java -p sun.net.InetAddressCachePolicy
```

输出：
```
# 没有 set / propertySet 字段
# 没有 negativeSet / propertyNegativeSet 字段
```

### 2. 字段对比表

| 字段 | Java 21 及之前 | Java 25 |
|------|---------------|---------|
| `cachePolicy` | ✅ 存在 | ✅ 存在 |
| `negativeCachePolicy` | ✅ 存在 | ✅ 存在 |
| `staleCachePolicy` | ✅ 存在 | ✅ 存在 |
| `propertySet` / `set` | ✅ 存在 | ❌ **已移除** |
| `propertyNegativeSet` / `negativeSet` | ✅ 存在 | ❌ **已移除** |

### 3. 验证直接设置字段是否生效

创建测试类验证在 Java 25 中直接设置 `cachePolicy` 字段是否生效：

```java
Field cachePolicyField = InetAddressCachePolicy.class.getDeclaredField("cachePolicy");
cachePolicyField.setAccessible(true);
cachePolicyField.setInt(null, 345);
System.out.println(InetAddressCachePolicy.get()); // 输出 345
```

**测试结果：**

| Java 版本 | 直接设置 cachePolicy | 通过 get() 读取 |
|-----------|---------------------|-----------------|
| Java 21 | ✅ 成功 | ✅ 返回设置值 |
| Java 25 | ✅ 成功 | ✅ 返回设置值 |

**结论：** Java 25 中直接设置 `cachePolicy` 和 `negativeCachePolicy` 字段是有效的，不需要设置 `set` 标志字段。

## 根本原因

原代码在 `InetAddressCacheUtilCommons.java` 中：

1. 尝试获取 `set` 或 `propertySet` 字段，如果都不存在则抛出 `NoSuchFieldException`
2. 获取字段后没有处理 `null` 情况，直接调用 `setAccessible(true)` 和 `setBoolean()`
3. 在 `setCachePolicy0()` 方法中，没有检查字段是否为 `null` 就直接设置

## 解决方案

### 修改 1：初始化字段时处理 Java 25+ 的情况

```java
try {
    f = clazz.getDeclaredField("propertySet");
} catch (NoSuchFieldException e) {
    try {
        f = clazz.getDeclaredField("set");
    } catch (NoSuchFieldException ex) {
        // Java 25+ does not have 'set' field, leave it as null
        f = null;
    }
}
if (f != null) {
    f.setAccessible(true);
}
setFiledOfInetAddressCachePolicy = f;
```

### 修改 2：设置值时检查字段是否为 null

```java
private static void setCachePolicy0(boolean isNegative, int seconds)
        throws NoSuchFieldException, IllegalAccessException {
    if (seconds < 0) {
        seconds = -1;
    }

    initFieldsOfInetAddressCachePolicy();

    synchronized (InetAddressCachePolicy.class) {
        if (isNegative) {
            negativeCachePolicyFiledOfInetAddressCachePolicy.setInt(null, seconds);
            if (negativeSetOfInetAddressCachePolicy != null) {
                negativeSetOfInetAddressCachePolicy.setBoolean(null, true);
            }
        } else {
            cachePolicyFiledOfInetAddressCachePolicy.setInt(null, seconds);
            if (setFiledOfInetAddressCachePolicy != null) {
                setFiledOfInetAddressCachePolicy.setBoolean(null, true);
            }
        }
    }
}
```

### 附加修复：setAccessible(true)

原代码在找到 `propertySet`/`set` 字段后没有调用 `setAccessible(true)`，导致在 Java 21 上也出现 `IllegalAccessException`。修复后确保在字段不为 null 时都调用 `setAccessible(true)`。

## 测试结果

| Java 版本 | 测试前 | 修复后 |
|-----------|--------|--------|
| Java 21.0.10 | ✅ 50/50 通过 | ✅ 50/50 通过 |
| Java 25.0.2 | ❌ 错误：NoSuchFieldException | ✅ 50/50 通过 |

## 核心原理

`InetAddressCachePolicy` 中的 `set` 标志字段（`propertySet`/`set`）的原始设计目的是：
- 标记是否已经通过系统属性或配置文件设置过缓存策略
- 防止被重复设置

但在通过反射直接修改字段值的场景下：
- 直接修改 `cachePolicy` 字段值即可生效
- `set` 标志字段只影响某些特定代码路径的判断
- `InetAddressCachePolicy.get()` 方法直接返回 `cachePolicy` 字段的值

因此，在 Java 25+ 中，即使没有 `set` 标志字段，直接设置 `cachePolicy` 字段依然可以正常工作。

## 相关文件

- `library/src/main/java/com/alibaba/dcm/internal/InetAddressCacheUtilCommons.java`

## 参考资料

- OpenJDK 21 源码：https://github.com/openjdk/jdk/blob/jdk-21-ga/src/java.base/share/classes/sun/net/InetAddressCachePolicy.java
- OpenJDK 25 源码：https://github.com/openjdk/jdk/blob/jdk-25-ga/src/java.base/share/classes/sun/net/InetAddressCachePolicy.java
