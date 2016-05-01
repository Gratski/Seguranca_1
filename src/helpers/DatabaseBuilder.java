package helpers;

import proxies.Proxy;

import java.io.File;
import java.io.IOException;

/**
 * Esta classe é responsável Pela
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class DatabaseBuilder {

	/**
	 * Este metodo eh o responsavel pela
	 * criacao da estrutura de ficheiros necessária
	 * para o programa correr correctamente
	 *
	 * @return
	 * 		true se todos estao criados, false caso contrario
	 */
	public boolean make(){
		boolean valid = true;
		try {
			valid = makeDir(Proxy.getDATABASE())
					&& makeDir(Proxy.getCONVERSATIONS())
					&& makeDir(Proxy.getConversationsGroup())
					&& makeDir(Proxy.getConversationsPrivate())
					&& makeFile(Proxy.getDATABASE(), Proxy.getUsersIndex())
					&& makeFile(Proxy.getDATABASE(), Proxy.getGroupsIndex())
					&& makeFile(Proxy.getCONVERSATIONS(), Proxy.getConversationsPrivateIndex());
		} catch(Exception e) {
			valid = false;
		}
		return valid;
	}

	/**
	 * Método que cria a directoria que está em path
	 *
	 * @param path
	 * 		Directoria a ser criada
	 * @return
	 * 		True se a directoria for criada ou se já existir, false se houver algum erro
	 * @requires
	 * 		path != null
	 * @throws IOException
     */
	private boolean makeDir(String path) throws IOException{
		File file = new File(path);
		if (!file.exists())
			file.mkdirs();
		return file.exists();
	}

	/**
	 * Método que cria um novo ficheiro, no path, com o nome que está em filename
	 * (filename = path + nome do ficheiro)
	 *
	 * @param path
	 * 		Directoria onde o ficheiro vai ser criada
	 * @param filename
	 * 		Directoria + nome do ficheiro a ser criado
	 * @return
	 * 		True se criar o ficheiro ou se já existir, false caso o contrário
	 * @requires
	 * 		path != null && filename != null
	 * @throws IOException
     */
	public boolean makeFile(String path, String filename) throws IOException{
		//directory
		this.makeDir(path);
		
		//file itself
		File file = new File(filename);
		if (!file.exists())
			file.createNewFile();
		return file.exists();
	}
}