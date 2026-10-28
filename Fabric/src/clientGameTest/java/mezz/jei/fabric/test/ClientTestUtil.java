package mezz.jei.fabric.test;

import net.minecraft.client.Minecraft;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Helpers for coordinating assertions with the Minecraft render thread.
 */
final class ClientTestUtil {
	private static final Duration CLIENT_TASK_TIMEOUT = Duration.ofSeconds(10);
	private static final long CLIENT_TASK_BATCH_NANOS = TimeUnit.MILLISECONDS.toNanos(5);
	private static final long CLIENT_TASK_HANDOFF_NANOS = TimeUnit.MILLISECONDS.toNanos(1);
	private static final BlockingQueue<Runnable> CLIENT_TASKS = new LinkedBlockingQueue<>();
	private static final AtomicBoolean CLIENT_TASKS_SCHEDULED = new AtomicBoolean();

	private ClientTestUtil() {

	}

	public static void runOnClient(Consumer<Minecraft> task) {
		runOnClient(task, CLIENT_TASK_TIMEOUT);
	}

	public static void runOnClient(Consumer<Minecraft> task, Duration timeout) {
		computeOnClient(client -> {
			task.accept(client);
			return null;
		}, timeout);
	}

	public static <T> T computeOnClient(Function<Minecraft, T> task) {
		return computeOnClient(task, CLIENT_TASK_TIMEOUT);
	}

	public static <T> T computeOnClient(Function<Minecraft, T> task, Duration timeout) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.isSameThread()) {
			return task.apply(minecraft);
		}
		CompletableFuture<T> future = new CompletableFuture<>();
		CLIENT_TASKS.add(() -> {
			try {
				future.complete(task.apply(minecraft));
			} catch (Throwable t) {
				future.completeExceptionally(t);
			}
		});
		scheduleClientTasks(minecraft);

		try {
			return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw new AssertionError("Timed out waiting for Minecraft client task", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AssertionError("Interrupted while waiting for Minecraft client task", e);
		} catch (Exception e) {
			throw new AssertionError("Minecraft client task failed", e);
		}
	}

	private static void scheduleClientTasks(Minecraft minecraft) {
		if (CLIENT_TASKS_SCHEDULED.compareAndSet(false, true)) {
			minecraft.tell(() -> runClientTasks(minecraft));
		}
	}

	private static void runClientTasks(Minecraft minecraft) {
		long deadline = System.nanoTime() + CLIENT_TASK_BATCH_NANOS;
		try {
			Runnable task = CLIENT_TASKS.poll();
			while (task != null) {
				task.run();
				long remaining = deadline - System.nanoTime();
				if (remaining <= 0) {
					break;
				}
				// Let the test thread submit its next assertion before rendering another frame.
				task = CLIENT_TASKS.poll(Math.min(remaining, CLIENT_TASK_HANDOFF_NANOS), TimeUnit.NANOSECONDS);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			CLIENT_TASKS_SCHEDULED.set(false);
			if (!CLIENT_TASKS.isEmpty()) {
				scheduleClientTasks(minecraft);
			}
		}
	}

	public static void waitUntil(BooleanSupplier condition, Duration timeout, Supplier<String> timeoutMessage) {
		Instant deadline = Instant.now().plus(timeout);
		while (Instant.now().isBefore(deadline)) {
			if (condition.getAsBoolean()) {
				return;
			}
			sleep();
		}
		throw new AssertionError(timeoutMessage.get());
	}

	private static void sleep() {
		try {
			Thread.sleep(50L);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AssertionError("Interrupted while waiting for client test condition", e);
		}
	}
}
