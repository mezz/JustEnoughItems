package mezz.jei.ingredients;

import gnu.trove.map.TCharObjectMap;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.suffixtree.GeneralizedSuffixTree;
import net.minecraft.client.Minecraft;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Collection;

public class IngredientFilterBackgroundBuilder {
	private final TCharObjectMap<PrefixedSearchTree> prefixedSearchTrees;
	private final NonNullList<IIngredientListElement> elementList;

	public IngredientFilterBackgroundBuilder(TCharObjectMap<PrefixedSearchTree> prefixedSearchTrees, NonNullList<IIngredientListElement> elementList) {
		this.prefixedSearchTrees = prefixedSearchTrees;
		this.elementList = elementList;
	}

	public void start() {
		boolean finished = run(10000);
		if (!finished) {
			MinecraftForge.EVENT_BUS.register(this);
		}
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.side == Side.CLIENT && Minecraft.getMinecraft().player != null) {
			boolean finished = run(20);
			if (!finished) {
				return;
			}
		}
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	private boolean run(final int timeoutMs) {
		final long startTime = System.currentTimeMillis();
		for (PrefixedSearchTree prefixedTree : this.prefixedSearchTrees.valueCollection()) {
			Config.SearchMode mode = prefixedTree.getMode();
			if (mode != Config.SearchMode.DISABLED) {
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
