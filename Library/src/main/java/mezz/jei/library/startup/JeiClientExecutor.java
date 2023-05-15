package mezz.jei.library.startup;

import mezz.jei.api.runtime.IJeiClientExecutor;
import net.minecraft.client.renderer.texture.Tickable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public final class JeiClientExecutor implements IJeiClientExecutor, Tickable {
    private final Executor executor;

    public JeiClientExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void tick() {
        if (executor instanceof Tickable tickable) {
            tickable.tick();
        }
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    @Override
    public CompletableFuture<Void> runOnClientThread(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, executor);
    }

    @Override
    public <T> CompletableFuture<T> runOnClientThread(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }
}
