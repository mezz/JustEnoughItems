package mezz.jei.test;

import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.suffixtree.GeneralizedSuffixTree;
import org.junit.Test;

public class SuffixTreeTest {
    @Test
    public void testWorking() {
        GeneralizedSuffixTree tree = new GeneralizedSuffixTree();

        tree.put("MAGIC KEY1", 1);
        tree.put("NOT MAGIC KEY2", 2);
        tree.put("NO KEY2", 3);

        IntSet res = tree.search("MAGIC");

        System.out.println("res = " + res);
        assert res.size() == 2 && res.contains(1) && res.contains(2);


        res = tree.search("NO");
        System.out.println("res = " + res);
        assert res.size() == 2 && res.contains(3) && res.contains(2);
    }
}
