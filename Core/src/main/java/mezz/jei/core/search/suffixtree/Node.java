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

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMaps;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import mezz.jei.core.util.SubString;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Represents a node of the generalized suffix tree graph
 *
 * @see GeneralizedSuffixTree
 * <p>
 * Edited by mezz:
 * - Use Java 6 features
 * - improve performance of search by passing a set around instead of creating new ones and using addAll
 * - only allow full searches
 * - add nullable/nonnull annotations
 * - formatting
 * - refactored and optimized
 */
public class Node<T> extends SubString {

	/**
	 * The payload array used to store the data (indexes) associated with this node.
	 * In this case, it is used to store all property indexes.
	 */
	private Collection<T> data;

	/**
	 * The set of edges starting from this node
	 */
	private Char2ObjectMap<Node<T>> edges;

	/**
	 * The suffix link as described in Ukkonen's paper.
	 * if str is the string denoted by the path from the root to this, this.suffix
	 * is the node denoted by the path that corresponds to str without the first char.
	 */
	@Nullable
	private Node<T> suffix;

	/**
	 * Creates a new Node
	 */
	Node(SubString string) {
		super(string);
		edges = Char2ObjectMaps.emptyMap();
		data = List.of();
		suffix = null;
	}

	/**
	 * Gets data from the payload of both this node and its children, the string representation
	 * of the path to this node is a substring of the one of the children nodes.
	 */
	public void getData(Consumer<Collection<T>> resultsConsumer) {
		if (!this.data.isEmpty()) {
			resultsConsumer.accept(Collections.unmodifiableCollection(this.data));
		}
		for (Node<T> dest : edges.values()) {
			dest.getData(resultsConsumer);
		}
	}

	/**
	 * Adds the given <tt>index</tt> to the set of indexes associated with <tt>this</tt>
	 * returns false if this node already contains the ref
	 */
	void addRef(T value) {
		if (contains(value)) {
			return;
		}

		addValue(value);

		// add this reference to all the suffixes as well
		Node<T> iter = this.suffix;
		while (iter != null) {
			if (!iter.contains(value)) {
				iter.addValue(value);
				iter = iter.suffix;
			} else {
				break;
			}
		}
	}

	/**
	 * Tests whether a node contains a reference to the given index.
	 *
	 * @param value the value to look for
	 * @return true <tt>this</tt> contains a reference to index
	 */
	protected boolean contains(T value) {
		return data.contains(value);
	}

	void addEdge(Node<T> edge) {
		char firstChar = edge.charAt(0);

		switch (edges.size()) {
			case 0 -> edges = Char2ObjectMaps.singleton(firstChar, edge);
			case 1 -> {
				Char2ObjectMap<Node<T>> newEdges = new Char2ObjectOpenHashMap<>(edges);
				newEdges.put(firstChar, edge);
				edges = newEdges;
			}
			default -> edges.put(firstChar, edge);
		}
	}

	@Nullable
	Node<T> getEdge(char ch) {
		return edges.get(ch);
	}

	@Nullable
	Node<T> getEdge(SubString string) {
		if (string.isEmpty()) {
			return null;
		}
		char ch = string.charAt(0);
		return edges.get(ch);
	}

	@Nullable
	Node<T> getSuffix() {
		return suffix;
	}

	void setSuffix(Node<T> suffix) {
		this.suffix = suffix;
	}

	protected void addValue(T value) {
		switch (data.size()) {
			case 0 -> data = List.of(value);
			case 1 -> data = List.of(data.iterator().next(), value);
			case 2 -> {
				List<T> newData = new ArrayList<>(4);
				newData.addAll(data);
				newData.add(value);
				data = newData;
			}
			case 16 -> {
				// "upgrade" data to a Set once it's getting bigger,
				// to improve its `contains` performance.
				Collection<T> newData = Collections.newSetFromMap(new IdentityHashMap<>());
				newData.addAll(data);
				newData.add(value);
				data = newData;
			}
			default -> data.add(value);
		}
	}

	@Override
	public String toString() {
		return "Node: edge: " + super.toString() + " size:" + data.size() + " Edges: " + edges;
	}

	public IntSummaryStatistics nodeSizeStats() {
		return nodeSizes().summaryStatistics();
	}

	private IntStream nodeSizes() {
		return IntStream.concat(
			IntStream.of(data.size()),
			edges.values().stream().flatMapToInt(Node::nodeSizes)
		);
	}

	public String nodeEdgeStats() {
		IntSummaryStatistics edgeCounts = nodeEdgeCounts().summaryStatistics();
		IntSummaryStatistics edgeLengths = nodeEdgeLengths().summaryStatistics();
		return "Edge counts: " + edgeCounts +
			"\nEdge lengths: " + edgeLengths;
	}

	private IntStream nodeEdgeCounts() {
		return IntStream.concat(
			IntStream.of(edges.size()),
			edges.values().stream().flatMapToInt(Node::nodeEdgeCounts)
		);
	}

	private IntStream nodeEdgeLengths() {
		return IntStream.concat(
			edges.values().stream().mapToInt(Node::length),
			edges.values().stream().flatMapToInt(Node::nodeEdgeLengths)
		);
	}

	public void printTree(PrintWriter out, boolean includeSuffixLinks) {
		out.println("digraph {");
		out.println("\trankdir = LR;");
		out.println("\tordering = out;");
		out.println("\tedge [arrowsize=0.4,fontsize=10]");
		out.println("\t" + nodeId(this) + " [label=\"\",style=filled,fillcolor=lightgrey,shape=circle,width=.1,height=.1];");
		out.println("//------leaves------");
		printLeaves(out);
		out.println("//------internal nodes------");
		printInternalNodes(this, out);
		out.println("//------edges------");
		printEdges(out);
		if (includeSuffixLinks) {
			out.println("//------suffix links------");
			printSLinks(out);
		}
		out.println("}");
	}

	private void printLeaves(PrintWriter out) {
		if (edges.isEmpty()) {
			out.println("\t" + nodeId(this) + " [label=\"" + data + "\",shape=point,style=filled,fillcolor=lightgrey,shape=circle,width=.07,height=.07]");
		} else {
			for (Node<T> edge : edges.values()) {
				edge.printLeaves(out);
			}
		}
	}

	private void printInternalNodes(Node<T> root, PrintWriter out) {
		if (this != root && !edges.isEmpty()) {
			out.println("\t" + nodeId(this) + " [label=\"" + data + "\",style=filled,fillcolor=lightgrey,shape=circle,width=.07,height=.07]");
		}

		for (Node<T> edge : edges.values()) {
			edge.printInternalNodes(root, out);
		}
	}

	private void printEdges(PrintWriter out) {
		for (Node<T> child : edges.values()) {
			out.println("\t" + nodeId(this) + " -> " + nodeId(child) + " [label=\"" + child + "\",weight=10]");
			child.printEdges(out);
		}
	}

	private void printSLinks(PrintWriter out) {
		if (suffix != null) {
			out.println("\t" + nodeId(this) + " -> " + nodeId(suffix) + " [label=\"\",weight=0,style=dotted]");
		}
		for (Node<T> edge : edges.values()) {
			edge.printSLinks(out);
		}
	}

	private static <T> String nodeId(Node<T> node) {
		return "node" + Integer.toHexString(node.hashCode()).toUpperCase();
	}
}
