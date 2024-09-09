package com.alibaba.dcm.agent;

import com.alibaba.dcm.DnsCache;
import com.alibaba.dcm.DnsCacheEntry;
import com.alibaba.dcm.DnsCacheManipulator;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * DCM agent.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see Instrumentation
 * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.instrument/java/lang/instrument/package-summary.html">The mechanism for instrumentation</a>
 * @see <a href="https://docs.oracle.com/javase/10/docs/specs/jar/jar.html#jar-manifest">JAR File Specification - JAR Manifest</a>
 * @see <a href="https://docs.oracle.com/javase/tutorial/deployment/jar/manifestindex.html">Working with Manifest Files - The Java™ Tutorials</a>
 * @since 1.4.0
 */
public class DcmAgent {
    private static final Logger logger = Logger.getLogger(DcmAgent.class.getName());

    private static final String FILE_KEY = "file";

    private static final String DCM_AGENT_SUPPRESS_EXCEPTION_STACK = "DCM_AGENT_SUPPRESS_EXCEPTION_STACK";

    static final String DCM_AGENT_SUCCESS_MARK_LINE = "!!DCM SUCCESS!!";

    /**
     * Entrance method of DCM Java Agent when used through a jvm command line option.
     */
    public static void premain(@Nonnull String agentArgument) throws Exception {
      agentmain(agentArgument);
    }

    /**
     * Entrance method of DCM Java Agent when connecting to a running jvm.
     */
    @SuppressFBWarnings("THROWS_METHOD_THROWS_CLAUSE_BASIC_EXCEPTION")
    public static void agentmain(@Nonnull String agentArgument) throws Exception {
        logger.info(format("%s: attached with agent argument: %s.%n", DcmAgent.class.getName(), agentArgument));

        agentArgument = agentArgument.trim();
        if (agentArgument.isEmpty()) {
            logger.info(DcmAgent.class.getName() + ": agent argument is blank, do nothing!");
            return;
        }

        initAction2Method();

        PrintWriter filePrinter = null;
        try {
            final Map<String, List<String>> action2Arguments = parseAgentArgument(agentArgument);

            // Extract file argument, set file printer if needed
            filePrinter = getFilePrintWriter(action2Arguments.remove(FILE_KEY));

            if (action2Arguments.isEmpty()) {
                logger.info(DcmAgent.class.getName() + ": No action in agent argument, do nothing!");
                if (filePrinter != null) {
                    filePrinter.printf("No action in agent argument, do nothing! agent argument: %s.%n", agentArgument);
                }
                return;
            }

            boolean allSuccess = true;
            for (Map.Entry<String, List<String>> entry : action2Arguments.entrySet()) {
                final String action = entry.getKey();
                final List<String> arguments = entry.getValue();

                boolean success = doAction(action, arguments, filePrinter);
                if (!success) allSuccess = false;
            }

            if (allSuccess && filePrinter != null) {
                filePrinter.println(DCM_AGENT_SUCCESS_MARK_LINE);
            }
        } finally {
            if (filePrinter != null) {
                try {
                    filePrinter.close();
                } catch (Exception e) {
                    // do nothing!
                }
            }
        }
    }

    @Nonnull
    private static Map<String, List<String>> parseAgentArgument(@Nonnull String argument) {
        final String[] split = argument.split("\\s+");

        int idx = 0;
        Map<String, List<String>> action2Arguments = new HashMap<>();
        while (idx < split.length) {
            final String action = split[idx++];
            if (!action2Method.containsKey(action)) {
                continue; // TODO error message
            }

            List<String> arguments = new ArrayList<>();
            while (idx < split.length) {
                if (action2Method.containsKey(split[idx])) {
                    break;
                }
                arguments.add(split[idx++]);
            }
            action2Arguments.put(action, arguments);
        }

        return action2Arguments;
    }

    @Nullable
    private static PrintWriter getFilePrintWriter(@Nullable List<String> files) throws FileNotFoundException {
        if (null == files) return null;

        FileOutputStream fileOutputStream = new FileOutputStream(files.get(0), false);
        final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, UTF_8);

        return new PrintWriter(outputStreamWriter, true);
    }

    private static boolean doAction(final String action, final List<String> arguments, final PrintWriter filePrinter) {
        final String argumentString = join(arguments);

        if (!action2Method.containsKey(action)) {
            logger.info(format(("%s: Unknown action %s, ignore! action: %<s %s!%n"), DcmAgent.class.getName(), action, argumentString));
            if (filePrinter != null) {
                filePrinter.printf("Unknown action %s, ignore! action: %<s %s !%n", action, argumentString);
            }
            return false;
        }

        try {
            final Object result = invokeAction(action, arguments.toArray(new String[0]));
            printActionResult(action, result, filePrinter);
            return true;
        } catch (Exception e) {
            final String exString = throwable2StackString(e);
            final String sdtoutExString;
            if (isDcmAgentSuppressExceptionStack()) {
                sdtoutExString = e.toString();
            } else {
                sdtoutExString = exString;
            }

            logger.info(format(("%s: Error to do action %s %s, cause: %s%n"), DcmAgent.class.getName(), action, argumentString, sdtoutExString));
            if (filePrinter != null) {
                filePrinter.printf("Error to do action %s %s, cause: %s%n", action, argumentString, exString);
            }

            return false;
        }
    }

    private static boolean isDcmAgentSuppressExceptionStack() {
        String suppressException = getConfig(DCM_AGENT_SUPPRESS_EXCEPTION_STACK);
        if (suppressException == null) return false;

        suppressException = suppressException.trim();
        if (suppressException.isEmpty()) return false;

        return "true".equalsIgnoreCase(suppressException);
    }

    private static Object invokeAction(String action, String[] arguments) throws InvocationTargetException, IllegalAccessException {
        Method method = action2Method.get(action);

        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Object[] methodArgs = convertStringArray2Arguments(action, arguments, parameterTypes);
        return method.invoke(null, methodArgs);
    }

    private static Object[] convertStringArray2Arguments(String action, String[] arguments, Class<?>[] parameterTypes) {
        if (arguments.length < parameterTypes.length) {
            final String message = format("Action %s need more argument! arguments: %s", action, Arrays.toString(arguments));
            throw new IllegalStateException(message);
        }
        if (parameterTypes.length == 0) return new Object[0];

        final Object[] methodArgs = new Object[parameterTypes.length];

        final int lastArgumentIdx = parameterTypes.length - 1;
        if (parameterTypes[(lastArgumentIdx)] == String[].class) {
            // set all tail method argument of type String[]
            String[] varArgs = new String[arguments.length - lastArgumentIdx];
            System.arraycopy(arguments, lastArgumentIdx, varArgs, 0, varArgs.length);
            methodArgs[(lastArgumentIdx)] = varArgs;
        } else if (arguments.length > parameterTypes.length) {
            String message = format("Too many arguments for action %s! arguments: %s", action, Arrays.toString(arguments));
            throw new IllegalStateException(message);
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            // already set
            if (methodArgs[i] != null) continue;

            Class<?> parameterType = parameterTypes[i];
            final String argument = arguments[i];
            if (parameterType.equals(String.class)) {
                methodArgs[i] = argument;
            } else if (parameterType.equals(int.class)) {
                methodArgs[i] = Integer.parseInt(argument);
            } else {
                final String message = format("Unexpected method type %s! Misused or Bug!!", parameterType.getName());
                throw new IllegalStateException(message);
            }
        }

        return methodArgs;
    }

    private static void printActionResult(String action, Object result, PrintWriter writer) {
        if (writer == null) return;

        final Method method = action2Method.get(action);
        if (method.getReturnType() == void.class) return;

        if (result == null) {
            writer.println((Object) null);
        } else if (result instanceof DnsCacheEntry) {
            printDnsCacheEntry((DnsCacheEntry) result, writer);
        } else if (result instanceof DnsCache) {
            DnsCache dnsCache = (DnsCache) result;

            printDnsCacheEntryList("Dns cache: ", dnsCache.getCache(), writer);

            writer.println();
            printDnsCacheEntryList("Dns negative cache: ", dnsCache.getNegativeCache(), writer);
        } else {
            writer.println(result);
        }
    }

    private static void printDnsCacheEntryList(String msg, List<DnsCacheEntry> dnsCacheEntries, PrintWriter writer) {
        writer.println(msg);
        for (DnsCacheEntry entry : dnsCacheEntries) {
            printDnsCacheEntry(entry, writer);
        }
    }

    private static void printDnsCacheEntry(DnsCacheEntry entry, PrintWriter writer) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        writer.printf("    %s %s %s%n", entry.getHost(), join(Arrays.asList(entry.getIps()), ","), dateFormat.format(entry.getExpiration()));
    }

    private static volatile Map<String, Method> action2Method;
    private static volatile ArrayList<String> actionList;

    private static synchronized void initAction2Method() throws NoSuchMethodException {
        if (action2Method != null) return;

        Map<String, Method> map = new LinkedHashMap<>();
        map.put("set", DnsCacheManipulator.class.getMethod("setDnsCache", String.class, String[].class));
        map.put("get", DnsCacheManipulator.class.getMethod("getDnsCache", String.class));
        map.put("rm", DnsCacheManipulator.class.getMethod("removeDnsCache", String.class));

        map.put("list", DnsCacheManipulator.class.getMethod("getWholeDnsCache"));
        map.put("ls", DnsCacheManipulator.class.getMethod("getWholeDnsCache"));
        map.put("clear", DnsCacheManipulator.class.getMethod("clearDnsCache"));

        map.put("load", DnsCacheManipulator.class.getMethod("loadDnsCacheConfigFromFileSystem", String.class));

        map.put("setPolicy", DnsCacheManipulator.class.getMethod("setDnsCachePolicy", int.class));
        map.put("getPolicy", DnsCacheManipulator.class.getMethod("getDnsCachePolicy"));
        map.put("setNegativePolicy", DnsCacheManipulator.class.getMethod("setDnsNegativeCachePolicy", int.class));
        map.put("getNegativePolicy", DnsCacheManipulator.class.getMethod("getDnsNegativeCachePolicy"));

        actionList = new ArrayList<>(map.keySet());

        map.put(FILE_KEY, null); // FAKE KEY

        action2Method = map;
    }

    /**
     * the action list for DCM agent.
     *
     * @since 1.6.0
     */
    @SuppressWarnings("unchecked")
    @SuppressFBWarnings("THROWS_METHOD_THROWS_RUNTIMEEXCEPTION")
    public static List<String> getActionList() {
        try {
            initAction2Method();

            return (List<String>) actionList.clone();
        } catch (Exception e) {
            throw new RuntimeException("fail to getActionList, cause: " + e, e);
        }
    }

    ///////////////////////////////////////////////
    // util methods
    ///////////////////////////////////////////////

    @Nullable
    private static String getConfig(@Nonnull String name) {
        String var = System.getenv(name);
        if (var == null || var.trim().isEmpty()) {
            var = System.getProperty(name);
        }
        return var;
    }

    @Nonnull
    private static String join(@Nonnull List<String> list) {
        return join(list, " ");
    }

    @Nonnull
    private static String join(@Nonnull List<String> list, @Nonnull String separator) {
        StringBuilder ret = new StringBuilder();
        for (String argument : list) {
            if (ret.length() > 0) {
                ret.append(separator);
            }
            ret.append(argument);
        }
        return ret.toString();
    }

    @Nonnull
    private static String throwable2StackString(@Nonnull Throwable e) {
        final StringWriter w = new StringWriter();
        e.printStackTrace(new PrintWriter(w, true));
        return w.toString();
    }
}
