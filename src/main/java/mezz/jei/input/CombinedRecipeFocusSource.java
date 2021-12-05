package mezz.jei.input;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class CombinedRecipeFocusSource {
	private final List<IRecipeFocusSource> handlers;

	public CombinedRecipeFocusSource(IRecipeFocusSource... handlers) {
		this.handlers = Arrays.asList(handlers);
	}

	@Nullable
	public IClickedIngredient<?> getIngredientUnderMouse(UserInput input) {
		double mouseX = input.getMouseX();
		double mouseY = input.getMouseY();

		Stream<? extends IClickedIngredient<?>> stream = handlers.stream()
			.map(handler -> handler.getIngredientUnderMouse(mouseX, mouseY))
			.filter(Objects::nonNull);

		if (input.isMouse()) {
			stream = stream.filter(IClickedIngredient::canSetFocusWithMouse);
		}
		return stream
			.findFirst()
			.orElse(null);
	}
}
