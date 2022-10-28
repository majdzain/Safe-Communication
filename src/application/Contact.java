package application;

import java.util.Date;
import java.util.Map;

/**
 * Contact Class to set objects as Senders, Recipients or User in itself which
 * is Abstract.
 */
public abstract class Contact {

	private String name;
	private Key publicKey;
	private String username;
	private String ip;
	private int port;
	private String smtpServer;

	protected Contact(String name, Key publicKey, String username, String ip, int port, String smtpServer) {
		this.name = name;
		this.publicKey = publicKey;
		this.username = username;
		this.ip = ip;
		this.port = port;
		this.smtpServer = smtpServer;
	}

	/** Gets the contact's public key.
	 * @return The {@link Key} which contains certificate, certificatePath and publicKey.
	*/
	public Key getPublicKey() {
		return publicKey;
	}

	/** Gets the contact's name.
	 * @return The {@link String} name.
	*/
	public String getName() {
		return name;
	}

	/** Gets the contact's user name.
	 * @return The {@link String} user name.
	*/
	public String getUsername() {
		return username;
	}

	/** Gets the contact's SMTP server.
	 * @return The {@link String} SMTP server.
	*/
	public String getSmtpAddress() {
		return smtpServer;
	}

	/** Gets the contact's IP address.
	 * @return The {@link String} IP address.
	*/
	public String getIp() {
		return ip;
	}

	/** Gets the contact's port.
	 * @return The {@link Integer} port.
	*/
	public int getPort() {
		return port;
	}

	abstract protected Map<String,String> sendMessage(User recipient, String title,String body,Date timestamp) throws Exception;

}
