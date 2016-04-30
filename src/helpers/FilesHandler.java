package helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Esta classe é responsável pelo download e upload de ficheiros
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class FilesHandler {

	/**
	 * Construtor da class FilesHandler
	 */
	public FilesHandler() {}
	
	/**
	 * Abre um canal de leitura de ficheiro
	 * @param f, File a considerar
	 * @return o canal de leitura aberto
	 * @throws IOException
	 */
	public BufferedReader getReader(File f) throws IOException{
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		return br;
	}
	
	/**
	 * Abre um canal de escrita de ficheiro
	 * @param f, File a considerar
	 * @return o canal de escrita aberto
	 * @throws IOException
	 */
	public BufferedWriter getWriter(File f) throws IOException{
		FileWriter fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);
		return bw;
	}
	
	
}