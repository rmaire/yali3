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
import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
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

    public void runPrompt() throws IOException, InterruptedException {
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
//                Path edit = Files.createTempFile("yali", ".tmp");
//                BufferedWriter writer = new BufferedWriter(new FileWriter(edit.toFile()));
//                writer.write("Bla\nBlubb");
//                writer.close();
//                Nano nano = new Nano(terminal, edit);
//                System.out.println(edit.getFileName().toString());
//                System.out.println(edit.toAbsolutePath().toString());
//                nano.open(edit.toAbsolutePath().toString());
//                nano.title = "Yali Editor";
//                nano.run();

                String[] procLine = line.split(" ");
                String editResult = "";
                if (procLine.length > 1) {
                    editResult = editProc(procLine[1]);
                } else {
                    editResult = editProc();
                }
                
                interpreter.read(editResult);
                
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
            } else if (nte.getExpected().contains(NodeType.PROCCALL)) {
                output.println(
                        String.format(
                                "; " + messages.getString("redundant_argument"),
                                nte.getNode().toString(),
                                nte.getReceived()
                        )
                );
            } else {
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

    private String editProc(String procName) throws IOException, InterruptedException {

        File file = File.createTempFile("temp", ".lgt");
        file.deleteOnExit();

        if (interpreter.env().defined(procName)) {
            String proc = interpreter.env().procedure(procName).getSource();

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(proc);
            writer.close();
        } else {

            String proc = "to " + procName + "\n" + "end";
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(proc);
            writer.close();
        }

//        Files.write(file.toPath(), fun.);
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            Process process = new ProcessBuilder()
                    .command("notepad.exe", file.toPath().toAbsolutePath().toString())
                    .directory(file.toPath().getParent().toFile())
                    .redirectErrorStream(true)
                    .start();
            process.waitFor();
        } else {
            Desktop.getDesktop().edit(file);
        }

        String newContent = Files
                .lines(file.toPath(), StandardCharsets.UTF_8)
                .collect(Collectors.joining("\n"));

//        System.out.println(newContent);

        return newContent;
    }

    private String editProc() throws IOException, InterruptedException {

        File file = File.createTempFile("temp", ".lgt");
        file.deleteOnExit();

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            Process process = new ProcessBuilder()
                    .command("notepad.exe", file.toPath().toAbsolutePath().toString())
                    .directory(file.toPath().getParent().toFile())
                    .redirectErrorStream(true)
                    .start();
            process.waitFor();
        } else {
            Desktop.getDesktop().edit(file);
        }

        String newContent = Files
                .lines(file.toPath(), StandardCharsets.UTF_8)
                .collect(Collectors.joining("\n"));

//        System.out.println(newContent);

        return newContent;
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
