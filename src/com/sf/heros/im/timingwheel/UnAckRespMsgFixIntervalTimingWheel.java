package com.sf.heros.im.timingwheel;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.redis.RedisConnException;
import com.sf.heros.im.common.redis.RedisManagerV2;
import com.sf.heros.im.service.RespMsgService;
import com.sf.heros.im.service.UnAckRespMsgService;
import com.sf.heros.im.timingwheel.service.IndicatorService;
import com.sf.heros.im.timingwheel.service.SlotKeyService;
import com.sf.heros.im.timingwheel.service.WheelService;

/**
 * A timing-wheel optimized for approximated I/O timeout scheduling.<br>
 * {@link UnAckRespMsgFixIntervalTimingWheel} creates a new thread whenever it is instantiated and started, so don't create many instances.
 * <p>
 * <b>The classic usage as follows:</b><br>
 * <li>using timing-wheel manage any object timeout</li>
 * <pre>
 *    // Create a timing-wheel with 60 ticks, and every tick is 1 second.
 *    private static final TimingWheel<CometChannel> TIMING_WHEEL = new TimingWheel<CometChannel>(1, 60, TimeUnit.SECONDS);
 *
 *    // Add expiration listener and start the timing-wheel.
 *    static {
 *    	TIMING_WHEEL.addExpirationListener(new YourExpirationListener());
 *    	TIMING_WHEEL.start();
 *    }
 *
 *    // Add one element to be timeout approximated after 60 seconds
 *    TIMING_WHEEL.add(e);
 *
 *    // Anytime you can cancel count down timer for element e like this
 *    TIMING_WHEEL.remove(e);
 * </pre>
 *
 * After expiration occurs, the {@link ExpirationListener} interface will be invoked and the expired object will be
 * the argument for callback method {@link ExpirationListener#expired(Object)}
 * <p>
 * {@link UnAckRespMsgFixIntervalTimingWheel} is based on <a href="http://cseweb.ucsd.edu/users/varghese/">George Varghese</a> and Tony Lauck's paper,
 * <a href="http://cseweb.ucsd.edu/users/varghese/PAPERS/twheel.ps.Z">'Hashed and Hierarchical Timing Wheels: data structures
 * to efficiently implement a timer facility'</a>.  More comprehensive slides are located <a href="http://www.cse.wustl.edu/~cdgill/courses/cs6874/TimingWheels.ppt">here</a>.
 *
 * @author mindwind
 * @version 1.0, Sep 20, 2012
 */
public class UnAckRespMsgFixIntervalTimingWheel {

    private static final Logger logger = Logger.getLogger(UnAckRespMsgFixIntervalTimingWheel.class);

    private final long tickDuration;
    private final int ticksPerWheel;
    private volatile int currentTickIndex = 0;

    private WheelService wheel;
    private IndicatorService indicator;
    private UnAckRespMsgService unAckRespMsgService;
    private RespMsgService respMsgService;

    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Executor expirationExecutor;
    private Thread workerThread;

    private String wheelName;



    // ~ -------------------------------------------------------------------------------------------------------------

    public long getTickDuration() {
        return tickDuration;
    }

    public WheelService getWheel() {
        return wheel;
    }

    /**
     * Construct a timing wheel.
     *
     * @param tickDuration
     *            tick duration with specified time unit.
     * @param ticksPerWheel
     * @param timeUnit
     * @param wheelName a name for this wheel instance
     */
    public UnAckRespMsgFixIntervalTimingWheel(int tickDuration, int ticksPerWheel, TimeUnit timeUnit, String wheelName, WheelService wheel, IndicatorService indicator, SlotKeyService slotKeyService, RespMsgService respMsgService) {
        if (timeUnit == null) {
            throw new NullPointerException("unit");
        }
        if (tickDuration <= 0) {
            throw new IllegalArgumentException("tickDuration must be greater than 0: " + tickDuration);
        }
        if (ticksPerWheel <= 0) {
            throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel);
        }

        this.wheel = wheel;
        this.indicator = indicator;
        this.respMsgService = respMsgService;
        this.tickDuration = TimeUnit.MILLISECONDS.convert(tickDuration, timeUnit);
        this.ticksPerWheel = ticksPerWheel + 1;
        this.expirationExecutor = Executors.newCachedThreadPool();
        for (int i = 0; i < this.ticksPerWheel; i++) {
            wheel.add(new Slot(i, slotKeyService.geneKey(i)){

                private RedisManagerV2 rm = RedisManagerV2.getInstance();

                @Override
                public void add(String e) {
                    rm.sadd(true, this.getKey(), e);
                }

                @Override
                public boolean remove(String e) {
                    return rm.srem(true, this.getKey(), e);
                }

                @Override
                public Set<String> elements() {
                    try {
                        return rm.smembers(true, this.getKey());
                    } catch (RedisConnException e) {
                        return null;
                    }
                }

                });
        }

        if (wheelName == null) {
            wheelName = "FixInterval-Timing-Wheel";
        }

        this.wheelName = wheelName;

        workerThread = new Thread(new TickWorker(), this.wheelName);
    }

    // ~ -------------------------------------------------------------------------------------------------------------

    public void setUnAckRespMsgService(UnAckRespMsgService unAckRespMsgService) {
        this.unAckRespMsgService = unAckRespMsgService;
    }

    public void start() {
        if (shutdown.get()) {
            throw new IllegalStateException("Cannot be started once stopped");
        }

        if (!workerThread.isAlive()) {
            workerThread.start();
            logger.info(wheelName + " is running");
        }
    }

    public boolean stop() {
        if (!shutdown.compareAndSet(false, true)) {
            return false;
        }

        boolean interrupted = false;
        while (workerThread.isAlive()) {
            workerThread.interrupt();
            try {
                workerThread.join(100);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        return true;
    }

    /**
     * Add a element to {@link UnAckRespMsgFixIntervalTimingWheel} and start to count down its life-time.
     *
     * @param e
     * @param a appoint listener, when e is expired, a thread will be start, appointExpirationListener.expired(e) will be invoked.
     * @return remain time to be expired in millisecond.
     */
    public long add(String e) {
        synchronized(e) {
            checkAdd(e);
            int previousTickIndex = getPreviousTickIndex();
            Slot slot = wheel.get(previousTickIndex);
            slot.add(e);
            indicator.put(e, slot);
            return (ticksPerWheel - 1) * tickDuration;
        }
    }

    private void checkAdd(String e) {
        Integer idx = indicator.get(e);
        if (idx != null) {
            Slot slot = wheel.get(idx);
            if (slot != null) {
                slot.remove(e);
            }
        }
    }


    public boolean exist(String e) {
        return indicator.get(e) != null;
    }

    private int getPreviousTickIndex() {
        lock.readLock().lock();
        try {
            int cti = currentTickIndex;
            if (cti == 0) {
                return ticksPerWheel - 1;
            }

            return cti - 1;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Removes the specified element from timing wheel.
     *
     * @param e
     * @return <tt>true</tt> if this timing wheel contained the specified
     *         element
     */
    public boolean remove(String e) {
        synchronized (e) {
            Integer idx = indicator.get(e);
            Slot slot = wheel.get(idx);
            if (slot == null) {
                return true;
            }

            indicator.remove(e);
            return slot.remove(e);
        }
    }

    private void notifyExpired(int idx) {
        final Slot slot = wheel.get(idx);
        if (slot == null) {
            return;
        }
        Set<String> elements = slot.elements();
        if (elements == null) {
            return;
        }
        for (final String e : elements) {
            slot.remove(e);
            synchronized (e) {
                Integer idx2 = indicator.get(e);
                Slot latestSlot = wheel.get(idx2);
                if (slot.equals(latestSlot)) {
                    indicator.remove(e);
                }
            }
            expirationExecutor.execute(new Runnable() {
                private AppointExpirationListener expirationListener = new AppointExpirationListener() {

                    @Override
                    public void expired(String expiredObject) {
                        if (expiredObject != null && UnAckRespMsgFixIntervalTimingWheel.this.unAckRespMsgService != null) {
                            while (!UnAckRespMsgFixIntervalTimingWheel.this.unAckRespMsgService.pushToQueue(expiredObject)) {
                                try {
                                    TimeUnit.SECONDS.sleep(1);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                };
                @Override
                public void run() {
                    expirationListener.expired(UnAckRespMsgFixIntervalTimingWheel.this.respMsgService.getUnAck(e));
                }
            });
        }
    }

    // ~ -------------------------------------------------------------------------------------------------------------

    private class TickWorker implements Runnable {

        private long startTime;
        private long tick;

        @Override
        public void run() {
            startTime = System.currentTimeMillis();
            tick = 1;

            for (int i = 0; !shutdown.get(); i++) {
                if (i == wheel.size()) {
                    i = 0;
                }
                lock.writeLock().lock();
                try {
                    currentTickIndex = i;
                } finally {
                    lock.writeLock().unlock();
                }
                notifyExpired(currentTickIndex);
                waitForNextTick();
            }
        }

        private void waitForNextTick() {
            for (;;) {
                long currentTime = System.currentTimeMillis();
                long sleepTime = tickDuration * tick - (currentTime - startTime);

                if (sleepTime <= 0) {
                    break;
                }

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    return;
                }
            }
            tick++;
        }
    }
}
