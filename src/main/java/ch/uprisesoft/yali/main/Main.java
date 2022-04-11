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
package ch.uprisesoft.yali.main;

import ch.uprisesoft.yali.ast.node.List;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.repl.PrintingTracer;
import ch.uprisesoft.yali.repl.Repl2;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.runtime.io.InputGenerator;
import ch.uprisesoft.yali.runtime.io.OutputObserver;
import ch.uprisesoft.yali.runtime.procedures.builtin.MockTurtleManager;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        java.util.List<String> outputs = new ArrayList<>();

        OutputObserver oo = new OutputObserver() {

            @Override
            public void inform(String output) {
                System.out.print(output);
                outputs.add(output);
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

        Interpreter it = new Interpreter();
        it.loadStdLib(oo, ig);
        MockTurtleManager mtm = new MockTurtleManager();
        mtm.registerProcedures(it);
        
//        Repl2 repl = new Repl2();
//        repl.runPrompt();
        
        String input1 = "to testit\n"
                + "ifelse (1 > 0) [print \"Hello!] [print \"Nope!]\n"
                + "end\n"
                + "print \"Bye!\n"
                + "print \"Well?\n"
                + "testit\n";
        
        String input2 = "to recurse :i\n"
                + "print :i\n"
                + "recurse :i + 1\n"
                + "end\n"
                + "\n"
                + "recurse 1\n";
        
        String input3 = "to recurse :i\n"
                + "print :i\n"
                + "if (:i > 0) [recurse :i - 1]\n"
                + "end\n"
                + "\n"
                + "recurse 8000\n";
        
        String input4 = "run [make \"test 3 + 4 print :test print \"BLAAA!]\n";
        
        String input5 = "to testit :i\n"
                + "if (:i < 10) [print \"first if (:i > 5) [print \"yes]]\n"
                + "end\n"
                + "\n"
                + "testit 6\n";
        
        Node ast = it.read(input5);
        it.load(ast);
        Node res = it.run();
        System.out.println(res);
    }
}
