package mezz.jei.test;

import mezz.jei.gui.ingredients.ListElementInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class ListElementInfoTest {
	@Test
	public void tooltipStringsExcludeEmptyStrings() {
		List<Component> tooltip = List.of(
			Component.empty(),
			Component.literal(""),
			Component.literal(" \t "),
			Component.literal(" Alpha  Beta "),
			Component.literal("\tGamma\tDelta\t"),
			Component.literal(ChatFormatting.RED.toString())
		);

		Set<String> strings = ListElementInfo.getStrings(tooltip);

		Assertions.assertFalse(strings.contains(""));
		Assertions.assertEquals(Set.of("alpha", "beta", "gamma", "delta"), strings);
	}
}
