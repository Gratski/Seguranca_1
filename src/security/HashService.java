package security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashService {

	public static byte[] createFileHash(File file) throws IOException, NoSuchAlgorithmException{
		FileInputStream fs = new FileInputStream(file); 
		MessageDigest md = GenericSignature.getMessageDigest();
		
		int read = 0;
		byte[] buf = new byte[16];
		
		while((read = fs.read(buf))>-1){
			md.update(buf, 0, read);
		}
		
		fs.close();
		return md.digest();
	}
	
	public static byte[] createStringHash(String content) throws NoSuchAlgorithmException{
		MessageDigest md = GenericSignature.getMessageDigest();
		byte[] bytes = content.getBytes();
		md.update(bytes);
		return md.digest();
	}
	
}
