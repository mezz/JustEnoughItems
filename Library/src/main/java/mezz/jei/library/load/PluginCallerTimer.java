package mezz.jei.library.load;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PluginCallerTimer implements AutoCloseable {
	private final ScheduledExecutorService executor;
	private final Set<Ref> refs = ConcurrentHashMap.newKeySet();

	public PluginCallerTimer() {
		this.executor = Executors.newSingleThreadScheduledExecutor();
		this.executor.scheduleAtFixedRate(this::run, 1, 1, TimeUnit.MILLISECONDS);
	}

	private synchronized void run() {
		refs.stream()
			.map(r -> r.runnable)
			.forEach(PluginCallerTimerRunnable::check);
	}

	public synchronized Ref begin(String title, ResourceLocation pluginUid) {
		PluginCallerTimerRunnable runnable = new PluginCallerTimerRunnable(title, pluginUid);
		Ref ref = new Ref(runnable);
		refs.add(ref);
		return ref;
	}

	private synchronized boolean end(Ref ref) {
		return refs.remove(ref);
	}

	@Override
	public synchronized void close() {
		this.executor.shutdown();
	}

	public final class Ref implements AutoCloseable {
		public final PluginCallerTimerRunnable runnable;

		public Ref(PluginCallerTimerRunnable runnable) {
			this.runnable = runnable;
		}

		@Override
		public void close() {
			if (end(this)) {
				this.runnable.stop();
			}
		}
	}
}
