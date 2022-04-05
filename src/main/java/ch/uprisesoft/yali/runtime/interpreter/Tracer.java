/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.runtime.interpreter;

import ch.uprisesoft.yali.ast.node.Call;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.scope.Environment;
import java.util.List;

/**
 *
 * @author rma
 */
public interface Tracer {
    public void parse(String source);
    public void start(Node node);
    public void callPrimitive(String name, List<Node> args, Environment env);
    public void call(String name, List<Node> args, Environment env);
    public void schedule(String name, Call call, Environment env);
    public void unschedule(String name, Call call, Environment env);
    public void arg(String name, Node val, Environment env);
    public void make(String name, Node val, Environment env);
    public void thing(String name, Node val, Environment env);
    public void local(String name, Environment env);
    public void scope(String name, Environment env);
    public void unscope(String name, Environment env);
    public void run(Node val);
    public void tick(Node val);
    public void apply(Node val);
    public void pause(Node val);
    public void resume(Node val);
}
