package com.alibaba.dcm.agent;

import com.alibaba.dcm.DnsCacheManipulator;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class DcmAgent {
    public static final String FILE = "file";

    public static void agentmain(String agentArgument) throws Exception {
        agentArgument = agentArgument.trim();
        if (agentArgument.isEmpty()) {
            System.out.println(DcmAgent.class.getName() + ": empty agent argument, do nothing!");
            return;
        }

        initAction2Method();

        PrintWriter writer = new PrintWriter(System.out, true);
        FileOutputStream outputStream = null;
        try {
            final Map<String, List<String>> action2Arguments = parseArgument(agentArgument);
            if (action2Arguments.containsKey(FILE)) {
                outputStream = new FileOutputStream(action2Arguments.get(FILE).get(0), true);
                final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");

                writer = new PrintWriter(outputStreamWriter, true);
                action2Arguments.remove(FILE);
            }
            writer.printf("Run %s!\n", DcmAgent.class.getName());
            writer.println("Arguments: " + agentArgument);

            if (action2Arguments.isEmpty()) {
                writer.println(DcmAgent.class.getName() + ": No action in agent argument, do nothing!");
            }

            for (Map.Entry<String, List<String>> entry : action2Arguments.entrySet()) {
                final String action = entry.getKey();
                final List<String> arguments = entry.getValue();
                doAction(action, arguments.toArray(new String[0]), writer);
            }
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    static Map<String, List<String>> parseArgument(String argument) {
        final String[] split = argument.split("\\s+");

        int idx = 0;
        Map<String, List<String>> action2Arguments = new HashMap<String, List<String>>();
        while (idx < split.length) {
            final String action = split[idx++].toLowerCase();
            if (!action2Method.containsKey(action)) {
                continue; // TODO error message
            }

            List<String> arguments = new ArrayList<String>();
            while (idx < split.length) {
                if (action2Method.containsKey(split[idx])) {
                    continue;
                }
                arguments.add(split[idx++]);
            }
            action2Arguments.put(action, arguments);
        }

        return action2Arguments;
    }

    static void doAction(String action, String[] arguments, PrintWriter writer) throws Exception {
        Method method = action2Method.get(action);

        if (method == null) {
            StringBuilder sb = new StringBuilder();
            for (String argument : arguments) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(argument);
            }
            writer.printf("Unknown action %s! ignore %<s %s !\n", action, sb);
        }

        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Object[] methodArgs = convertStringArray2Arguments(action, arguments, parameterTypes);
        final Object ret = method.invoke(null, methodArgs);
        if (ret != null) {
            writer.println(ret);
        }
    }

    static volatile Map<String, Method> action2Method;

    static synchronized void initAction2Method() throws Exception {
        if (action2Method != null) return;

        Map<String, Method> map = new HashMap<String, Method>();
        map.put("set", DnsCacheManipulator.class.getMethod("setDnsCache", String.class, String[].class));
        map.put("get", DnsCacheManipulator.class.getMethod("getDnsCache", String.class));

        map.put("list", DnsCacheManipulator.class.getMethod("getWholeDnsCache"));
        map.put("clear", DnsCacheManipulator.class.getMethod("clearDnsCache"));

        map.put("setPolicy", DnsCacheManipulator.class.getMethod("setDnsCachePolicy", int.class));
        map.put("getPolicy", DnsCacheManipulator.class.getMethod("getDnsCachePolicy"));
        map.put("setNegativePolicy", DnsCacheManipulator.class.getMethod("setDnsNegativeCachePolicy", int.class));
        map.put("getNegativePolicy", DnsCacheManipulator.class.getMethod("getDnsNegativeCachePolicy"));

        map.put(FILE, null);

        action2Method = map;
    }

    static Object[] convertStringArray2Arguments(String action, String[] arguments, Class<?>[] parameterTypes) {
        if (arguments.length < parameterTypes.length) {
            final String message = String.format("action %s need more argument! arguments: %s", action, Arrays.toString(arguments));
            throw new IllegalStateException(message);
        }
        if (parameterTypes.length == 0) {
            return new Object[0];
        }

        final Object[] methodArgs = new Object[parameterTypes.length];

        final int lastArgumentIdx = parameterTypes.length - 1;
        if (parameterTypes[(lastArgumentIdx)] == String[].class) {
            // set all tail method argument of type String[] 
            String[] varArgs = new String[arguments.length - lastArgumentIdx];
            System.arraycopy(arguments, lastArgumentIdx, varArgs, 0, varArgs.length);
            methodArgs[(lastArgumentIdx)] = varArgs;
        } else if (arguments.length > parameterTypes.length) {
            String message = String.format("Too more arguments for Action %s! arguments: %s", action, Arrays.toString(arguments));
            throw new IllegalStateException(message);
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            if (methodArgs[i] != null) { // already set
                continue;
            }

            Class<?> parameterType = parameterTypes[i];
            final String argument = arguments[i];
            if (parameterType.equals(String.class)) {
                methodArgs[i] = argument;
            } else if (parameterType.equals(int.class)) {
                methodArgs[i] = Integer.parseInt(argument);
            } else {
                final String message = String.format("Unexpected method type %s! Bug!!", parameterType.getName());
                throw new IllegalStateException(message);
            }
        }

        return methodArgs;
    }
}
