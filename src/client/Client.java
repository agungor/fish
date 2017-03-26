/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author arman
 */
public class Client {

    static String sharedFolder;
    String serverAddress, serverPort, fileServerPort;
    Socket clientSocket;
    PrintWriter wr;
    BufferedReader rd;
    BufferedReader consoleIn;

    static enum CommandName {
        search, quit, help, list;
    };

    /**
     *
     * @param sharedFolder
     * @param serverAddress
     * @param serverPort
     */
    public Client(String sharedFolder, String serverAddress, String serverPort) {
        FileServerListener fs = new FileServerListener();
        fs.start();
        this.sharedFolder = sharedFolder;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.fileServerPort = Integer.toString(fs.getPort());
    }

    /**
     *
     * @return
     */
    public static String getSharedfolder() {
        return sharedFolder;
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public boolean connect() throws IOException {
        try {
            clientSocket = new Socket(serverAddress, Integer.parseInt(serverPort));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + serverAddress + ".");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection to: " + serverAddress + "");
            System.exit(1);
        }
        wr = new PrintWriter(clientSocket.getOutputStream());
        rd = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        //clientSocket.close();
        return true;
    }

    /**
     *
     * @param message
     */
    public void sendToServer(String message) {
        wr.write(message);
        wr.flush();
    }

    /**
     *
     * @return
     */
    public String recvFromServer() {

        String rcvdStr = "";
        try {
            rcvdStr = rd.readLine();
            System.out.println(rcvdStr);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rcvdStr;
    }

    /**
     *
     */
    public void share() {
        String register = "register " + fileServerPort + "\n";
        System.out.println(register);
        sendToServer(register);

        ArrayList<File> results = getFiles(sharedFolder);
        String share = "share";
        for (int i = 0; i < results.size(); i++) {
            share += ";" + results.get(i).getName();

        }
        share += "\n";
        System.out.println(share);
        sendToServer(share);
    }

    /**
     *
     */
    public void unshare() {
        sendToServer("unregister\n");
    }

    /**
     *
     * @param keywords
     */
    public void searchFiles(String keywords) {
        sendToServer("search " + keywords + "\n");
        String result = recvFromServer();
        String[] rows = result.split(";");
        if (!rows[0].contains("not found")) {
            System.out.println("Please Select a File:");
            for (int i = 0; i < rows.length; i++) {
                String paddedIndex = String.format("%3d", i).replace(' ', '0');
                System.out.println("[" + paddedIndex + "] " + rows[i]);
            }
            System.out.println("[C] CANCEL");

            int choice = -1;
            try {
                choice = Integer.parseInt(consoleIn.readLine());
            } catch (NumberFormatException e) {
                return;
            } catch (IOException ex) {
                return;
            }
            System.out.println("choice: " + choice);
            //example "20002686.pdf 127.0.0.1 6000"
            if (choice < rows.length) {
                String[] fileParams = rows[choice].split(" ");
                String filename = fileParams[0];
                String address = fileParams[1];
                String port = fileParams[2];
                FileReceiver fr = new FileReceiver(filename, address, port);
                fr.start();
            }

        }

    }

    /**
     *
     */
    public void receiveCommands() {
        consoleIn = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("client" + "@" + "Fish" + ">");

            String userInput;
            try {
                userInput = consoleIn.readLine();
                execute(parse(userInput));
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private Command parse(String userInput) {
        if (userInput == null) {
            return null;
        }

        StringTokenizer tokenizer = new StringTokenizer(userInput);
        if (tokenizer.countTokens() == 0) {
            return null;
        }

        CommandName commandName = null;
        String arguments = "";

        if (tokenizer.hasMoreTokens()) {
            try {
                String commandNameString = tokenizer.nextToken();
                commandName = CommandName.valueOf(CommandName.class, commandNameString);
            } catch (IllegalArgumentException commandDoesNotExist) {
                System.out.println("Illegal command");
                return null;
            }
        }

        while (tokenizer.hasMoreTokens()) {

            arguments += tokenizer.nextToken() + " ";

        }
        arguments = arguments.trim();
        return new Command(commandName, arguments);
    }

    void execute(Command command) throws RemoteException {
        if (command == null) {
            return;
        }

        switch (command.getCommandName()) {
            case search:
                searchFiles(command.getArguments());
            case list:
                return;
            case quit:
                unshare();
                System.exit(0);
            case help:
                for (CommandName commandName : CommandName.values()) {
                    System.out.println(commandName);
                }
                return;

        }
    }

    private class Command {

        private CommandName commandName;
        private String arguments;

        private CommandName getCommandName() {
            return commandName;
        }

        private Command(Client.CommandName commandName, String arguments) {
            this.commandName = commandName;
            this.arguments = arguments;
        }

        public String getArguments() {
            return arguments;
        }
    }

    /**
     *
     * @param path
     * @return
     */
    public static ArrayList<File> getFiles(String path) {

        ArrayList<File> results = new ArrayList<File>();

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                //System.out.println("File " + listOfFiles[i].getName());
                results.add(listOfFiles[i]);
            } else if (listOfFiles[i].isDirectory()) {
                //System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        return results;
    }

    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        String filepath, address, port;
        if (args.length > 2) {
            filepath = args[0];
            //IP Address of the server:
            address = args[1];
            //Port of the server:
            port = args[2];
        } else {
            filepath = "C:\\fish\\Shared";
            address = "localhost";
            port = "4444";
        }
        Client client = new Client(filepath, address, port);
        client.connect();
        client.share();
        client.receiveCommands();
    }
}
