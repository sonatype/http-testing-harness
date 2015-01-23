/*
 * Copyright (c) 2010-2014 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.tests.http.server.jetty.behaviour;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.B64Code;

/**
 * Incomplete, last step is not validated.
 *
 * @author Benjamin Hanzelmann
 */
public class NTLMAuth
    extends BehaviourSupport
{
  private boolean authorized;

  private int state;

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    String authHeader = request.getHeader("Authorization");
    switch (state) {
      case 1:
        if (authHeader != null && authHeader.startsWith("NTLM")) {
          log.debug("received type 1: {}", authHeader);
          answerType1(authHeader, response);
          state = 2;
          return false;
        }
        break;
      case 2:
        if (authHeader != null && authHeader.startsWith("NTLM")) {
          log.debug("received type 3: {}", authHeader);
          state = 0;
          return checkType3(authHeader, response);
        }
      default:
        sendChallenge(response);
        state = 1;
        break;
    }
    return false;
  }

  /**
   * @param authHeader
   * @param response
   * @return
   */
  private boolean checkType3(String authHeader, HttpServletResponse response)
  {
    String user = "user";
    String password = "password";
    String host = "test";
    String domain = "Test Server";
    return true;
  }

  /**
   * @param authHeader
   * @param response
   * @throws IOException
   * @throws UnsupportedEncodingException
   */
  private void answerType1(String authHeader, HttpServletResponse response)
      throws UnsupportedEncodingException, IOException
  {
    // byte[] decode = new Base64Encoder().decode( authHeader );
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write("NTLMSSP".getBytes("iso-8859-1"));
    out.write(0);
    out.write(2);
    byte[] zero7 = {0, 0, 0, 0, 0, 0, 0};
    out.write(zero7);
    out.write(convertShort(40));
    out.write(new byte[]{0, 0});
    out.write(new byte[]{1, 65, 0, 0});
    byte[] nonce = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
    out.write(nonce);
    out.write(zero7);
    out.write(0);

    out.close();
    byte[] ba = out.toByteArray();
    System.err.println(Arrays.toString(ba));
    System.err.println(ba.length);
    String answer = new String(B64Code.encode(ba));

    log.debug("Sending type 2 message: NTLM {}", answer);

    response.setHeader("WWW-Authenticate", "NTLM " + answer);
    response.sendError(401);
  }

  /**
   * @param response
   * @throws IOException
   */
  private void sendChallenge(HttpServletResponse response)
      throws IOException
  {
    log.debug("Challenging NTML authentication");
    response.addHeader("WWW-Authenticate", "NTLM");
    response.sendError(401);
  }

  private byte[] convertShort(int num)
  {
    byte[] val = new byte[2];
    String hex = Integer.toString(num, 16);
    while (hex.length() < 4) {
      hex = "0" + hex;
    }
    String low = hex.substring(2, 4);
    String high = hex.substring(0, 2);

    val[0] = (byte) Integer.parseInt(low, 16);
    val[1] = (byte) Integer.parseInt(high, 16);
    return val;
  }

  /**
   * Creates the LANManager and NT response for the given password using the given nonce.
   *
   * @param password the password to create a hash for.
   * @param nonce    the nonce sent by the server.
   * @return The response.
   */
  private byte[] hashPassword(String password, byte[] nonce)
      throws Exception
  {
    byte[] passw = password.toUpperCase().getBytes("iso-8859-1");
    byte[] lmPw1 = new byte[7];
    byte[] lmPw2 = new byte[7];

    int len = passw.length;
    if (len > 7) {
      len = 7;
    }

    int idx;
    for (idx = 0; idx < len; idx++) {
      lmPw1[idx] = passw[idx];
    }
    for (; idx < 7; idx++) {
      lmPw1[idx] = (byte) 0;
    }

    len = passw.length;
    if (len > 14) {
      len = 14;
    }
    for (idx = 7; idx < len; idx++) {
      lmPw2[idx - 7] = passw[idx];
    }
    for (; idx < 14; idx++) {
      lmPw2[idx - 7] = (byte) 0;
    }

    // Create LanManager hashed Password
    byte[] magic =
        {(byte) 0x4B, (byte) 0x47, (byte) 0x53, (byte) 0x21, (byte) 0x40, (byte) 0x23, (byte) 0x24, (byte) 0x25};

    byte[] lmHpw1;
    lmHpw1 = encrypt(lmPw1, magic);

    byte[] lmHpw2 = encrypt(lmPw2, magic);

    byte[] lmHpw = new byte[21];
    for (int i = 0; i < lmHpw1.length; i++) {
      lmHpw[i] = lmHpw1[i];
    }
    for (int i = 0; i < lmHpw2.length; i++) {
      lmHpw[i + 8] = lmHpw2[i];
    }
    for (int i = 0; i < 5; i++) {
      lmHpw[i + 16] = (byte) 0;
    }

    // Create the responses.
    byte[] lmResp = new byte[24];
    calcResp(lmHpw, nonce, lmResp);

    return lmResp;
  }

  private byte[] encrypt(byte[] key, byte[] bytes)
      throws Exception
  {
    Cipher ecipher = getCipher(key);
    byte[] enc = ecipher.doFinal(bytes);
    return enc;

  }

  private Cipher getCipher(byte[] key)
      throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException
  {
    final Cipher ecipher = Cipher.getInstance("DES/ECB/NoPadding");
    key = setupKey(key);
    ecipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "DES"));
    return ecipher;
  }

  private byte[] setupKey(byte[] key56)
  {
    byte[] key = new byte[8];
    key[0] = (byte) ((key56[0] >> 1) & 0xff);
    key[1] = (byte) ((((key56[0] & 0x01) << 6) | (((key56[1] & 0xff) >> 2) & 0xff)) & 0xff);
    key[2] = (byte) ((((key56[1] & 0x03) << 5) | (((key56[2] & 0xff) >> 3) & 0xff)) & 0xff);
    key[3] = (byte) ((((key56[2] & 0x07) << 4) | (((key56[3] & 0xff) >> 4) & 0xff)) & 0xff);
    key[4] = (byte) ((((key56[3] & 0x0f) << 3) | (((key56[4] & 0xff) >> 5) & 0xff)) & 0xff);
    key[5] = (byte) ((((key56[4] & 0x1f) << 2) | (((key56[5] & 0xff) >> 6) & 0xff)) & 0xff);
    key[6] = (byte) ((((key56[5] & 0x3f) << 1) | (((key56[6] & 0xff) >> 7) & 0xff)) & 0xff);
    key[7] = (byte) (key56[6] & 0x7f);

    for (int i = 0; i < key.length; i++) {
      key[i] = (byte) (key[i] << 1);
    }
    return key;
  }

  private void calcResp(byte[] keys, byte[] plaintext, byte[] results)
      throws Exception
  {
    byte[] keys1 = new byte[7];
    byte[] keys2 = new byte[7];
    byte[] keys3 = new byte[7];
    for (int i = 0; i < 7; i++) {
      keys1[i] = keys[i];
    }

    for (int i = 0; i < 7; i++) {
      keys2[i] = keys[i + 7];
    }

    for (int i = 0; i < 7; i++) {
      keys3[i] = keys[i + 14];
    }
    byte[] results1 = encrypt(keys1, plaintext);

    byte[] results2 = encrypt(keys2, plaintext);

    byte[] results3 = encrypt(keys3, plaintext);

    for (int i = 0; i < 8; i++) {
      results[i] = results1[i];
    }
    for (int i = 0; i < 8; i++) {
      results[i + 8] = results2[i];
    }
    for (int i = 0; i < 8; i++) {
      results[i + 16] = results3[i];
    }
  }

}
