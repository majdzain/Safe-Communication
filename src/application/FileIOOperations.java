package application;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;

/**
 * FileIOOperations Class contains the static methods which is used for the operations on files with
 * ApacheCommonsIO library.
 */
public class FileIOOperations {
	
	/**
	 * Gets The text body of the message after reads it from the text file which in the path "C:/Safe Communication/messages/".
	 * 
	 * @param name {@link String} The name of message file.
	 * @return The {@link String} result of reading the file.
	 */
	public static String readMessageFile(String name) throws IOException {
		File file = new File("C:\\Safe Communication\\messages\\" + name + ".txt");
		String data = FileUtils.readFileToString(file, Charset.defaultCharset());
		return data;
	}

	/**
	 * Creates a text file in the path "C:/Safe Communication/messages/" for the body of the message.
	 * 
	 * @param data {@link String} The body of message.
	 * @param name {@link String} The name of message file.
	 */
	public static void writeMessageFile(String data, String name) throws IOException {
		File file = new File("C:\\Safe Communication\\messages\\" + name + ".txt");
		FileUtils.write(file, data);
	}

}
