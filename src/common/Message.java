package common;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {

	private String from, to, body, type;
	private Date timestamp;
	
	public Message(String from, String to, String body) {
		this.from = from;
		this.to = to;
		this.body = body;
		this.timestamp = null;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public String getBody() {
		return body;
	}
	
	public String getType(){
		return this.type;
	}

	public void setType(String type){
		this.type = type;
	}

	public void setTimestampNow(){
		this.timestamp = new Date();
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public long getTimeInMiliseconds() {
		return this.timestamp.getTime();
	}

	public void setTimeInMilliseconds(long milliseconds) {
		this.timestamp.setTime(milliseconds);
	}

	public String getHumanDateString() {
		Date date = this.timestamp;
		StringBuilder sb = new StringBuilder();
		sb.append(date.getDate() + " ");
		sb.append("-");
		sb.append(date.getMonth());
		sb.append("-");
		sb.append(date.getYear() + 1900);
		sb.append(" ");
		sb.append(date.getHours());
		sb.append(":");
		sb.append(date.getMinutes());
		return sb.toString();
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Message{");
		sb.append("from='").append(from).append('\'');
		sb.append(", to='").append(to).append('\'');
		sb.append(", body='").append(body).append('\'');
		sb.append(", timestamp=").append(timestamp);
		sb.append('}');
		return sb.toString();
	}
	
	
}
