package com.googlecode.jmeter.plugins.webdriver.sampler;

import com.googlecode.jmeter.plugins.w3c.NavigationTiming;
import org.apache.jmeter.samplers.SampleResult;

public class WebSampleResult extends SampleResult {
    private NavigationTiming navigationTiming;

    public NavigationTiming getNavigationTiming() {
        return navigationTiming;
    }

    public void setNavigationTiming(NavigationTiming navigationTiming) {
        this.navigationTiming = navigationTiming;
    }
}
