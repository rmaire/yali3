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

    private Node lastResult = Node.none();

    private List<Iterator<Node>> iterators = new ArrayList<>();

    private Call currentCall;
//    private boolean paused = false;

    public Node lastResult() {
        return lastResult;
    }

    public void lastResult(Node lastResult) {
        this.lastResult = lastResult;
    }

//    public void push(Iterator<Node> iterator) {
//        iterators.add(iterator);
//    }
    public void push(Node node) {
        iterators.add(node.getChildren().iterator());
    }

    public Iterator<Node> pop() {
        Iterator<Node> ret = iterators.get(iterators.size() - 1);
        iterators.remove(iterators.size() - 1);
        return ret;
    }

    public boolean hasNext() {
        if (iterators.isEmpty()) {
            return false;
        }

        if (iterators.get(iterators.size() - 1).hasNext()) {
            return true;
        } else {
            iterators.remove(iterators.size() - 1);
            return false;
        }
    }

    public Node next() {
        Node ret = iterators.get(iterators.size() - 1).next();
        if (ret.type().equals(NodeType.PROCCALL)) {
            currentCall = ret.toProcedureCall();
        }
        return ret;
    }

    public Call currentCall() {
        return currentCall;
    }
    
    public void doneCurrentCall() {
        this.currentCall = null;
    }

    private void currentCall(Call currentCall) {
        this.currentCall = currentCall;
    }

//    public boolean paused() {
//        return paused;
//    }
//
//    public void pause() {
//        this.paused = true;
//    }
//
//    public void resume() {
//        this.paused = false;
//    }
}
