package mezz.jei.api.runtime;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public interface IJeiClientExecutor {
    CompletableFuture<Void> runOnClientThread(Runnable runnable);
    <T> CompletableFuture<T> runOnClientThread(Supplier<T> supplier);
    Executor getExecutor();
}
