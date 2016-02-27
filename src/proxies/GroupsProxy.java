package proxies;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import common.Group;
import common.User;
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
		
		File file = new File("DATABASE/GROUPS/" + Filenames.GROUPS.toString());
		if( !file.exists() )
			file.createNewFile();
		
		FileReader fr = new FileReader(this.file);
		BufferedReader br = new BufferedReader(fr);
		
		//reading users file
		String line = null;
		while((line = br.readLine()) != null){
			
			//read group
			String[] lineSplit = line.split(" ");
			String dateStr = lineSplit[0];
			String owner = lineSplit[1];
			String name = lineSplit[2];
			Group group = new Group(name, new User(owner));
			
			//adiciona membros ao group
			for(int i = 3; i < lineSplit.length; i++)
				group.addMember(new User(lineSplit[i]));
			
			//adiciona aos groups
			this.groups.put(name, group);
			
		}
		
		//close read streams
		fw.close();
		bw.close();
		
		//open write streams
		this.file = new File("DATABASE/GROUPS/"+Filenames.GROUPS.toString());
		this.fw = new FileWriter(file, true);
		this.bw = new BufferedWriter(this.fw);
	}
	
	private boolean exists(String name){
		return this.groups.containsKey(name);
	}
	
	public boolean addMember(String groupName, User owner, User member){
		if(!this.exists(groupName))
		{
			Group group = new Group(groupName, owner);
			group.addMember(member);
				
		}else{
			if(this.groups.get(groupName).hasMember(member) 
					|| !this.groups.get(groupName).getOwner().equals(owner.getName()) )
				return false;
			else 
				this.groups.get(groupName).addMember(member);
		}
		
		//update file
		String line = null;
		/*
		try{
			BufferedReader reader = createReader();
			BufferedWriter writer = createWriter();
			
			while( (line = reader.readLine()) != null ){
				
				String[] lineSplit = line.split(" ");
				if( lineSplit[2].equals(groupName) )
				
			}
			
		}catch(Exception e){
			
		}
		*/
		
		
		
		return true;
	}
	
	@Override
	public void destroy() throws IOException {
		
	}
	
	
	
	
	//////AUX
	private BufferedReader createReader() throws FileNotFoundException{
		File file = new File("DATABASE/GROUPS/"+Filenames.GROUPS.toString());
		FileReader reader = new FileReader(file);
		return new BufferedReader(reader);
	}
	private BufferedWriter createWriter() throws IOException{
		File file = new File("DATABASE/GROUPS/"+Filenames.GROUPS.toString());
		FileWriter writer = new FileWriter(file);
		return new BufferedWriter(writer);
	}
	
}
