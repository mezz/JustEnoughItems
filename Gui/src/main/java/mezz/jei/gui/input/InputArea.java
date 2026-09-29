package mezz.jei.gui.input;

import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.IMouseOverable;
import mezz.jei.common.input.handlers.InputGroup;
import mezz.jei.common.input.interaction.ApiInputAdapter.IMouseDragHandler;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A screen area with its controls, shortcuts, and ingredient drag sources.
 * Examples include the ingredient list, recipe display, and tooltips. Create one with {@link #builder}.
 *
 * @param name label used in debug logs
 * @param area tests whether this input area is visible and contains the mouse pointer;
 *             also supplies ingredient lookup when it implements {@link IRecipeFocusSource}
 * @param blocksUnhandledMouseInput whether to block mouse input from reaching the Minecraft screen
 *                                  even when no control or shortcut handles it
 * @param controls input handler for the controls, tried before this input area's shortcuts
 * @param commands shortcuts for this input area; those with {@link InputCommands.Scope#SCREEN}
 *                 can also run when the pointer is elsewhere
 * @param dragSources sources that can start an ingredient drag, tried in order on left mouse button press;
 *                    the first to accept it starts the drag, along with any selected click action
 * @param apiDragHandler public recipe callbacks to try under the pointer when no control is using mouse movement;
 *                       null for input areas without recipe callbacks
 */
public record InputArea(String name, IMouseOverable area, boolean blocksUnhandledMouseInput, IInputTarget controls,
	InputCommands commands, List<DragSource> dragSources, @Nullable IMouseDragHandler apiDragHandler) {
	public InputArea {
		dragSources = List.copyOf(dragSources);
	}

	/**
	 * Starts an input area with no controls, shortcuts, or drag sources.
	 * By default, mouse input that JEI does not handle can reach the Minecraft screen.
	 *
	 * @param name label used in debug logs
	 * @param area tests visibility and whether the mouse pointer is inside; may also supply ingredient lookup
	 */
	public static Builder builder(String name, IMouseOverable area) {
		return new Builder(name, area);
	}

	/** Configures the controls, shortcuts, and dragging available in an input area. */
	public static final class Builder {
		private final String name;
		private final IMouseOverable area;
		private boolean blocksUnhandledMouseInput;
		private IInputTarget controls;
		private InputCommands commands = new InputCommands();
		private List<DragSource> dragSources = List.of();
		private @Nullable IMouseDragHandler apiDragHandler;

		private Builder(String name, IMouseOverable area) {
			this.name = name;
			this.area = area;
			this.controls = new InputGroup(name);
		}

		/**
		 * Stops unhandled mouse input from reaching the Minecraft screen behind this area.
		 * For example, clicking empty space in the ingredient list does not activate an inventory
		 * slot behind it. Controls and shortcuts can still handle input within this area.
		 */
		public Builder blockUnhandledMouseInput() {
			this.blocksUnhandledMouseInput = true;
			return this;
		}

		/** Sets the controls to try before this area's shortcuts. */
		public Builder controls(IInputTarget controls) {
			this.controls = controls;
			return this;
		}

		/** Sets this area's shortcuts. If omitted, the area starts with an empty collection. */
		public Builder commands(InputCommands commands) {
			this.commands = commands;
			return this;
		}

		/** Sets the ingredient drag sources to try, in order, when the player presses the left mouse button. */
		public Builder dragSources(List<DragSource> dragSources) {
			this.dragSources = dragSources;
			return this;
		}

		/** Sets public recipe drag callbacks to try when no internal control is using mouse movement. */
		public Builder apiDragHandler(IMouseDragHandler apiDragHandler) {
			this.apiDragHandler = apiDragHandler;
			return this;
		}

		public InputArea build() {
			return new InputArea(name, area, blocksUnhandledMouseInput, controls, commands, dragSources, apiDragHandler);
		}
	}
}
