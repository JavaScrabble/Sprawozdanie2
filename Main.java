import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

class Main {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<Long>> tasks = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Dodaj zadanie (oblicz Fibonacciego)");
            System.out.println("2. Pokaż status zadań");
            System.out.println("3. Pokaż wyniki zadań");
            System.out.println("4. Anuluj zadanie");
            System.out.println("5. Wyjdź");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Podaj n do Fibonacciego: ");
                    int n = Integer.parseInt(scanner.nextLine());
                    FutureFib fib = new FutureFib(new FibonacciTaskManager.FibonacciTask(n, tasks.size()), tasks.size());
                    executor.execute(fib);
                    tasks.add(fib);
                    System.out.println("Zadanie dodane (index: " + (tasks.size() - 1) + ")");
                    break;

                case "2":
                    for (int i = 0; i < tasks.size(); i++) {
                        Future<Long> f = tasks.get(i);
                        if (f.isCancelled()) {
                            System.out.println("Zadanie " + i + ": ANULOWANE");
                        } else if (f.isDone()) {
                            System.out.println("Zadanie " + i + ": ZAKOŃCZONE");
                        } else {
                            System.out.println("Zadanie " + i + ": W TOKU");
                        }
                    }
                    break;

                case "3":
                    for (int i = 0; i < tasks.size(); i++) {
                        Future<Long> f = tasks.get(i);
                        if (f.isDone() && !f.isCancelled()) {
                            try {
                                System.out.println("Zadanie " + i + ": wynik = " + f.get());
                            } catch (Exception e) {
                                System.out.println("Zadanie " + i + ": błąd podczas pobierania wyniku.");
                            }
                        }
                    }
                    break;

                case "4":
                    System.out.print("Podaj index zadania do anulowania: ");
                    int idx = Integer.parseInt(scanner.nextLine());
                    if (idx >= 0 && idx < tasks.size()) {
                        boolean cancelled = tasks.get(idx).cancel(true);
                        if (cancelled) {
                            System.out.println("Zadanie anulowane.");
                        } else {
                            System.out.println("Nie udało się anulować zadania.");
                        }
                    } else {
                        System.out.println("Niepoprawny index.");
                    }
                    break;

                case "5":
                    executor.shutdownNow();
                    System.out.println("Zamykanie programu.");
                    return;

                default:
                    System.out.println("Nieznana opcja.");
            }
        }
    }
}