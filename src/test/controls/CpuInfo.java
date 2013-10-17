package test.controls;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarLoader;
import org.hyperic.sigar.cmd.Shell;
import org.hyperic.sigar.cmd.SigarCommandBase;


public class CpuInfo extends SigarCommandBase {

    public boolean displayTimes = true;

    public CpuInfo(Shell shell) {
        super(shell);
    }

    public CpuInfo() {
        super();
    }

    public String getUsageShort() {
        return "Display cpu information";
    }

    private void output(CpuPerc cpu) {
        println("User Time....." + CpuPerc.format(cpu.getUser()));
        println("Sys Time......" + CpuPerc.format(cpu.getSys()));
        println("Idle Time....." + CpuPerc.format(cpu.getIdle()));
        println("Wait Time....." + CpuPerc.format(cpu.getWait()));
        println("Nice Time....." + CpuPerc.format(cpu.getNice()));
        println("Combined......" + CpuPerc.format(cpu.getCombined()));
        println("Irq Time......" + CpuPerc.format(cpu.getIrq()));
        if (SigarLoader.IS_LINUX) {
            println("SoftIrq Time.." + CpuPerc.format(cpu.getSoftIrq()));
            println("Stolen Time...." + CpuPerc.format(cpu.getStolen()));
        }
        println("");
    }

    public void output(String[] args) throws SigarException {
        org.hyperic.sigar.CpuInfo[] infos = this.sigar.getCpuInfoList();

        CpuPerc[] cpus = this.sigar.getCpuPercList();

        org.hyperic.sigar.CpuInfo info = infos[0];
        long cacheSize = info.getCacheSize();
        println("Vendor........." + info.getVendor());
        println("Model.........." + info.getModel());
        println("Mhz............" + info.getMhz());
        println("Total CPUs....." + info.getTotalCores());
        if ((info.getTotalCores() != info.getTotalSockets()) || (info.getCoresPerSocket() > info.getTotalCores())) {
            println("Physical CPUs.." + info.getTotalSockets());
            println("Cores per CPU.." + info.getCoresPerSocket());
        }
        if (cacheSize != Sigar.FIELD_NOTIMPL) {
            println("Cache size...." + cacheSize);
        }
        println("");
        if (!this.displayTimes) {
            return;
        }
        for (int i = 0; i < cpus.length; i++) {
            println("CPU " + i + ".........");
            output(cpus[i]);
        }
        println("Totals........");
        output(this.sigar.getCpuPerc());

        StringBuffer sb=new StringBuffer("cpu号="+getCPUSerial()+"\n");
        
        String[] interfaces = sigar.getNetInterfaceList();
        if(interfaces!=null || interfaces.length>0)
            sb.append("第一个网卡号="+sigar.getNetInterfaceConfig(interfaces[0]).getHwaddr());

        org.hyperic.sigar.FileSystem[] filesystems = sigar.getFileSystemList();
        if(filesystems!=null || filesystems.length>0)
            sb.append("\n"+"硬盘第一个分区的卷标="+getHDSerial(filesystems[0].getDevName()));
        
        System.out.println(sb.toString());
    }

    public static void main(String[] args) throws Exception {
        //先加载siga动太库 在不同的平台只要加载特定的动态库，这里我就全加载不区分了
        //在IDE环境中，可以不加载动态库 设置natinve lib patch location 既可
        File nativeDir = new File("E:\\BCKF\\apache-tomcat-6.0.29\\webapps\\JavaWebapp\\resources\\native");
        File[] libs = nativeDir.listFiles();
        for (int i = 0; i < libs.length; i++) {
            if (libs[i].isFile())
                try {
                    System.load(new File(nativeDir, libs[i].getName()).getPath());
                } catch (Throwable t) {

                }
        }
        new CpuInfo().processCommand(args);
    }

    /**
     * 
     * 返回CPU的闲置率
     * 
     */
    public static double getCpuIdle() {
        Sigar sigar = null;
        try {
            sigar = new Sigar();
            return sigar.getCpuPerc().getIdle();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (sigar != null)
                sigar.close();
        }
        return 0;
    }

    /**
     * 
     * @param drive 硬盘驱动器分区 如C,D
     * @return 该分区的卷标
     */
    public static String getHDSerial(String drive) {
        String result = "";
        try {
            File file = File.createTempFile("tmp", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);

            String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                    + "Set colDrives = objFSO.Drives\n" + "Set objDrive = colDrives.item(\"" + drive + "\")\n"
                    + "Wscript.Echo objDrive.SerialNumber";
            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
            file.delete();
        } catch (Exception e) {

        }
        if (result.trim().length() < 1 || result == null) {
            result = "无磁盘ID被读取";

        }

        return result.trim();
    }

    /**
     * 获取CPU号,多CPU时,只取第一个
     * @return
     */
    public static String getCPUSerial() {
        String result = "";
        try {
            File file = File.createTempFile("tmp", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);

            String vbs = "On Error Resume Next \r\n\r\n" + "strComputer = \".\"  \r\n"
                    + "Set objWMIService = GetObject(\"winmgmts:\" _ \r\n"
                    + "    & \"{impersonationLevel=impersonate}!\\\\\" & strComputer & \"\\root\\cimv2\") \r\n"
                    + "Set colItems = objWMIService.ExecQuery(\"Select * from Win32_Processor\")  \r\n "
                    + "For Each objItem in colItems\r\n " + "    Wscript.Echo objItem.ProcessorId  \r\n "
                    + "    exit for  ' do the first cpu only! \r\n" + "Next                    ";

            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
            file.delete();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        if (result.trim().length() < 1 || result == null) {
            result = "无CPU_ID被读取";
        }
        return result.trim();
    }
}