package mezz.jei.gui.ingredients;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Table;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.config.Config;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.RecipeClickableArea;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.FileUtil;
import mezz.jei.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

/**
 * Keeps track of what the player was viewing at last time they looked up the recipes or uses of an ingredient.
 */
public class IngredientLookupMemory {
	private final Table<IFocus.Mode, String, IngredientLookupState> mostRecentLookups = HashBasedTable.create();
	private final Table<IFocus.Mode, String, RecipeLookupFromFile> mostRecentLookupsFromFile = HashBasedTable.create();
	private final RecipeRegistry recipeRegistry;
	private final IIngredientRegistry ingredientRegistry;

	private final Object saveLock = new Object();
	private boolean needsSaving = false;
	private boolean saving = false;

	public IngredientLookupMemory(RecipeRegistry recipeRegistry, IIngredientRegistry ingredientRegistry) {
		this.recipeRegistry = recipeRegistry;
		this.ingredientRegistry = ingredientRegistry;
		readFromFile();
	}

	public <T> IngredientLookupState getState(IFocus<T> focus, List<IRecipeCategory> recipeCategories) {
		ErrorUtil.checkNotEmpty(recipeCategories, "recipeCategories");
		focus = Focus.check(focus);

		final IFocus.Mode focusMode = focus.getMode();
		final T value = focus.getValue();
		final IIngredientHelper<T> ingredientHelper = this.ingredientRegistry.getIngredientHelper(value);
		final String uniqueId = ingredientHelper.getUniqueId(value);

		IngredientLookupState state = this.mostRecentLookups.get(focusMode, uniqueId);
		if (state == null) {
			RecipeLookupFromFile stateFromFile = this.mostRecentLookupsFromFile.get(focusMode, uniqueId);
			if (stateFromFile != null) {
				final int categoryIndex = getRecipeCategoryIndex(recipeCategories, stateFromFile.categoryUid);
				if (categoryIndex >= 0) {
					IRecipeCategory<?> recipeCategory = recipeCategories.get(categoryIndex);
					if (stateFromFile.recipeIndex < this.recipeRegistry.getRecipeWrappers(recipeCategory, focus).size()) {
						state = new IngredientLookupState(focus, recipeCategories, categoryIndex, stateFromFile.recipeIndex);
					}
				}
			}
		}

		if (state == null) {
			state = new IngredientLookupState(focus, recipeCategories, 0, 0);
		}

		final Set<Integer> indexesForOpenContainer = getRecipeCategoryIndexesForOpenContainer(state.getRecipeCategories());
		if (!indexesForOpenContainer.isEmpty() && !indexesForOpenContainer.contains(state.getRecipeCategoryIndex())) {
			Integer index = Collections.min(indexesForOpenContainer);
			state.setRecipeCategoryIndex(index);
		}

		if (this.mostRecentLookups.put(focusMode, uniqueId, state) != state) {
			markDirty();
		}
		if (this.mostRecentLookupsFromFile.remove(focusMode, uniqueId) != null) {
			markDirty();
		}

		return state;
	}

	private Set<Integer> getRecipeCategoryIndexesForOpenContainer(List<IRecipeCategory> recipeCategories) {
		final Set<Integer> indexes = new HashSet<>();
		final Minecraft minecraft = Minecraft.getMinecraft();
		final EntityPlayerSP player = minecraft.player;
		if (player != null) {
			final Container container = player.openContainer;
			if (container != null) {
				for (int i = 0; i < recipeCategories.size(); i++) {
					IRecipeCategory recipeCategory = recipeCategories.get(i);
					if (this.recipeRegistry.getRecipeTransferHandler(container, recipeCategory) != null) {
						indexes.add(i);
					}
				}
			}
			GuiScreen screen = minecraft.currentScreen;
			if (screen instanceof RecipesGui) {
				RecipesGui recipesGui = (RecipesGui) screen;
				screen = recipesGui.getParentScreen();
			}
			if (screen instanceof GuiContainer) {
				final GuiContainer guiContainer = (GuiContainer) screen;
				final ImmutableCollection<RecipeClickableArea> clickableAreas = this.recipeRegistry.getAllRecipeClickableAreas(guiContainer);
				final Set<String> recipeCategoryUids = new HashSet<>();
				for (RecipeClickableArea clickableArea : clickableAreas) {
					recipeCategoryUids.addAll(clickableArea.getRecipeCategoryUids());
				}
				for (int i = 0; i < recipeCategories.size(); i++) {
					IRecipeCategory recipeCategory = recipeCategories.get(i);
					if (recipeCategoryUids.contains(recipeCategory.getUid())) {
						indexes.add(i);
					}
				}
			}
		}

		return indexes;
	}

	private int getRecipeCategoryIndex(List<IRecipeCategory> recipeCategories, String recipeCategoryUid) {
		for (int i = 0; i < recipeCategories.size(); i++) {
			IRecipeCategory recipeCategory = recipeCategories.get(i);
			if (recipeCategory.getUid().equals(recipeCategoryUid)) {
				return i;
			}
		}
		return -1;
	}

	private void readFromFile() {
		final File file = new File(Config.getJeiConfigurationDir(), "lookupHistory.zip");
		FileUtil.readZipFileSafely(file, "lookupHistory.json", zipInputStream -> {
			final JsonReader jsonReader = new JsonReader(new InputStreamReader(zipInputStream));
			jsonReader.beginObject();

			while (jsonReader.hasNext()) {
				final String name = jsonReader.nextName();
				if (name.equals("lookupStates")) {
					readLookupStatesObject(jsonReader);
				}
			}

			jsonReader.endObject();
		});
	}

	private void readLookupStatesObject(JsonReader jsonReader) throws IOException {
		jsonReader.beginObject();
		while (jsonReader.hasNext()) {
			final String focusModeName = jsonReader.nextName();
			final IFocus.Mode focusMode = IFocus.Mode.valueOf(focusModeName);
			jsonReader.beginArray();
			while (jsonReader.hasNext()) {
				jsonReader.beginArray();
				String ingredientString = jsonReader.nextString();
				String category = jsonReader.nextString();
				int recipeIndex = jsonReader.nextInt();
				jsonReader.endArray();

				RecipeLookupFromFile recipeLookupFromFile = new RecipeLookupFromFile(category, recipeIndex);
				this.mostRecentLookupsFromFile.put(focusMode, ingredientString, recipeLookupFromFile);
			}
			jsonReader.endArray();
		}
		jsonReader.endObject();
	}

	public void markDirty() {
		this.needsSaving = true;
	}

	public void saveToFile() {
		synchronized (this.saveLock) {
			if (this.needsSaving && !this.saving) {
				this.saving = true;
				this.needsSaving = false;
				saveToFileAsync();
			}
		}
	}

	private void saveToFileAsync() {
		final Table<IFocus.Mode, String, IngredientLookupState> mostRecentLookups = HashBasedTable.create(this.mostRecentLookups);
		final Table<IFocus.Mode, String, RecipeLookupFromFile> mostRecentLookupsFromFile = HashBasedTable.create(this.mostRecentLookupsFromFile);
		new Thread(() -> {
			saveToFileSync(mostRecentLookups, mostRecentLookupsFromFile);
			synchronized (saveLock) {
				saving = false;
			}
		}).start();
	}

	private static void saveToFileSync(
			final Table<IFocus.Mode, String, IngredientLookupState> mostRecentLookups,
			final Table<IFocus.Mode, String, RecipeLookupFromFile> mostRecentLookupsFromFile) {
		final File file = new File(Config.getJeiConfigurationDir(), "lookupHistory.zip");

		final boolean write = FileUtil.writeZipFileSafely(file, "lookupHistory.json", zipOutputStream -> {
			JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(zipOutputStream));
			jsonWriter.beginObject();
			{
				writeLookupStatesObject(jsonWriter, mostRecentLookups, mostRecentLookupsFromFile);
			}
			jsonWriter.endObject();
			jsonWriter.flush();
		});

		if (write) {
			Log.get().debug("Saved IngredientLookupMemory to {}.", file.getAbsoluteFile());
		}
	}

	private static void writeLookupStatesObject(
			JsonWriter jsonWriter,
			Table<IFocus.Mode, String, IngredientLookupState> mostRecentLookups,
			Table<IFocus.Mode, String, RecipeLookupFromFile> mostRecentLookupsFromFile
	) throws IOException {
		jsonWriter.name("lookupStates")
				.beginObject();

		for (IFocus.Mode focusMode : IFocus.Mode.values()) {

			jsonWriter.name(focusMode.toString())
					.beginArray();

			for (Map.Entry<String, IngredientLookupState> lookups : mostRecentLookups.row(focusMode).entrySet()) {
				String ingredientString = lookups.getKey();
				IngredientLookupState state = lookups.getValue();
				int recipeCategoryIndex = state.getRecipeCategoryIndex();
				String categoryUid = state.getRecipeCategories().get(recipeCategoryIndex).getUid();

				jsonWriter.beginArray()
						.value(ingredientString)
						.value(categoryUid)
						.value(state.getRecipeIndex())
						.endArray();
			}

			for (Map.Entry<String, IngredientLookupMemory.RecipeLookupFromFile> lookups : mostRecentLookupsFromFile.row(focusMode).entrySet()) {
				String ingredientString = lookups.getKey();
				IngredientLookupMemory.RecipeLookupFromFile state = lookups.getValue();

				jsonWriter.beginArray()
						.value(ingredientString)
						.value(state.categoryUid)
						.value(state.recipeIndex)
						.endArray();
			}

			jsonWriter.endArray();
		}
		jsonWriter.endObject();
	}

	private static class RecipeLookupFromFile {
		private final String categoryUid;
		private final int recipeIndex;

		public RecipeLookupFromFile(String categoryUid, int recipeIndex) {
			this.categoryUid = categoryUid;
			this.recipeIndex = recipeIndex;
		}
	}
}
