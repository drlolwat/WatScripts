package org.lolwat.tasks.types.misc;

import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.utilities.Logger;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.*;

public class Tasker {
    private static final String serverAddress = "lic-auth-usa1.botbuddy.net";
    private static final int serverPort = 7843;
    private static final byte[] key = "AES_LOLWAT_KEYBB".getBytes();
    private static final String version = "2.0.6";

    private static boolean d() {
        String licenseKey = "freedom";
        try (Socket socket = new Socket(serverAddress, serverPort)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            byte[] nonce = new byte[12];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(nonce);

            byte[] ciphertext = cipher.doFinal(licenseKey.getBytes());

            ByteArrayOutputStream encryptedMsg = new ByteArrayOutputStream();
            encryptedMsg.write(nonce);
            encryptedMsg.write(ciphertext);

            int msgLen = encryptedMsg.size();
            out.writeInt(msgLen);
            out.write(encryptedMsg.toByteArray());
            out.flush();

            int responseLen = in.readInt();
            byte[] encryptedResponse = new byte[responseLen];
            in.readFully(encryptedResponse);

            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, encryptedResponse, 0, 12);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);

            byte[] decryptedResponse = cipher.doFinal(encryptedResponse, 12, responseLen - 12);
            String plaintext = new String(decryptedResponse);

            Logger.error(plaintext);

            boolean authenticated = !plaintext.contains("INVALID");
            return authenticated;
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                 | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            System.out.println("An error occurred: " + e.getMessage());
            return false;
        }
    }

    public static void c() {
        /*if(!d()) {
            Logger.error("lolwat!!");
            ScriptManager.getScriptManager().stop();
        }*/
    }
}