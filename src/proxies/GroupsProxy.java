package proxies;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import common.Group;
import enums.Filenames;

public class GroupsProxy implements Proxy{

	private static GroupsProxy instance = null;
	private File file;
	private FileWriter fw;
	private BufferedWriter bw;
	private Map<String, Group> groups;
	
	private GroupsProxy() throws IOException{
		this.groups = new HashMap<>();
		this.init();
	}
	
	public static GroupsProxy getInstance() throws IOException{
		if(instance == null)
			instance = new GroupsProxy();
		return instance;
	}

	public void init() throws IOException{
		
		File file = new File(Filenames.GROUPS.toString());
		if( !file.exists() )
			file.createNewFile();
		
		FileReader fr = new FileReader(this.file);
		BufferedReader br = new BufferedReader(fr);
		
		//reading users file
		String line = null;
		while((line = br.readLine()) != null){
			
			//read
			//cria group
			//add members
			
		}
		
		//close read streams
		fw.close();
		bw.close();
		
	}
	
	@Override
	public void destroy() throws IOException {
		
	}
	
}
