package com.giyeok.dexdio.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis.BasicBlock;
import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis.Edge;

public class GraphUtil {
	
	public static ArrayList<BasicBlock> getOutBasicBlocksOf(Edge[] edges, BasicBlock node) {
		ArrayList<BasicBlock> outedges = new ArrayList<BasicBlock>();
		
		for (Edge edge: edges) {
			if (edge.getSource() == node) {
				outedges.add(edge.getDestination());
			}
		}
		return outedges;
	}
	
	public static Edge[] getOutEdgesOf(Edge[] edges, BasicBlock node) {
		ArrayList<Edge> outedges = new ArrayList<Edge>();
		
		for (Edge edge: edges) {
			if (edge.getSource() == node) {
				outedges.add(edge);
			}
		}
		return outedges.toArray(new Edge[0]);
	}
	
	public static BasicBlock[] getInBasicBlocksOf(Edge[] edges, BasicBlock node) {
		ArrayList<BasicBlock> outedges = new ArrayList<BasicBlock>();
		
		for (Edge edge: edges) {
			if (edge.getDestination() == node) {
				outedges.add(edge.getDestination());
			}
		}
		return outedges.toArray(new BasicBlock[0]);
	}
	
	public static Edge[] getInEdgesOf(Edge[] edges, BasicBlock node) {
		ArrayList<Edge> inedges = new ArrayList<Edge>();
		
		for (Edge edge: edges) {
			if (edge.getDestination() == node) {
				inedges.add(edge);
			}
		}
		return inedges.toArray(new Edge[0]);
	}
	
	/**
	 * edges와 edges에 포함된 모든 노드로 이루어진 그래프에서 start에서 출발하여 end에 도달하기 직전까지 포함하여 도달할 수 있는 모든 노드의 집합을 반환한다
	 * end가 null이면 start로부터 도달할 수 있는 모든 노드의 집합을 반환한다
	 * @param edges
	 * @param start
	 * @param end
	 * @return
	 */
	public static Set<BasicBlock> getReachables(Edge[] edges, BasicBlock start, BasicBlock end) {
		return getReachables(null, edges, start, end, new HashSet<BasicBlock>());
	}
	
	/**
	 * nodes와 edges로 이루어진 그래프에서 start에서 출발하여 end에 도달하기 직전까지 포함하여 도달할 수 있는 모든 노드의 집합을 반환한다
	 * end가 null이면 start로부터 도달할 수 있는 모든 노드의 집합을 반환한다
	 * @param edges
	 * @param start
	 * @param end
	 * @return
	 */
	public static Set<BasicBlock> getReachables(BasicBlock[] nodes, Edge[] edges, BasicBlock start, BasicBlock end) {
		return getReachables(nodes, edges, start, end, new HashSet<BasicBlock>());
	}
	
	private static Set<BasicBlock> getReachables(BasicBlock[] nodes, Edge[] edges, BasicBlock start, BasicBlock end, Set<BasicBlock> set) {
		if (end != null && isReachable(nodes, edges, end, start)) {
			return set;
		}
		set.add(start);
		for (BasicBlock outnode: getOutBasicBlocksOf(edges, start)) {
			if ((nodes == null || ArraysUtil.existsExact(nodes, outnode)) && (! set.contains(outnode))) {
				set.addAll(getReachables(nodes, edges, outnode, end, set));
			}
		}
		return set;
	}
	
	public static boolean isReachable(BasicBlock[] nodes, Edge[] edges, BasicBlock start, BasicBlock end) {
		return isReachable(nodes, edges, start, end, new HashSet<BasicBlock>());
	}
	
	private static boolean isReachable(BasicBlock[] nodes, Edge[] edges, BasicBlock start, BasicBlock end, Set<BasicBlock> visited) {
		visited.add(start);
		if (start == end) {
			return true;
		}
		for (BasicBlock outnode: getOutBasicBlocksOf(edges, start)) {
			if ((nodes == null || ArraysUtil.existsExact(nodes, outnode)) && (! visited.contains(outnode))) {
				if (isReachable(nodes, edges, outnode, end, visited)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * edges 중에 nodes에 속한 노드로부터 target으로 들어오는(destination이 target인) 엣지가 있으면 true, 없으면 false를 반환한다
	 * nodes == null이면 edges 전체를 검사한다
	 * @param edges
	 * @param nodes
	 * @param target
	 * @return
	 */
	public static boolean existsInEdge(Edge[] edges, BasicBlock[] nodes, BasicBlock target) {
		for (Edge edge: edges) {
			if (target == edge.getDestination()) {
				if (nodes == null || ArraysUtil.existsExact(nodes, edge.getSource())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * edges와 nodes로 이루어진 그래프 내에서, starts에 속한 모든 노드에서 모두 도달할 수 있는 가장 가까운 노드를 찾아서 반환한다.
	 * 즉, getConfluence가 반환하는 노드를 result라고 할 때 다음의 조건을 만족시켜야 한다
	 * isReachable(start, result) for each start in starts
	 * AND
	 * for each node N in (path(start, result) - result), there exists !isReachable(start, N) for each start in starts
	 * 이 조건을 만족시키는 노드가 없을 경우 null을 반환한다
	 * @param edges
	 * @param nodes
	 * @param starts
	 * @return
	 */
	public static BasicBlock getConfluence(BasicBlock[] nodes, Edge[] edges, BasicBlock[] starts) {
		if (starts == null || starts.length == 0) {
			return null;
		}
		BasicBlock result = starts[0];
		Set<BasicBlock> reachables = getReachables(nodes, edges, result, null);
		for (int i = 1; i < starts.length; i++) {
			result = getConfluence2(nodes, edges, reachables, starts[i], new HashSet<BasicBlock>());
			reachables = getReachables(nodes, edges, result, null);
		}
		return result;
	}
	
	private static BasicBlock getConfluence2(BasicBlock[] nodes, Edge[] edges, Set<BasicBlock> reachables, BasicBlock next, Set<BasicBlock> visited) {
		if (reachables.contains(next)) {
			return next;
		}
		visited.add(next);
		for (Edge outedge: getOutEdgesOf(edges, next)) {
			BasicBlock dst = (BasicBlock) outedge.getDestination();
			if ((nodes == null || ArraysUtil.existsExact(nodes, dst)) && (! visited.contains(dst))) {
				BasicBlock r = getConfluence2(nodes, edges, reachables, dst, visited);
				if (r != null) {
					return r;
				}
			}
		}
		return null;
	}
}
