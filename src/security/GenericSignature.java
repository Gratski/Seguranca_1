package security;

import java.io.*;
import java.security.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Class that represents a Signature of a Message or a File
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class GenericSignature implements Serializable{

	/**
	 * Byte array that represents the signature
	 */
	private final byte[] signature;

	/**
	 * Constructor
	 *
	 * @param hash Byte array that represents the signature
     */
	public GenericSignature(byte[] hash) {
		this.signature = hash;
	}

	/**
	 * Getter for the signature
	 *
	 * @return Byte array which is the signature
     */
	public byte[] getSignature(){
		return this.signature;
	}

	/**
	 * Creates a signature of the content of the byte array
	 * received and signs it with privateKey
	 *
	 * @param privateKey
	 * 		The private key used to sign the signature
	 * @param content
	 * 		The byte array that represents content to be signed
	 * @return
	 * 		GenericSignature object which holds the signature
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
     * @throws SignatureException
     */
	public static GenericSignature createGenericMessageSignature(PrivateKey privateKey, byte[] content) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, SignatureException {
		//gera sintese da mensagem
		MessageDigest md = HashService.getMessageDigest();
		byte[] hash = md.digest(content);

		//assina cifra da mensagem
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(privateKey);
		signature.update(hash);
		return new GenericSignature(signature.sign());
	}

	/**
	 * Creates a signature of the file received and signs it with privateKey
	 *
	 * @param privateKey
	 * 		The private key used to sign the signature
	 * @param file
	 * 		The file whoose content will be signed
	 * @return
	 * 		GenericSignature object which holds the signature
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
     * @throws SignatureException
     */
	public static GenericSignature createGenericFileSignature(PrivateKey privateKey, 
			File file) throws NoSuchAlgorithmException, IOException, 
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, 
			BadPaddingException, SignatureException {
		
		//gerar sintese de ficheiro
		MessageDigest md = HashService.getMessageDigest();
		byte[] hash = new byte[16];
		int sent = 0;
		FileInputStream fis = new FileInputStream(file);
		while ((sent = fis.read(hash)) > 0) {
			md.update(hash, 0, sent);
		}
		hash = md.digest();
		
		//assinar sintese de ficheiro
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(privateKey);
		signature.update(hash);		
		return new GenericSignature(signature.sign());
	}

	/**
	 * Creates a new signature from the path to the file
	 *
	 * @param path
	 * 		The path to the file which contains the signature in String format
	 * @return
	 * 		GenericSignature object which holds the signature
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
	public static GenericSignature readSignatureFromFile(String path) throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		BufferedReader bis = new BufferedReader(new FileReader(path));
		String signatureAsString = bis.readLine();
		bis.close();
		return new GenericSignature(SecUtils.getStringHex(signatureAsString));
	}
}