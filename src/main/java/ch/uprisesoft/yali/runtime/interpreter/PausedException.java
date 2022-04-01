/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.runtime.interpreter;

import ch.uprisesoft.yali.ast.node.Node;

/**
 *
 * @author rmaire
 */
public class PausedException  extends RuntimeException {
    
    private Node node;

    public PausedException(Node node) {
        this.node = node;
    }

    public Node node() {
        return node;
    }
    
}
