package com.googlecode.jmeter.plugins.webdriver.config.gui;

import com.googlecode.jmeter.plugins.webdriver.config.WebDriverConfig;
import com.googlecode.jmeter.plugins.webdriver.proxy.ProxyType;
import kg.apc.jmeter.JMeterPluginsUtils;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;

public class WebDriverConfigGui extends AbstractConfigGui implements ItemListener {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 241L;

    private static final int PROXY_FIELD_INDENT = 28;

    private static final int DEFAULT_PROXY_PORT = 8080;

    private static final String DEFAULT_NO_PROXY_LIST = "localhost";

    static {
        NUMBER_FORMAT.setGroupingUsed(false);
    }

    JRadioButton directProxy; // synonymous with no proxy

    JRadioButton autoDetectProxy;

    JRadioButton systemProxy;

    JRadioButton manualProxy;

    JRadioButton pacUrlProxy;

    JTextField pacUrl;

    JTextField httpProxyHost;

    JFormattedTextField httpProxyPort;

    JCheckBox useHttpSettingsForAllProtocols;

    JTextField httpsProxyHost;

    JFormattedTextField httpsProxyPort;

    JTextField ftpProxyHost;

    JFormattedTextField ftpProxyPort;

    JTextField socksProxyHost;

    JFormattedTextField socksProxyPort;

    JTextArea noProxyList;

    public WebDriverConfigGui() {
        init();
    }

    @Override
    public String getStaticLabel() {
        return JMeterPluginsUtils.prefixLabel("Web Driver Config");
    }

    @Override
    public String getLabelResource() {
        return getClass().getCanonicalName();
    }


    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if(element instanceof WebDriverConfig) {
            WebDriverConfig webDriverConfig = (WebDriverConfig)element;
            switch (webDriverConfig.getProxyType()) {
                case DIRECT:
                    directProxy.setSelected(true);
                    break;
                case AUTO_DETECT:
                    autoDetectProxy.setSelected(true);
                    break;
                case MANUAL:
                    manualProxy.setSelected(true);
                    break;
                case PROXY_PAC:
                    pacUrlProxy.setSelected(true);
                    break;
                default:
                    systemProxy.setSelected(true); // fallback to system proxy
            }
            pacUrl.setText(webDriverConfig.getProxyPacUrl());
            httpProxyHost.setText(webDriverConfig.getHttpHost());
            httpProxyPort.setValue(webDriverConfig.getHttpPort());
            useHttpSettingsForAllProtocols.setSelected(webDriverConfig.isUseHttpSettingsForAllProtocols());
            httpsProxyHost.setText(webDriverConfig.getHttpsHost());
            httpsProxyPort.setValue(webDriverConfig.getHttpsPort());
            ftpProxyHost.setText(webDriverConfig.getFtpHost());
            ftpProxyPort.setValue(webDriverConfig.getFtpPort());
            socksProxyHost.setText(webDriverConfig.getSocksHost());
            socksProxyPort.setValue(webDriverConfig.getSocksPort());
            noProxyList.setText(webDriverConfig.getNoProxyHost());
        }
    }

    @Override
    public TestElement createTestElement() {
        WebDriverConfig element = new WebDriverConfig();
        modifyTestElement(element);
        return element;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        if(element instanceof WebDriverConfig) {
            WebDriverConfig webDriverConfig = (WebDriverConfig)element;
            if(directProxy.isSelected()) {
                webDriverConfig.setProxyType(ProxyType.DIRECT);
            } else if(autoDetectProxy.isSelected()) {
                webDriverConfig.setProxyType(ProxyType.AUTO_DETECT);
            } else if(pacUrlProxy.isSelected()) {
                webDriverConfig.setProxyType(ProxyType.PROXY_PAC);
            } else if(manualProxy.isSelected()) {
                webDriverConfig.setProxyType(ProxyType.MANUAL);
            } else {
                webDriverConfig.setProxyType(ProxyType.SYSTEM); // fallback
            }
            webDriverConfig.setProxyPacUrl(pacUrl.getText());
            webDriverConfig.setHttpHost(httpProxyHost.getText());
            webDriverConfig.setHttpPort((Integer) httpProxyPort.getValue());
            webDriverConfig.setUseHttpSettingsForAllProtocols(useHttpSettingsForAllProtocols.isSelected());
            webDriverConfig.setHttpsHost(httpsProxyHost.getText());
            webDriverConfig.setHttpsPort((Integer) httpsProxyPort.getValue());
            webDriverConfig.setFtpHost(ftpProxyHost.getText());
            webDriverConfig.setFtpPort((Integer) ftpProxyPort.getValue());
            webDriverConfig.setSocksHost(socksProxyHost.getText());
            webDriverConfig.setSocksPort((Integer) socksProxyPort.getValue());
            webDriverConfig.setNoProxyHost(noProxyList.getText());
        }
    }

    @Override
    public void clearGui() {
        super.clearGui();

        systemProxy.setSelected(true);
        pacUrl.setText("");
        httpProxyHost.setText("");
        httpProxyPort.setValue(DEFAULT_PROXY_PORT);
        useHttpSettingsForAllProtocols.setSelected(true);
        httpsProxyHost.setText("");
        httpsProxyPort.setValue(DEFAULT_PROXY_PORT);
        ftpProxyHost.setText("");
        ftpProxyPort.setValue(DEFAULT_PROXY_PORT);
        socksProxyHost.setText("");
        socksProxyPort.setValue(DEFAULT_PROXY_PORT);
        noProxyList.setText(DEFAULT_NO_PROXY_LIST);
    }

    private void createPacUrlProxy(JPanel panel, ButtonGroup group) {
        pacUrlProxy = new JRadioButton("Automatic proxy configuration URL");
        group.add(pacUrlProxy);
        panel.add(pacUrlProxy);

        pacUrlProxy.addItemListener(this);

        JPanel pacUrlPanel = new HorizontalPanel();
        pacUrl = new JTextField();
        pacUrl.setEnabled(false);
        pacUrlPanel.add(pacUrl, BorderLayout.CENTER);
        pacUrlPanel.setBorder(BorderFactory.createEmptyBorder(0, PROXY_FIELD_INDENT, 0, 0));
        panel.add(pacUrlPanel);
    }

    private void createManualProxy(JPanel panel, ButtonGroup group) {
        manualProxy = new JRadioButton("Manual proxy configuration");
        group.add(manualProxy);
        panel.add(manualProxy);

        manualProxy.addItemListener(this);

        JPanel manualPanel = new VerticalPanel();
        manualPanel.setBorder(BorderFactory.createEmptyBorder(0, PROXY_FIELD_INDENT, 0, 0));

        httpProxyHost = new JTextField();
        httpProxyPort = new JFormattedTextField(NUMBER_FORMAT);
        httpProxyPort.setValue(DEFAULT_PROXY_PORT);
        manualPanel.add(createProxyHostAndPortPanel(httpProxyHost, httpProxyPort, "HTTP Proxy:"));
        useHttpSettingsForAllProtocols = new JCheckBox("Use HTTP proxy server for all protocols");
        useHttpSettingsForAllProtocols.setSelected(true);
        useHttpSettingsForAllProtocols.setEnabled(false);
        useHttpSettingsForAllProtocols.addItemListener(this);
        manualPanel.add(useHttpSettingsForAllProtocols);

        httpsProxyHost = new JTextField();
        httpsProxyPort = new JFormattedTextField(NUMBER_FORMAT);
        httpsProxyPort.setValue(DEFAULT_PROXY_PORT);
        manualPanel.add(createProxyHostAndPortPanel(httpsProxyHost, httpsProxyPort, "SSL Proxy:"));

        ftpProxyHost = new JTextField();
        ftpProxyPort = new JFormattedTextField(NUMBER_FORMAT);
        ftpProxyPort.setValue(DEFAULT_PROXY_PORT);
        manualPanel.add(createProxyHostAndPortPanel(ftpProxyHost, ftpProxyPort, "FTP Proxy:"));

        socksProxyHost = new JTextField();
        socksProxyPort = new JFormattedTextField(NUMBER_FORMAT);
        socksProxyPort.setValue(DEFAULT_PROXY_PORT);
        manualPanel.add(createProxyHostAndPortPanel(socksProxyHost, socksProxyPort, "SOCKS Proxy:"));

        manualPanel.add(createNoProxyPanel());

        panel.add(manualPanel);
    }

    private JPanel createNoProxyPanel() {
        JPanel noProxyPanel = new VerticalPanel();
        JLabel noProxyListLabel = new JLabel("No Proxy for:");
        noProxyPanel.add(noProxyListLabel);

        noProxyList = new JTextArea(3,10);
        noProxyList.setText(DEFAULT_NO_PROXY_LIST);
        noProxyList.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        noProxyList.setEnabled(false);
        noProxyPanel.add(noProxyList);

        JLabel noProxyExample = new JLabel("Example: .jmeter.org, .com.au, 192.168.1.0/24");
        noProxyPanel.add(noProxyExample);

        return noProxyPanel;
    }

    private JPanel createProxyHostAndPortPanel(JTextField proxyHost, JTextField proxyPort, String label) {
        JPanel httpPanel = new HorizontalPanel();
        JLabel httpProxyHostLabel = new JLabel(label);
        httpPanel.add(httpProxyHostLabel);
        httpPanel.add(proxyHost);
        proxyHost.setEnabled(false);
        JLabel httpProxyPortLabel = new JLabel("Port:");
        httpPanel.add(httpProxyPortLabel);
        httpPanel.add(proxyPort);
        proxyPort.setEnabled(false);
        return httpPanel;
    }

    private void createSystemProxy(JPanel panel, ButtonGroup group) {
        systemProxy = new JRadioButton("Use system proxy settings");
        group.add(systemProxy);
        panel.add(systemProxy);
    }

    private void createAutoDetectProxy(JPanel panel, ButtonGroup group) {
        autoDetectProxy = new JRadioButton("Auto-detect proxy settings for this network");
        group.add(autoDetectProxy);
        panel.add(autoDetectProxy);
    }

    private void createDirectProxy(JPanel panel, ButtonGroup group) {
        directProxy = new JRadioButton("No proxy");
        group.add(directProxy);
        panel.add(directProxy);
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));

        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        // MAIN PANEL
        JPanel mainPanel = new VerticalPanel();
        ButtonGroup group = new ButtonGroup();

        createDirectProxy(mainPanel, group);
        createAutoDetectProxy(mainPanel, group);
        createSystemProxy(mainPanel, group);
        createManualProxy(mainPanel, group);
        createPacUrlProxy(mainPanel, group);

        systemProxy.setSelected(true);

        add(mainPanel, BorderLayout.CENTER);
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {
        if(itemEvent.getSource() == pacUrlProxy) {
            pacUrl.setEnabled(itemEvent.getStateChange() == ItemEvent.SELECTED);
        } else if(itemEvent.getSource() == manualProxy) {
            httpProxyHost.setEnabled(itemEvent.getStateChange() == ItemEvent.SELECTED);
            httpProxyPort.setEnabled(itemEvent.getStateChange() == ItemEvent.SELECTED);
            useHttpSettingsForAllProtocols.setEnabled(itemEvent.getStateChange() == ItemEvent.SELECTED);
            noProxyList.setEnabled(itemEvent.getStateChange() == ItemEvent.SELECTED);
        } else if(itemEvent.getSource() == useHttpSettingsForAllProtocols) {
            httpsProxyHost.setEnabled(itemEvent.getStateChange() == ItemEvent.DESELECTED);
            httpsProxyPort.setEnabled(itemEvent.getStateChange() == ItemEvent.DESELECTED);
            ftpProxyHost.setEnabled(itemEvent.getStateChange() == ItemEvent.DESELECTED);
            ftpProxyPort.setEnabled(itemEvent.getStateChange() == ItemEvent.DESELECTED);
            socksProxyHost.setEnabled(itemEvent.getStateChange() == ItemEvent.DESELECTED);
            socksProxyPort.setEnabled(itemEvent.getStateChange() == ItemEvent.DESELECTED);
        }
    }
}