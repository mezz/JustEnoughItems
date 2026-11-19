package mezz.jei.forge.test;

import net.minecraft.client.Minecraft;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

final class ForgeClientTestUtil {
	private ForgeClientTestUtil() {

	}

	static void run(Consumer<Minecraft> action) {
		compute(client -> {
			action.accept(client);
			return null;
		});
	}

	static <T> T compute(Function<Minecraft, T> action) {
		Minecraft client = Minecraft.getInstance();
		if (client.isSameThread()) {
			return action.apply(client);
		}
		CompletableFuture<T> result = new CompletableFuture<>();
		client.tell(() -> {
			try {
				result.complete(action.apply(client));
			} catch (Throwable error) {
				result.completeExceptionally(error);
			}
		});
		try {
			return result.get(120, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AssertionError("Interrupted while waiting for a client GameTest action", e);
		} catch (Exception e) {
			throw new AssertionError("Client GameTest action failed", e);
		}
	}

	static void waitUntil(BooleanSupplier condition, String message) {
		long deadline = System.nanoTime() + Duration.ofSeconds(120).toNanos();
		while (System.nanoTime() < deadline) {
			if (condition.getAsBoolean()) {
				return;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new AssertionError("Interrupted while waiting for a client GameTest condition", e);
			}
		}
		throw new AssertionError(message);
	}
}
