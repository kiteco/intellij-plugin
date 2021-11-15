package com.kite.intellij.action.signatureInfo;

import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.util.ThrowableRunnable;
import com.kite.intellij.action.KiteActionOverrideLifecycleListener;
import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class KiteSignaturePopupManagerTest extends KiteLightFixtureTest {
    @Test
    public void testFallback() throws Throwable {
        EdtTestUtil.runInEdtAndWait((ThrowableRunnable<Throwable>) () -> {
            myFixture.configureByFile("fallback/test.java");
            Assert.assertEquals("No popup must be shown before the action", 0, MockKiteSignaturePopupManager.getCallCount());

            myFixture.testAction(new KiteSignatureInfoAction());
            Assert.assertEquals("The signature info panel must not be shown for Java files", 0, MockKiteSignaturePopupManager.getCallCount());
        });
    }

    @Test
    public void testSignaturePopup() throws Throwable {
        EdtTestUtil.runInEdtAndWait((ThrowableRunnable<Throwable>) () -> {
            myFixture.configureByFile("signaturePopup/test.py");

            Assert.assertEquals("No popup must be shown before the action", 0, MockKiteSignaturePopupManager.getCallCount());
            myFixture.testAction(new KiteSignatureInfoAction());

            Assert.assertEquals("The signature info panel must be shown for Python files", 1, MockKiteSignaturePopupManager.getCallCount());
        });
    }

    @Test
    public void testSignaturePopupNextParam() throws Throwable {
        EdtTestUtil.runInEdtAndWait((ThrowableRunnable<Throwable>) () -> {

            //the signature info request returns a call with 2 parameters for all calls inside this test
            MockKiteApiService.getInstance().enableHttpCalls();
            MockKiteHttpConnection.getInstance().addPostPathHandler("/clientapi/editor/signatures", (path, payload) -> loadFile("signaturePopup/signatures.json"), getTestRootDisposable());

            myFixture.configureByFile("signaturePopup/test.py");

            myFixture.testAction(new KiteSignatureInfoAction());
            Assert.assertEquals("The signature info panel must be shown for Python files", 1, MockKiteSignaturePopupManager.getCallCount());

            //now we invoce the "next param" editor action
            SignaturePopupController controller = SignatureInfoEditorTracker.currentlyVisibleController(myFixture.getEditor());
            Assert.assertNotNull("there must be a visible controller after the popup was invoked", controller);
            Assert.assertEquals(0, controller.getActiveParameterIndex());

            goToNextParameter();
            Assert.assertEquals(1, controller.getActiveParameterIndex());

            //back to the first parameter
            goToPrevParameter();
            Assert.assertEquals(0, controller.getActiveParameterIndex());

            //lower param range check
            goToPrevParameter();
            goToPrevParameter();
            Assert.assertEquals(0, controller.getActiveParameterIndex());

            //upper limit check
            goToNextParameter();
            goToNextParameter();
            goToNextParameter();
            Assert.assertEquals("There are only 2 parameters in the callee, the index must not be larger than the last param", 1, controller.getActiveParameterIndex());
        });
    }

    @Test
    public void testSignaturePopupNextParamVararg() throws Throwable {
        EdtTestUtil.runInEdtAndWait((ThrowableRunnable<Throwable>) () -> {
            //the signature info request returns a call with 2 parameters for all calls inside this test
            MockKiteApiService.getInstance().enableHttpCalls();
            MockKiteHttpConnection.getInstance().addPostPathHandler("/clientapi/editor/signatures", (path, payload) -> loadFile("signaturePopup/signatures_vararg.json"), getTestRootDisposable());

            myFixture.configureByFile("signaturePopup/test_vararg.py");

            myFixture.testAction(new KiteSignatureInfoAction());
            Assert.assertEquals("The signature info panel must be shown for Python files", 1, MockKiteSignaturePopupManager.getCallCount());

            //now we invoke the "next param" editor action
            SignaturePopupController controller = SignatureInfoEditorTracker.currentlyVisibleController(myFixture.getEditor());
            Assert.assertNotNull("there must be a visible controller after the popup was invoked", controller);
            //regular arg
            Assert.assertEquals(0, controller.getActiveParameterIndex());

            //vararg
            goToNextParameter();
            Assert.assertEquals(1, controller.getActiveParameterIndex());

            //back to the first parameter
            goToPrevParameter();
            Assert.assertEquals(0, controller.getActiveParameterIndex());

            //lower param range check
            goToPrevParameter();
            goToPrevParameter();
            Assert.assertEquals(0, controller.getActiveParameterIndex());

            //upper limit check
            goToNextParameter();
            goToNextParameter();
            goToNextParameter();
            Assert.assertEquals("There are only 2 parameters in the callee, the index must not be larger than the last param", 1, controller.getActiveParameterIndex());
        });
    }

    @Test
    @Ignore("tab doesn't invoke editor actions at the moment")
    public void _testTabToNext() {
        //the signature info request returns a call with 2 parameters for all calls inside this test
        MockKiteApiService.getInstance().enableHttpCalls();
        MockKiteHttpConnection.getInstance().addPostPathHandler("/clientapi/editor/signatures", (path, payload) -> loadFile("signaturePopup/signatures.json"), getTestRootDisposable());

        myFixture.configureByFile("signaturePopup/test.py");
        myFixture.testAction(new KiteSignatureInfoAction());
        Assert.assertEquals("The signature info panel must be shown for Python files", 1, MockKiteSignaturePopupManager.getCallCount());

        //now we invoce the "next param" editor action
        SignaturePopupController controller = SignatureInfoEditorTracker.currentlyVisibleController(myFixture.getEditor());
        Assert.assertNotNull("there must be a visible controller after the popup was invoked", controller);
        Assert.assertEquals(0, controller.getActiveParameterIndex());

        myFixture.type("\t"); //TAB selects the next parameter
        Assert.assertEquals(1, controller.getActiveParameterIndex());
    }

    @Override
    protected String getBasePath() {
        return "editor/signaturePopupManager";
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    //@Override // unavailable in super in 2020.3+
    protected void invokeTestRunnable(@NotNull Runnable runnable) throws Exception {
        runnable.run();
    }

    @Override
    protected boolean runInDispatchThread() {
        return false;
    }

    private static void goToPreviousParameter(CodeInsightTestFixture myFixture, String actionId) {
        myFixture.performEditorAction(actionId);
    }

    private void goToNextParameter() {
        myFixture.performEditorAction(KiteActionOverrideLifecycleListener.INTELLIJ_NEXT_PARAMETER_ACTION_ID);
    }

    private void goToPrevParameter() {
        goToPreviousParameter(myFixture, KiteActionOverrideLifecycleListener.INTELLIJ_PREVIOUS_PARAMETER_ACTION_ID);
    }
}
