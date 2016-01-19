/*
 * Copyright(c) 2016 Mastek Ltd. All rights reserved.
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

package swift.selenium;

import java.sql.*;

public class JDBCConnection {

	protected static ResultSet rs = null;
	protected static Connection c = null;
	protected static Statement st = null;


	public static ResultSet establishExcelConn(String filePath,String sheetName) throws SQLException, ClassNotFoundException
	{

		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		c = DriverManager.getConnection("jdbc:odbc:Driver={Microsoft Excel Driver (*.xls)};DBQ="+ filePath + ";DriverID=22;READONLY=false;");
		st = c.createStatement();
		rs = st.executeQuery("Select * from ["+ sheetName +"$]");
		return rs;
	}

	public static ResultSet establishDBConn(String filePath,String Query) throws SQLException, ClassNotFoundException
	{

		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());//oracle.jdbc.driver.OracleDriver()
		//Class.forName("com.mysql.jdbc.Driver");
		c = DriverManager.getConnection("jdbc:oracle:thin:@192.xx.xxx.xxx:1521:dbinstance", "new_release", "RELEASEDUJUE");

		st = c.createStatement();
		rs = st.executeQuery(Query);
		return rs;
	}

	public static int getRowCount(ResultSet rs) throws SQLException
	{
		int rowCount = 0 ;
		while(rs.next())
		{
			rowCount += 1;
		}

		return rowCount;

	}
}