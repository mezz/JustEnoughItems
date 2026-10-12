package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.NativeImage;
import mezz.jei.fabric.test.mixin.MouseHandlerAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Runs client assertions and real mouse callbacks through the 1.21.1 test harness. */
final class FabricClientTestContext {
	private static final AtomicBoolean REGISTERED = new AtomicBoolean();
	private static final AtomicLong TICKS = new AtomicLong();
	private final Input input = new Input();

	FabricClientTestContext() {
		if (REGISTERED.compareAndSet(false, true)) {
			ClientTestUtil.runOnClient(client -> ClientTickEvents.END_CLIENT_TICK.register(ignored -> TICKS.incrementAndGet()));
		}
	}

	void runOnClient(Consumer<Minecraft> task) {
		ClientTestUtil.runOnClient(task);
	}

	<T> T computeOnClient(Function<Minecraft, T> task) {
		return ClientTestUtil.computeOnClient(task);
	}

	void setScreen(Supplier<Screen> screen) {
		runOnClient(client -> client.setScreen(screen.get()));
		waitTicks(2);
	}

	void waitTick() {
		waitTicks(1);
	}

	void waitTicks(int count) {
		long target = TICKS.get() + count;
		ClientTestUtil.waitUntil(() -> TICKS.get() >= target, Duration.ofSeconds(30), () -> "Timed out waiting for " + count + " client ticks");
	}

	Input getInput() {
		return input;
	}

	Path takeScreenshot(String name) {
		return computeOnClient(client -> {
			Path path = client.gameDirectory.toPath().resolve("screenshots").resolve(name + ".png");
			try (NativeImage image = Screenshot.takeScreenshot(client.getMainRenderTarget())) {
				Files.createDirectories(path.getParent());
				image.writeToFile(path);
				return path;
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	static final class Input {
		void setCursorPos(double x, double y) {
			ClientTestUtil.runOnClient(client -> {
				long window = client.getWindow().getWindow();
				// Native mouse movement is ignored when the test window loses desktop focus.
				client.setWindowActive(true);
				// Inject one movement event; desktop cursor warps can enqueue a second,
				// clipped event when the test window is larger than the virtual display.
				((MouseHandlerAccessor) client.mouseHandler).jei$invokeOnMove(window, x, y);
				client.mouseHandler.handleAccumulatedMovement();
			});
		}

		void holdKey(int key) {
			FabricClientTestInput.holdKey(key);
		}

		void releaseKey(int key) {
			FabricClientTestInput.releaseKey(key);
		}

		void pressKey(int key) {
			FabricClientTestInput.pressKey(key);
		}

		void holdMouse(int button) {
			mouseButton(button, GLFW.GLFW_PRESS);
		}

		void releaseMouse(int button) {
			mouseButton(button, GLFW.GLFW_RELEASE);
		}

		private void mouseButton(int button, int action) {
			ClientTestUtil.runOnClient(client -> ((MouseHandlerAccessor) client.mouseHandler)
				.jei$invokeOnPress(client.getWindow().getWindow(), button, action, 0));
		}

		void scroll(double horizontal, double vertical) {
			ClientTestUtil.runOnClient(client -> ((MouseHandlerAccessor) client.mouseHandler)
				.jei$invokeOnScroll(client.getWindow().getWindow(), horizontal, vertical));
		}

		void scroll(double vertical) {
			ClientTestUtil.runOnClient(client -> ((MouseHandlerAccessor) client.mouseHandler)
				.jei$invokeOnScroll(client.getWindow().getWindow(), 0, vertical));
		}

		void resizeWindow(int width, int height) {
			ClientTestUtil.runOnClient(client -> GLFW.glfwSetWindowSize(client.getWindow().getWindow(), width, height));
		}
	}
}
