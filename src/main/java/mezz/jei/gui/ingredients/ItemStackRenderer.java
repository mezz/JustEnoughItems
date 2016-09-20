package mezz.jei.gui.ingredients;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import mezz.jei.Internal;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {
	private static final String oreDictionaryIngredient = Translator.translateToLocal("jei.tooltip.recipe.ore.dict");

	@Nullable
	private String oreDictEquivalent;

	@Override
	public void setIngredients(Collection<ItemStack> itemStacks) {
		oreDictEquivalent = Internal.getStackHelper().getOreDictEquivalent(itemStacks);
	}

	@Override
	public void draw(Minecraft minecraft, int xPosition, int yPosition, @Nullable ItemStack itemStack) {
		if (itemStack == null) {
			return;
		}

		FontRenderer font = getFontRenderer(minecraft, itemStack);

		minecraft.getRenderItem().renderItemAndEffectIntoGUI(null, itemStack, xPosition, yPosition);
		minecraft.getRenderItem().renderItemOverlayIntoGUI(font, itemStack, xPosition, yPosition, null);
		GlStateManager.disableBlend();
	}

	@Override
	public List<String> getTooltip(Minecraft minecraft, ItemStack itemStack) {
		List<String> list = itemStack.getTooltip(minecraft.thePlayer, minecraft.gameSettings.advancedItemTooltips);
		for (int k = 0; k < list.size(); ++k) {
			if (k == 0) {
				list.set(k, itemStack.getRarity().rarityColor + list.get(k));
			} else {
				list.set(k, TextFormatting.GRAY + list.get(k));
			}
		}

		if (oreDictEquivalent != null) {
			final String acceptsAny = String.format(oreDictionaryIngredient, oreDictEquivalent);
			list.add(TextFormatting.GRAY + acceptsAny);
		}

		return list;
	}

	@Override
	public FontRenderer getFontRenderer(Minecraft minecraft, ItemStack itemStack) {
		FontRenderer fontRenderer = itemStack.getItem().getFontRenderer(itemStack);
		if (fontRenderer == null) {
			fontRenderer = minecraft.fontRendererObj;
		}
		return fontRenderer;
	}
}
