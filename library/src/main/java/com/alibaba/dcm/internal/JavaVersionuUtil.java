package com.alibaba.dcm.internal;

/**
 * @author antfling (ding_zhengang at hithinksoft dot com)
 */
public class JavaVersionuUtil {

    public static final JavaVersion CURRENT_JAVA_VERSION;

    static {
        CURRENT_JAVA_VERSION = parseCurrentJavaVersion();
    }

    private static JavaVersion parseCurrentJavaVersion() {
        String javaVersionTmp = System.getProperty("java.version");
        String[] split = javaVersionTmp.split("\\.", 3);
        String javaVersion = "0";
        if (split.length <= 0) {
            throw new IllegalStateException("get current java version failed");
        } else if (split.length == 1) {
            javaVersion = split[0];
        } else {
            javaVersion = split[0] + '.' + split[1];
        }
        double javaVersionNum = Double.parseDouble(javaVersion);
        JavaVersion currentVersion = JavaVersion.JDK6;
        JavaVersion[] javaVersions = JavaVersion.values();
        for (JavaVersion version : javaVersions) {
            if (version.isLessThenOrEqual(javaVersionNum)) {
                currentVersion = version;
            }
        }
        return currentVersion;
    }

}
