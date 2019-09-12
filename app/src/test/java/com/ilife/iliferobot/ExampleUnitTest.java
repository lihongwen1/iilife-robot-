package com.ilife.iliferobot;

import com.ilife.iliferobot.utils.UserUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertTrue("the email is incorrect", UserUtils.isEmail("18565713334@163.com"));
    }
}