/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author arman
 */
public class Endpoint {

    private String id;
    private String ipaddr;
    private String port;

    /**
     *
     * @param id
     * @param ipaddr
     * @param port
     */
    public Endpoint(String id, String ipaddr, String port) {
        this.id = id;
        this.ipaddr = ipaddr;
        this.port = port;
    }

    @Override
    public String toString() {
        return ipaddr + " " + port;
    }

    /**
     *
     * @return
     */
    public String getEndpointStr() {
        return "Endpoint{" + "id=" + id + ", ipaddr=" + ipaddr + ", port=" + port + '}';
    }

}
