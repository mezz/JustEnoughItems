package mezz.jei.library.startup;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class ClientTaskExecutor {
    private final InternalExecutor executor = new InternalExecutor();

    public void tick() {
        executor.tick();
    }

    public void runAsync(Stream<Runnable> runnables) {
        Stream<CompletableFuture<Void>> futures = runnables.map(r -> CompletableFuture.runAsync(r, executor));
        CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        join(future);
    }

    public void runAsync(Runnable runnable) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(runnable, executor);
        join(future);
    }

    public void runAsync(Supplier<CompletableFuture<Void>> supplier) {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(supplier, executor)
            .thenCompose(f -> f);
        join(future);
    }

    @SuppressWarnings("UnusedReturnValue")
    private <T> T join(CompletableFuture<T> future) {
        if (RenderSystem.isOnRenderThreadOrInit()) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.managedBlock(() -> {
                if (future.isDone()) {
                    return true;
                }
                tick();
                return false;
            });
        }
        return future.join();
    }

    public InternalExecutor getExecutor() {
        return executor;
    }

    private static final class InternalExecutor implements Executor {
        private static final long TICK_BUDGET = TimeUnit.MILLISECONDS.toNanos(2);

        private final ConcurrentLinkedQueue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();

        public void tick() {
            final long startTime = System.nanoTime();
            do {
                Runnable r = this.taskQueue.poll();
                if (r != null) {
                    r.run();
                } else {
                    return;
                }
            } while ((System.nanoTime() - startTime) < TICK_BUDGET);
        }

        @Override
        public void execute(Runnable runnable) {
            taskQueue.add(runnable);
        }
    }

}
