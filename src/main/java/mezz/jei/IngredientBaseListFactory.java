package mezz.jei;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.util.IngredientListElement;
import mezz.jei.util.Java6Helper;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.common.ProgressManager;

public class IngredientBaseListFactory {
	private IngredientBaseListFactory() {

	}

	public static ImmutableList<IIngredientListElement> create(boolean showProgressBar) {
		Log.info("Building item filter...");
		long start_time = System.currentTimeMillis();

		IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		JeiHelpers jeiHelpers = Internal.getHelpers();
		IngredientChecker ingredientChecker = new IngredientChecker(jeiHelpers);

		List<IIngredientListElement> ingredientListElements = new LinkedList<IIngredientListElement>();

		for (Class ingredientClass : ingredientRegistry.getRegisteredIngredientClasses()) {
			addToBaseList(ingredientListElements, ingredientRegistry, ingredientChecker, ingredientClass, showProgressBar);
		}

		sortIngredientListElements(ingredientListElements);
		ImmutableList<IIngredientListElement> immutableElements = ImmutableList.copyOf(ingredientListElements);

		Log.info("Built	item filter in {} ms", System.currentTimeMillis() - start_time);
		return immutableElements;
	}

	private static <V> void addToBaseList(List<IIngredientListElement> baseList, IIngredientRegistry ingredientRegistry, IngredientChecker ingredientChecker, Class<V> ingredientClass, final boolean showProgressBar) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientClass);
		IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredientClass);

		ImmutableList<V> ingredients = ingredientRegistry.getIngredients(ingredientClass);
		final int ingredientCount = ingredients.size();
		if (ingredientCount <= 0) {
			return;
		}
		final int steps = 100;
		ProgressManager.ProgressBar bar = null;
		if (showProgressBar) {
			bar = ProgressManager.push("Adding " + ingredientClass.getSimpleName() + " ingredients.", steps);
			SplashProgress.pause();
		}
		int count = 0;
		for (V ingredient : ingredients) {
			if (ingredient != null) {
				if (!ingredientChecker.isIngredientHidden(ingredient, ingredientHelper)) {
					IngredientListElement<V> ingredientListElement = IngredientListElement.create(ingredient, ingredientHelper, ingredientRenderer);
					if (ingredientListElement != null) {
						baseList.add(ingredientListElement);
					}
				}
			}
			// invariant: progressBar.getStep() * ingredientCount >= count at the end of the cycle
			// at the end: count = steps * ingredientCount, therefore bar.step() would be called exactly steps times
			count += steps;
			while (bar != null && (count > bar.getStep() * ingredientCount)) {
				SplashProgress.resume();
				bar.step("" + count / ingredientCount + "%");
				SplashProgress.pause();
			}
		}
		if (bar != null) {
			SplashProgress.resume();
			ProgressManager.pop(bar);
		}
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
		return Internal.getModIdUtil().getModNameForModId(modId);
	}

	private static <V> String getWildcardUid(IIngredientListElement<V> ingredientListElement) {
		V ingredient = ingredientListElement.getIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientListElement.getIngredientHelper();
		return ingredientHelper.getWildcardId(ingredient);
	}

	private static class IngredientChecker {
		private final IngredientBlacklist ingredientBlacklist;

		public IngredientChecker(JeiHelpers jeiHelpers) {
			ingredientBlacklist = jeiHelpers.getIngredientBlacklist();
		}

		public <V> boolean isIngredientHidden(V ingredient, IIngredientHelper<V> ingredientHelper) {
			try {
				if(ingredientBlacklist.isIngredientBlacklistedByApi(ingredient)) {
					return true;
				}

				if (!Config.isEditModeEnabled() && Config.isIngredientOnConfigBlacklist(ingredient, ingredientHelper)) {
					return true;
				}
			} catch (RuntimeException e) {
				String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
				Log.error("Could not check blacklist for ingredient {}", ingredientInfo, e);
				return true;
			}

			return false;
		}
	}
}
