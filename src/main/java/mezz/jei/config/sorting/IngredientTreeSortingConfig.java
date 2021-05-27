package mezz.jei.config.sorting;

import mezz.jei.ingredients.IIngredientListElementInfo;
import mezz.jei.ingredients.tree.InvTweaksItemTree;
import mezz.jei.ingredients.tree.InvTweaksItemTreeLoader;
import net.minecraft.item.ItemStack;

import java.io.File;
import java.util.Comparator;

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
		return Comparator.comparingInt(t -> tree.getItemOrder(t.getCheatItemStack()));
	}

	protected Comparator<IIngredientListElementInfo<?>> getDefaultSortOrder() {
		Comparator<IIngredientListElementInfo<?>> naturalOrder = Comparator.comparingInt(o -> {
			return 0;
		});
		return naturalOrder;
	}

}
