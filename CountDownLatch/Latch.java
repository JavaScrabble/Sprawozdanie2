package Lab6;

import java.util.concurrent.CountDownLatch;

public class Latch {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Thread inf = new Thread(() -> {
            try {
                latch.await(); // Czekanie na spełnienie warunku
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // Dalsze zadania
        });

        inf.start();
        Thread.sleep(20000);

        latch.countDown();
    }
}
