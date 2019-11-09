import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class X {

    static int transform(int number) {
        System.out.println("transforming " + number + " " + Thread.currentThread());

        return number;
    }

    // check on parallel stream where are executed
    // it doesn't really matter where you create the stream but where you invoke terminal operation
    static void process(Stream<Integer> stream) throws InterruptedException {
        ForkJoinPool fjp = new ForkJoinPool(50);

        fjp.submit(() -> stream.forEach(e -> {}));

        fjp.shutdown();

        fjp.awaitTermination(30, TimeUnit.SECONDS);
    }
}
