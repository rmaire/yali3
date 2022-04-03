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
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.runtime.io.InputGenerator;
import ch.uprisesoft.yali.runtime.io.OutputObserver;

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
        it.addTracer(new PrintingTracer());

//        String input = "to recurse :i\n"
//                + "print :i\n"
////                + "if (:i > 0) [recurse :i + 1]\n"
//                + "recurse :i + 1\n"
//                + "end\n"
//                + "\n"
//                + "recurse 1\n";
//
//        Node res = it.run(it.read(input));
//
//String input = "ifelse (1 > 0) [print \"Hello!] [print \"Nope!]\n";
//        String input = "to testit\n"
//                + "ifelse (1 > 0) [print \"Hello!] [print \"Nope!]\n"
//                + "end\n";
//        List ast = it.read(input).toList();
//        it.run(it.read("testit\n"));
//
        //        String input = "ifelse (1 > 0) [print \"Hello!] [print \"Nope!]\n";
//        it.run(it.read(input));

        StringBuilder sb = new StringBuilder();
        sb.append("make \"size 81 / 9").append("\n");
        sb.append("print 2*3").append("\n");
        sb.append("print :size - 4").append("\n");
        Node res = it.run(it.read(sb.toString()));

//        StringBuilder sb = new StringBuilder();
//        sb.append("make \"first_programmer \"Ada_Lovelace").append("\n");
//        sb.append("print :first_programmer").append("\n");
//        StringBuilder sb = new StringBuilder();
//        sb.append("to pausetest").append("\n");
//        sb.append("if (1 > 0) [pause print \"two]").append("\n");
//        sb.append("pause").append("\n");
//        sb.append("print \"three").append("\n");
//        sb.append("end").append("\n");
//        sb.append("\n");
//        sb.append("print \"one").append("\n");
//        sb.append("pause").append("\n");
//        sb.append("pausetest").append("\n");
//        it.run(it.read(sb.toString()));
//        it.resume();
//        it.resume();
//        it.resume();
//        String input = "[print butfirst [one two]]\n";
//        Node list = it.read(input);
//        Node prog = it.read(list.getChildren().get(0).toList());
//        it.run(prog);
//        System.out.println(prog.toString());
//        String input = "print butfirst [one two]\n";
//        Node prog = it.read(input);
//        System.out.println(prog.toString());
//        Repl2 repl = new Repl2();
//        repl.runPrompt();
//        String input = "to listit\n"
//                + "if (1 > 0) [ print \"one pause print butfirst [one two] print \"three ]\n"
//                + "end\n"
//                + "\n"
//                + "listit\n";
//        Node res = it.run(it.read(input));
//        it.resume();
//        String input = "print \"one pause print butfirst [one two] print \"three\n";
////        String input = "run [print \"one  pause print butfirst [one two] print \"three]\n";
//
//        Node res = it.run(it.read(input));
//        it.resume();
    }
}
