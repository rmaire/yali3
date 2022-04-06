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
    private java.util.List<Call> program = new ArrayList<>();

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

        while (tick()) {
        }

        return lastResult;
    }

    public Node run(Call call) {
        tracers.forEach(t -> t.run(call));

        program.add(call);

        while (tick()) {
        }

        return lastResult;
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
        System.out.println("Stack size: " + stack.size() + ", Program size: " + program.size());

        // Global Program state
        if (paused) {
            return false;
        }

        // If the stack is empty, check for more program lines to evaluate
        if (stack.empty()) {
            // If both program and stack are empty, execution is finished or no
            // program was loaded in the first place
            if (program.isEmpty()) {
                return false;
            } else {
                schedule(program.remove(0));
                return true;
            }
        }

        // Result handling
        // Check for finished procedures. If stack is 1 and program empty, this
        // is the result. Else, deschedule the call and set the result to the
        // previous call. Has to be done before argument handling.
        if (stack.peek().evaluated()) {
            System.out.println(stack.peek().getName() + " IS EVALUATED: " + stack.peek().result());
            Call evaluatedCall = unschedule();
            Node res = evaluatedCall.result();
            if(stack.empty() && !program.isEmpty()) {
                System.out.println("Case stack.empty() && !program.isEmpty() -> true");
                lastResult = res;
                return true;
            } else if (stack.empty() && program.isEmpty()){
                System.out.println("Case stack.empty() && program.isEmpty() -> false");
                lastResult = res;
                return false;
            }  else if (stack.size() == 1 && stack.peek().evaluated() && program.isEmpty()) {
                System.out.println("stack.size() == 1 && stack.peek().evaluated() && program.isEmpty() -> false");
                lastResult = res;
                return false;
            } else if (stack.peek().hasMoreParameters()){
                System.out.println("Case stack.peek().hasMoreParameters() -> true");
                System.out.println(stack.peek() + " HAS MORE PARAMETERS");
                stack.peek().arg(res);
                return true;
            } else {
                System.out.println("Case else -> continue");
                stack.peek().result(res);
            }
        }

        // Arguments evaluation
        // Arguments are evaluated first. If a call does not have it's argument
        // evaluated, schedule the next argument to be evaluated
        if (stack.peek().hasMoreParameters()) {
            Node nextParam = stack.peek().nextParameter();
            tracers.forEach(t -> t.arg(stack.peek().getName(), nextParam, env));

            // If it's not a procedure call, no evaluation is necessary. Add to
            // arguments as-is.
            if (!nextParam.type().equals(NodeType.PROCCALL)) {
                stack.peek().arg(nextParam);
                return true;
            } else {
                schedule(nextParam.toProcedureCall());
                return true;
            }
        }

        // Procedure evaluation
        // Non-ready procedures on top of the stack shouldn't reach this part
        if (stack.peek().hasMoreParameters()) {
            System.out.println("Non-ready procedure too far in tick()");
        }

        System.out.println("HIER!!!");

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
            call.result(result);
            call.evaluated(true);
            return true;
        } else {
            if (call.hasMoreCalls()) {
                schedule(call.nextCall());
            } else {
                call.evaluated(true);
                call.result(lastResult);
            }
            return true;
        }

        // Evaluate the next call on the stack. Native procedures are evaluated
        // with their lambda, 
        // Check if there's more to do
//        System.out.println("Should we get here?");
//        if(!program.isEmpty()) {
//            return true;
//        }
//        
//        if (stack.size() == 1 && !stack.peek().evaluated()) {
//            return true;
//        }
//        if (stack.size() > 1) {
//            return true;
//        }
//        System.out.println("DEFAULT EXIT: NOT GOOD :(");
//        return false;
    }

//    public boolean tick() {
//        System.out.println("Stack size: " + stack.size() + ", Program size: " + program.size());
//
//        if (paused) {
//            return false;
//        }
//        
//        // Nothing happened, no program loaded.
//        if (stack.empty() && program.isEmpty()) {
//            return false;
//        }
//        
//        if(!program.isEmpty() && stack.size() == 1 && stack.peek().evaluated()) {
//            System.out.println("Not finished");
//            schedule(program.remove(0));
//            lastResult = stack.pop().result();
//            return true;
//        }
//
//        // If only one evaluated procedure is on the stack, the program is finished
//        // and stack.peek.result() gives the final result
//        if (program.isEmpty() && stack.size() == 1 && stack.peek().evaluated()) {
//            System.out.println("Finished: " + stack.peek() + " -> " + stack.peek().result());
//            return false;
//        }
//        
//        if(!program.isEmpty() && stack.size() == 0) {
//            schedule(program.remove(0));
//            return true;
//        }
//
//        tracers.forEach(t -> t.apply(stack.peek()));
//
//        if (stack.peek().evaluated()) {
//            lastResult = stack.peek().result();
//        }
//
//        // If more than one call is on the stack, and the current call is evaluated,
//        // this must be an arg or a result to the previous call. Pop the current call, get it's
//        // result and add it to the args or the result  of the precious call
//        if (stack.size() > 1 && stack.peek().evaluated()) {
//            if (!stack.peek().definition().isMacro()) {
//                tracers.forEach(t -> t.unscope(env.peek().getScopeName(), env));
//                env.pop();
//            }
//
//            Node lastResult = stack.pop().result();
//
//            // If previous call is ready but has no result, set the last
//            // result. If not, add it to the args
////            System.out.println("HERE: " + stack.peek().getName() + " -> " + stack.peek().ready() + " -> " + stack.peek().evaluated());
//            if (stack.peek().ready() && !stack.peek().evaluated()) {
//                stack.peek().result(lastResult);
//                return true;
//            } else {
//                stack.peek().arg(lastResult);
//            }
//            return true;
//        }
//
//        Call call = stack.peek();
//
//        if (!call.ready()) {
//            Node param = call.nextParameter();
//
//            tracers.forEach(t -> t.arg(call.getName(), param, env));
//
//            if (!param.type().equals(NodeType.PROCCALL)) {
//                call.arg(param);
//            } else {
//                schedule(param.toProcedureCall());
//            }
//            return true;
//        } else {
//            //define all args in the local scope
//            for (int i = 0; i < call.definition().getArity(); i++) {
//                env.local(call.definition().getArgs().get(i));
//                env.make(
//                        call.definition().getArgs().get(i),
//                        call.args().get(i)
//                );
//            }
//        }
//
//        if (call.ready() && (call.definition().isNative() || call.definition().isMacro())) {
//            tracers.forEach(t -> t.callPrimitive(call.getName(), call.args(), env));
//            Node result = call.definition().getNativeCall().apply(env.peek(), call.args());
////            stack.pop();
//            call.result(result);
//            call.evaluated(true);
//        } else if (call.ready() && !call.definition().isNative() && call.hasMoreCalls()) {
//            tracers.forEach(t -> t.call(call.getName(), call.args(), env));
//            schedule(call.nextCall());
//        }
//
//        // Check if there's more to do
//        if(!program.isEmpty()) {
//            return true;
//        }
//        
//        if (stack.size() == 1 && !stack.peek().evaluated()) {
//            return true;
//        }
//        if (stack.size() > 1) {
//            return true;
//        }
//
//        System.out.println("WHYYYY?");
//        return false;
//    }
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

    private void schedule(Call call) {
        tracers.forEach(t -> t.schedule(call.getName(), call, env));

        if (!env.defined(call.getName())) {
            throw new FunctionNotFoundException(call.getName());
        }

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
