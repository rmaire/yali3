/* 
 * Copyright 2020 Uprise Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.uprisesoft.yali.ast.node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class Call extends Node implements Iterator {

    private final String name;
    private final java.util.List<Node> args;
    private Procedure definition;
    private Node result;
    private int callPos = 0;
    
    public Call(String name) {
        super(NodeType.PROCCALL);
        this.name = name;
        this.args = new ArrayList<>();
        this.result = Node.none();
    }

    public Procedure definition() {
        return definition;
    }

    public void definition(Procedure definition) {
        this.definition = definition;
    }
    
    public void arg(Node arg) {
        this.args.add(arg);
    }

    public List<Node> args() {
        return args;
    }

    public void args(List<Node> args) {
        this.args.clear();
        this.args.addAll(args);
    }
    
    public Node nextParameter() {
        int next = args.size();
        return children.get(next);
    }
    
    public boolean hasMoreCalls() {
        return callPos >= children.size()-1;
    }
    
    public Call nextCall(){
        return children.get(callPos++).toProcedureCall();
    }
    
    public void reset() {
        callPos = 0;
        args.clear();
        result = Node.none();
    }
    
    /**
     * Checks if all arguments are evaluated
     * @return true if all arguments are evaluated, false otherwise
     */
    public boolean ready() {
        if(definition == null) {
            System.out.println("DEFINITION: " + name);
        }
        return args.size() == definition.getArity();
    }
    
    public boolean evaluated() {
        return !result.type().equals(NodeType.NONE);
    }

    public Node result() {
        return result;
    }

    public void result(Node result) {
        this.result = result;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("(").append(name).append(" ");

        sb.append(
                children
                        .stream()
                        .map(e -> e.toString())
                        .collect(Collectors.joining(" ")
                        ));

        sb.append(")");

        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 31 * hash + (name == null ? 0 : name.hashCode());
        for (Node n : children) {
            hash = 31 * hash + (n == null ? 0 : n.hashCode());
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Call)) {
            return false;
        }

        final Call other = (Call) obj;

        if (this.hashCode() == other.hashCode()) {
            return true;
        }

        return true;
    }

    @Override
    public boolean hasNext() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object next() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
