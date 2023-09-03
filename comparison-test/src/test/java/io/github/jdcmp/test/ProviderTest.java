package io.github.jdcmp.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@ParameterizedTest(name = "{0}")
@MethodSource("io.github.jdcmp.test.Providers#providersToTest")
@Retention(RetentionPolicy.RUNTIME)
@interface ProviderTest {

}
