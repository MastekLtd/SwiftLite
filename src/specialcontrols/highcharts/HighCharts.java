package specialcontrols.highcharts;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.DefaultElementLocatorFactory;

abstract class HighCharts {

    protected WebDriver driver;
    protected WebElement chart;
    protected String toolTipLOcator = "g[class='highcharts-tooltip'] text";

    public HighCharts(WebDriver driver, WebElement chart) {
        PageFactory.initElements(new DefaultElementLocatorFactory(chart), this);
        this.driver = driver;
        this.chart = chart;

    }
}
