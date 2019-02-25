package mezz.jei.ingredients;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionHelper;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.NonNullList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.startup.IModIdHelper;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.IngredientSet;
import mezz.jei.util.Log;

public class IngredientRegistry implements IIngredientRegistry {
	private final IModIdHelper modIdHelper;
	private final IngredientBlacklistInternal blacklist;
	private final Map<IIngredientType, IngredientSet> ingredientsMap;
	private final ImmutableMap<IIngredientType, IIngredientHelper> ingredientHelperMap;
	private final ImmutableMap<IIngredientType, IIngredientRenderer> ingredientRendererMap;
	private final ImmutableMap<Class, IIngredientType> ingredientTypeMap;

	private final NonNullList<ItemStack> fuels = NonNullList.create();
	private final NonNullList<ItemStack> potionIngredients = NonNullList.create();

	public IngredientRegistry(
		IModIdHelper modIdHelper,
		IngredientBlacklistInternal blacklist,
		Map<IIngredientType, IngredientSet> ingredientsMap,
		ImmutableMap<IIngredientType, IIngredientHelper> ingredientHelperMap,
		ImmutableMap<IIngredientType, IIngredientRenderer> ingredientRendererMap
	) {
		this.modIdHelper = modIdHelper;
		this.blacklist = blacklist;
		this.ingredientsMap = ingredientsMap;
		this.ingredientHelperMap = ingredientHelperMap;
		this.ingredientRendererMap = ingredientRendererMap;
		ImmutableMap.Builder<Class, IIngredientType> ingredientTypeBuilder = ImmutableMap.builder();
		for (IIngredientType ingredientType : ingredientsMap.keySet()) {
			ingredientTypeBuilder.put(ingredientType.getIngredientClass(), ingredientType);
		}
		this.ingredientTypeMap = ingredientTypeBuilder.build();

		for (ItemStack itemStack : getAllIngredients(VanillaTypes.ITEM)) {
			getStackProperties(itemStack);
		}
	}

	private void getStackProperties(ItemStack itemStack) {
		try {
			if (TileEntityFurnace.isItemFuel(itemStack)) {
				fuels.add(itemStack);
			}
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
			Log.get().error("Failed to check if item is fuel {}.", itemStackInfo, e);
		}

		try {
			if (PotionHelper.isReagent(itemStack)) {
				potionIngredients.add(itemStack);
			}
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
			Log.get().error("Failed to check if item is a potion ingredient {}.", itemStackInfo, e);
		}
	}

	@Override
	@Deprecated
	public <V> List<V> getIngredients(Class<V> ingredientClass) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");
		IIngredientType<V> ingredientType = getIngredientType(ingredientClass);

		@SuppressWarnings("unchecked")
		IngredientSet<V> ingredients = ingredientsMap.get(ingredientType);
		if (ingredients == null) {
			return ImmutableList.of();
		} else {
			return ImmutableList.copyOf(ingredients);
		}
	}

	@Override
	public <V> Collection<V> getAllIngredients(IIngredientType<V> ingredientType) {
		@SuppressWarnings("unchecked")
		IngredientSet<V> ingredients = ingredientsMap.get(ingredientType);
		if (ingredients == null) {
			return Collections.emptySet();
		} else {
			return Collections.unmodifiableCollection(ingredients);
		}
	}

	@Nullable
	public <V> V getIngredientByUid(IIngredientType<V> ingredientType, String uid) {
		@SuppressWarnings("unchecked")
		IngredientSet<V> ingredients = ingredientsMap.get(ingredientType);
		if (ingredients == null) {
			return null;
		} else {
			return ingredients.getByUid(uid);
		}
	}

	@Override
	@Deprecated
	public <V> Collection<V> getAllIngredients(Class<V> ingredientClass) {
		IIngredientType<V> ingredientType = getIngredientType(ingredientClass);
		return getAllIngredients(ingredientType);
	}

	public <V> boolean isValidIngredient(V ingredient) {
		try {
			IIngredientHelper<V> ingredientHelper = getIngredientHelper(ingredient);
			return ingredientHelper.isValidIngredient(ingredient);
		} catch (RuntimeException ignored) {
			return false;
		}
	}

	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		IIngredientType<V> ingredientType = getIngredientType(ingredient);
		return getIngredientHelper(ingredientType);
	}

	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(IIngredientType<V> ingredientType) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		@SuppressWarnings("unchecked")
		IIngredientHelper<V> ingredientHelper = ingredientHelperMap.get(ingredientType);
		if (ingredientHelper != null) {
			return ingredientHelper;
		}
		throw new IllegalArgumentException("Unknown ingredient type: " + ingredientType);
	}

	@Override
	@Deprecated
	public <V> IIngredientHelper<V> getIngredientHelper(Class<? extends V> ingredientClass) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");
		IIngredientType<V> ingredientType = getIngredientType(ingredientClass);
		return getIngredientHelper(ingredientType);
	}

	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		IIngredientType<V> ingredientType = getIngredientType(ingredient);
		return getIngredientRenderer(ingredientType);
	}

	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(IIngredientType<V> ingredientType) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		@SuppressWarnings("unchecked")
		IIngredientRenderer<V> ingredientRenderer = ingredientRendererMap.get(ingredientType);
		if (ingredientRenderer == null) {
			throw new IllegalArgumentException("Could not find ingredient renderer for " + ingredientType);
		}
		return ingredientRenderer;
	}

	@Override
	@Deprecated
	public <V> IIngredientRenderer<V> getIngredientRenderer(Class<? extends V> ingredientClass) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");
		IIngredientType<V> ingredientType = getIngredientType(ingredientClass);
		return getIngredientRenderer(ingredientType);
	}

	@Override
	public Collection<IIngredientType> getRegisteredIngredientTypes() {
		return ingredientTypeMap.values();
	}

	@Override
	@Deprecated
	public Collection<Class> getRegisteredIngredientClasses() {
		return ingredientTypeMap.keySet();
	}

	@Override
	public List<ItemStack> getFuels() {
		return Collections.unmodifiableList(fuels);
	}

	@Override
	public List<ItemStack> getPotionIngredients() {
		return Collections.unmodifiableList(potionIngredients);
	}

	@Override
	public <V> void addIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients) {
		addIngredientsAtRuntime(ingredientType, ingredients, Internal.getIngredientFilter());
	}

	@Override
	@Deprecated
	public <V> void addIngredientsAtRuntime(Class<V> ingredientClass, List<V> ingredients) {
		IIngredientType<V> ingredientType = getIngredientType(ingredientClass);
		addIngredientsAtRuntime(ingredientType, ingredients);
	}

	@Override
	@Deprecated
	public <V> void addIngredientsAtRuntime(Class<V> ingredientClass, Collection<V> ingredients) {
		IIngredientType<V> ingredientType = getIngredientType(ingredientClass);
		addIngredientsAtRuntime(ingredientType, ingredients);
	}

	public <V> void addIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients, IngredientFilter ingredientFilter) {
		ErrorUtil.assertMainThread();
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");

		Collection<EnchantmentData> enchantmentData = hack_getBookEnchantmentData(ingredientType, ingredients);
		if (!enchantmentData.isEmpty()) {
			addIngredientsAtRuntime(VanillaTypes.ENCHANT, enchantmentData, ingredientFilter);
			if (ingredients.isEmpty()) {
				return;
			}
		}

		Log.get().info("Ingredients are being added at runtime: {} {}", ingredients.size(), ingredientType.getIngredientClass().getName());

		IIngredientHelper<V> ingredientHelper = getIngredientHelper(ingredientType);
		//noinspection unchecked
		Set<V> set = ingredientsMap.computeIfAbsent(ingredientType, k -> IngredientSet.create(ingredientType, ingredientHelper));
		for (V ingredient : ingredients) {
			set.add(ingredient);
			if (ingredient instanceof ItemStack) {
				getStackProperties((ItemStack) ingredient);
			}
		}

		NonNullList<IIngredientListElement<V>> ingredientListElements = IngredientListElementFactory.createList(this, ingredientType, ingredients, modIdHelper);
		for (IIngredientListElement<V> element : ingredientListElements) {
			List<IIngredientListElement<V>> matchingElements = ingredientFilter.findMatchingElements(element);
			if (!matchingElements.isEmpty()) {
				for (IIngredientListElement<V> matchingElement : matchingElements) {
					blacklist.removeIngredientFromBlacklist(matchingElement.getIngredient(), ingredientHelper);
					ingredientFilter.updateHiddenState(matchingElement);
				}
				if (Config.isDebugModeEnabled()) {
					Log.get().debug("Updated ingredient: {}", ingredientHelper.getErrorInfo(element.getIngredient()));
				}
			} else {
				blacklist.removeIngredientFromBlacklist(element.getIngredient(), ingredientHelper);
				ingredientFilter.addIngredient(element);
				if (Config.isDebugModeEnabled()) {
					Log.get().debug("Added ingredient: {}", ingredientHelper.getErrorInfo(element.getIngredient()));
				}
			}
		}
		ingredientFilter.invalidateCache();
	}

	@Override
	@Deprecated
	public <V> void removeIngredientsAtRuntime(Class<V> ingredientClass, List<V> ingredients) {
		IIngredientType<V> ingredientType = getIngredientType(ingredientClass);
		removeIngredientsAtRuntime(ingredientType, ingredients, Internal.getIngredientFilter());
	}

	@Override
	@Deprecated
	public <V> void removeIngredientsAtRuntime(Class<V> ingredientClass, Collection<V> ingredients) {
		IIngredientType<V> ingredientType = getIngredientType(ingredientClass);
		removeIngredientsAtRuntime(ingredientType, ingredients, Internal.getIngredientFilter());
	}

	@Override
	public <V> void removeIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients) {
		removeIngredientsAtRuntime(ingredientType, ingredients, Internal.getIngredientFilter());
	}

	@Override
	public <V> IIngredientType<V> getIngredientType(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		@SuppressWarnings("unchecked")
		Class<? extends V> ingredientClass = (Class<? extends V>) ingredient.getClass();
		return getIngredientType(ingredientClass);
	}

	@Override
	public <V> IIngredientType<V> getIngredientType(Class<? extends V> ingredientClass) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");
		@SuppressWarnings("unchecked")
		IIngredientType<V> ingredientType = this.ingredientTypeMap.get(ingredientClass);
		if (ingredientType != null) {
			return ingredientType;
		}
		for (IIngredientType<?> type : ingredientTypeMap.values()) {
			if (type.getIngredientClass().isAssignableFrom(ingredientClass)) {
				@SuppressWarnings("unchecked")
				IIngredientType<V> castType = (IIngredientType<V>) type;
				return castType;
			}
		}
		throw new IllegalArgumentException("Unknown ingredient class: " + ingredientClass);
	}

	public <V> void removeIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients, IngredientFilter ingredientFilter) {
		ErrorUtil.assertMainThread();
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");

		Collection<EnchantmentData> enchantmentData = hack_getBookEnchantmentData(ingredientType, ingredients);
		if (!enchantmentData.isEmpty()) {
			removeIngredientsAtRuntime(VanillaTypes.ENCHANT, enchantmentData, ingredientFilter);
			if (ingredients.isEmpty()) {
				return;
			}
		}

		Log.get().info("Ingredients are being removed at runtime: {} {}", ingredients.size(), ingredientType.getIngredientClass().getName());

		@SuppressWarnings("unchecked")
		IngredientSet<V> set = ingredientsMap.get(ingredientType);
		if (set != null) {
			set.removeAll(ingredients);
		}

		IIngredientHelper<V> ingredientHelper = getIngredientHelper(ingredientType);

		NonNullList<IIngredientListElement<V>> ingredientListElements = IngredientListElementFactory.createList(this, ingredientType, ingredients, modIdHelper);
		for (IIngredientListElement<V> element : ingredientListElements) {
			List<IIngredientListElement<V>> matchingElements = ingredientFilter.findMatchingElements(element);
			if (matchingElements.isEmpty()) {
				V ingredient = element.getIngredient();
				String errorInfo = ingredientHelper.getErrorInfo(ingredient);
				Log.get().error("Could not find any matching ingredients to remove: {}", errorInfo);
			} else if (Config.isDebugModeEnabled()) {
				Log.get().debug("Removed ingredient: {}", ingredientHelper.getErrorInfo(element.getIngredient()));
			}
			for (IIngredientListElement<V> matchingElement : matchingElements) {
				blacklist.addIngredientToBlacklist(matchingElement.getIngredient(), ingredientHelper);
				matchingElement.setVisible(false);
			}
		}
		ingredientFilter.invalidateCache();
	}

	public <V> boolean isIngredientVisible(V ingredient, IngredientFilter ingredientFilter) {
		IIngredientType<V> ingredientType = getIngredientType(ingredient);
		IIngredientListElement<V> element = IngredientListElementFactory.createUnorderedElement(this, ingredientType, ingredient, modIdHelper);
		if (element == null) {
			return false;
		}
		List<IIngredientListElement<V>> matchingElements = ingredientFilter.findMatchingElements(element);
		if (matchingElements.isEmpty()) {
			return true;
		}
		for (IIngredientListElement matchingElement : matchingElements) {
			if (matchingElement.isVisible()) {
				return true;
			}
		}
		return false;
	}

	private <V> Collection<EnchantmentData> hack_getBookEnchantmentData(IIngredientType<V> ingredientType, Collection<V> ingredients) {
		if (ingredientType == VanillaTypes.ITEM) {
			List<EnchantmentData> enchantmentData = new ArrayList<>();
			for (Iterator<V> iterator = ingredients.iterator(); iterator.hasNext(); ) {
				V ingredient = iterator.next();
				ItemStack itemStack = VanillaTypes.ITEM.getIngredientClass().cast(ingredient);
				EnchantmentData bookEnchantmentData = getBookEnchantmentData(itemStack);
				if (bookEnchantmentData != null) {
					enchantmentData.add(bookEnchantmentData);
					iterator.remove();
				}
			}
			return enchantmentData;
		}
		return Collections.emptyList();
	}

	@Nullable
	private EnchantmentData getBookEnchantmentData(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item instanceof ItemEnchantedBook) {
			NBTTagList enchantments = ItemEnchantedBook.getEnchantments(itemStack);
			return getBookEnchantmentData(enchantments);
		}
		return null;
	}

	@Nullable
	private EnchantmentData getBookEnchantmentData(NBTTagList enchantments) {
		EnchantmentData bookEnchantment = null;
		for (NBTBase nbt : enchantments) {
			if (nbt instanceof NBTTagCompound) {
				NBTTagCompound nbttagcompound = (NBTTagCompound) nbt;
				int id = nbttagcompound.getShort("id");
				int level = nbttagcompound.getShort("lvl");
				Enchantment enchantment = Enchantment.getEnchantmentByID(id);
				if (enchantment != null && level > 0) {
					if (bookEnchantment == null) {
						bookEnchantment = new EnchantmentData(enchantment, level);
					} else {
						// 2+ enchantments can't be translated
						return null;
					}
				}
			}
		}
		return bookEnchantment;
	}
}
