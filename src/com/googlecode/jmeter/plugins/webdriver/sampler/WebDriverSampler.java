package com.googlecode.jmeter.plugins.webdriver.sampler;

import com.googlecode.jmeter.plugins.w3c.NavigationTiming;
import com.googlecode.jmeter.plugins.webdriver.config.WebDriverConfig;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import javax.script.*;
import java.net.URL;


/**
 * A Sampler that makes HTTP requests using a real browser (via. Selenium/WebDriver).  It currently 
 * provides a scripting mechanism via. Javascript to control the browser instance.
 */
public class WebDriverSampler extends AbstractSampler {

    private static final long serialVersionUID = 100L;
    public static final String SCRIPT = "WebDriverSampler.script";
	public static final String PARAMETERS = "WebDriverSampler.parameters";
	private static final Logger LOGGER = LoggingManager.getLoggerForClass();
    private static final String DEFAULT_ENGINE = "JavaScript";
    private final transient ScriptEngineManager scriptEngineManager;
    private final transient ObjectMapper mapper;

    public WebDriverSampler() {
        this.scriptEngineManager = new ScriptEngineManager();
        mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    @Override
	public SampleResult sample(Entry e) {
        if(getWebDriver() == null) {
            throw new IllegalArgumentException("Browser has not been configured.  Please ensure at least 1 WebDriverConfig is created for a ThreadGroup.");
        }

        final WebSampleResult res = new WebSampleResult();
        res.setSampleLabel(getName());
        res.setSamplerData(toString());
        res.setDataType(SampleResult.TEXT);
        res.setContentType("text/plain");
        res.setDataEncoding("UTF-8");
        res.setSuccessful(true);

        LOGGER.info("Current thread name: '"+getThreadName()+"', has browser: '"+getWebDriver()+"'");

        try {
            final ScriptEngine scriptEngine = createScriptEngineWith(res);
            scriptEngine.eval(getScript());

            // setup the data in the SampleResult
            res.setResponseData(getWebDriver().getPageSource(), null);
            res.setURL(new URL(getWebDriver().getCurrentUrl()));
            res.setResponseCode(res.isSuccessful() ? "200" : "500");
            if(res.isSuccessful()) {
                res.setResponseMessageOK();
            }
            measureNavigationTiming(res);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            res.setResponseMessage(ex.toString());
            res.setResponseCode("500");
            res.setSuccessful(false);
        }

        return res;
	}

    private void measureNavigationTiming(WebSampleResult res) {
        try {
            final Object performanceTiming = getJavascriptExecutor().executeScript("return (function(w){ if((typeof w.performance != 'undefined') && (typeof w.performance.timing != 'undefined')) {return w.performance.timing;} })(window);");
            final NavigationTiming navigationTiming = mapper.convertValue(performanceTiming, NavigationTiming.class);
            res.setNavigationTiming(navigationTiming);
        } catch (IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    public String getScript() {
		return getPropertyAsString(SCRIPT);
	}
	
	public void setScript(String script) {
		setProperty(SCRIPT, script);
	}

	public String getParameters() {
		return getPropertyAsString(PARAMETERS);
	}

	public void setParameters(String parameters) {
		setProperty(PARAMETERS, parameters);
	}

    private WebDriver getWebDriver() {
        return (WebDriver) getThreadContext().getVariables().getObject(WebDriverConfig.BROWSER);
    }

    private JavascriptExecutor getJavascriptExecutor() {
        return (JavascriptExecutor) getThreadContext().getVariables().getObject(WebDriverConfig.JAVASCRIPT_EXECUTOR);
    }

    ScriptEngine createScriptEngineWith(SampleResult sampleResult) {
        final ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(DEFAULT_ENGINE);
        Bindings engineBindings = new SimpleBindings();
        WebDriverScriptable scriptable = new WebDriverScriptable();
        scriptable.setName(getName());
        scriptable.setParameters(getParameters());
        scriptable.setLog(LOGGER);
        scriptable.setSampleResult(sampleResult);
        scriptable.setBrowser(getWebDriver());
        engineBindings.put("WDS", scriptable);
        scriptEngine.setBindings(engineBindings, ScriptContext.ENGINE_SCOPE);
        return scriptEngine;
    }
}
