package com.kite.pebble;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.ColorUtil;
import com.intellij.util.PathUtilRt;
import com.kite.intellij.backend.WebappLinks;
import com.kite.intellij.backend.model.*;
import com.kite.intellij.lang.documentation.LinksHandlers;
import com.kite.intellij.lang.documentation.RenderStyle;
import com.kite.intellij.lang.documentation.linkHandler.*;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.extension.Function;
import com.mitchellbosecke.pebble.extension.Test;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;

/**
 * Pebble extension which adds the filters
 * kiteExternalLink: Links to the external documentation of the Kite web
 * kiteInternalLink: Links to the internal documentation link handler to show the documentation for a string, id, symbol or value
 * kiteMembersLink: Links to the internal list of members for a given string, id, value or symbol
 * <p>
 * The filter takes a string, an {@link Id}, a {@link Symbol} or a {@link SymbolExt} and will return an url to the kite service.
 * The links includes the given id.
 *
  */
public class KitePebbleExtension extends AbstractExtension {
    private static final Logger LOG = Logger.getInstance("#kite.pebble.extension");

    @Override
    public Map<String, Filter> getFilters() {
        Map<String, Filter> filters = Maps.newLinkedHashMap();

        filters.put("kiteLink", new GenericKiteLinkFilter());
        filters.put("kiteExternalLink", new OpenInKiteFilter());
        filters.put("kiteLinksLink", new ShowLinksFilter());
        filters.put("kiteFilename", new FilenameFilter());
        filters.put("kiteSignatureInfoLink", new SignatureInfoLink());
        filters.put("kiteCopilotDocsLink", new CopilotDocsLink());
        filters.put("kiteClasspathUrl", new ClasspathImageLink());
        filters.put("kiteFileUrl", new FileUrlFilter());
        filters.put("kiteColor", new KiteColorFilter());

        return filters;
    }

    @Override
    public Map<String, Test> getTests() {
        Map<String, Test> tests = Maps.newLinkedHashMap();
        tests.put("validId", new IsValidIdTest());
        tests.put("empty", new KiteEmptyTest());
        return tests;
    }

    @Override
    public Map<String, Function> getFunctions() {
        Map<String, Function> functions = Maps.newLinkedHashMap();

        functions.put("kiteInviteLink", new StaticStringFunction(WebappLinks.getInstance().redirectUrl(WebappLinks.RedirectPath.Invite)));
        functions.put("roundNearest", new RoundNearestFunction());

        return functions;
    }

    @NotNull
    private static Color emulateAlpha(@NotNull Color c, @NotNull Color background, double alpha) {
        alpha = Math.min(1, Math.max(0, alpha));
        //noinspection UseJBColor
        return new Color(
                (int) (c.getRed() * alpha + background.getRed() * (1 - alpha)),
                (int) (c.getGreen() * alpha + background.getGreen() * (1 - alpha)),
                (int) (c.getBlue() * alpha + background.getBlue() * (1 - alpha)),
                0);
    }

    private static class GenericKiteLinkFilter implements Filter {
        @Override
        public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            if (input == null) {
                return null;
            }

            if (!(input instanceof KiteLinkData)) {
                throw new IllegalStateException(String.format("input is not of type KiteLinkData: %s", input));
            }

            return LinksHandlers.asLink((KiteLinkData) input);
        }

        @Override
        public List<String> getArgumentNames() {
            return Collections.emptyList();
        }
    }

    private static class OpenInKiteFilter implements Filter {
        @Override
        public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            if (input == null) {
                return null;
            }

            String id;
            if (input instanceof String) {
                id = (String) input;
            } else if (input instanceof Id) {
                id = ((Id) input).getValue();
            } else if (input instanceof WithId) {
                id = ((WithId) input).getId().getValue();
            } else {
                throw new IllegalStateException("Unsupported element type " + input);
            }

            return LinksHandlers.asLink(new ExternalDocumentationLinkData(id));
        }

        @Override
        public List<String> getArgumentNames() {
            return null;
        }
    }

    private static class IsValidIdTest implements Test {
        @Override
        public boolean apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            if (input instanceof String && !((String) input).isEmpty()) {
                return true;
            }

            if (input instanceof Id && !((Id) input).getValue().isEmpty()) {
                return true;
            }

            return input instanceof WithId && ((WithId) input).getId().length() > 0;
        }

        @Override
        public List<String> getArgumentNames() {
            return null;
        }
    }

    private static class StaticStringFunction implements Function {
        private final String url;

        private StaticStringFunction(String stringValue) {
            this.url = stringValue;
        }

        @Override
        public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            return url;
        }

        @Override
        public List<String> getArgumentNames() {
            return null;
        }
    }

    /**
     * Takes a float or double and returns the nearest integer.
     */
    private static class RoundNearestFunction implements Function {
        private RoundNearestFunction() {
        }

        @Override
        public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            if (args.containsKey("0")) {
                Object candidate = args.get("0");
                if (candidate instanceof Float) {
                    return Math.round((Float) candidate);
                }
                if (candidate instanceof Double) {
                    return Math.round((Double) candidate);
                }
            }
            return null;
        }

        @Override
        public List<String> getArgumentNames() {
            return null;
        }
    }

    private static class ShowLinksFilter implements Filter {
        @Override
        public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            String id = null;
            String label = null;

            if (input instanceof Symbol) {
                id = ((Symbol) input).getId().getValue();
                label = ((Symbol) input).getName();

                input = ((Symbol) input).getFirstValue();
            } else if (input instanceof SymbolExt) {
                id = ((SymbolExt) input).getId().getValue();
                label = ((SymbolExt) input).getName();

                input = ((SymbolExt) input).getFirstValue();
            }

            if (id == null) {
                if (input instanceof Value) {
                    id = ((Value) input).getId().getValue();
                    label = ((Value) input).getRepresentation();
                } else {
                    throw new IllegalStateException("Unsupported input " + input);
                }
            }

            return LinksHandlers.asLink(new LinksLinkData(id, label, (Integer) args.getOrDefault("length", 25)));
        }

        @Override
        public List<String> getArgumentNames() {
            return Lists.newArrayList("length");
        }
    }

    private static class FilenameFilter implements Filter {
        @Override
        public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            if (input instanceof String) {
                String path = (String) input;

                return PathUtilRt.getFileName(path);
            }

            return input;
        }

        @Override
        public List<String> getArgumentNames() {
            return null;
        }
    }

    private static class SignatureInfoLink implements Filter {
        @Override
        public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            SignatureLinkData in = input instanceof SignatureLinkData ? (SignatureLinkData) input : null;

            Object value = args.get("argIndex");
            OptionalInt argIndex = value instanceof Integer ? OptionalInt.of((Integer) value) : OptionalInt.empty();

            boolean inKwargs = Boolean.TRUE.equals(args.getOrDefault("inKwargs", in != null ? in.isInKwargs() : null));
            boolean expandKwargs = Boolean.TRUE.equals(args.getOrDefault("expandKwargs", in != null ? in.isExpandKwargs() : null));
            boolean expandPopuplarPatterns = Boolean.TRUE.equals(args.getOrDefault("expandPopularPatterns", in != null ? in.isExpandPopularPatterns() : null));

            return LinksHandlers.asLink(new SignatureLinkData(argIndex, inKwargs, expandKwargs, expandPopuplarPatterns, false));
        }

        @Override
        public List<String> getArgumentNames() {
            return Lists.newArrayList("expandPopularPatterns", "argIndex", "inKwargs", "expandKwargs");
        }
    }

    private static class CopilotDocsLink implements Filter {
        @Override
        public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            if (input instanceof WithId) {
                // don't escape the ID as kited isn't able to handle escaped chars in its kite:// urls
                return "kite://docs/" + ((WithId) input).getId().getValue();
            }
            return "";
        }

        @Override
        public List<String> getArgumentNames() {
            return Collections.emptyList();
        }
    }

    private static class ClasspathImageLink implements Filter {
        @Override
        public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            String classpathReference = String.valueOf(input);
            try {
                Application application = ApplicationManager.getApplication();
                if (application == null || application.isUnitTestMode()) {
                    //let it work without an application to simplify the test cases, in PyCharm/IntelliJ an application is always available
                    return input;
                }

                return KitePebbleExtension.class.getClassLoader().getResource(classpathReference);
            } catch (Exception e) {
                LOG.warn("Error referencing classpath element " + input, e);
                return input;
            }
        }

        @Override
        public List<String> getArgumentNames() {
            return null;
        }
    }

    private static class KiteHexColorFilter implements Filter {
        @Override
        public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            if (input instanceof Color) {
                return "#" + ColorUtil.toHex((Color) input);
            }

            return input;
        }

        @Override
        public List<String> getArgumentNames() {
            return Collections.emptyList();
        }
    }

    /**
     * Adds a color modification to Kite.
     */
    private static class KiteColorFilter implements Filter {
        @Override
        public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            Color color = null;
            if (input instanceof RenderStyle) {
                color = ((RenderStyle) input).getForeground();
            } else if (input instanceof Color) {
                color = (Color) input;
            } else if (input instanceof String && ((String) input).startsWith("#")) {
                color = ColorUtil.fromHex((String) input);
            }

            if (color == null) {
                return input;
            }

            if (args.get("brighter") instanceof Number) {
                color = ColorUtil.brighter(color, ((Number) args.get("brighter")).intValue());
            }

            if (args.get("darker") instanceof Number) {
                color = ColorUtil.darker(color, ((Number) args.get("darker")).intValue());
            }

            Object bg = args.get("bgColor");
            bg = bg == null && input instanceof RenderStyle ? ((RenderStyle) input).getBackground() : bg;
            if (args.get("opacity") instanceof Number && bg instanceof Color) {
                color = emulateAlpha(color, (Color) bg, ((Number) args.get("opacity")).doubleValue());
            }

            return "#" + ColorUtil.toHex(color);
        }

        @Override
        public List<String> getArgumentNames() {
            return Lists.newArrayList("brighter", "darker", "opacity", "bgColor");
        }
    }

    private static class FileUrlFilter implements Filter {
        @Override
        public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            if (input instanceof String) {
                //allow file values in unit test mode
                Application application = ApplicationManager.getApplication();
                if (new File((String) input).exists() || (application == null || application.isUnitTestMode())) {
                    int line = args.get("line") instanceof Number
                            ? ((Number) args.get("line")).intValue()
                            : 1;

                    return LinksHandlers.asLink(new DocumentFileLinkData((String) input, line));
                }
            }

            return null;
        }

        @Override
        public List<String> getArgumentNames() {
            return Lists.newArrayList("line");
        }
    }

    private static class KiteEmptyTest implements Test {
        @Override
        public List<String> getArgumentNames() {
            return null;
        }

        @Override
        public boolean apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            if (input == null) {
                return true;
            }

            if (input instanceof String) {
                String value = (String) input;
                return value.isEmpty() || value.trim().isEmpty();
            }

            if (input instanceof Object[]) {
                Object[] value = (Object[]) input;
                return value.length == 0;
            }

            if (input instanceof Collection) {
                return ((Collection<?>) input).isEmpty();
            }

            if (input instanceof Map) {
                return ((Map<?, ?>) input).isEmpty();
            }

            return false;
        }
    }

}
