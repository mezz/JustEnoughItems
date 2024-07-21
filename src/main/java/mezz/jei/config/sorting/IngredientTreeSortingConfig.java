package mezz.jei.config.sorting;

import mezz.jei.api.constants.ModIds;
import mezz.jei.config.IClientConfig;
import mezz.jei.ingredients.IIngredientListElementInfo;
import mezz.jei.ingredients.IngredientSortStage;
import mezz.jei.ingredients.IngredientUtils;
import mezz.jei.ingredients.tree.InvTweaksItemTree;
import mezz.jei.ingredients.tree.InvTweaksItemTreeLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.InputStream;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IngredientTreeSortingConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private final IClientConfig clientConfig;
	private File file;
	private final File invTweaksFile;
	private final File jeiFile;
	//private long timestamp;// of last file load for reloading.
	InvTweaksItemTree tree;
	private long lastFileTimestamp;

	public IngredientTreeSortingConfig(IClientConfig clientConfig, File jeiFile, File invFile) {
		this.jeiFile = jeiFile;
		this.invTweaksFile = invFile;
		this.clientConfig = clientConfig;
		lastFileTimestamp = Long.MIN_VALUE;
	}

	private InvTweaksItemTree loadSortedFromFile() {
		//If this changes in the config screen.
		if (clientConfig.getUseJeiTreeFile() || !invTweaksFile.exists()) {
			file = jeiFile;
		} else {
			file = invTweaksFile;
		}
		
		if (!file.exists()) {
			ResourceLocation baseFileSource = new ResourceLocation(ModIds.JEI_ID, "templates/invtweakstree.txt");
			try {
				InputStream treedata = Minecraft.getInstance().getResourceManager().getResource(baseFileSource).getInputStream();
				FileUtils.copyInputStreamToFile(treedata, file);
			} catch (Exception e) {
				LOGGER.error("Failed to save to file {}", file, e);
			}	
		}
		if (file.exists()) {			
			try {
				lastFileTimestamp = file.lastModified();
				return InvTweaksItemTreeLoader.load(file);
			} catch (Exception e) {
				LOGGER.error("Failed to load from file {}", file, e);
			}
		}
		return null;
	}

	private void load() {
		tree = loadSortedFromFile();
	}

	public void reset() {
		tree = null;
	}

	//Monitor the tree file for changes and update when modified
	//Only checks on each filter change, so only happens once per key press
	//while looking at the JEI gui.
	public boolean hasFileChanged() {
		try {
			if (file != null 
				&& clientConfig.getIngredientSorterStages().contains(IngredientSortStage.ITEM_TREE)
				&& file.lastModified() != lastFileTimestamp
				&& InvTweaksItemTreeLoader.isValidVersion(file)) {
					return true;
			}
		} catch (Exception e) {
			//This will probably happen if the file is being updated.
			LOGGER.error("Failed to load from file {}", file, e);
		}
		return false;
	}

	public Comparator<IIngredientListElementInfo<?>> getComparator() {
		if (tree == null) {
			load();
		}
		if (tree == null) {
			return getDefaultSortOrder();
		}
		return Comparator.comparingInt(t -> getItemOrder(t));
	}

	private int getItemOrder(IIngredientListElementInfo<?> t) {
		ItemStack itemStack = IngredientUtils.getItemStack(t);
		if (itemStack != ItemStack.EMPTY) {
			return tree.getItemOrder(itemStack);
		}
		
		//I wish I could get the NBT for the non-items.
		return tree.getItemOrder(IngredientUtils.getUniqueId(t));
	}
	

	protected Comparator<IIngredientListElementInfo<?>> getDefaultSortOrder() {
		Comparator<IIngredientListElementInfo<?>> naturalOrder = Comparator.comparingInt(o -> {
			return 0;
		});
		return naturalOrder;
	}

}
