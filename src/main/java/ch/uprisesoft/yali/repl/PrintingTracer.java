/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.repl;

import ch.uprisesoft.yali.ast.node.Call;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.runtime.interpreter.Tracer;
import ch.uprisesoft.yali.scope.Environment;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author rma
 */
public class PrintingTracer implements Tracer {
    
    private final Interpreter it;

    public PrintingTracer(Interpreter it) {
        this.it = it;
    }

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
        System.out.println(lpad() + "Calling primitive Procedure " + name + ": " + args.toString());
//        System.out.println("Trace:");
//        System.out.println(env.trace());
    }

    @Override
    public void call(String name, List<Node> args, Environment env) {
        System.out.println(lpad() + "Calling Procedure " + name + ": " + args.toString());
//        System.out.println("Trace:");
//        System.out.println(env.trace());
    }

    @Override
    public void make(String name, Node val, Environment env) {
        System.out.println(lpad() + "Defining Variable " + name + " in scope " + env.peek().getScopeName() + " with value " + val.toString());
    }

    @Override
    public void thing(String name, Node val, Environment env) {
        System.out.println(lpad() + "Resolving Variable " + name + " in scope " + env.peek().getScopeName() + " with value " + val.toString());
    }

    @Override
    public void local(String name, Environment env) {
        System.out.println(lpad() + "Local Variable " + name + " in scope " + env.peek().getScopeName());
    }

    @Override
    public void run(Node val) {
        System.out.println(lpad() + "Run: " + val.toString());
    }

    @Override
    public void tick(Node val) {
        System.out.println(lpad() + "tick: " + val.toString());
    }

    @Override
    public void apply(Node val) {
//        System.out.println("apply: " + val.toString());
    }

    @Override
    public void pause(Node val) {
        System.out.println(lpad() + "Paused, current call: " + val.toString());
    }

    @Override
    public void resume(Node val) {
        if (val != null) {
            System.out.println(lpad() + "Resumed, current call: " + val.toString());
        } else {
            System.out.println(lpad() + "Resumed");
        }
    }

    @Override
    public void scope(String name, Environment env) {
        System.out.println(lpad() + "Open environment " + name);
    }

    @Override
    public void unscope(String name, Environment env) {
        System.out.println(lpad() + "Close environment " + name);
    }

    @Override
    public void arg(String name, Node val, Environment env) {
        System.out.println(lpad() + "Evaluating argument for Call " + name + " -> " + val);
    }

    @Override
    public void schedule(String name, Call call, Environment env) {
        System.out.println(lpad() + "Scheduling " + name + ": " + call);
    }

    @Override
    public void unschedule(String name, Call call, Environment env) {
        System.out.println(lpad() + "Unscheduling " + name + ": " + call);
    }

    @Override
    public void load(Node val) {
        System.out.println(lpad() + "Loading " + val);
    }
    
    private String lpad(){
        String lpad = String.join("", Collections.nCopies(it.env().size(), ">")) + " ";
        return lpad;        
    }

    @Override
    public void returnTick(Node val, String pos) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
