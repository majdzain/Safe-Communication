package application;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.KeyTransRecipientInformation;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

/**
 * Message Class to set objects as e-mails and for decryption/encryption.
 */
public class Message {
	private String body;
	private String title;
	private Date timestamp;
	private User sender;
	private User receiver;
	private boolean isDecrypted;

	/**
	 * Creates a new Message object for sending.
	 * 
	 * @param body      {@link String} The body of message.
	 * @param title     {@link String} The title of message.
	 * @param timestamp {@link Date} The date and time of message.
	 * @param sender    which is {@link User} object of user.
	 * @param receiver  which is {@link User} object of recipient.
	 */
	public Message(String body, String title, Date timestamp, User sender, User receiver) {
		this.body = body;
		this.title = title;
		this.timestamp = timestamp;
		this.sender = sender;
		this.receiver = receiver;
	}

	/**
	 * Creates a new Message object for receiving.
	 * 
	 * @param body      {@link String} The body of message.
	 * @param title     {@link String} The title of message.
	 * @param timestamp {@link Date} The date and time of message.
	 * @param sender    which is {@link User} object of sender.
	 */
	public Message(String body, String title, Date timestamp, User sender) {
		this.body = body;
		this.title = title;
		this.timestamp = timestamp;
		this.sender = sender;
	}

	/**
	 * Gets the body of the message.
	 * 
	 * @return The {@link String} body of the message.
	 */
	public String getBody() {
		return body;
	}

	/**
	 * Gets the title of the message.
	 * 
	 * @return The {@link String} title of the message.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Gets the sender of the message.
	 * 
	 * @return The {@link User} sender of the message.
	 */
	public User getSender() {
		return sender;
	}

	/**
	 * Gets the date and time of the message.
	 * 
	 * @return The {@link Date} which contains the time and date of the message.
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * Gets the receiver of the message.
	 * 
	 * @return The {@link User} receiver of the message.
	 */
	public User getReceiver() {
		return receiver;
	}
	

	/**
	 * Secure The Message by sign the message's body with the private key of user
	 * then encrypt The message's body with the public key of receiver then returns
	 * the new message object.
	 * 
	 * @param userPrivateKey   {@link Key} object of the user which contains private
	 *                         key.
	 * @param contactPublicKey {@link Key} object of the receiver which contains
	 *                         certificate.
	 * @return The {@link Message} object after securing.
	 */
	public Message secureMessage(Key userPrivateKey, Key contactPublicKey) throws Exception {
		System.out.println("Signing Message...");
		sign(userPrivateKey);
		System.out.println("Encrypting Message....");
		encrypt(contactPublicKey);
		return this;
	}

	/**
	 * UnSecure The Message if it isn't decrypted by decrypt the message's body with
	 * the private key of user then check if the signature of The message's body is
	 * from the specific sender then returns the new message object.
	 * 
	 * @param userPrivateKey   {@link Key} object of the user which contains private
	 *                         key.
	 * @param contactPublicKey {@link Key} object of the receiver which contains
	 *                         certificate.
	 * @return The {@link Message} object after unsecuring.
	 */
	public Message unsecureMessage(Key userPrivateKey, Key contactPublicKey) throws Exception {
		if (!isDecrypted) {
			decrypt(userPrivateKey);
			decryptWithPublicKey(contactPublicKey);
			this.isDecrypted = true;
		}
		return this;
	}

	private byte[] encrypt(Key publicKey) throws CertificateEncodingException, CMSException, IOException {
		byte[] encryptedData = null;
		if (null != body && publicKey != null && null != publicKey.getCertificate()) {
			CMSEnvelopedDataGenerator cmsEnvelopedDataGenerator = new CMSEnvelopedDataGenerator();

			JceKeyTransRecipientInfoGenerator jceKey = new JceKeyTransRecipientInfoGenerator(
					publicKey.getCertificate());
			cmsEnvelopedDataGenerator.addRecipientInfoGenerator(jceKey);
			CMSTypedData msg = new CMSProcessableByteArray(body.getBytes());
			OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC).setProvider("BC")
					.build();
			CMSEnvelopedData cmsEnvelopedData = cmsEnvelopedDataGenerator.generate(msg, encryptor);
			encryptedData = cmsEnvelopedData.getEncoded();
		}
		this.body = new String(encryptedData);
		return encryptedData;
	}

	private byte[] decrypt(Key privateKey) throws CMSException {
		byte[] decryptedData = null;
		if (null != body && null != privateKey && null != privateKey.getPrivateKey()) {
			CMSEnvelopedData envelopedData = new CMSEnvelopedData(body.getBytes());

			Collection<RecipientInformation> recipients = envelopedData.getRecipientInfos().getRecipients();
			KeyTransRecipientInformation recipientInfo = (KeyTransRecipientInformation) recipients.iterator().next();
			JceKeyTransRecipient recipient = new JceKeyTransEnvelopedRecipient(privateKey.getPrivateKey());

			decryptedData = recipientInfo.getContent(recipient);
		}
		this.body = new String(decryptedData);
		return decryptedData;

	}

	private byte[] sign(Key privateKey) throws Exception {
		byte[] signedMessage = null;
		List<X509Certificate> certList = new ArrayList<X509Certificate>();
		CMSTypedData cmsData = new CMSProcessableByteArray(body.getBytes());
		certList.add(privateKey.getCertificate());
		Store certs = new JcaCertStore(certList);

		CMSSignedDataGenerator cmsGenerator = new CMSSignedDataGenerator();
		ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey.getPrivateKey());
		cmsGenerator.addSignerInfoGenerator(
				new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").build())
						.build(contentSigner, privateKey.getCertificate()));
		cmsGenerator.addCertificates(certs);

		CMSSignedData cms = cmsGenerator.generate(cmsData, true);
		signedMessage = cms.getEncoded();
		this.body = new String(signedMessage);
		return signedMessage;
	}

	private boolean checkSignature(Key publicKey) throws Exception {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(body.getBytes());
		ASN1InputStream asnInputStream = new ASN1InputStream(inputStream);
		CMSSignedData cmsSignedData = new CMSSignedData(ContentInfo.getInstance(asnInputStream.readObject()));
		for (Object info : cmsSignedData.getSignerInfos().getSigners()) {
			SignerInformation signer = (SignerInformation) info;
			if (signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC")
					.build(publicKey.getCertificate().getPublicKey()))) {
				return true;
			}
		}
		return false;
	}

	private byte[] decryptWithPublicKey(Key publicKey) throws Exception {
		if (checkSignature(publicKey)) {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(body.getBytes());
			ASN1InputStream asnInputStream = new ASN1InputStream(inputStream);
			CMSSignedData cmsSignedData = new CMSSignedData(ContentInfo.getInstance(asnInputStream.readObject()));
			byte[] data = (byte[]) cmsSignedData.getSignedContent().getContent();
			this.body = new String(data);
			return data;
		} else {
			return null;
		}
	}
}
