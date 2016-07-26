package specialcontrols.highcharts;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.Point;
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

public class BarChart extends HighCharts {	
     
   @FindBy(how = How.CSS, using = "g[class='highcharts-series highcharts-tracker']")
   List<WebElement> elementToHoverOver;
   
   @FindBy(how = How.CSS, using = "g[class='highcharts-axis-labels highcharts-xaxis-labels'] text")
   List<WebElement> elementXAxisPoints;
   
   @FindBy(how = How.CSS, using = "div[id='assetAlloc'] text[text-anchor='middle'] tspan")
   List<WebElement> elementPieChartInfo;
   
   @FindBy(how = How.CSS, using = "g.highcharts-legend-item")
   List<WebElement> elementsLegend;
 
    public BarChart(WebDriver driver, WebElement chart) {
        super(driver, chart);
    }

    public String getTooltipForBarElement(WebElement hoverbarElement) throws InterruptedException {
    		
		Actions action = new Actions(driver);
		action.moveToElement(hoverbarElement).build().perform();
		Thread.sleep(1000);		
		List<WebElement> toolTiplist = driver.findElements(By.cssSelector(toolTipLOcator+ " tspan"));
		String toolTipText="";
		for (WebElement element:toolTiplist ){
			
			toolTipText+=element.getText()+" ";
			
		}		
		return toolTipText;
    	
    }
    
    public List<String> getListOfXAxisPoints() throws InterruptedException {
    	
    	List<String> listXAxisPoints = new ArrayList<String>();
		
		for (WebElement element:elementXAxisPoints){					
			listXAxisPoints.add(element.getText());
		}
		return listXAxisPoints;    	
    	
    }
    
    public List<String> getColumnNames() throws InterruptedException {
    	
    	ArrayList<String> columns = new ArrayList<String>(
				Arrays.asList("PointMonth","ColorCode_Addition","Tooltip_Addition","ColorCode_Withdrawals","Tooltip_Withdrawals"));			
		return columns;    	
    	
    }
    
    public List<String> getLegendColumnNames() throws InterruptedException {
    	
    	ArrayList<String> columns = new ArrayList<String>(
				Arrays.asList("ColorCode", "AssetType"));			
		return columns;    	
    	
    }
    
    public List<List<String>> getChartData(List<String> seriesGroup) throws InterruptedException  {
    	List<List<String>> chartData = new ArrayList<List<String>>();
    	List<WebElement> barElements = new ArrayList<WebElement>();
    	int iCount=0;
    	List<Integer> listLineSeries = getIndicesOfLineSeries();
    	
    	//Turn off line Graph otherwise mouse hover would not be accurate as line chart points may coincide Barchart section
    	for (Integer iSeries:listLineSeries){
    		elementsLegend.get(iSeries).findElement(By.cssSelector(" path")).click();
    		Thread.sleep(500);
    		
    	}    	
    	
		for (String xPoint:getListOfXAxisPoints()){
			
			List<String> listInfo = new ArrayList<String>();
			
			listInfo.add(xPoint);//add X axis Point	
			
			for (String series:seriesGroup){
				
				String colorCode = getColorForSeries(series);
				int iColorIndex= getIndexOfColor(colorCode);				
				
				barElements = elementToHoverOver.get(iColorIndex).findElements(By.cssSelector(" rect[fill='"+colorCode+"']"));
				listInfo.add(colorCode);
				listInfo.add(getTooltipForBarElement(barElements.get(iCount)));//Add AssetType
				
			}
			
			iCount++;				
	    	chartData.add((ArrayList<String>) listInfo);
	    			
		}
		return  chartData;
		
    }
    
    private int getIndexOfColor(String colorCode) throws InterruptedException {
		// TODO Auto-generated method stub
    	int iCount=0;
    	for (List<String> legendItem:getLegendData()){
    		if (legendItem.get(0).trim().equals(colorCode)){
    			
    			return iCount;
    		}
    		    		
    		iCount++;
    	}
		return -1;
	}

	private String getColorForSeries(String series) throws InterruptedException {
		// TODO Auto-generated method stub
		System.out.println("");
    	for (List<String> legendItem:getLegendData()){
    		if (legendItem.get(1).trim().equals(series)){
    			
    			return legendItem.get(0);
    		}
    	
    	}
		return null;
	}
	
	public List<Integer> getIndicesOfLineSeries() {
    	    	
    	List<Integer> listLineSeries = new ArrayList<Integer>();
		int iCount=0;
		for (WebElement element:elementsLegend){
			
			if (element.findElements(By.cssSelector(" rect")).size() == 0){
				listLineSeries.add(iCount);				
			}
			
		iCount++;	
		}
		return listLineSeries;		    	
    	
    }

	public List<List<String>> getLegendData() throws InterruptedException  {
    	List<List<String>> legendData = new ArrayList<List<String>>();
    	int iCount=0;
    	List<Integer> listLineSeries = getIndicesOfLineSeries();
    	for (WebElement element:elementsLegend){
    		List<String> listInfo = new ArrayList<String>();
    		if (listLineSeries.contains(iCount)){
    			listInfo.add(element.findElement(By.cssSelector(" path")).getAttribute("stroke"));
    			listInfo.add(element.getText());
    		 
    		}
    		else{
							 
				listInfo.add(element.findElement(By.cssSelector(" rect")).getAttribute("fill"));//Add ColorCode			
				listInfo.add(element.getText());//Add AssetType
    		}
			legendData.add((ArrayList<String>) listInfo);
			iCount++;
			
		}
		return  legendData;
		
    }     
  
}