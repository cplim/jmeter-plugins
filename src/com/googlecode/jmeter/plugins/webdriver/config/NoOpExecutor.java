package com.googlecode.jmeter.plugins.webdriver.config;

import org.openqa.selenium.JavascriptExecutor;

/**
 * Returned if/when the {@link org.openqa.selenium.WebDriver} does not implement the {@link JavascriptExecutor}
 * interface, and will return null for all methods invoked on its intances.
 */
class NoOpExecutor implements JavascriptExecutor {

    private static final NoOpExecutor instance = new NoOpExecutor();

    public static JavascriptExecutor getInstance() {
        return instance;
    }

    private NoOpExecutor() {
    }

    @Override
    public Object executeScript(String s, Object... objects) {
        return null;
    }

    @Override
    public Object executeAsyncScript(String s, Object... objects) {
        return null;
    }
}
