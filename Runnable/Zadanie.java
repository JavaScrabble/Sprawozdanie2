import java.util.Random;

/*
    Klasa, której życiowym celem jest iterowanie
 */
public class Zadanie implements Runnable {
    private final int id; // id zadania
    private int iteracja = 0; // liczba wykonanych iteracji
    private int maxIteracje; // ile iteracji musi wykonać wątek
    private int interwal; // ile czasu zajmie wątkowi jedna iteracja

    private Thread thread;

    public Zadanie(int id){
        this.id = id;
        Random rand = new Random();
        this.maxIteracje = rand.nextInt(10);
        this.interwal = rand.nextInt(2000);
        this.thread = new Thread(this);
    }

    public synchronized void start() {
        if (thread.getState() == Thread.State.NEW) {
            thread.start();
        }
    }

    public synchronized boolean anuluj(){
        thread.interrupt();
        System.out.println("Zadanie o id " + this.id + " zostalo anulowane");
        return true;
    }

    public synchronized Thread.State getStan() {
        return thread.getState();
    }

    @Override
    public void run(){
        System.out.println("uruchomiono watek o id: " + this.id);
        try {
            for(int i = 0; i< this.maxIteracje; i++){
                if(Thread.currentThread().isInterrupted()) return;
                System.out.println("watek " + this.id + " pracuje...");
                this.iteracja = i;
                Thread.sleep(interwal);
            }
            System.out.println("Wynik watku o id " + this.id + " - wykonano " + this.maxIteracje + " iteracji w czasie " + this.maxIteracje * this.interwal /1000 + " sekund" );
        } catch(InterruptedException e){
            System.out.println("Zadanie o id " + this.id + " zostalo przerwane. Zdążyło wykonać " + this.iteracja + " iteracji.");
        }
    }
}
