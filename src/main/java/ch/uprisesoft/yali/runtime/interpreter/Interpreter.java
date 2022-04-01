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
import ch.uprisesoft.yali.exception.RecursionException;
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
//    private CallStack stack = new CallStack();
    private boolean paused = false;

    private java.util.Stack<Call> stack = new java.util.Stack<>();

    public Interpreter() {
        env.push(new Scope("global"));
    }

    public void addTracer(Tracer tracer) {
        tracers.add(tracer);
    }

    public List<Tracer> tracers() {
        return tracers;
    }

//    public CallStack stack() {
//        return stack;
//    }
    /**
     * Interpreting functionality, public interface
     */
    public Node run(Node node) {
        tracers.forEach(t -> t.run(node));

        if(!node.type().equals(NodeType.PROCCALL)) {
            return node;
        }
        
        Call call = node.toProcedureCall();
        
        if(!env.defined(call.getName())) {
            throw new FunctionNotFoundException(call.getName());
        }
        
        call.definition(env.procedure(call.getName()));
        
        stack.push(call);

//        stack.schedule(node);
        while (!stack.empty()) {
            if (paused) {
                break;
            }

            tick();
        }

        return stack.pop().result();
    }

//    public Node runBounded(Node node) {
//        tracers.forEach(t -> t.run(node));
//
//        CallStack save = stack;
//        stack = new CallStack();
//
//        Node result = run(node);
//
//        stack = save;
//        return result;
//    }
    public Node resume() {
//        tracers.forEach(t -> t.resume(stack.currentCall()));
//        callDepth = 0;
        paused = false;

        while (!stack.empty()) {
            if (paused) {
                break;
            }

            tick();
        }

        return stack.pop().result();
    }

    public void pause() {
//        tracers.forEach(t -> t.pause(stack.currentCall()));
        paused = true;
    }

    public boolean paused() {
        return paused;
    }

    public Node read(String source) {
        tracers.forEach(t -> t.parse(source));
        Node node = new Parser(this).read(source);
        return node;
    }

    public Node read(ch.uprisesoft.yali.ast.node.List list) {
//        tracers.forEach(t -> t.parse(source));

        Node node = new Parser(this).read(list);
        return node;
    }

//    public Node read(ch.uprisesoft.yali.ast.node.List list) {
////        tracers.forEach(t -> t.parse(source));
//        return new Parser(this).read(list);
//    }
    public Node result() {
        return stack.pop();
    }

    public Environment env() {
        return env;
    }

    /**
     * This method evaluates an actual Procedure Call, with all arguments.
     * Checks if the procedure is native. If yes, it calls the lambda defined in
     * the Procedure definition. If no, it calls apply again, as it needs
     * further evaluation (every Procedure needs to be reduced to native calls
     * in the end).
     */
    public boolean tick() {

        if (paused) {
            return false;
        }

        if (stack.empty()) {
            return false;
        }

        Node actual = stack.pop();

        if (!actual.type().equals(NodeType.PROCCALL)) {
            throw new NodeTypeException(actual, actual.type(), NodeType.PROCCALL);
        }
        
        Call call = actual.toProcedureCall();
        
        if(call.prepped()) {
            
        }
        
        if(call.definition().isNative()) {
            Node result = call.definition().getNativeCall().apply(env.peek(), call.args());
            call.result(result);
        } 
        
        return false;
    }

    public boolean apply(Node node) {
        boolean recursedCall = false;
        boolean recursedScope = false;

        tracers.forEach(t -> t.apply(node));

        if (paused) {
            return false;
        }

        // First base case: If a Node is not a Procedure call, evaluation is finished
        if (!node.type().equals(NodeType.PROCCALL)) {
            stack.output(node);
            return false;
        }

        Call call = node.toProcedureCall();
//        stack.currentCall(call);

        if (!env.defined(call.getName())) {
            throw new FunctionNotFoundException(call.getName());
        }

        call.definition(env.procedure(call.getName()));

        if (!call.definition().isMacro()) {
            recursedScope = env.push(new Scope(call.definition().getName()));
        }

        // Evaluate arguments first
        java.util.List<Node> args = new ArrayList<>();

        int i = 0;
        for (Node c : call.getChildren()) {

            // Recursive case: Arguments are evaluated before evaluating the function itself
//            apply(c);
            try {
                apply(c);
            } catch (RecursionException re) {
                resume();
            }

//            Node result = stack.output();
            if (call.args().size() < call.definition().getArity()) {
                env.local(call.definition().getArgs().get(i));
                env.make(
                        call.definition().getArgs().get(i),
                        stack.output()
                );
                i++;
            }
            call.arg(stack.output());
//            args.add(stack.output());
        }
//        call.args(args);

        // Evaluate Call with concrete arguments
//        stack.currentCall(call);
        // TODO differentiate from macros
        if (call.definition().isNative() || call.definition().isMacro()) {
            tracers.forEach(t -> t.callPrimitive(call.definition().getName(), call.args(), env));

            // Second base case: a primitive Procedure should never recursively call another Procedure
            stack.output(call.definition().getNativeCall().apply(env.peek(), call.args()));
        } else {

            tracers.forEach(t -> t.call(call.definition().getName(), call.args(), env));
            recursedCall = stack.schedule(call.definition());

            if (recursedScope && recursedCall) {
                throw new RecursionException(call.getName(), call);
            } else if (recursedCall) {
                System.out.println("RECURSED CALL ONLY");
            } else if (recursedScope) {
                System.out.println("RECURSED SCOPE ONLY");
            }

            while (stack.hasNext()) {
                Node line = stack.next();

                // every direct child should be a function call
                if (!line.type().equals(NodeType.PROCCALL)) {
                    throw new NodeTypeException(line, line.type(), NodeType.PROCCALL);
                }

                // Check if function call is output or stop. If yes, no further
                // lines will be evaluated
                if (line.toProcedureCall().getName().equals("output") || line.toProcedureCall().getName().equals("stop")) {
                    break;
                }

                // Recursive case: The children of a Procedure itself are Procedures too.
                if (!paused) {
//                    apply(line);

                    try {
                        apply(line);
                    } catch (RecursionException re) {
                        resume();
                    }
                } else {
                    return false;
                }
            }
        }

        stack.completeCurrentCall();

        if (!call.definition().isMacro()) {
            env.pop();
        }

        return true;
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

//public static void main(String[] args) {
//    LOGGER.info("Start calculating war casualties");
//    var result = loop(10, 1).result();
//    LOGGER.info("The number of orcs perished in the war: {}", result);
//}
//
//public static Trampoline<Integer> loop(int times, int prod) {
//    if (times == 0) {
//        return Trampoline.done(prod);
//    } else {
//        return Trampoline.more(() -> loop(times - 1, prod * times));
//    }
//}
interface Trampoline<T> {

    T get();

    default Trampoline<T> jump() {
        return this;
    }

    default T result() {
        return get();
    }

    default boolean complete() {
        return true;
    }

    static <T> Trampoline<T> done(final T result) {
        return () -> result;
    }

    static <T> Trampoline<T> more(final Trampoline<Trampoline<T>> trampoline) {
        return new Trampoline<T>() {
            @Override
            public boolean complete() {
                return false;
            }

            @Override
            public Trampoline<T> jump() {
                return trampoline.result();
            }

            @Override
            public T get() {
                return trampoline(this);
            }

            T trampoline(final Trampoline<T> trampoline) {
                return Stream.iterate(trampoline, Trampoline::jump)
                        .filter(Trampoline::complete)
                        .findFirst()
                        .map(Trampoline::result)
                        .orElseThrow(null);
            }
        };
    }
}
