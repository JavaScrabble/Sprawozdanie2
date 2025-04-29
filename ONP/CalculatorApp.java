import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.*;

public class CalculatorApp {
    private static final Lock lock = new ReentrantLock();
    private static final String FILE_PATH = "rownania.txt";
    private static final ExecutorService readerExecutor = Executors.newFixedThreadPool(2);
    private static final ExecutorService calculatorExecutor = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {
        List<Future<String>> equations = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            AtomicBoolean moreLines = new AtomicBoolean(true);

            while (moreLines.get()) {
                Future<String> readFuture = readerExecutor.submit(() -> {
                    String line;
                    lock.lock();
                    try {
                        line = br.readLine();
                        if (line != null) {
                            System.out.println("Wczytano linie: " + line);
                            return line;
                        }
                        moreLines.set(false);
                        return null;
                    } catch (IOException e) {
                        moreLines.set(false);
                        //e.printStackTrace();
                        return null;
                    } finally {
                        lock.unlock();
                    }
                });
                if (readFuture.get() != null)
                    equations.add(readFuture);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (Future<String> future : equations) {
            try {
                String equation = future.get();
                CalculationTask task = new CalculationTask(equation);
                CalculationFutureTask futureTask = new CalculationFutureTask(task);
                calculatorExecutor.submit(futureTask);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        readerExecutor.shutdown();
        calculatorExecutor.shutdown();
    }



    static class CalculationTask implements Callable<String> {
        private final String equation;

        CalculationTask(String equation) {
            this.equation = equation;
        }

        @Override
        public String call() {
            String cleanEquation = equation.replace("=", "").trim();
            List<String> postfix = toPostfix(cleanEquation);
            double result = evaluatePostfix(postfix);
            return equation + " " + result;
        }

        private List<String> toPostfix(String infix) {
            List<String> output = new ArrayList<>();
            Stack<String> stack = new Stack<>();
            StringTokenizer tokenizer = new StringTokenizer(infix, "+-*/^() ", true);

            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().trim();
                if (token.isEmpty()) continue;

                if (isNumeric(token)) {
                    output.add(token);
                } else if (token.equals("(")) {
                    stack.push(token);
                } else if (token.equals(")")) {
                    while (!stack.isEmpty() && !stack.peek().equals("(")) {
                        output.add(stack.pop());
                    }
                    stack.pop();
                } else {
                    while (!stack.isEmpty() && precedence(stack.peek()) >= precedence(token)) {
                        output.add(stack.pop());
                    }
                    stack.push(token);
                }
            }

            while (!stack.isEmpty()) {
                output.add(stack.pop());
            }

            return output;
        }

        private double evaluatePostfix(List<String> postfix) {
            Stack<Double> stack = new Stack<>();
            for (String token : postfix) {
                if (isNumeric(token)) {
                    stack.push(Double.parseDouble(token));
                } else {
                    double b = stack.pop();
                    double a = stack.pop();
                    switch (token) {
                        case "+": stack.push(a + b); break;
                        case "-": stack.push(a - b); break;
                        case "*": stack.push(a * b); break;
                        case "/": stack.push(a / b); break;
                        case "^": stack.push(Math.pow(a, b)); break;
                    }
                }
            }
            return stack.pop();
        }

        private boolean isNumeric(String s) {
            try {
                Double.parseDouble(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        private int precedence(String op) {
            return switch (op) {
                case "+", "-" -> 1;
                case "*", "/" -> 2;
                case "^" -> 3;
                default -> 0;
            };
        }
    }

    static class CalculationFutureTask extends FutureTask<String> {
        private final CalculationTask task;

        public CalculationFutureTask(CalculationTask callable) {
            super(callable);
            this.task = callable;
        }

        @Override
        protected void done() {
            try {
                String resultLine = get();

                lock.lock();
                try {
                    List<String> lines = Files.readAllLines(Paths.get(FILE_PATH));
                    for (int i = 0; i < lines.size(); i++) {
                        if (lines.get(i).equals(task.equation)) {
                            lines.set(i, resultLine);
                            System.out.println("Wynik równania " + i + ": " + resultLine);
                            break;
                        }
                    }
                    Files.write(Paths.get(FILE_PATH), lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                } finally {
                    lock.unlock();
                }

            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}