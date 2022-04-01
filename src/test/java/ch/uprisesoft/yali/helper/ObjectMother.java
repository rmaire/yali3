/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.helper;

import ch.uprisesoft.yali.lexer.Lexer;
import ch.uprisesoft.yali.parser.Parser;
import ch.uprisesoft.yali.runtime.procedures.builtin.MockTurtleManager;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.runtime.io.InputGenerator;
import ch.uprisesoft.yali.runtime.io.OutputObserver;

/**
 *
 * @author rmaire
 */
public class ObjectMother {
    
    private Interpreter i;
    private Parser p;
    private Lexer l;
    
    public ObjectMother(OutputObserver o) {
        
        this.i = new Interpreter();
        this.p = new Parser(i);
        this.l = new Lexer();
        this.i.loadStdLib(o);
        
        MockTurtleManager mtm = new MockTurtleManager();
        mtm.registerProcedures(i);
    }
    
    public ObjectMother(OutputObserver oo, InputGenerator ig) {
        this.i = new Interpreter();
        this.i.loadStdLib(oo, ig);
        
        MockTurtleManager mtm = new MockTurtleManager();
        mtm.registerProcedures(i);
        
        this.p = new Parser(i);
        this.l = new Lexer();
    }
    
    public ObjectMother() {
        
        OutputObserver oo = new OutputObserver() {
            
            @Override
            public void inform(String output) {
                System.out.println(output);
            }
        };
        
        InputGenerator ig = new InputGenerator() {
            @Override
            public String request() {
                return "requestedinput";
            }

            @Override
            public String requestLine() {
                return "requestedinputline";
            }
        };
        
        this.i = new Interpreter();
        this.i.loadStdLib(oo, ig);
        
        MockTurtleManager mtm = new MockTurtleManager();
        mtm.registerProcedures(i);
        
        this.p = new Parser(i);
        this.l = new Lexer();
    }


    public Interpreter getInterpreter() {
        return i;
    }

    public Parser getParser() {
        return p;
    }

    public Lexer getLexer() {
        return l;
    }
}
