/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author arman
 */
public class Server {

    /**
     *
     * @param port
     * @throws FishDBException
     */
    public Server(int port) throws FishDBException {
        FishDB db = new FishDB();
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                (new EndpointHandler(clientSocket, db)).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port: 4444.");
            System.exit(1);
        }
    }

    /**
     *
     * @param args
     * @throws FishDBException
     */
    public static void main(String[] args) throws FishDBException {

        int serverPort = 4444;
        if (args.length > 0) {
            serverPort = Integer.parseInt(args[0]);
        }
        Server server = new Server(serverPort);
    }
}
