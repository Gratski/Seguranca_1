package proxies;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import builders.FileStreamBuilder;
import domain.Group;
import domain.User;
import security.MACService;

/**
 * Esta classe representa a entidade responsavel
 * pela persistencia e acesso a dados de Groups
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class GroupsProxy extends Proxy {

	private static GroupsProxy instance = null;
	private Map<String, Group> groups;

	/**
	 * Constructor for GroupsProxy
	 * Cria um novo Map para usar como memória e popula-o
	 * com os grupos existentes no ficheiro
	 *
	 * @throws IOException
     */
	private GroupsProxy() throws IOException {
		this.groups = new HashMap<>();
		this.init();
	}

	/**
	 * Obter instancia de GroupsProxy para persistir grupos
	 *
	 * @return
	 * @throws IOException
     */
	public static GroupsProxy getInstance() throws IOException {
		if (instance == null)
			instance = new GroupsProxy();
		return instance;
	}

	/**
	 * Popula o map de groups
	 *
	 * @throws IOException
	 */
	public void init() throws IOException {
		BufferedReader br = FileStreamBuilder.makeReader(GROUPS_INDEX);
		
		//reading users file
		String line = null;
		while ((line = br.readLine()) != null) {
			
			//read group
			String[] lineSplit = line.split(" ");
			String owner = lineSplit[0];
			String name = lineSplit[1];
			Group group = new Group(name, new User(owner));
			
			//adiciona membros ao group
			if (lineSplit.length > 2) {
				String[] members = lineSplit[2].split(",");
				for (String member : members)
					group.addMember(member);
			}	
			//adiciona aos groups
			this.groups.put(name, group);
		}
		br.close();
	}

	/**
	 * Gets all Groups where the user with name name exists
	 *
	 * @param name
	 * 			Name of the User that should be in the groups to be returned
	 * @return
	 * 			Map of all the groups where the users exist, might be empty
     */
	public Map<String, Group> getGroupsWhereMember(String name) {
		Map<String, Group> map = new HashMap<>();
		Collection<Group> groups = this.groups.values();
		for(Group group : groups) {
			if (group.hasMemberOrOwner(name))
				map.put(group.getName(), group);
		}
		return map;
	}
	
	/**
	 * Find a group by name if exists
	 * @param groupName
	 * 		GroupName to be searched by
	 * @return
	 * 		Group if exists, null if not
	 * @throws IOException
	 */
	public Group find(String groupName) throws IOException {
		return this.groups.containsKey(groupName) ? this.groups.get(groupName) : null;
	} 
	
	/**
	 * Creates a new Group, persists it into the file, adds it to the Map
	 * and creates all the necessery file structure
	 * @param groupName
	 * 		Nome do novo group
	 * @param owner
	 * 		Owner do novo group
	 *
	 * 	@require !exists(groupName)
	 */
	public void create(String groupName, User owner) throws IOException {
		this.groups.put(groupName, new Group(groupName, owner));

		//persistencia em ficheiro
		BufferedWriter bw = FileStreamBuilder.makeWriter(GROUPS_INDEX, true);
		StringBuilder sb = new StringBuilder();
		sb.append(owner.getName() + " " + groupName);
		sb.append("\n");
		
		bw.write(sb.toString());
		bw.flush();
		bw.close();
		
		//cria estrutura de pastas necessaria
		File filesFolder = new File(CONVERSATIONS_GROUP + groupName + "/" + FILES_FOLDER);
		if (!filesFolder.exists())
			filesFolder.mkdirs();
	}
	
	/**
	 * Verifica se um user eh owner de um group
	 * @param groupName
	 * 		Nome do grupo em questao
	 * @param username
	 * 		Username a ser considerado
	 * @return
	 * 		true se eh owner, false caso contrario
	 * @require
	 * 		exists(groupName)
	 */
	public boolean isOwner(String groupName, String username) {
		return this.groups.get(groupName).getOwner().equals(username);
	}

	/**
	 * Verifica se um determinado group existe
	 * @param name
	 * 		Nome do group a ser considerado
	 * @return
	 * 		true se sim, false caso contrario
	 */
	public boolean exists(String name){
		return this.groups.containsKey(name);
	}
	
	/**
	 * Adiciona um novo membro ao group
	 * @param groupName
	 * 		Nome do group ao qual o novo membro vai ser adicionado
	 * @param member
	 * 		novo membro a ser adicionado ao group
	 * @return
	 * 		true se adicionou, false caso contrario
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @require
	 * 		exists(groupName)
	 */
	public boolean addMember(String groupName, String member, SecretKey key) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
		if ( !this.groups.get(groupName).addMember(member) )
			return false;
		
		updateFile();
		updateMAC(key);
		return true; 
	}
	
	/**
	 * Remove um User com nome member do Group com nome groupName
	 * Actualiza o Map em memória e actualiza o ficheiro
	 * Se esse User for o criador do grupo, remove o grupo de memória e apaga as
	 * pastas associadas a esse grupo
	 *
	 * @param groupName
	 * 		Nome do group a considerar
	 * @param member
	 * 		Member a ser retirado do grupo
	 * @return
	 * 		true se ok, false caso contrario
	 *
	 * @requires
	 * 		exists(groupName)
	 * @throws IOException
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public boolean removeMember(String groupName, String member, SecretKey key) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
		Group g = this.groups.get(groupName);
		//apagar grupo e suas mensagens
		if ( g.getOwner().equals(member) ) {
			//apaga pastas de group
			deleteGroup(new File(CONVERSATIONS_GROUP + g.getName()));
			//apaga grupo em si
			this.groups.remove(g.getName());
		}
		//remover membro de grupo
		else {
			if (!this.groups.get(groupName).removeMember(member) || !deleteUserKeys(groupName, member)) {
				return false;
			}
		}
		updateFile();
		updateMAC(key);
		return true;
	}

	private boolean deleteUserKeys(String groupName, String member) {

		// Remove keys de mensagens
		File f = new File(CONVERSATIONS_GROUP + groupName);
		String[] msgs = f.list();
		for (int i = 0; i < msgs.length; i++) {
			File msg = new File(CONVERSATIONS_GROUP + groupName + "/" + msgs[i]);
			String[] innerFiles = msg.list();
			
			if (contains(innerFiles, "message" + KEY_FILE_EXTENSION + "." + member)) {
				File kf = new File(CONVERSATIONS_GROUP + groupName + "/" + msgs[i] + "/" + "message" + KEY_FILE_EXTENSION + "." + member);
				kf.delete();
			}
		}

		// Remove keys de ficheiros
		f = new File(CONVERSATIONS_GROUP + groupName + "/" + FILES_FOLDER);
		String[] ficheiros = f.list();
		for (int i = 0; i < ficheiros.length; i++) {
			File ficheiro = new File(CONVERSATIONS_GROUP + groupName + "/" + FILES_FOLDER + ficheiros[i]);
			String[] innerFiles = ficheiro.list();

			if (contains(innerFiles, ficheiros[i] + KEY_FILE_EXTENSION + "." + member)) {
				File kf = new File(CONVERSATIONS_GROUP + groupName + "/" + FILES_FOLDER + ficheiros[i] + "/" + ficheiros[i] + KEY_FILE_EXTENSION + "." + member);
				kf.delete();
			}
		}
		return true;
	}
	
	
	private static boolean contains(String[] arr, String m){
		for (int i = 0; i < arr.length; i++)
			if (arr[i].equals(m))
				return true;
		return false;
	}
	
	/**
	 * Elimina a directoria e todas as subdirectorias de File f
	 * @param f O File que corresponde à directoria a ser apagada
     */
	private void deleteGroup(File f) {
		File[] fl = f.listFiles();
		for ( File file : fl ) {
			if ( file.isDirectory() && file.listFiles().length > 0 ) {
				deleteGroup(file);
			} else {
				file.delete();
			}
		}
		f.delete();
	}

	/**
	 * Actualiza o ficheiro de Groups
	 * @throws IOException
	 */
	private void updateFile() throws IOException {
		//persistencia em ficheiro
		StringBuilder sb = new StringBuilder();
		Collection<Group> list = this.groups.values();

		for (Group g : list) {
			sb.append(g.getOwner());
			sb.append(" " + g.getName());
			
			Collection<User> members = g.getMembersAndOwner();
			int i = 0;
			for ( User m : members ) {
				//se eh o primeiro dos membros
				if ( i == 0 )
					sb.append(" " + m.getName());
				else
					sb.append("," + m.getName());
				
				i++;
			}
			sb.append("\n");
		}
		//reescreve ficheiro
		BufferedWriter writer = FileStreamBuilder.makeWriter(GROUPS_INDEX, false);
		writer.write(sb.toString());
		writer.close();
	}

	/**
	 * Actualiza MAC de users file
	 * @param key, chave simetrica a ser utilizada
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
 	private void updateMAC(SecretKey key) throws InvalidKeyException, NoSuchAlgorithmException, IOException{
		MACService.updateMAC(Proxy.getGroupsIndex(), key);
	}
}