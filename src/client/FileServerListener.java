/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author arman
 */
public class FileServerListener extends Thread {

    private ServerSocket serverSocket = null;
    
    /**
     *
     */
    public FileServerListener() {
        try {
            //select random port
            serverSocket = new ServerSocket(0);
        } catch (IOException ex) {
            Logger.getLogger(FileServerListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     *
     * @return
     */
    public int getPort()
    {
        return serverSocket.getLocalPort();
    }
    
    @Override
    public void run() {
        

        try {
            while (true) {
                Socket peerSocket = serverSocket.accept();
                (new FileServer(peerSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen");
            System.exit(1);
        }
        
    }
}
