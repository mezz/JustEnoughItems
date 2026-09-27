package mezz.jei.gui.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.input.IGuiInputLayer;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.IMouseOverable;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.InputType;
import mezz.jei.common.input.UserInput;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Front-to-back surface order shared by ingredient queries, pointer input, and drag starts.
 */
public final class GuiInputSurfaceStack {
	public enum KeyboardRouting {
		/** Apply pointer targeting to keyboard input. */
		TARGETED,
		/** Allow all keyboard input regardless of the mouse target. */
		GLOBAL,
		/** Allow page navigation globally; apply pointer targeting to other keys. */
		PAGE_NAVIGATION
	}

	private final List<IMouseOverable> surfaces;

	public GuiInputSurfaceStack(IMouseOverable... surfaces) {
		this.surfaces = List.of(surfaces);
	}

	private Optional<IMouseOverable> getSurfaceUnderMouse(double mouseX, double mouseY) {
		return surfaces.stream()
			.filter(surface -> surface.isMouseOver(mouseX, mouseY))
			.findFirst();
	}

	public boolean isTarget(IMouseOverable surface, double mouseX, double mouseY) {
		return getSurfaceUnderMouse(mouseX, mouseY)
			.map(target -> target == surface)
			.orElse(true);
	}

	private boolean isTarget(List<IMouseOverable> owners, double mouseX, double mouseY) {
		return getSurfaceUnderMouse(mouseX, mouseY)
			.map(owners::contains)
			.orElse(true);
	}

	Stream<IRecipeFocusSource> getFocusSources(double mouseX, double mouseY) {
		return getSurfaceUnderMouse(mouseX, mouseY).stream()
			.filter(IRecipeFocusSource.class::isInstance)
			.map(IRecipeFocusSource.class::cast);
	}

	public List<IGuiInputLayer> getForegroundLayers() {
		return surfaces.stream()
			.filter(IGuiInputLayer.class::isInstance)
			.map(IGuiInputLayer.class::cast)
			.toList();
	}

	public IUserInputHandler routeInput(IUserInputHandler handler, IMouseOverable... owners) {
		return routeInput(handler, KeyboardRouting.TARGETED, owners);
	}

	/** Focused typing and global navigation keys can opt out of mouse targeting. */
	public IUserInputHandler routeInput(
		IUserInputHandler handler,
		KeyboardRouting keyboardRouting,
		IMouseOverable... owners
	) {
		return new RoutedInputHandler(this, handler, keyboardRouting, List.of(owners));
	}

	public IDragHandler routeDrag(IDragHandler handler, IMouseOverable... owners) {
		return new RoutedDragHandler(this, handler, List.of(owners));
	}

	/** Consume unhandled pointer events over JEI, while letting the current Screen handle its own surface. */
	public IUserInputHandler createPointerBarrier(IMouseOverable... screenSurfaces) {
		return new PointerBarrierInputHandler(this, List.of(screenSurfaces));
	}

	private static final class RoutedInputHandler implements IUserInputHandler {
		private final GuiInputSurfaceStack surfaceStack;
		private final IUserInputHandler handler;
		private final KeyboardRouting keyboardRouting;
		private final List<IMouseOverable> owners;

		private RoutedInputHandler(
			GuiInputSurfaceStack surfaceStack,
			IUserInputHandler handler,
			KeyboardRouting keyboardRouting,
			List<IMouseOverable> owners
		) {
			this.surfaceStack = surfaceStack;
			this.handler = handler;
			this.keyboardRouting = keyboardRouting;
			this.owners = owners;
		}

		private boolean isGlobalKeyboardInput(UserInput input, IInternalKeyMappings keys) {
			if (input.getEvent().right().isEmpty()) {
				return false;
			}
			return switch (keyboardRouting) {
				case TARGETED -> false;
				case GLOBAL -> true;
				case PAGE_NAVIGATION -> input.is(keys.getNextPage()) || input.is(keys.getPreviousPage());
			};
		}

		@Override
		public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keys) {
			if (isGlobalKeyboardInput(input, keys) || surfaceStack.isTarget(owners, input.getMouseX(), input.getMouseY())) {
				// Return the actual handler so its captured release and drag are not hit-tested again.
				return handler.handleUserInput(screen, guiProperties, input, keys);
			}
			return Optional.empty();
		}

		@Override
		public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
			if (surfaceStack.isTarget(owners, mouseX, mouseY)) {
				return handler.handleMouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
			}
			return Optional.empty();
		}

		@Override
		public Optional<IUserInputHandler> handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
			if (surfaceStack.isTarget(owners, mouseX, mouseY)) {
				return handler.handleMouseDragged(mouseX, mouseY, mouseKey, dragX, dragY);
			}
			return Optional.empty();
		}

		@Override
		public void unfocus() {
			handler.unfocus();
		}
	}

	private static final class RoutedDragHandler implements IDragHandler {
		private final GuiInputSurfaceStack surfaceStack;
		private final IDragHandler handler;
		private final List<IMouseOverable> owners;

		private RoutedDragHandler(GuiInputSurfaceStack surfaceStack, IDragHandler handler, List<IMouseOverable> owners) {
			this.surfaceStack = surfaceStack;
			this.handler = handler;
			this.owners = owners;
		}

		@Override
		public Optional<IDragHandler> handleDragStart(Screen screen, UserInput input) {
			if (surfaceStack.isTarget(owners, input.getMouseX(), input.getMouseY())) {
				return handler.handleDragStart(screen, input);
			}
			return Optional.empty();
		}

		@Override
		public boolean handleDragComplete(Screen screen, UserInput input) {
			return handler.handleDragComplete(screen, input);
		}

		@Override
		public void handleDragCanceled() {
			handler.handleDragCanceled();
		}
	}

	private static final class PointerBarrierInputHandler implements IUserInputHandler {
		private final GuiInputSurfaceStack surfaceStack;
		private final List<IMouseOverable> screenSurfaces;

		private PointerBarrierInputHandler(GuiInputSurfaceStack surfaceStack, List<IMouseOverable> screenSurfaces) {
			this.surfaceStack = surfaceStack;
			this.screenSurfaces = screenSurfaces;
		}

		private Optional<IUserInputHandler> block(double mouseX, double mouseY) {
			return surfaceStack.getSurfaceUnderMouse(mouseX, mouseY)
				.filter(surface -> !screenSurfaces.contains(surface))
				.map(surface -> this);
		}

		@Override
		public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keys) {
			if (input.getEvent().right().isPresent()) {
				return Optional.empty();
			}
			if (input.getInputType() == InputType.EXECUTE) {
				return Optional.of(this);
			}
			return block(input.getMouseX(), input.getMouseY());
		}

		@Override
		public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
			return block(mouseX, mouseY);
		}

		@Override
		public Optional<IUserInputHandler> handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
			return block(mouseX, mouseY);
		}
	}
}
