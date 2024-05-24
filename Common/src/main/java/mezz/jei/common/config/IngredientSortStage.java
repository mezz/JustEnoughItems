package mezz.jei.common.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class IngredientSortStage {
	private static final Logger LOGGER = LogManager.getLogger();
	public final String name;
	public final Boolean customStage;

	private IngredientSortStage(String name, Boolean customStage) {
		this.name = name.toUpperCase().trim();
		this.customStage = customStage;
		var existingStage = getStage(name);
		if (existingStage == null) {
			LOGGER.info("Adding new Sort Stage: " + name);
			allStages.add(this);
		} else if (existingStage.customStage) {
			LOGGER.info("Replacing Sort Stage: " + name);
			//Replace the existing one, maybe the comparator is somehow different.
			allStages.remove(existingStage);
			allStages.add(this);
		} else {
			LOGGER.debug("Ignoring Duplicate Sort Stage: " + name);
		}
		//Don't replace our built-in stages.
	}

	private IngredientSortStage(String name) {
		this(name, true);
	}

	private static List<IngredientSortStage> allStages = new ArrayList<IngredientSortStage>(10);

	public static final IngredientSortStage MOD_NAME = new IngredientSortStage("MOD_NAME", false);
	public static final IngredientSortStage INGREDIENT_TYPE = new IngredientSortStage("INGREDIENT_TYPE", false);
	public static final IngredientSortStage ALPHABETICAL = new IngredientSortStage("ALPHABETICAL", false);
	public static final IngredientSortStage CREATIVE_MENU = new IngredientSortStage("CREATIVE_MENU", false);
	public static final IngredientSortStage TAG = new IngredientSortStage("TAG", false);
	public static final IngredientSortStage ARMOR = new IngredientSortStage("ARMOR", false);
	public static final IngredientSortStage MAX_DURABILITY = new IngredientSortStage("MAX_DURABILITY", false);

	public static final List<IngredientSortStage> defaultStages = List.of(
		IngredientSortStage.MOD_NAME,
		IngredientSortStage.INGREDIENT_TYPE,
		IngredientSortStage.CREATIVE_MENU
	);

	public static List<IngredientSortStage> getAllStages() {
		return new ArrayList<IngredientSortStage>(allStages);
	}

	public static IngredientSortStage getOrCreateStage(String name) {
		var stage = getStage(name);
		if (stage == null) {
			stage = new IngredientSortStage(name, true);
		}
		return stage;
	}

	public static IngredientSortStage getStage(String needle) {
		needle = needle.toUpperCase().trim();
		LOGGER.debug("All Sort Stage Count: " + allStages.size());
		LOGGER.debug("Searching for Sort Stage: " + needle);
		for (IngredientSortStage stage : allStages) {
			LOGGER.debug("- Checking Existing Sort Stage: " + stage.name);
			if (stage.name.equals(needle)) {
				LOGGER.debug("- Matched Existing Sort Stage: " + stage.name);
				return stage;
			}
		}
		LOGGER.debug("- Matched No Existing Sort Stage: ");
		return null;
	}

	public static final List<String> defaultStageNames = List.of(
		IngredientSortStage.MOD_NAME.name,
		IngredientSortStage.INGREDIENT_TYPE.name,
		IngredientSortStage.CREATIVE_MENU.name
	);

	public static List<String> getAllStageNames() {
		var names = new ArrayList<String>(allStages.size());
		for(IngredientSortStage stage: allStages) {
			names.add(stage.name);
		}
		return names;
	}

	public IngredientSortStage getBestSelf() {
		if (!customStage) {
			LOGGER.debug(name + " is a standard sort option.");
			return this;
		}
		var otherSelf = getStage(name);
		if (otherSelf != null) {
			LOGGER.debug("Real Sort Stage exists for " + name);
			return otherSelf;
		}
		return this;
	}
}
