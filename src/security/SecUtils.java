package security;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
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

public class SecUtils {

    private static final String HEXES = "0123456789ABCDEF";
	
    /**
     * Gera representacao hexadecimal de array de bytes
     * @param raw, array de bytes a considerar
     * @return string com a representacao hexadecimal de raw
     */
	public static String getHexString( byte [] raw ) {
		return DatatypeConverter.printHexBinary(raw);
	}
	  
	
	
	/**
	 * Gera chave simetrica
	 * @return chave simetrica gerada
	 * @throws NoSuchAlgorithmException
	 */
	public static Key generateSymetricKey() throws NoSuchAlgorithmException{
		KeyGenerator kg = KeyGenerator.getInstance("AES");
		return kg.generateKey();
	}
	
	/**
	 * Converte uma string hexadecimal em array de bytes
	 * @param s, string a considerar
	 * @return array de bytes
	 */
	public static byte[] getStringHex(String s) {
	    return DatatypeConverter.parseHexBinary(s);
	}
	
	/**
	 * Gera salt aleatorio
	 * @param size, tamanho de salt
	 * @return salt em array de bytes
	 */
	public static byte[] generateRandomSalt(int size){
		Random rand = new Random();
		byte[] salt = new byte[size];
		rand.nextBytes(salt);
		return salt;
	}
	
	/**
	 * Cria um novo certificado
	 * @param password
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws InvalidKeySpecException 
	 * @throws SignatureException 
	 * @throws NoSuchProviderException 
	 * @throws InvalidKeyException 
	 */
	public static Certificate generateCertificate(String filename, PublicKey publicKey, PrivateKey privateKey) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeySpecException, InvalidKeyException, NoSuchProviderException, SignatureException{
		
		X509CertInfo certInfo = new X509CertInfo();
		X500Name owner = new X500Name("CN="+filename);
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
		System.out.println("SENT: "+X509CertImpl.ALG_ID);
		usedAlgorithm = (AlgorithmId) certificate.get(X509CertImpl.NAME+"."+X509CertImpl.ALG_ID);
		certInfo.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, usedAlgorithm);
		
		//reassinar algorithm
		certificate = new X509CertImpl(certInfo);
		certificate.sign(privateKey, "MD5WithRSA");
		
		return certificate;
	}
	
	/**
	 * Gerar um par de chaves publica e privada
	 * @return o par de chaves
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyPair generateKeyPair() throws NoSuchAlgorithmException{
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		keyPairGen.initialize(1028);
		return keyPairGen.generateKeyPair();
	}
	
	
	
	private static CertificateValidity getDefaultCertificateValidity(){
		Date from = new Date();
		Date to = new Date(from.getTime() + 10 * 86400000l);
		CertificateValidity validity = new CertificateValidity(from, to);
		return validity;
	}
	
}
