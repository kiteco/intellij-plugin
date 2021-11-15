package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class ModuleDetailsTest {

    @Test
    public void testBasic() throws Exception {
        ModuleDetails a = new ModuleDetails("file1.py", 5, Symbol.EMPTY_ARRAY);

        Assert.assertEquals("file1.py", a.getFilename());
        Assert.assertEquals(5, a.getTotalMembers());
        Assert.assertEquals(0, a.getMembers().length);
        Assert.assertFalse(a.hasMembers());
    }

    @Test
    public void testEquals() throws Exception {
        ModuleDetails a = new ModuleDetails("file1.py", 10, Symbol.EMPTY_ARRAY);
        ModuleDetails b = new ModuleDetails("file1.py", 10, Symbol.EMPTY_ARRAY);
        ModuleDetails c = new ModuleDetails("file other.py", 10, Symbol.EMPTY_ARRAY);

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);
        Assert.assertEquals(c, c);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(c, a);
    }
}