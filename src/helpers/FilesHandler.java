package helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FilesHandler {

	private String filename;
	private Connection connection;
	
	private static final int CHUNK = 1024;
	
	public FilesHandler(){}
	
	public boolean sendReceive(Connection conn, String filename) {
		this.connection = conn;
		this.filename = filename;
		return true;
	}
	
	public boolean receiveSend(Connection conn, String filename) {
		this.connection = conn;
		this.filename = filename;
		return true;
	}
	
	
	public boolean send(Connection conn, File file) throws IOException {
		byte[] byteArr;
		
		FileInputStream fs = new FileInputStream(file);
		
		//send filesize
		conn.getOutputStream().writeLong(file.length());
		conn.getOutputStream().flush();
		
		System.out.println("Receiving file with size : " + file.length());
		
		//send file itself
		long fileSize = file.length();
		long totalSent = 0;
		
		while (totalSent < fileSize) {
			int byteNum = 0;
			if ( (totalSent + CHUNK) <= fileSize )
				byteNum = CHUNK;
			else
				byteNum = (int) ( fileSize - totalSent );
				
			System.out.println("ByteNum: " + byteNum);
			byteArr = new byte[byteNum];
			int toSend = fs.read(byteArr,(int) 0, byteNum);

			System.out.println("Sent now: " + toSend);
			conn.getOutputStream().write(byteArr, 0, toSend);
			conn.getOutputStream().flush();
			totalSent += toSend;
		}
		fs.close();
		System.out.println("Total sent bytes: " + totalSent);
		return totalSent == fileSize;
	}
	
	public File receive(Connection conn, String dir, String filename) throws Exception {
		byte[] byteArr = new byte[CHUNK];

		//receiving filename
		File file = new File(dir + "/" + filename);
		file.getParentFile().mkdirs();
		FileOutputStream out = new FileOutputStream(dir + "/" + filename);
		
		//receiving filesize
		System.out.println("Waiting for filesize");
		long fileSize = conn.getInputStream().readLong();
		long totalRead = 0;
		
		System.out.println("Receiving file with size : " + fileSize);
		//receiving file itself
		while (totalRead < fileSize) {
			int cur = conn.getInputStream().read(byteArr);
			if (cur == -1)
				continue;
			
			out.write(byteArr, 0, cur);
			System.out.println("writing on file...");
			totalRead += cur;
		}
		
		System.out.println("Total Received: " + totalRead);
		out.close();
		return totalRead == fileSize ? file : null;
	}
	
	public boolean existsFile(String path){
		File file = new File(path);
		return file.exists();
		
	}
	
}