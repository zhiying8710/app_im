package com.sf.heros.im.server;

import java.util.concurrent.ExecutorService;

public class ExecutorShutdownHook implements Runnable {

    private ExecutorService executor;

    public ExecutorShutdownHook(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void run() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

}
