/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.runtime.interpreter;

import ch.uprisesoft.yali.ast.node.Call;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.ast.node.NodeType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author rma
 */
public class CallStack {

    private Node output = Node.none();

    private List<NodeIterator> trace = new ArrayList<>();

    private Call currentCall;

    public Node output() {
        return output;
    }

    public void output(Node lastResult) {
        this.output = lastResult;
    }

    public boolean schedule(Node node) {
        if(node.type() == NodeType.PROCEDURE) {
//            System.out.println("SCHEDULING PROCCALL");
            String name = node.toProcedureDef().getName();
            for(int i = trace.size() -2; i >= 0; i--) {
                NodeIterator currentNode = trace.get(i);
                if(currentNode.node().type() == NodeType.PROCEDURE && node.toProcedureDef().getName().equals(name)) {
                    for(int j = trace.size()-1; j>=i; j--) {
                        trace.remove(j);
                        currentNode.reset();
                        trace.add(currentNode);
                    }
//                    System.out.println("RECURSION: " + (trace.size()- i) + " -> " + trace.get(i).node().toString());
                    return true;
                } else {
//                     System.out.println("NOT RECURSION: " + currentNode.type());
                }
            }
        }
        
        trace.add(new NodeIterator(node));
        return false;
    }

    public boolean hasNext() {
        if (trace.isEmpty()) {
            return false;
        }

        if (trace.get(trace.size() - 1).hasNext()) {
            return true;
        } else {
            trace.remove(trace.size() - 1);
            return false;
        }
    }

    public Node next() {
        Node ret = trace.get(trace.size() - 1).next();
        if (ret.type().equals(NodeType.PROCCALL)) {
            currentCall = ret.toProcedureCall();
        }
        return ret;
    }

    public Call currentCall() {
        return currentCall;
    }
    
    public void completeCurrentCall() {
        this.currentCall = null;
    }
}
