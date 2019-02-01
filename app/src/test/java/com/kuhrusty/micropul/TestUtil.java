package com.kuhrusty.micropul;

import android.content.Context;
import android.content.res.AssetManager;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Matchers.anyString;

/**
 * A couple utility methods for use in tests, copied from Morbad Scorepad and
 * then screwed with in ways I will probably wish I'd copied back to Morbad
 * Scorepad.
 */
public class TestUtil {

    /**
     * Set this to System.err or whatever if you want to see when/where the
     * Context returned by mockContext() is opening files.
     */
    public static PrintStream mockContextFileLog = null;

    /**
     * This returns a mock Context with the following methods mocked:
     * <ul>
     *     <li>getAssets().open() returns a file from src/main/assets</li>
     *     <li>openFileInput() returns a file from src/test/resources</li>
     *     <li>openFileOutput() returns a file from build/tmp</li>
     * </ul>
     * <p>Set <code>TestUtil.mockContextFileLog</code> to System.err or whatever
     * if you want to see when/where the Context is opening files.</p>
     */
    public static Context mockContext() throws Exception {
        return mockContext("src/main/assets", "src/test/resources", "build/tmp");
    }

    /**
     * This returns a mock Context with the following methods mocked:
     * <ul>
     *     <li>getAssets().open() returns a file from the given assetBase</li>
     *     <li>openFileInput() returns a file from the given privateInputBase</li>
     *     <li>openFileOutput() returns a file from the given privateOutputBase</li>
     * </ul>
     * <p>Set <code>TestUtil.mockContextFileLog</code> to System.err or whatever
     * if you want to see when/where the Context is opening files.</p>
     */
    public static Context mockContext(String assetBase,
                                      String privateInputBase,
                                      String privateOutputBase) throws Exception {
        Context rv = PowerMockito.mock(Context.class);
        AssetManager am = PowerMockito.mock(AssetManager.class);
        PowerMockito.doReturn(am).when(rv).getAssets();
        PowerMockito.when(am.open(anyString())).thenAnswer(new FileInputStreamAnswer(assetBase));
        PowerMockito.when(rv.openFileInput(anyString())).thenAnswer(new FileInputStreamAnswer(privateInputBase));
        PowerMockito.when(rv.openFileOutput(anyString(), anyInt())).thenAnswer(new FileOutputStreamAnswer(privateOutputBase));
        return rv;
    }

    private static class FileInputStreamAnswer implements Answer<InputStream> {
        private String base;
        FileInputStreamAnswer(String base) {
            this.base = base;
        }
        @Override
        public InputStream answer(InvocationOnMock invocation) throws Throwable {
            Object[] args = invocation.getArguments();
            //Object mock = invocation.getMock();
            String newFullPath = base + "/" + args[0];
            if (mockContextFileLog != null) {
                mockContextFileLog.println("mockContext opening " + newFullPath +
                        " for input");
            }
            return new FileInputStream(newFullPath);
        }
    }
    private static class FileOutputStreamAnswer implements Answer<OutputStream> {
        private String base;
        FileOutputStreamAnswer(String base) {
            this.base = base;
        }
        @Override
        public OutputStream answer(InvocationOnMock invocation) throws FileNotFoundException {
            Object[] args = invocation.getArguments();
            //Object mock = invocation.getMock();
            String newFullPath = base + "/" + args[0];
            if (mockContextFileLog != null) {
                mockContextFileLog.println("mockContext opening " + newFullPath +
                        " for output");
            }
            return new FileOutputStream(newFullPath);
        }
    }

    /**
     * Asserts that the contents of the two given files are identical.
     *
     * @param expectFileName will be read from the filesystem
     * @param gotFileName will be read from the filesystem
     * @param ignoreComments true if lines starting with "//" should be ignored
     * @param ignoreWhitespace true if \s+ should be evaluated as a single space
     */
    public static void compare(String expectFileName, String gotFileName,
                               boolean ignoreComments, boolean ignoreWhitespace)
            throws IOException {
        String es = snort(expectFileName, false, ignoreComments, ignoreWhitespace);
        String gs = snort(gotFileName, false, ignoreComments, ignoreWhitespace);
        assertEquals("expected contents of " + expectFileName + ", got " + gotFileName,
                es, gs);
    }

    /**
     * Reads the entire text file into a String.
     *
     * @param fileName
     * @param classpath true if this should be loaded from CLASSPATH, false if
     *                  from the filesystem.
     * @param ignoreComments true if lines starting with // should be discarded.
     * @param ignoreWhitespace true if contiguous whitespace should be collapsed
     *                         into a single space.
     */
    public static String snort(String fileName, boolean classpath,
                                boolean ignoreComments, boolean ignoreWhitespace)
            throws IOException {
        BufferedReader in = new BufferedReader(classpath ?
                new InputStreamReader(TestUtil.class.getClassLoader().getResourceAsStream(fileName)) :
                new FileReader(fileName));
        StringBuilder buf = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            if (ignoreComments && (line.matches("^\\s*//.+$"))) {
                continue;
            }
            if (ignoreWhitespace) line = line.replaceAll("\\s+", " ");
            buf.append(line).append("\n");
        }
        return buf.toString();
    }
}
