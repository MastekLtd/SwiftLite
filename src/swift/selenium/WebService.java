package swift.selenium;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
//import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
//import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class WebService {
	
	public static String responseXml;

	public static void callWebService() throws Exception
	{
		OutputStreamWriter requestWriter = null;
		BufferedReader responseReader = null;
		Scanner scanner = null;

		try{ 	

			URL wsdlUrl = new URL(WebHelper.wsdl_url);
			HttpURLConnection con = (HttpURLConnection)wsdlUrl.openConnection(); 

			/** Proxy settings ONLY if required **/
			System.setProperty("http.proxyHost", "192.168.100.40");
			System.setProperty("http.proxyPort", "8080");
			con.usingProxy();

			con.setRequestMethod("POST");
			con.setRequestProperty("Content-type", "text/xml; charset=utf-8");
			con.setRequestProperty("SOAPAction",WebHelper.request_url);
			con.setDoOutput(true);
			con.setDoInput(true);

			/** Reading data from Request XML **/
			String reqXml = Automation.configHashMap.get("INPUT_DATA_FILEPATH").toString() + "WebService\\" + WebHelper.request_xml + ".xml";
			//String soapMessage = new Scanner(new File(reqXml)).useDelimiter("\\A").next();
			scanner = new Scanner(new File(reqXml));
			String soapMessage = scanner.useDelimiter("\\A").next();

			/** Sending Request **/
			requestWriter = new OutputStreamWriter(con.getOutputStream());
			requestWriter.write(soapMessage);
			requestWriter.flush();

			/** Reading data from Response XML**/
			responseReader = new BufferedReader(new InputStreamReader(con.getInputStream())); 
			String line; 
			StringBuffer stringBuffer = new StringBuffer(); 
			while ((line = responseReader.readLine())!= null) { 
				//if (line.startsWith("<?xml "))	//DS:This condition is used when response contains non-xml data also
				//{
					stringBuffer.append(line);
					stringBuffer.append("\n");
				//}
			}				

			/** Printing Response **/
			//System.out.println(stringBuffer.toString()); 

			/** Writing Response to XML file **/
			String date = WebHelper.frmDate.toString().replaceAll("[-/: ]","");
			String fileName = WebHelper.testcaseID.toString() + "_" + WebHelper.transactionType.toString() + "_"+date;
			responseXml = Automation.configHashMap.get("RESULTS_PATH").toString() +"XMLOutput\\" + fileName + ".xml";			
			
			File file = new File(responseXml);
			String content = stringBuffer.toString();
			FileOutputStream fop = new FileOutputStream(file);						
			if (!file.exists())
				file.createNewFile();
			byte[] contentInBytes = content.getBytes();
			fop.write(contentInBytes);
			fop.flush();
			fop.close();	
			
		}
		catch(Exception e)
		{
			throw new Exception("Error while triggering web service: " + e.getMessage());
		}
		finally
		{			
			/** Closing input and output stream buffers **/
			requestWriter.close();
			responseReader.close();
			scanner.close();
		}
	}
	
	public static void callRESTWebService(String requestType, String controlId) throws Exception
	{
		OutputStreamWriter requestWriter = null;
		BufferedReader responseReader = null;
		Scanner scanner = null;
		String soapMessage = "";
		HttpURLConnection con= null;
		String outputExtension="";

		try{ 	

			URL wsdlUrl = new URL(WebHelper.request_url);
			con = (HttpURLConnection)wsdlUrl.openConnection(); 

			/** Proxy settings ONLY if required **/
			System.setProperty("http.proxyHost", "192.168.100.40");
			System.setProperty("http.proxyPort", "8080");
			con.usingProxy();
			con.setDoOutput(true);
			
			if (requestType.equalsIgnoreCase("post")){

				con.setRequestMethod("POST");
				con.setRequestProperty("Content-type", "text/xml; charset=utf-8");
				//con.setRequestProperty("SOAPAction",WebHelper.request_url);				
				con.setDoInput(true);
				
				/** Reading data from Request XML **/
				String reqXml = Automation.configHashMap.get("INPUT_DATA_FILEPATH").toString() + "WebService\\" + WebHelper.request_xml + ".xml";
				//String reqXml = System.getProperty("user.dir") + "\\Resources\\Input\\WebService\\" + WebHelper.request_xml + ".xml";
				//String soapMessage = new Scanner(new File(reqXml)).useDelimiter("\\A").next();
				scanner = new Scanner(new File(reqXml));
				soapMessage = scanner.useDelimiter("\\A").next();
				
				/** Sending Request **/
				requestWriter = new OutputStreamWriter(con.getOutputStream());
				requestWriter.write(soapMessage);
				requestWriter.flush();
			}
			
			else if (requestType.equalsIgnoreCase("get")) {
				con.setRequestMethod("GET");
			}		

			

			/** Reading data from Response XML**/
			responseReader = new BufferedReader(new InputStreamReader(con.getInputStream())); 
			String line; 
			StringBuffer stringBuffer = new StringBuffer(); 
			while ((line = responseReader.readLine())!= null) { 
				//if (line.startsWith("<?xml "))	//DS:This condition is used when response contains non-xml data also
				//{
					stringBuffer.append(line);
					stringBuffer.append("\n");
				//}
			}				

			/** Printing Response **/
			//System.out.println(stringBuffer.toString()); 

			/** Writing Response to XML file **/
			String date = WebHelper.frmDate.toString().replaceAll("[-/: ]","");
			String fileName = WebHelper.testcaseID.toString() + "_" + WebHelper.transactionType.toString() + "_"+date;
			if (controlId.equalsIgnoreCase("JSON")){
				outputExtension=".json";
			}
			else{
				outputExtension=".xml";
			}
			responseXml = Automation.configHashMap.get("RESULTS_PATH").toString() +"XMLOutput\\" + fileName + outputExtension;		
			//responseXml = System.getProperty("user.dir") + "\\Resources\\Results\\XMLOutput\\" + fileName + outputExtension;			
			
			File file = new File(responseXml);
			String content = stringBuffer.toString();
			FileOutputStream fop = new FileOutputStream(file);						
			if (!file.exists())
				file.createNewFile();
			byte[] contentInBytes = content.getBytes();
			fop.write(contentInBytes);
			fop.flush();
			fop.close();	
			
		}
		catch(Exception e)
		{
			throw new Exception("Error while triggering web service: " + e.getMessage());
		}
		finally
		{			
			/** Closing input and output stream buffers **/
			if (requestWriter!=null){
				requestWriter.close();
			}
			if (responseReader!=null){
				responseReader.close();
			}
			if (scanner!=null){
				scanner.close();
			}			
		}
	}
	
	public static String getXMLTagValue(String xmlTagName) throws Exception
	{
		String tagValue = null;
		File fXmlFile = new File(responseXml);
			
		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			//First matching node						
			Node firstNode = doc.getElementsByTagName(xmlTagName).item(0);
			tagValue = firstNode.getTextContent().toString();			
			return tagValue;

		}
		catch(Exception e)
		{
			throw new Exception("Error while XML tag verification: " + e.getMessage());
		}	
	}
	
	/*public static String getJSONTagValue(String nodeName) throws Exception
	{		
			
		try{
						
			// read the json file
			FileReader reader = new FileReader(responseXml);

			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
			
			
			if (!nodeName.contains(".")){
				String nodeValue =  (String) jsonObject.get(nodeName);
				System.out.println(nodeValue);
				return nodeValue;
			}
			else{
				JSONArray arrayChildNodes=null;
				String[] nodeNamesSplitted = nodeName.split("\\.");
				for (int iCount=0;iCount<=nodeNamesSplitted.length;iCount++){
					
			         System.out.println(nodeNamesSplitted[iCount]);
			         if (iCount == 0){
			        	 arrayChildNodes= (JSONArray) jsonObject.get(nodeNamesSplitted[iCount]);
			         }			         
			        
			         Iterator i = arrayChildNodes.iterator();

						// take each value from the json array separately
						while (i.hasNext()) {
							JSONObject innerObj = (JSONObject) i.next();
							
							if (innerObj.containsKey(nodeNamesSplitted[iCount+1]) && iCount<nodeNamesSplitted.length){
								jsonObject=(JSONObject) innerObj.get(nodeNamesSplitted[iCount+1]);
								
								arrayChildNodes= new JSONArray();
								
								Iterator<JSONObject> iterator = jsonObject.keySet().iterator();
							
								while (iterator.hasNext()) {
																		
									arrayChildNodes.add(iterator.next());

									 //DO what ever you whont with jsonChildObject 

									  
									}
								
																
								break;
							}
							else if (iCount==nodeNamesSplitted.length){
								return (String) innerObj.get(nodeNamesSplitted[iCount]);
							}
							else{
								System.out.println("Key " + nodeNamesSplitted[iCount+1] + " Not Found");
							}
							
							
						}
				
				}
				
				
				
			}
			
			return "";

			// get a String from the JSON object
			

		}
		catch(Exception e)
		{
			throw new Exception("Error while XML tag verification: " + e.getMessage());
		}	
	}*/
	
	public static String getJSONTagValue(String nodeName) throws Exception
	{		
			
		try{
						
			// read the json file
			FileReader reader = new FileReader(responseXml);

			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
			
			
			if (!nodeName.contains(".")){
				String nodeValue =  (String) jsonObject.get(nodeName);
				System.out.println(nodeValue);
				return nodeValue;
			}
			else{
				JSONArray arrayChildNodes=null;
				String[] nodeNamesSplitted = nodeName.split("\\.");
				for (int iCount=0;iCount<=nodeNamesSplitted.length;iCount++){
					
			         System.out.println(nodeNamesSplitted[iCount]);
			         if (iCount == 0){
			        	 arrayChildNodes= (JSONArray) jsonObject.get(nodeNamesSplitted[iCount]);
			         }			         
			         
			         Iterator i = arrayChildNodes.iterator();

						// take each value from the json array separately
						while (i.hasNext()) {
							JSONObject innerObj = (JSONObject) i.next();
							
							if (innerObj.containsKey(nodeNamesSplitted[iCount+1]) && iCount<nodeNamesSplitted.length){
								jsonObject=(JSONObject) innerObj.get(nodeNamesSplitted[iCount+1]);
								
								arrayChildNodes= new JSONArray();
								
								Iterator<JSONObject> iterator = jsonObject.keySet().iterator();
							
								while (iterator.hasNext()) {
																		
									arrayChildNodes.add(iterator.next());

									 //DO what ever you whont with jsonChildObject 

									  
									}
								
																
								break;
							}
							else if (iCount==nodeNamesSplitted.length){
								return (String) innerObj.get(nodeNamesSplitted[iCount]);
							}
							else{
								System.out.println("Key " + nodeNamesSplitted[iCount+1] + " Not Found");
							}
							
							
						}
				
				}
				
				
				
			}
			
			return "";

			// get a String from the JSON object
			

		}
		catch(Exception e)
		{
			throw new Exception("Error while XML tag verification: " + e.getMessage());
		}	
	}
	
	/**
	 * This method captures the JSON Reponse into a txt file
	 * @param JSONURL
	 * @param responseFileName
	 * @throws Exception
	 * @throws IOException
	 */
	public static void downloadAndStoreJson(String JSONURL, String responseFileName) throws Exception, IOException {

		InputStream input = null;
		OutputStream output = null;
		// Proxy Configuration
		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.100.40", 8080));
		try {
			final URL url = new URL(JSONURL);
			final URLConnection urlConnection = url.openConnection(proxy);
			input = urlConnection.getInputStream();
			output = new FileOutputStream(responseFileName);
			byte[] buffer = new byte[1024];
			for (int length = 0; (length = input.read(buffer)) > 0;) {
				output.write(buffer, 0, length);
			}
			// Here you could append further stuff to `output` if necessary.
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException logOrIgnore) {
				}
			if (input != null)
				try {
					input.close();
				} catch (IOException logOrIgnore) {
				}
		}
	}
}
