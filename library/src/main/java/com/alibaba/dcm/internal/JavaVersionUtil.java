package com.alibaba.dcm.internal;

/**
 * @author antfling (ding_zhengang at hithinksoft dot com)
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 1.6.0
 */
public final class JavaVersionUtil {
    public static boolean isJdkAtMost8() {
        return JAVA_SPECIFICATION_VERSION_AS_ENUM.atMost(JavaVersion.JAVA_1_8);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // blow source code is copied from commons-lang-3.12.0:
    //
    // https://github.com/apache/commons-lang/blob/rel/commons-lang-3.12.0/src/main/java/org/apache/commons/lang3/SystemUtils.java
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final String JAVA_SPECIFICATION_VERSION = getSystemProperty("java.specification.version");
    private static final JavaVersion JAVA_SPECIFICATION_VERSION_AS_ENUM = JavaVersion.get(JAVA_SPECIFICATION_VERSION);

    @SuppressWarnings({"CommentedOutCode", "SameParameterValue"})
    private static String getSystemProperty(final String property) {
        try {
            return System.getProperty(property);
        } catch (final SecurityException ex) {
            // we are not allowed to look at this property
            // System.err.println("Caught a SecurityException reading the system property '" + property
            //   + "'; the SystemUtils property value will default to null.");
            return null;
        }
    }

    private JavaVersionUtil() {
    }
}
