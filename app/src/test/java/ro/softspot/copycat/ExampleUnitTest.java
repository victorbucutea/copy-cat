package ro.softspot.copycat;

import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Stack;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        LinkedList<String> clippedItems = new LinkedList<>();

        clippedItems.addFirst("String 1");
        clippedItems.addFirst("String 2");
        clippedItems.addFirst("String 3");
        clippedItems.addFirst("String 4");
        clippedItems.addFirst("String 5");

    }
}