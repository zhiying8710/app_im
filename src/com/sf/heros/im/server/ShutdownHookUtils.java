package com.sf.heros.im.server;

import java.util.concurrent.ExecutorService;

public class ShutdownHookUtils {

    public static void shutdownExecutor(ExecutorService executor) {

        Runtime.getRuntime().addShutdownHook(new Thread(new ExecutorShutdownHook(executor)));

    }

    public static void shutdownControllers() {

        Runtime.getRuntime().addShutdownHook(new Thread(new ControllerShutdownHook()));

    }

}
