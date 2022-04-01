/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.yali.lexer;

import static ch.uprisesoft.yali.lexer.TokenType.LEFT_BRACKET;
import static ch.uprisesoft.yali.lexer.TokenType.RIGHT_BRACKET;
import java.util.List;

/**
 *
 * @author rma
 */
public class ListScanner extends Scanner {

    int listDepth = 1;

    public ListScanner(Lexer context, String source) {
        super(context, source);
    }

    protected ListScanner(Lexer context, String source, List<Token> tokens, int start, int current, int line, int linePos, Token funStart, Token funEnd) {
        super(context, source, tokens, start, current, line, linePos, funStart, funEnd);
    }

    @Override
    public void scanToken() {
        char c = advance();
        switch (c) {
            case '[':
                addToken(LEFT_BRACKET);
                listDepth++;
                break;
            case ']':
                addToken(RIGHT_BRACKET);
                listDepth--;
                if (listDepth < 1) {
                    context.setScanner(new BaseScanner(context, source, tokens, start, current, line, linePos, funStart, funEnd));
                }
                break;
            case ' ':
                break;
            default:
                symbol();
                break;
        }
    }

    @Override
    protected boolean isNextSpecialChar() {
        return peek() == ' '
                || peek() == ']'
                || testEnd();
    }

    @Override
    protected void symbol() {
        while (!isNextSpecialChar()) {
            advance();
        }
        TokenType type = TokenType.SYMBOL;
        addToken(type);
    }
}
