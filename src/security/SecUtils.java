package security;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Random;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;

import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

/**
 * Class that has many Security functions and utilities
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class SecUtils {

    /**
     * Creates a string hex representation of a byte array 
     * @param raw, byte array to be considered
     * @return byte array hex representation
     */
	public static String getHexString( byte [] raw ) {
		return DatatypeConverter.printHexBinary(raw);
	}
	  
	/**
	 * Creates a symmetric key
	 * @return symmetric key
	 * @throws NoSuchAlgorithmException
	 */
	public static Key generateSymetricKey() throws NoSuchAlgorithmException{
		KeyGenerator kg = KeyGenerator.getInstance("AES");
		kg.init(128);
		return kg.generateKey();
	}
	
	/**
	 * Creates a byte array based on a given hex string
	 * @param s, string to be considered
	 * @return byte array
	 */
	public static byte[] getStringHex(String s) {
	    return DatatypeConverter.parseHexBinary(s);
	}
	
	/**
	 * Creates a random salt
	 * @param size, salt size
	 * @return salt
	 */
	public static int generateRandomSalt(int size){
		Random rand = new Random();
		int salt = 0;
		for(int i = 0; i < size; i++){
			salt *= 10;
			salt += rand.nextInt(10);
		}
		return salt;
	}

	/**
	 * Creates self-signed Certificates on the fly using the parameters
	 *
	 * @param filename
	 *		Name to be used as CN
	 * @param publicKey
	 * 		Public key to be used in the Certificate
	 * @param privateKey
	 * 		Private key to sign the Certificate
	 * @return
	 * 		Self-signed Certificate
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 * @throws InvalidKeyException
	 * @throws NoSuchProviderException
     * @throws SignatureException
     */
	public static Certificate generateCertificate(String filename, PublicKey publicKey, PrivateKey privateKey) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeySpecException, InvalidKeyException, NoSuchProviderException, SignatureException{
		
		X509CertInfo certInfo = new X509CertInfo();
		X500Name owner = new X500Name("CN=" + filename);
		BigInteger serialNumber = new BigInteger(64, new SecureRandom());
		CertificateValidity certValidity = getDefaultCertificateValidity();
		
		//detalhes de certificado
		certInfo.set(X509CertInfo.VALIDITY, certValidity);	//validade de certificado
		certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(serialNumber));	//numero de serie do certificado
		certInfo.set(X509CertInfo.SUBJECT, owner); 	//dono do certificado
		certInfo.set(X509CertInfo.ISSUER, owner);	//entidade que gerou o certificado	
		certInfo.set(X509CertInfo.KEY, new CertificateX509Key(publicKey));  //chave publica associada ao certificado
		certInfo.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));	//versao de certificado
		AlgorithmId usedAlgorithm = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);  
		certInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(usedAlgorithm)); //Id de algorithm utilizado
		
		//criar certificado
		X509CertImpl certificate = new X509CertImpl(certInfo);
		//assinar certificado
		certificate.sign(privateKey, "MD5WithRSA");
		
		//actualizacao algorithm
		System.out.println("SENT: " + X509CertImpl.ALG_ID);
		usedAlgorithm = (AlgorithmId) certificate.get(X509CertImpl.NAME + "." + X509CertImpl.ALG_ID);
		certInfo.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, usedAlgorithm);
		
		//reassinar algorithm
		certificate = new X509CertImpl(certInfo);
		certificate.sign(privateKey, "MD5WithRSA");
		
		return certificate;
	}

	/**
	 * Creates a CertificateValidity
	 *
	 * @return CertificateValidity
     */
	private static CertificateValidity getDefaultCertificateValidity(){
		Date from = new Date();
		Date to = new Date(from.getTime() + 10 * 86400000l);
		CertificateValidity validity = new CertificateValidity(from, to);
		return validity;
	}

	/**
	 * Reads a key in String format in a file transforms it
	 * into a byte array format key
	 *
 	 * @param path
	 * 		The path to the file where the key is stored
	 * @return
	 * 		The key in Byte array format
	 * @throws IOException
     */
	public static byte[] readKeyFromFile(String path) throws IOException {
		File fileKey = new File(path);
		BufferedReader bis = new BufferedReader(new FileReader(fileKey));
		String keyAsString = bis.readLine();
		bis.close();
		return SecUtils.getStringHex(keyAsString);
	}

	/**
	 * Creates a secret key based on a given string
	 *
	 * @param str, string to be considered
	 * @return symmetric key
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public static SecretKey getKeyByString(String str) throws NoSuchAlgorithmException, InvalidKeySpecException {
		PBEKeySpec param = new PBEKeySpec(str.toCharArray());
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		return skf.generateSecret(param);
	}
}
