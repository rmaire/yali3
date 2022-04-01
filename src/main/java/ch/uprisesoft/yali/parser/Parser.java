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
package ch.uprisesoft.yali.parser;

import ch.uprisesoft.yali.ast.node.Call;
import ch.uprisesoft.yali.ast.node.Procedure;
import ch.uprisesoft.yali.ast.node.List;
import ch.uprisesoft.yali.ast.node.Node;
import ch.uprisesoft.yali.ast.node.word.BooleanWord;
import ch.uprisesoft.yali.ast.node.word.FloatWord;
import ch.uprisesoft.yali.ast.node.word.NilWord;
import ch.uprisesoft.yali.ast.node.word.QuotedWord;
import ch.uprisesoft.yali.ast.node.word.SymbolWord;
import ch.uprisesoft.yali.lexer.Token;
import ch.uprisesoft.yali.lexer.TokenType;
import ch.uprisesoft.yali.ast.node.NodeType;
import ch.uprisesoft.yali.ast.node.word.IntegerWord;
import ch.uprisesoft.yali.exception.TokenTypeException;
import ch.uprisesoft.yali.lexer.Lexer;
import ch.uprisesoft.yali.runtime.interpreter.Interpreter;
import java.util.ArrayList;

public class Parser {


    private java.util.List<Token> tokens = new ArrayList<>();
    private final Interpreter it;
    private String source;

    private int current = 0;
    private boolean inParens = false;

    public Parser(Interpreter functions) {
        this.it = functions;
    }

    public Interpreter getFunctions() {
        return it;
    }
    
    public Node read(String source) {
        this.source = source;
        this.tokens = new Lexer().scan(source);

        return read();
    }
    
    public Node read(ch.uprisesoft.yali.ast.node.List list) {
        tokens.clear();
        
        StringBuilder newSource = new StringBuilder();
        
        for (Token t: list.token().subList(1, list.token().size()-1)) {
            newSource.append(t.getLexeme()).append(" ");
        }

//        java.util.List<Token> token = list.token().subList(1, list.token().size()-1);
//        token.add(new Token(TokenType.EOF, "", current, current, current));
        
//        tokens.addAll(token);
        
        return read(newSource.toString().trim());
    }

    
    
    private Node read() {
        parseFunctionHeaders();
        parseFunctionAliases();

        Node program = new List();
        while (!isAtEnd()) {
            Node expression = parseExpression();
            if (!expression.type().equals(NodeType.NONE) && !expression.type().equals(NodeType.PROCEDURE)) {
                program.addChild(expression);
            }
        }
        return program;
    }

    private void parseFunctionHeaders() {
        while (!isAtEnd()) {
            if (match(TokenType.TO)) {

                Token defStartToken = previous();
                Procedure fun = new Procedure(consume(TokenType.SYMBOL).getLexeme());
                java.util.List<String> args = new java.util.ArrayList<>();
                while (!check(TokenType.NEWLINE)) {
                    args.add(consume(TokenType.REFERENCE).getLexeme().substring(1));
                }
                fun.setArgs(args);
                advance();

                fun.setPosInSource(defStartToken.getLine(), defStartToken.getPos());
                it.env().define(fun);
            }
            advance();
        }
        current = 0;
    }

    private void parseFunctionAliases() {
        while (!isAtEnd()) {
            if (match(TokenType.SYMBOL) && previous().getLexeme().toLowerCase().equals("alias")) {

                String original = consume(TokenType.QUOTE).getLexeme().substring(1);
                String alias = consume(TokenType.QUOTE).getLexeme().substring(1);

                it.env().alias(original, alias);

                advance();
            }
            advance();
        }
        current = 0;
    }

    private Node parseExpression() {
        Node node = expression();
        return node;
    }

    private Node expression() {
        if (match(TokenType.NEWLINE)) {
            return Node.none();
        }
        return funBody();
    }

    private Node funBody() {
        Node node = Node.none();

        if (match(TokenType.TO)) {

            Procedure fun = it.env().procedure(consume(TokenType.SYMBOL).getLexeme());

            while (!check(TokenType.NEWLINE)) {
                advance();
            }
            advance();

            while (!check(TokenType.END)) {
                fun.addChild(expression());

                // Check for unclosed function body
                if (peek().type().equals(TokenType.EOF)) {
                    throw new TokenTypeException(TokenType.END, TokenType.EOF);
                }

                advance();
            }
            consume(TokenType.END);

            fun.setSource(previous().getLexeme());

            match(TokenType.NEWLINE);

            it.env().define(fun);

            node = fun;
        } else {
            node = funCall();
        }

        return node;
    }

    private Node funCall() {
        Node node = Node.none();

        if (current().type().equals(TokenType.SYMBOL) && it.env().defined(current().getLexeme().toLowerCase())) {

            String name = current().getLexeme();
            int arity = it.env().procedure(name).getArity();
            advance();

            node = new Call(name);
            node.setPosInSource(current().getLine(), current().getPos());

            if (inParens) {
                while (!check(TokenType.RIGHT_PAREN)) {
                    node.addChild(expression());
                }
            } else {
                for (int i = 0; i < arity; i++) {
                    node.addChild(expression());
                }
            }

        } else {
            node = equality();
        }

        return node;
    }

    private Node equality() {
        Node node = comparison();

        if (match(TokenType.EQUAL, TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {

            TokenType operator = previous().type();
            Node left = node;
            Node right = comparison();

            if (operator.equals(TokenType.EQUAL) || operator.equals(TokenType.EQUAL_EQUAL)) {
                node = new Call("equal?");
            }

            if (operator.equals(TokenType.BANG_EQUAL)) {
                node = new Call("notequal?");
            }

            node.addChild(left);
            node.addChild(right);
            node.setPosInSource(previous().getLine(), previous().getPos());
        }

        return node;
    }

    private Node comparison() {
        Node node = term();

        while (match(TokenType.LESS, TokenType.GREATER, TokenType.LESS_EQUAL, TokenType.GREATER_EQUAL)) {

            TokenType operator = previous().type();
            Node left = node;
            Node right = term();

            if (operator.equals(TokenType.LESS)) {
                node = new Call("less?");
            }

            if (operator.equals(TokenType.GREATER)) {
                node = new Call("greater?");
            }

            if (operator.equals(TokenType.LESS_EQUAL)) {
                node = new Call("lessequal?");
            }

            if (operator.equals(TokenType.GREATER_EQUAL)) {
                node = new Call("greaterequal?");
            }

            node.addChild(left);
            node.addChild(right);
            node.setPosInSource(previous().getLine(), previous().getPos());
        }

        return node;
    }

    private Node term() {
        Node node = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {

            TokenType operator = previous().type();
            Node left = node;
            Node right = factor();

            if (operator.equals(TokenType.PLUS)) {
                node = new Call("add");
            }

            if (operator.equals(TokenType.MINUS)) {
                node = new Call("sub");
            }

            node.addChild(left);
            node.addChild(right);
            node.setPosInSource(previous().getLine(), previous().getPos());
        }

        return node;
    }

    private Node factor() {
        Node node = word();

        while (match(TokenType.STAR, TokenType.SLASH)) {

            TokenType operator = previous().type();
            Node left = node;
            Node right = word();

            if (operator.equals(TokenType.STAR)) {
                node = new Call("mul");
            }

            if (operator.equals(TokenType.SLASH)) {
                node = new Call("div");
            }

            node.addChild(left);
            node.addChild(right);
            node.setPosInSource(previous().getLine(), previous().getPos());
        }

        return node;
    }

    private Node word() {
        Node node = Node.none();

        if (match(TokenType.FALSE)) {
            node = new BooleanWord(Boolean.FALSE);
            node.token(previous());
        } else if (match(TokenType.TRUE)) {
            node = new BooleanWord(Boolean.TRUE);
            node.token(previous());
        } else if (match(TokenType.NIL)) {
            node = new NilWord();
            node.token(previous());
        } else if (match(TokenType.NUMBER)) {
            Token token = previous();

            if (token.getLexeme().contains(".")) {
                node = new FloatWord(Double.parseDouble(token.getLexeme()));
            } else {
                node = new IntegerWord(Integer.parseInt(token.getLexeme()));
            }

            node.token(token);
        } else if (match(TokenType.SYMBOL)) {
            node = new SymbolWord(previous().getLexeme());
            node.token(previous());
        } else if (match(TokenType.QUOTE)) {
            node = new QuotedWord(previous().getLexeme().substring(1));
            node.token(previous());
        } else if (match(TokenType.REFERENCE)) {
            node = new Call("thing");
            SymbolWord symbol = new SymbolWord(previous().getLexeme().substring(1));
            symbol.token(previous());
            node.addChild(symbol);
//            node = new ReferenceWord(previous().getLexeme().substring(1));
            node.token(previous());
        } else if (match(TokenType.LEFT_BRACKET)) {
            node = parseList();
        } else if (match(TokenType.LEFT_PAREN)) {
            inParens = true;
            node = expression();
            node.setPosInSource(previous().getLine(), previous().getPos());
            consume(TokenType.RIGHT_PAREN);
            inParens = false;
        }
        return node;
    }

    private List parseList() {
        int start = previous().getAbsolute();
        
        ch.uprisesoft.yali.ast.node.List list = new ch.uprisesoft.yali.ast.node.List();
        list.token(previous());
        list.setPosInSource(previous().getLine(), previous().getPos());

        while (!check(TokenType.RIGHT_BRACKET) && !isAtEnd()) {
            if (match(TokenType.LEFT_BRACKET)) {
                List nestedList = parseList();
                list.addChild(nestedList);
                list.token(nestedList.token());
                continue;
            }
            list.addChild(new SymbolWord(current().getLexeme()));
//            word.token(current());
            list.token(current());
//            list.addChild(word);
            advance();
        }
        
        int end = current().getAbsolute()+1;
        list.source(source.substring(start, end));
        list.token(current());
        consume(TokenType.RIGHT_BRACKET);
        
        return list;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type() == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token current() {
        return tokens.get(current);
    }

    private Token consume(TokenType type) throws TokenTypeException {
        if (check(type)) {
            return advance();
        }
        throw new TokenTypeException(type, peek().type());
    }
}
