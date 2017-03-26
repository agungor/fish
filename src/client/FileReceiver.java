/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author arman
 */
public class FileReceiver extends Thread {

    private static int FILE_SIZE = 104857600; //100MB
    InetAddress address;
    int port;
    String filename;

    /**
     *
     * @param filename
     * @param address
     * @param port
     */
    public FileReceiver(String filename, String address, String port) {
        try {
            this.address = InetAddress.getByName(address);
            this.port = Integer.parseInt(port);
            this.filename = filename;
        } catch (UnknownHostException ex) {
            Logger.getLogger(FileReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        int bytesRead;
        int current = 0;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        Socket socket = null;
        try {
            System.out.println("Connecting " + address.toString() + ":" + port);
            System.out.println("client@Fish>");
            
            socket = new Socket(address, port);           
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.write(filename+"\n");
            out.flush();

            // receive file
            byte[] bytearray = new byte[FILE_SIZE];
            InputStream is = socket.getInputStream();
            fos = new FileOutputStream("C:\\fish\\Downloads\\" + filename);
            
            bos = new BufferedOutputStream(fos);
            bytesRead = is.read(bytearray, 0, bytearray.length);
            current = bytesRead;

            bos.write(bytearray, 4, current);
            bos.flush();
            System.out.println("File " + "C:\\fish\\Downloads\\" + filename
                    + " downloaded (" + (current-4) + " bytes read)");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileReceiver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileReceiver.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    Logger.getLogger(FileReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException ex) {
                    Logger.getLogger(FileReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    Logger.getLogger(FileReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
