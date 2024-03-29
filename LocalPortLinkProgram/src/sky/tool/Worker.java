package sky.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Worker
{
	public static void main(String[] args)
	{
		if(!System.getProperty("os.name").contains("Windows"))
		{
			System.out.println("非windows 系统，程序退出");
			return;
		}
		if(args.length != 1)
		{
			System.out.println("参数数量不合法，程序退出");
			return;
		}
		Integer portNum = getPortNum(args);
		if(portNum == -1)
		{
			System.out.println("端口号不合法，程序退出");
			return;
		}
		String commandNetstat = "netstat -ano|findstr " + portNum;
		Integer pid = analysisNetstat(exe(commandNetstat) , portNum);
		if(pid == null)
		{
			System.out.println("端口未被占用");
			return;
		}
		String commandTasklist = "tasklist|findstr " + pid;
		String proccessName = analysisTaskList(exe(commandTasklist) , pid);
		System.out.println("端口:" + portNum + "\tpid:" + pid + "\t进程名:" + proccessName);
		return;
	}
	
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
