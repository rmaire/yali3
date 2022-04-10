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

/**
 *
 * @author rma
 */
public class Interpreter implements OutputObserver {

    private List<Tracer> tracers = new ArrayList<>();

    private Environment env = new Environment();
    private boolean paused = false;

    private java.util.Stack<Call> stack = new java.util.Stack<>();
    private java.util.List<Call> program = new ArrayList<>();

    // Bounded execution
    private boolean bounded;
    private java.util.Stack<Call> saveStack = stack;
    private java.util.List<Call> saveProgram = program;

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
            program.add(call);
        }

        return run();
    }

    public Node run(Call call) {
        tracers.forEach(t -> t.run(call));

        load(call);

        return run();
    }

    public Node run() {
        while (tick()) {
        }

        return lastResult;
    }
//
//    public void load(Call call) {
//        tracers.forEach(t -> t.load(call));
//        program.add(call);
//    }

    public void load(Node node) {
        tracers.forEach(t -> t.load(node));

        switch (node.type()) {
            case LIST:
                for (Node n : node.getChildren()) {
                    Call call = n.toProcedureCall();
                    tracers.forEach(t -> t.load(node));
                    program.add(call);
                }
                break;
            case PROCCALL:
                stack.push(node.toProcedureCall());
                break;
            case REFERENCE:
                lastResult = env.thing(node.toReferenceWord().getReference());
                break;
            default:
                lastResult = node;
                break;
        }
    }

    public Node runBounded(Node node) {
//        tracers.forEach(t -> t.run(node));
        saveStack();

        for (Node n : node.getChildren()) {
            Call call = n.toProcedureCall();
            program.add(call);
        }

        while (tick()) {
        }

        if (!paused) {
            restoreStack();
        }

        return lastResult;
    }

    private void saveStack() {
        bounded = true;
        saveStack = stack;
        saveProgram = program;

        stack = new java.util.Stack<>();
        program = new java.util.ArrayList<>();
        bounded = false;
    }

    private void restoreStack() {
        stack = saveStack;
        program = saveProgram;
        bounded = false;
    }

    public Node resume() {
        tracers.forEach(t -> t.resume(stack.peek()));
        paused = false;

        while (tick()) {
        }

        return lastResult;
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

    public Node output(Node output) {
        lastResult = output;
        return output;
    }

    public boolean tick() {
        System.out.println("Stack size: " + stack.size() + ", Program size: " + program.size() + ", Env size: " + env.size());

        /*
        Global Program state
         */
        if (paused) {
            System.out.println("LEAVING TICK WITH false IN paused");
            return false;
        }

        // If the stack is empty, check for more program lines to evaluate
        if (stack.empty()) {
            // If both program and stack are empty, execution is finished or no
            // program was loaded in the first place
            if (program.isEmpty()) {

                if (bounded) {
                    restoreStack();
                    System.out.println("LEAVING TICK WITH true IN stack.empty() -> program.isEmpty() -> bounded");
                    return true;
                }
                System.out.println("LEAVING TICK WITH false IN stack.empty() -> program.isEmpty()");
                return false;
            } else {
                System.out.println("LEAVING TICK WITH true IN stack.empty() -> !program.isEmpty()");
                schedule(program.remove(0));
                return true;
            }
        }

        /*
        Result handling
         */
        // Check for finished procedures. If stack is 1 and program empty, this
        // is the result. Else, deschedule the call and set the result to the
        // previous call. Has to be done before argument handling.
        if (stack.peek().evaluated()) {
            if(stack.peek().getName().equals("run")) {
                System.out.println("RUN FINISHED IN TICK()");
            }
            unschedule();
            if (stack.empty() && !program.isEmpty()) {
                System.out.println("LEAVING TICK WITH true IN stack.peek().evaluated() -> stack.empty() && !program.isEmpty()");
                return true;
            } else if (stack.empty() && program.isEmpty()) {
                System.out.println("LEAVING TICK WITH false IN stack.peek().evaluated() -> stack.empty() && program.isEmpty()");
                return false;
            } else if (stack.size() == 1 && stack.peek().evaluated() && program.isEmpty()) {
                System.out.println("LEAVING TICK WITH false IN stack.peek().evaluated() -> stack.size() == 1 && stack.peek().evaluated() && program.isEmpty()");
                return false;
            } else if (stack.peek().hasMoreParameters()) {
                System.out.println("LEAVING TICK WITH true IN stack.peek().evaluated() -> stack.peek().hasMoreParameters()");
                stack.peek().arg(lastResult);
                return true;
            } else {
                System.out.println("LEAVING TICK WITH true IN stack.peek().evaluated() -> else");
                return true;
            }
        }

        /*
        Arguments evaluation
         */
        // Arguments are evaluated first. If a call does not have it's argument
        // evaluated, schedule the next argument to be evaluated
        if (stack.peek().hasMoreParameters()) {
            Node nextParam = stack.peek().nextParameter();
            tracers.forEach(t -> t.arg(stack.peek().getName(), nextParam, env));

            // If it's not a procedure call, no evaluation is necessary. Add to
            // arguments as-is.
            if (!nextParam.type().equals(NodeType.PROCCALL)) {
                System.out.println("LEAVING TICK WITH true IN stack.peek().hasMoreParameters() -> !nextParam.type().equals(NodeType.PROCCALL)");
                stack.peek().arg(nextParam);
                return true;
            } else {
                System.out.println("LEAVING TICK WITH true IN stack.peek().hasMoreParameters() -> else");
                schedule(nextParam.toProcedureCall());
                return true;
            }
        }

        /*
        Procedure evaluation
         */
        Call call = stack.peek();

        // Prepare env
        for (int i = 0; i < call.definition().getArity(); i++) {
            env.local(call.definition().getArgs().get(i));
            env.make(
                    call.definition().getArgs().get(i),
                    call.args().get(i)
            );
        }

        if (call.definition().isNative()) {
            tracers.forEach(t -> t.callPrimitive(call.getName(), call.args(), env));
            Node result = call.definition().getNativeCall().apply(env.peek(), call.args());
            
            if (!nodeIsTrue(call.definition().getHasMoreCallback().apply(env.peek(), result))) {
                call.result(result, env.peek());
                call.evaluated(true);
            }
            System.out.println("LEAVING TICK WITH true IN call.definition().isNative() -> true");
            return true;
        } else {
            if (call.hasMoreCalls()) {
                schedule(call.nextCall());
            } else {
                call.evaluated(true);
                call.result(lastResult, env.peek());
            }
            System.out.println("LEAVING TICK WITH true IN call.definition().isNative() -> false");
            return true;
        }
    }

    private boolean nodeIsTrue(Node node) {
        return node.type().equals(NodeType.BOOLEAN) && node.toBooleanWord().getBoolean();
    }

    private Call unschedule() {
        Call call = stack.pop();

        if (!call.definition().isMacro()) {
            tracers.forEach(t -> t.unscope(env.peek().getScopeName(), env));
            env.pop();
        }
        lastResult = call.result();
        tracers.forEach(t -> t.unschedule(call.getName(), call, env));
        return call;
    }

    public void schedule(Call call) {
        tracers.forEach(t -> t.schedule(call.getName(), call, env));

        if (!env.defined(call.getName())) {
            throw new FunctionNotFoundException(call.getName());
        }

        call.definition(env.procedure(call.getName()));
        call.reset();
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
