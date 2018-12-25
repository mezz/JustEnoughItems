package mezz.jei.ingredients;

import java.util.Collection;
import java.util.function.Consumer;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraft.client.Minecraft;
import net.minecraft.util.NonNullList;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import mezz.jei.config.ClientConfig;
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
			MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, TickEvent.ClientTickEvent.class, this.onTickHandler);
		}
	}

	private void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.side == LogicalSide.CLIENT && Minecraft.getInstance().player != null) {
			boolean finished = run(20);
			if (!finished) {
				return;
			}
		}
		MinecraftForge.EVENT_BUS.unregister(this.onTickHandler);
	}

	private boolean run(final int timeoutMs) {
		final long startTime = System.currentTimeMillis();
		for (PrefixedSearchTree prefixedTree : this.prefixedSearchTrees.values()) {
			ClientConfig.SearchMode mode = prefixedTree.getMode();
			if (mode != ClientConfig.SearchMode.DISABLED) {
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
