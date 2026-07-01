package mezz.jei.test;

import mezz.jei.config.StyledTextHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StyledTextHelperTest {
	@Test
	public void preservesComponentStyle() {
		StringTextComponent text = new StringTextComponent("Minecraft");
		text.withStyle(TextFormatting.BLUE, TextFormatting.ITALIC);

		String result = StyledTextHelper.replaceFirst(text, "Minecraft", "%MODNAME%");

		assertEquals(TextFormatting.BLUE + "" + TextFormatting.ITALIC + "%MODNAME%", result);
	}

	@Test
	public void replacesTextAcrossComponentSegments() {
		StringTextComponent text = new StringTextComponent("Mine");
		text.withStyle(TextFormatting.BLUE);
		text.append(new StringTextComponent("craft").withStyle(TextFormatting.BLUE));

		String result = StyledTextHelper.replaceFirst(text, "Minecraft", "%MODNAME%");

		assertEquals(TextFormatting.BLUE + "%MODNAME%", result);
	}
}
