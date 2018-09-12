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

import block.Blockchain;
import java.security.SecureRandom;

public class PeriodicCommitHandler implements Runnable{

    private volatile boolean isRunning;
    private int nonce;
    private Blockchain blockchain;
    private SecureRandom randomGenerator;

    public PeriodicCommitHandler(Blockchain blockchain) {
        isRunning = true;
        this.blockchain = blockchain;
        randomGenerator = new SecureRandom();
        nonce = randomGenerator.nextInt();
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public boolean getRunning() {
        return isRunning;
    }

    @Override
    public void run() {
        while (isRunning) {
            blockchain.commit(nonce);
            nonce = randomGenerator.nextInt();
        }
    }
}
