package mezz.jei.ingredients;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.ImmutableSet;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.startup.ModIdHelper;
import mezz.jei.util.LegacyUtil;
import mezz.jei.util.Log;
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
	private final List<String> tooltipStrings;
	private final Collection<String> oreDictStrings;
	private final Collection<String> creativeTabsStrings;
	private final Collection<String> colorStrings;
	private final String resourceId;

	@Nullable
	public static <V> IngredientListElement<V> create(V ingredient, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer) {
		try {
			return new IngredientListElement<V>(ingredient, ingredientHelper, ingredientRenderer);
		} catch (RuntimeException e) {
			try {
				String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
				Log.warning("Found a broken ingredient {}", ingredientInfo, e);
				Config.addIngredientToConfigBlacklist(ingredient, Config.IngredientBlacklistType.WILDCARD, ingredientHelper);
			} catch (RuntimeException e2) {
				Log.warning("Found a broken ingredient.", e2);
				Config.addIngredientToConfigBlacklist(ingredient, Config.IngredientBlacklistType.WILDCARD, ingredientHelper);
			}
			return null;
		}
	}

	protected IngredientListElement(V ingredient, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer) {
		this.ingredient = ingredient;
		this.ingredientHelper = ingredientHelper;

		this.modId = ingredientHelper.getModId(ingredient);
		ModIdHelper modIdHelper = Internal.getModIdHelper();
		this.modName = modIdHelper.getModNameForModId(modId).toLowerCase(Locale.ENGLISH);

		this.displayName = IngredientInformation.getDisplayName(ingredient, ingredientHelper);

		if (Config.getColorSearchMode() != Config.SearchMode.DISABLED) {
			this.colorStrings = IngredientInformation.getColorStrings(ingredient, ingredientHelper);
		} else {
			this.colorStrings = Collections.emptyList();
		}

		this.resourceId = LegacyUtil.getResourceId(ingredient, ingredientHelper);

		this.tooltipStrings = IngredientInformation.getTooltipStrings(ingredient, ingredientRenderer, ImmutableSet.of(modId, modName, displayName, resourceId));

		if (ingredient instanceof ItemStack) {
			ItemStack itemStack = (ItemStack) ingredient;
			Item item = itemStack.getItem();

			this.oreDictStrings = new ArrayList<String>();
			for (int oreId : OreDictionary.getOreIDs(itemStack)) {
				String oreName = OreDictionary.getOreName(oreId).toLowerCase(Locale.ENGLISH);
				this.oreDictStrings.add(oreName);
			}

			this.creativeTabsStrings = new ArrayList<String>();
			for (CreativeTabs creativeTab : item.getCreativeTabs()) {
				if (creativeTab != null) {
					String creativeTabName = I18n.format(creativeTab.getTranslatedTabLabel()).toLowerCase();
					this.creativeTabsStrings.add(creativeTabName);
				}
			}
		} else {
			this.oreDictStrings = Collections.emptyList();
			this.creativeTabsStrings = Collections.emptyList();
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

	@Override
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
	public final List<String> getTooltipStrings() {
		return tooltipStrings;
	}

	@Override
	public Collection<String> getOreDictStrings() {
		return oreDictStrings;
	}

	@Override
	public Collection<String> getCreativeTabsStrings() {
		return creativeTabsStrings;
	}

	@Override
	public Collection<String> getColorStrings() {
		return colorStrings;
	}

	@Override
	public String getResourceId() {
		return resourceId;
	}
}
