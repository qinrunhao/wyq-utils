package com.wyq.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CmdUtil {

    /**
     * 本机调用无返回
     *
     * @param command
     */
    public static void execLocal(String command) {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(command);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 本机调用有返回
     *
     * @param command
     * @param pipe    command 中是否有管道 |
     * @return
     * @throws Exception
     */
    public static String execReturnLocal(String command, boolean pipe) throws Exception {
        //执行一个命令需要展示返回结果的
        Runtime r = Runtime.getRuntime();
        Process p = (pipe) ? r.exec(new String[]{"/bin/bash", "-c", command}) : r.exec(command);
        p.waitFor();
        BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        StringBuffer sb = new StringBuffer();
        while ((line = b.readLine()) != null) {
            sb.append(line).append("\n");
        }
        b.close();
        return sb.toString();
    }
}
