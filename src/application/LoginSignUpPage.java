package application;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * LoginSignUpPage Class to show the two pages the Login Page and the Signup Page.
 */
public class LoginSignUpPage extends Application {
	private TextField emailTextField;
	private PasswordField passwordTextField;
	private TextField rEmailTextField, rNameTextField, rIpAddressTextField, rPortTextField, rSMTPServerTextField;
	private PasswordField rPasswordTextField, rRePasswordTextField;
	private Button loginButton, signupButton;
	private Button backButton, registerButton;
	private Scene loginScene, signUpScene;
	private Text loginErrorText, signupErrorText;
	private VBox loadingLoginVBox, loadingSignupVBox;
	private Text loadingLoginErrorText, loadingSignupErrorText;
	private Stage stage;
	private HomePage homePage;

	/** Creates a new LoginSignUpPage object to show the page by the program.
	*/
	public LoginSignUpPage() {

	}

	/** Creates a new LoginSignUpPage object to show the page by {@link HomePage} Class when deleting the profile and come back.
	*/
	public LoginSignUpPage(Stage stage) {
		this.stage = stage;
		startScreen(stage);
	}

	/** Opens this page by the program.
	*/
	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		launch(args);
	}

	@Override
	public void stop() throws Exception {
		//closes all the sockets opened when stop the program.
		if (homePage != null) {
			homePage.serverSocket.close();
			homePage.receivingMessagesFromPOP3Pool.shutdownNow();
		}
		super.stop();
	}

	@Override
	public void start(Stage primaryStage) {
		startScreen(primaryStage);
	}

	private void startScreen(Stage primaryStage) {
		primaryStage.setTitle("Safe Communication - Login");

		// initialize login items
		Text welcomeText = new Text();
		emailTextField = new TextField();
		passwordTextField = new PasswordField();
		loginButton = new Button();
		signupButton = new Button();
		loginErrorText = new Text();
		loadingLoginErrorText = new Text();
		loadingLoginVBox = new VBox(3);
		VBox loginVBox = new VBox(5);
		HBox loginHBox = new HBox(5);

		// initialize signup items
		Text registerText = new Text();
		signupErrorText = new Text();
		rEmailTextField = new TextField();
		rNameTextField = new TextField();
		rIpAddressTextField = new TextField();
		rPortTextField = new TextField();
		rSMTPServerTextField = new TextField();
		rPasswordTextField = new PasswordField();
		rRePasswordTextField = new PasswordField();
		backButton = new Button();
		registerButton = new Button();
		loadingSignupErrorText = new Text();
		loadingSignupVBox = new VBox(5);
		VBox signupVBox = new VBox(5);
		HBox signupHBox = new HBox(5);
		HBox signupHBoxIPPort = new HBox(5);

		// setup login items
		welcomeText.setText("Welcome To Safe Communication...");
		welcomeText.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 14));
		loadingLoginErrorText.setFill(Color.RED);
		loginErrorText.setFill(Color.RED);
		loginErrorText.setVisible(false);
		loadingLoginVBox.setVisible(false);
		loadingLoginErrorText.setVisible(false);
		loginVBox.setAlignment(Pos.CENTER);
		loginHBox.setAlignment(Pos.CENTER);
		loadingLoginVBox.setAlignment(Pos.CENTER);
		loginButton.setText("Login");
		signupButton.setText("Sign Up");
		emailTextField.setPromptText("Enter Your Email Or Username");
		passwordTextField.setPromptText("Enter Your Password");
		loginButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				boolean isError = false;
				if (passwordTextField.getText().isEmpty()) {
					isError = true;
					loginErrorText.setText("Please Enter Your Password");
					loginErrorText.setVisible(true);
				}
				if (emailTextField.getText().isEmpty()) {
					isError = true;
					loginErrorText.setText("Please Enter Your Email");
					loginErrorText.setVisible(true);
				}

				if (!isError) {
					loadingLoginErrorText.setVisible(false);
					loadingLoginVBox.setVisible(true);
					Key key = null;
					try {
						key = Key.getExistUserKey(emailTextField.getText(), passwordTextField.getText());
					} catch (Exception e) {
						loadingLoginErrorText.setText(e.getMessage());
						loadingLoginErrorText.setVisible(true);
					}
					if (key != null) {
						String name = null, ip = null, port = null, smtp = null;
						try {
							ArrayList<Map<String, String>> users = FileCSVOperations.readUsersCSV();
							for (int i = 0; i < users.size(); i++) {
								if (users.get(i).get("username").matches(emailTextField.getText())) {
									ip = users.get(i).get("ip");
									port = users.get(i).get("port");
									smtp = users.get(i).get("smtp");
									break;
								}
							}
							name = key.getCertificate().getSubjectDN().getName()
									.substring(key.getCertificate().getSubjectDN().getName().indexOf("L=") + 2);
						} catch (Exception e) {
							
						}
							if (name != null && ip != null && port != null && smtp != null) {
								System.out.println("Success");
								User user = User.getInstance(key, passwordTextField.getText(), name,
										new Key(key.getCertificate(), key.getCertificatePath()),
										emailTextField.getText(), ip, Integer.parseInt(port), smtp);
								homePage = new HomePage(primaryStage, user);
							} else {
								loadingLoginErrorText.setText("Missed Informations");
								loadingLoginErrorText.setVisible(true);
							}

					}
				}
			}
		});
		signupButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				primaryStage.setScene(signUpScene);
				primaryStage.setTitle("Safe Communication - Sign Up");
			}
		});
		loadingLoginVBox.getChildren().addAll(loadingLoginErrorText);
		loginHBox.getChildren().addAll(loginButton, signupButton);
		loginVBox.getChildren().addAll(welcomeText, emailTextField, passwordTextField, loginErrorText, loginHBox,
				loadingLoginVBox);
		loginScene = new Scene(loginVBox, 600, 500);

		// setup signup items
		registerText.setText("Create A New Profile...");
		registerText.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 14));
		signupErrorText.setFill(Color.RED);
		loadingSignupErrorText.setFill(Color.RED);
		signupErrorText.setVisible(false);
		loadingSignupVBox.setVisible(false);
		loadingSignupErrorText.setVisible(false);
		signupVBox.setAlignment(Pos.CENTER);
		signupHBox.setAlignment(Pos.CENTER);
		signupHBoxIPPort.setAlignment(Pos.CENTER);
		loadingSignupVBox.setAlignment(Pos.CENTER);
		registerButton.setText("Register");
		backButton.setText("Go Back");
		rEmailTextField.setPromptText("Email Or Username");
		rPasswordTextField.setPromptText("Password");
		rRePasswordTextField.setPromptText("Re-Password");
		rNameTextField.setPromptText("Full Name");
		rIpAddressTextField.setPromptText("IP Address");
		rPortTextField.setPromptText("Port");
		rSMTPServerTextField.setPromptText("SMTP Server");
		emailTextField.setMaxWidth(250);
		passwordTextField.setMaxWidth(250);
		rEmailTextField.setMaxWidth(250);
		rPasswordTextField.setMaxWidth(250);
		rRePasswordTextField.setMaxWidth(250);
		rNameTextField.setMaxWidth(250);
		rIpAddressTextField.setMaxWidth(220);
		rPortTextField.setMaxWidth(97);
		rSMTPServerTextField.setMaxWidth(250);
		registerButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				boolean isError = false;
				if (rSMTPServerTextField.getText().isEmpty()) {
					isError = true;
					signupErrorText.setText("Please Enter Your SMTP Server");
					signupErrorText.setVisible(true);
				}
				if (rPortTextField.getText().isEmpty()) {
					isError = true;
					signupErrorText.setText("Please Enter Your Port");
					signupErrorText.setVisible(true);
				}
				if (rIpAddressTextField.getText().isEmpty()) {
					isError = true;
					signupErrorText.setText("Please Enter Your IP Address");
					signupErrorText.setVisible(true);
				}
				if (rPasswordTextField.getText().isEmpty()) {
					isError = true;
					signupErrorText.setText("Please Enter Your Password");
					signupErrorText.setVisible(true);
				}
				if (rRePasswordTextField.getText().isEmpty()) {
					isError = true;
					signupErrorText.setText("Please Enter Your Password");
					signupErrorText.setVisible(true);
				}
				if (rEmailTextField.getText().isEmpty()) {
					isError = true;
					signupErrorText.setText("Please Enter Your Email");
					signupErrorText.setVisible(true);
				}
				if (rNameTextField.getText().isEmpty()) {
					isError = true;
					signupErrorText.setText("Please Enter Your Name");
					signupErrorText.setVisible(true);
				}

				if (!isError) {
					if (!rPasswordTextField.getText().matches(rRePasswordTextField.getText())) {
						isError = true;
						signupErrorText.setText("The Two Passwords Do Not Matches");
						signupErrorText.setVisible(true);
					}
					if (!isError) {
						loadingSignupErrorText.setVisible(false);
						loadingSignupVBox.setVisible(true);
						Key key = null;
						try {
							key = Key.generateNewUserKey(rNameTextField.getText(), rEmailTextField.getText(),
									rPasswordTextField.getText());
						} catch (Exception e) {
							loadingSignupErrorText.setText(e.getMessage());
							loadingSignupErrorText.setVisible(true);
						}

						if (key != null) {
							boolean isEr = false;
							try {
								Map<String, String> map = new HashMap<>();
								map.put("username", rEmailTextField.getText());
								map.put("ip", rIpAddressTextField.getText());
								map.put("port", rPortTextField.getText());
								map.put("smtp", rSMTPServerTextField.getText());
								FileCSVOperations.writeUsersCSV(map);
							} catch (Exception e) {
								isEr = true;
								loadingSignupErrorText.setText(e.getMessage());
								loadingSignupErrorText.setVisible(true);
							}
							if (!isEr) {
								System.out.println("Success");
								User user = User.getInstance(key, rPasswordTextField.getText(),
										rNameTextField.getText(),
										new Key(key.getCertificate(), key.getCertificatePath()),
										rEmailTextField.getText(), rIpAddressTextField.getText(),
										Integer.parseInt(rPortTextField.getText()), rSMTPServerTextField.getText());
								homePage = new HomePage(primaryStage, user);
							}
						}
					}
				}
			}
		});
		backButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				primaryStage.setScene(loginScene);
				primaryStage.setTitle("Safe Communication - Login");
			}
		});
		loadingSignupVBox.getChildren().addAll(loadingSignupErrorText);
		signupHBoxIPPort.getChildren().addAll(rIpAddressTextField, rPortTextField);
		signupHBox.getChildren().addAll(backButton, registerButton);
		signupVBox.getChildren().addAll(registerText, rNameTextField, rEmailTextField, rPasswordTextField,
				rRePasswordTextField, signupHBoxIPPort, rSMTPServerTextField, signupErrorText, signupHBox,
				loadingSignupVBox);
		signUpScene = new Scene(signupVBox, 600, 500);

		primaryStage.setScene(loginScene);
		primaryStage.show();
	}
}
