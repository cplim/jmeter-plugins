package com.googlecode.jmeter.plugins.webdriver.sampler;

import com.googlecode.jmeter.plugins.w3c.NavigationTiming;
import com.googlecode.jmeter.plugins.webdriver.config.WebDriverConfig;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.log.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class WebDriverSamplerTest {

    private WebDriverSampler sampler;
    private JMeterVariables variables;
    private WebDriver browser;
    private JavascriptExecutor javascriptExecutor;

    @Before
    public void createSampler() {
        variables = new JMeterVariables();

        browser = Mockito.mock(WebDriver.class);
        when(browser.getPageSource()).thenReturn("page source");
        when(browser.getCurrentUrl()).thenReturn("http://google.com.au");
        variables.putObject(WebDriverConfig.BROWSER, browser);

        javascriptExecutor = Mockito.mock(JavascriptExecutor.class);
        variables.putObject(WebDriverConfig.JAVASCRIPT_EXECUTOR, javascriptExecutor);

        JMeterContextService.getContext().setVariables(variables);
        sampler = new WebDriverSampler();
    }

    @Test
    public void shouldBeAbleToSerialiseAndDeserialise() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream(bytes);

        output.writeObject(sampler);
        output.flush();
        output.close();

        ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        final WebDriverSampler deserializedSampler = (WebDriverSampler) input.readObject();

        assertThat(deserializedSampler, is(sampler));
    }

    @Test
    public void shouldBeAbleToSetParameters() {
        sampler.setParameters("parameters");
        assertThat(sampler.getParameters(), is("parameters"));
    }

    @Test
    public void shouldBeAbleToSetScript() {
        sampler.setScript("script");
        assertThat(sampler.getScript(), is("script"));
    }

    @Test
    public void shouldHaveExpectedInstanceVariablesOnScriptContext() {
        sampler.setName("name");
        sampler.setParameters("p1 p2 p3");
        final SampleResult sampleResult = new SampleResult();
        final ScriptEngine scriptEngine = sampler.createScriptEngineWith(sampleResult);
        final ScriptContext scriptContext = scriptEngine.getContext();
        final WebDriverScriptable scriptable = (WebDriverScriptable) scriptContext.getAttribute("WDS");
        assertThat(scriptable.getLog(), is(instanceOf(Logger.class)));
        assertThat(scriptable.getName(), is(sampler.getName()));
        assertThat(scriptable.getParameters(), is(sampler.getParameters()));
        assertThat(scriptable.getArgs(), is(new String[]{"p1", "p2", "p3"}));
        assertThat(scriptable.getBrowser(), is(instanceOf(WebDriver.class)));
        assertThat(scriptable.getSampleResult(), is(sampleResult));
    }

    @Test
    public void shouldReturnSuccessfulSampleResultWhenEvalScriptCompletes() throws MalformedURLException {
        sampler.setName("name");
        sampler.setScript("var x = 'hello';");
        final SampleResult sampleResult = sampler.sample(null);

        assertThat(sampleResult.isResponseCodeOK(), is(true));
        assertThat(sampleResult.getResponseMessage(), is("OK"));
        assertThat(sampleResult.isSuccessful(), is(true));
        assertThat(sampleResult.getContentType(), is("text/plain"));
        assertThat(sampleResult.getDataType(), is(SampleResult.TEXT));
        assertThat(sampleResult.getSampleLabel(), is("name"));
        assertThat(sampleResult.getResponseDataAsString(), is("page source"));
        assertThat(sampleResult.getURL(), is(new URL("http://google.com.au")));

        verify(browser, times(1)).getPageSource();
        verify(browser, times(1)).getCurrentUrl();
    }

    @Test
    public void shouldReturnSuccessfulSampleResultWhenScriptSetsSampleResultToSuccess() throws MalformedURLException {
        sampler.setScript("WDS.sampleResult.setSuccessful(true);");
        final SampleResult sampleResult = sampler.sample(null);

        assertThat(sampleResult.isSuccessful(), is(true));
        assertThat(sampleResult.getResponseCode(), is("200"));
        assertThat(sampleResult.getResponseMessage(), is("OK"));
        assertThat(sampleResult.getResponseDataAsString(), is("page source"));
        assertThat(sampleResult.getURL(), is(new URL("http://google.com.au")));

        verify(browser, times(1)).getPageSource();
        verify(browser, times(1)).getCurrentUrl();
    }

    @Test
    public void shouldReturnFailureSampleResultWhenScriptSetsSampleResultToFailure() throws MalformedURLException {
        sampler.setScript("WDS.sampleResult.setSuccessful(false);");
        final SampleResult sampleResult = sampler.sample(null);

        assertThat(sampleResult.isSuccessful(), is(false));
        assertThat(sampleResult.getResponseCode(), is("500"));
        assertThat(sampleResult.getResponseMessage(), not("OK"));
        assertThat(sampleResult.getResponseDataAsString(), is("page source"));
        assertThat(sampleResult.getURL(), is(new URL("http://google.com.au")));

        verify(browser, times(1)).getPageSource();
        verify(browser, times(1)).getCurrentUrl();
    }

    @Test
    public void shouldReturnFailureSampleResultWhenEvalScriptIsInvalid() {
        sampler.setScript("x.methodThatDoesNotExist();");
        final SampleResult sampleResult = sampler.sample(null);

        assertThat(sampleResult.isResponseCodeOK(), is(false));
        assertThat(sampleResult.getResponseMessage(), containsString("javax.script.ScriptException"));
        assertThat(sampleResult.isSuccessful(), is(false));

        verify(browser, never()).getPageSource();
        verify(browser, never()).getCurrentUrl();
    }

    @Test
    public void shouldReturnFailureSampleResultWhenBrowserURLIsInvalid() {
        when(browser.getCurrentUrl()).thenReturn("unknown://uri");
        sampler.setScript("var x = 'hello';");
        final SampleResult sampleResult = sampler.sample(null);

        assertThat(sampleResult.isResponseCodeOK(), is(false));
        assertThat(sampleResult.getResponseMessage(), containsString("MalformedURLException"));
        assertThat(sampleResult.isSuccessful(), is(false));

        verify(browser, times(1)).getCurrentUrl();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenBrowserNotConfigured() {
        variables.remove(WebDriverConfig.BROWSER);
        sampler.setScript("var x=1;");
        sampler.sample(null);
        fail("Did not throw expected exception"); // should throw exception if Browser is null
    }

    @Test
    public void shouldInvokeW3CNavigationTiming() {
        sampler.sample(null);

        ArgumentCaptor<String> performanceTimingScript = ArgumentCaptor.forClass(String.class);
        verify(javascriptExecutor, times(1)).executeScript(performanceTimingScript.capture());

        assertThat(performanceTimingScript.getValue(), containsString("performance.timing"));
    }

    @Test
    public void shouldReturnSampleResultWithW3CNavigationTiming() {
        final long navigationStart = 1371467808230L;
        final long unloadEventStart = 0;
        final long unloadEventEnd = 0;
        final long redirectStart = 0;
        final long redirectEnd = 0;
        final long fetchStart = 1371467808230L;
        final long domainLookupStart = 1371467808231L;
        final long domainLookupEnd = 1371467808252L;
        final long connectStart = 1371467808230L;
        final long connectEnd = 1371467808230L;
        final long secureConnectionStart = 0;
        final long requestStart = 1371467808294L;
        final long responseStart = 1371467808427L;
        final long responseEnd = 1371467808434L;
        final long domLoading = 1371467808427L;
        final long domInteractive = 1371467809096L;
        final long domContentLoadedEventStart = 1371467809114L;
        final long domContentLoadedEventEnd = 1371467809181L;
        final long domComplete = 1371467810901L;
        final long loadEventStart = 1371467810901L;
        final long loadEventEnd = 1371467810903L;
        final Map<String, String> json = new HashMap<String, String>();
        json.put("fetchStart", String.valueOf(fetchStart));
        json.put("redirectStart", String.valueOf(redirectStart));
        json.put("domComplete", String.valueOf(domComplete));
        json.put("redirectEnd", String.valueOf(redirectEnd));
        json.put("loadEventStart", String.valueOf(loadEventStart));
        json.put("navigationStart", String.valueOf(navigationStart));
        json.put("requestStart", String.valueOf(requestStart));
        json.put("responseEnd", String.valueOf(responseEnd));
        json.put("secureConnectionStart", String.valueOf(secureConnectionStart));
        json.put("domLoading", String.valueOf(domLoading));
        json.put("domInteractive", String.valueOf(domInteractive));
        json.put("domContentLoadedEventStart", String.valueOf(domContentLoadedEventStart));
        json.put("domainLookupEnd", String.valueOf(domainLookupEnd));
        json.put("responseStart", String.valueOf(responseStart));
        json.put("connectEnd", String.valueOf(connectEnd));
        json.put("loadEventEnd", String.valueOf(loadEventEnd));
        json.put("unloadEventStart", String.valueOf(unloadEventStart));
        json.put("connectStart", String.valueOf(connectStart));
        json.put("domContentLoadedEventEnd", String.valueOf(domContentLoadedEventEnd));
        json.put("unloadEventEnd", String.valueOf(unloadEventEnd));
        json.put("domainLookupStart", String.valueOf(domainLookupStart));

        when(javascriptExecutor.executeScript(anyString())).thenReturn(json);

        final WebSampleResult sample = (WebSampleResult)sampler.sample(null);
        final NavigationTiming navigationTiming = sample.getNavigationTiming();

        assertThat(sample.isSuccessful(), is(true));
        assertThat(navigationTiming, is(notNullValue()));
        assertThat(navigationTiming.getFetchStart(), is(navigationStart));
        assertThat(navigationTiming.getUnloadEventStart(), is(unloadEventStart));
        assertThat(navigationTiming.getUnloadEventEnd(), is(unloadEventEnd));
        assertThat(navigationTiming.getRedirectStart(), is(redirectStart));
        assertThat(navigationTiming.getRedirectEnd(), is(redirectEnd));
        assertThat(navigationTiming.getFetchStart(), is(fetchStart));
        assertThat(navigationTiming.getDomainLookupStart(), is(domainLookupStart));
        assertThat(navigationTiming.getDomainLookupEnd(), is(domainLookupEnd));
        assertThat(navigationTiming.getConnectStart(), is(connectStart));
        assertThat(navigationTiming.getConnectEnd(), is(connectEnd));
        assertThat(navigationTiming.getSecureConnectionStart(), is(secureConnectionStart));
        assertThat(navigationTiming.getRequestStart(), is(requestStart));
        assertThat(navigationTiming.getResponseStart(), is(responseStart));
        assertThat(navigationTiming.getResponseEnd(), is(responseEnd));
        assertThat(navigationTiming.getDomLoading(), is(domLoading));
        assertThat(navigationTiming.getDomInteractive(), is(domInteractive));
        assertThat(navigationTiming.getDomContentLoadedEventStart(), is(domContentLoadedEventStart));
        assertThat(navigationTiming.getDomContentLoadedEventEnd(), is(domContentLoadedEventEnd));
        assertThat(navigationTiming.getDomComplete(), is(domComplete));
        assertThat(navigationTiming.getLoadEventStart(), is(loadEventStart));
        assertThat(navigationTiming.getLoadEventEnd(), is(loadEventEnd));
    }

    @Test
    public void shouldReturnSampleWithoutW3CNavigationTiming() {
        when(javascriptExecutor.executeScript(anyString())).thenReturn(null);

        final WebSampleResult sample = (WebSampleResult)sampler.sample(null);

        assertThat(sample.isSuccessful(), is(true));
        assertThat(sample.getNavigationTiming(), is(nullValue()));
    }

    @Test
    public void shouldReturnSampleWithoutW3CNavigationTimingWhenObjectMapperThrowsException() {
        when(javascriptExecutor.executeScript(anyString())).thenReturn("hello world");

        final WebSampleResult sample = (WebSampleResult)sampler.sample(null);

        assertThat(sample.isSuccessful(), is(true));
        assertThat(sample.getNavigationTiming(), is(nullValue()));
    }
}
