package domain;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

public class Message implements Serializable, Comparable {

	private String from, to, body, type;
	private Date timestamp;
	
	public Message(String from, String to, String body) {
		this.from = from;
		this.to = to;
		this.body = body;
		this.timestamp = null;
	}

	public Message(String from, String body) {
		this.from = from;
		this.to = null;
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
		this.timestamp = new Date();
		this.timestamp.setTime(milliseconds);
	}

	public String getHumanDateString() {
		Date date = this.timestamp;
		StringBuilder sb = new StringBuilder();
		sb.append(date.getDate());
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
	
	/**
	 * Este metodo retorna o formato em que
	 * uma mensagem eh guardada
	 * @return
	 * 		String com o formato a persistir
	 */
	public String toStoreFormat(){
		StringBuilder sb = new StringBuilder();
		sb.append(this.getTimeInMiliseconds() + " ");
		sb.append(this.getFrom() + " ");
		sb.append(this.getType() + " ");
		sb.append(this.getBody() + "\n");
		return sb.toString();
	}


	@Override
	public int compareTo(Object o) {
		if (o == null)
			return -1;
		Message other = (Message) o;
		return this.timestamp.compareTo(other.getTimestamp());
	}
}
