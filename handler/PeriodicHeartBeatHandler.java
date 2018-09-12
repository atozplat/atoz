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

import launch.ServerInfo;
import java.util.Date;
import java.util.HashMap;

public class PeriodicHeartBeatHandler implements Runnable {

    private final HashMap<ServerInfo, Date> serverStatus;
    private int sequenceNumber;
    private final int localPort;

    public PeriodicHeartBeatHandler(HashMap<ServerInfo, Date> serverStatus, int localPort) {
        this.serverStatus = serverStatus;
        this.sequenceNumber = 0;
        this.localPort = localPort;
        
    }

    @Override
    public void run() {
    	String message;
        while(true) {
            // broadcast HeartBeat message to all peers
            message = "hb|" + String.valueOf(localPort) + "|" + String.valueOf(sequenceNumber);

            for (ServerInfo info : serverStatus.keySet()) {
                Thread thread = new Thread(new HeartBeatSenderHandler(info, message));
                thread.start();
            }

            // increment the sequenceNumber
            sequenceNumber += 1;
            
            // sleep for two seconds
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        }
    }
}
