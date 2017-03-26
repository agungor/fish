/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author arman
 */
public class FileServer extends Thread {

    private Socket peerSocket;
    private BufferedReader in;
    private ObjectOutputStream out;
    private String sharedfolder = Client.getSharedfolder();

    /**
     *
     * @param peerSocket
     */
    public FileServer(Socket peerSocket) {
        this.peerSocket = peerSocket;
        try {
            in = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
            out = new ObjectOutputStream(peerSocket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        String filename;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;
        try {
            filename = in.readLine();
            System.out.println("requestedFile:" + filename);
            File file = new File(sharedfolder + "\\" + filename);
            byte[] bytearray = new byte[(int) file.length()];
            bytearray[0]=0;
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            bis.read(bytearray, 0, bytearray.length);
            os = peerSocket.getOutputStream();
            System.out.println("Sending " + filename + "(" + bytearray.length + " bytes)");
            
            os.write(bytearray, 0, bytearray.length);
            os.flush();
            System.out.println("File " + filename + " sent!");

        } catch (IOException ex) {
            Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ex) {
                    Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                    Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (peerSocket != null) {
                try {
                    peerSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
