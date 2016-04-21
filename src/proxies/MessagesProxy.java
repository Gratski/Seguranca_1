package proxies;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import domain.Message;
import security.CipheredKey;
import security.GenericSignature;
import security.SecUtils;

/**
 * Esta classe é responsável por persistir as Messages em ficheiros
 * Class é Singleton
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class MessagesProxy extends Proxy {

	private static MessagesProxy instance = null;

	/**
	 * Constructor for MessagesProxy
	 * @throws IOException
     */
	private MessagesProxy() throws IOException {}

	/**
	 * Função para obter a instância de MessagesProxy
	 *
	 * @return messagesProxy to be used and persist Messages
	 * @throws IOException
     */
	public static MessagesProxy getInstance() throws IOException {
		if(instance == null)
			instance = new MessagesProxy();
		return instance;
	}
	
	/**
	 * Regista uma mensagem em ficheiro
	 * @param path
	 * 		Path onde a mensagem vai ser registada
	 * @param fname
	 * 		Nome de ficheiro de mensagem
	 * @param msg
	 * 		Mensagem a ser registada
	 * @return
	 * 		true se registou, false caso contrario
	 */
	public boolean persist(String path, String fname, Message msg, Map<String, CipheredKey> keys, GenericSignature sign ) {
		File dir = new File(path + "/" + fname);
		if(!dir.exists())
			dir.mkdirs();
		
		//grava mensagem
		File file = new File(path + "/" + fname + "/" + "body" + MESSAGE_FILE_EXTENSION);
		if (file.exists())
			return false;
		try {
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(toStoreFormat(msg));
			bw.close();
			fw.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Erro ao persistir mensagem em " + path);
			return false;
		}
		
		//grava assinatura
		file = new File(path + "/" + fname + "/" + "signature.sign");
		if(file.exists())
			return false;
		try {
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(SecUtils.getHexString(sign.getSignature()));
			bw.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//grava autor
		file = new File(path + "/" + fname + "/" +"author");
		if(file.exists())
			return false;
		try {
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(msg.getFrom());
			bw.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//grava chaves
		Set<String> chaves = keys.keySet();
		Iterator<String> it = chaves.iterator();
		try {
			while(it.hasNext()){
				String name = it.next();
				CipheredKey key = keys.get(name);
				file = new File(path + "/" + fname + "/"+name+".key");
				file.createNewFile();
				FileWriter fw = new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fw);
				
				bw.write(SecUtils.getHexString(key.getKey()));
				bw.close();
				fw.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return true;
	}

	/**
	 * Private message that has the format in which to store the message in the file
	 * @param msg
	 * 			Message that needs to be stored
	 * @return The message format to be saved in the message file
     */
	private String toStoreFormat(Message msg){
		StringBuilder sb = new StringBuilder();
		sb.append(msg.getTimeInMiliseconds() + " ");
		sb.append(msg.getFrom() + " ");
		sb.append(msg.getType() + " ");
		sb.append(msg.getBody() + "\n");
		return sb.toString();
	}
}