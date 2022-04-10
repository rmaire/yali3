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

import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.helper.ObjectMother;
import ch.uprisesoft.yali.runtime.io.InputGenerator;
import ch.uprisesoft.yali.runtime.io.OutputObserver;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

/**
 *
 * @author rma
 */

//@Disabled
public class InterpreterTest {

    private java.util.List<String> outputs;

    private Interpreter it;

    private OutputObserver oo;
    private InputGenerator ig;

    public InterpreterTest() {
    }

    @BeforeEach
    public void setUp() {

        outputs = new ArrayList<>();

        oo = new OutputObserver() {

            @Override
            public void inform(String output) {
//                System.out.println(output);
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
        it = om.getInterpreter();
    }

//    @Test
//    public void testInterpreter() {
//        Node result = it.eval("print [Hello World!?!]");
//
//        assertThat(result.type(), is(NodeType.NIL));
//        assertThat(outputs.size(), is(1));
//        assertThat(outputs.get(0), is("Hello World!?!\n"));
//    }
//
//    @Test
//    public void testFunDefAndCallSameString() {
//        String input = "to testfun :in\n"
//                + "print :in\n"
//                + "end\n"
//                + "\n"
//                + "testfun \"testit!!!";
//        it.eval(input);
//
//        assertThat(outputs.size(), is(1));
//        assertThat(outputs.get(0), is("testit!!!\n"));
//    }
//
//    @Test
//    public void testFunDefAndCall() {
//        String input = "to testfun :in\n"
//                + "print :in\n"
//                + "end\n";
//        it.eval(input);
//
//        input = "testfun \"testit!!!";
//        it.eval(input);
//
//        assertThat(outputs.size(), is(1));
//        assertThat(outputs.get(0), is("testit!!!\n"));
//    }
//
//    @Test
//    public void testNestedFunDefAndCall() {
//        String input = "to testfun :in\n"
//                + "testfun2\n"
//                + "end\n"
//                + "\n"
//                + "to testfun2\n"
//                + "print \"testit!!!\n"
//                + "end\n"
//                + "\n"
//                + "testfun";
//        it.eval(input);
//
//        assertThat(outputs.get(0), is("testit!!!\n"));
//    }
//
//    @Test
//    public void testUndefinedFunction() {
//        String input = "blabla";
//
//        NodeTypeException nte = assertThrows(NodeTypeException.class, () -> it.eval(input));
//
//        assertThat(nte.getExpected().get(0), is(NodeType.FUNCALL));
//        assertThat(nte.getReceived(), is(NodeType.SYMBOL));
//        assertThat(nte.getNode().getToken().getLexeme(), is("blabla"));
//        assertThat(nte.getNode().getLine(), is(1));
//        assertThat(nte.getNode().getCol(), is(0));
//
//    }
//
//    @Test
//    public void testUndefinedFunctionInFunction() {
//        String input = "to testfun" + "\n"
//                + "blabla" + "\n"
//                + "end" + "\n"
//                + "" + "\n"
//                + "testfun" + "\n";
//
//        NodeTypeException nte = assertThrows(NodeTypeException.class, () -> it.eval(input));
//
//        assertThat(nte.getExpected().get(0), is(NodeType.FUNCALL));
//        assertThat(nte.getReceived(), is(NodeType.SYMBOL));
//        assertThat(nte.getNode().getToken().getLexeme(), is("blabla"));
//        assertThat(nte.getNode().getLine(), is(2));
//        assertThat(nte.getNode().getCol(), is(0));
//    }
//
//    @Test
//    public void testUndefinedFunctionInFunctionAsArgument() {
//        String input = "to testfun" + "\n"
//                + "print blabla" + "\n"
//                + "end" + "\n"
//                + "" + "\n"
//                + "testfun" + "\n";
//
//        NodeTypeException nte = assertThrows(NodeTypeException.class, () -> it.eval(input));
//
//        assertTrue(nte.getExpected().contains(NodeType.FUNCALL));
//        assertThat(nte.getReceived(), is(NodeType.SYMBOL));
//        assertThat(nte.getNode().getToken().getLexeme(), is("blabla"));
//        assertThat(nte.getNode().getLine(), is(2));
//        assertThat(nte.getNode().getCol(), is(6));
//    }
//
//    @Test
//    public void testDynamicScope() {
//        String input = "make \"testvar \"one\n"
//                + "to testfun1 :in\n"
//                + "make \"testvar \"two\n"
//                + "testfun2\n"
//                + "end\n"
//                + "\n"
//                + "to testfun2\n"
//                + "print :testvar\n"
//                + "end\n"
//                + "\n"
//                + "testfun1\n";
//
//        it.eval(input);
//
//        assertThat(outputs.size(), is(1));
//        assertThat(outputs.get(0), is("two\n"));
//    }
//
//    @Test
//    public void testNestedList() {
//        String testInput = "output [:one [\"two ?3]]";
//
//        Node res = it.eval(testInput);
//
//        assertThat(res.type(), is(NodeType.LIST));
//        assertThat(res.getChildren().get(0).type(), is(NodeType.SYMBOL));
//        assertThat(res.getChildren().get(0).toSymbolWord().getSymbol(), is(":one"));
//        assertThat(res.getChildren().get(1).type(), is(NodeType.LIST));
//        assertThat(res.getChildren().get(1).getChildren().get(0).type(), is(NodeType.SYMBOL));
//        assertThat(res.getChildren().get(1).getChildren().get(0).toSymbolWord().getSymbol(), is("\"two"));
//        assertThat(res.getChildren().get(1).getChildren().get(1).type(), is(NodeType.SYMBOL));
//        assertThat(res.getChildren().get(1).getChildren().get(1).toSymbolWord().getSymbol(), is("?3"));
//    }
//
//    @Test
//    public void testOutput() {
//        String input = "to stoptest\n"
//                + "make \"test \"one\n"
//                + "output \"three\n"
//                + "make \"test \"two\n"
//                + "end\n"
//                + "\n"
//                + "stoptest\n"
//                + "print :test\n"
//                + "stoptest\n";
//
//        Node res = it.eval(input);
//
//        assertThat(outputs.size(), is(1));
//        assertThat(outputs.get(0), is("one\n"));
//        assertThat(res.type(), is(NodeType.QUOTE));
//        assertThat(res.toQuotedWord().getQuote(), is("three"));
//    }
//    @Test
//    public void testAlias() {
//        String input = "alias \"print \"drucke\n"
//                + "drucke \"test\n";
//
//        it.eval(input);
//
//        assertThat(outputs.size(), is(1));
//        assertThat(outputs.get(0), is("test\n"));
//    }
    
    @Test
    public void testRecursion2() {
        String input = "to recurse :i\n"
                + "print :i\n"
                + "if (:i > 0) [recurse :i - 1]\n"
                + "end\n"
                + "\n"
                + "recurse 8000\n";

        Node res = it.run(it.read(input));
        
        assertThat(outputs.size(), is(8001));
        
        for(int i = 8000; i >= 0; i--) {
            assertThat(outputs.get(8000-i), is(i + "\n"));
        }        
    }
    
//    @Test
//    public void testOutput() {
//        String input = "to testoutput :i\n"
//                + "print \"one\n"
//                + "output :i\n"
//                + "print \"two\n"
//                + "end\n"
//                + "\n"
//                + "testoutput \"test\n";
//        
//        Node res = it.eval(input);
//
//        assertThat(outputs.size(), is(1));
//        assertThat(outputs.get(0), is("one\n"));
//    }

}
