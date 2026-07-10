package mezz.jei.neoforge.tests.client;

import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Helpers for coordinating assertions with the Minecraft render thread.
 */
final class ClientTestUtil {
	private static final Duration CLIENT_TASK_TIMEOUT = Duration.ofSeconds(10);

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

	@Nullable
	public static <T> T computeOnClient(Function<Minecraft, T> task) {
		return computeOnClient(task, CLIENT_TASK_TIMEOUT);
	}

	@Nullable
	public static <T> T computeOnClient(Function<Minecraft, @Nullable T> task, Duration timeout) {
		Minecraft minecraft = Minecraft.getInstance();
		CompletableFuture<@Nullable T> future = new CompletableFuture<>();
		minecraft.execute(() -> {
			try {
				future.complete(task.apply(minecraft));
			} catch (Throwable t) {
				future.completeExceptionally(t);
			}
		});

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
