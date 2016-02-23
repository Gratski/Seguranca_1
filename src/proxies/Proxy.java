package proxies;

import java.io.IOException;

public interface Proxy {
	
	/**
	 * Fecha todos os streams
	 * @throws IOException
	 */
	public void destroy() throws IOException;
	
}
