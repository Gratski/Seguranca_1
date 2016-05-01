package security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class that represents a service for Hashes that all the aplication can use
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class HashService {

	/**
	 * Creates a hash with the contents of a file
	 *
	 * @param file
	 * 		File whoose content will be used to create a hash
	 * @return
	 * 		Byte array that represents the hash
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
     */
	public static byte[] createFileHash(File file) throws IOException, NoSuchAlgorithmException{
		FileInputStream fs = new FileInputStream(file); 
		MessageDigest md = GenericSignature.getMessageDigest();
		int read = 0;
		byte[] buf = new byte[16];
		
		while ((read = fs.read(buf)) > -1)
			md.update(buf, 0, read);

		fs.close();
		return md.digest();
	}
}