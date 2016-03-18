package mezz.jei.gui.ingredients;

import mezz.jei.Internal;
import mezz.jei.util.StackHelper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {
	private static final String oreDictionaryIngredient = Translator.translateToLocal("jei.tooltip.recipe.ore.dict");

	@Nullable
	private String oreDictEquivalent;

	@Override
	public void setIngredients(@Nonnull Collection<ItemStack> itemStacks) {
		oreDictEquivalent = getOreDictEquivalent(itemStacks);
	}

	@Nullable
	private static String getOreDictEquivalent(@Nonnull Collection<ItemStack> itemStacks) {
		if (itemStacks.size() < 2) {
			return null;
		}

		StackHelper stackHelper = Internal.getStackHelper();

		final ItemStack firstStack = itemStacks.iterator().next();
		if (firstStack != null) {
			for (final int oreId : OreDictionary.getOreIDs(firstStack)) {
				final String oreName = OreDictionary.getOreName(oreId);
				List<ItemStack> ores = OreDictionary.getOres(oreName);
				ores = stackHelper.getAllSubtypes(ores);
				if (stackHelper.containsSameStacks(itemStacks, ores)) {
					return oreName;
				}
			}
		}
		return null;
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft, int xPosition, int yPosition, @Nullable ItemStack itemStack) {
		if (itemStack == null) {
			return;
		}

		FontRenderer font = getFontRenderer(minecraft, itemStack);

		minecraft.getRenderItem().renderItemAndEffectIntoGUI(null, itemStack, xPosition, yPosition);
		minecraft.getRenderItem().renderItemOverlayIntoGUI(font, itemStack, xPosition, yPosition, null);
		GlStateManager.disableBlend();
	}

	@Nonnull
	@Override
	public List<String> getTooltip(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
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
	public FontRenderer getFontRenderer(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
		FontRenderer fontRenderer = itemStack.getItem().getFontRenderer(itemStack);
		if (fontRenderer == null) {
			fontRenderer = minecraft.fontRendererObj;
		}
		return fontRenderer;
	}
}
