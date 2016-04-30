package domain;

import security.GenericSignature;

import java.io.Serializable;
import java.security.cert.Certificate;
import java.util.Comparator;
import java.util.Date;

/**
 * This class represents an entity: Message
 * A message flows always from a user to a contact
 * This contact can either be a group name or a user name
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class Message implements Serializable, Comparable {

	/**
	 * message sender name
	 */
	private String from;
	
	/**
	 * message receiver name
	 * can be either a user name or a group name
	 */
	private String to;
	
	/**
	 * message body
	 */
	private String body;
	
	/**
	 * message type
	 * it can be text type -t
	 * it can be file type -f
	 */
	private String type;
	
	/**
	 * sent date
	 */
	private Date timestamp;
	
	/**
	 * message signature
	 */
	private GenericSignature signature;
	
	/**
	 * message ciphered key
	 */
	private byte[] key;

	/**
	 * Constructor
	 *
	 * @param from, sender name
	 * @param to, receiver name
	 * @param body, message content
     */
	public Message(String from, String to, String body) {
		this.from = from;
		this.to = to;
		this.body = body;
		this.timestamp = null;
	}

	/**
	 * Constructor
	 *
	 * @param from, sender name
	 * @param body, message content
     */
	public Message(String from, String body) {
		this.from = from;
		this.to = null;
		this.body = body;
		this.timestamp = null;
	}

	/**
	 * Gets message key
	 * 
	 * @return the key
	 */
	public byte[] getKey() {
		return key;
	}

	/**
	 * Sets a new value to ciphered key
	 * 
	 * @param key, new key value
	 */
	public void setKey(byte[] key) {
		this.key = key;
	}

	/**
	 * Gets the message signature
	 * 
	 * @return the message signature
	 */
	public GenericSignature getSignature() {
		return signature;
	}
	
	/**
	 * Sets message signature
	 * 
	 * @param signature, new message signature 
	 */
	public void setSignature(GenericSignature signature) {
		this.signature = signature;
	}

	/**
	 * Gets sender name
	 *
	 * @return String, sender name
     */
	public String getFrom() {
		return from;
	}

	/**
	 * Gets message receiver name
	 *
	 * @return String, receiver name
     */
	public String getTo() {
		return to;
	}

	/**
	 * Gets message content
	 *
	 * @return String, message content
     */
	public String getBody() {
		return body;
	}

	/**
	 * Gets message type
	 *
	 * @return String, message type
     */
	public String getType(){
		return this.type;
	}

	/**
	 * Sets message type
	 *
	 * @param type, new message type
     */
	public void setType(String type){ this.type = type; }

	/**
	 * Sets message timestamp to the exact moment
	 * this method is called
	 */
	public void setTimestampNow(){
		this.timestamp = new Date();
	}

	/**
	 * Gets messagem timestamp
	 *
	 * @return Date, message timestamp
     */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * Gets message timestamp in milliseconds
	 *
	 * @return long, message timestamp
     */
	public long getTimeInMiliseconds() {
		return this.timestamp.getTime();
	}

	/**
	 * Sets message timestamp value
	 *
	 * @param milliseconds, new timestamp in milliseconds
     */
	public void setTimeInMilliseconds(long milliseconds) {
		this.timestamp = new Date();
		this.timestamp.setTime(milliseconds);
	}

	/**
	 * Prints message in a human readable format
	 *
	 * @require name != null
     */
	public void printMessageFromPerspective(String name) {
		String personInPerspective = (name.equals(this.from) ? "me" : this.from);
		System.out.println(personInPerspective + ": " + this.body);
		System.out.println(getHumanDateString());
	}

	/**
	 * Sets message body
	 * 
	 * @param body, new message body
	 */
	public void setBody(String body){
		this.body = body;
	}
	
	/**
	 * Gets message timestamp in human readable format
	 *
	 * @return message timestamp in a readable format
     */
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
		if (date.getMinutes() < 10)
			sb.append(0);
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

	@Override
	public int compareTo(Object o) {
		if (o == null)
			return -1;
		Message other = (Message) o;
		return this.timestamp.compareTo(other.getTimestamp());
	}
}
