package kg.apc.jmeter.vizualizers;

import kg.apc.jmeter.charting.DateTimeRenderer;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

/**
 *
 * @author apc
 */
abstract class AbstractOverTimeVisualizer extends AbstractGraphPanelVisualizer {
    private static final String HHMMSS = "HH:mm:ss";
    private long relativeStartTime=0;

    public AbstractOverTimeVisualizer()
    {
      graphPanel.getGraphObject().setxAxisLabelRenderer(new DateTimeRenderer(HHMMSS));
      graphPanel.getGraphObject().setxAxisLabel("Elapsed time");
      graphPanel.getGraphObject().setDrawFinalZeroingLines(true);
      graphPanel.getGraphObject().setDisplayPrecision(true);
    }

    public void add(SampleResult sample)
    {
        if (relativeStartTime==0)
        {
            relativeStartTime=sample.getStartTime();

            if (graphPanel.getGraphObject().isUseRelativeTime())
                graphPanel.getGraphObject().setxAxisLabelRenderer(new DateTimeRenderer(HHMMSS, JMeterUtils.getPropDefault("TESTSTART.MS", sample.getStartTime())));
        }
    }

    @Override
    public void clearData()
    {
        super.clearData();
        relativeStartTime=0;
    }
}