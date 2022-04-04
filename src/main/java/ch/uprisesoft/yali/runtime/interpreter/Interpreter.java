/*
 * Copyright 2021 rma.
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
package ch.uprisesoft.yali.runtime.interpreter;

import ch.uprisesoft.yali.ast.node.Call;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.exception.NodeTypeException;
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.parser.Parser;
import ch.uprisesoft.yali.runtime.io.InputGenerator;
import ch.uprisesoft.yali.runtime.io.OutputObserver;
import ch.uprisesoft.yali.runtime.procedures.FunctionNotFoundException;
import ch.uprisesoft.yali.runtime.procedures.builtin.Arithmetic;
import ch.uprisesoft.yali.runtime.procedures.builtin.Control;
import ch.uprisesoft.yali.runtime.procedures.builtin.Data;
import ch.uprisesoft.yali.runtime.procedures.builtin.IO;
import ch.uprisesoft.yali.runtime.procedures.builtin.Logic;
import ch.uprisesoft.yali.runtime.procedures.builtin.Template;
import ch.uprisesoft.yali.scope.Environment;
import ch.uprisesoft.yali.scope.Scope;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author rma
 */
public class Interpreter implements OutputObserver {

    private List<Tracer> tracers = new ArrayList<>();

    private Environment env = new Environment();
    private boolean paused = false;

    private java.util.Stack<Call> stack = new java.util.Stack<>();
    
    private Node lastResult;

    public Interpreter() {
        env.push(new Scope("global"));
    }

    public void addTracer(Tracer tracer) {
        tracers.add(tracer);
        env.addTracer(tracer);
    }

    public List<Tracer> tracers() {
        return tracers;
    }

    /**
     * Interpreting functionality, public interface
     */
    public Node run(Node node) {
        tracers.forEach(t -> t.run(node));

        if (!node.type().equals(NodeType.LIST)) {
            throw new NodeTypeException(node, node.type(), NodeType.LIST);
        }

        for (Node n : node.getChildren()) {

            Call call = n.toProcedureCall();

            if (!env.defined(call.getName())) {
                throw new FunctionNotFoundException(call.getName());
            }

            schedule(call);

            while (tick()) {
            }

        }

        if(lastResult != null) {
            return lastResult;
        }
        
        if (stack.size() > 0) {
            return stack.pop().result();
        }
        return Node.none();
    }

    public Node run(Call call) {
        tracers.forEach(t -> t.run(call));

        if (!env.defined(call.getName())) {
            throw new FunctionNotFoundException(call.getName());
        }

        schedule(call);

        while (tick()) {
        }

        if(lastResult != null) {
            return lastResult;
        }
        
        return stack.pop().result();
    }

    public Node runBounded(Node node) {
//        tracers.forEach(t -> t.run(node));

        java.util.Stack<Call> save = stack;
        stack = new java.util.Stack<>();

        Node result = run(node);

        stack = save;
        return result;
    }

    public Node resume() {
        tracers.forEach(t -> t.resume(stack.peek()));
        paused = false;

        while (tick()) {
        }

        return stack.pop().result();
    }

    public void pause() {
        tracers.forEach(t -> t.pause(stack.peek()));
        paused = true;
    }

    public boolean paused() {
        return paused;
    }

    public Node read(String source) {
        return new Parser(this).read(source);
    }

    public Node read(ch.uprisesoft.yali.ast.node.List list) {
        return new Parser(this).read(list);
    }

    public Environment env() {
        return env;
    }
    
    public Node output(Node output){
        lastResult = output;
        return output;
    }

    public boolean tick() {

        if (paused) {
            return false;
        }

        // Nothing happened, no program loaded.
        if (stack.empty()) {
            return false;
        }

        // If only one evaluated procedure is on the stack, the program is finished
        // and stack.peek.result() gives the final result
        if (stack.size() == 1 && stack.peek().evaluated()) {
            return false;
        }
        
        tracers.forEach(t -> t.apply(stack.peek()));
        
        if(stack.peek().evaluated()) {
            lastResult = stack.peek().result();
        }

        // If more than one call is on the stack, and the current call is evaluated,
        // this must be an arg or a result to the previous call. Pop the current call, get it's
        // result and add it to the args or the result  of the precious call
        if (stack.size() > 1 && stack.peek().evaluated()) {
            if (!stack.peek().definition().isMacro()) {
                tracers.forEach(t -> t.unscope(env.peek().getScopeName(), env));
                env.pop();
            }

            Node lastResult = stack.pop().result();

            // If previous call is ready but has no result, set the last
            // result. If not, add it to the args
//            System.out.println("HERE: " + stack.peek().getName() + " -> " + stack.peek().ready() + " -> " + stack.peek().evaluated());
            if (stack.peek().ready() && !stack.peek().evaluated()) {
                stack.peek().result(lastResult);
                return true;
            } else {
                stack.peek().arg(lastResult);
            }
            return true;
        }

        Call call = stack.peek();

        if (!call.ready()) {
            Node param = call.nextParameter();

            tracers.forEach(t -> t.arg(call.getName(), param, env));

            if (!param.type().equals(NodeType.PROCCALL)) {
                call.arg(param);
            } else {
                schedule(param.toProcedureCall());
            }
            return true;
        } else {
            //define all args in the local scope
            for(int i = 0; i < call.definition().getArity(); i++) {
                env.local(call.definition().getArgs().get(i));
                env.make(
                        call.definition().getArgs().get(i),
                        call.args().get(i)
                );
            }
        }

        if (call.ready() && (call.definition().isNative() || call.definition().isMacro())) {
            tracers.forEach(t -> t.callPrimitive(call.getName(), call.args(), env));
            Node result = call.definition().getNativeCall().apply(env.peek(), call.args());
            call.result(result);
        } else if (call.ready() && !call.definition().isNative() && call.hasMoreCalls()) {
            tracers.forEach(t -> t.call(call.getName(), call.args(), env));
            schedule(call.nextCall());
        }

        // Check if there's more to do
        if (stack.size() == 1 && !stack.peek().evaluated()) {
            return true;
        }
        if (stack.size() > 1) {
            return true;
        }

        return false;
    }

    private void schedule(Call call) {
        call.definition(env.procedure(call.getName()));
        stack.push(call);
        if (!call.definition().isMacro()) {
            env.push(new Scope(call.getName()));
            tracers.forEach(t -> t.scope(env.peek().getScopeName(), env));
        }
    }

    public Interpreter loadStdLib(OutputObserver oo) {

        Logic logic = new Logic();
        logic.registerProcedures(this);

        Control control = new Control();
        control.registerProcedures(this);

        Arithmetic arithmetic = new Arithmetic();
        arithmetic.registerProcedures(this);

        Template template = new Template();
        template.registerProcedures(this);

        Data data = new Data();
        data.registerProcedures(this);

        return this;
    }

    public Interpreter loadStdLib(OutputObserver oo, InputGenerator ig) {
        IO com = new IO();
        com.register(oo);
        com.register(ig);
        com.registerProcedures(this);

        return loadStdLib(oo);
    }

    public java.util.List<String> stringify(Node arg) {
        java.util.List<String> stringifiedArgs = new ArrayList<>();
        if (arg.type().equals(NodeType.LIST)) {
            stringifiedArgs.addAll(stringify(arg.getChildren()));
        } else {
            stringifiedArgs.add(arg.toString());
        }
        return stringifiedArgs;
    }

    public java.util.List<String> stringify(java.util.List<Node> args) {
        java.util.List<String> stringifiedArgs = new ArrayList<>();
        for (Node arg : args) {
            if (arg.type().equals(NodeType.LIST)) {
                stringifiedArgs.addAll(stringify(arg.getChildren()));
            } else {
                stringifiedArgs.add(arg.toString());
            }
        }
        return stringifiedArgs;
    }

    /**
     * Observer and helper methods
     */
    @Override
    public void inform(String output) {
    }
}
