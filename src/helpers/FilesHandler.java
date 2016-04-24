package helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import client.MyWhats;
import domain.User;
import security.CipherFactory;
import security.CipheredKey;
import security.GenericSignature;
import security.HashService;
import security.SecUtils;

/**
 * Esta classe é responsável pelo download e upload de ficheiros
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class FilesHandler {

	/**
	 * Numero de bytes a ser transmitido de cada vez
	 */
	private static final int CHUNK = 1024;

	/**
	 * Construtor da class FilesHandler
	 */
	public FilesHandler() {}

	/**
	 *	Método que envia o File file, em CHUNK bytes de cada vez
	 *
	 * @param conn
	 * 		Connection que detem o socket por onde vai ser enviado o ficheiro
	 * @param file
	 * 		Ficheiro a ser enviado pela rede
	 * @return
	 * 		Retorna verdadeiro se enviar o mesmo número de bytes que o tamanho em bytes do ficheiro file
	 *
	 * @requires conn != null && file != null
	 * @throws IOException
	 * @throws NoSuchAlgorithmException 
	 * @throws SignatureException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws InvalidKeyException 
	 * @throws KeyStoreException 
	 * @throws CertificateException 
     */
	public boolean send(Connection conn, ArrayList<String> members, User user, File file) throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, SignatureException, CertificateException, KeyStoreException {
		byte[] byteArr;
		FileInputStream fs = new FileInputStream(file);
		
		//gerar chave secreta
		Key key = SecUtils.generateSymetricKey();
		
		//gerar sintese
		byte[] hash = HashService.createFileHash(file);
		
		//assinar
		GenericSignature gs = GenericSignature.createGenericFileSignature(user.getPrivateKey(), file);
		
		//enviar assinatura
		conn.getOutputStream().writeObject(gs);
		
		//envia filesize
		long fileSize = file.length() * 16;
		conn.getOutputStream().writeLong(fileSize);
		
		//cifrar ficheiro e enviar
		Cipher c = CipherFactory.getStandardCipher();
		c.init(Cipher.ENCRYPT_MODE, key);
		FileInputStream fis = new FileInputStream(file);
		CipherOutputStream cos = new CipherOutputStream(conn.getOutputStream(), c);
		
		byte[] buf = new byte[16];
		int sent = 0;
		while((sent=fis.read(buf))>-1){
			cos.write(buf, 0, sent);
		}
		cos.close();
		
		//enviar chave K cifrada com chave publica de members
		Map<String, Certificate> certs = MyWhats.getCertificates(members, user);
		certs.put(user.getName(), user.getCertificate());
		Map<String, CipheredKey> keys = MyWhats.cipherAllKeys(certs, key);
		conn.getOutputStream().writeObject(keys);
		
		//send filesize
		conn.getOutputStream().writeLong(file.length());
		conn.getOutputStream().flush();
		
		//return totalSent == fileSize;
		return true;
	}

	/**
	 *	Método que recebe da rede o ficheiro com nome filename, em CHUNK bytes de cada vez
	 *  Recebe o tamanho do ficheiro primeiro e depois vai escrevendo em disco à medida que recebe
	 *
	 * @param conn
	 * 		Connection que detem o socket por onde vai ser recebido o ficheiro
	 * @param dir
	 * 		Path onde o ficheiro vai ser gravado
	 * @param filename
	 * 		Nome do ficheiro a ser enviado pela rede
	 * @return
	 * 		Retorna verdadeiro se receber o mesmo número de bytes que o tamanho em bytes do ficheiro filename
	 *
	 * @requires conn != null && filename != null
     * @throws Exception
     */
	public File receive(Connection conn, String dir, String filename, long fileSize) throws Exception {
		byte[] byteArr = new byte[CHUNK];

		//receiving filename
		File file = new File(dir + "/" + filename);
		file.getParentFile().mkdirs();
		FileOutputStream out = new FileOutputStream(dir + "/" + filename);
		
		
		long totalRead = 0;
		
		//receiving file itself
		while (totalRead < fileSize) {
			System.out.println("READING...");
			int cur = conn.getInputStream().read(byteArr);
			
			if (cur == -1)
				continue;
			
			out.write(byteArr, 0, cur);
			totalRead += cur;
			System.out.println("TOTAL READ: " + totalRead);
		}
		out.close();
		return totalRead == fileSize ? file : null;
	}

	/**
	 * Método que testa se um ficheiro já existe no path recebido
	 * @param path
	 * 		Path para verificar se o ficheiro existe
	 * @return
	 * 		Retorna True se o ficheiro em path já existir, false caso contrário
	 * @requires
	 * 		path != null
     */
	public boolean existsFile(String path) {
		return new File(path).exists();
	}
	
	/**
	 * Abre um canal de leitura de ficheiro
	 * @param f, File a considerar
	 * @return o canal de leitura aberto
	 * @throws IOException
	 */
	public BufferedReader getReader(File f) throws IOException{
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		return br;
	}
	
	/**
	 * Abre um canal de escrita de ficheiro
	 * @param f, File a considerar
	 * @return o canal de escrita aberto
	 * @throws IOException
	 */
	public BufferedWriter getWriter(File f) throws IOException{
		FileWriter fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);
		return bw;
	}
	
	
}