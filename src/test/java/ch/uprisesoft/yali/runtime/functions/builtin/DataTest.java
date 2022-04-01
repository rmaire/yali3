/* 
 * Copyright 2020 Uprise Software.
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
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.runtime.io.InputGenerator;
import ch.uprisesoft.yali.runtime.io.OutputObserver;
import java.util.ArrayList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author uprisesoft@gmail.com
 */
public class DataTest {
    
    Interpreter it;
    private OutputObserver oo;
    private InputGenerator ig;
    private java.util.List<String> outputs;

    public DataTest() {
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
        
        it = om.getInterpreter();
        
        outputs = new ArrayList<>();
    }

    @Test
    public void testList() {
       StringBuilder sb = new StringBuilder();
       sb.append("output [1 2 3 4]").append("\n");
       Node res = it.run(it.read(sb.toString()));

       assertThat(res.type(), is(NodeType.LIST));
       assertThat(res.getChildren().size(), is(4));
    }
    
    @Test
    public void testNestedList() {
       StringBuilder sb = new StringBuilder();
        sb.append("output [1 2 [3 4]]").append("\n");
        Node res = it.run(it.read(sb.toString()));

        assertThat(res.type(), is(NodeType.LIST));
        assertThat(res.getChildren().size(), is(3));
        assertThat(res.getChildren().get(2).type(), is(NodeType.LIST));
        assertThat(res.getChildren().get(2).getChildren().get(1).type(), is(NodeType.SYMBOL));
    }
    
    @Test
    public void testListFunction() {
        StringBuilder sb = new StringBuilder();
        sb.append("list 1 2").append("\n");
        Node res = it.run(it.read(sb.toString()));

        assertThat(res.type(), is(NodeType.LIST));
        assertThat(res.getChildren().size(), is(2));
        assertThat(res.getChildren().get(0).toString(), is("1"));
        assertThat(res.getChildren().get(1).toString(), is("2"));
        
        sb = new StringBuilder();
        sb.append("(list 1 2 3 4)").append("\n");
        res = it.run(it.read(sb.toString()));

        assertThat(res.type(), is(NodeType.LIST));
        assertThat(res.getChildren().size(), is(4));
        assertThat(res.getChildren().get(0).toString(), is("1"));
        assertThat(res.getChildren().get(3).toString(), is("4"));
        
        sb = new StringBuilder();
        sb.append("(list 1 [2 3 4])").append("\n");
        res = it.run(it.read(sb.toString()));

        assertThat(res.type(), is(NodeType.LIST));
        assertThat(res.getChildren().size(), is(2));
        assertThat(res.getChildren().get(0).toString(), is("1"));
        assertThat(res.getChildren().get(1).type(), is(NodeType.LIST));
    }
    
    @Test
    public void testSentenceFunction() {
        StringBuilder sb = new StringBuilder();
        sb.append("sentence 1 2").append("\n");
        Node res = it.run(it.read(sb.toString()));

        assertThat(res.type(), is(NodeType.LIST));
        assertThat(res.getChildren().size(), is(2));
        assertThat(res.getChildren().get(0).toString(), is("1"));
        assertThat(res.getChildren().get(1).toString(), is("2"));
        
        sb = new StringBuilder();
        sb.append("(sentence 1 2 3 4)").append("\n");
        res = it.run(it.read(sb.toString()));

        assertThat(res.type(), is(NodeType.LIST));
        assertThat(res.getChildren().size(), is(4));
        assertThat(res.getChildren().get(0).toString(), is("1"));
        assertThat(res.getChildren().get(3).toString(), is("4"));
        
        sb = new StringBuilder();
        sb.append("(sentence 1 [2 3 4])").append("\n");
        res = it.run(it.read(sb.toString()));

        assertThat(res.type(), is(NodeType.LIST));
        assertThat(res.getChildren().size(), is(4));
        assertThat(res.getChildren().get(0).toString(), is("1"));
        assertThat(res.getChildren().get(3).type(), is(NodeType.SYMBOL));
        // TODO
        assertThat(res.getChildren().get(3).toString(), is("4"));
        
        sb = new StringBuilder();
        sb.append("(sentence 1 [2 [3 4]])").append("\n");
        res = it.run(it.read(sb.toString()));

        assertThat(res.type(), is(NodeType.LIST));
        assertThat(res.getChildren().size(), is(4));
        assertThat(res.getChildren().get(0).toString(), is("1"));
        assertThat(res.getChildren().get(3).type(), is(NodeType.SYMBOL));
        assertThat(res.getChildren().get(3).toString(), is("4"));
    }
}
