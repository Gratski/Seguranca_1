package client;

public class Client {

	public static void main(String[]args){
		
		String str = "FROM -u TO -m |\"This is a single\"";
		String[]base = str.split(" ");
		String[]content = str.split("|");
		for(String s : base)
			System.out.println(s);
		
		System.out.println("---------");
		
		for(String s : content)
			System.out.println(s);
		
	}
	
}
