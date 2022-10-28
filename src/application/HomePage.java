package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * HomePage Class to show the two four screens: Home (contains contacts and
 * received messages and some buttons), New Message Screen, New Contact Screen,
 * Edit Profile Screen.
 */
public class HomePage {
	private Stage stage;
	private User user;
	private Text nameText, usernameText, certPathText, ipPortText, smtpText;
	private Button addContactButton, editProfileButton, createMessageButton;
	private ListView<String> contactsListView, messagesListView, contactsDetailsListView, messagesDetailsListView;
	private TextField titleTextField, bodyTextField;
	private Scene homeScene, createMessageScene, createContactScene, editProfileScene;
	private Button sendMessageButton, backMessageButton;
	private int selectedContact = -1;
	private TextField nameTextField, ipTextField, portTextField, smtpTextField;
	private Button createContactButton, pickCertificateButton, backContactButton;
	private FileChooser fileChooser;
	private ListView<String> contactsMessageListView;
	private int selectedContactDetails = -1;
	private VBox createMessageVBox, createContactVBox;
	private TextField rEmailTextField, rNameTextField, rIpAddressTextField, rPortTextField, rSMTPServerTextField,
			rPasswordSTextField;
	private PasswordField rPasswordTextField;
	private Button rEditButton, rShowPasswordButton, backEditButton;
	private Text rEditErrorText, rCertificateText;
	private VBox editVBox;
	private ObservableList<String> messages;
	private ObservableList<Message> messagesObjects;
	public ExecutorService receivingMessagesFromSocketPool;
	public ScheduledExecutorService receivingMessagesFromPOP3Pool;
	public ServerSocket serverSocket;

	/** Creates a new HomePage object to show the page by {@link LoginSignUpPage} Class when login or register.
	*/
	public HomePage(Stage stage, User user) {
		this.stage = stage;
		this.user = user;
		stage.setTitle("Safe Communication - Home");
		fileChooser = new FileChooser();
		// initialize data
		messages = FXCollections.observableArrayList();
		messagesObjects = FXCollections.observableArrayList();
		ObservableList<User> contactsObjects = FXCollections.observableArrayList();
		ObservableList<String> contacts = FXCollections.observableArrayList();
		ObservableList<String> contactDetails = FXCollections.observableArrayList();
		ObservableList<String> messageDetails = FXCollections.observableArrayList();
		ExecutorService messagesPool = Executors.newFixedThreadPool(5);
		ArrayList<Map<String, String>> contactsList = new ArrayList<>();
		ArrayList<Map<String, String>> messagesList = new ArrayList<>();
		try {
			contactsList = FileCSVOperations.readContactsCSV();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			messagesList = FileCSVOperations.readMessagesCSV();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		for (Map<String, String> map : contactsList) {
			Key k = null;
			try {
				k = new Key(map.get("cert"));
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			if (k != null) {
				User contact = new User(map.get("name"), k, map.get("username"), map.get("ip"),
						Integer.parseInt(map.get("port")), map.get("smtp"));
				contactsObjects.add(contact);
				contacts.add(map.get("username") + " - " + map.get("name"));
			}
		}
		for (Map<String, String> map : messagesList) {
			System.out.println("dsadsa");
			messagesPool.execute(() -> {
				System.out.println("rtatere");
				boolean isSenderExist = false;
				User sender = null;
				for (User contact : contactsObjects) {
					if (contact.getUsername().matches(map.get("sender"))) {
						isSenderExist = true;
						sender = contact;
						break;
					}
				}
				if (isSenderExist) {
					System.out.println(map.get("timestamp"));
					try {
						String body = FileIOOperations.readMessageFile(map.get("filename"));
						Message m = new Message(body, map.get("title"),
								(new SimpleDateFormat("dd/MM/yyyy HH:mm")).parse(map.get("timestamp")), sender);
						messagesObjects.add(m.unsecureMessage(user.getPrivateKey(), sender.getPublicKey()));
						messages.add(map.get("title") + " - " + map.get("timestamp"));
						System.out.println("trweqrewgrqw");
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
			});

		}
		messagesPool.shutdown();

		// initialize delete profile alert dialog
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Deleteing Profile");
		alert.setResizable(false);
		alert.setContentText("Are you sure for deleting your profile?");

		// initialize home items
		nameText = new Text();
		usernameText = new Text();
		certPathText = new Text();
		ipPortText = new Text();
		smtpText = new Text();
		Text contactsText = new Text();
		Text messagesText = new Text();
		Text contactDetailsText = new Text("Contact Details : ");
		Text messageDetailsText = new Text("Message Details : ");
		addContactButton = new Button();
		editProfileButton = new Button();
		createMessageButton = new Button();
		Button deleteContactButton = new Button("Delete Selected Contact");
		contactsListView = new ListView<String>(contacts);
		contactsMessageListView = new ListView<String>(contacts);
		messagesListView = new ListView<String>(messages);
		messagesDetailsListView = new ListView<String>(messageDetails);
		contactsDetailsListView = new ListView<String>(contactDetails);
		VBox homeVBox = new VBox(5);
		VBox homeContactsVBox = new VBox(5);
		VBox homeContactsDetailsVBox = new VBox(5);
		HBox homeContactsHBox = new HBox(10);
		VBox homeMessagesVBox = new VBox(5);
		VBox homeMessagesDetailsVBox = new VBox(5);
		HBox homeMessagesHBox = new HBox(10);

		// setup home items
		nameText.setText("Name : " + user.getName());
		usernameText.setText("Username : " + user.getUsername());
		certPathText.setText("Certificate Path : " + user.getPublicKey().getCertificatePath());
		ipPortText.setText("IP Address : " + user.getIp() + " || Port : " + String.valueOf(user.getPort()));
		smtpText.setText("SMTP Server : " + user.getSmtpAddress());
		contactsText.setText("Contacts : ");
		messagesText.setText("Recieved Emails : ");
		addContactButton.setText("Create A New Contact");
		editProfileButton.setText("Edit Profile");
		createMessageButton.setText("Create A New Message");
		deleteContactButton.setVisible(false);
		homeVBox.setAlignment(Pos.CENTER);
		homeContactsVBox.setAlignment(Pos.CENTER);
		homeContactsDetailsVBox.setAlignment(Pos.CENTER);
		homeContactsHBox.setAlignment(Pos.CENTER);
		homeMessagesVBox.setAlignment(Pos.CENTER);
		homeMessagesDetailsVBox.setAlignment(Pos.CENTER);
		homeMessagesHBox.setAlignment(Pos.CENTER);
		contactsListView.setMaxSize(290, 90);
		contactsDetailsListView.setMaxSize(290, 90);
		messagesDetailsListView.setMaxSize(290, 90);
		contactsMessageListView.setMaxSize(290, 90);
		messagesListView.setMaxSize(290, 90);
		deleteContactButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (selectedContactDetails != -1) {
					try {
						FileCSVOperations
								.deleteOnContactsCSV(contactsObjects.get(selectedContactDetails).getUsername());
					} catch (IOException e) {

						e.printStackTrace();
					}
					contacts.remove(selectedContactDetails);
					contactsObjects.remove(selectedContactDetails);
					contactDetails.removeAll(contactDetails);
					deleteContactButton.setVisible(false);
				}
			}
		});
		editProfileButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				stage.setTitle("Safe Communication - Edit Profile");
				stage.setScene(editProfileScene);
				editVBox.requestFocus();
			}
		});
		createMessageButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				stage.setTitle("Safe Communication - Create A Message");
				stage.setScene(createMessageScene);
				createMessageVBox.requestFocus();
			}
		});
		addContactButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				stage.setTitle("Safe Communication - Add A Contact");
				stage.setScene(createContactScene);
				createContactVBox.requestFocus();
			}
		});
		contactsListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// Your action here
				System.out.println("Selected item: " + newValue);
				int selectedContact = contacts.indexOf(newValue);
				Contact contact = contactsObjects.get(selectedContact);
				contactDetails.removeAll(contactDetails);
				contactDetails.add("Name : " + contact.getName());
				contactDetails.add("Username : " + contact.getUsername());
				contactDetails.add("IP Address : " + contact.getIp());
				contactDetails.add("Port : " + String.valueOf(contact.getPort()));
				contactDetails.add("SMTP Server : " + contact.getSmtpAddress());
				contactDetails.add("Certificate Path : " + contact.getPublicKey().getCertificatePath());
				deleteContactButton.setVisible(true);
				selectedContactDetails = selectedContact;
			}
		});
		messagesListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// Your action here
				System.out.println("Selected item: " + newValue);
				int selectedMessage = messages.indexOf(newValue);
				Message message = messagesObjects.get(selectedMessage);
				messageDetails.removeAll(messageDetails);
				messageDetails.add("Title : " + message.getTitle());
				messageDetails.add("Body : " + message.getBody());
				messageDetails.add("Contact Sender : " + message.getSender().getUsername() + " - "
						+ message.getSender().getName());
				messageDetails.add(
						"Date And Time : " + (new SimpleDateFormat("yyyy-mm-dd HH:mm")).format(message.getTimestamp()));
			}
		});
		homeContactsVBox.getChildren().addAll(contactsText, contactsListView);
		homeContactsDetailsVBox.getChildren().addAll(contactDetailsText, contactsDetailsListView);
		homeContactsHBox.getChildren().addAll(homeContactsVBox, homeContactsDetailsVBox);
		homeMessagesVBox.getChildren().addAll(messagesText, messagesListView);
		homeMessagesDetailsVBox.getChildren().addAll(messageDetailsText, messagesDetailsListView);
		homeMessagesHBox.getChildren().addAll(homeMessagesVBox, homeMessagesDetailsVBox);
		homeVBox.getChildren().addAll(nameText, usernameText, certPathText, ipPortText, smtpText, homeContactsHBox,
				deleteContactButton, addContactButton, homeMessagesHBox, createMessageButton, editProfileButton);
		homeScene = new Scene(homeVBox, 600, 500);

		// initialize (edit profile) items
		Text editProfileText = new Text();
		rEditErrorText = new Text();
		rCertificateText = new Text();
		rEmailTextField = new TextField();
		rNameTextField = new TextField();
		rIpAddressTextField = new TextField();
		rPortTextField = new TextField();
		rSMTPServerTextField = new TextField();
		rPasswordTextField = new PasswordField();
		rPasswordSTextField = new TextField();
		backEditButton = new Button();
		rEditButton = new Button();
		rShowPasswordButton = new Button("Show");
		editVBox = new VBox(5);
		HBox editHBox = new HBox(5);
		HBox editHBoxIPPort = new HBox(5);
		HBox editHBoxPassword = new HBox(5);
		StackPane editPasswordStackPane = new StackPane();

		// setup (edit profile) items
		editProfileText.setText("Edit Your Profile...");
		editProfileText.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 14));
		rEditErrorText.setFill(Color.RED);
		rEditErrorText.setVisible(false);
		editVBox.setAlignment(Pos.CENTER);
		editHBox.setAlignment(Pos.CENTER);
		editHBoxIPPort.setAlignment(Pos.CENTER);
		editHBoxPassword.setAlignment(Pos.CENTER);
		rEditButton.setText("Edit Profile");
		backEditButton.setText("Go Back");
		rEmailTextField.setPromptText("Email Or Username");
		rPasswordTextField.setPromptText("Password");
		rNameTextField.setPromptText("Full Name");
		rIpAddressTextField.setPromptText("IP Address");
		rPortTextField.setPromptText("Port");
		rSMTPServerTextField.setPromptText("SMTP Server");
		rCertificateText.setText(user.getPublicKey().getCertificatePath());
		rEmailTextField.setText(user.getUsername());
		rPasswordTextField.setText(user.getPassword());
		rPasswordSTextField.setText(user.getPassword());
		rNameTextField.setText(user.getName());
		rIpAddressTextField.setText(user.getIp());
		rPortTextField.setText(String.valueOf(user.getPort()));
		rSMTPServerTextField.setText(user.getSmtpAddress());
		rEmailTextField.setMaxWidth(250);
		rPasswordTextField.setMaxWidth(250);
		rPasswordSTextField.setMaxWidth(250);
		editPasswordStackPane.setMaxWidth(220);
		rNameTextField.setMaxWidth(250);
		rIpAddressTextField.setMaxWidth(220);
		rPortTextField.setMaxWidth(97);
		rSMTPServerTextField.setMaxWidth(250);
		rShowPasswordButton.setMaxWidth(97);

		rPasswordTextField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				rPasswordSTextField.setText(newValue);
			}
		});
		rPasswordSTextField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				rPasswordTextField.setText(newValue);
			}
		});
		rEditButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				boolean isError = false;
				if (rSMTPServerTextField.getText().isEmpty()) {
					isError = true;
					rEditErrorText.setText("Please Enter Your SMTP Server");
					rEditErrorText.setVisible(true);
				}
				if (rPortTextField.getText().isEmpty()) {
					isError = true;
					rEditErrorText.setText("Please Enter Your Port");
					rEditErrorText.setVisible(true);
				}
				if (rIpAddressTextField.getText().isEmpty()) {
					isError = true;
					rEditErrorText.setText("Please Enter Your IP Address");
					rEditErrorText.setVisible(true);
				}
				if (rPasswordTextField.getText().isEmpty()) {
					isError = true;
					rEditErrorText.setText("Please Enter Your Password");
					rEditErrorText.setVisible(true);
				}
				if (rEmailTextField.getText().isEmpty()) {
					isError = true;
					rEditErrorText.setText("Please Enter Your Email");
					rEditErrorText.setVisible(true);
				}
				if (rNameTextField.getText().isEmpty()) {
					isError = true;
					rEditErrorText.setText("Please Enter Your Name");
					rEditErrorText.setVisible(true);
				}

				if (!isError) {
					if (rPasswordTextField.getText().length() < 8) {
						isError = true;
						rEditErrorText.setText("Please Enter Password With 8 Charachters Or More");
						rEditErrorText.setVisible(true);
					}
					if (!isError) {
						rEditErrorText.setVisible(false);

						Key key = null;
						try {
							Key.deleteUserKey(user.getUsername());
						} catch (Exception e1) {

							e1.printStackTrace();
						}
						try {
							key = Key.generateNewUserKey(rNameTextField.getText(), rEmailTextField.getText(),
									rPasswordTextField.getText());
						} catch (Exception e) {
							rEditErrorText.setText(e.getMessage());
							rEditErrorText.setVisible(true);
						}

						if (key != null) {
							boolean isEr = false;
							try {
								FileCSVOperations.deleteOnUsersCSV(user.getUsername());
								Map<String, String> map = new HashMap<>();
								map.put("username", rEmailTextField.getText());
								map.put("ip", rIpAddressTextField.getText());
								map.put("port", rPortTextField.getText());
								map.put("smtp", rSMTPServerTextField.getText());
								FileCSVOperations.writeUsersCSV(map);
							} catch (Exception e) {
								isEr = true;
								rEditErrorText.setText(e.getMessage());
								rEditErrorText.setVisible(true);
							}
							if (!isEr) {
								System.out.println("Success");
								User user = User.getInstance(key, rPasswordTextField.getText(),
										rNameTextField.getText(),
										new Key(key.getCertificate(), key.getCertificatePath()),
										rEmailTextField.getText(), rIpAddressTextField.getText(),
										Integer.parseInt(rPortTextField.getText()), rSMTPServerTextField.getText());
								new HomePage(stage, user);
							}
						}
					}
				}
			}
		});
		backEditButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				stage.setScene(homeScene);
				homeVBox.requestFocus();
				stage.setTitle("Safe Communication - Home");
			}
		});
		rShowPasswordButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (rPasswordTextField.isVisible()) {
					rPasswordTextField.setVisible(false);
					rPasswordSTextField.setVisible(true);
					rShowPasswordButton.setText("Hide");
				} else {
					rPasswordTextField.setVisible(true);
					rPasswordSTextField.setVisible(false);
					rShowPasswordButton.setText("Show");
				}
			}
		});
		editHBoxIPPort.getChildren().addAll(rIpAddressTextField, rPortTextField);
		editPasswordStackPane.getChildren().addAll(rPasswordSTextField, rPasswordTextField);
		editHBoxPassword.getChildren().addAll(editPasswordStackPane, rShowPasswordButton);
		editHBox.getChildren().addAll(backEditButton, rEditButton);

		editVBox.getChildren().addAll(editProfileText, rNameTextField, rEmailTextField, editHBoxPassword,
				editHBoxIPPort, rSMTPServerTextField, rCertificateText, rEditErrorText, editHBox);
		editProfileScene = new Scene(editVBox, 600, 500);

		// initialize (create message) items
		createMessageVBox = new VBox(5);
		HBox createMessageHBox = new HBox(5);
		sendMessageButton = new Button();
		titleTextField = new TextField();
		bodyTextField = new TextField();
		Text createMessageText = new Text("Create A New Message...");
		Text selectContactText = new Text("Select A Contact : ");
		Text createMessageResultText = new Text();
		backMessageButton = new Button("Go Back");

		// setup (create message) items
		createMessageVBox.setAlignment(Pos.CENTER);
		createMessageHBox.setAlignment(Pos.CENTER);
		createMessageResultText.setVisible(false);
		createMessageText.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 14));
		sendMessageButton.setText("Send Message");
		titleTextField.setMaxWidth(250);
		bodyTextField.setMaxWidth(250);
		bodyTextField.setMinHeight(60);
		titleTextField.setPromptText("Title");
		bodyTextField.setPromptText("Body");
		sendMessageButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				createMessageResultText.setFill(Color.BLACK);
				createMessageResultText.setText("Sending Message...");
				createMessageResultText.setVisible(true);
				boolean isError = false;
				if (selectedContact == -1) {
					isError = true;
					createMessageResultText.setFill(Color.RED);
					createMessageResultText.setText("Please Select A Contact");
				}
				if (bodyTextField.getText().isEmpty()) {
					isError = true;
					createMessageResultText.setFill(Color.RED);
					createMessageResultText.setText("The Body Is Required");
				}
				if (titleTextField.getText().isEmpty()) {
					isError = true;
					createMessageResultText.setFill(Color.RED);
					createMessageResultText.setText("The Title Is Required");
				}
				if (!isError) {
					System.out.println("Messaging.....");
					Date timestamp = new Date();
					User contact = contactsObjects.get(selectedContact);
					try {
						final Map<String, String> messageMap = user.sendMessage(contact, titleTextField.getText(),
								bodyTextField.getText(), timestamp);

						ExecutorService sendingMessagePool = Executors.newSingleThreadExecutor();
						sendingMessagePool.execute(() -> {
							boolean isError1 = false;
							try {
								String body = messageMap.get("body");
								messageMap.remove("body");
								System.out.println("Message Sending....");
								isError1 = !send(contact, titleTextField.getText(),
										MapStringConversion.convertMapToString(messageMap), body);
							} catch (IOException e) {

								e.printStackTrace();
								createMessageResultText.setFill(Color.RED);
								createMessageResultText.setText(e.getMessage());
								isError1 = true;
							}
							if (!isError1) {
								createMessageResultText.setFill(Color.GREEN);
								createMessageResultText.setText("The Message Is Sent Successfuly");
							}
						});

					} catch (Exception e) {
						createMessageResultText.setFill(Color.RED);
						createMessageResultText.setText(e.getMessage());
						System.out.println(e.getMessage());
					}

				}
			}
		});
		backMessageButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				stage.setTitle("Safe Communication - Home");
				stage.setScene(homeScene);
				homeVBox.requestFocus();
			}
		});
		createMessageHBox.getChildren().addAll(backMessageButton, sendMessageButton);
		createMessageVBox.getChildren().addAll(createMessageText, titleTextField, bodyTextField, selectContactText,
				contactsMessageListView, createMessageResultText, createMessageHBox);
		createMessageScene = new Scene(createMessageVBox, 600, 500);

		// initialize (add contact) items
		createContactVBox = new VBox(5);
		HBox createContactHBox = new HBox(5);
		HBox ipPortHBox = new HBox(5);
		HBox certificateHBox = new HBox(5);
		createContactButton = new Button();
		pickCertificateButton = new Button();
		backContactButton = new Button("Go Back");
		nameTextField = new TextField();
		ipTextField = new TextField();
		portTextField = new TextField();
		smtpTextField = new TextField();
		Text createContactText = new Text("Add A New Contact...");
		Text certificatePickText = new Text("Pick A Certificate For The Contact");
		Text certificateText = new Text("Certificate Path");
		Text createContactErrorText = new Text();

		// setup (add contact) items
		createContactVBox.setAlignment(Pos.CENTER);
		createContactHBox.setAlignment(Pos.CENTER);
		ipPortHBox.setAlignment(Pos.CENTER);
		certificateHBox.setAlignment(Pos.CENTER);
		createContactErrorText.setFill(Color.RED);
		createContactErrorText.setVisible(false);
		createContactText.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 14));
		nameTextField.setMaxWidth(250);
		smtpTextField.setMaxWidth(250);
		ipTextField.setMaxWidth(220);
		portTextField.setMaxWidth(97);
		createContactButton.setText("Add Contact");
		pickCertificateButton.setText("Pick A File");
		nameTextField.setPromptText("Name");
		ipTextField.setPromptText("IP Address");
		portTextField.setPromptText("Port");
		smtpTextField.setPromptText("SMTP Server");
		pickCertificateButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				File file = fileChooser.showOpenDialog(stage);

				if (file != null) {
					certificateText.setText(file.getAbsolutePath());
				}
			}
		});
		createContactButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				createContactErrorText.setVisible(true);
				boolean isError = false;
				if ((!ipTextField.getText().isEmpty() && portTextField.getText().isEmpty())
						|| (ipTextField.getText().isEmpty() && !portTextField.getText().isEmpty())) {
					isError = true;
					createContactErrorText.setText("Please Enter IP Address & Port Or SMTP Server");
				}
				if ((ipTextField.getText().isEmpty() && portTextField.getText().isEmpty())
						&& smtpTextField.getText().isEmpty()) {
					isError = true;
					createContactErrorText.setText("Please Enter IP Address & Port Or SMTP Server");
				}
				if (nameTextField.getText().isEmpty()) {
					isError = true;
					createContactErrorText.setText("The Name Is Required");
				}
				if (!certificateText.getText().contains(".cer")) {
					isError = true;
					createContactErrorText.setText("Please Pick Vaild Certificate File");
				}
				if (!isError) {
					boolean isEr = false;
					Key k = null;
					try {
						k = new Key(certificateText.getText());
					} catch (Exception e1) {
						isEr = true;
						createContactErrorText.setText(e1.getMessage());
					}
					if (!isEr) {
						String username = k.getCertificate().getIssuerDN().getName()
								.substring(k.getCertificate().getIssuerDN().getName().indexOf("CN=") + 3);
						Map<String, String> map = new HashMap<>();
						map.put("username", username);
						map.put("name", nameTextField.getText());
						map.put("ip", ipTextField.getText());
						map.put("port", portTextField.getText());
						map.put("smtp", smtpTextField.getText());
						map.put("cert", certificateText.getText());
						try {
							FileCSVOperations.writeContactsCSV(map);
						} catch (IOException e) {

							createContactErrorText.setText(e.getMessage());
							isEr = true;
						}
						if (!isEr) {
							User contact = new User(map.get("name"), k, map.get("username"), map.get("ip"),
									Integer.parseInt(map.get("port")), map.get("smtp"));
							contactsObjects.add(contact);
							contacts.add(map.get("username") + " - " + map.get("name"));
							contactsObjects.add(contact);
							createContactErrorText.setFill(Color.GREEN);
							createContactErrorText.setText("The Contact Is Added Successfuly");
						}
					}
				}

			}
		});
		backContactButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				stage.setTitle("Safe Communication - Home");
				stage.setScene(homeScene);
				homeVBox.requestFocus();
			}
		});
		contactsMessageListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// Your action here
				System.out.println("Selected item: " + newValue);
				selectedContact = contacts.indexOf(newValue);

			}
		});
		certificateHBox.getChildren().addAll(pickCertificateButton, certificateText);
		ipPortHBox.getChildren().addAll(ipTextField, portTextField);
		createContactHBox.getChildren().addAll(backContactButton, createContactButton);
		createContactVBox.getChildren().addAll(createContactText, nameTextField, ipPortHBox, smtpTextField,
				certificatePickText, certificateHBox, createContactErrorText, createContactHBox);
		createContactScene = new Scene(createContactVBox, 600, 500);

		// view stage with home scene
		stage.close();
		stage.setScene(homeScene);
		stage.show();
		homeVBox.requestFocus();
		receivingMessagesFromSocketPool = Executors.newSingleThreadExecutor();
		receivingMessagesFromPOP3Pool = Executors.newScheduledThreadPool(1);
		//receiving messages from sockets.
		receivingMessagesFromSocketPool.execute(() -> {
			final int PORT = user.getPort();

			try {
				serverSocket = new ServerSocket(PORT);
				System.out.println("Server started...");
				System.out.println("Wating for clients...");
				while (true) {
					Socket clientSocket = serverSocket.accept();
					Thread t = new Thread() {
						public void run() {
							try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
									Scanner in = new Scanner(clientSocket.getInputStream());) {
								if (in.hasNextLine()) {
									String input1 = in.nextLine();
									Map<String, String> data = MapStringConversion.convertStringToMap(input1);
									System.out.println("Received data from client: " + data);
									String sender = data.get("sender");
									String recipient = data.get("recipient");
									System.out.println(sender);
									System.out.println(recipient);
									System.out.println(data.get("title"));
									System.out.println(data.get("timestamp"));
									if (recipient.matches(user.getUsername())) {
										User senderUser = null;
										boolean isExist = false;
										for (User contact : contactsObjects) {
											if (contact.getUsername().matches(sender)) {
												isExist = true;
												senderUser = contact;
											}
										}
										if (isExist) {
											out.println("OK");
											DataInputStream dIn = new DataInputStream(clientSocket.getInputStream());
											int length = dIn.readInt();
											String input2 = "";// read length of incoming message
											if (length > 0) {
												byte[] message = new byte[length];
												dIn.readFully(message, 0, message.length);
												input2 = new String(message);// read the message
											}
											if (!input2.isEmpty()) {
												out.println("OK BODY");
												data.put("body", input2);
												Message message = null;
												try {
													message = user.receiveMessage(senderUser, data);
												} catch (Exception e) {

													e.printStackTrace();
												}
												if (message != null) {
													receive(message);
												}
												clientSocket.close();
											} else {
												out.println("EMPTY BODY");
												clientSocket.close();
											}
										} else {
											out.println("NOT EXIST");
											clientSocket.close();
										}
									} else {
										out.println("NOT ME");
										clientSocket.close();
									}
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					};
					t.start();
				}
			} catch (IOException e1) {

				e1.printStackTrace();
			}
		});
		receivingMessagesFromSocketPool.shutdown();
		//receiving messages from POP3.
		receivingMessagesFromPOP3Pool.scheduleAtFixedRate(() -> {
			System.out.println("Recieving From POP3");
			POP3Session pop3 = new POP3Session(user.getSmtpAddress(), user.getUsername(), user.getPassword());
			try {
				System.out.println("Connecting to POP3 server...");
				pop3.connectAndAuthenticate();
				System.out.println("Connected to POP3 server.");
				int messageCount = pop3.getMessageCount();
				System.out.println("\nWaiting massages on POP3 server : " + messageCount);
				String[] messages = pop3.getHeaders();
				for (int i = 0; i < messages.length; i++) {
					StringTokenizer messageTokens = new StringTokenizer(messages[i]);
					String messageId = messageTokens.nextToken();
					String messageSize = messageTokens.nextToken();
					String[] message = pop3.getMessage(messageId);
					String messageSender = message[0];
					String messageTitle = message[1];
					String messageBody = message[3];
					User senderUser = null;
					boolean isExist = false;
					for (User contact : contactsObjects) {
						if (contact.getUsername().matches(messageSender)) {
							isExist = true;
							senderUser = contact;
						}
					}
					if (isExist) {
						Message messageObject = null;
						Map<String, String> data = new HashMap<>();
						data.put("sender", messageSender);
						data.put("recipient", user.getUsername());
						data.put("title", messageTitle);
						data.put("body", messageBody);
						data.put("timestamp", (new SimpleDateFormat("dd/MM/yyyy HH:mm")).format(new Date()));
						try {
							messageObject = user.receiveMessage(senderUser, data);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (messageObject != null) {
							receive(messageObject);
						}
					}
				}
			} catch (Exception e) {
				pop3.close();
				System.out.println("Can not receive e-mail!");
				e.printStackTrace();
			}
		}, 0, 10, TimeUnit.MINUTES);
	}

	private boolean send(User recipient, String title, String mailMap, String body) throws IOException {
		System.out.println("Client started.");
		if (recipient.getIp() != null) {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(recipient.getIp(), recipient.getPort()), 10000);
			if (socket.isConnected()) {
				System.out.println("Client Connected");
				System.out.println(body);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				Scanner in = new Scanner(socket.getInputStream());
				String input1 = mailMap;
				//sending map...
				out.println(input1);
				String next = in.nextLine();
				System.out.println(next);
				if (next.matches("OK")) {
					DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
					dOut.writeInt(body.getBytes().length); // write length of the message
					dOut.write(body.getBytes());
					String next1 = in.nextLine();
					if (next1.matches("EMPTY BODY")) {
						socket.close();
						throw new NullPointerException("You Entered An Empty Body");
					} else if (next1.matches("OK BODY")) {
						socket.close();
						return true;
					} else {
						socket.close();
						return false;
					}
				} else if (next.matches("NOT ME")) {
					socket.close();
					throw new NullPointerException("The Recipient's (IP and Port) do not matches with his username");
				} else if (next.matches("NOT EXIST")) {
					socket.close();
					throw new NullPointerException("The Recipient Dosn't has a contact of your profile");
				} else {
					socket.close();
					return false;
				}
			} else {
				socket.close();
				 if (recipient.getSmtpAddress() != null) {
						SMTPSession smtp = new SMTPSession(recipient.getSmtpAddress(), recipient.getUsername(), user.getUsername(),
								title, body);
						boolean isError = false;
						try {
							System.out.println("Sending e-mail By SMTP...");
							smtp.sendMessage();
							System.out.println("E-mail sent.");
						} catch (Exception e) {
							smtp.close();
							System.out.println("Can not send e-mail!");
							e.printStackTrace();
							isError = true;
						}
						return !isError;
				 }else {
					 return false;
				 }
			}
		} else if (recipient.getSmtpAddress() != null) {
			SMTPSession smtp = new SMTPSession(recipient.getSmtpAddress(), recipient.getUsername(), user.getUsername(),
					title, body);
			boolean isError = false;
			try {
				System.out.println("Sending e-mail By SMTP...");
				smtp.sendMessage();
				System.out.println("E-mail sent.");
			} catch (Exception e) {
				smtp.close();
				System.out.println("Can not send e-mail!");
				e.printStackTrace();
				isError = true;
			}
			return !isError;
		} else {
			return false;
		}
	}

	private void receive(Message message) {
		messages.add(
				message.getTitle() + " - " + (new SimpleDateFormat("dd/MM/yyyy HH:mm")).format(message.getTimestamp()));
		messagesObjects.add(message);

	}
}
