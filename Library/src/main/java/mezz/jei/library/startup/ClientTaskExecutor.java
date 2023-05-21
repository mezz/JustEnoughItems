package mezz.jei.library.startup;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.Tickable;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public final class ClientTaskExecutor implements Executor {
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
        if (RenderSystem.isOnRenderThreadOrInit()) {
            // we can't queue on the client render thread,
            // it would block forever waiting for the next tick to happen
            runnable.run();
        } else {
            this.taskQueue.add(runnable);
        }
    }
}
