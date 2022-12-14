/**
* %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
* 
* Copyright (c) 2012 - SCAPI (http://crypto.biu.ac.il/scapi)
* This file is part of the SCAPI project.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
* to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
* and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
* FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
* 
* We request that any publication and/or code referring to and/or based on SCAPI contain an appropriate citation to SCAPI, including a reference to
* http://crypto.biu.ac.il/SCAPI.
* 
* SCAPI uses Crypto++, Miracl, NTL and Bouncy Castle. Please see these projects for any further licensing issues.
* %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
* 
*/
package edu.biu.scapi.midLayer.asymmetricCrypto.digitalSignature;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import edu.biu.scapi.midLayer.signature.RSASignature;
import edu.biu.scapi.midLayer.signature.Signature;

/**
 * This class implements the RSA PSS signature scheme, using OpenSSL RSAPss implementation.
 * The RSA PSS (Probabilistic Signature Scheme) is a provably secure way of creating signatures with RSA.
 * 
 * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Moriya Farbstein)
 *
 */
public class OpenSSLRSAPss extends RSAPssAbs {

	private long rsa;		//Pointer to the native OpenSSL's signature object.
	private boolean isPrivateKeySet;
	
	// JNI native functions. The functions of this class call the necessary native functions to perform the signature operations.
	//Creates the native signature object.
	private native long createRSASignature();
	//Initializes the native RSA object with a public key.
	private native void initRSAVerifier(long verifier, byte[] modulus, byte[] exponent);
	//Initializes the native RSA object with a private key.
	private native void initRSACrtSigner(long signer, byte[] modulus, byte[] exponent, byte[] d, byte[] p, byte[] q, byte[] dp, byte[]dq, byte[] crt);
	//Initializes the native RSA object with a private CRT key.
	private native void initRSASigner(long signer, byte[] modulus, byte[] exponent, byte[] d);
	//Signs on the given message.
	private native byte[] doSign(long signer, byte[] msg, int offset, int length);	
	//Verifies the signature of the given message.
	private native boolean doVerify(long verifier, byte[] signature, byte[] msg, int offset, int msgLen);	
	//Deletes the native RSA object.
	private native void deleteRSA(long rsa);	
	
	/**
	 * Default constructor. uses default implementation of SecureRandom.
	 */
	public OpenSSLRSAPss(){
		//Calls the other constructor with default parameter.
		this(new SecureRandom());
	}
	
	/**
	 * Constructor that receives random number generation algorithm to use.
	 * @param randNumGenAlg random number generation algorithm to use
	 * @throws NoSuchAlgorithmException if there is no random number generation algorithm
	 */
	public OpenSSLRSAPss(String randNumGenAlg) throws NoSuchAlgorithmException {
		//Call the other constructor with SecureRandom object.
		this(SecureRandom.getInstance(randNumGenAlg));
	}
	
	/**
	 * Constructor that receives the secure random object to use.
	 * @param random secure random to use
	 */
	public OpenSSLRSAPss(SecureRandom random) {
		this.random = random;
		
		//Create the native signature object.
		this.rsa = createRSASignature();
	
	}
	
	/**
	 * Sets this RSA PSS scheme with public key and private key.
	 * @param publicKey should be an instance of RSAPublicKey.
	 * @param privateKey should be an instance of RSAPrivateKey.
	 * @throws InvalidKeyException if the given keys are not instances of RSA keys.
	 */
	@Override
	public void setKey(PublicKey publicKey, PrivateKey privateKey) throws InvalidKeyException {
		if (!(publicKey instanceof RSAPublicKey)) {
			throw new InvalidKeyException("Key type doesn't match the RSA signature scheme type");
		}
		
		if ((privateKey!=null) && !(privateKey instanceof RSAPrivateKey)){
			throw new InvalidKeyException("Key type doesn't match the RSA signature scheme type");
		}
			
		//Notice! We set the public key twice - in the PublicKey member and in the native verifier object.
		//This can lead to many synchronization problems, so we need to be very careful not to change just one of them.
		this.publicKey = (RSAPublicKey) publicKey;
		
		//Get the values of modulus (N), pubExponent (e), privExponent (d).
		BigInteger pubExponent = ((RSAPublicKey) publicKey).getPublicExponent();
		BigInteger modN = ((RSAKey) publicKey).getModulus();
		
		//Initialize the native verifier with the RSA parameters - n, e.
		initRSAVerifier(rsa, modN.toByteArray(), pubExponent.toByteArray());
		
		if (privateKey != null){
			
			BigInteger privExponent = ((RSAPrivateKey) privateKey).getPrivateExponent();
			//If private key is CRT private key.
			if (privateKey instanceof RSAPrivateCrtKey)
			{
				//Get all the crt parameters.
				RSAPrivateCrtKey key = (RSAPrivateCrtKey) privateKey;
				BigInteger p = key.getPrimeP();
				BigInteger q = key.getPrimeQ();
				BigInteger dp = key.getPrimeExponentP();
				BigInteger dq = key.getPrimeExponentQ();
				BigInteger crt = key.getCrtCoefficient();
				
				//Initialize the native signer.
				initRSACrtSigner(rsa, modN.toByteArray(), pubExponent.toByteArray(), privExponent.toByteArray(), 
						p.toByteArray(), q.toByteArray(), dp.toByteArray(), dq.toByteArray(), crt.toByteArray());
				
			//If private key is key with N, e, d.
			} else {
				
				//Initialize the native signer with the RSA parameters - n, e, d.
				initRSASigner(rsa, modN.toByteArray(), pubExponent.toByteArray(), privExponent.toByteArray());
			}
			isPrivateKeySet = true;
		}
		
		isKeySet = true;
		
	}

	/**
	 * Sets this RSA PSS with a public key.<p> 
	 * In this case the signature object can be used only for verification.
	 * @param publicKey should be an instance of RSAPublicKey.
	 * @throws InvalidKeyException if the given key is not an instance of RSAPublicKey.
	 */
	@Override
	public void setKey(PublicKey publicKey) throws InvalidKeyException {
		setKey(publicKey, null);
		
	}

	/**
	 * Signs the given message using the native signature object.
	 * @param msg the byte array to verify the signature with.
	 * @param offset the place in the msg to take the bytes from.
	 * @param length the length of the msg.
	 * @return the output signature from the signing.
	 * @throws KeyException if PrivateKey is not set.
	 * @throws ArrayIndexOutOfBoundsException if the given offset and length are wrong for the given message.
	 */
	@Override
	public Signature sign(byte[] msg, int offset, int length)
			throws KeyException {
		//If there is no private key can not sign, throws exception.
		if (!isPrivateKeySet){
			throw new KeyException("in order to sign a message, this object must be initialized with private key");
		}
		
		// Check that the offset and length are correct.
		if ((offset > msg.length) || (offset+length > msg.length)){
			throw new ArrayIndexOutOfBoundsException("wrong offset for the given input buffer");
		}
		
		//Call native function that performs the signing.
		byte[] signature = doSign(rsa, msg, offset, length);
		
		return new RSASignature(signature);
	}

	/**
	 * Verifies the given signature using the native object.
	 * @param signature to verify should be an instance of RSASignature.
	 * @param msg the byte array to verify the signature with
	 * @param offset the place in the msg to take the bytes from
	 * @param length the length of the msg
	 * @return true if the signature is valid. false, otherwise.
	 * @throws IllegalStateException if no public key was set.
	 * @throws IllegalArgumentException if the given Signature is not an instance of RSASignature.
	 * @throws ArrayIndexOutOfBoundsException if the given offset and length are wrong for the given message.
	 */
	@Override
	public boolean verify(Signature signature, byte[] msg, int offset, int length) {
		//If there is no public key can not encrypt, throws exception.
		if (!isKeySet()){
			throw new IllegalStateException("in order to encrypt a message this object must be initialized with public key");
		}
		
		if (!(signature instanceof RSASignature)){
			throw new IllegalArgumentException("Signature must be instance of RSASignature");
		}
		
		// Check that the offset and length are correct.
		if ((offset > msg.length) || (offset+length > msg.length)){
			throw new ArrayIndexOutOfBoundsException("wrong offset for the given input buffer");
		}
		
		//Get the signature bytes
		byte[] sigBytes = ((RSASignature) signature).getSignatureBytes();
		
		//Call native function that perform the verification.
		return doVerify(rsa, sigBytes, msg, offset, length);
		
	}
	
	/**
	 * Deletes the related RSA object.
	 */
	protected void finalize() throws Throwable {

		// Delete from the dll the dynamic allocation of the RSA object.
		deleteRSA(rsa);

	}	
	
	//Loads the OpenSSL library.
	static {
	      System.loadLibrary("OpenSSLJavaInterface");
	}

}
