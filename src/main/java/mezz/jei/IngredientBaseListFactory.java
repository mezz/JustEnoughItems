package mezz.jei;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import mezz.jei.api.IItemBlacklist;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.util.IngredientListElement;
import mezz.jei.util.Java6Helper;
import mezz.jei.util.Log;
import mezz.jei.util.ModIdUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;

public class IngredientBaseListFactory {
	private IngredientBaseListFactory() {

	}

	public static ImmutableList<IIngredientListElement> create(IIngredientRegistry ingredientRegistry) {
		IngredientChecker ingredientChecker = new IngredientChecker();

		List<IIngredientListElement> ingredientListElements = new LinkedList<IIngredientListElement>();

		for (Class ingredientClass : ingredientRegistry.getRegisteredIngredientClasses()) {
			addToBaseList(ingredientListElements, ingredientRegistry, ingredientChecker, ingredientClass);
		}

		for (Multiset.Entry<Item> brokenItem : ingredientChecker.getBrokenItems().entrySet()) {
			int count = brokenItem.getCount();
			if (count > 1) {
				Item item = brokenItem.getElement();
				IIngredientHelper<ItemStack> ingredientHelper = ingredientRegistry.getIngredientHelper(ItemStack.class);
				String modId = ingredientHelper.getModId(new ItemStack(item));
				String modName = Internal.getHelpers().getModIdUtil().getModNameForModId(modId);
				Log.error("Couldn't get ItemModel for {} item {}. Suppressed {} similar errors.", modName, item, count);
			}
		}

		sortIngredientListElements(ingredientListElements);
		return ImmutableList.copyOf(ingredientListElements);
	}

	private static <V> List<IIngredientListElement> addToBaseList(List<IIngredientListElement> baseList, IIngredientRegistry ingredientRegistry, IngredientChecker ingredientChecker, Class<V> ingredientClass) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientClass);
		IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredientClass);

		for (V ingredient : ingredientRegistry.getIngredients(ingredientClass)) {
			if (ingredient != null) {
				if (!ingredientChecker.isIngredientHidden(ingredient, ingredientHelper)) {
					IngredientListElement<V> ingredientListElement = IngredientListElement.create(ingredient, ingredientHelper, ingredientRenderer);
					if (ingredientListElement != null) {
						baseList.add(ingredientListElement);
					}
				}
			}
		}

		return baseList;
	}

	private static void sortIngredientListElements(List<IIngredientListElement> ingredientListElements) {
		int index = 0;
		final Map<String, Integer> itemAddedOrder = new HashMap<String, Integer>();
		for (IIngredientListElement ingredientListElement : ingredientListElements) {
			String uid = getWildcardUid(ingredientListElement);
			if (!itemAddedOrder.containsKey(uid)) {
				itemAddedOrder.put(uid, index);
				index++;
			}
		}

		Collections.sort(ingredientListElements, new Comparator<IIngredientListElement>() {
			@Override
			public int compare(IIngredientListElement o1, IIngredientListElement o2) {
				final String modName1 = getModName(o1);
				final String modName2 = getModName(o2);

				if (modName1.equals(modName2)) {
					boolean isItemStack1 = (o1.getIngredient() instanceof ItemStack);
					boolean isItemStack2 = (o2.getIngredient() instanceof ItemStack);
					if (isItemStack1 && !isItemStack2) {
						return -1;
					} else if (!isItemStack1 && isItemStack2) {
						return 1;
					}

					final String uid1 = getWildcardUid(o1);
					final String uid2 = getWildcardUid(o2);

					final int orderIndex1 = itemAddedOrder.get(uid1);
					final int orderIndex2 = itemAddedOrder.get(uid2);
					return Java6Helper.compare(orderIndex1, orderIndex2);
				} else if (modName1.equals(Constants.minecraftModName)) {
					return -1;
				} else if (modName2.equals(Constants.minecraftModName)) {
					return 1;
				} else {
					return modName1.compareTo(modName2);
				}
			}
		});
	}

	private static <V> String getModName(IIngredientListElement<V> ingredientListElement) {
		V ingredient = ingredientListElement.getIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientListElement.getIngredientHelper();
		String modId = ingredientHelper.getModId(ingredient);
		return Internal.getHelpers().getModIdUtil().getModNameForModId(modId);
	}

	private static <V> String getWildcardUid(IIngredientListElement<V> ingredientListElement) {
		V ingredient = ingredientListElement.getIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientListElement.getIngredientHelper();
		return ingredientHelper.getWildcardId(ingredient);
	}

	private static class IngredientChecker {
		private final IItemBlacklist itemBlacklist;
		private final ItemModelMesher itemModelMesher;
		private final IBakedModel missingModel;
		private final Multiset<Item> brokenItems = HashMultiset.create();

		public IngredientChecker() {
			itemBlacklist = Internal.getHelpers().getItemBlacklist();
			itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
			missingModel = itemModelMesher.getModelManager().getMissingModel();
		}

		public <V> boolean isIngredientHidden(V ingredient, IIngredientHelper<V> ingredientHelper) {
			if (isIngredientHiddenByBlacklist(ingredient, ingredientHelper)) {
				return true;
			}

			if (ingredient instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) ingredient;
				Item item = itemStack.getItem();
				if (brokenItems.contains(item)) {
					return true;
				}

				final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
				final IBakedModel itemModel;
				try {
					itemModel = renderItem.getItemModelWithOverrides(itemStack, null, null);
				} catch (RuntimeException e) {
					ModIdUtil modIdUtil = Internal.getHelpers().getModIdUtil();
					String modName = modIdUtil.getModNameForIngredient(itemStack);
					String stackInfo = ingredientHelper.getErrorInfo(ingredient);
					Log.error("Couldn't get ItemModel for {} itemStack {}", modName, stackInfo, e);
					brokenItems.add(item);
					return true;
				} catch (LinkageError e) {
					ModIdUtil modIdUtil = Internal.getHelpers().getModIdUtil();
					String modName = modIdUtil.getModNameForIngredient(itemStack);
					String stackInfo = ingredientHelper.getErrorInfo(ingredient);
					Log.error("Couldn't get ItemModel for {} itemStack {}", modName, stackInfo, e);
					brokenItems.add(item);
					return true;
				}

				if (Config.isHideMissingModelsEnabled()) {
					if (itemModel == null || itemModel == missingModel) {
						return true;
					}
				}

				// Game freezes when loading player skulls, see https://bugs.mojang.com/browse/MC-65587
				if (item instanceof ItemSkull && itemStack.getMetadata() == 3) {
					return true;
				}
			}

			return false;
		}

		public Multiset<Item> getBrokenItems() {
			return brokenItems;
		}

		private <V> boolean isIngredientHiddenByBlacklist(V ingredient, IIngredientHelper<V> ingredientHelper) {
			try {
				if (ingredient instanceof ItemStack) {
					// check if it is blacklisted through the API or Config
					if (!itemBlacklist.isItemBlacklisted((ItemStack) ingredient)) {
						return false;
					} else if (Config.isEditModeEnabled()) {
						// edit mode can only change the config blacklist, not things blacklisted through the API
						return !Config.isIngredientOnConfigBlacklist(ingredient);
					}
				} else {
					if (!Config.isIngredientOnConfigBlacklist(ingredient) || Config.isEditModeEnabled()) {
						return false;
					}
				}
			} catch (RuntimeException e) {
				String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
				Log.error("Could not check blacklist for ingredient {}", ingredientInfo, e);
			}

			return true;
		}
	}
}
