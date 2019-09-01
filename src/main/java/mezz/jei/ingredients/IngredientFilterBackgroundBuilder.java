package mezz.jei.ingredients;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.event.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.NonNullList;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.SearchMode;
import mezz.jei.events.EventBusHelper;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.suffixtree.GeneralizedSuffixTree;

public class IngredientFilterBackgroundBuilder {
	private final Char2ObjectMap<PrefixedSearchTree> prefixedSearchTrees;
	private final NonNullList<IIngredientListElement> elementList;
	private final IIngredientManager ingredientManager;
	private final IModIdHelper modIdHelper;
	private final Consumer<TickEvent.ClientTickEvent> onTickHandler;

	public IngredientFilterBackgroundBuilder(
		Char2ObjectMap<PrefixedSearchTree> prefixedSearchTrees,
		NonNullList<IIngredientListElement> elementList,
		IIngredientManager ingredientManager,
		IModIdHelper modIdHelper
	) {
		this.prefixedSearchTrees = prefixedSearchTrees;
		this.elementList = elementList;
		this.ingredientManager = ingredientManager;
		this.modIdHelper = modIdHelper;
		this.onTickHandler = this::onClientTick;
	}

	public void start() {
		boolean finished = run(10000);
		if (!finished) {
			EventBusHelper.addListener(TickEvent.ClientTickEvent.class, this.onTickHandler);
		}
	}

	private void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.side == LogicalSide.CLIENT && Minecraft.getInstance().player != null) {
			boolean finished = run(20);
			if (!finished) {
				return;
			}
		}
		EventBusHelper.removeListener(this.onTickHandler);
	}

	private boolean run(final int timeoutMs) {
		final long startTime = System.currentTimeMillis();
		List<PrefixedSearchTree> activeTrees = new ArrayList<>();
		int startIndex = Integer.MAX_VALUE;
		for (PrefixedSearchTree prefixedTree : this.prefixedSearchTrees.values()) {
			SearchMode mode = prefixedTree.getMode();
			if (mode != SearchMode.DISABLED) {
				GeneralizedSuffixTree tree = prefixedTree.getTree();
				int nextFreeIndex = tree.getHighestIndex() + 1;
				startIndex = Math.min(nextFreeIndex, startIndex);
				if (nextFreeIndex < elementList.size()) {
					activeTrees.add(prefixedTree);
				}
			}
		}

		if (activeTrees.isEmpty()) {
			return true;
		}

		for (int i = startIndex; i < elementList.size(); i++) {
			IIngredientListElement<?> element = elementList.get(i);
			IngredientListElementInfo<?> info = IngredientListElementInfo.create(element, ingredientManager, modIdHelper);
			if (info == null) {
				continue;
			}
			for (PrefixedSearchTree prefixedTree : activeTrees) {
				GeneralizedSuffixTree tree = prefixedTree.getTree();
				int nextFreeIndex = tree.getHighestIndex() + 1;
				if (nextFreeIndex >= i) {
					PrefixedSearchTree.IStringsGetter stringsGetter = prefixedTree.getStringsGetter();
					Collection<String> strings = stringsGetter.getStrings(info);
					if (strings.isEmpty()) {
						tree.put("", i);
					} else {
						for (String string : strings) {
							tree.put(string, i);
						}
					}
				}
			}
			if (System.currentTimeMillis() - startTime >= timeoutMs) {
				return false;
			}
		}
		return true;
	}
}
