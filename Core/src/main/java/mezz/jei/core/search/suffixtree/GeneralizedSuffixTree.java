/*
 * Copyright 2012 Alessandro Bahgat Shehata
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mezz.jei.core.search.suffixtree;

import mezz.jei.core.search.ISearchStorage;
import mezz.jei.core.util.Pair;
import mezz.jei.core.util.SubString;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.util.Set;

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
 * - refactored and optimized
 */
public class GeneralizedSuffixTree<T> implements ISearchStorage<T> {
	/**
	 * The root of the suffix tree
	 */
	private final RootNode<T> root = new RootNode<>();
	/**
	 * The last leaf that was added during the update operation
	 */
	private Node<T> activeLeaf = root;

	/**
	 * Searches for the given word within the GST.
	 * <p>
	 * Returns all the indexes for which the key contains the <tt>word</tt> that was
	 * supplied as input.
	 *
	 * @param word the key to search for
	 * @param results the indexes associated with the input <tt>word</tt>
	 */
	@Override
	public void getSearchResults(String word, Set<T> results) {
		Node<T> tmpNode = searchNode(root, word);
		if (tmpNode == null) {
			return;
		}

		tmpNode.getData(results);
	}

	@Override
	public void getAllElements(Set<T> results) {
		root.getData(results);
	}

	/**
	 * Verifies if exists a path from the root to a node such that the concatenation
	 * of all the labels on the path is a superstring of the given word.
	 * If such a path is found, the last node on it is returned.
	 *
	 * Returns the tree node (if present) that corresponds to the given string.
	 */
	@Nullable
	private static <T> Node<T> searchNode(final Node<T> root, final String word) {
		Node<T> currentNode = root;
		SubString wordSubString = new SubString(word);

		while (!wordSubString.isEmpty()) {
			// follow the edge corresponding to this char
			Edge<T> currentEdge = currentNode.getEdge(wordSubString);
			if (currentEdge == null) {
				// there is no edge starting with this char
				return null;
			}

			int lenToMatch = Math.min(wordSubString.length(), currentEdge.length());
			if (!currentEdge.regionMatches(wordSubString, lenToMatch)) {
				// the label on the edge does not correspond to the one in the string to search
				return null;
			}
			if (lenToMatch == wordSubString.length()) {
				// we found the edge we're looking for
				return currentEdge.getDest();
			}

			// advance to next node
			currentNode = currentEdge.getDest();
			wordSubString = wordSubString.substring(lenToMatch);
		}

		return null;
	}

	/**
	 * Adds the specified <tt>index</tt> to the GST under the given <tt>key</tt>.
	 *
	 * @param key   the string key that will be added to the index
	 * @param value the value that will be added
	 */
	@Override
	public void put(String key, T value) {
		// reset activeLeaf
		activeLeaf = root;

		Node<T> s = root;

		// proceed with tree construction (closely related to procedure in Ukkonen's paper)
		SubString text = new SubString(key, 0, 0);
		// iterate over the string, one char at a time
		for (int i = 0; i < key.length(); i++) {
			// line 6, line 7: update the tree with the new transitions due to this new char
			SubString rest = new SubString(key, i);
			Pair<Node<T>, SubString> active = update(s, text, key.charAt(i), rest, value);

			s = active.first();
			text = active.second();
		}

		// add leaf suffix link, if necessary
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
	 * @param startNode    the starting node
	 * @param searchString the string to search
	 * @param t            the following character
	 * @param remainder    the remainder of the string to add to the index
	 * @param value        the value to add to the index
	 * @return a pair containing
	 * true/false depending on whether (stringPart + t) is contained in the subtree starting in inputNode
	 * the last node that can be reached by following the path denoted by stringPart starting from inputNode
	 */
	private static <T> Pair<Boolean, Node<T>> testAndSplit(
		Node<T> startNode,
		SubString searchString,
		final char t,
		final SubString remainder,
		final T value
	) {
		assert !remainder.isEmpty();
		assert remainder.charAt(0) == t;

		// descend the tree as far as possible
		Pair<Node<T>, SubString> canonizeResult = canonize(startNode, searchString);
		startNode = canonizeResult.first();
		searchString = canonizeResult.second();

		if (!searchString.isEmpty()) {
			Edge<T> g = startNode.getEdge(searchString);
			assert g != null;
			// must see whether "searchString" is substring of the label of an edge
			if (g.length() > searchString.length() && g.charAt(searchString.length()) == t) {
				return new Pair<>(true, startNode);
			}
			Node<T> newNode = splitNode(startNode, g, searchString);
			return new Pair<>(false, newNode);
		}

		Edge<T> e = startNode.getEdge(remainder);
		if (e == null) {
			// if there is no t-transition from s
			return new Pair<>(false, startNode);
		}

		if (e.startsWith(remainder)) {
			if (e.length() == remainder.length()) {
				// update payload of destination node
				Node<T> dest = e.getDest();
				dest.addRef(value);
				return new Pair<>(true, startNode);
			} else {
				Node<T> newNode = splitNode(startNode, e, remainder);
				newNode.addRef(value);
				return new Pair<>(false, startNode);
			}
		} else {
			return new Pair<>(true, startNode);
		}
	}

	private static <T> Node<T> splitNode(Node<T> s, Edge<T> e, SubString splitFirstPart) {
		assert e == s.getEdge(splitFirstPart);
		assert e.startsWith(splitFirstPart);
		assert e.length() > splitFirstPart.length();

		// need to split the edge
		SubString splitSecondPart = e.substring(splitFirstPart.length());

		// build a new node r in between s and e.dest
		Node<T> r = new Node<>();
		// replace e with new first part pointing to r
		s.addEdge(new Edge<>(splitFirstPart, r));
		// r is the new node sitting in between s and the original destination
		r.addEdge(new Edge<>(splitSecondPart, e.getDest()));

		return r;
	}

	/**
	 * Return a (Node, String) (n, remainder) pair such that n is the farthest descendant of
	 * s (the input node) that can be reached by following a path of edges denoting
	 * a prefix of input and remainder will be string that must be
	 * appended to the concatenation of labels from s to n to get input.
	 */
	private static <T> Pair<Node<T>, SubString> canonize(final Node<T> s, final SubString input) {
		Node<T> currentNode = s;

		// descend the tree as long as a proper label is found
		SubString remainder = input;

		while (!remainder.isEmpty()) {
			Edge<T> nextEdge = currentNode.getEdge(remainder);
			if (nextEdge == null || !nextEdge.isPrefix(remainder)) {
				break;
			}
			currentNode = nextEdge.getDest();
			remainder = remainder.substring(nextEdge.length());
		}

		return new Pair<>(currentNode, remainder);
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
	 * @param s          the node to start from
	 * @param stringPart the string to add to the tree
	 * @param rest       the rest of the string
	 * @param value      the value to add
	 */
	private Pair<Node<T>, SubString> update(
		Node<T> s,
		final SubString stringPart,
		final char newChar,
		final SubString rest,
		final T value
	) {
		assert !rest.isEmpty();
		assert rest.charAt(0) == newChar;

		SubString k = stringPart.append(newChar);

		// line 1
		Node<T> oldRoot = root;

		// line 1b
		Pair<Boolean, Node<T>> ret = testAndSplit(s, stringPart, newChar, rest, value);
		Node<T> r = ret.second();
		boolean endpoint = ret.first();

		Node<T> leaf;
		// line 2
		while (!endpoint) {
			// line 3
			Edge<T> tempEdge = r.getEdge(newChar);
			if (tempEdge != null) {
				// such a node is already present. This is one of the main differences from Ukkonen's case:
				// the tree can contain deeper nodes at this stage because different strings were added by previous iterations.
				leaf = tempEdge.getDest();
			} else {
				// must build a new leaf
				leaf = new Node<>();
				leaf.addRef(value);
				r.addEdge(new Edge<>(rest, leaf));
			}

			// update suffix link for newly created leaf
			if (activeLeaf != root) {
				activeLeaf.setSuffix(leaf);
			}
			activeLeaf = leaf;

			// line 4
			if (oldRoot != root) {
				oldRoot.setSuffix(r);
			}

			// line 5
			oldRoot = r;

			// line 6
			if (null == s.getSuffix()) { // root node
				assert (root == s);
				// this is a special case to handle what is referred to as node _|_ on the paper
				k = k.substring(1);
			} else {
				Pair<Node<T>, SubString> canonized = canonize(s.getSuffix(), safeCutLastChar(k));
				char nextChar = k.charAt(k.length() - 1);
				s = canonized.first();
				k = canonized.second().append(nextChar);
			}

			// line 7
			ret = testAndSplit(s, safeCutLastChar(k), newChar, rest, value);
			endpoint = ret.first();
			r = ret.second();
		}

		// line 8
		if (oldRoot != root) {
			oldRoot.setSuffix(r);
		}

		// make sure the active pair is canonical
		return canonize(s, k);
	}

	private static SubString safeCutLastChar(SubString subString) {
		if (subString.length() == 0) {
			return subString;
		}
		return subString.shorten(1);
	}

	@Override
	public String statistics() {
		return "GeneralizedSuffixTree:" +
			"\nNode size stats: \n" + this.root.nodeSizeStats() +
			"\nNode edge stats: \n" + this.root.nodeEdgeStats();
	}

	/**
	 * Print the tree for use by graphviz.
	 * To view, run the command: `dot -Tpng -O <filename>.dot`
	 */
	@SuppressWarnings("unused") // used for debugging
	public void printTree(PrintWriter out, boolean includeSuffixLinks) {
		root.printTree(out, includeSuffixLinks);
	}
}
