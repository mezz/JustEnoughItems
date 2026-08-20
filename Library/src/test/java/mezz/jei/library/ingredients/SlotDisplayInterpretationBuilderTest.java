package mezz.jei.library.ingredients;

import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotDisplayInterpretationBuilderTest {
	@Test
	void childDisplaysCanHaveIndependentTransformers() {
		SlotDisplayInterpretationBuilder<Integer> builder = new SlotDisplayInterpretationBuilder<>();
		SlotDisplay display = SlotDisplay.Empty.INSTANCE;

		builder
			.addChildDisplay(display)
			.addChildDisplay(display, value -> value + 1);

		List<SlotDisplayInterpretationBuilder.ChildDisplay<Integer>> children = builder.getChildDisplays();
		assertEquals(2, children.size());
		assertEquals(1, children.getFirst().ingredientTransformer().apply(1));
		assertEquals(2, children.getLast().ingredientTransformer().apply(1));
	}

	@Test
	void addingChildrenPreservesTransformingChildDisplay() {
		SlotDisplayInterpretationBuilder<Integer> builder = new SlotDisplayInterpretationBuilder<>();
		SlotDisplay display = SlotDisplay.Empty.INSTANCE;

		builder.addChildDisplay(display, value -> value + 1);
		builder.addChildDisplay(display);

		List<SlotDisplayInterpretationBuilder.ChildDisplay<Integer>> children = builder.getChildDisplays();
		assertEquals(2, children.size());
		assertEquals(2, children.getFirst().ingredientTransformer().apply(1));
		assertEquals(1, children.getLast().ingredientTransformer().apply(1));
	}

	@Test
	@SuppressWarnings("removal")
	void emptyChildListRemainsExplicitlyConfigured() {
		SlotDisplayInterpretationBuilder<Integer> builder = new SlotDisplayInterpretationBuilder<>();

		builder.setChildDisplays(List.of());

		assertTrue(builder.isChildDisplaysSet());
		assertTrue(builder.getChildDisplays().isEmpty());
	}
}
