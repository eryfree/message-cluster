package com.erybobo.message.core.utils;

import com.google.common.graph.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created on 2023/9/27.
 * Description:
 * Copyright (c) 2018, 成都冰鉴信息科技有限公司
 * All rights reserved.
 *
 * @author zhangbo
 */
public class GraphUtil {


    public static void main(String[] args) {
        MutableValueGraph<String, Integer> graph = ValueGraphBuilder.directed().allowsSelfLoops(false).build();
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        graph.putEdgeValue("A", "B", 1);
        graph.putEdgeValue("B", "C", 2);
        graph.putEdgeValue("C", "D", 3);
        graph.putEdgeValue("D", "A", 4);  // Adding a cycle


        System.out.println(hasCycleUsingKahn(graph));
    }

    public static boolean hasCycleUsingKahn(MutableValueGraph<String, Integer> graph) {
        Queue<String> queue = new LinkedList<>();
        List<String> topologicalOrder = new ArrayList();

        // Initialize the queue with nodes that have no incoming edges (in-degree = 0).
        for (String node : graph.nodes()) {
            if (graph.inDegree(node) == 0) {
                queue.offer(node);
            }
        }

        while (!queue.isEmpty()) {
            String node = queue.poll();
            topologicalOrder.add(node);

            for (String successor : graph.successors(node)) {
                graph.removeEdge(node, successor);
                if (graph.inDegree(successor) == 0) {
                    queue.offer(successor);
                }
            }
        }

        // If topological order has the same size as the original graph, it's a DAG.
        return topologicalOrder.size() != graph.nodes().size();
    }

    // 判断是否有环
    /*public static boolean hasCycle(Graph<Object> graph) {
        int numNodes = graph.nodes().size();
        int[] inDegree = new int[numNodes];
        Graph<Object> newGraph = GraphBuilder.from(graph).build();
        for (Object o : newGraph.nodes()) {
            newGraph.successors()
            newGraph.inDegree(o);
        }


        for (int i=0; i<newGraph.nodes().size();i++) {
            newGraph.
        }

        // 计算每个节点的入度
        for (List<Integer> neighbors : graph) {
            for (int neighbor : neighbors) {
                inDegree[neighbor]++;
            }
        }

        Queue<Integer> queue = new LinkedList<>();
        // 将入度为0的节点加入队列
        for (int i = 0; i < numNodes; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
            }
        }

        int count = 0;
        while (!queue.isEmpty()) {
            int node = queue.poll();
            count++;

            for (int neighbor : graph.get(node)) {
                inDegree[neighbor]--;
                if (inDegree[neighbor] == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        // 如果图中所有节点都能被访问，说明无环
        return count != numNodes;
    }*/
}
