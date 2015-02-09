/*
 * Copyright(c) 2015 Mastek Ltd. All rights reserved.
 * 
 *	SwiftLite is distributed in the hope that it will be useful.
 *
 *	This file is part of SwiftLite Framework: Licensed under the Apache License, 
 *	Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 * 
 *	http://www.apache.org/licenses/LICENSE-2.0
 * 
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and 
 *	limitations under the License.
 */

package SwiftSeleniumWeb;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public  class CalendarSnippet {
	private static final String TASKLIST = "tasklist";
	private static String KILL = "\\System32\\taskkill /F /IM ";	

	/**
	 * This method returns month name for month number e.g. January for 1, February for 2 and so on
	 * 
	 * @param monthInt i.e. integer number of the month for which month name is required
	 * @return monthName i.e. January, February etc.
	 */
	public static String getMonthForInt(int monthInt) {
		String monthName = "invalid";
		monthInt = monthInt-1;
		DateFormatSymbols dfs = new DateFormatSymbols();
		String[] months = dfs.getMonths();
		if (monthInt >= 0 && monthInt <= 11 ) {
			monthName = months[monthInt];
		}
		return monthName;
	}
	
	/**
	 * This method returns short month name for month number i.e. Jan for 1, Feb for 2 and so on
	 * 
	 * @param monthInt - month number 1 for Jan, 2 for Feb and so on
	 * @return monthName - Jan for 1, Feb for 2 and so on
	 */
	public static String getShortMonthForInt(int monthInt) {
		String monthName = "invalid";
		monthInt = monthInt-1;
		DateFormatSymbols dfs = new DateFormatSymbols();
		String[] months = dfs.getShortMonths();
		if (monthInt >= 0 && monthInt <= 11 ) {
			monthName = months[monthInt];
		}
		return monthName;
	}	
	
	/**
	 * This method returns month number as 01, 02.....12 for month name i.e. Jan or January, Feb or February
	 * 
	 * @param monthName [Jan or January, Feb or February]
	 * @return monthInt [01 for Jan, 02 for Feb]
	 * @throws ParseException 
	 */
	
	public static String getMonthForString(String monthName) throws ParseException{
		Calendar cal = Calendar.getInstance();
		cal.setTime(new SimpleDateFormat("MMM").parse("december"));
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(2);
		String monthInt = nf.format(cal.get(Calendar.MONTH) + 1);
		
		return monthInt;
	}

	
	public static boolean isProcessRunning(String serviceName) throws Exception {

		Process p = Runtime.getRuntime().exec(TASKLIST);		
		BufferedReader reader = new BufferedReader(new InputStreamReader( p.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
			if (line.contains(serviceName)) {
				return true;
			}
		}
		return false;
	}

	public static void killProcess(String serviceName) throws Exception
	{
		KILL =System.getenv("SystemRoot") +  KILL;
		Runtime.getRuntime().exec(KILL + serviceName);
	}
}

