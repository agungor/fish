package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author arman
 */
public class EndpointHandler extends Thread {

    private Socket clientSocket;
    private FishDB fishdb;
    private int endPointID;
    private BufferedReader in;
    private PrintWriter out;
    private static final String DEFAULT_ENDPOINT_PORT = "6000";
    private static final int FILENAME = 0;
    private static final int OWNERID = 1;
    private static Map<String, Endpoint> endPointMap = new HashMap<>();
    private static int endPointIDCounter = 0;

    /**
     *
     * @param clientSocket
     * @param fishdb
     */
    public EndpointHandler(Socket clientSocket, FishDB fishdb) {

        this.clientSocket = clientSocket;
        this.fishdb = fishdb;
        this.endPointID = endPointIDCounter++;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(EndpointHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addFileList(String filelist) {
        List<String> filenames = Arrays.asList(filelist.split("\\;"));
        if (filenames.size() > 1) {
            for (int i = 1; i < filenames.size(); i++) {
                try {
                    fishdb.addFile(filenames.get(i), Integer.toString(endPointID));
                } catch (SQLException ex) {
                    Logger.getLogger(EndpointHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void listClients() {
        System.out.println("----------client list----------");
        for (String key : endPointMap.keySet()) {
            System.out.println(endPointMap.get(key).getEndpointStr());
        }
        System.out.println("-------end of client list------");
    }

    private void sendToClient(String message) {
        out.write(message);
        out.flush();
    }

    private void addEndpoint(String message) {
        String id = Integer.toString(endPointID);
        String ip = clientSocket.getInetAddress().getHostAddress();
        String port = DEFAULT_ENDPOINT_PORT;
        String[] parts = message.split(" ");
        if (parts.length > 1) {
            port = parts[1];
        }

        endPointMap.put(id, new Endpoint(id, ip, port));
        listClients();
    }

    private void searchFile(String message) {
        String[] parts = message.split(" ");
        if (parts.length > 1) {
            try {
                ArrayList<String[]> results = fishdb.searchFile(parts);
                if (results.isEmpty()) {
                    System.out.println(message + "not found");
                    sendToClient("not found\n");
                } else {
                    String response = "";
                    for (int i = 0; i < results.size(); i++) {
                        if (endPointMap.containsKey(results.get(i)[OWNERID])) {
                            response += results.get(i)[FILENAME];
                            response += " ";
                            response += endPointMap.get(results.get(i)[OWNERID]);
                            response += ";";
                        } else {
                            System.out.println("File Owner Not Found");
                        }

                    }
                    response += "\n";
                    sendToClient(response);
                    System.out.println("search result: \n" + response);
                }
            } catch (SQLException ex) {
                Logger.getLogger(EndpointHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void removeFiles() {
        try {
            fishdb.deleteFiles(Integer.toString(endPointID));
        } catch (SQLException ex) {
            Logger.getLogger(EndpointHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void removeEndpoint() {
        removeFiles();
        endPointMap.remove(Integer.toString(endPointID));
    }

    public void run() {

        try {
            String rcvdStr;
            while ((rcvdStr = in.readLine()) != null) {

                System.out.println("received:" + rcvdStr);

                //register;portno
                if (rcvdStr.startsWith("register")) {
                    addEndpoint(rcvdStr);
                } else if (rcvdStr.startsWith("share")) {
                    addFileList(rcvdStr);
                } else if (rcvdStr.startsWith("search")) {
                    searchFile(rcvdStr);
                } else if (rcvdStr.startsWith("unregister")) {
                    removeEndpoint();
                } else if (rcvdStr.startsWith("unshare")) {
                    removeFiles();
                }
            }
        } catch (IOException ex) {
            System.out.println("client " + endPointID + " disconnected:");
            removeEndpoint();
        }
    }
}
