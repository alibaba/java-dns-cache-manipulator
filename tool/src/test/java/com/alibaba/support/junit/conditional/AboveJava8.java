package com.alibaba.support.junit.conditional;

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.IgnoreCondition;

/**
 * @see [Getting Java version at runtime](https://stackoverflow.com/a/23706899/922688)
 */
public class AboveJava8 implements IgnoreCondition {
    @Override
    public boolean isSatisfied() {
        final String version = System.getProperty("java.specification.version");
        return Double.parseDouble(version) >= 9;
    }
}
