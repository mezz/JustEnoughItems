package mezz.jei.config.sorting;

import mezz.jei.api.constants.ModIds;
import mezz.jei.ingredients.IIngredientListElementInfo;
import mezz.jei.ingredients.IngredientUtils;
import mezz.jei.ingredients.tree.InvTweaksItemTree;
import mezz.jei.ingredients.tree.InvTweaksItemTreeLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IngredientTreeSortingConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private final File file;
	//private long timestamp;// of last file load for reloading.
	InvTweaksItemTree tree;

	public IngredientTreeSortingConfig(File file) {
		this.file = file;
	}

	private InvTweaksItemTree loadSortedFromFile() {
		final File file = this.file;
		if (!file.exists()) {
			ResourceLocation baseFileSource = new ResourceLocation(ModIds.JEI_ID, "templates/invtweakstree.txt");
			try {
				InputStream treedata = Minecraft.getInstance().getResourceManager().getResource(baseFileSource).getInputStream();
				FileUtils.copyInputStreamToFile(treedata, file);
			} catch (Exception e) {
				LOGGER.error("Failed to save to file {}", this.file, e);
			}	
		}
		if (file.exists()) {
			try {
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
