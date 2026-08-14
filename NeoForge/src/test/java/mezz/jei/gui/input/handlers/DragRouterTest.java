package mezz.jei.gui.input.handlers;

import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DragRouterTest {
	@Test
	@SuppressWarnings("DataFlowIssue")
	public void cancelDragCancelsStartedCallbackAfterProxyChanges() {
		// Setup: a proxy starts a drag, then changes its delegate after the GUI state changes.
		RecordingDragHandler activeDragHandler = new RecordingDragHandler();
		RecordingDragHandler originalProxySource = new RecordingDragHandler(activeDragHandler);
		AtomicReference<IDragHandler> proxySource = new AtomicReference<>(originalProxySource);
		RecordingDragHandler otherRegisteredHandler = new RecordingDragHandler();
		DragRouter dragRouter = new DragRouter(
			new ProxyDragHandler(proxySource::get),
			otherRegisteredHandler
		);

		assertTrue(dragRouter.startDrag(null, null));
		int originalProxySourceCancelCount = originalProxySource.cancelCount;
		int otherRegisteredHandlerCancelCount = otherRegisteredHandler.cancelCount;

		// Operation: cancel after the proxy can no longer resolve the handler that started the drag.
		proxySource.set(NullDragHandler.INSTANCE);
		dragRouter.cancelDrag();

		// Assertions: the saved active callback and every currently registered handler are canceled.
		assertEquals(1, activeDragHandler.cancelCount);
		assertEquals(originalProxySourceCancelCount, originalProxySource.cancelCount);
		assertEquals(otherRegisteredHandlerCancelCount + 1, otherRegisteredHandler.cancelCount);
	}

	@Test
	public void cancelDragCancelsAllRegisteredHandlersWithoutAnActiveDrag() {
		// Setup: two registered handlers have passive drag state but neither started an active drag.
		RecordingDragHandler firstHandler = new RecordingDragHandler();
		RecordingDragHandler secondHandler = new RecordingDragHandler();
		DragRouter dragRouter = new DragRouter(firstHandler, secondHandler);

		// Operation: cancel drag handling while there is no active drag callback.
		dragRouter.cancelDrag();

		// Assertions: every registered handler is notified so passive state is cleared.
		assertEquals(1, firstHandler.cancelCount);
		assertEquals(1, secondHandler.cancelCount);
	}

	private static class RecordingDragHandler implements IDragHandler {
		private final @Nullable IDragHandler startedCallback;
		private int cancelCount;

		private RecordingDragHandler() {
			this(null);
		}

		private RecordingDragHandler(@Nullable IDragHandler startedCallback) {
			this.startedCallback = startedCallback;
		}

		@Override
		public Optional<IDragHandler> handleDragStart(Screen screen, UserInput input) {
			return Optional.ofNullable(startedCallback);
		}

		@Override
		public boolean handleDragComplete(Screen screen, UserInput input) {
			return false;
		}

		@Override
		public void handleDragCanceled() {
			cancelCount++;
		}
	}
}
