package mezz.jei.startup;

import net.minecraftforge.fml.common.ProgressManager;

/** A startup progress bar that is removed even when registration fails. */
public final class StartupProgress implements AutoCloseable {
	private final ProgressManager.ProgressBar bar;

	private StartupProgress(ProgressManager.ProgressBar bar) {
		this.bar = bar;
	}

	public static StartupProgress start(String title, int steps) {
		return new StartupProgress(ProgressManager.push(title, steps));
	}

	public void step(String message) {
		bar.step(message);
	}

	@Override
	public void close() {
		ProgressManager.pop(bar);
	}
}
