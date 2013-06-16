package com.googlecode.jmeter.plugins.webdriver.config;

import org.junit.Test;
import org.openqa.selenium.JavascriptExecutor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class NoOpExecutorTest {
    @Test
    public void shouldAlwaysReturnNullWhenMethodsAreInvoked() {
        JavascriptExecutor executor = NoOpExecutor.getInstance();

        assertThat(executor.executeScript(null, null), is(nullValue()));
        assertThat(executor.executeAsyncScript(null, null), is(nullValue()));
    }
}
