package com.aexp.alerts.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.aexp.alerts.constants.Constants;
import com.aexp.alerts.model.EventLog;
import com.aexp.alerts.model.ServerDetails;
import com.aexp.alerts.notifier.ServerStatus;

public class DBUtil {

	private static final Logger LOG = Logger.getLogger("DBUtil");

	/**
	 * Code to retrieve server details
	 * 
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public static Map<Integer, Object> getServerDetails() throws SQLException,
			IOException {
		LOG.info("Begin fetching server Details");

		Connection dbConnection = null;
		Statement statement = null;
		Map<Integer, Object> serverDetailsMap = new HashMap<Integer, Object>();
		try {
			dbConnection = ConnectionUtil.getDBConnection();
			statement = dbConnection.createStatement();
			ResultSet rs = statement
					.executeQuery(Constants.SQL_GET_SERVER_DETAILS);
			while (rs.next()) {
				ServerDetails sd = new ServerDetails();
				Integer serverId = rs.getInt(Constants.SERVER_ID);
				sd.setServerId(serverId);
				sd.setIpAddress(rs.getString(Constants.IP_ADDRESS));
				sd.setHostname(rs.getString(Constants.HOSTNAME));
				sd.setLocation(rs.getString(Constants.LOCATION));
				sd.setDescription(rs.getString(Constants.DESCRIPTION));
				serverDetailsMap.put(serverId, sd);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}

		}
		LOG.info("End fetching server Details");
		return serverDetailsMap;
	}

	/**
	 * Retrieving Error Details
	 * 
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */

	public static Map<String, String> getErrorDetails() throws SQLException,
			IOException {
		LOG.info("Begin fetching Error codes");

		Connection dbConnection = null;
		Statement statement = null;
		Map<String, String> errorDetails = new HashMap<String, String>();
		try {
			dbConnection = ConnectionUtil.getDBConnection();
			statement = dbConnection.createStatement();
			ResultSet rs = statement
					.executeQuery(Constants.SQL_GET_ERROR_DETAILS);
			while (rs.next()) {
				errorDetails.put(rs.getString(Constants.ERROR_DESCRIPTION),
						String.valueOf(rs.getInt(Constants.ERROR_ID)));
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}

		}
		LOG.info("End fetching Error Codes");
		return errorDetails;
	}

	/**
	 * get events to be notified
	 * 
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public List<EventLog> getEvents() throws SQLException, IOException {
		LOG.info("Begin fetching to be notified events Details");

		Connection dbConnection = null;
		Statement statement = null;
		// String eventLogQry = "SELECT * FROM dbo.event_log"; //get last three
		// minute records based on Business Logic
		List<EventLog> eventLogList = new ArrayList<EventLog>();
		try {
			dbConnection = ConnectionUtil.getDBConnection();
			statement = dbConnection.createStatement();
			ResultSet rs = statement
					.executeQuery(Constants.SQL_GET_EVENT_DETAILS);
			while (rs.next()) {
				EventLog el = new EventLog();
				el.setEventId(rs.getInt(Constants.EVENT_ID));
				el.setServerId(rs.getInt(Constants.SERVER_ID));
				el.setErrorId(rs.getInt(Constants.EVENT_ERROR_ID));
				el.setEventTimeStamp(rs.getTimestamp(Constants.EVENT_TS));
				el.setCreatedTimeStamp(rs.getTimestamp(Constants.CREATED_TS));
				eventLogList.add(el);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}

		}
		LOG.info("End fetching server Details");
		return eventLogList;

	}

	/**
	 * Inserting into EventLog
	 * 
	 * @param eventTimeStamp
	 * @param serverId
	 * @param errorId
	 * @throws SQLException
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void addEventLog(Timestamp eventTimeStamp, int serverId,
			int errorId) throws SQLException, IOException, ParseException  {

		LOG.info("Started adding records into EVENT_LOG table for serverId: "
				+ serverId);

		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		try {
			dbConnection = ConnectionUtil.getDBConnection();
			preparedStatement = dbConnection.prepareStatement(Constants.SQL_INSERT_EVENT_DETAILS);
			preparedStatement.setTimestamp(1, eventTimeStamp);
			preparedStatement.setInt(2, serverId);
			preparedStatement.setInt(3, errorId);
			preparedStatement.setTimestamp(4, getCurrentTimeStamp());

			// execute insert SQL statement
			preparedStatement.executeUpdate();

			LOG.info("Record is inserted into EVENT_LOG table for serverId: "
					+ serverId);

		} catch (SQLException e) {
			LOG.info(e.getMessage());

		} finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}

		}

	}

	public static List<Integer> getAllServersDown() throws IOException, SQLException{

		LOG.info("Begin fetching Servers down");

		Connection dbConnection = null;
		Statement statement = null;
		List<Integer> serverList = new ArrayList<Integer>();
		try {
			dbConnection = ConnectionUtil.getDBConnection();
			statement = dbConnection.createStatement();
			ResultSet rs = statement
					.executeQuery(Constants.SQL_GET_ALL_SERVER_DOWN);
			while (rs.next()) {
				serverList.add(rs.getInt(Constants.SERVER_ID));
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}

		}
		LOG.info("End fetching Error Codes");
		
		return serverList;
	}
	
	public static void updateServerStatus(List<Integer> list) throws SQLException, IOException, ParseException  {

		LOG.info("Started updating records into SERVER_STATUS table for serverIds: "
				+ list);

		String values = "";
		for(Integer value: list){
			values = values+value+",";
			
		}
		values = values.substring(0,values.lastIndexOf(","));
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		try {
			dbConnection = ConnectionUtil.getDBConnection();
			dbConnection.setAutoCommit(false);
			preparedStatement  = dbConnection.prepareStatement("UPDATE dbo.server_status SET STATUS = 'ON', STATUS_TS = ?");
			preparedStatement.setTimestamp(1, getCurrentTimeStamp());
			preparedStatement.executeUpdate();
			
			String query = String.format(Constants.SQL_UPDATE_SERVER_STATUS, values);
			LOG.info("Query prepared is : "+query);
			preparedStatement = dbConnection.prepareStatement(query);
			preparedStatement.setString(1, "OFF");
			preparedStatement.setTimestamp(2, getCurrentTimeStamp());
			//preparedStatement.setInt(3,serverId);

			// execute update SQL statement
			preparedStatement.executeUpdate();
			LOG.info("Record is updated into SERVER_STATUS table for serverIds: "
					+ list);

			dbConnection.commit();
		} catch (SQLException e) {
			LOG.info(e.getMessage());
			if(dbConnection != null){
				dbConnection.rollback();
			}

		} finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}

		}

	}

	public static Map<Integer,List<? extends Object>> getOffServerDetails() throws IOException, SQLException{
		

		Map<Integer, List<? extends Object>> offServerDetails = new HashMap<Integer,List<? extends Object>>();
		LOG.info("Begin fetching Servers down");

		Connection dbConnection = null;
		Statement statement = null;
		List finalDetails = new ArrayList();
		try {
			dbConnection = ConnectionUtil.getDBConnection();
			statement = dbConnection.createStatement();
			ResultSet rs = statement
					.executeQuery("Your Query");
			while (rs.next()) {
				ServerDetails sd = new ServerDetails();
				
				Integer serverId = rs.getInt(Constants.SERVER_ID);
				sd.setServerId(serverId);
				sd.setIpAddress(rs.getString(Constants.IP_ADDRESS));
				sd.setHostname(rs.getString(Constants.HOSTNAME));
				sd.setLocation(rs.getString(Constants.LOCATION));
				sd.setDescription(rs.getString(Constants.DESCRIPTION));
				
				String timeStamp = rs.getString("STATUS_TS");
				
				finalDetails.add(sd);
				finalDetails.add(timeStamp);
				
				offServerDetails.put(serverId, finalDetails);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}

		}
		LOG.info("End fetching Error Codes");
		
		return offServerDetails;
	}
	
	private static java.sql.Timestamp getCurrentTimeStamp() {
		java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(today.getTime());

	}
}
