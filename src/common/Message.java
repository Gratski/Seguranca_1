package common;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable{

	private String from, to, body;
	private Date timestamp;
	
	public Message(String from, String to, String body){
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

	public Date getTimestamp() {
		return timestamp;
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
