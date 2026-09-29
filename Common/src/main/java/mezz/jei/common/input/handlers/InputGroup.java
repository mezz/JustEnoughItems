package mezz.jei.common.input.handlers;

import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

/** Tries a group of controls in order and stops at the first one that handles the input. */
public class InputGroup implements IInputTarget {
	/** Identifies the group in debug output. */
	private final String name;
	/** Controls in priority order; the first visible child accepting an event handles it. */
	private final List<Child> children;

	/**
	 * A control that can be shown or hidden without rebuilding the group.
	 *
	 * @param visible whether to send the control a press or scroll event; checked for each event
	 * @param target the control receiving those events; reset with the group even when hidden
	 */
	public record Child(BooleanSupplier visible, IInputTarget target) {
	}

	public InputGroup(String name) {
		this(name, List.of());
	}

	public InputGroup(String name, Child... children) {
		this.name = name;
		this.children = List.of(children);
	}

	public InputGroup(String name, IInputTarget... children) {
		this(name, List.of(children));
	}

	public InputGroup(String name, List<IInputTarget> children) {
		this.name = name;
		this.children = children.stream().map(child -> new Child(() -> true, child)).toList();
	}

	@Override
	public Optional<IInputInteraction> beginInput(Screen screen, UserInput input, IInternalKeyMappings keys) {
		for (Child child : children) {
			if (!child.visible().getAsBoolean()) {
				continue;
			}
			Optional<IInputInteraction> interaction = child.target().beginInput(screen, input, keys);
			if (interaction.isPresent()) {
				return interaction;
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean scroll(double mouseX, double mouseY, double scrollX, double scrollY) {
		for (Child child : children) {
			if (child.visible().getAsBoolean() && child.target().scroll(mouseX, mouseY, scrollX, scrollY)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void resetInput() {
		children.forEach(child -> child.target().resetInput());
	}

	@Override
	public String toString() {
		return name + children;
	}
}
