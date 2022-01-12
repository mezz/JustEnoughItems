package mezz.jei.input;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class CombinedRecipeFocusSource {
	private final List<IRecipeFocusSource> handlers;

	public CombinedRecipeFocusSource(IRecipeFocusSource... handlers) {
		this.handlers = Arrays.asList(handlers);
	}

	public Optional<? extends IClickedIngredient<?>> getIngredientUnderMouse(UserInput input) {
		double mouseX = input.getMouseX();
		double mouseY = input.getMouseY();

		Stream<? extends IClickedIngredient<?>> stream = handlers.stream()
			.map(handler -> handler.getIngredientUnderMouse(mouseX, mouseY))
			.flatMap(Optional::stream);

		if (input.isMouse()) {
			stream = stream.filter(IClickedIngredient::canSetFocusWithMouse);
		}

		return stream.findFirst();
	}
}
