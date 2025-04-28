import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

class FutureFib extends FutureTask<Long> {
    private final int id;

    public FutureFib(Callable<Long> callable, int id) {
        super(callable);
        this.id = id;
    }

    @Override
    protected void done() {
        if(isCancelled())
            System.out.printf("Zadanie %d anulowane!%n", id);
        else if(isDone())
            try{
                System.out.printf("Zadanie %d ukończone! Wynik: %d%n", id, get());
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
    }
}
