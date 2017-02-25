package mezz.jei.gui.recipes;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.config.Config;
import mezz.jei.util.FileUtil;
import mezz.jei.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.inventory.Container;

/**
 * Keeps track of what the player was viewing at last time they looked up the recipes or uses of an ingredient.
 */
public class IngredientLookupMemory {
	private final Table<IFocus.Mode, String, IngredientLookupState> mostRecentLookups = HashBasedTable.create();
	private final Table<IFocus.Mode, String, RecipeLookupFromFile> mostRecentLookupsFromFile = HashBasedTable.create();
	private final IRecipeRegistry recipeRegistry;
	private final IIngredientRegistry ingredientRegistry;
	private boolean needsSaving;

	public IngredientLookupMemory(IRecipeRegistry recipeRegistry, IIngredientRegistry ingredientRegistry) {
		this.recipeRegistry = recipeRegistry;
		this.ingredientRegistry = ingredientRegistry;
		readFromFile();
	}

	public <T> IngredientLookupState getState(IFocus<T> focus, List<IRecipeCategory> recipeCategories) {
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
					IRecipeCategory recipeCategory = recipeCategories.get(categoryIndex);
					if (stateFromFile.recipeIndex < this.recipeRegistry.getRecipeWrappers(recipeCategory).size()) {
						state = new IngredientLookupState(focus, recipeCategories, categoryIndex, stateFromFile.recipeIndex);
					}
				}
			}
		}

		if (state == null) {
			state = new IngredientLookupState(focus, recipeCategories, 0, 0);
			final int categoryIndexForOpenContainer = getRecipeCategoryIndexForOpenContainer(recipeCategories);
			if (categoryIndexForOpenContainer >= 0) {
				state.setRecipeCategoryIndex(categoryIndexForOpenContainer);
			}
		}

		if (this.mostRecentLookups.put(focusMode, uniqueId, state) != null) {
			markDirty();
		}
		if (this.mostRecentLookupsFromFile.remove(focusMode, uniqueId) != null) {
			markDirty();
		}

		return state;
	}

	private int getRecipeCategoryIndexForOpenContainer(List<IRecipeCategory> recipeCategories) {
		final Container container = getOpenContainer();
		if (container != null) {
			for (int i = 0; i < recipeCategories.size(); i++) {
				IRecipeCategory recipeCategory = recipeCategories.get(i);
				if (this.recipeRegistry.getRecipeTransferHandler(container, recipeCategory) != null) {
					return i;
				}
			}
		}
		return -1;
	}

	@Nullable
	private static Container getOpenContainer() {
		final Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft != null) {
			final EntityPlayerSP player = minecraft.player;
			if (player != null) {
				return player.openContainer;
			}
		}
		return null;
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
		FileUtil.readFileSafely(file, new FileUtil.FileOperation() {
			@Override
			public void handle(File file) throws IOException {
				final ZipInputStream zipInput = new ZipInputStream(new FileInputStream(file));
				if (getZipEntry(zipInput, "lookupHistory.json")) {
					final JsonReader jsonReader = new JsonReader(new InputStreamReader(zipInput));
					jsonReader.beginObject();

					while (jsonReader.hasNext()) {
						final String name = jsonReader.nextName();
						if (name.equals("lookupStates")) {
							readLookupStatesObject(jsonReader);
						}
					}

					jsonReader.endObject();

					zipInput.close();
				}
			}
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

	private static boolean getZipEntry(ZipInputStream zipInputStream, String zipEntryName) throws IOException {
		while (true) {
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			if (zipEntry != null) {
				if (zipEntry.getName().equals(zipEntryName)) {
					return true;
				}
			} else {
				return false;
			}
		}
	}

	public void markDirty() {
		this.needsSaving = true;
	}

	public void saveToFile() {
		if (this.needsSaving) {
			final File file = new File(Config.getJeiConfigurationDir(), "lookupHistory.zip");
			final boolean write = FileUtil.writeFileSafely(file, new FileUtil.FileOperation() {
				@Override
				public void handle(File file) throws IOException {
					ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(file));
					zipOutput.putNextEntry(new ZipEntry("lookupHistory.json"));

					JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(zipOutput));

					jsonWriter.beginObject();
					{
						writeLookupStatesObject(jsonWriter);
					}
					jsonWriter.endObject();

					jsonWriter.flush();
					zipOutput.closeEntry();
					zipOutput.close();
				}
			});

			if (write) {
				Log.debug("Saved IngredientLookupMemory to {}.", file.getAbsoluteFile());
				this.needsSaving = false;
			}
		}
	}

	private void writeLookupStatesObject(JsonWriter jsonWriter) throws IOException {
		jsonWriter.name("lookupStates")
				.beginObject();

		for (IFocus.Mode focusMode : IFocus.Mode.values()) {

			jsonWriter.name(focusMode.toString())
					.beginArray();

			for (Map.Entry<String, IngredientLookupState> lookups : this.mostRecentLookups.row(focusMode).entrySet()) {
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

			for (Map.Entry<String, IngredientLookupMemory.RecipeLookupFromFile> lookups : this.mostRecentLookupsFromFile.row(focusMode).entrySet()) {
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
