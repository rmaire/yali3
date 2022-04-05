/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.repl;

import ch.uprisesoft.yali.ast.node.Call;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.runtime.interpreter.Tracer;
import ch.uprisesoft.yali.scope.Environment;
import java.util.List;

/**
 *
 * @author rma
 */
public class PrintingTracer implements Tracer {

    @Override
    public void parse(String source) {
//        System.out.println("Parsing. Source: " + source);
//        System.out.println(source);
    }

    @Override
    public void start(Node node) {
//        System.out.println("Start interpreting.");
    }

    @Override
    public void callPrimitive(String name, List<Node> args, Environment env) {
        System.out.println("Calling primitive Procedure " + name + ": " + args.toString());
//        System.out.println("Trace:");
//        System.out.println(env.trace());
    }

    @Override
    public void call(String name, List<Node> args, Environment env) {
        System.out.println("Calling Procedure " + name + ": " + args.toString());
//        System.out.println("Trace:");
//        System.out.println(env.trace());
    }

    @Override
    public void make(String name, Node val, Environment env) {
        System.out.println("Defining Variable " + name + " in scope " + env.peek().getScopeName() + " with value " + val.toString());
    }

    @Override
    public void thing(String name, Node val, Environment env) {
        System.out.println("Resolving Variable " + name + " in scope " + env.peek().getScopeName() + " with value " + val.toString());
    }

    @Override
    public void local(String name, Environment env) {
        System.out.println("Local Variable " + name + " in scope " + env.peek().getScopeName());
    }

    @Override
    public void run(Node val) {
        System.out.println("Run: " + val.toString());
    }

    @Override
    public void tick(Node val) {
        System.out.println("tick: " + val.toString());
    }

    @Override
    public void apply(Node val) {
//        System.out.println("apply: " + val.toString());
    }

    @Override
    public void pause(Node val) {
        System.out.println("Paused, current call: " + val.toString());
    }

    @Override
    public void resume(Node val) {
        if (val != null) {
            System.out.println("Resumed, current call: " + val.toString());
        } else {
            System.out.println("Resumed");
        }
    }

    @Override
    public void scope(String name, Environment env) {
        System.out.println("Open environment " + name);
    }

    @Override
    public void unscope(String name, Environment env) {
        System.out.println("Close environment " + name);
    }

    @Override
    public void arg(String name, Node val, Environment env) {
        System.out.println("Evaluating argument for Call " + name + " -> " + val);
    }

    @Override
    public void schedule(String name, Call call, Environment env) {
        System.out.println("Scheduling " + name + ": " + call);
    }

    @Override
    public void unschedule(String name, Call call, Environment env) {
        System.out.println("Unscheduling " + name + ": " + call);
    }

}
