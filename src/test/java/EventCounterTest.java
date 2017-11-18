package test.java;

import main.java.EventCounter;
import main.java.EventCounterImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class EventCounterTest {
    private static ExecutorService service;
    private static Runnable action;
    private static EventCounter eventCounter;
    private static CyclicBarrier barrier;

    @BeforeAll
    static void init() {
        service = Executors.newFixedThreadPool(10);
        eventCounter = EventCounterImpl.getInstance();
        barrier = new CyclicBarrier(10);
        action = () -> {
            try {
                barrier.await();
                eventCounter.stateEvent();
                Thread.sleep(300);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    @Test
    void checkEventCountForLastMinuteImitatingThreadWorking() throws ExecutionException, InterruptedException {
        List<Future> futures = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            futures.add(service.submit(action));
            if (i == 50) Thread.sleep(60 * 1000);
        }

        for (Future future : futures) {
            future.get();
        }

        assertTrue(eventCounter.getEventCountForLastMinute().intValue() < 100);
    }
}
