package com.kite.intellij.action.signatureInfo;

import com.intellij.openapi.editor.Editor;
import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.editor.events.TestcaseEditorEventListener;
import com.kite.intellij.test.KiteLightFixtureTest;

public class SignatureTypedHandlerTest extends KiteLightFixtureTest {
    public void testTyping() throws InterruptedException {
        myFixture.configureByText("file.py", "");
        Editor editor = myFixture.getEditor();

        typeAndWait("a");
        typeAndWait("b");
        typeAndWait("(");
        assertEquals((Character) '(', SignatureTypedHandler.getLastTypedCharacter(editor, true));
        assertNull("Expected null character after caret moved and without look behind", SignatureTypedHandler.getLastTypedCharacter(editor, false));

        editor.getCaretModel().moveToOffset(0);
        assertNull(SignatureTypedHandler.getLastTypedCharacter(editor, false));
    }

    public void testUnsupportedFile() throws InterruptedException {
        myFixture.configureByText("file.txt", "");
        Editor editor = myFixture.getEditor();

        typeAndWait("a");
        typeAndWait("(");
        assertNull(SignatureTypedHandler.getLastTypedCharacter(editor, false));
    }

    public void testErrorSignatureResponse() throws InterruptedException {
        MockKiteApiService service = MockKiteApiService.getInstance();
        service.enableHttpCalls();
        MockKiteHttpConnection.getInstance().addPostPathHandler("/clientapi/editor/signatures", (path, payload) -> {
            throw new HttpStatusException("invalid signature response", 400, null);
        }, getTestRootDisposable());

        myFixture.configureByText("file.py", "");
        typeAndWait("print(a=");
    }

    protected void typeAndWait(String a) throws InterruptedException {
        myFixture.type(a);
        TestcaseEditorEventListener.sleepForQueueWork(getProject());
    }
}