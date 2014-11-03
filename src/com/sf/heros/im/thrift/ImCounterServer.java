package com.sf.heros.im.thrift;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;

import com.sf.heros.im.thrift.ImCounter.Iface;
import com.sf.heros.im.thrift.ImCounter.Processor;


public class ImCounterServer {

    private static final Logger logger = Logger.getLogger(ImCounterServer.class);

    public void boot(int port) throws Exception {

        ImCounterIfaceImpl imIface = new ImCounterIfaceImpl();
        Processor<Iface> processor = new ImCounter.Processor<Iface>(imIface);
        TNonblockingServerTransport transport = new TNonblockingServerSocket(
                new InetSocketAddress("0.0.0.0", port));
        TThreadedSelectorServer server = new TThreadedSelectorServer(
                new TThreadedSelectorServer.Args(transport)
                        .processor(processor).selectorThreads(5)
                        .workerThreads(100)
                        .executorService(Executors.newCachedThreadPool()));
        logger.info("im counter serv on 0.0.0.0:" + port + ", args : selectThs(5), workerThs(100)");
        server.serve();
    }

}
