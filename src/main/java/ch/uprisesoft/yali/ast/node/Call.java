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
    private boolean evaluated = false;

    public Call(String name) {
        super(NodeType.PROCCALL);
        this.name = name;
        this.args = new ArrayList<>();
//        this.result = null;
    }

    public Procedure definition() {
        return definition;
    }

    public void definition(Procedure definition) {
        this.definition = definition;
    }

    public void arg(Node arg) {
        System.out.println("ADDING ARG " + arg + " TO " + name);
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
//        System.out.println(name + ": GET CHILD " + (paramPos+1) + " OF " + children.size() + " -> " + children.get(paramPos));
        return children.get(args.size());
    }

    public boolean hasMoreCalls() {
        return callPos <= definition.getChildren().size() - 1;
    }

    public Call nextCall() {
        Call next = definition.getChildren().get(callPos++).toProcedureCall();
        return next;
    }

    public void reset() {
        callPos = 0;
        args.clear();
        result = null;
    }

    /**
     * Checks if all arguments are evaluated
     *
     * @return true if all arguments are evaluated, false otherwise
     */
    public boolean hasMoreParameters() {
        boolean more = args.size() < children.size();
        if (!more) {
            System.out.println(name + " READY WITH " + args.size() + " ARGS!");
        }
        return more;
    }

    public void evaluated(boolean e) {
        this.evaluated = e;
    }

    public boolean evaluated() {
//        // A native procedure is evaluated when it has a result
//        boolean isNativeAndEvaluated = definition().isNative()
//                && evaluated;
//
//        // A user defined procedure is evaluated when it has no more calls and a result
//        boolean isUserDefinedAndEvaluated = !definition().isNative()
//                && !hasMoreCalls()
//                && result != null;
//        
////        System.out.println("CALL " + name + " -> " + isNativeAndEvaluated + "/" + isUserDefinedAndEvaluated);
//        
        return evaluated;
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
