package application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * FileCSVOperations Class contains the static methods which is used for the operations on CSV files with
 * ApacheCommonsCSV library.
 */
public class FileCSVOperations {
	
	private static final String[] MESSAGES_HEADERS = { "filename", "sender", "timestamp", "title" };
	private static final String[] CONTACTS_HEADERS = { "username", "name", "ip", "port", "smtp", "cert" };
	private static final String[] USERS_HEADERS = { "username", "ip", "port", "smtp"};

	/**
	 * Gets The array list of messages maps after reads it from the CSV file which in the path "C:/Safe Communication/messages/index.csv".
	 * 
	 * @return The {@link ArrayList} which contains the maps of the messages.
	 */
	public static ArrayList<Map<String, String>> readMessagesCSV() throws IOException {
		File file = new File("C:\\Safe Communication\\messages\\index.csv");
		if (file.exists()) {
			ArrayList<Map<String, String>> list = new ArrayList<>();
			Reader in = new FileReader("C:\\Safe Communication\\messages\\index.csv");
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader(MESSAGES_HEADERS).withFirstRecordAsHeader()
					.parse(in);
			for (CSVRecord record : records) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("filename", record.get("filename"));
				map.put("sender", record.get("sender"));
				map.put("timestamp", record.get("timestamp"));
				map.put("title", record.get("title"));
				list.add(map);
			}
			return list;
		} else {
			return new ArrayList<>();
		}
	}

	/**
	 * Gets The array list of contacts maps after reads it from the CSV file which in the path "C:/Safe Communication/contacts/index.csv".
	 * 
	 * @return The {@link ArrayList} which contains the maps of the contacts.
	 */
	public static ArrayList<Map<String, String>> readContactsCSV() throws IOException {
		File file = new File("C:\\Safe Communication\\contacts\\index.csv");
		if (file.exists()) {
			ArrayList<Map<String, String>> list = new ArrayList<>();
			Reader in = new FileReader("C:\\Safe Communication\\contacts\\index.csv");
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader(CONTACTS_HEADERS).withFirstRecordAsHeader()
					.parse(in);
			for (CSVRecord record : records) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("username", record.get("username"));
				map.put("name", record.get("name"));
				map.put("ip", record.get("ip"));
				map.put("port", record.get("port"));
				map.put("smtp", record.get("smtp"));
				map.put("cert", record.get("cert"));
				list.add(map);
			}
			return list;
		} else {
			return new ArrayList<>();
		}
	}
	
	/**
	 * Gets The array list of users maps after reads it from the CSV file which in the path "C:/Safe Communication/users/index.csv".
	 * 
	 * @return The {@link ArrayList} which contains the maps of the users.
	 */
	public static ArrayList<Map<String, String>> readUsersCSV() throws IOException {
		File file = new File("C:\\Safe Communication\\users\\index.csv");
		if (file.exists()) {
			ArrayList<Map<String, String>> list = new ArrayList<>();
			Reader in = new FileReader("C:\\Safe Communication\\users\\index.csv");
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader(USERS_HEADERS).withFirstRecordAsHeader()
					.parse(in);
			for (CSVRecord record : records) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("username", record.get("username"));
				map.put("ip", record.get("ip"));
				map.put("port", record.get("port"));
				map.put("smtp", record.get("smtp"));
				list.add(map);
			}
			return list;
		} else {
			return new ArrayList<>();
		}
	}

	/**
	 * Adds a new row in messages CSV file which in the path "C:/Safe Communication/messages/index.csv" if it exists or Create A new one and add a new row in it.
	 * 
	 * @param newMessage {@link Map} The new message as a map to write it in messages CSV file.
	 */
	public static void writeMessagesCSV(Map<String, String> newMessage) throws IOException {
		CSVPrinter printer = createOrGetMessagesCSV();
		printer.printRecord(newMessage.get("filename"), newMessage.get("sender"), newMessage.get("timestamp"),
				newMessage.get("title"));
		printer.close();
	}

	/**
	 * Adds a new row in contacts CSV file which in the path "C:/Safe Communication/contacts/index.csv" if it exists or Create A new one and add a new row in it.
	 * 
	 * @param newContact {@link Map} The new contact as a map to write it in contacts CSV file.
	 */
	public static void writeContactsCSV(Map<String, String> newContact) throws IOException {
		CSVPrinter printer = createOrGetContactsCSV();
		printer.printRecord(newContact.get("username"), newContact.get("name"), newContact.get("ip"),
				newContact.get("port"), newContact.get("smtp"), newContact.get("cert"));
		printer.close();
	}
	
	/**
	 * Adds a new row in users CSV file which in the path "C:/Safe Communication/users/index.csv" if it exists or Create A new one and add a new row in it.
	 * 
	 * @param newUser {@link Map} The new user as a map to write it in users CSV file.
	 */
	public static void writeUsersCSV(Map<String, String> newUser) throws IOException {
		CSVPrinter printer = createOrGetUsersCSV();
		printer.printRecord(newUser.get("username"), newUser.get("ip"),
				newUser.get("port"), newUser.get("smtp"));
		printer.close();
	}

	private static CSVPrinter createOrGetMessagesCSV() throws IOException {
		File file = new File("C:\\Safe Communication\\messages\\index.csv");
		if (!file.exists()) {
			File path = new File("C:\\Safe Communication\\messages\\");
			if (!path.exists()) {
				path.mkdir();
			}
		}
		BufferedWriter writer = Files.newBufferedWriter(Paths.get("C:\\Safe Communication\\messages\\index.csv"),
				file.exists() ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);

		CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(MESSAGES_HEADERS));
		return printer;
	}

	private static CSVPrinter createOrGetContactsCSV() throws IOException {
		File file = new File("C:\\Safe Communication\\contacts\\index.csv");
		if (!file.exists()) {
			File path = new File("C:\\Safe Communication\\contacts\\");
			if (!path.exists()) {
				path.mkdir();
			}
		}
		BufferedWriter writer = Files.newBufferedWriter(Paths.get("C:\\Safe Communication\\contacts\\index.csv"),
				file.exists() ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);

		CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(CONTACTS_HEADERS));
		return printer;
	}
	
	private static CSVPrinter createOrGetUsersCSV() throws IOException {
		File file = new File("C:\\Safe Communication\\users\\index.csv");
		if (!file.exists()) {
			File path = new File("C:\\Safe Communication\\users\\");
			if (!path.exists()) {
				path.mkdir();
			}
		}
		BufferedWriter writer = Files.newBufferedWriter(Paths.get("C:\\Safe Communication\\users\\index.csv"),
				file.exists() ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);

		CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(USERS_HEADERS));
		return printer;
	}
	
	/**
	 * Deletes an existing row in contacts CSV file.
	 * 
	 * @param username {@link String} The user name of contact which will be searched on it and delete it from contacts CSV file.
	 */
	public static void deleteOnContactsCSV(String username) throws IOException {
		ArrayList<Map<String, String>> list = readContactsCSV();
		File file = new File("C:\\Safe Communication\\contacts\\index.csv");
		file.delete();
		BufferedWriter writer = Files.newBufferedWriter(Paths.get("C:\\Safe Communication\\contacts\\index.csv"));
		CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(CONTACTS_HEADERS));
		for (int i = 0; i < list.size(); i++) {
			if (!list.get(i).get("username").matches(username)) {
				printer.printRecord(list.get(i).get("username"), list.get(i).get("name"), list.get(i).get("ip"),
						list.get(i).get("port"), list.get(i).get("smtp"), list.get(i).get("cert"));
			}
		}
		printer.flush();
	}
	
	/**
	 * Deletes an existing row in users CSV file.
	 * 
	 * @param username {@link String} The user name of user which will be searched on it and delete it from users CSV file.
	 */
	public static void deleteOnUsersCSV(String username) throws IOException {
		ArrayList<Map<String, String>> list = readContactsCSV();
		File file = new File("C:\\Safe Communication\\users\\index.csv");
		file.delete();
		BufferedWriter writer = Files.newBufferedWriter(Paths.get("C:\\Safe Communication\\users\\index.csv"));
		CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(USERS_HEADERS));
		for (int i = 0; i < list.size(); i++) {
			if (!list.get(i).get("username").matches(username)) {
				printer.printRecord(list.get(i).get("username"), list.get(i).get("ip"),
						list.get(i).get("port"), list.get(i).get("smtp"));
			}
		}
		printer.flush();
	}
}
