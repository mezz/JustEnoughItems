/*
 * Copyright 2012 Alessandro Bahgat Shehata
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mezz.jei.suffixtree;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A Generalized Suffix Tree, based on the Ukkonen's paper "On-line construction of suffix trees"
 * http://www.cs.helsinki.fi/u/ukkonen/SuffixT1withFigs.pdf
 * <p>
 * Allows for fast storage and fast(er) retrieval by creating a tree-based index out of a set of strings.
 * Unlike common suffix trees, which are generally used to build an index out of one (very) long string,
 * a Generalized Suffix Tree can be used to build an index over many strings.
 * <p>
 * Its main operations are put and search:
 * Put adds the given key to the index, allowing for later retrieval of the given value.
 * Search can be used to retrieve the set of all the values that were put in the index with keys that contain a given input.
 * <p>
 * In particular, after put(K, V), search(H) will return a set containing V for any string H that is substring of K.
 * <p>
 * The overall complexity of the retrieval operation (search) is O(m) where m is the length of the string to search within the index.
 * <p>
 * Although the implementation is based on the original design by Ukkonen, there are a few aspects where it differs significantly.
 * <p>
 * The tree is composed of a set of nodes and labeled edges. The labels on the edges can have any length as long as it's greater than 0.
 * The only constraint is that no two edges going out from the same node will start with the same character.
 * <p>
 * Because of this, a given (startNode, stringSuffix) pair can denote a unique path within the tree, and it is the path (if any) that can be
 * composed by sequentially traversing all the edges (e1, e2, ...) starting from startNode such that (e1.label + e2.label + ...) is equal
 * to the stringSuffix.
 * See the search method for details.
 * <p>
 * The union of all the edge labels from the root to a given leaf node denotes the set of the strings explicitly contained within the GST.
 * In addition to those Strings, there are a set of different strings that are implicitly contained within the GST, and it is composed of
 * the strings built by concatenating e1.label + e2.label + ... + $end, where e1, e2, ... is a proper path and $end is prefix of any of
 * the labels of the edges starting from the last node of the path.
 * <p>
 * This kind of "implicit path" is important in the testAndSplit method.
 * <p>
 * Edited by mezz:
 * - improve performance of search by passing a set around instead of creating new ones and using addAll
 * - only allow full searches
 * - add nullable/nonnull annotations
 * - formatting
 */
public class GeneralizedSuffixTree implements ISearchTree {

	private int highestIndex = -1;
	/**
	 * The root of the suffix tree
	 */
	private final Node root = new Node();
	/**
	 * The last leaf that was added during the update operation
	 */
	private Node activeLeaf = root;

	/**
	 * Searches for the given word within the GST.
	 * <p>
	 * Returns all the indexes for which the key contains the <tt>word</tt> that was
	 * supplied as input.
	 *
	 * @param word the key to search for
	 * @return the collection of indexes associated with the input <tt>word</tt>
	 */
	@Override
	public IntSet search(String word) {
		Node tmpNode = searchNode(word);
		if (tmpNode == null) {
			return new IntOpenHashSet();
		}

		IntSet ret = new IntOpenHashSet(1000);
		tmpNode.getData(ret);
		return ret;
	}

	/**
	 * Returns the tree node (if present) that corresponds to the given string.
	 */
	@Nullable
	private Node searchNode(String word) {
		/*
		 * Verifies if exists a path from the root to a node such that the concatenation
		 * of all the labels on the path is a superstring of the given word.
		 * If such a path is found, the last node on it is returned.
		 */
		Node currentNode = root;
		Edge currentEdge;

		for (int i = 0; i < word.length(); ++i) {
			char ch = word.charAt(i);
			// follow the edge corresponding to this char
			currentEdge = currentNode.getEdge(ch);
			if (null == currentEdge) {
				// there is no edge starting with this char
				return null;
			} else {
				String label = currentEdge.getLabel();
				int lenToMatch = Math.min(word.length() - i, label.length());
				if (!word.regionMatches(i, label, 0, lenToMatch)) {
					// the label on the edge does not correspond to the one in the string to search
					return null;
				}

				if (label.length() >= word.length() - i) {
					return currentEdge.getDest();
				} else {
					// advance to next node
					currentNode = currentEdge.getDest();
					i += lenToMatch - 1;
				}
			}
		}

		return null;
	}

	/**
	 * Adds the specified <tt>index</tt> to the GST under the given <tt>key</tt>.
	 * <p>
	 * Entries must be inserted so that their indexes are in non-decreasing order,
	 * otherwise an IllegalStateException will be raised.
	 *
	 * @param key   the string key that will be added to the index
	 * @param index the value that will be added to the index
	 */
	public void put(String key, int index) throws IllegalStateException {
		if (index < highestIndex) {
			throw new IllegalStateException("The input index must not be less than any of the previously inserted ones. Got " + index + ", expected at least " + highestIndex);
		} else {
			highestIndex = index;
		}

		// reset activeLeaf
		activeLeaf = root;

		Node s = root;

		// proceed with tree construction (closely related to procedure in Ukkonen's paper)
		String text = "";
		// iterate over the string, one char at a time
		for (int i = 0; i < key.length(); i++) {
			// line 6, line 7: update the tree with the new transitions due to this new char
			Pair<Node, String> active = update(s, text, key.charAt(i), key.substring(i), index);

			s = active.getFirst();
			text = active.getSecond();
		}

		// add leaf suffix link, is necessary
		if (null == activeLeaf.getSuffix() && activeLeaf != root && activeLeaf != s) {
			activeLeaf.setSuffix(s);
		}

	}

	/**
	 * Tests whether the string stringPart + t is contained in the subtree that has inputs as root.
	 * If that's not the case, and there exists a path of edges e1, e2, ... such that
	 * e1.label + e2.label + ... + $end = stringPart
	 * and there is an edge g such that
	 * g.label = stringPart + rest
	 * <p>
	 * Then g will be split in two different edges, one having $end as label, and the other one
	 * having rest as label.
	 *
	 * @param inputs     the starting node
	 * @param stringPart the string to search
	 * @param t          the following character
	 * @param remainder  the remainder of the string to add to the index
	 * @param value      the value to add to the index
	 * @return a pair containing
	 * true/false depending on whether (stringPart + t) is contained in the subtree starting in inputs
	 * the last node that can be reached by following the path denoted by stringPart starting from inputs
	 */
	private Pair<Boolean, Node> testAndSplit(final Node inputs, final String stringPart, final char t, final String remainder, final int value) {
		// descend the tree as far as possible
		Pair<Node, String> ret = canonize(inputs, stringPart);
		Node s = ret.getFirst();
		String str = ret.getSecond();

		if (!"".equals(str)) {
			Edge g = s.getEdge(str.charAt(0));
			Objects.requireNonNull(g);
			String label = g.getLabel();
			// must see whether "str" is substring of the label of an edge
			if (label.length() > str.length() && label.charAt(str.length()) == t) {
				return new Pair<>(true, s);
			} else {
				// need to split the edge
				String newlabel = label.substring(str.length());
				assert (label.startsWith(str));

				// build a new node
				Node r = new Node();
				// build a new edge
				Edge newedge = new Edge(str, r);

				g.setLabel(newlabel);

				// link s -> r
				r.addEdge(newlabel.charAt(0), g);
				s.addEdge(str.charAt(0), newedge);

				return new Pair<>(false, r);
			}

		} else {
			Edge e = s.getEdge(t);
			if (null == e) {
				// if there is no t-transtion from s
				return new Pair<>(false, s);
			} else {
				if (remainder.equals(e.getLabel())) {
					// update payload of destination node
					e.getDest().addRef(value);
					return new Pair<>(true, s);
				} else if (remainder.startsWith(e.getLabel())) {
					return new Pair<>(true, s);
				} else if (e.getLabel().startsWith(remainder)) {
					// need to split as above
					Node newNode = new Node();
					newNode.addRef(value);

					Edge newEdge = new Edge(remainder, newNode);

					e.setLabel(e.getLabel().substring(remainder.length()));

					newNode.addEdge(e.getLabel().charAt(0), e);

					s.addEdge(t, newEdge);

					return new Pair<>(false, s);
				} else {
					// they are different words. No prefix. but they may still share some common substr
					return new Pair<>(true, s);
				}
			}
		}

	}

	/**
	 * Return a (Node, String) (n, remainder) pair such that n is a farthest descendant of
	 * s (the input node) that can be reached by following a path of edges denoting
	 * a prefix of inputstr and remainder will be string that must be
	 * appended to the concatenation of labels from s to n to get inpustr.
	 */
	private Pair<Node, String> canonize(final Node s, final String inputstr) {

		if ("".equals(inputstr)) {
			return new Pair<>(s, inputstr);
		} else {
			Node currentNode = s;
			String str = inputstr;
			Edge g = s.getEdge(str.charAt(0));
			// descend the tree as long as a proper label is found
			while (g != null && str.startsWith(g.getLabel())) {
				str = str.substring(g.getLabel().length());
				currentNode = g.getDest();
				if (str.length() > 0) {
					g = currentNode.getEdge(str.charAt(0));
				}
			}

			return new Pair<>(currentNode, str);
		}
	}

	/**
	 * Updates the tree starting from inputNode and by adding stringPart.
	 * <p>
	 * Returns a reference (Node, String) pair for the string that has been added so far.
	 * This means:
	 * - the Node will be the Node that can be reached by the longest path string (S1)
	 * that can be obtained by concatenating consecutive edges in the tree and
	 * that is a substring of the string added so far to the tree.
	 * - the String will be the remainder that must be added to S1 to get the string
	 * added so far.
	 *
	 * @param inputNode  the node to start from
	 * @param stringPart the string to add to the tree
	 * @param rest       the rest of the string
	 * @param value      the value to add to the index
	 */
	private Pair<Node, String> update(final Node inputNode, final String stringPart, final char newChar, final String rest, final int value) {
		Node s = inputNode;
		String tempstr = stringPart + newChar;

		// line 1
		Node oldroot = root;

		// line 1b
		Pair<Boolean, Node> ret = testAndSplit(s, stringPart, newChar, rest, value);

		Node r = ret.getSecond();
		boolean endpoint = ret.getFirst();

		Node leaf;
		// line 2
		while (!endpoint) {
			// line 3
			Edge tempEdge = r.getEdge(newChar);
			if (null != tempEdge) {
				// such a node is already present. This is one of the main differences from Ukkonen's case:
				// the tree can contain deeper nodes at this stage because different strings were added by previous iterations.
				leaf = tempEdge.getDest();
			} else {
				// must build a new leaf
				leaf = new Node();
				leaf.addRef(value);
				Edge newedge = new Edge(rest, leaf);
				r.addEdge(newChar, newedge);
			}

			// update suffix link for newly created leaf
			if (activeLeaf != root) {
				activeLeaf.setSuffix(leaf);
			}
			activeLeaf = leaf;

			// line 4
			if (oldroot != root) {
				oldroot.setSuffix(r);
			}

			// line 5
			oldroot = r;

			// line 6
			if (null == s.getSuffix()) { // root node
				assert (root == s);
				// this is a special case to handle what is referred to as node _|_ on the paper
				tempstr = tempstr.substring(1);
			} else {
				Pair<Node, String> canret = canonize(s.getSuffix(), safeCutLastChar(tempstr));
				s = canret.getFirst();
				tempstr = (canret.getSecond() + tempstr.charAt(tempstr.length() - 1));
			}

			// line 7
			ret = testAndSplit(s, safeCutLastChar(tempstr), newChar, rest, value);
			r = ret.getSecond();
			endpoint = ret.getFirst();

		}

		// line 8
		if (oldroot != root) {
			oldroot.setSuffix(r);
		}

		// make sure the active pair is canonical
		return canonize(s, tempstr);
	}

	private static String safeCutLastChar(String seq) {
		if (seq.length() == 0) {
			return "";
		}
		return seq.substring(0, seq.length() - 1);
	}

	public int getHighestIndex() {
		return highestIndex;
	}

	public void trimToSize() {
		ArrayDeque<Node> nodes = new ArrayDeque<>(128);
		nodes.add(root);
		while (!nodes.isEmpty()) {
			Node node = nodes.remove();
			node.trimToSize();
			Node suffix = node.getSuffix();
			if (suffix != null) {
				suffix.trimToSize();
			}
			for (Edge edge : node.edges()) {
				nodes.add(edge.getDest());
			}
		}
	}

	/**
	 * A private class used to return a tuples of two elements
	 */
	private static class Pair<A, B> {

		private final A first;
		private final B second;

		public Pair(A first, B second) {
			this.first = first;
			this.second = second;
		}

		public A getFirst() {
			return first;
		}

		public B getSecond() {
			return second;
		}

		@Override
		public String toString() {
			return "Pair (" + first + ", " + second + ")";
		}
	}
}
