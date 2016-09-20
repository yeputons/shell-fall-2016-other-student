package ru.spbau.mit.parsing;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Just a simple tokenizer.
 */
public final class Tokenizer {

    @NotNull private final String line;

    private int lastPosition;
    private int currentPosition;

    private boolean isExpectingCommand; // we expect to be a command first word and each word next to the pipe.
    private boolean isInsideSingleQuoted;
    private boolean isInsideDoubleQuoted;
    private boolean isAssignment;
    private boolean isPipe;
    private boolean isOmitted;

    public Tokenizer(@NotNull String line) {
        this.line = line.trim() + " "; // we add space only because its easier to not to think about end of line
        lastPosition = 0;
    }

    /**
     * tokenize given line,
     * @return array ot tokens
     * @throws IllegalArgumentException if we could not parse given line
     */
    @NotNull
    public Token[] tokenizeCommands() throws IllegalArgumentException {
        isExpectingCommand = true;
        return tokenize();
    }

    /**
     * tokenize arguments for a command,
     * @return array ot tokens
     * @throws IllegalArgumentException if we could not parse given line
     */
    @NotNull
    public Token[] tokenizeArgs() throws IllegalArgumentException {
        isExpectingCommand = false;
        return tokenize();
    }

    private Token[] tokenize() throws IllegalArgumentException {
        if (line.trim().isEmpty()) {
            return new Token[0];
        }

        final ArrayList<Token> tokens = new ArrayList<>();

        while (lastPosition < line.length()) {
            tokens.add(getNextToken());
        }

        return tokens.toArray(new Token[0]);
    }

    /**
     * retrieves next token from the line and moves lastPosition
     * @return next token
     * @throws IllegalArgumentException if we could not parse given line
     */
    @NotNull
    private Token getNextToken() throws IllegalArgumentException {
        setUpFlags();

        currentPosition = lastPosition + 1;
        while (shouldProceed()) {
            updateFlags();
            currentPosition += 1;
        }

        if (currentPosition >= line.length()) {
            // This check works well because we added space in the end of the line
            throw new IllegalArgumentException("could not parse line '" + line
                    + "' starting from position " + lastPosition + "\n(" + line.substring(lastPosition) + ")");
        }

        final Token.TokenType tokenType = getTokenType();
        final String content = getTokenContent(tokenType);

        isExpectingCommand = tokenType == Token.TokenType.PIPE;

        while (isSpace()) {
            currentPosition += 1;
        }
        lastPosition = currentPosition;

        return new Token(tokenType, content);
    }

    private void setUpFlags() {
        char currentChar = line.charAt(lastPosition);

        isInsideDoubleQuoted = currentChar == '"';
        isInsideSingleQuoted = currentChar == '\'';
        isAssignment         = currentChar == '=';
        isPipe               = currentChar == '|';
        isOmitted            = false;
    }

    private void updateFlags() {
        char currentChar = line.charAt(currentPosition);

        if (!isInsideSingleQuoted && !isInsideDoubleQuoted && currentChar == '=') {
            isAssignment = true;
        }
        if (!isInsideDoubleQuoted) {
            isInsideSingleQuoted ^= currentChar == '\'';
        }
        if (!isInsideSingleQuoted) {
            if (!(isInsideDoubleQuoted && isOmitted)) {
                isInsideDoubleQuoted ^= currentChar == '"';
            }
        }

        isOmitted = !isOmitted && currentChar == '\\';
    }

    private boolean shouldProceed() {
        if (currentPosition >= line.length()) {
            return false;
        }

        if (isInsideSingleQuoted || isInsideDoubleQuoted) {
            return true;
        }

        if (isPipe) {
            return false;
        }

        return line.charAt(currentPosition) != ' ' && line.charAt(currentPosition) != '|';
    }

    private boolean isSpace() {
        return currentPosition < line.length() && line.charAt(currentPosition) == ' ';
    }

    private Token.TokenType getTokenType() {
        if (isPipe) {
            return Token.TokenType.PIPE;
        }

        if (isAssignment) {
            return Token.TokenType.ASSIGNMENT;
        }

        if (line.charAt(lastPosition) == '\'') {
            return Token.TokenType.SINGLE_QUOTED_STRING;
        }

        if (line.charAt(lastPosition) == '"') {
            return Token.TokenType.DOUBLE_QUOTED_STRING;
        }

        if (isExpectingCommand) {
            return Token.TokenType.COMMAND;
        }

        return Token.TokenType.WORD;
    }

    //TODO I have problems with omitting characters but I didn't come up with a good solution yet.
    @NotNull
    private String getTokenContent(Token.TokenType tokenType) {
        switch (tokenType) {
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
                return line.substring(lastPosition + 1, currentPosition - 1);
            default:
                return line.substring(lastPosition, currentPosition);
        }
    }

}
