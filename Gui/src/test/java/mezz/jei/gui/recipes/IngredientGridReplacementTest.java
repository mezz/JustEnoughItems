package mezz.jei.gui.recipes;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IngredientGridReplacementTest {
	@Test
	void interactiveGridReplacesOnlyJeisStaticGrid() {
		List<String> tooltipElements = new ArrayList<>(List.of(
			"ingredient tooltip",
			"static ingredient grid",
			"mod tooltip callback"
		));

		IngredientGridReplacement.replace(tooltipElements, e -> e.equals("static ingredient grid"), "interactive ingredient grid");

		assertEquals(
			List.of("ingredient tooltip", "interactive ingredient grid", "mod tooltip callback"),
			tooltipElements
		);
	}

	@Test
	void interactiveGridIsAddedWithoutRemovingModComponents() {
		List<String> tooltipElements = new ArrayList<>(List.of("mod tooltip callback"));

		IngredientGridReplacement.replace(tooltipElements, e -> e.equals("static ingredient grid"), "interactive ingredient grid");

		assertEquals(List.of("mod tooltip callback", "interactive ingredient grid"), tooltipElements);
	}
}
