package com.kite.intellij.backend.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kite.intellij.backend.json.deserializer.*;
import com.kite.intellij.backend.json.deserializer.base.*;
import com.kite.intellij.backend.json.deserializer.endpoint.LicenseInfoDeserializer;
import com.kite.intellij.backend.json.deserializer.endpoint.ReportDeserializer;
import com.kite.intellij.backend.json.deserializer.python.PythonFunctionDetailsDeserializer;
import com.kite.intellij.backend.json.deserializer.python.PythonParameterDeserializer;
import com.kite.intellij.backend.json.deserializer.python.PythonSignatureDeserializer;
import com.kite.intellij.backend.json.deserializer.python.PythonTypeDetailsDeserializer;
import com.kite.intellij.backend.model.*;
import com.kite.intellij.backend.response.*;
import com.kite.intellij.lang.KiteLanguage;

/**
 * A factory class which creates the gson instances to deserialize the Json data of Kite.
 *
  */
public class KiteGsonFactory {
    public static Gson createPython() {
        GsonBuilder builder = baseBuilder();

        builder.registerTypeAdapter(Parameter.class, new PythonParameterDeserializer());
        builder.registerTypeAdapter(Signature.class, new PythonSignatureDeserializer());
        builder.registerTypeAdapter(FunctionDetails.class, new PythonFunctionDetailsDeserializer());
        builder.registerTypeAdapter(TypeDetails.class, new PythonTypeDetailsDeserializer());

        return builder.create();
    }

    private static GsonBuilder baseBuilder() {
        GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();

        builder.registerTypeAdapter(KiteLanguage.class, new LanguageDeserializer());

        builder.registerTypeAdapter(Id.class, new IDDeserializer());
        builder.registerTypeAdapter(ModuleDetails.class, new ModuleDetailsDeserializer());
        builder.registerTypeAdapter(InstanceDetails.class, new InstanceDetailsDeserializer());
        builder.registerTypeAdapter(FunctionDetailsBase.class, new FunctionDetailsBaseDeserializer());
        builder.registerTypeAdapter(TypeDetailsBase.class, new TypeDetailsBaseDeserializer());

        builder.registerTypeAdapter(Value.class, new ValueDeserializer());
        builder.registerTypeAdapter(ValueExt.class, new ValueExtDeserializer());

        builder.registerTypeAdapter(Symbol.class, new SymbolDeserializer());
        builder.registerTypeAdapter(SymbolExt.class, new SymbolExtDeserializer());

        builder.registerTypeAdapter(Union.class, new UnionDeserializer());
        builder.registerTypeAdapter(UnionExt.class, new UnionExtDeserializer());

        builder.registerTypeAdapter(ParameterBase.class, new ParameterBaseDeserializer());

        builder.registerTypeAdapter(UserInfo.class, new UserInfoDeserializer());
        builder.registerTypeAdapter(LicenseInfo.class, new LicenseInfoDeserializer());
        builder.registerTypeAdapter(Location.class, new LocationDeserializer());
        builder.registerTypeAdapter(Example.class, new ExampleDeserializer());
        builder.registerTypeAdapter(Usage.class, new UsageDeserializer());
        builder.registerTypeAdapter(Token.class, new TokenDeserializer());
        builder.registerTypeAdapter(Report.class, new ReportDeserializer());

        builder.registerTypeAdapter(ParameterTypeExample.class, new ParameterTypeExampleDeserializer());
        builder.registerTypeAdapter(ParameterExample.class, new ParameterExampleDeserializer());
        builder.registerTypeAdapter(SignatureBase.class, new SignatureBaseDeserializer());
        builder.registerTypeAdapter(Call.class, new CallDeserializer());
        builder.registerTypeAdapter(Calls.class, new CallsDeserializer());

        builder.registerTypeAdapter(HoverResponse.class, new HoverResponseDeserializer());
        builder.registerTypeAdapter(ValueReportResponse.class, new ValueReportResponseDeserializer());
        builder.registerTypeAdapter(SymbolReportResponse.class, new SymbolReportResponseDeserializer());
        builder.registerTypeAdapter(MembersResponse.class, new MemberResponseDeserializer());

        builder.registerTypeAdapter(CompletionRange.class, new CompletionRangeDeserializer());
        builder.registerTypeAdapter(CompletionSnippet.class, new CompletionSnippetDeserializer());
        builder.registerTypeAdapter(KiteCompletion.class, new CompletionSuggestionDeserializer());
        builder.registerTypeAdapter(KiteCompletions.class, new CompletionResponseDeserializer());

        builder.registerTypeAdapter(KiteFileStatus.class, new FileStatusDeserializer());

        return builder;
    }
}
