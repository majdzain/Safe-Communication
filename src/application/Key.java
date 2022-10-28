package application;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.misc.NetscapeCertType;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Hex;

/**
 * Key Class to set encyption and decryption variables for senders, recipients,
 * and users.
 */
public class Key {
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private String password;
	private X509Certificate certificate;
	private String certificatePath;

	/** Creates a new Key object for users.
	 * @param privateKey which is {@link PrivateKey} object of user.
	 * @param publicKey which is {@link PublicKey} object of user.
	 * @param password {@link String} The password of user.
	 * @param certificate which is {@link X509Certificate} object of user.
	 * @param certificatePath {@link String} The path of certificate.
	*/
	public Key(PrivateKey privateKey, PublicKey publicKey, String password, X509Certificate certificate,
			String certificatePath) {
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.password = password;
		this.certificate = certificate;
		this.certificatePath = certificatePath;
	}

	/** Creates a new Key object for recipients.
	 * @param certificate which is {@link X509Certificate} object and contains public key of recipient.
	 * @param certificatePath {@link String} The path of certificate.
	*/
	public Key(X509Certificate certificate, String certificatePath) {
		this.certificate = certificate;
		this.publicKey = certificate.getPublicKey();
		this.certificatePath = certificatePath;
	}

	/** Creates a new Key object from certificate path for recipients.
	 * @param certificatePath {@link String} The path of certificate.
	*/
	public Key(String certificatePath) throws Exception {
		this(getContactFromCertificatePath(certificatePath).getCertificate(), certificatePath);
	}

	/** Gets the certificate of user and recipient.
	 * @return The {@link X509Certificate} which contains public key.
	*/
	public X509Certificate getCertificate() {
		return certificate;
	}

	/** Gets the user's private key.
	 * @return The {@link PrivateKey} of user.
	*/
	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	/** Gets the public key of user and recipient.
	 * @return The {@link PublicKey} of user and recipient.
	*/
	public PublicKey getPublicKey() {
		return publicKey;
	}
	
	/** Gets the certificate path of user and recipient.
	 * @return The {@link String} certificate path of user and recipient.
	*/
	public String getCertificatePath() {
		return certificatePath;
	}
	
	/** Gets the password of user.
	 * @return The {@link String} password of user.
	*/
	public String getPassword() {
		return password;
	}

	private static Key getContactFromCertificatePath(String path) throws Exception {
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");
		X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new FileInputStream(path));
		return new Key(certificate, path);
	}
	
	/** Gets A New Key object from existing certificates files.
	 * @param username {@link String} The user name of user.
	 * @param password {@link String} The password of user. 
	 * @return The {@link Key} object after checking if the certificates exists.
	*/
	public static Key getExistUserKey(String username, String password) throws Exception {
		if (!(new File("C:\\Safe Communication\\users\\" + username + ".cer")).exists()
				|| !(new File("C:\\Safe Communication\\users\\" + username + ".p12")).exists()) {
			throw new NullPointerException("You Entered A User Which Is Not Found");
		} else {

			CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");
			X509Certificate certificate = (X509Certificate) certFactory
					.generateCertificate(new FileInputStream("C:\\Safe Communication\\users\\" + username + ".cer"));
			KeyStore keystore = KeyStore.getInstance("PKCS12");
			keystore.load(new FileInputStream("C:\\Safe Communication\\users\\" + username + ".p12"),
					password.toCharArray());
			PrivateKey privateKey = (PrivateKey) keystore.getKey("privatekeyalias", password.toCharArray());
			return new Key(privateKey, certificate.getPublicKey(), password, certificate,
					"C:\\Safe Communication\\users\\" + username + ".cer");
		}
	}


	/** Generate A New Key object for users and save its certificates in files.
	 * @param name {@link String} The name of user.
	 * @param username {@link String} The user name of user.
	 * @param password {@link String} The password of user. 
	 * @return The {@link Key} object after saving.
	*/
	public static Key generateNewUserKey(String name, String username, String password) throws Exception {
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");
		KeyPair keyPair = createKeyPair("RSA", 2048);
		X509Certificate certificate = readCertificateFromASN1Certificate(createCertificate(username, name, keyPair),
				certFactory);
		convertCertificateToPEM(certificate, username);
		convertPrivateKeyPasswordToPEM(keyPair.getPrivate(), certificate, username, password);

		return new Key(keyPair.getPrivate(), keyPair.getPublic(), password, certificate,
				"C:\\Safe Communication\\users\\" + username + ".cer");
	}

	/** Delete The certificates files of Key object.
	 * @param username {@link String} The user name of user.
	*/
	public static void deleteUserKey(String username) throws Exception {
		File certFile = new File("C:\\Safe Communication\\users\\" + username + ".cer");
		File p12File = new File("C:\\Safe Communication\\users\\" + username + ".p12");
		certFile.delete();
		p12File.delete();
	}

	private static KeyPairGenerator createKeyPairGenerator(String algorithmIdentifier, int bitCount)
			throws NoSuchProviderException, NoSuchAlgorithmException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance(algorithmIdentifier, BouncyCastleProvider.PROVIDER_NAME);
		kpg.initialize(bitCount);
		return kpg;
	}

	private static KeyPair createKeyPair(String encryptionType, int byteCount)
			throws NoSuchProviderException, NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = createKeyPairGenerator(encryptionType, byteCount);
		KeyPair keyPair = keyPairGenerator.genKeyPair();
		return keyPair;
	}

	private static String convertCertificateToPEM(X509Certificate signedCertificate, String username)
			throws IOException {
		File path1 = new File("C:\\Safe Communication\\");
		File path2 = new File("C:\\Safe Communication\\users\\");
		if (!path1.exists()) {
			path1.mkdir();
		}
		if (!path2.exists()) {
			path2.mkdir();
		}
		FileWriter writer = new FileWriter("C:\\Safe Communication\\users\\" + username + ".cer");
		JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
		pemWriter.writeObject(signedCertificate);
		pemWriter.close();
		return writer.toString();
	}

	private static String convertPrivateKeyPasswordToPEM(PrivateKey privateKey, X509Certificate certificate,
			String username, String password) throws Exception {
		KeyStore pkcs12 = KeyStore.getInstance("PKCS12");
		pkcs12.load(null, null);
		pkcs12.setKeyEntry("privatekeyalias", privateKey, password.toCharArray(),
				new X509Certificate[] { certificate });
		try (FileOutputStream p12 = new FileOutputStream("C:\\Safe Communication\\users\\" + username + ".p12")) {
			pkcs12.store(p12, password.toCharArray());
		}
		KeyStore testp12 = KeyStore.getInstance("PKCS12");
		try (FileInputStream p12 = new FileInputStream("C:\\Safe Communication\\users\\" + username + ".p12")) {
			testp12.load(p12, password.toCharArray());
		}
		return Hex.toHexString(testp12.getKey("privatekeyalias", password.toCharArray()).getEncoded());
	}

	private static org.bouncycastle.asn1.x509.Certificate createCertificate(String username, String name,
			KeyPair keyPair) throws Exception {

		X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(new X500Name("CN=" + username),
				BigInteger.valueOf(System.currentTimeMillis()), new Date(System.currentTimeMillis()),
				new Date(System.currentTimeMillis() + 30L * 365L * 24L * 60L * 60L * 1000L),
				new X500Name("CN=" + username + ",L=" + name), keyPair.getPublic())
						.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), false, new BasicConstraints(false))
						.addExtension(new ASN1ObjectIdentifier("2.16.840.1.113730.1.1"), false,
								new NetscapeCertType(NetscapeCertType.sslClient | NetscapeCertType.sslServer
										| NetscapeCertType.smime | NetscapeCertType.objectSigning))
						.addExtension(new ASN1ObjectIdentifier("2.5.29.15"), true,
								new X509KeyUsage(X509KeyUsage.keyEncipherment));
		ContentSigner sigGen = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
		X509CertificateHolder x509CertificateHolder = certificateBuilder.build(sigGen);
		org.bouncycastle.asn1.x509.Certificate eeX509CertificateStructure = x509CertificateHolder.toASN1Structure();
		return eeX509CertificateStructure;
	}

	private static X509Certificate readCertificateFromASN1Certificate(
			org.bouncycastle.asn1.x509.Certificate eeX509CertificateStructure, CertificateFactory certificateFactory)
			throws IOException, CertificateException {
		InputStream is1 = new ByteArrayInputStream(eeX509CertificateStructure.getEncoded());
		X509Certificate signedCertificate = (X509Certificate) certificateFactory.generateCertificate(is1);
		return signedCertificate;
	}
}
