package com.alibaba.dcm.tool;

import com.sun.tools.attach.VirtualMachine;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class DcmTool {
    static final String DCM_AGENT_SUCCESS_MARK_LINE = "!!DCM SUCCESS!!";

    static List<String> actionList = new ArrayList<String>();

    static {
        actionList.add("set");
        actionList.add("get");

        actionList.add("list");
        actionList.add("clear");

        actionList.add("setPolicy");
        actionList.add("getPolicy");
        actionList.add("setNegativePolicy");
        actionList.add("getNegativePolicy");
    }


    public static void main(String[] args) throws Exception {
        final String tmpFile = System.getenv("DCM_TOOLS_TMP_FILE");
        final String agentJar = System.getenv("DCM_TOOLS_AGENT_JAR");
        if (tmpFile == null || tmpFile.trim().isEmpty() || agentJar == null || agentJar.trim().isEmpty()) {
            throw new IllegalStateException("blank tmp file " + tmpFile + ", or blank agent jar file " + agentJar);
        }

        final Options options = new Options();
        options.addOption("p", "pid", true, "java process id to attach");
        options.addOption("h", "help", false, "show help");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption('h')) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("Options", options);
            return;
        }

        final String pid; // TODO improve command, if no pid, list local jvm for user
        if (cmd.hasOption('p')) {
            pid = cmd.getOptionValue('p');
        } else {
            throw new IllegalStateException("required pid argument!");
        }

        final String[] arguments = cmd.getArgs();
        if (arguments.length < 1) {
            System.out.println("No Action! Available action: " + actionList);
            exit(2);
        }

        final String action = arguments[0].trim();
        if (!actionList.contains(action)) {
            throw new IllegalStateException("Unknown action " + action + ". Available action: " + actionList);
        }

        StringBuilder agentArgument = new StringBuilder();
        agentArgument.append(action).append(' ');
        for (int i = 1; i < arguments.length; i++) {
            String s = arguments[i];
            agentArgument.append(' ').append(s);
        }
        agentArgument.append(" file ").append(tmpFile);

        VirtualMachine vm = null; // target java process pid
        boolean actionSuccess = false;
        try {
            vm = VirtualMachine.attach(pid);
            vm.loadAgent(agentJar, agentArgument.toString()); // loadAgent method will wait to agentmain finished.

            final List<String> lines = FileUtils.readLines(new File(tmpFile), "UTF-8");

            final int lastIdx = lines.size() - 1;
            final String lastLine = lines.get(lastIdx);
            if (DCM_AGENT_SUCCESS_MARK_LINE.equals(lastLine)) {
                lines.remove(lastIdx);
                actionSuccess = true;
            }

            for (String line : lines) {
                System.out.println(line);
            }
        } finally {
            if (null != vm) {
                vm.detach();
            }
        }

        if (!actionSuccess) {
            exit(1);
        }
    }
}
