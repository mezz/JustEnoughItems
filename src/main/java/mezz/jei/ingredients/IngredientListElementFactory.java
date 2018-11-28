package mezz.jei.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.startup.IModIdHelper;
import mezz.jei.util.Log;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.ProgressManager;

import javax.annotation.Nullable;

import com.google.common.base.Stopwatch;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public final class IngredientListElementFactory {
	private IngredientListElementFactory() {
	}

	public static NonNullList<IIngredientListElement> createBaseList(IIngredientRegistry ingredientRegistry, IModIdHelper modIdHelper) {
		NonNullList<IIngredientListElement> ingredientListElements = NonNullList.create();

		for (IIngredientType<?> ingredientType : ingredientRegistry.getRegisteredIngredientTypes()) {
			addToBaseList(ingredientListElements, ingredientRegistry, ingredientType, modIdHelper);
		}
		
		//PerformanceEvaluation(ingredientListElements);
		
		try {
			ingredientListElements.sort(new IngredientListElementComparator());
		} catch (Exception ex) {
			if (Config.isDebugModeEnabled()) {
				//If you are developing a new sorting option, you probably want it to stay stopped to see what it did. 
				Log.get().error("Item sorting failed.  Aborting sort.", ex);
			} else {
				Log.get().error("Item sorting failed.  Using old method.", ex);
				try {
					ingredientListElements.sort(IngredientListElementClassicComparator.INSTANCE);
				} catch (Exception ex2) {
					Log.get().error("Classic Item sorting failed.  Aborting sort.", ex2);
				}					
			}
		}
		return ingredientListElements;
	}

	private static void PerformanceEvaluation(NonNullList<IIngredientListElement> ingredientListElements) {
		
		//This builds a list of classes that getIngredient can return so mod devs
		//writing a comparator can know what they would see.  Mostly ItemStack and FluidStack.
		HashSet<String> IngredientClasses = new HashSet<String>();
		for (IIngredientListElement ingredient: ingredientListElements) {
			IngredientClasses.add(ingredient.getIngredient().getClass().getName());
		}

		Log.get().info("List of classes found: ");
		Log.get().info(String.join(", ", IngredientClasses));

		PerformanceEvaluation("Classic Sort", ingredientListElements, 
				IngredientListElementClassicComparator.INSTANCE);

		PerformanceEvaluation("New Classic Sort", ingredientListElements, 
				new IngredientListElementComparator("minecraft,mod,default"));

		PerformanceEvaluation("Default Sort", ingredientListElements, 
				new IngredientListElementComparator(IngredientListElementComparator.initConfig()));

		PerformanceEvaluation("Tool Sort", ingredientListElements, 
				new IngredientListElementComparator("tool,default"));

		PerformanceEvaluation("Melee Sort", ingredientListElements, 
				new IngredientListElementComparator("melee,default"));

		PerformanceEvaluation("Armor Sort", ingredientListElements, 
				new IngredientListElementComparator("armor,default"));

		PerformanceEvaluation("Dictionary Sort", ingredientListElements, 
				new IngredientListElementComparator("dictionary,default"));

		PerformanceEvaluation("Minecraft Sort", ingredientListElements, 
				new IngredientListElementComparator("minecraft,default"));

		PerformanceEvaluation("Mod Sort", ingredientListElements, 
				new IngredientListElementComparator("mod,default"));

		PerformanceEvaluation("Name Sort", ingredientListElements, 
				new IngredientListElementComparator("name,default"));

		PerformanceEvaluation("Damage Sort", ingredientListElements, 
				new IngredientListElementComparator("damage,default"));

		PerformanceEvaluation("ID Sort", ingredientListElements, 
				new IngredientListElementComparator("id,default"));

		PerformanceEvaluation("'DefaultOrderComparator' Sort", ingredientListElements, 
				new IngredientListElementComparator("default"));

		/*
			addToolComparator();    tool
			addMeleeComparator();      melee
			addArmorComparator();      armor
			addDictionaryComparator(); dictionary
			addMinecraftComparator();  minecraft
			addModComparator();        mod
			addNameComparator();       name
			addDamageComparator();     damage
			addIdComparator();         id
			addDefaultOrderComparator();  default
		 */

	}
	
	
	private static void PerformanceEvaluation(String sortLabel, NonNullList<IIngredientListElement> ingredientListElements, Comparator<IIngredientListElement> comparator) {
		//Stable starting point.
		ingredientListElements.sort(IngredientListElementClassicComparator.INSTANCE);

		//Do an un-timed initial pass to get passed any first-time run caching.
		Collections.shuffle(ingredientListElements);
		ingredientListElements.sort(comparator);
		
		final Stopwatch stopWatch = Stopwatch.createUnstarted();
		long runs = 100;
		for (int i = 0; i < runs; i++) {
			//We have to shuffle because a sorted list will sort really, really fast.
			Collections.shuffle(ingredientListElements);
			stopWatch.start();
			ingredientListElements.sort(comparator);
			stopWatch.stop();
		}
		Log.get().info(sortLabel + " benchmark took a total of " + stopWatch + " for " + runs + " executions.");
		Log.get().info(sortLabel + " benchmark took an average of " + (stopWatch.elapsed(TimeUnit.MILLISECONDS) / runs) + " ms.");

		//How well does it do when the sort is already sorted?
		stopWatch.reset();
		stopWatch.start();
		for (int i = 0; i < runs; i++) {
			ingredientListElements.sort(comparator);
		}
		stopWatch.stop();
		Log.get().info(sortLabel + " pre-sorted benchmark took a total of " + stopWatch + " for " + runs + " executions.");
		Log.get().info(sortLabel + " pre-sorted benchmark took an average of " + (stopWatch.elapsed(TimeUnit.MILLISECONDS) / runs) + " ms.");

	}

	public static <V> NonNullList<IIngredientListElement<V>> createList(IIngredientRegistry ingredientRegistry, IIngredientType<V> ingredientType, Collection<V> ingredients, IModIdHelper modIdHelper) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
		IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredientType);
		
		NonNullList<IIngredientListElement<V>> list = NonNullList.create();
		for (V ingredient : ingredients) {
			if (ingredient != null) {
				IngredientListElement<V> ingredientListElement = IngredientListElement.create(ingredient, ingredientHelper, ingredientRenderer, modIdHelper);
				if (ingredientListElement != null) {
					list.add(ingredientListElement);
				}
			}
		}
		return list;
	}

	@Nullable
	public static <V> IIngredientListElement<V> createElement(IIngredientRegistry ingredientRegistry, IIngredientType<V> ingredientType, V ingredient, IModIdHelper modIdHelper) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
		IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredientType);
		return IngredientListElement.create(ingredient, ingredientHelper, ingredientRenderer, modIdHelper);
	}

	private static <V> void addToBaseList(NonNullList<IIngredientListElement> baseList, IIngredientRegistry ingredientRegistry, IIngredientType<V> ingredientType, IModIdHelper modIdHelper) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
		IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredientType);

		Collection<V> ingredients = ingredientRegistry.getAllIngredients(ingredientType);
		ProgressManager.ProgressBar progressBar = ProgressManager.push("Registering ingredients: " + ingredientType.getIngredientClass().getSimpleName(), ingredients.size());
		for (V ingredient : ingredients) {
			progressBar.step("");
			if (ingredient != null) {
				IngredientListElement<V> ingredientListElement = IngredientListElement.create(ingredient, ingredientHelper, ingredientRenderer, modIdHelper);
				if (ingredientListElement != null) {
					baseList.add(ingredientListElement);
				}
			}
		}
		ProgressManager.pop(progressBar);
	}

}
