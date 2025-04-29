import java.util.concurrent.Callable;

class FibonacciTask implements Callable<Long> {
    private final int n;
    private final int id;

    public FibonacciTask(int n, int id) {
        this.n = n;
        this.id = id;
    }

    @Override
    public String toString() {
        return "FibonacciTaskManager [id = " + Integer.toString(id) + ", n = " + Integer.toString(n)
                + "] ";
    }

    @Override
    public Long call() {
        if(this.n < 0) return null;

        if (this.n <= 1)
            return (long) this.n;

        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            if (Thread.currentThread().isInterrupted()) {
                System.out.println(this + " przerwane");
                return null;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                System.out.println(this + " sleep() przerwane");
                return null;
            }
            long temp = a + b;
            a = b;
            b = temp;
        }
        System.out.println(this + " -> Wynik końcowy " + b);
        return b;
    }
}

