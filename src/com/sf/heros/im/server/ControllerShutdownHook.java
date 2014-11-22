package com.sf.heros.im.server;

import java.util.Collection;

import com.sf.heros.im.req.controller.CommonController;

public class ControllerShutdownHook implements Runnable {

    @Override
    public void run() {
        Collection<CommonController> controllers = CommonController.getAll();
        for (CommonController controller : controllers) {
            if (controller != null) {
                controller.release();
            }
        }
    }

}
