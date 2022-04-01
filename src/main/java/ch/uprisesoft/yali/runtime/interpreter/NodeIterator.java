/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.runtime.interpreter;

import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.ast.node.NodeType;
import java.util.Iterator;
import java.util.Optional;

/**
 *
 * @author rma
 */
public class NodeIterator implements Iterator<Node> {
    
    private final Node node;
    private Iterator<Node> nodeIter;

    public NodeIterator(Node node) {
        this.node = node;
        reset();
    }

    @Override
    public boolean hasNext() {
        return nodeIter.hasNext();
    }

    @Override
    public Node next() {
        return nodeIter.next();
    }

    public Node node() {
        return node;
    }
    
    public void reset(){
        this.nodeIter = node.getChildren().iterator();
    }
}
