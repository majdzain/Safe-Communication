package application;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/** User Class to set users objects as Senders or Receipts which is inherited from {@link Contact} Class.
*/
public final class User extends Contact {
	
	private static User instance;
	private Key privateKey;
	private String password;

	private User(Key privateKey, String password, String name, Key publicKey, String username, String ip, int port,
			String smtpAddress) {
		super(name, publicKey, username, ip, port, smtpAddress);
		this.privateKey = privateKey;
		this.password = password;
	}

	/** Creates a new Receipt User object.
	 * @param name {@link String} The name of Recipient.
	 * @param publicKey which is {@link Key} object contains certificate, certificatePath and publicKey.
	 * @param username {@link String} The user name of Receipt.
	 * @param ip {@link String} The IP Address of Receipt.
	 * @param port {@link Integer} The Port of Receipt.
	 * @param smtpServer {@link String} The SMTP Server of Receipt.
	*/
	public User(String name, Key publicKey, String username, String ip, int port, String smtpAddress) {
		super(name, publicKey, username, ip, port, smtpAddress);
	}

	/** Creates a new Singleton User object just for whole program's threads.
	 * @param privateKey which is {@link Key} object contains privateKey, password, certificate, certificatePath and publicKey.
	 * @param password {@link String} The password of User account.
	 * @param name {@link String} The name of User.
	 * @param publicKey which is {@link Key} object contains certificate, certificatePath and publicKey.
	 * @param username {@link String} The user name of User.
	 * @param ip {@link String} The IP Address of User.
	 * @param port {@link Integer} The Port of User.
	 * @param smtpServer {@link String} The SMTP Server of User.
	*/
	public static User getInstance(Key privateKey, String password, String name, Key publicKey, String username,
			String ip, int port, String smtpAddress) {
		if (instance == null) {
			instance = new User(privateKey, password, name, publicKey, username, ip, port, smtpAddress);
		}
		return instance;
	}

	/** Gets the user's private key.
	 * @return The {@link Key} which contains privateKey, password, certificate, certificatePath and publicKey.
	*/
	public Key getPrivateKey() {
		return privateKey;
	}

	/** Gets the user's password.
	 * @return The {@link String} password.
	*/
	public String getPassword() {
		return password;
	}

	/**
	 * Gets the Message object after decryption and saving in files.
	 * 
	 * @param sender {@link User} object which contains the public key for
	 *               decryption.
	 * @param data   {@link Map} which contains title, timestamp and body of
	 *               message.
	 * @return The {@link Message} object after decryption.
	 */
	public Message receiveMessage(User sender, Map<String, String> data) throws Exception {
		System.out.println(data.get("body"));
		Message m = new Message(data.get("body"), data.get("title"),
				(new SimpleDateFormat("dd/MM/yyyy HH:mm")).parse(data.get("timestamp")), sender);

		Message decryptedMessage = m.unsecureMessage(privateKey, sender.getPublicKey());
		Map<String, String> map = new HashMap<>();
		map.put("filename", (new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss")).format(new Date()));
		map.put("title", m.getTitle());
		map.put("sender", sender.getUsername());
		map.put("timestamp", (new SimpleDateFormat("dd/MM/yyyy HH:mm")).format(m.getTimestamp()));
		try {
			FileIOOperations.writeMessageFile(data.get("body"),
					(new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss")).format(new Date()));
			FileCSVOperations.writeMessagesCSV(map);
		} catch (IOException e) {

		}
		return decryptedMessage;
	}

	/** Implementaion Method From {@link Contact} class for getting the message map and for encrypting the message.
	 * @param recipient {@link User} object which contains the public key for encryption.
	 * @param title {@link String} which represents the title of message. 
	 * @param body {@link String} which represents the body of message before encryption. 
	 * @param timestamp {@link Date} which represents the time and date of message. 
	 * @return The {@link Map} object after encryption.
	*/
	@Override
	public Map<String, String> sendMessage(User recipient, String title, String body, Date timestamp) throws Exception {
		System.out.println("Encrypting Message");
		Message message = new Message(body, title, timestamp, this, recipient);
		Message encryptedMessage = null;
		encryptedMessage = message.secureMessage(privateKey, recipient.getPublicKey());
		System.out.println("Message Is Encrypted");
		Map<String, String> messageMap = new HashMap<>();
		messageMap.put("sender", getUsername());
		messageMap.put("recipient", recipient.getUsername());
		messageMap.put("title", title);
		messageMap.put("timestamp", (new SimpleDateFormat("dd/MM/yyyy HH:mm")).format(timestamp));
		// System.out.println(encryptedMessage.getBody());
		messageMap.put("body", encryptedMessage.getBody());

		return messageMap;
	}
}
