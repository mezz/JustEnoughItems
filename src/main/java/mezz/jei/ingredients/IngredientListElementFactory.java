package mezz.jei.ingredients;

import javax.annotation.Nullable;
import java.util.Collection;

import net.minecraftforge.fml.common.ProgressManager;
import net.minecraft.util.NonNullList;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.startup.IModIdHelper;
import mezz.jei.util.Log;

import com.google.common.base.Stopwatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class IngredientListElementFactory {
	private static final IngredientOrderTracker ORDER_TRACKER = new IngredientOrderTracker();

	private IngredientListElementFactory() {
	}

	public static NonNullList<IIngredientListElement> createBaseList(IIngredientRegistry ingredientRegistry, IModIdHelper modIdHelper) {
		NonNullList<IIngredientListElement> ingredientListElements = NonNullList.create();

		for (IIngredientType<?> ingredientType : ingredientRegistry.getRegisteredIngredientTypes()) {
			addToBaseList(ingredientListElements, ingredientRegistry, ingredientType, modIdHelper);
		}
		
		//PerformanceEvaluation(ingredientListElements);
		HashSet<String> IngredientClasses = new HashSet<String>();
		for (IIngredientListElement ingredient: ingredientListElements) {
			IngredientClasses.add(ingredient.getIngredient().getClass().getName());
		}

		Log.get().info("List of classes found: ");
		Log.get().info(String.join(", ", IngredientClasses));
		
		try {
			ingredientListElements.sort(new IngredientListElementComparator());
		} catch (Exception ex) {
			if (Config.isDebugModeEnabled()) {
				//If you are developing a new sorting option, you probably want it to stay stopped to see what it did. 
				Log.get().error("Item sorting failed.  Aborting sort.", ex);
				TheHuntForTheOffendingItems(ingredientListElements);
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
	
	public static void TheHuntForTheOffendingItems(List<IIngredientListElement> ingredientListElements) {
		NonNullList<IIngredientListElement> listSet = NonNullList.create();
		for (IIngredientListElement e: ingredientListElements)
			listSet.add(e);
		TheHuntForTheOffendingItems(listSet);
	}
	
	//Or More accurately, "The search for test data that your comparator doesn't handle correctly."
	public static void TheHuntForTheOffendingItems(NonNullList<IIngredientListElement> ingredientListElements) {
		Log.get().info("The full List has " + ingredientListElements.size() + " entries.");
		
		NonNullList<IIngredientListElement> smallestError = BinaryErrorSearch(ingredientListElements);
				
		if (smallestError.size() > 100) {
			//Try again after shuffling and see if we get a smaller list?
			Collections.shuffle(smallestError);
			smallestError = BinaryErrorSearch(smallestError);
		}
		
		//This is to keep it from taking forever, and is just hope.
		if (smallestError.size() > 100) {
			Log.get().info("Smallest Error List is " + smallestError.size() + " truncating down to 100 items.");
			smallestError = SplitList(smallestError, 0, 100);
		}
		
		if (smallestError.size() == 0) {
			Log.get().info("Smallest Error List is empty.  We somehow lost the error situation.");
			return;
		}	
		
		Log.get().info("Smallest Error List has " + smallestError.size() + " entries.");
		ListScanner(smallestError);
	}
	
	private static NonNullList<IIngredientListElement> BinaryErrorSearch(NonNullList<IIngredientListElement> ingredientListElements) {
		int midpoint = ingredientListElements.size() / 2;
		NonNullList<IIngredientListElement> leftList = SplitList(ingredientListElements, 0, midpoint);
		NonNullList<IIngredientListElement> rightList = SplitList(ingredientListElements, midpoint, ingredientListElements.size());
		boolean leftClean = TestSort(leftList);
		boolean rightClean = TestSort(rightList);
		leftList = null;
		rightList = null;

		//We are trying to get the smallest set, so if both are dirty, we'll stick with the left set.
		if (!leftClean) {
			//Reset the list order so the error can continue
			leftList = SplitList(ingredientListElements, 0, midpoint);
			if (ingredientListElements.size() < 101)
				return leftList;
			return BinaryErrorSearch(leftList);
		} else if (!rightClean) {
			//Reset the list order so the error can continue
			rightList = SplitList(ingredientListElements, midpoint, ingredientListElements.size());
			if (ingredientListElements.size() < 101)
				return rightList;
			return BinaryErrorSearch(rightList);
		}
		
		//We didn't find any, so chop the array up a bit and try that.
		return HexthErrorSearch(ingredientListElements);
	}
	
	private static NonNullList<IIngredientListElement> HexthErrorSearch(NonNullList<IIngredientListElement> ingredientListElements) {
		if (ingredientListElements.size() < 6)
			return ingredientListElements;

		int hexthPoint = ingredientListElements.size() / 6;
		List<List<IIngredientListElement>> hexthList = new ArrayList<List<IIngredientListElement>>();
		
		for (int i = 0; i < 5; i++)
			hexthList.add(SplitList(ingredientListElements, i * hexthPoint, (i + 1) * hexthPoint));
		//The last gets the remainder.
		hexthList.add(SplitList(ingredientListElements, 5 * hexthPoint, ingredientListElements.size()));
		
		for (int left = 0; left < 4; left++) {
			for(int middle = left + 1; middle < 5; middle++) {
				for(int right = middle + 1; right < 6; right++) {
					NonNullList<IIngredientListElement> testList = NonNullList.create();
					testList.addAll(hexthList.get(left));
					testList.addAll(hexthList.get(middle));
					testList.addAll(hexthList.get(right));
					if (!TestSort(testList)) {
						//We have a smaller error.
						testList.clear();
						testList.addAll(hexthList.get(left));
						testList.addAll(hexthList.get(middle));
						testList.addAll(hexthList.get(right));
						hexthList = null;
						if (testList.size() < 101)
							return testList;
						return BinaryErrorSearch(testList);
					}
				}
			}
		}
		//We didn't find any, so return the input set.  This is now our smallest set.
		return ingredientListElements;
	}

	private static boolean TestSort(NonNullList<IIngredientListElement> ingredientListElements) {
		try {
			ingredientListElements.sort(new IngredientListElementComparator());
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
	
	private static NonNullList<IIngredientListElement> SplitList(NonNullList<IIngredientListElement> ingredientListElements, int start, int end) {
		NonNullList<IIngredientListElement> newList = NonNullList.create();
		if (start < 0) start = 0;
		if (end > ingredientListElements.size()) end = ingredientListElements.size();
		for (int i = start; i < end; i++)
		{
			newList.add(ingredientListElements.get(i));
		}
		return newList;
	}
	
	public static void ListScanner(NonNullList<IIngredientListElement> ingredientListElements) {
		IngredientListElementComparator instance = new IngredientListElementComparator();
		for (int i = 0; i < ingredientListElements.size(); i+=1) {
			IIngredientListElement<?> ingredientI = ingredientListElements.get(i);
			for (int j = i + 1; j < ingredientListElements.size(); j+=1)	{
				IIngredientListElement<?> ingredientJ = ingredientListElements.get(j);
				int ijComp = instance.compare(ingredientI, ingredientJ);
				int jiComp = instance.compare(ingredientJ, ingredientI);
				if (ijComp + jiComp != 0) {
					Log.get().error("Ingredients do not have reflective property in sorting.");
					Log.get().error(ingredientI.getResourceId());
					Log.get().error(ingredientJ.getResourceId());
					Log.get().error(ijComp + " vs " + jiComp);
					instance.debugCompare(ingredientI, ingredientJ);
					instance.debugCompare(ingredientJ, ingredientI);
					Log.get().error("-------------------");
				}
				for (int k = j + 1; k < ingredientListElements.size(); k+=1)	{
					IIngredientListElement<?> ingredientK = ingredientListElements.get(k);
					int ikComp = instance.compare(ingredientI, ingredientK);
					int jkComp = instance.compare(ingredientJ, ingredientK);
					int kiComp = instance.compare(ingredientK, ingredientI);
					int kjComp = instance.compare(ingredientK, ingredientJ);
					boolean resOk = true;
					
					//Transitivity: a < b and b < c then a < c
					//Transitivity: a > b and b > c then a > c
					
					// a = i, b = j, c = k 
					// i > j and j > k then i > k  kji
					if (ijComp > 0 && jkComp > 0 && ikComp <=0 ) resOk = false;
					// i < j and j < k then i < k  ijk
					if (ijComp < 0 && jkComp < 0 && ikComp >=0 ) resOk = false;
					
					// a = j, b = i, c = k 
					// j > i and i > k then j > k  kij
					if (jiComp > 0 && ikComp > 0 && jkComp <=0 ) resOk = false;
					// j < i and i < k then j < k  jik
					if (jiComp < 0 && ikComp < 0 && jkComp >=0 ) resOk = false;

					// a = i, b = k, c = j 
					// i > k and k > j then i > j  jki
					if (ikComp > 0 && kjComp > 0 && ijComp <=0 ) resOk = false;
					// i < k and k < j then i < j  ikj
					if (ikComp < 0 && kjComp < 0 && ijComp >=0 ) resOk = false;

					if (!resOk) {
						Log.get().error("Ingredients do not have transitive property in sorting.");
						Log.get().error(ingredientI.getResourceId());
						Log.get().error(ingredientJ.getResourceId());
						Log.get().error(ingredientK.getResourceId());
						Log.get().error(ijComp + " vs " + jkComp + " vs " + ikComp);
						instance.debugCompare(ingredientI, ingredientJ);
						instance.debugCompare(ingredientJ, ingredientK);
						instance.debugCompare(ingredientI, ingredientK);
						Log.get().error("-------------------");
					}
				}
			}
		}
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
				int orderIndex = ORDER_TRACKER.getOrderIndex(ingredient, ingredientHelper);
				IngredientListElement<V> ingredientListElement = IngredientListElement.create(ingredient, ingredientHelper, ingredientRenderer, modIdHelper, orderIndex);
				if (ingredientListElement != null) {
					list.add(ingredientListElement);
				}
			}
		}
		return list;
	}

	@Nullable
	public static <V> IIngredientListElement<V> createUnorderedElement(IIngredientRegistry ingredientRegistry, IIngredientType<V> ingredientType, V ingredient, IModIdHelper modIdHelper) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
		IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredientType);
		return IngredientListElement.create(ingredient, ingredientHelper, ingredientRenderer, modIdHelper, 0);
	}

	private static <V> void addToBaseList(NonNullList<IIngredientListElement> baseList, IIngredientRegistry ingredientRegistry, IIngredientType<V> ingredientType, IModIdHelper modIdHelper) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
		IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredientType);

		Collection<V> ingredients = ingredientRegistry.getAllIngredients(ingredientType);
		ProgressManager.ProgressBar progressBar = ProgressManager.push("Registering ingredients: " + ingredientType.getIngredientClass().getSimpleName(), ingredients.size());
		for (V ingredient : ingredients) {
			progressBar.step("");
			if (ingredient != null) {
				int orderIndex = ORDER_TRACKER.getOrderIndex(ingredient, ingredientHelper);
				IngredientListElement<V> ingredientListElement = IngredientListElement.create(ingredient, ingredientHelper, ingredientRenderer, modIdHelper, orderIndex);
				if (ingredientListElement != null) {
					baseList.add(ingredientListElement);
				}
			}
		}
		ProgressManager.pop(progressBar);
	}

}
