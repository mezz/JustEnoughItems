package mezz.jei.ingredients;

import java.util.Collection;
import java.util.function.Consumer;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.NonNullList;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import mezz.jei.config.SearchMode;
import mezz.jei.events.EventBusHelper;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.suffixtree.GeneralizedSuffixTree;

public class IngredientFilterBackgroundBuilder {
	private final Char2ObjectMap<PrefixedSearchTree> prefixedSearchTrees;
	private final NonNullList<IIngredientListElement> elementList;
	private final Consumer<TickEvent.ClientTickEvent> onTickHandler;

	public IngredientFilterBackgroundBuilder(Char2ObjectMap<PrefixedSearchTree> prefixedSearchTrees, NonNullList<IIngredientListElement> elementList) {
		this.prefixedSearchTrees = prefixedSearchTrees;
		this.elementList = elementList;
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
		for (PrefixedSearchTree prefixedTree : this.prefixedSearchTrees.values()) {
			SearchMode mode = prefixedTree.getMode();
			if (mode != SearchMode.DISABLED) {
				PrefixedSearchTree.IStringsGetter stringsGetter = prefixedTree.getStringsGetter();
				GeneralizedSuffixTree tree = prefixedTree.getTree();
				for (int i = tree.getHighestIndex() + 1; i < this.elementList.size(); i++) {
					IIngredientListElement element = elementList.get(i);
					Collection<String> strings = stringsGetter.getStrings(element);
					if (strings.isEmpty()) {
						tree.put("", i);
					} else {
						for (String string : strings) {
							tree.put(string, i);
						}
					}
					if (System.currentTimeMillis() - startTime >= timeoutMs) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
