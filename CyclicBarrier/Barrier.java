package Lab6;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Barrier {
    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        CyclicBarrier barrier = new CyclicBarrier(2);

        Thread inf = new Thread(() -> {
            try {
                barrier.await(); // Czekanie na spełnienie warunku
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
            // Dalsze zadania
        });

        inf.start();
        Thread.sleep(20000);

        barrier.await(); // Ogłoszenie przez główny wątek ukończenia zadania
    }
}
