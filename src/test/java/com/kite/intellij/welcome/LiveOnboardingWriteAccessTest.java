package com.kite.intellij.welcome;

import com.kite.intellij.lang.KiteLanguage;
import com.kite.intellij.lang.KiteLanguageSupport;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Test;

public class LiveOnboardingWriteAccessTest extends KiteLightFixtureTest {
    @Test
    public void testFiles() {
        LiveOnboardingWriteAccess access = new LiveOnboardingWriteAccess();

        assertTrue(access.isWritable(myFixture.configureByText("kite_tutorial.py", "").getVirtualFile()));
        assertTrue(access.isWritable(myFixture.configureByText("KiteOnboarding.py", "").getVirtualFile()));

        assertFalse(access.isWritable(myFixture.configureByText("kite_tutorial.txt", "").getVirtualFile()));
        assertFalse(access.isWritable(myFixture.configureByText("not_a_kite_file.py", "").getVirtualFile()));

        // the go plugin might be disabled and the VirtualFiles would be binary files. Skip if disabled.
        if (KiteLanguageSupport.isSupportedKiteOnboardingLanguage(KiteLanguage.Golang)) {
            assertTrue(access.isWritable(myFixture.configureByText("kite_tutorial.go", "").getVirtualFile()));
            assertFalse(access.isWritable(myFixture.configureByText("not_a_kite_file.go", "").getVirtualFile()));
        }
    }
}