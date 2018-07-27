package edu.uiuc.zenvisage.server;
import java.io.*;
import java.nio.Buffer;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.awt.Desktop;
import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import edu.uiuc.zenvisage.api.*;
import edu.uiuc.zenvisage.api.Readconfig;
import edu.uiuc.zenvisage.data.remotedb.SQLQueryExecutor;
import edu.uiuc.zenvisage.service.ZvMain;

public class ZvServer {

	private Server server;
	private static int port;	
	
	static{
		//port = Readconfig.getPort();
		port = 8080;
	}
	
	public void setPort(int port) {
		this.port = port;
	}

	public void start() throws Exception {	
		server = new Server(port);	
		server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", 500000);
		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setContextPath("~/");
		webAppContext.setWar("zenvisage.war");
		webAppContext.setParentLoaderPriority(true);
		webAppContext.setServer(server);
		webAppContext.setClassLoader(ClassLoader.getSystemClassLoader());
		webAppContext.getSessionHandler().getSessionManager()
				.setMaxInactiveInterval(10);
		server.setHandler(webAppContext);	
		server.start();
//		ZvMain zvMain = (ZvMain) SpringApplicationContext.getBean("zvMain");
//		zvMain.loadData();
		
		DatabaseAutoLoader databaseAutoLoader = new DatabaseAutoLoader(this);
		databaseAutoLoader.run();
	
	}
	
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		ZvServer zvServer = new ZvServer();
		zvServer.start();
		System.out.println("\n [Running] \n");
		System.out.println("Please Enter the dataset name followed by path to a CSV file (e.g. Dataset /file/dataset.csv) OR enter a sql query in the format of SQL [database name] [Query] OR enter \"UI\" to launch the user interface: ");
		Scanner sc = new Scanner(System.in);
		String line = new String();
		while (sc.hasNextLine() && !((line = sc.nextLine()).equals(""))){
			if (line.equals("UI")){
				if (Desktop.isDesktopSupported()) {
					Desktop.getDesktop().browse(new URI("http://localhost:8080"));
				}
				continue;
			}
			String[] arguments = line.split(" ");
			if (arguments[0].equals("SQL")){
				String datasetname = arguments[1];
				StringBuilder sqlb = new StringBuilder();
				sqlb.append(arguments[2]);
				for (int i = 3; i < arguments.length; ++i)
				{
					sqlb.append(" ");
					sqlb.append(arguments[i]);
				}
				SQLQueryExecutor ex = new SQLQueryExecutor();
				ex.executeStatement(sqlb.toString());

				List<String> dataset4 = new ArrayList<String>(); //cmu
				dataset4.add(datasetname);
				File file = new File("/tmp/temp.csv");
				BufferedReader bfd = new BufferedReader(new FileReader(file));
				for (int i = 0; i < 10; ++i){
					System.out.println(bfd.readLine());
				}
				dataset4.add(file.getAbsolutePath());
				ZvBasicAPI api = new ZvBasicAPI();
				File meta = api.generateMetaFile(file);
				//file = new File(zvServer.getClass().getClassLoader().getResource(("cmu_clean.txt")).getFile());
				dataset4.add(meta.getAbsolutePath());
				ZvMain zvMain=new ZvMain();
				zvMain.uploadDatasettoDB(dataset4,false);
				zvMain.insertUserTablePair("public", datasetname);
				file.delete();
				meta.delete();
				System.out.println("Please Enter the dataset name followed by path to a CSV file (e.g. Dataset /file/dataset.csv) OR enter a sql query in the format of SQL [database name] [Query] OR enter \"UI\" to launch the user interface: ");
				continue;
			}

			System.out.println("sc------ " +line);
			List<String> dataset4 = new ArrayList<String>(); //cmu
			dataset4.add(arguments[0]);
			File file = new File(arguments[1]);
			dataset4.add(file.getAbsolutePath());
			ZvBasicAPI api = new ZvBasicAPI();
			File meta = api.generateMetaFile(file);
			//file = new File(zvServer.getClass().getClassLoader().getResource(("cmu_clean.txt")).getFile());
			dataset4.add(meta.getAbsolutePath());
			ZvMain zvMain=new ZvMain();
			zvMain.uploadDatasettoDB(dataset4,false);
			zvMain.insertUserTablePair("public", arguments[0]);
			meta.delete();
			System.out.println("Please Enter the dataset name followed by path to a CSV file (e.g. Dataset /file/dataset.csv) OR enter a sql query in the format of SQL [database name] [Query] OR enter \"UI\" to launch the user interface: ");

		}
	}	

}
