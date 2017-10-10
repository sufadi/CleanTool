package cleantool.su.starcleanmaster.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AdbShellUtil {

    /**
     * A silent uninstall of the application through the command line
     *
     * @param packageName
     * @return
     */
    public static String unInstallApp(String packageName) {
        String[] command = {"pm", "uninstall", packageName};
        return execCommand(command);
    }

    /**
     * Complete the silent installation of the application through the command
     * line
     *
     * @param apkAbsolutePath
     * @return success
     */
    public static String installApp(String apkAbsolutePath) {
        // TODO Auto-generated method stub
        String[] command = {"pm", "install", "-r", apkAbsolutePath};
        return execCommand(command);
    }

    /**
     * Commands can be executed in shell through ADB
     */
    public static String execCommand(String... command) {
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        String result = "";

        try {
            process = new ProcessBuilder().command(command).start();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            result = new String(baos.toByteArray());
            if (inIs != null)
                inIs.close();
            if (errIs != null)
                errIs.close();
            process.destroy();
        } catch (IOException e) {
            result = e.getMessage();
        }
        return result;
    }
}
