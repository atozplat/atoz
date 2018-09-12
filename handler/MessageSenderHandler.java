/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package handler;

/**
 *
 * @author 김승현
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import launch.ServerInfo;

public class MessageSenderHandler implements Runnable {

    private ServerInfo destServer;
    private String message;

    public MessageSenderHandler(ServerInfo destServer, String message) {
        this.destServer = destServer;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(this.destServer.getHost(), this.destServer.getPort()), 10000);
            PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
            System.out.println("sending " + message);
            pw.println(message);
            pw.flush();
            pw.close();
            s.close();
        } catch (IOException e) {

        }
    }
}
