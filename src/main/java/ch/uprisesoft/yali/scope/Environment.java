/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.scope;

import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.ast.node.Procedure;
import ch.uprisesoft.yali.runtime.interpreter.Tracer;
import ch.uprisesoft.yali.runtime.procedures.FunctionNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author rmaire
 */
public class Environment {
    
    private List<Tracer> tracers = new ArrayList<>();

    private List<Scope> scopes = new ArrayList<>();
    
    public void addTracer(Tracer tracer) {
        tracers.add(tracer);
    }
    
    public Scope peek() {
        return scopes.get(scopes.size() - 1);
    }

    public boolean push(Scope scope) {
        scopes.add(scope);
        System.out.println("ACTUAL: " + (scopes.size()- 1) + " -> " + scopes.get(scopes.size()- 1).getScopeName());
        for(int i = scopes.size() -2; i >= 0; i--) {
                Scope currentScope = scopes.get(i);
                if(scope.getScopeName().equals(currentScope.getScopeName())) {
//                    for(int j = scopes.size()-1; j>=i; j--) {
//                        scopes.remove(j);
//                        scopes.add(currentScope);
//                    }
                    System.out.println("RECURSION: " + i + " -> " + scopes.get(i).getScopeName());
//                    return true;
                } else {
                     System.out.println("NOT RECURSION: " +  i + " -> " + scopes.get(i).getScopeName());
                }
            }
        
//        scopes.add(scope);
        return false;
    }

    public Scope pop() {
        return scopes.remove(scopes.size() - 1);
    }
    
    private Scope first() {
        return scopes.get(0);
    }
    
    /**
     * Variables
     */

    public void make(String name, Node value) {

        tracers.forEach(t -> t.make(name, value, this));
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).defined(name.toLowerCase())) {
                scopes.get(i).define(name.toLowerCase(), value);
                return;
            }
        }
        
        scopes.get(0).define(name.toLowerCase(), value);
    }

    public void local(String name) {
        tracers.forEach(t -> t.local(name, this));
        peek().local(name.toLowerCase());
    }

    public Node thing(String name) {
        
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).defined(name.toLowerCase())) {
                final Node ret = scopes.get(i).resolve(name.toLowerCase());
                tracers.forEach(t -> t.thing(name, ret, this));
                return ret;
            }
        }

        tracers.forEach(t -> t.thing(name, Node.none(), this));
        return Node.none();
    }

    public Boolean thingable(String name) {

        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).defined(name)) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * Procedures
     */
    
    public void define(Procedure function) {
        first().define(function.getName(), function);
    }

    public Boolean defined(String name) {
        return first().defined(name);
    }
    
    public Procedure procedure(String name) {
        return first().resolve(name).toProcedureDef();
    }

    public void alias(String original, String alias) {
        if (!(first().defined(original))) {
            throw new FunctionNotFoundException(original);
        }

        first().define(alias, first().resolve(original));
    }
    
    public String trace() {
        StringBuilder sb = new StringBuilder();
        
        for(Scope s: scopes) {
            sb.append(s.getScopeName()).append("\n");
        }
        return sb.toString();
    }

    public Integer size() {
        return scopes.size();
    }
}
