package sky.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Worker
{
	public static void main(String[] args)
	{
		if(!System.getProperty("os.name").contains("Windows"))//不是Windows 系统 不能用
		{
			System.out.println("非windows 系统，程序退出");
			return;
		}
		if(args.length != 1) //传参请传一个参
		{
			System.out.println("参数数量不合法，程序退出");
			return;
		}
		Integer portNum = getPortNum(args);
		if(portNum == -1)
		{
			System.out.println("端口号不合法，程序退出");//这个参数是要查询的本地端口号
			return;
		}
		String commandNetstat = "netstat -ano|findstr " + portNum;//netstat命令 查询端口占用情况
		Integer pid = analysisNetstat(exe(commandNetstat) , portNum);//解析netstat命令 返回pid
		if(pid == null)
		{
			System.out.println("端口未被占用");
			return;
		}
		String commandTasklist = "tasklist|findstr " + pid;//tasklist命令 查询pid对应信息
		String proccessName = analysisTaskList(exe(commandTasklist) , pid);//解析tasklist命令 返回进程名
		System.out.println("端口:" + portNum + "\tpid:" + pid + "\t进程名:" + proccessName);
		Scanner scanner = new Scanner(System.in);
		System.out.println("输入kk杀死该进程以释放端口，其他任意输入退出。");
		String input = scanner.next();
		scanner.close();
		if(input.equals("kk"))
		{
			String commandTaskkill = "taskkill /pid " + pid + " -t -f";//taskkill命令
			exe(commandTaskkill);
			if(analysisNetstat(exe(commandNetstat), portNum) == null)//再查一次端口是否被占用
			{
				System.out.println("该端口已释放");
			}
			else
			{
				System.out.println("释放端口失败，请手动释放");
			}
		}
		return;
	}
	
	/**
	 * 解析tasklist命令返回的东西
	 * @param taskList 每一行tasklist的返回组成的数组
	 * @param pid 之前查出的pid
	 * @return 进程名
	 */
	private static String analysisTaskList(String[] taskList , Integer pid)
	{
		for(String task : taskList)
		{
			String[] infos = getInfos(task);
			if(infos[1].equals(String.valueOf(pid)))
				return infos[0];
		}
		return null;
	}

	/**
	 * 解析netstat 命令 
	 * @param netStatResponse 每一行netstat的返回组成的数组
	 * @param portNum 期望的本地端口号
	 * @return 返回pid
	 */
	private static Integer analysisNetstat(String[] netStatResponse , Integer portNum)
	{
		for(String netStat : netStatResponse)
		{
			String[] infos = getInfos(netStat);
			String[] localInfo = infos[1].split(":");
			if(localInfo[1].toString().equals(portNum.toString()))
			{
				return Integer.valueOf(infos[4]);
			}
		}
		return null;
	}
	
	/**
	 * 将每一行的 返回内容拆成数组
	 * @param info 一行返回内容
	 * @return 返回内容的数组
	 */
	private static String[] getInfos(String info)
	{
		info = info.trim();
		StringTokenizer pas = new StringTokenizer(info, " ");
		StringBuilder sb = new StringBuilder();
		while(pas.hasMoreTokens())
		{
			String s = pas.nextToken();
			sb.append(s);
			sb.append(" ");
		}
		return sb.toString().split(" ");
	}

	/**
	 * 通过最开始的入参 得到端口号
	 * @param args 程序开始的入参
	 * @return 正确的端口号就正常返回 不正确的端口号返回-1
	 */
	private static Integer getPortNum(String[] args)
	{
		Integer port;
		try
		{
			port = Integer.valueOf(args[0]);
			if(port > 65536)
				port = -1;
		}
		catch (NumberFormatException e)
		{
			port = -1;
		}
		return port;
	}

	/**
	 * 执行cmd命令
	 * @param command cmd命令体
	 * @return cmd返回的内容每一行为元素组成的数组
	 * @throws 当调用返回值>1时 会抛出IllegalArgumentException
	 */
	private static String[] exe(String command) 
	{
		Process process = null;
		StringBuilder sb = new StringBuilder("C:/Windows/System32/cmd.exe /c ");
		sb.append(command);
		ArrayList<String> resultList = new ArrayList<>();
		try
		{
			process = Runtime.getRuntime().exec(sb.toString());
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			while((line = br.readLine()) != null)
			{
				resultList.add(line);
			}
			int exitStatus = process.waitFor();
			if(exitStatus > 1)
			{
				throw new IllegalArgumentException("调用进程未正常返回");
			}
			process.destroy();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		String[] result = new String[resultList.size()];
		resultList.toArray(result);
		return result;
	}
	//测试方法
	/*public static void main(String[] args)
	{
		String port = "51542";
		String command = "netstat -ano|findstr " + port;
		String[] result = exe(command);
		Integer pid = analysisNetstat(result , Integer.valueOf(port));
		command = "tasklist|findstr " + pid;
		String proccessName = analysisTaskList(exe(command) , pid);
		System.out.println("端口:" + port + "\tpid:" + pid + "\t进程名:" + proccessName);
	}*/
}
