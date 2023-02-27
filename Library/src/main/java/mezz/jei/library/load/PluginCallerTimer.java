package mezz.jei.library.load;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PluginCallerTimer implements AutoCloseable {
	private final ScheduledExecutorService executor;
	private @Nullable PluginCallerTimerRunnable runnable;

	public PluginCallerTimer() {
		this.executor = Executors.newSingleThreadScheduledExecutor();
		this.executor.scheduleAtFixedRate(this::run, 1, 1, TimeUnit.MILLISECONDS);
	}

	private synchronized void run() {
		if (this.runnable != null) {
			this.runnable.check();
		}
	}

	public synchronized void begin(String title, ResourceLocation pluginUid) {
		this.runnable = new PluginCallerTimerRunnable(title, pluginUid);
	}

	public synchronized void end() {
		if (this.runnable != null) {
			this.runnable.stop();
			this.runnable = null;
		}
	}

	@Override
	public void close() {
		this.executor.shutdown();
	}
}
