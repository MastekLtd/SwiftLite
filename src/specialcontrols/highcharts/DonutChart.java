package specialcontrols.highcharts;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DonutChart extends HighCharts {	
     
   @FindBy(how = How.CSS, using = "g[class='highcharts-data-labels highcharts-tracker'] text tspan")
   List<WebElement> elementToHoverOver;
   
   @FindBy(how = How.CSS, using = "g.highcharts-series-group g[class='highcharts-series highcharts-tracker'] path")
   List<WebElement> elementColorCodes;
   
   @FindBy(how = How.CSS, using = "div[id='assetAlloc'] text[text-anchor='middle'] tspan")
   List<WebElement> elementPieChartInfo;
   
   @FindBy(how = How.CSS, using = "div.highcharts-legend-item")
   List<WebElement> elementsLegend;
 
    public DonutChart(WebDriver driver, WebElement chart) {
        super(driver, chart);
    }

    public String getTooltipForColorCode(String colorCode) throws InterruptedException {
    	    	
    	int indexColor = getListOfColorCodes().indexOf(colorCode);    	
    	if (indexColor == -1){//color code not found
    		return "";
    	}
    	
    	WebElement element = elementToHoverOver.get(indexColor);
		
		Actions action = new Actions(driver);
		action.moveToElement(element).build().perform();
		Thread.sleep(1000);
		List<WebElement> toolTiplist = driver.findElements(By.cssSelector(toolTipLOcator+ " tspan"));
		String toolTipText="";
		for (WebElement elementtoolTip:toolTiplist ){
			
			toolTipText+=elementtoolTip.getText();
			
		}		
		return toolTipText;
		
    	
    }
    
    public List<String> getListOfColorCodes() throws InterruptedException {
    	
    	List<String> listColorCodes = new ArrayList<String>();
		
		for (WebElement element:elementColorCodes){					
			listColorCodes.add(element.getAttribute("fill").toString().trim());
		}
		return listColorCodes;    	
    	
    }
    
    public List<String> getDonutColumnNames() throws InterruptedException {
    	
    	ArrayList<String> columns = new ArrayList<String>(
				Arrays.asList("ColorCode","ToolTip","Percentage","MiddleInfo"));			
		return columns;    	
    	
    }
    
    public List<String> getDonutLegendColumnNames() throws InterruptedException {
    	
    	ArrayList<String> columns = new ArrayList<String>(
				Arrays.asList("ColorCode", "AssetType","Amount","Percentage"));			
		return columns;    	
    	
    }
    
    public List<List<String>> getDonutChartData() throws InterruptedException  {
    	List<List<String>> donutChartData = new ArrayList<List<String>>();
    	    	
		for (String colorCode:getListOfColorCodes()){
			
			List<String> listInfo = new ArrayList<String>();
			listInfo.add(colorCode.trim());//Add Color Code
			listInfo.add(getTooltipForColorCode(colorCode).trim());//add tooltip
			
			int indexColor = getListOfColorCodes().indexOf(colorCode);
	    		    	
	    	WebElement element = elementToHoverOver.get(indexColor);
	    	listInfo.add(element.getText().trim());//add percentage
	    		    	
	    	String finalPieChartInfo = "";
	    	for (WebElement elementPie:elementPieChartInfo){
	    		finalPieChartInfo+=elementPie.getText()+" ";
	    		
	    	}
	    	
	    	listInfo.add(finalPieChartInfo.trim());//add centre Info
			
	    	donutChartData.add((ArrayList<String>) listInfo);
			
		}
		return  donutChartData;
		
    }
    
    public List<List<String>> getDonutLegendData() throws InterruptedException  {
    	List<List<String>> donutLegendData = new ArrayList<List<String>>();
    	int iCount =0;
    	    	
		for (String colorCode:getListOfColorCodes()){
			
			List<String> listInfo = new ArrayList<String>();
			
			 List<WebElement> RowElements = elementsLegend.get(iCount).findElements(By.cssSelector(" tr td"));
			 
			listInfo.add(colorCode.trim());//Add ColorCode			
			listInfo.add(RowElements.get(0).getText().trim());//Add AssetType
			listInfo.add(RowElements.get(1).getText().trim());//Add Amount
			listInfo.add(RowElements.get(2).getText().trim());//Add Percentage			
			
			donutLegendData.add((ArrayList<String>) listInfo);	    	
	    	iCount++;
			
		}
		return  donutLegendData;
		
    }  
    
  
}


