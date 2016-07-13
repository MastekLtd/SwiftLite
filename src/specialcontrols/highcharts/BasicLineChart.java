package specialcontrols.highcharts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

public class BasicLineChart extends HighCharts {
	
	public BasicLineChart(WebDriver driver, WebElement chart) {
		super(driver, chart);
	}
	
	@FindBy(how = How.CSS, using = "g[class='highcharts-axis-labels highcharts-xaxis-labels'] text")
	List<WebElement> elementXAxisPoints;
	
	@FindBy(how = How.CSS, using = "g.highcharts-legend-item")
	List<WebElement> elementsLegend;
	
	@FindBy(how = How.CSS, using = "g[class='highcharts-markers highcharts-tracker']")
	List<WebElement> elementToHoverOver;

   public List<String> getColumnNames() throws InterruptedException {
   	
   	ArrayList<String> columns = new ArrayList<String>(Arrays.asList("PointQuarterYear","ColorCode_MarketValue","Tooltip_MarketValue","ColorCode_GoalTarget","Tooltip_GoalTarget","ColorCode_NetContributions","Tooltip_NetContributions"));			
		return columns;    	
   	
   }
   
   public List<List<String>> getChartData() throws InterruptedException  {
	   List<List<String>> chartData = new ArrayList<List<String>>();
	   List<WebElement> lineElements = new ArrayList<WebElement>();
	   int iSeries=elementsLegend.size();
	   
	   for (int x = 0; x<iSeries; x++){
		   int iCount=elementXAxisPoints.size()-1;
		   int y = 0, index = 0;
		   if (x == 0) {
			   while(y < iSeries){
				   //Toggle legend items 
				   elementsLegend.get(y).findElement(By.cssSelector(" path")).click();
				   Thread.sleep(500);
				   y++;
			   }
		   }
		   else {
			   elementsLegend.get(x-1).findElement(By.cssSelector(" path")).click();
			   Thread.sleep(500);
		   }
		   elementsLegend.get(x).findElement(By.cssSelector(" path")).click();
		   Thread.sleep(500);
		
		   for (String xPoint:getListOfXAxisPoints()){
				List<String> listInfo = new ArrayList<String>();
						
				if (x == 0) listInfo.add(xPoint);//add X axis Point	
				
				for (List<String> legendItem:getLegendData()){
					String colorCode = getColorForSeries(legendItem.get(1).trim());
					int iColorIndex = getIndexOfColor(colorCode);
					
					if (!colorCode.equals("#CCC")) {
						lineElements = elementToHoverOver.get(iColorIndex).findElements(By.cssSelector(" path[stroke='"+colorCode+"']"));
						if (x == 0) {
							listInfo.add(colorCode);
							listInfo.add(getTooltipForBarElement(lineElements.get(iCount)));//Add AssetType
						}
						else {
							chartData.get(index).add(colorCode);
							chartData.get(index).add(getTooltipForBarElement(lineElements.get(iCount)));//Add AssetType
							index++;
						}
						
					}
				}
					
				iCount--;				
				if (x == 0) chartData.add((ArrayList<String>) listInfo);
							
		 	}
	   }
		return  chartData;
	   
   }
   
   public List<String> getLegendColumnNames() throws InterruptedException {
   	
   	ArrayList<String> columns = new ArrayList<String>(Arrays.asList("ColorCode", "AssetType"));			
		return columns;    	
   	
   }
   
   public List<List<String>> getLegendData() throws InterruptedException  {
	   
	   List<List<String>> legendData = new ArrayList<List<String>>();
	   
	   for (WebElement element:elementsLegend){
		   List<String> listInfo = new ArrayList<String>();
		   listInfo.add(element.findElement(By.cssSelector(" path[fill='none']")).getAttribute("stroke"));
		   listInfo.add(element.getText());
		   legendData.add((ArrayList<String>) listInfo);
			}
	   
	   return  legendData;
		
   }

   
   
   
   //Supporting function for getChartData()
   public List<String> getListOfXAxisPoints() throws InterruptedException {
   	
   	List<String> listXAxisPoints = new ArrayList<String>();
		
		for (WebElement element:elementXAxisPoints){					
			listXAxisPoints.add(element.getText());
		}
		return listXAxisPoints;    	
   	
   }
	//Supporting function for getChartData()
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
	//Supporting function for getChartData()
	private int getIndexOfColor(String colorCode) throws InterruptedException {
		
		int iCount=0;
		for (List<String> legendItem:getLegendData()){
			if (legendItem.get(0).trim().equals(colorCode)){
				return iCount;
			}
			iCount++;
		}
		return -1;
	}
	//Supporting function for getChartData()
	private String getColorForSeries(String series) throws InterruptedException {
		
		for (List<String> legendItem:getLegendData()){
			if (legendItem.get(1).trim().equals(series)){
				return legendItem.get(0);
			}
		}
		return null;
	}
   
}


