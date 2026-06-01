package eightBit.vm;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            if (args.length == 0) {
                System.err.println("Please provide the compiled output.");
            } else {
                System.err.println("Too many arguments");
            }
            return;
        }

        try {
            String source = Files.readString(Paths.get(args[0]), StandardCharsets.UTF_8);
            VmRunResult result = VmRunner.runAsm(source);

            if (result.getStatus() == VmRunResult.Status.SUCCESS) {
                System.out.println("Execution finished.");
                System.out.println("Full output:");
                System.out.print(result.getStdout());
                System.out.println();
            } else {
                System.err.println("Execution error: " + result.getStderr());
            }
        } catch (Exception e) {
            System.err.println("Execution error: " + e.getMessage());
        }
    }

}