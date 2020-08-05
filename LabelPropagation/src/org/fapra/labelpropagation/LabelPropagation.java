/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fapra.labelpropagation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.statistics.spi.Statistics;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;

/**
 *
 * @author arm
 */
public class LabelPropagation implements Statistics {

    @Override
    public void execute(GraphModel gm) {
        DirectedGraph graph = gm.getDirectedGraph();
        DirectedGraph aggregatedGraph = gm.getDirectedGraph();
        List<String> communityList = new ArrayList();
        // Zu Beginn werden 5 Lösungen für das Netzwerk erzeugt und in einer Liste gespeichert.
        List<Map<Node, String>> solutionList = new ArrayList();
        for (int i = 0; i < 5; i++) {
           solutionList.add(assignLabels(graph));
        }
        //Die Lösungen werden aggregiert.
        Map<Node, String> aggregatedMap = solutionList.get(0);
        for(int i=1;i<solutionList.size();i++){
            System.out.println(i+"th solution aggregation ");
            aggregatedMap=  aggregateLabels(aggregatedGraph, solutionList.get(i),aggregatedMap);
        }
        for (Node n : aggregatedGraph.getNodes()) {
            if (!communityList.contains(aggregatedMap.get(n))) {
                communityList.add(aggregatedMap.get(n));
            }
        }
        System.out.println(" so many communities: " + communityList.size());
        for (String s:communityList){
        System.out.println(s);
        }
    }

    /**
     * Der im Parameter übergegebene Graph g bekommt Labels zugewiesen, die mit
     * s vorangestellt erzeugt werden. Diese Labels werden sortiert und dann mit
     * den Labels im aggregatedLabels aggregiert.
     *
     * @param g
     * @param aggegatedLabels
     * @param s
     * @return
     */
    private Map<Node,String> aggregateLabels(DirectedGraph g, Map<Node, String> aggegatedLabels,
        Map<Node, String> currentMap) {
        //verschmelze die Labels aus den aktuellen und alten Listen zusammen. 
        for (Node n : g.getNodes()) {
            
            n.setLabel(aggegatedLabels.get(n) + "_" + currentMap.get(n));
        }
        return assignLabels(g);
    }

    /**
     * Startet die Schleife und macht weiter bis kein Knoten mehr gefunden wird,
     * der noch nicht das Label der Mehrheit seiner Nachbarknoten hat.
     *
     * @param graph
     * @param nodes
     */
    private Map<Node, String> assignLabels(DirectedGraph graph) {
        Map<Node, String> lMap = new HashMap();
        for (Node n : graph.getNodes()) {
            lMap.put(n, n.getLabel());
        }
        boolean labelcomplete = false;
        while (labelcomplete == false) {
            labelcomplete = true;
            for (Node n1 : graph.getNodes()) {
                System.out.println();
                System.out.println();
                System.out.println();
                //iteriere durch die Nachbarn eines Knoten und füge deren Labels in ein Map ein.
                Map<String, Integer> labelMap = new HashMap();
                for (Node n2 : graph.getNeighbors(n1)) {
                    //bei selfloops springe weiter.
                    if (n2.getId() == n1.getId()) {
                        continue;
                    }
                    //wenn ein Label in labelMap nicht vorkommt, bekommt er beim Einfügen den  Value 1.
                    //wenn ein Label in labelMap vorkommt,ersetzt er sein Duplikat und bekommt  dessen Value + 1.
                    if (labelMap.containsKey(lMap.get(n2))) {
                        int a=labelMap.get(lMap.get(n2))+1;
                        labelMap.put(lMap.get(n2),  a);
                    } else {
                        labelMap.put(lMap.get(n2), 1);
                    }
                }
                //sortiere die Labels der Nachbarn basiert auf den Frequenzen.
                Map<String, Integer> sortedMap = sortLabel(labelMap);
                System.out.println("current node: "+lMap.get(n1)+" ");
                for (String s:sortedMap.keySet()){
                    System.out.print(s+":" +sortedMap.get(s)+",");
                }
                List<String> labelList = new ArrayList<>(sortedMap.keySet());
                List<Integer> frequencyList = new ArrayList<>(sortedMap.values());
                //Falls das Label des aktuellen Knoten dem ersten Label auf labelList nicht entspricht, 
                //wird ein neues Label für diesen Knoten gewählt.
                System.out.println();
                if (!lMap.get(n1).equals(labelList.get(0))) {
                    String chosenLabel=chooseHighestFrequencyLabel(labelList, frequencyList);
                    lMap.put(n1, chosenLabel);
                    System.out.println("chosenlabel: "+chosenLabel);

                    //labelComplete wird auf false gesetzt, da ein Knoten ein neues Label bekommen hat.
                    labelcomplete = false;
                }
                System.out.println();
            }
        }

        return lMap;
    }

    /**
     * Sortiert eine HashMap absteigend basiert auf dem Value.
     *
     * @param unsortMap
     * @return
     *
     */
    private Map<String, Integer> sortLabel(Map<String, Integer> unsortMap) {
        // wandle die Map in eine List um.
        List<Map.Entry<String, Integer>> list
                = new LinkedList<>(unsortMap.entrySet());
        //  Sortiere diese List mit Collections.sort() und einem Comparator
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1,
                    Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        // iteriere durch die sortierte List und kopiere diese in der Reihenfolge in ein LinkedHashMap
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    /**
     * Es wird ein zufällig gewähltes element von list1 mit höchster Frequenz
     * zurückgegeben.
     *
     * @param list1
     * @param list2
     * @return
     */
    private String chooseHighestFrequencyLabel(List<String> list1, List<Integer> list2) {

        List<String> list3 = new ArrayList<>();
        list3.add(list1.get(0));
        int i = 1;
        //Vergleiche die list2 Elemente beginnend ab dem zweiten mit dem ersten Element.
        // wenn zwei Elemente gleich sind, wird das Element der list1 mit Index i in list3 kopiert. 
        if (list1.size() == 1) {
            return list1.get(0);
        } else {
            while (i < list2.size() && Objects.equals(list2.get(0), list2.get(i))) {
                list3.add(list1.get(i));
                i++;
            }
        }
        //wähle  zufällig ein Element von list3 aus und gebe dieses zurück.
        return list3.get(new Random().nextInt(list3.size()));
    }

    @Override
    public String getReport() {
        // @TODO hier einen sinnvolleren Report ausgeben
        return "Plugin fertig ausgeführt!";
    }
}
