package application;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Map;

/**
 * SMTPSession Class to set objects for communicate with SMTP Servers and send
 * messages to them.
 */
public class SMTPSession {

	private static final int SOCKET_READ_TIMEOUT = 15 * 1000;
	private String host;
	private int port;
	private String recipient;
	private String sender;
	private String title;
	private String body;
	private Socket smtpSocket;
	private BufferedReader in;
	private OutputStreamWriter out;

	/**
	 * Creates new SMTP session. Assumes SMTP
	 * port is 25 (default for SMTP service).
	 * @param host {@link String} The SMTP host.
	 * @param recipient {@link String} The recipient's email address.
	 * @param sender {@link String} The sender's email address.
	 * @param title {@link String} The email title.
	 * @param body {@link String} The email body text.
	 */
	public SMTPSession(String host, String recipient, String sender, String title, String body) {
		this.host = host;
		this.port = 25;
		this.recipient = recipient;
		this.body = body;
		this.sender = sender;
		this.title = title;
	}

	/**
	 * Closes down the connection to SMTP server (if open). Should be called if an
	 * exception is raised during the SMTP session.
	 */
	public void close() {
		try {
			in.close();
			out.close();
			smtpSocket.close();
		} catch (Exception ex) {
			// Ignore the exception. Probably the socket is not open.
		}
	}

	private void connect() throws IOException {
		smtpSocket = new Socket(host, port);
		smtpSocket.setSoTimeout(SOCKET_READ_TIMEOUT);
		in = new BufferedReader(new InputStreamReader(smtpSocket.getInputStream()));
		out = new OutputStreamWriter(smtpSocket.getOutputStream());
	}

	private String sendCommand(String commandString) throws IOException {
		out.write(commandString + "\n");
		out.flush();
		String response = getResponse();
		return response;
	}

	private void doCommand(String commandString, char expectedResponseStart) throws IOException {
		String response = sendCommand(commandString);
		checkServerResponse(response, expectedResponseStart);
	}

	private void checkServerResponse(String response, char expectedResponseStart) throws IOException {
		if (response.charAt(0) != expectedResponseStart)
			throw new IOException(response);
	}

	private String getResponse() throws IOException {
		String response = "";
		String line;
		do {
			line = in.readLine();
			if ((line == null) || (line.length() < 3)) {
				// SMTP response lines should at the very least have a 3-digit number
				throw new IOException("Bad response from server.");
			}
			response += line + "\n";
		} while ((line.length() > 3) && (line.charAt(3) == '-'));
		return response;
	}

	private String getMessageHeaders() {
		// Most header are less than 1024 characters long
		String headers = "";
		headers = headers + "Date: " + new Date().toString() + "\n";
		headers = headers + "Sender: " + sender + "\n";
		headers = headers + "From: " + sender + "\n";
		headers = headers + "To: " + recipient + "\n";
		headers = headers + "Subject: " + title + "\n";
		return headers + "\n\n";
	}

	/**
	 * Sends a message using the SMTP protocol.
	 */
	public void sendMessage() throws IOException {
		connect();
		// After connecting, the SMTP server will send a response string.
		// Make sure it starts with a '2' (reponses in the 200's are positive).
		String response = getResponse();
		checkServerResponse(response, '2');
		// Introduce ourselves to the SMTP server with a polite "HELO localhostname"
		doCommand("HELO " + smtpSocket.getLocalAddress().toString(), '2');
		// Tell the server who this message is from
		doCommand("MAIL FROM: <" + sender + ">", '2');
		// Now tell the server who we want to send a message to
		doCommand("RCPT TO: <" + recipient + ">", '2');
		// Okay, now send the mail message. We expect a response beginning
		// with '3' indicating that the server is ready for data.
		doCommand("DATA", '3');
		// Send the message headers
		out.write(getMessageHeaders());

		DataOutputStream dOut = new DataOutputStream(smtpSocket.getOutputStream());
		dOut.write(body.getBytes());
//		BufferedReader msgBodyReader = new BufferedReader(new StringReader(body));
		// Send each line of the message
//		String line;
//		while ((line = msgBodyReader.readLine()) != null) {
//			// If the line begins with a ".", put an extra "." in front of it.
//			if (line.startsWith("."))
//				out.write('.');
//			out.write(line + "\n");
//		}
		// A "." on a line by itself ends a message.
		doCommand(".", '2');
		// Message is sent. Close the connection to the server
		close();
	}
}
