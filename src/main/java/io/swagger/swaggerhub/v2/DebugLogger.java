package io.swagger.swaggerhub.v2;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class DebugLogger {
    public static void info(String message) {
        System.out.println(message);
        try (PrintWriter out = new PrintWriter(new FileWriter("test-output.log", true))) {
            out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
