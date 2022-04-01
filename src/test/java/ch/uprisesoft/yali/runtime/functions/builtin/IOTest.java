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
package ch.uprisesoft.yali.runtime.functions.builtin;

import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.lexer.Lexer;
import ch.uprisesoft.yali.helper.ObjectMother;
import ch.uprisesoft.yali.parser.Parser;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.runtime.io.InputGenerator;
import ch.uprisesoft.yali.runtime.io.OutputObserver;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.BeforeEach;

/**
 *
 * @author rma
 */
public class IOTest {
        
    private Lexer l;
    private Parser p;
    private Interpreter it;
    private OutputObserver oo;
    private InputGenerator ig;
    private java.util.List<String> outputs;

    public IOTest() {
    }
    
    @BeforeEach
    public void setUp() {
        oo = new OutputObserver() {
            
            @Override
            public void inform(String output) {
                outputs.add(output);
            }
        };
        
        ig = new InputGenerator() {
            
            @Override
            public String request() {
                return "requestedinput";
            }

            @Override
            public String requestLine() {
                return "requestedinputline";
            }
        };
        
        ObjectMother om = new ObjectMother(oo, ig);
        
        l = om.getLexer();
        p = om.getParser();
        it = om.getInterpreter();
        
        outputs = new ArrayList<>();
    }
    
    @Test
    public void testPrint() {
        it.run(it.read("print [Hello World!]"));
        
        assertThat(outputs.size(), is(1));
        assertThat(outputs.get(0), is("Hello World!\n"));
    }
    
    @Test
    public void testPrintVarargs() {
        it.run(it.read("(print \"Hello \"Varargs!)"));
        
        assertThat(outputs.size(), is(1));
        assertThat(outputs.get(0), is("Hello Varargs!\n"));
    }
    
    @Test
    public void testReadlist() {
        Node res = it.run(it.read("make \"testvar readlist\nprint :testvar"));
        
        assertThat(outputs.size(), is(1));
        assertThat(outputs.get(0), is("requestedinputline\n"));

    }
}
