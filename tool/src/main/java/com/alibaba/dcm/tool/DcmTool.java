package com.alibaba.dcm.tool;

import com.sun.tools.attach.VirtualMachine;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class DcmTool {
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

        Options options = new Options();
        options.addOption("p", "pid", true, "java process id to attach");
        options.addOption("h", "help", false, "show help");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption('h')) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("Options", options);
            return;
        }

        String pid = null;
        if (cmd.hasOption('p')) {
            pid = cmd.getOptionValue('p');
        } else {
            throw new IllegalStateException("required pid argument!");
        }

        final String[] arguments = cmd.getArgs();
        if (arguments.length < 1) {
            System.out.println("No Action! Available action: " + actionList);
        }

        String action = arguments[0].trim().toLowerCase();
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

        VirtualMachine vm = VirtualMachine.attach(pid); // target java process pid
        System.out.println(vm);
        vm.loadAgent(agentJar, agentArgument.toString());

        Thread.sleep(1000);
        vm.detach();
    }
}
