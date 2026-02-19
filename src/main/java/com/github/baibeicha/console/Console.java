package com.github.baibeicha.console;

import com.github.baibeicha.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Console {

    public static boolean AUTO_USE_FLASH = true;
    public final static PrintStream STDOUT = System.out;
    public final static PrintStream STDERR = System.err;
    public final static InputStream STDIN = System.in;

    protected boolean autoUseFlash;

    protected PrintStream out;
    protected PrintStream err;
    protected InputStream in;
    protected Scanner scanner;

    public Console() {
        setAutoUseFlash(AUTO_USE_FLASH);
        setInputStream(STDIN);
        setOutputStream(STDOUT);
        setErrorStream(STDERR);
    }

    public Console(InputStream in, PrintStream out) {
        setAutoUseFlash(AUTO_USE_FLASH);
        setInputStream(in);
        setOutputStream(out);
        setErrorStream(STDERR);
    }

    public Console(InputStream in, PrintStream out, PrintStream err) {
        setAutoUseFlash(AUTO_USE_FLASH);
        setInputStream(in);
        setOutputStream(out);
        setErrorStream(err);
    }

    public String readLine() {
        return scanner.nextLine();
    }

    public String read() {
        return scanner.next();
    }

    public String read(String pattern) {
        return scanner.next(pattern);
    }

    public String read(Pattern pattern) {
        return scanner.next(pattern);
    }

    public String readPassword() throws IOException {
        StringBuilder password = new StringBuilder();

        char c;
        while ((c = (char)in.read()) != '\n' && c != '\r') {
            if (c != '\b') {
                password.append(c);
                out.print("\b");
                out.flush();
            } else {
                password.deleteCharAt(password.length() - 1);
            }
        }

        return password.toString();
    }

    public boolean hasNext() {
        return scanner.hasNext();
    }

    public int readInt() {
        return scanner.nextInt();
    }

    public double readDouble() {
        return scanner.nextDouble();
    }

    public float readFloat() {
        return scanner.nextFloat();
    }

    public long readLong() {
        return scanner.nextLong();
    }

    public short readShort() {
        return scanner.nextShort();
    }

    public boolean readBoolean() {
        return scanner.nextBoolean();
    }

    public byte readByte() {
        return scanner.nextByte();
    }

    public void print(String msg) {
        out.print(msg);
        if (autoUseFlash) {
            out.flush();
        }
    }

    public void println(String msg) {
        out.println(msg);
        if (autoUseFlash) {
            out.flush();
        }
    }

    public void println() {
        out.println();
        if (autoUseFlash) {
            out.flush();
        }
    }

    public void printf(String format, Object... args) {
        out.print(StringUtils.format(format, args));
        if (autoUseFlash) {
            out.flush();
        }
    }

    public void println(String format, Object... args) {
        out.println(StringUtils.format(format, args));
        if (autoUseFlash) {
            out.flush();
        }
    }

    public void printf(String format, String delimiter, Object... args) {
        out.print(StringUtils.format(format, delimiter, args));
        if (autoUseFlash) {
            out.flush();
        }
    }

    public void println(String format, String delimiter, Object... args) {
        out.println(StringUtils.format(format, delimiter, args));
        if (autoUseFlash) {
            out.flush();
        }
    }

    public void flush() {
        out.flush();
        err.flush();
    }

    public void close() throws IOException {
        out.close();
        in.close();
        err.close();
    }

    public void error(String msg) {
        err.println(msg);
        if (autoUseFlash) {
            err.flush();
        }
    }

    public void error(String msg, Throwable e) {
        err.println(msg);
        err.println(e);
        if (autoUseFlash) {
            err.flush();
        }
    }

    public void error(Throwable e) {
        err.println(e);
        if (autoUseFlash) {
            err.flush();
        }
    }

    public void setAutoUseFlash(boolean autoUseFlash) {
        this.autoUseFlash = autoUseFlash;
    }

    public void setInputStream(InputStream in) {
        this.in = in;
        this.scanner = new Scanner(in);
    }

    public void setOutputStream(PrintStream out) {
        this.out = out;
    }

    public void setErrorStream(PrintStream err) {
        this.err = err;
    }

    public void setInputStream(File file) throws FileNotFoundException {
        this.in = new FileInputStream(file);
        this.scanner = new Scanner(in);
    }

    public void setErrorStream(File file) throws FileNotFoundException {
        this.err = new PrintStream(file);
    }

    public void setOutputStream(File file) throws FileNotFoundException {
        this.out = new PrintStream(file);
    }
}
