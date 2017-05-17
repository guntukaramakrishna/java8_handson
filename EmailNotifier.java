package com.aexp.alerts.notifier;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.aexp.alerts.MailClient;
import com.aexp.alerts.model.EventLog;
import com.aexp.alerts.model.ServerDetails;
import com.aexp.alerts.util.DBUtil;

public class EmailNotifier {

	public void checkServerStatusAndSendEmail(){
		try{
			int internationalCounter = 0, foreignCounter  = 0;
			Map<Integer,List<? extends Object>> offServerDetails = DBUtil.getOffServerDetails();

			Set<Entry<Integer, List<? extends Object>>> entrySets = offServerDetails.entrySet();

			for(Entry<Integer, List<? extends Object>> entrySet: entrySets){
				//entrySet.getKey();
				List list = entrySet.getValue();
				ServerDetails serverDetails = (ServerDetails)list.get(0);
				String location = serverDetails.getLocation();

				if(location.contains("Singapore") || location.contains("Guragaon")){
					internationalCounter++;
				}
				else if(location.contains("US")){
					foreignCounter++;
				}
			}
			StringBuilder email = new StringBuilder();
			// This handles all 3 conditions. 
			// if all are OFF, or if only foriegn are OFF, or if international are OFF
			if(internationalCounter >= 3 || foreignCounter >=3) {
				// send an email
				for(Entry<Integer, List<? extends Object>> entrySet: entrySets){

					List list = entrySet.getValue();
					ServerDetails serverDetails = (ServerDetails)list.get(0);

					// you have these all values now to show
					int serverId = serverDetails.getServerId();
					String locaiton = serverDetails.getLocation();
					String ipAddress = serverDetails.getIpAddress();
					String description = serverDetails.getDescription();
					String hostname = serverDetails.getHostname();

					String timestamp = (String)list.get(1);

					email.append("<html><body>"
							+ "<table style='border:2px solid black'>");
					email.append("<tr bgcolor=\"#33CC99\">");
					email.append("<td>");
					email.append("Okta server: ")
					.append("Host: ").append(hostname);
					email.append("</td>");

					email.append("<td>");
					email.append("IP: ").append(ipAddress);
					email.append("</td>");

					email.append("<td>");
					email.append("Location: ").append(locaiton);
					email.append("</td>");

					email.append("<td>");
					email.append("Hostname: ").append(hostname);
					email.append("</td>");

					email.append("<td>");
					email.append("Description: ").append(description);
					email.append("</td>");

					email.append("<td>");
					email.append("Timestamp (MST): ").append(timestamp);
					email.append("</td>");

					email.append("</tr>");
					email.append("</table></body></html>");
				}

				String subject = "Okta radius call failed on";

				MailClient mc = new MailClient();
				mc.sendMail(email.toString(), subject) ;

			}

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void checkStatusAndSendEMail(){
		DBUtil util = new DBUtil();
		try {
			List<EventLog> events = util.getEvents();
			StringBuilder email = new StringBuilder();
			//get ServerDetails
			Map<Integer, Object> serverDetailsMap = DBUtil.getServerDetails();

			//get Error Details
			Map<String,String> errorDetailsMap = DBUtil.getErrorDetails();

			email.append("<html><body>"
					+ "<table style='border:2px solid black'>");
			if(null != events && events.size() > 0){

				for(EventLog event: events){

					ServerDetails sd = (ServerDetails) serverDetailsMap.get(event.getServerId());
					String errorDesc = "";

					for (Entry<String, String> entry : errorDetailsMap.entrySet()) {
						if (Integer.parseInt(entry.getValue())==event.getErrorId()) {
							errorDesc = entry.getKey();
						}
					}

					email.append("<tr bgcolor=\"#33CC99\">");
					email.append("<td>");
					email.append("Okta server: ")
					.append("Host: ").append(sd.getHostname());
					email.append("</td>");

					email.append("<td>");
					email.append("IP: ").append(sd.getIpAddress());
					email.append("</td>");

					email.append("<td>");
					email.append("Location: ").append(sd.getLocation());
					email.append("</td>");

					email.append("<td>");
					email.append("Timestamp (MST): ").append(event.getEventTimeStamp());
					email.append("</td>");


					email.append("<td>");
					email.append("Error Message: ").append(errorDesc);
					email.append("</td>");

					email.append("</tr>");


				}
				email.append("</table></body></html>");

				String subject = "Okta radius call failed on";

				MailClient mc = new MailClient();
				mc.sendMail(email.toString(), subject) ;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		EmailNotifier en = new EmailNotifier();
		en.checkStatusAndSendEMail();
	}

}
