package com.alibaba.dcm.internal

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.apache.commons.lang3.JavaVersion
import org.apache.commons.lang3.SystemUtils

class JavaVersionUtilTests : FunSpec({
    test("JavaVersionUtil.isJavaVersionAtMost8") {
        JavaVersionUtil.isJavaVersionAtMost8() shouldBe SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_1_8)
    }
})
