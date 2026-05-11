package com.xjp.sms.util;

import java.util.Scanner;

public class SafeInput {
    private final Scanner scanner;

    public SafeInput(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readNonEmptyLine(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) return "";
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) return line;
            System.out.println("输入不能为空，请重试。");
        }
    }

    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) return 0;
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("请输入整数。");
            }
        }
    }

    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int v = readInt(prompt);
            if (v < min || v > max) {
                System.out.println("请输入范围 " + min + " 到 " + max + " 的整数。");
                continue;
            }
            return v;
        }
    }
}

