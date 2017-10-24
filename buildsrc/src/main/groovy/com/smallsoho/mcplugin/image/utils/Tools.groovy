package com.smallsoho.mcplugin.image.utils
/**
 * Created by longlong on 2017/4/15.
 */
class Tools {

    def static Boolean cmd(def cmd) {
        String system = System.getProperty("os.name")
        switch (system) {
            case "Mac OS X":
                cmd = FileUtil.instance.getToolsDirPath() + "mac/" + cmd
                break
            case "Linux":
                LinuxInit()
                cmd = FileUtil.instance.getToolsDirPath() + "linux/" + cmd
                break
            case "Windows":
            case "Windows 10":
                cmd = FileUtil.instance.getToolsDirPath() + "windows/" + cmd
                break
            default:
                LogUtil.log("not support $system system")
                return false
        }

        return outputMessage(cmd)
    }

    def static LinuxInit() {
        outputMessage("chmod 755 -R ${FileUtil.instance.getToolsDirPath() + "/linux/"}")
    }

    def static Boolean outputMessage(def cmd) {
        def isSuccess
        def proc = cmd.execute()
        def out = new StringBuilder(), err = new StringBuilder()
        proc.consumeProcessOutput(out, err)
        proc.waitFor()
//        proc.exitValue()
        if (out.toString() != "" || err.toString() != "") {
            println "out> $out err> $err"
        }
        if (err.toString() != "") {
            isSuccess = false;
        } else {
            isSuccess = true
        }

        return isSuccess
    }

}
