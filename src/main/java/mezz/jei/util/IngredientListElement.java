package mezz.jei.util;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Joiner;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.util.color.ColorNamer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.oredict.OreDictionary;

public class IngredientListElement<V> implements IIngredientListElement<V> {
	private final V ingredient;
	private final IIngredientHelper<V> ingredientHelper;
	private final String searchString;
	private final String modNameString;
	private final String tooltipString;
	private final String oreDictString;
	private final String creativeTabsString;
	private final String colorString;

	@Nullable
	public static <V> IngredientListElement<V> create(V ingredient, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer) {
		try {
			return new IngredientListElement<V>(ingredient, ingredientHelper, ingredientRenderer);
		} catch (RuntimeException e) {
			try {
				String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
				Log.warning("Found a broken ingredient {}", ingredientInfo, e);
			} catch (RuntimeException e2) {
				Log.warning("Found a broken ingredient.", e2);
			}
			return null;
		}
	}

	protected IngredientListElement(V ingredient, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer) {
		this.ingredient = ingredient;
		this.ingredientHelper = ingredientHelper;

		String modId = ingredientHelper.getModId(ingredient);
		ModIdUtil modIdUtil = Internal.getModIdUtil();
		String modName = modIdUtil.getModNameForModId(modId).toLowerCase(Locale.ENGLISH);
		modId = modName.toLowerCase(Locale.ENGLISH);

		String displayName = ingredientHelper.getDisplayName(ingredient);
		this.searchString = displayName.toLowerCase();

		this.modNameString = modId + ' ' + modName;

		this.tooltipString = getTooltipString(ingredient, ingredientRenderer, modId, modName, this.searchString);

		if (Config.isColorSearchEnabled()) {
			Iterable<Color> colors = ingredientHelper.getColors(ingredient);
			ColorNamer colorNamer = Internal.getColorNamer();
			Collection<String> colorNames = colorNamer.getColorNames(colors);
			this.colorString = Joiner.on(' ').join(colorNames).toLowerCase();
		} else {
			this.colorString = "";
		}

		if (ingredient instanceof ItemStack) {
			ItemStack itemStack = (ItemStack) ingredient;
			Item item = itemStack.getItem();

			StringBuilder oreDictStringBuilder = new StringBuilder();
			for (int oreId : OreDictionary.getOreIDs(itemStack)) {
				String oreName = OreDictionary.getOreName(oreId).toLowerCase(Locale.ENGLISH);
				oreDictStringBuilder.append(oreName).append(' ');
			}
			this.oreDictString = oreDictStringBuilder.toString();

			StringBuilder creativeTabStringBuilder = new StringBuilder();
			for (CreativeTabs creativeTab : item.getCreativeTabs()) {
				if (creativeTab != null) {
					String creativeTabName = I18n.format(creativeTab.getTranslatedTabLabel()).toLowerCase();
					creativeTabStringBuilder.append(creativeTabName).append(' ');
				}
			}
			this.creativeTabsString = creativeTabStringBuilder.toString();
		} else {
			this.oreDictString = "";
			this.creativeTabsString = "";
		}
	}

	private static <T> String getTooltipString(T ingredient, IIngredientRenderer<T> ingredientRenderer, String modId, String modName, String searchString) {
		List<String> tooltip;
		try {
			tooltip = ingredientRenderer.getTooltip(Minecraft.getMinecraft(), ingredient);
		} catch (RuntimeException ignored) {
			return "";
		} catch (LinkageError ignored) {
			return "";
		}

		String tooltipString = Joiner.on(' ').join(tooltip).toLowerCase();
		tooltipString = TextFormatting.getTextWithoutFormattingCodes(tooltipString);
		if (tooltipString != null) {
			tooltipString = tooltipString.replace(modId, "");
			tooltipString = tooltipString.replace(modName, "");
			return tooltipString.replace(searchString, "");
		}

		return "";
	}

	@Override
	public final V getIngredient() {
		return ingredient;
	}

	@Override
	public IIngredientHelper<V> getIngredientHelper() {
		return ingredientHelper;
	}

	@Override
	public final String getSearchString() {
		return searchString;
	}

	@Override
	public final String getModNameString() {
		return modNameString;
	}

	@Override
	public final String getTooltipString() {
		return tooltipString;
	}

	@Override
	public String getOreDictString() {
		return oreDictString;
	}

	@Override
	public String getCreativeTabsString() {
		return creativeTabsString;
	}

	@Override
	public String getColorString() {
		return colorString;
	}
}
