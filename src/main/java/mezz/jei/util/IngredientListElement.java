package mezz.jei.util;

import javax.annotation.Nullable;
import java.util.Locale;

import mezz.jei.IngredientInformation;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class IngredientListElement<V> implements IIngredientListElement<V> {
	private final V ingredient;
	private final IIngredientHelper<V> ingredientHelper;
	private final String displayName;
	private final String modName;
	private final String modId;
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

		this.modId = ingredientHelper.getModId(ingredient);
		ModIdUtil modIdUtil = Internal.getModIdUtil();
		this.modName = modIdUtil.getModNameForModId(modId).toLowerCase(Locale.ENGLISH);

		this.displayName = ingredientHelper.getDisplayName(ingredient).toLowerCase();

		this.tooltipString = IngredientInformation.getTooltipString(ingredient, ingredientRenderer, modId, modName, this.displayName);

		if (Config.getColorSearchMode() != Config.SearchMode.DISABLED) {
			this.colorString = IngredientInformation.getColorString(ingredient, ingredientHelper);
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

	@Override
	public final V getIngredient() {
		return ingredient;
	}

	@Override
	public IIngredientHelper<V> getIngredientHelper() {
		return ingredientHelper;
	}

	public final String getDisplayName() {
		return displayName;
	}

	@Override
	public final String getModName() {
		return modName;
	}

	@Override
	public String getModId() {
		return modId;
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
