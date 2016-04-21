package domain;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

/**
 * Esta classe representa a entidade Message
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class Message implements Serializable, Comparable {

	private String from, to, body, type;
	private Date timestamp;

	/**
	 * Constructor
	 *
	 * @param from 	Nome do autor
	 * @param to 	Nome do destinatario
	 * @param body 	Conteudo da mensagem
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
	 * @param from 	Nome do autor
	 * @param body	Conteudo da mensagem
     */
	public Message(String from, String body) {
		this.from = from;
		this.to = null;
		this.body = body;
		this.timestamp = null;
	}

	/**
	 * Obtem o nome do autor
	 *
	 * @return String
     */
	public String getFrom() {
		return from;
	}

	/**
	 * Obtem o destinatario
	 *
	 * @return String
     */
	public String getTo() {
		return to;
	}

	/**
	 * Obtem o conteudo
	 *
	 * @return String
     */
	public String getBody() {
		return body;
	}

	/**
	 * Obtem tipo de mensagem
	 *
	 * @return String
     */
	public String getType(){
		return this.type;
	}

	/**
	 * Altera tipo de mensagem
	 *
	 * @param type Novo tipo de mensagem
     */
	public void setType(String type){ this.type = type; }

	/**
	 * Altera o valor de timestamp da mensagem
	 * para timestamp do instante da invocacao
	 * deste metodo
	 */
	public void setTimestampNow(){
		this.timestamp = new Date();
	}

	/**
	 * Obtem o timestamp da mensagem
	 *
	 * @return Date timestamp
     */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * Obtem o timestamp me Milisegundos
	 *
	 * @return long milisegundos
     */
	public long getTimeInMiliseconds() {
		return this.timestamp.getTime();
	}

	/**
	 * Altera o timestamp da mensagem
	 *
	 * @param milliseconds Novo valor timestamp em milisegundos
     */
	public void setTimeInMilliseconds(long milliseconds) {
		this.timestamp = new Date();
		this.timestamp.setTime(milliseconds);
	}

	/**
	 * Obtem a data readable
	 *
	 * @return mensagem em String num formato perceptivel
	 * @require name != null
     */
	public void printMessageFromPerspective(String name) {
		String personInPerspective = (name.equals(this.from) ? "me" : this.from);
		System.out.println(personInPerspective + ": " + this.body);
		System.out.println(getHumanDateString());
	}

	public void setBody(String body){
		this.body = body;
	}
	
	/**
	 * Obtem data
	 *
	 * @return data num formato perceptivel
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
