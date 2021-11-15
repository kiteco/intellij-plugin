package com.kite.intellij.editor.events;

import com.intellij.mock.MockDocument;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.model.TextSelection;
import com.kite.intellij.platform.fs.UnixCanonicalPath;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

public class KiteEventTest extends KiteLightFixtureTest {
    @Test
    public void testOverride() {
        EditEvent e1 = new EditEvent(new MockDocument(), new UnixCanonicalPath("/home/test.py"), "content", 10, true);
        EditEvent e2 = new EditEvent(new MockDocument(), new UnixCanonicalPath("/home/test.py"), "content other", 15, true);

        SelectionEvent s1 = new SelectionEvent(new MockDocument(), new UnixCanonicalPath("/home/test.py"), "content other", TextSelection.create(15), true);
        SelectionEvent s2 = new SelectionEvent(new MockDocument(), new UnixCanonicalPath("/home/test.py"), "content other", TextSelection.create(10), true);

        FocusEvent f1 = new FocusEvent(new MockDocument(), new UnixCanonicalPath("/home/test.py"), "", 10, true);

        //edit overrides edit and selection
        Assert.assertTrue(e1.isOverriding(e2));
        Assert.assertTrue(e2.isOverriding(e1));
        Assert.assertTrue(e2.isOverriding(s1));
        Assert.assertFalse(e2.isOverriding(f1));

        //selection overrides edit and selection
        Assert.assertTrue(e1.isOverriding(s1));
        Assert.assertTrue(s1.isOverriding(e1));
        Assert.assertTrue(s1.isOverriding(s2));
        Assert.assertFalse(s1.isOverriding(f1));

        //focus overrides all
        Assert.assertTrue(f1.isOverriding(e1));
        Assert.assertTrue(f1.isOverriding(s1));
    }

    @Test
    public void testDifferentFilesOverride() throws Exception {
        EditEvent e1 = new EditEvent(new MockDocument(), new UnixCanonicalPath("/home/test.py"), "content", 10, true);
        EditEvent e2 = new EditEvent(new MockDocument(), new UnixCanonicalPath("/home/other.py"), "content other", 15, true);

        SelectionEvent s1 = new SelectionEvent(new MockDocument(), new UnixCanonicalPath("/home/test.py"), "content other", TextSelection.create(15), true);
        SelectionEvent s2 = new SelectionEvent(new MockDocument(), new UnixCanonicalPath("/home/other.py"), "content other", TextSelection.create(10), true);

        FocusEvent f1 = new FocusEvent(new MockDocument(), new UnixCanonicalPath("/home/test.py"), "", 10, true);
        FocusEvent f2 = new FocusEvent(new MockDocument(), new UnixCanonicalPath("/home/other.py"), "", 10, true);

        Assert.assertFalse(e1.isOverriding(e2));
        Assert.assertFalse(e1.isOverriding(s2));
        Assert.assertFalse(e1.isOverriding(f2));

        Assert.assertFalse(s1.isOverriding(e2));
        Assert.assertFalse(s1.isOverriding(s2));
        Assert.assertFalse(s1.isOverriding(f2));

        Assert.assertFalse(f1.isOverriding(e2));
        Assert.assertFalse(f1.isOverriding(s2));
        Assert.assertFalse(f1.isOverriding(f2));
    }
}