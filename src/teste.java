import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class teste {

	public static void main(String[]args) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException{
		
		KeyStore ks = KeyStore.getInstance("JKS");
		FileInputStream fis = new FileInputStream("keys.ks");
		ks.load(fis, "ahahah".toCharArray());
		Certificate cer = ks.getCertificate("server");
		PublicKey pk = cer.getPublicKey();
		
	}
	
}
