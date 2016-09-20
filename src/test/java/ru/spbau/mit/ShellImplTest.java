package ru.spbau.mit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import ru.spbau.mit.commands.CommandFactory;
import ru.spbau.mit.parsing.Token;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class ShellImplTest {

    private ShellImpl shell;

    @Rule
    public final Timeout globalTimeout = new Timeout(2, TimeUnit.SECONDS);

    @Before
    public void createShell() {
        shell = new ShellImpl(CommandFactory.INSTANCE,
                System.in, System.out);
    }

    @Test
    public void testSubstituteVariables() {
        shell.setVariable("x", "123");
        shell.setVariable("some_1variable1", "aba c aba ");

        final Token testToken1 = new Token(Token.TokenType.DOUBLE_QUOTED_STRING, "$x$'$$some_1variable1'123'");
        final String answer1 = "123$'$aba c aba '123'";
        shell.substituteVariables(testToken1);

        assertEquals(answer1, testToken1.getContent());

        final Token testToken2 = new Token(Token.TokenType.SINGLE_QUOTED_STRING, "$x$'$$some_1variable1\"123\"");
        final String answer2 = "$x$'$$some_1variable1\"123\"";
        shell.substituteVariables(testToken1);

        assertEquals(answer2, testToken2.getContent());

        final Token testToken3 = new Token(Token.TokenType.DOUBLE_QUOTED_STRING, "\\\"");
        final String answer3 = "\\\"";
        shell.substituteVariables(testToken3);

        assertEquals(answer3, testToken3.getContent());
    }

    @Test
    public void testParseLine() {
        // it's not a correct bash line, but it should be parsed by our parser
        final String sample1 = "some_var=\"123\"|x=\"$some_var\"|y='$some_var'|z=$y";
        final String answer1 = " |  |  |  ";

        Assert.assertEquals(answer1, shell.parseLine(sample1));

        final String sample2 = "x=\"aba c aba\"|echo $x|echo \"\\\"$x\"";
        final String answer2 = " | echo aba c aba | echo \"\\\"aba c aba\" ";

        Assert.assertEquals(answer2, shell.parseLine(sample2));
    }

    @Test
    public void testProcessLineSimple() {
        final String sample1 = "x=123 | echo \"$x\" '$x' $x 'something' /proc/cpuinfo | cat";
        final String answer1 = "123 $x 123 something /proc/cpuinfo\n";

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ShellImpl shell = new ShellImpl(CommandFactory.INSTANCE, System.in, out);

        try {
            shell.processLine(sample1);
        } catch (InvocationTargetException e) {
            // this is impossible
        }

        assertEquals(answer1, out.toString());
    }


    @Test
    public void testProcessLineHard() throws IOException {
        final File temp = File.createTempFile("process_sample1_file", ".tmp");

        final String sample1 = "x=\"some 'interesting' text\" | echo \"$x\" | tee " + temp.getAbsolutePath()
                + " | echo '$x'";
        final String answer1File = "some 'interesting' text\n";
        final String answer1Out = "$x\n";

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ShellImpl shell = new ShellImpl(CommandFactory.INSTANCE, System.in, out);

        try {
            shell.processLine(sample1);
        } catch (InvocationTargetException e) {
            // this is impossible
        }

        assertEquals(answer1File, new String(Files.readAllBytes(Paths.get(temp.getAbsolutePath()))));
        assertEquals(answer1Out, out.toString());
    }
}