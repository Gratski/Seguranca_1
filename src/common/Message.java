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
		this.timestamp = new Date();
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

	public Date getTimestamp() {
		return timestamp;
	}

	public void setType(String type){
		this.type = type;
	}
	
	public String getDateString() {
		Date now = getTimestamp();
		StringBuilder sb = new StringBuilder();
		sb.append(now.getDate());
		sb.append("-");
		sb.append(now.getMonth());
		sb.append("-");
		sb.append(now.getYear() + 1900);
		sb.append(" ");
		sb.append(now.getHours());
		sb.append(":");
		sb.append(now.getMinutes());
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
