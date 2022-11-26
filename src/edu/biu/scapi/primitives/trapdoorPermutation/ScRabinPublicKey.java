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


package edu.biu.scapi.primitives.trapdoorPermutation;

import java.math.BigInteger;


/**
 * Concrete class of RabinPublicKey
 *
 * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Moriya Farbstein)
 */
public class ScRabinPublicKey extends ScRabinKey implements RabinPublicKey {
	
	private static final long serialVersionUID = 1L;
	
	private BigInteger quadraticResidueModPrime1 = null; //r
	private BigInteger quadraticResidueModPrime2 = null; //s

	/**
	 * Constructor that accepts the public key parameters and sets them.
	 * @param mod modulus
	 * @param r - quadratic residue mod prime1
	 * @param s - quadratic residue mod prime2
	 */
	public ScRabinPublicKey (BigInteger mod, BigInteger r, BigInteger s) {
		modulus = mod;
		quadraticResidueModPrime1 = r;
		quadraticResidueModPrime2 = s;
	}

	/**
	 * @return the algorithm name - Rabin
	 */
	public String getAlgorithm() {
		
		return "Rabin";
	}

	/**
	 * @return the encoded key
	 */
	public byte[] getEncoded() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the format of the encoding
	 */
	public String getFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return BigInteger - QuadraticResidueModPrime1 (r)
	 */
	public BigInteger getQuadraticResidueModPrime1() {
		
		return quadraticResidueModPrime1;
	}

	/**
	 * @return BigInteger - QuadraticResidueModPrime2 (s)
	 */
	public BigInteger getQuadraticResidueModPrime2() {
		
		return quadraticResidueModPrime2;
	}

}
