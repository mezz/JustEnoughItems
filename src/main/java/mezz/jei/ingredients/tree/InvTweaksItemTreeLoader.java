package mezz.jei.ingredients.tree;

import mezz.jei.api.ingredients.tree.IItemTreeListener;
import mezz.jei.util.LoggedTimer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Loads the item tree by parsing the XML file.
 *
 * @author Jimeo Wan
 */
public class InvTweaksItemTreeLoader extends DefaultHandler {

    public final static String ATTR_ID = "id";
    public final static String ATTR_DAMAGE = "damage";
    public final static String ATTR_RANGE_DMIN = "dmin"; // Damage ranges
    public final static String ATTR_RANGE_DMAX = "dmax";
    public final static String ATTR_OREDICT_NAME = "oreDictName"; // OreDictionary names
    public final static String ATTR_DATA = "data";
    public final static String ATTR_CLASS = "class";
    public final static String ATTR_LAST_ORDER = "mergePrevious";
    public final static String ATTR_MERGE_CHILDREN = "mergeChildren";
    public final static String ATTR_TREE_VERSION = "treeVersion";
    public final static String ATTR_TREE_ORDER = "treeOrder";
    private static final List<IItemTreeListener> onLoadListeners = new ArrayList<>();
    private static InvTweaksItemTree tree;
    
    private static String treeVersion;
    private static int itemOrder;
    private static int mergeChildren;
    private static LinkedList<String> categoryStack;
    private static LinkedList<Boolean> mergeStack;
    private static boolean treeLoaded = false;

    private static void init() {
        treeVersion = null;
        tree = new InvTweaksItemTree();
        itemOrder = 0;
        mergeChildren = 0;
        categoryStack = new LinkedList<>();
        mergeStack = new LinkedList<>();
    }

    public synchronized static InvTweaksItemTree load(File file) throws Exception {
        init();
    	
		LoggedTimer treeTimer = new LoggedTimer();
		treeTimer.start("Starting Loading Tree.");
        
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        SAXParser parser = parserFactory.newSAXParser();
        parser.parse(file, new InvTweaksItemTreeLoader());

        // Tree loaded event
        synchronized(onLoadListeners) {
            treeLoaded = true;
            for( IItemTreeListener onLoadListener : onLoadListeners) {
                onLoadListener.onTreeLoaded(tree);
            }
        }

        MinecraftForge.EVENT_BUS.register(tree);
        
        treeTimer.stop();
        LogManager.getLogger().info("Finished Loading Tree.  Highest Order: " + tree.getHighestOrder());
        
        return tree;
    }

    public synchronized static boolean isValidVersion( File file) throws Exception {
        init();

        if(file.exists()) {
            treeVersion = null;
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            SAXParser parser = parserFactory.newSAXParser();

            VersionLoader loader = new VersionLoader();
            parser.parse(file, loader);
            return InvTweaksConst.TREE_VERSION.equals(loader.version);
        } else {
            return false;
        }
    }

    public synchronized static void addOnLoadListener( IItemTreeListener listener) {
        onLoadListeners.add(listener);
        if(treeLoaded) {
            // Late event triggering
            listener.onTreeLoaded(tree);
        }
    }

    public synchronized static boolean removeOnLoadListener(IItemTreeListener listener) {
        return onLoadListeners.remove(listener);
    }

    private int getNextItemOrder(boolean lastOrder) {
        if ((lastOrder || mergeChildren > 0) && itemOrder > 0)
            return itemOrder - 1;
        return itemOrder++;
    }

    @Override
    public synchronized void startElement(String uri, String localName, String name,  Attributes attributes)
            throws SAXException {

        String rangeDMinAttr = attributes.getValue(ATTR_RANGE_DMIN);
        String newTreeVersion = attributes.getValue(ATTR_TREE_VERSION);
        String oreDictNameAttr = attributes.getValue(ATTR_OREDICT_NAME);
        String id = attributes.getValue(ATTR_ID);
        String className = attributes.getValue(ATTR_CLASS);
        String lastOrderValue = attributes.getValue(ATTR_LAST_ORDER);
        String mergeChildrenValue = attributes.getValue(ATTR_MERGE_CHILDREN);
        lastOrderValue = lastOrderValue == null ? "" : lastOrderValue.toLowerCase();
        boolean lastOrder = (lastOrderValue.equals("1") || lastOrderValue.equals("true") || lastOrderValue.equals("yes") || lastOrderValue.equals("t") || lastOrderValue.equals("y"));
        mergeChildrenValue = mergeChildrenValue == null ? "" : mergeChildrenValue.toLowerCase();
        boolean willMergeChildren = (mergeChildrenValue.equals("1") || mergeChildrenValue.equals("true") || mergeChildrenValue.equals("yes") || mergeChildrenValue.equals("t") || mergeChildrenValue.equals("y"));
        mergeStack.add(willMergeChildren);
        
        // Tree version
        if(treeVersion == null) {
            treeVersion = newTreeVersion;
        }
        

        // Item
        if(id != null) {
            int damage = InvTweaksConst.DAMAGE_WILDCARD;
            String extraDataAttr = attributes.getValue(ATTR_DATA);
             CompoundNBT extraData = null;
            if(extraDataAttr != null) {
                try {
                    extraData = JsonToNBT.getTagFromJson(extraDataAttr);
                } catch(Exception e) {
                    throw new RuntimeException("Data attribute failed for tree entry '" + name + "'", e);
                }
            }
            if(attributes.getValue(ATTR_DAMAGE) != null) {
                damage = Integer.parseInt(attributes.getValue(ATTR_DAMAGE));
            }
            tree.addItem(categoryStack.getLast(),
                    new InvTweaksItemTreeItem(name, id, damage, extraData, getNextItemOrder(lastOrder), String.join("\\", categoryStack) + "\\" + name));
        } else if(oreDictNameAttr != null) {
            tree.registerOre(categoryStack.getLast(), name, oreDictNameAttr, getNextItemOrder(lastOrder), String.join("\\", categoryStack) + "\\" + name);
        } else if(className != null) {
            String extraDataAttr = attributes.getValue(ATTR_DATA);
             CompoundNBT extraData = null;
            if(extraDataAttr != null) {
                try {
                    extraData = JsonToNBT.getTagFromJson(extraDataAttr.toLowerCase());
                } catch(Exception e) {
                    throw new RuntimeException("Data attribute failed for tree entry '" + name + "'", e);
                }
            }
            tree.registerClass(categoryStack.getLast(), name, className.toLowerCase(), extraData, getNextItemOrder(lastOrder), String.join("\\", categoryStack) + "\\" + name);
        } else { 
            // Category
            if(categoryStack.isEmpty()) {
                // Root category
                tree.setRootCategory(new InvTweaksItemTreeCategory(name));
            } else {
                // Normal category
                tree.addCategory(categoryStack.getLast(), new InvTweaksItemTreeCategory(name));
            }

            // Handle damage ranges
            if(rangeDMinAttr != null) {                
                int rangeDMin = Integer.parseInt(rangeDMinAttr);
                int rangeDMax = Integer.parseInt(attributes.getValue(ATTR_RANGE_DMAX));
                for(int damage = rangeDMin; damage <= rangeDMax; damage++) {
                    tree.addItem(name, new InvTweaksItemTreeItem((name + id + "-" + damage), id, damage, null,
                            getNextItemOrder(lastOrder), String.join("\\", categoryStack) + "\\" + name));
                }
            } else if (willMergeChildren) {
                //Try to get a new ID for the children to use.  
                //(If an ancestor already set the flag, this will do nothing.)
                getNextItemOrder(lastOrder);
            }
            categoryStack.add(name);
        }
            
        //This happens last so if this node got an ID and it was supposed to be new, it did.
        if (willMergeChildren)
            mergeChildren++;
        
    }

    @Override
    public synchronized void endElement(String uri, String localName,  String name) throws SAXException {
        if(!categoryStack.isEmpty() && name.equals(categoryStack.getLast())) {
            categoryStack.removeLast();
        }
        if (!mergeStack.isEmpty()) {
            if (mergeStack.getLast() && mergeChildren > 0)
                mergeChildren--;
            mergeStack.removeLast();
        }
    }
    
    @Override
    public void endDocument () throws SAXException {
        tree.endFileRead();
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
    	LogManager.getLogger().warn("Tree XML Warning: ", e);
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
    	LogManager.getLogger().error("Tree XML Error: ", e);
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
    	LogManager.getLogger().fatal("Tree XML Fatal Error: ", e);
    }

    private static class VersionLoader extends DefaultHandler {
        
        String version;

        @Override
        public synchronized void startElement(String uri, String localName, String name,  Attributes attributes)
                throws SAXException {
            if(version == null) {
                version = attributes.getValue(ATTR_TREE_VERSION);
            }
        }
    }
}
