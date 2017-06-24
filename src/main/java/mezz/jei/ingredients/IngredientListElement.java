package mezz.jei.ingredients;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.startup.IModIdHelper;
import mezz.jei.util.LegacyUtil;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class IngredientListElement<V> implements IIngredientListElement<V> {
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s");
	private static final Map<String, Integer> WILDCARD_ADDED_ORDER = new HashMap<>();
	private static int ADDED_INDEX = 0;

	private final V ingredient;
	private final int orderIndex;
	private final IIngredientHelper<V> ingredientHelper;
	private final IIngredientRenderer<V> ingredientRenderer;
	private final String displayName;
	private final String modName;
	private final String modId;
	private final String resourceId;
	private boolean hidden = false;

	@Nullable
	public static <V> IngredientListElement<V> create(V ingredient, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer, IModIdHelper modIdHelper) {
		try {
			final int orderIndex;
			String uid = ingredientHelper.getWildcardId(ingredient);
			if (WILDCARD_ADDED_ORDER.containsKey(uid)) {
				orderIndex = WILDCARD_ADDED_ORDER.get(uid);
			} else {
				WILDCARD_ADDED_ORDER.put(uid, ADDED_INDEX);
				orderIndex = ADDED_INDEX;
				ADDED_INDEX++;
			}

			return new IngredientListElement<>(ingredient, orderIndex, ingredientHelper, ingredientRenderer, modIdHelper);
		} catch (RuntimeException e) {
			try {
				String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
				Log.get().warn("Found a broken ingredient {}", ingredientInfo, e);
			} catch (RuntimeException e2) {
				Log.get().warn("Found a broken ingredient.", e2);
			}
			return null;
		}
	}

	protected IngredientListElement(V ingredient, int orderIndex, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer, IModIdHelper modIdHelper) {
		this.ingredient = ingredient;
		this.orderIndex = orderIndex;
		this.ingredientHelper = ingredientHelper;
		this.ingredientRenderer = ingredientRenderer;

		this.modId = ingredientHelper.getModId(ingredient);
		this.modName = modIdHelper.getModNameForModId(modId);
		this.displayName = IngredientInformation.getDisplayName(ingredient, ingredientHelper);
		this.resourceId = LegacyUtil.getResourceId(ingredient, ingredientHelper);
	}

	@Override
	public final V getIngredient() {
		return ingredient;
	}

	@Override
	public int getOrderIndex() {
		return orderIndex;
	}

	@Override
	public IIngredientHelper<V> getIngredientHelper() {
		return ingredientHelper;
	}

	@Override
	public IIngredientRenderer<V> getIngredientRenderer() {
		return ingredientRenderer;
	}

	@Override
	public final String getDisplayName() {
		return displayName;
	}

	@Override
	public String getModName() {
		return modName;
	}

	@Override
	public Set<String> getModNameStrings() {
		String modNameLowercase = this.modName.toLowerCase(Locale.ENGLISH);
		String modNameNoSpaces = SPACE_PATTERN.matcher(modNameLowercase).replaceAll("");
		String modIdNoSpaces = SPACE_PATTERN.matcher(modId).replaceAll("");
		return ImmutableSet.of(modNameLowercase, modId, modNameNoSpaces, modIdNoSpaces);
	}

	@Override
	public final List<String> getTooltipStrings() {
		String modNameLowercase = this.modName.toLowerCase(Locale.ENGLISH);
		String displayNameLowercase = Translator.toLowercaseWithLocale(this.displayName);
		return IngredientInformation.getTooltipStrings(ingredient, ingredientHelper, ingredientRenderer, ImmutableSet.of(modId, modNameLowercase, displayNameLowercase, resourceId));
	}

	@Override
	public Collection<String> getOreDictStrings() {
		Collection<String> oreDictStrings = new ArrayList<>();

		if (ingredient instanceof ItemStack) {
			ItemStack itemStack = (ItemStack) ingredient;
			for (int oreId : OreDictionary.getOreIDs(itemStack)) {
				String oreNameLowercase = OreDictionary.getOreName(oreId).toLowerCase(Locale.ENGLISH);
				oreDictStrings.add(oreNameLowercase);
			}
		}

		return oreDictStrings;
	}

	@Override
	public Collection<String> getCreativeTabsStrings() {
		Collection<String> creativeTabsStrings = new ArrayList<>();

		if (ingredient instanceof ItemStack) {
			ItemStack itemStack = (ItemStack) ingredient;
			Item item = itemStack.getItem();
			for (CreativeTabs creativeTab : item.getCreativeTabs()) {
				if (creativeTab != null) {
					String creativeTabName = I18n.format(creativeTab.getTranslatedTabLabel());
					String creativeTabNameLowercase = Translator.toLowercaseWithLocale(creativeTabName);
					creativeTabsStrings.add(creativeTabNameLowercase);
				}
			}
		}

		return creativeTabsStrings;
	}

	@Override
	public Collection<String> getColorStrings() {
		return IngredientInformation.getColorStrings(ingredient, ingredientHelper);
	}

	@Override
	public String getResourceId() {
		return resourceId;
	}

	@Override
	public boolean isHidden() {
		return hidden;
	}

	@Override
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
}
