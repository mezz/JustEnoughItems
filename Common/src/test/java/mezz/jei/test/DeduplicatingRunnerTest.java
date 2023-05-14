package mezz.jei.test;

import mezz.jei.common.util.DeduplicatingRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class DeduplicatingRunnerTest {
    /**
     * The de-duplicating runner should only run one time.
     * Spamming runs should be ignored if they are within the given duration.
     */
    @Test
    public void testDeduplicatedRuns() throws InterruptedException {
        AtomicInteger runs = new AtomicInteger();
        Runnable testRunnable = runs::getAndIncrement;
        Duration delay = Duration.ofMillis(10);
        DeduplicatingRunner deduplicatingRunner = new DeduplicatingRunner(testRunnable, delay, "test");
        for (int i = 0; i < 10; i++) {
            deduplicatingRunner.run();
        }
        Assertions.assertEquals(0, runs.get());
        Thread.sleep(2 * delay.toMillis());
        Assertions.assertEquals(1, runs.get());
    }

    /**
     * The de-duplicating runner should allow multiple runs if they are spaced out more than
     * the given duration.
     */
    @Test
    public void testSpacedOutRuns() throws InterruptedException {
        AtomicInteger runs = new AtomicInteger();
        Runnable testRunnable = runs::getAndIncrement;
        Duration delay = Duration.ofMillis(1);
        DeduplicatingRunner deduplicatingRunner = new DeduplicatingRunner(testRunnable, delay, "test");
        for (int i = 0; i < 10; i++) {
            deduplicatingRunner.run();
            Thread.sleep(2 * delay.toMillis());
            Assertions.assertEquals(i + 1, runs.get());
        }
    }
}
