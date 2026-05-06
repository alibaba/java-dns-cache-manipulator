package com.alibaba.dcm.tool;

import com.alibaba.dcm.agent.DcmAgent;
import com.sun.tools.attach.*;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.cli.*;
import org.apache.commons.cli.help.HelpFormatter;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import static java.lang.System.exit;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * DCM Tool.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 1.4.0
 */
public class DcmTool {
    static final String DCM_TOOLS_TMP_FILE_KEY = "DCM_TOOLS_TMP_FILE";
    static final String DCM_TOOLS_AGENT_JAR_KEY = "DCM_TOOLS_AGENT_JAR";

    private static final String DCM_AGENT_SUCCESS_MARK_LINE = "!!DCM SUCCESS!!";

    private static final List<String> actionList = DcmAgent.getActionList();

    /**
     * entry main method.
     */
    @SuppressFBWarnings("THROWS_METHOD_THROWS_CLAUSE_BASIC_EXCEPTION")
    public static void main(String[] args) throws Exception {
        final CommandLine cmd = parseCommandLine(args);

        final String[] arguments = cmd.getArgs();
        if (arguments.length < 1) {
            System.out.println("No Action! Available action: " + actionList);
            exit(2);
        }

        final String action = arguments[0].trim();
        if (!actionList.contains(action)) {
            throw new IllegalStateException("Unknown action " + action + ". Available action: " + actionList);
        }

        final String pid;
        if (cmd.hasOption('p')) {
            pid = cmd.getOptionValue('p');
        } else {
            pid = selectProcess();
        }

        doDcmActionViaAgent(action, arguments, pid);
    }

    private static CommandLine parseCommandLine(String[] args) throws ParseException, IOException {
        final Options options = new Options();
        options.addOption("p", "pid", true, "java process id to attach");
        options.addOption("h", "help", false, "show help");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption('h')) {
            HelpFormatter hf = HelpFormatter.builder().setShowSince(false).get();
            hf.printHelp("dcm", "setting/viewing dns of running JVM process",
                    options, "Report issues at https://github.com/alibaba/java-dns-cache-manipulator/issues", true);
            exit(0);
        }

        return cmd;
    }

    private static void doDcmActionViaAgent(String action, String[] arguments, String pid)
            throws AttachNotSupportedException, IOException, AgentLoadException, AgentInitializationException {
        final String tmpFile = getConfig(DCM_TOOLS_TMP_FILE_KEY);
        final String agentJar = getConfig(DCM_TOOLS_AGENT_JAR_KEY);

        final StringBuilder agentArgument = new StringBuilder();
        agentArgument.append(action);
        for (int i = 1; i < arguments.length; i++) {
            String s = arguments[i];
            agentArgument.append(' ').append(s);
        }
        agentArgument.append(" file ").append(tmpFile);

        VirtualMachine vm = null; // target java process pid
        boolean actionSuccess;
        try {
            vm = VirtualMachine.attach(pid);
            vm.loadAgent(agentJar, agentArgument.toString()); // loadAgent method will wait to agentmain finished.

            actionSuccess = printDcmResult(tmpFile);
        } finally {
            if (null != vm) {
                vm.detach();
            }
        }

        if (!actionSuccess) {
            exit(1);
        }
    }

    private static boolean printDcmResult(String tmpFile) throws IOException {
        boolean actionSuccess = false;

        final List<String> lines = Files.readAllLines(Paths.get(tmpFile), UTF_8);

        final int lastIdx = lines.size() - 1;
        final String lastLine = lines.get(lastIdx);
        if (DCM_AGENT_SUCCESS_MARK_LINE.equals(lastLine)) {
            lines.remove(lastIdx);
            actionSuccess = true;
        }

        for (String line : lines) {
            System.out.println(line);
        }

        return actionSuccess;
    }

    ///////////////////////////////////////////////
    // util methods
    ///////////////////////////////////////////////

    private static String getConfig(String name) {
        String var = System.getenv(name);
        if (var == null || var.trim().isEmpty()) {
            var = System.getProperty(name);
        }
        if (var == null || var.trim().isEmpty()) {
            throw new IllegalStateException("fail to var " + name + ", is absent or blank string!");
        }

        return var;
    }

    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    private static String selectProcess() {
        System.out.println("Which java process to attach:");
        final List<VirtualMachineDescriptor> list = VirtualMachine.list();

        // remove current process
        list.removeIf(vm -> vm.id().equals(pid()));

        for (int i = 0; i < list.size(); i++) {
            final VirtualMachineDescriptor vm = list.get(i);
            System.out.printf("%d) %-5s %s%n", i + 1, vm.id(), vm.displayName());
        }

        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.print("?# ");
            final String select = in.nextLine();
            try {
                final int idx = Integer.parseInt(select);
                if (idx > 0 && idx <= list.size()) {
                    return list.get(idx - 1).id();
                }
                System.out.println("Invalid selection!");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, not number!");
            }
        }
    }

    @VisibleForTesting
    static String pid() {
        final String name = ManagementFactory.getRuntimeMXBean().getName();
        final int idx = name.indexOf("@");
        return name.substring(0, idx);
    }
}
