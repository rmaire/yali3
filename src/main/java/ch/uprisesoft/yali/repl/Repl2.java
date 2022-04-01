/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.repl;

import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.exception.NodeTypeException;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import ch.uprisesoft.yali.runtime.io.InputGenerator;
import ch.uprisesoft.yali.runtime.io.OutputObserver;
import ch.uprisesoft.yali.scope.VariableNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.ResourceBundle;
import org.jline.builtins.Nano;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 *
 * @author rmaire
 */
public class Repl2 implements InputGenerator, OutputObserver {

    private final InputStreamReader input;
    private final PrintStream output;

    private final Interpreter interpreter;
    private boolean procDefinitionMode = false;
    private ResourceBundle messages;

    private final Terminal terminal;
    private final LineReader reader;

    public Repl2(InputStreamReader input, PrintStream output) throws IOException {
        this.input = input;
        this.output = output;

        messages = ResourceBundle.getBundle("Translation", Locale.getDefault());

        procDefinitionMode = false;

        interpreter = new Interpreter();
        interpreter.loadStdLib(this, this);

        terminal = TerminalBuilder
                .terminal();
        reader = LineReaderBuilder.builder().terminal(terminal)
                .build();
        
    }

    public Repl2() throws IOException {
        this(new InputStreamReader(System.in), new PrintStream(System.out));
    }

    public void runPrompt() throws IOException {
        output.println("Welcome to Yali. To exit type ctrl-D or bye and press <ENTER>");

        while (true) {
            String line = null;
            try {
                line = reader.readLine("> ");
            } catch (UserInterruptException e) {
                // Ignore
            } catch (EndOfFileException e) {
                return;
            }

            if (line.trim().toLowerCase().equals("bye")) {
                break;
            } else if (line.toLowerCase().startsWith("edit")) {
                Path edit = Files.createTempFile("tmp", "tmp");
//                BufferedWriter writer = new BufferedWriter(new FileWriter(edit.toFile()));
//                writer.write("Bla\nBlubb");
//                writer.close();
                Nano nano = new Nano(terminal, edit);
                System.out.println(edit.getFileName().toString());
                nano.open(edit.getFileName().toString());
//                nano.title = "Yali Editor";
                nano.run();
            } else {
                run(line);
            }
        }
    }

    private void run(String source) {
        try {
            Node result = interpreter.run(interpreter.read(source));
            output.println("; " + result.toString());
        } catch (NodeTypeException nte) {
            if (nte.getExpected().contains(NodeType.PROCCALL) && nte.getReceived().equals(NodeType.SYMBOL)) {
                output.println(
                        String.format(
                                "; " + messages.getString("function_not_found"),
                                nte.getNode().token().get(0).getLexeme(),
                                nte.getReceived()
                        )
                );
            } 
            else if (nte.getExpected().contains(NodeType.PROCCALL)) {
                output.println(
                        String.format(
                                "; " + messages.getString("redundant_argument"),
                                nte.getNode().toString(),
                                nte.getReceived()
                        )
                );
            } 
            else {
                output.println(
                        String.format(
                                "; " + messages.getString("not_expected"),
                                nte.getNode().token().get(0).getLexeme(),
                                nte.getExpected(),
                                nte.getReceived()
                        )
                );
            }
        } catch (VariableNotFoundException vnfe) {
            output.println(
                    String.format(
                            "; " + messages.getString("variable_not_found"),
                            vnfe.getName()
                    )
            );
        }
    }

    @Override
    public String request() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String requestLine() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void inform(String output) {
//        this.output.println();
        this.output.println(output);
    }

}
