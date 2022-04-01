/*
 * Copyright 2021 rmaire.
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
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.helper.ObjectMother;
import ch.uprisesoft.yali.lexer.Lexer;
import ch.uprisesoft.yali.parser.Parser;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.runtime.io.OutputObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author rmaire
 */
public class LogicTest {
    
    private Lexer l;
    private Parser p;
    private Interpreter it;
    private OutputObserver o;
    
    public LogicTest() {
    }
    
    @BeforeEach
    public void setUp() {
        o = new OutputObserver() {
            @Override
            public void inform(String output) {
            }
        };
        
        ObjectMother om = new ObjectMother(o);
        
        l = om.getLexer();
        p = om.getParser();
        it = om.getInterpreter();
    }

    @Test
    public void testEqual() {
        o.inform("Start testEqual()");
        String input = "2 = 2";
        Node res = it.run(it.read(input));
                
        assertThat(res.type(), is(NodeType.BOOLEAN));
        assertThat(res.toBooleanWord().getBoolean(), is(true));
                
        o.inform("End testEqual()");
    }
    
    @Test
    public void testNotEqual() {
        o.inform("Start testNotEqual()");
        String input = "2 = 3";
        Node res = it.run(it.read(input));
                
        assertThat(res.type(), is(NodeType.BOOLEAN));
        assertThat(res.toBooleanWord().getBoolean(), is(false));
                
        o.inform("End testNotEqual()");
    }
    
    @Test
    public void testDoubleEqual() {
        o.inform("Start testDoubleEqual()");
        String input = "2 == 2";
        Node res = it.run(it.read(input));
                
        assertThat(res.type(), is(NodeType.BOOLEAN));
        assertThat(res.toBooleanWord().getBoolean(), is(true));
                
        o.inform("End testDoubleEqual()");
    }
    
    @Test
    public void testNotDoubleEqual() {
        o.inform("Start testNotDoubleEqual()");
        String input = "2 == 3";
        Node res = it.run(it.read(input));
                
        assertThat(res.type(), is(NodeType.BOOLEAN));
        assertThat(res.toBooleanWord().getBoolean(), is(false));
                
        o.inform("End testNotDoubleEqual()");
    }

    @Test
    public void testInequal() {
        o.inform("Start testInequal()");
        String input = "2 != 3";
        Node res = it.run(it.read(input));
                
        assertThat(res.type(), is(NodeType.BOOLEAN));
        assertThat(res.toBooleanWord().getBoolean(), is(true));
                
        o.inform("End testInequal()");
    }
    
    @Test
    public void testNotInequal() {
        o.inform("Start testNotInequal()");
        String input = "2 != 2";
        Node res = it.run(it.read(input));
                
        assertThat(res.type(), is(NodeType.BOOLEAN));
        assertThat(res.toBooleanWord().getBoolean(), is(false));
                
        o.inform("End testNotInequal()");
    }

    @Test
    public void testGreater() {
        o.inform("Start testGreater()");
        String input = "2 > 1";
        Node res = it.run(it.read(input));
                
        assertThat(res.type(), is(NodeType.BOOLEAN));
        assertThat(res.toBooleanWord().getBoolean(), is(true));
                
        o.inform("End testGreater()");
    }
    
    @Test
    public void testNotGreater() {
        o.inform("Start testNotGreater()");
        String input = "1 > 2";
        Node res = it.run(it.read(input));
                
        assertThat(res.type(), is(NodeType.BOOLEAN));
        assertThat(res.toBooleanWord().getBoolean(), is(false));
        
        o.inform("End testNotGreater()");
    }

    @Test
    public void testLess() {
        o.inform("Start testLess()");
        String input = "1 < 2";
        Node res = it.run(it.read(input));
                
        assertThat(res.type(), is(NodeType.BOOLEAN));
        assertThat(res.toBooleanWord().getBoolean(), is(true));
        
        o.inform("End testLess()");
    }
    
    @Test
    public void testNotLess() {
        o.inform("Start testNotLess()");
        String input = "2 < 1";
        Node res = it.run(it.read(input));
                
        assertThat(res.type(), is(NodeType.BOOLEAN));
        assertThat(res.toBooleanWord().getBoolean(), is(false));
        
        o.inform("End testNotLess()");
    }

    @Test
    public void testGreaterorequal() {
        o.inform("Start testGreaterorequal()");
        String input = "2 >= 1";
        Node res = it.run(it.read(input));
                
        assertThat(res.type(), is(NodeType.BOOLEAN));
        assertThat(res.toBooleanWord().getBoolean(), is(true));
        
        o.inform("End testGreaterorequal()");
    }
    
    @Test
    public void testNotGreaterorequal() {
        o.inform("Start testNotGreaterorequal()");
        String input = "2 >= 3";
        Node res = it.run(it.read(input));
                
        assertThat(res.type(), is(NodeType.BOOLEAN));
        assertThat(res.toBooleanWord().getBoolean(), is(false));
        
        o.inform("End testNotGreaterorequal()");
    }
    
    @Test
    public void testGreaterorequalSameVal() {
        o.inform("Start testGreaterorequal()");
        String input = "2 >= 2";
        Node res = it.run(it.read(input));
                
        assertThat(res.type(), is(NodeType.BOOLEAN));
        assertThat(res.toBooleanWord().getBoolean(), is(true));
        
        o.inform("End testGreaterorequal()");
    }

    @Test
    public void testLessorequal() {
        o.inform("Start testLessorequal()");
        String input = "1 <= 2";
        Node res = it.run(it.read(input));
                
        assertThat(res.type(), is(NodeType.BOOLEAN));
        assertThat(res.toBooleanWord().getBoolean(), is(true));
        
        o.inform("End testLessorequal()");
    }
    
    @Test
    public void testNotLessorequal() {
        o.inform("Start testNotLessorequal()");
        String input = "3 <= 2";
        Node res = it.run(it.read(input));
                
        assertThat(res.type(), is(NodeType.BOOLEAN));
        assertThat(res.toBooleanWord().getBoolean(), is(false));
        
        o.inform("End testNotLessorequal()");
    }
    
    @Test
    public void testLessorequalSameVal() {
        o.inform("Start testLessorequalSameVal()");
        String input = "2 <= 2";
        Node res = it.run(it.read(input));
                
        assertThat(res.type(), is(NodeType.BOOLEAN));
        assertThat(res.toBooleanWord().getBoolean(), is(true));
        
        o.inform("End testLessorequalSameVal()");
    }
    
}
