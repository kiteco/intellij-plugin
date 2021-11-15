package com.kite.intellij.backend.json;

import com.kite.intellij.backend.model.Detail;
import com.kite.intellij.backend.model.DetailType;
import com.kite.intellij.backend.model.KiteFileStatus;
import com.kite.intellij.backend.model.KiteFileStatusResponse;
import com.kite.intellij.backend.model.KiteServiceNotification;
import com.kite.intellij.backend.model.ModuleDetails;
import com.kite.intellij.backend.model.SymbolExt;
import com.kite.intellij.backend.model.UserInfo;
import com.kite.intellij.backend.response.MembersResponse;
import com.kite.intellij.backend.response.SymbolReportResponse;
import com.kite.intellij.backend.response.ValueReportResponse;
import com.kite.intellij.test.KiteTestUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class KiteJsonParsingTest {
    @Test
    public void testMembersResponse() throws Exception {
        MembersResponse response = new KiteJsonParsing().parseMembersResponse(KiteTestUtils.loadTestDataFile("model/json/members/simpleResponse.json"));
        Assert.assertNotNull(response);

        Assert.assertEquals(1, response.size());

        SymbolExt firstSymbol = response.getMembers()[0];
        Assert.assertNotNull(firstSymbol);

        Assert.assertNotNull(firstSymbol.getValues());
        Assert.assertNotNull(firstSymbol.getValues());
        Assert.assertEquals(1, firstSymbol.getValues().getValues().length);

        Detail detail = firstSymbol.getValues().getValues()[0].getDetail();
        Assert.assertNotNull(detail);
        Assert.assertTrue(detail instanceof ModuleDetails);
        Assert.assertEquals(DetailType.Module, detail.getType());
        Assert.assertEquals(2, ((ModuleDetails) detail).getTotalMembers());
        Assert.assertEquals(1, ((ModuleDetails) detail).getMembers().length);
        Assert.assertEquals("requests.models.Response", ((ModuleDetails) detail).getMembers()[0].getId().getValue());
        Assert.assertEquals("Response", ((ModuleDetails) detail).getMembers()[0].getName());
    }

    @Test
    public void testValueReportResponse() throws Exception {
        ValueReportResponse response = new KiteJsonParsing().parseValueReportResponse(KiteTestUtils.loadTestDataFile("model/json/valueReport/simpleResponse.json"));
        Assert.assertNotNull(response);

        Assert.assertNotNull(response.getReport());
        Assert.assertNotNull(response.getValue());

        Assert.assertEquals("__builtin__", response.getValue().getId().getValue());
        Assert.assertEquals(141, ((ModuleDetails) response.getValue().getDetail()).getTotalMembers());
        Assert.assertEquals(141, ((ModuleDetails) response.getValue().getDetail()).getMembers().length);

        Assert.assertEquals("A reference to the module containing builtins, and its name as a string.", response.getReport().getDescriptionText());
    }

    @Test
    public void testSymbolReportResponse() throws Exception {
        SymbolReportResponse response = new KiteJsonParsing().parseSymbolReportResponse(KiteTestUtils.loadTestDataFile("model/json/symbolReport/simpleResponse.json"));
        Assert.assertNotNull(response);

        Assert.assertNotNull(response.getReport());
        Assert.assertNotNull(response.getSymbol());

        Assert.assertEquals("__builtin__", response.getSymbol().getName());
        Assert.assertEquals("__builtin__", response.getSymbol().getFirstValue().getId().getValue());
        Assert.assertEquals(141, ((ModuleDetails) response.getSymbol().getFirstValue().getDetail()).getTotalMembers());
        Assert.assertEquals(141, ((ModuleDetails) response.getSymbol().getFirstValue().getDetail()).getMembers().length);

        Assert.assertEquals("A reference to the module containing builtins, and its name as a string.", response.getReport().getDescriptionText());
    }

    @Test
    public void testUserInfo() throws Exception {
        UserInfo user = new KiteJsonParsing().parseUserInfo(KiteTestUtils.loadTestDataFile("model/json/userInfo/userInfo.json"));
        Assert.assertNotNull(user);

        Assert.assertEquals("42", user.getId());
        Assert.assertEquals("Firstname Lastname", user.getName());
        Assert.assertEquals("mail@example.com", user.getEmail());
        Assert.assertEquals(null, user.getBio());
        Assert.assertEquals(true, user.isInternal());
        Assert.assertEquals(true, user.isEmailVerified());
        Assert.assertEquals(false, user.isUnsubscribed());
    }

    @Test
    public void testFileStatus() throws Exception {
        KiteFileStatusResponse ready = new KiteJsonParsing().parseFileStatus(KiteTestUtils.loadTestDataFile("model/json/fileStatus/ready.json"));
        Assert.assertEquals(KiteFileStatus.Ready, ready.getStatus());

        KiteFileStatusResponse indexing = new KiteJsonParsing().parseFileStatus(KiteTestUtils.loadTestDataFile("model/json/fileStatus/indexing.json"));
        Assert.assertEquals(KiteFileStatus.Indexing, indexing.getStatus());

        KiteFileStatusResponse syncing = new KiteJsonParsing().parseFileStatus(KiteTestUtils.loadTestDataFile("model/json/fileStatus/noIndex.json"));
        Assert.assertEquals(KiteFileStatus.NoIndex, syncing.getStatus());
    }

    @Test
    public void testParseNotification() {
        KiteServiceNotification notification = new KiteJsonParsing().parseKiteNotification("{\"notification\": {\n" +
                "  \"title\": \"Kite is locked until tomorrow\",\n" +
                "  \"body\": \"Kite Free gives you 42 completions per day to use.\"," +
                "  \"buttons\": [\n" +
                "    {\n" +
                "      \"text\": \"Upgrade to Pro\",\n" +
                "      \"action\": \"open\",\n" +
                "      \"link\": \"https://www.kite.com/pro\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"text\": \"Close\",\n" +
                "      \"action\": \"dismiss\"\n" +
                "    }\n" +
                "  ]\n" +
                "}}");

        Assert.assertNotNull(notification);
        Assert.assertEquals("Kite is locked until tomorrow", notification.title);
        Assert.assertEquals("Kite Free gives you 42 completions per day to use.", notification.body);
        Assert.assertNotNull(notification.body);
        Assert.assertEquals(2, notification.buttons.length);

        Assert.assertEquals("Upgrade to Pro", notification.buttons[0].text);
        Assert.assertTrue(notification.buttons[0].isOpenAction());

        Assert.assertEquals("Close", notification.buttons[1].text);
        Assert.assertTrue(notification.buttons[1].isDismissAction());
    }

    @Test
    public void testParseReason() {
        String json = "{\"reason\": \"the reason message\", \"other_key\": 123}";
        Assert.assertEquals("the reason message", new KiteJsonParsing().parseReason(json));
    }
}