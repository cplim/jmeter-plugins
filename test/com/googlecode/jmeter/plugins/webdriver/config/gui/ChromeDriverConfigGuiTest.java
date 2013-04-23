package com.googlecode.jmeter.plugins.webdriver.config.gui;

import com.googlecode.jmeter.plugins.webdriver.config.ChromeDriverConfig;
import kg.apc.emulators.TestJMeterUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;

public class ChromeDriverConfigGuiTest {

    private ChromeDriverConfigGui gui;

    @BeforeClass
    public static void setupJMeterEnv() {
        TestJMeterUtils.createJmeterEnv();
    }

    @Before
    public void createConfig() {
        gui = new ChromeDriverConfigGui();
    }

    @Test
    public void shouldReturnStaticLabel() {
        assertThat(gui.getStaticLabel(), containsString("Chrome Driver Config"));
    }

    @Test
    public void shouldReturnCanonicalClassNameAsLabelResource() {
        assertThat(gui.getLabelResource(), is(gui.getClass().getCanonicalName()));
    }

    @Test
    public void shouldReturnChromeDriverConfig() {
        assertThat(gui.createTestElement(), is(instanceOf(ChromeDriverConfig.class)));
    }

    @Test
    public void shouldSetChromeDriverPath() {
        gui.chromeServicePath.setText("chromedriver");
        final ChromeDriverConfig testElement = (ChromeDriverConfig) gui.createTestElement();
        assertThat(testElement.getChromeDriverPath(), is("chromedriver"));
    }

    @Test
    public void shouldResetValuesOnClearGui() {
        gui.chromeServicePath.setText("path");

        gui.clearGui();

        assertThat(gui.chromeServicePath.getText(), is(""));
    }

    @Test
    public void shouldSetChromeDriverPathOnConfigure() {
        ChromeDriverConfig config = new ChromeDriverConfig();
        config.setChromeDriverPath("chromedriver.path");
        gui.configure(config);

        assertThat(gui.chromeServicePath.getText(), is(config.getChromeDriverPath()));
    }

}