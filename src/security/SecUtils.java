package security;

import java.util.Random;

public class SecUtils {

    private static final String HEXES = "0123456789ABCDEF";
	
    /**
     * Gera representacao hexadecimal de array de bytes
     * @param raw, array de bytes a considerar
     * @return string com a representacao hexadecimal de raw
     */
	public static String getHex( byte [] raw ) {
		if ( raw == null ) {
	      return null;
	    }
	    final StringBuilder hex = new StringBuilder( 2 * raw.length );
	    for ( final byte b : raw ) {
	      hex.append(HEXES.charAt((b & 0xF0) >> 4))
	         .append(HEXES.charAt((b & 0x0F)));
	    }
	    return hex.toString();
	}
	  
	/**
	 * Converte uma string hexadecimal em array de bytes
	 * @param s, string a considerar
	 * @return array de bytes
	 */
	public static byte[] hexStringToByteArray(String s) {
	    byte[] b = new byte[s.length() / 2];
	    for (int i = 0; i < b.length; i++) {
	      int index = i * 2;
	      int v = Integer.parseInt(s.substring(index, index + 2), 16);
	      b[i] = (byte) v;
	    }
		return b;
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
	
}
