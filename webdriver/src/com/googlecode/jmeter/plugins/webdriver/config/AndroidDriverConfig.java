package com.googlecode.jmeter.plugins.webdriver.config;

import io.selendroid.SelendroidCapabilities;
import io.selendroid.SelendroidDriver;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Sergey Marakhov
 * @author Linh Pham
 */
public class AndroidDriverConfig extends WebDriverConfig<SelendroidDriver> {
    private static final long serialVersionUID = 100L;

    private static final Logger LOGGER = LoggingManager.getLoggerForClass();

    private static final String ANDROID_DRIVER_HOST_PORT = "AndroidDriverConfig.driver_host_port";

    DesiredCapabilities createCapabilities() {
        DesiredCapabilities capabilities = SelendroidCapabilities.android();
        capabilities.setCapability(CapabilityType.PROXY, createProxy());
        return capabilities;
    }

    @Override
    protected SelendroidDriver createBrowser() {
        try {
            return new SelendroidDriver(new URL(getAndroidDriverUrl()), createCapabilities());
        } catch (MalformedURLException e) {
            LOGGER.error("MalformedURLException thrown for invalid URL: " + getAndroidDriverUrl());
            return null;
        } catch (Exception e) {
            LOGGER.error("Exception thrown when constructing Selenium Android Driver: " + e.getMessage());
            LOGGER.debug("Stacktrace:", e);
            return null;
        }
    }

    private String getAndroidDriverUrl() {
        return "http://localhost:" + getAndroidDriverHostPort() + "/wd/hub";
    }

    public void setAndroidDriverHostPort(String port) {
        setProperty(ANDROID_DRIVER_HOST_PORT, port);
    }

    public String getAndroidDriverHostPort() {
        return getPropertyAsString(ANDROID_DRIVER_HOST_PORT);
    }
}
