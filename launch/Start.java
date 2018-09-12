/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch;

/**
 *
 * @author 김승현
 */

import handler.PeriodicCatchupHandler;
import handler.PeriodicHeartBeatHandler;
import handler.PeriodicCommitHandler;
import block.Blockchain;
import constants.ServerConstants;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Start {

    public static AtomicInteger CompletedLoadingThreads = new AtomicInteger(0);
    public static final Start instance = new Start();
    public static int threadCount = 0;

    public void run() throws IOException {
        Blockchain blockchain = new Blockchain();

        final HashMap<ServerInfo, Date> serverStatus = new HashMap<>();
        serverStatus.put(new ServerInfo(ServerConstants.remoteHost, ServerConstants.remotePort), new Date());

        final PeriodicCommitHandler pcr = new PeriodicCommitHandler(blockchain);
        final List<LoadingThread> threads = new ArrayList<>();

        threads.add(new LoadingThread(new Runnable() {
            public void run() {
                pcr.run();
            }
        }, "PeriodicCommit", this));

        threads.add(new LoadingThread(new Runnable() {
            public void run() {
                new PeriodicHeartBeatHandler(serverStatus, ServerConstants.localPort).run();
            }
        }, "PeriodicHeartBeat", this));

        threads.add(new LoadingThread(new Runnable() {
            public void run() {
                new PeriodicCatchupHandler(blockchain, serverStatus, ServerConstants.localPort).run();
            }
        }, "PeriodicCatchup", this));

        ServerSocket serverSocket = new ServerSocket(ServerConstants.localPort);
        threads.add(new LoadingThread(new Runnable() {
            public void run() {
                while (true) {
                    Socket clientSocket;
                    try {
                        clientSocket = serverSocket.accept();
                        new Thread(new handler.BlockchainServerHandler(clientSocket, blockchain, serverStatus, ServerConstants.localPort)).start();
                    } catch (IOException ex) {
                    }
                }
            }
        }, "BlockchainServer", this));
        threads.add(new LoadingThread(new Runnable() {
            public void run() {
                new Command().start();
            }
        }, "Command", this));
        Start.threadCount = threads.size();
        for (Thread t : threads) {
            t.start();
        }
    }

    private static class Command extends Thread {

        @Override
        public void run() {
            System.out.println("Command List [debug]");
            while (true) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String command = null;
                try {
                    command = reader.readLine();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if (command != null) {
                    System.out.println("Error Command Null Point");
                    continue;
                }
                String[] split = command.split(" ");
                switch (split[0]) {
                    case "debug":
                        try {

                        } catch (Exception ex) {

                        }
                        break;
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        instance.run();
    }

    private static class LoadingThread extends Thread {

        protected String LoadingThreadName;

        private LoadingThread(Runnable r, String t, Object o) {
            super(new NotifyingRunnable(r, o, t));
            LoadingThreadName = t;
        }

        @Override
        public synchronized void start() {
            System.out.println("[Loading...] Started " + LoadingThreadName + " Thread");
            super.start();
        }
    }

    private static class NotifyingRunnable implements Runnable {

        private String LoadingThreadName;
        private long StartTime;
        private Runnable WrappedRunnable;
        private final Object ToNotify;

        private NotifyingRunnable(Runnable r, Object o, String name) {
            WrappedRunnable = r;
            ToNotify = o;
            LoadingThreadName = name;
        }

        public void run() {
            StartTime = System.currentTimeMillis();
            new Thread(WrappedRunnable).start();
            System.out.println("[Loading Completed] " + LoadingThreadName + " | Completed in " + (System.currentTimeMillis() - StartTime) + " Milliseconds. (" + (CompletedLoadingThreads.get() + 1) + "/" + Start.threadCount + ")");
            synchronized (ToNotify) {
                CompletedLoadingThreads.incrementAndGet();
                ToNotify.notify();
            }
        }
    }
}
