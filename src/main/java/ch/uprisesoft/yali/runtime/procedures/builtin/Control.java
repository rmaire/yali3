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
package ch.uprisesoft.yali.runtime.procedures.builtin;

import ch.uprisesoft.yali.ast.node.Call;
import ch.uprisesoft.yali.ast.node.Procedure;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.exception.NodeTypeException;
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.scope.Scope;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.runtime.procedures.ProcedureProvider;
import ch.uprisesoft.yali.scope.VariableNotFoundException;
import java.util.ArrayList;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class Control implements ProcedureProvider {

    private Interpreter it;

    // Only here so we don't have to strip it out as edge cases in the Reader. Does nothing.
    public Node alias(Scope scope, java.util.List<Node> args) {
        return Node.nil();
    }

    public Node thing(Scope scope, java.util.List<Node> args) {

        final String name;
        switch (args.get(0).type()) {
            case SYMBOL:
                name = args.get(0).toSymbolWord().getSymbol();
                break;
            case QUOTE:
                name = args.get(0).toQuotedWord().getQuote();
                break;
            default:
                throw new NodeTypeException(args.get(0), args.get(0).type(), NodeType.SYMBOL, NodeType.QUOTE);
        }

        if (!it.env().thingable(name)) {
            throw new VariableNotFoundException(name);
        }

        Node value = it.env().thing(name);

        return value;
    }

    public Node local(Scope scope, java.util.List<Node> args) {
        final String name;

        switch (args.get(0).type()) {
            case SYMBOL:
                name = args.get(0).toSymbolWord().getSymbol();
                break;
            case QUOTE:
                name = args.get(0).toQuotedWord().getQuote();
                break;
            default:
                throw new NodeTypeException(args.get(0), args.get(0).type(), NodeType.SYMBOL, NodeType.QUOTE);
        }

        it.tracers().forEach(t -> t.local(name, it.env()));
        it.env().local(name);

        return Node.nil();
    }

    public Node make(Scope scope, java.util.List<Node> args) {
        final Node newVar;
        final String name;

        switch (args.get(0).type()) {
            case SYMBOL:
                name = args.get(0).toSymbolWord().getSymbol();
                break;
            case QUOTE:
                name = args.get(0).toQuotedWord().getQuote();
                break;
            default:
                throw new NodeTypeException(args.get(0), args.get(0).type(), NodeType.SYMBOL, NodeType.QUOTE);
        }

        newVar = args.get(1);

        it.env().make(name, newVar);

        return newVar;
    }

    public Node localmake(Scope scope, java.util.List<Node> args) {
        Node newVar = null;
        String name = "";

        switch (args.get(0).type()) {
            case SYMBOL:
                name = args.get(0).toSymbolWord().getSymbol();
                break;
            case QUOTE:
                name = args.get(0).toQuotedWord().getQuote();
                break;
            default:
                throw new NodeTypeException(args.get(0), args.get(0).type(), NodeType.SYMBOL, NodeType.QUOTE);
        }

        newVar = args.get(1);
        it.env().local(name);
        it.env().make(name, newVar);

        return newVar;
    }

    private java.util.List<Node> ifexprsToRun;

    public Node ifexpr(Scope scope, java.util.List<Node> args) {
        Node result = Node.none();

        if (ifexprsToRun == null) {

            ifexprsToRun = new ArrayList<>();

            Node condition = args.get(0);
            Node iftrue = args.get(1);

            if (!condition.type().equals(NodeType.BOOLEAN)) {
                throw new NodeTypeException(condition, condition.type(), NodeType.BOOLEAN);
            }

            if (condition.toBooleanWord().getBoolean()) {
                Node ast = it.read(iftrue.toList());
                ifexprsToRun.addAll(ast.getChildren());
            } else {
                result = it.output(Node.nil());
                return Node.nil();
            }

        }

        if (!ifexprsToRun.isEmpty()) {
            Call next = ifexprsToRun.remove(0).toProcedureCall();
            it.schedule(next);
        }

        return result;
    }

    private Node ifexprFinished(Scope scope, Node result) {
        if (ifexprsToRun.isEmpty()) {
            ifexprsToRun = null;
            return Node.bool(false);
        } else {
            return Node.bool(true);
        }
    }

    private java.util.List<Node> ifelseexprsToRun;

    public Node ifelseexpr(Scope scope, java.util.List<Node> args) {
        Node result = Node.none();

        if (ifelseexprsToRun == null) {
            ifelseexprsToRun = new ArrayList<>();

            Node condition = args.get(0);
            Node iftrue = args.get(1);
            Node iffalse = args.get(2);

            if (!condition.type().equals(NodeType.BOOLEAN)) {
                throw new NodeTypeException(condition, condition.type(), NodeType.BOOLEAN);
            }

            if (condition.toBooleanWord().getBoolean()) {
                result = run(scope, iftrue.toList());
            } else {
                result = run(scope, iffalse.toList());
            }

            if (condition.toBooleanWord().getBoolean()) {
                Node ast = it.read(iftrue.toList());
                ifelseexprsToRun.addAll(ast.getChildren());
            } else {
                Node ast = it.read(iffalse.toList());
                ifelseexprsToRun.addAll(ast.getChildren());
            }
        }

        if (!ifelseexprsToRun.isEmpty()) {
            Call next = ifelseexprsToRun.remove(0).toProcedureCall();
            it.schedule(next);
        }

        return result;
    }

    private Node ifelseexprFinished(Scope scope, Node result) {
        if (ifelseexprsToRun.isEmpty()) {
            ifelseexprsToRun = null;
            return Node.bool(false);
        } else {
            return Node.bool(true);
        }
    }

    private java.util.List<Node> repeatexprToRun;

    public Node repeat(Scope scope, java.util.List<Node> args) {
        Node result = Node.none();

        if (repeatexprToRun == null) {
            repeatexprToRun = new ArrayList<>();
            
            Node control = args.get(0);
            Node block = args.get(1);

            if (!control.type().equals(NodeType.INTEGER)) {
                if (!control.type().equals(NodeType.INTEGER)) {
                    throw new NodeTypeException(control, control.type(), NodeType.INTEGER);
                }
            }

            if (!block.type().equals(NodeType.LIST)) {
                throw new NodeTypeException(block, block.type(), NodeType.LIST);
            }

            Integer idx = control.toIntegerWord().getInteger();
            result = Node.nil();
            
            for (int i = 0; i < idx; i++) {
                Node ast = it.read(block.toList());
                repeatexprToRun.addAll(ast.getChildren());
//                result = run(scope, block.toList());
            }
        }
        
        if (!repeatexprToRun.isEmpty()) {
            Call next = repeatexprToRun.remove(0).toProcedureCall();
            it.schedule(next);
        }

        return result;
    }
    
    private Node repeatexprFinished(Scope scope, Node result) {
        if (repeatexprToRun.isEmpty()) {
            repeatexprToRun = null;
            return Node.bool(false);
        } else {
            return Node.bool(true);
        }
    }

    private java.util.List<Node> proceduresToRun;

    public Node run(Scope scope, java.util.List<Node> args) {

        Node result = Node.none();

        if (proceduresToRun == null) {
            proceduresToRun = new ArrayList<>();
            Node ast = it.read(args.get(0).toList());
            proceduresToRun.addAll(ast.getChildren());
        }

        Call next = proceduresToRun.remove(0).toProcedureCall();
        it.schedule(next);

        return result;
    }

    private Node run(Scope scope, ch.uprisesoft.yali.ast.node.List args) {

        Node result = Node.none();

        result = it.runBounded(
                it.read(
                        args
                )
        );
        return result;
    }

    private Node runFinished(Scope scope, Node result) {
        if (proceduresToRun.isEmpty()) {
            proceduresToRun = null;
            return Node.bool(false);
        } else {
            return Node.bool(true);
        }
    }

    public Node output(Scope scope, java.util.List<Node> args) {
        return it.output(args.get(0));
    }

    public Node stop(Scope scope, java.util.List<Node> args) {
        return Node.nil();
    }

    public Node pause(Scope scope, java.util.List<Node> arg) {
        it.pause();
        return Node.nil();
    }

    @Override
    public Interpreter registerProcedures(Interpreter interpreter) {
        this.it = interpreter;

        it.env().define(new Procedure("alias", (scope, val) -> this.alias(scope, val), (scope, val) -> Node.none(), "__original__", "__alias__"));
        it.env().define(new Procedure("thing", (scope, val) -> this.thing(scope, val), (scope, val) -> Node.none(), "__name__").macro());
        it.env().define(new Procedure("make", (scope, val) -> this.make(scope, val), (scope, val) -> Node.none(), "__name__", "__value__").macro());
        it.env().define(new Procedure("local", (scope, val) -> this.local(scope, val), (scope, val) -> Node.none(), "__name__").macro());
        it.env().define(new Procedure("localmake", (scope, val) -> this.localmake(scope, val), (scope, val) -> Node.none(), "__name__", "__value__").macro());
        it.env().define(new Procedure("repeat", (scope, val) -> this.repeat(scope, val), (scope, val) -> this.repeatexprFinished(scope, val), "__control__", "__block__").macro());
        it.env().define(new Procedure("run", (scope, val) -> this.run(scope, val), (scope, val) -> this.runFinished(scope, val), "__block__").macro());
        it.env().define(new Procedure("output", (scope, val) -> this.output(scope, val), (scope, val) -> Node.none(), "__block__"));
        it.env().define(new Procedure("stop", (scope, val) -> this.output(scope, val), (scope, val) -> Node.none()));
        it.env().define(new Procedure("ifelse", (scope, val) -> this.ifelseexpr(scope, val), (scope, val) -> this.ifelseexprFinished(scope, val), "__condition__", "__iftrue__", "__iffalse__").macro());
        it.env().define(new Procedure("if", (scope, val) -> this.ifexpr(scope, val), (scope, val) -> this.ifexprFinished(scope, val), "__condition__", "__iftrue__").macro());
        it.env().define(new Procedure("pause", (scope, val) -> this.pause(scope, val), (scope, val) -> Node.none()).macro());

        return it;
    }
}
