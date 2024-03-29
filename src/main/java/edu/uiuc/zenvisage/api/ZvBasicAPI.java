package edu.uiuc.zenvisage.api;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import edu.uiuc.zenvisage.server.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uiuc.zenvisage.model.DynamicClass;
import edu.uiuc.zenvisage.model.Variables;
import edu.uiuc.zenvisage.model.ZvQuery;
import edu.uiuc.zenvisage.model.AxisVariables;
import edu.uiuc.zenvisage.service.ZvMain;
import edu.uiuc.zenvisage.service.utility.PasswordStorage.CannotPerformOperationException;
import edu.uiuc.zenvisage.service.utility.PasswordStorage.InvalidHashException;
import edu.uiuc.zenvisage.api.Readconfig;
import edu.uiuc.zenvisage.data.remotedb.SQLQueryExecutor;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ZvBasicAPI {

	@Autowired
	public static ZvMain zvMain;
	public String logFilename="";
	public String querieslogFilename="";
	private String username="Anonymous user";
    public ZvBasicAPI(){
      zvMain = new ZvMain();
	}
    
    @RequestMapping(value = "/loginAvailable", method = RequestMethod.GET)
	@ResponseBody
    	public boolean loginAvailable(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, InterruptedException, SQLException, CannotPerformOperationException, InvalidHashException{
    		return Readconfig.getLoginAvaliable();
    	}
    
    @RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
    	public Map<String, ArrayList<String>> login(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, InterruptedException, SQLException, CannotPerformOperationException, InvalidHashException{
    		String uname = request.getParameter("uname");
    		String pass = request.getParameter("pass");
    		
    		if(zvMain.checkUser(uname, pass)) {
    			username = uname;
    			return zvMain.userinfo(uname);
    		}else {
    			return null;
    		}
    	}
    
    @RequestMapping(value = "/register", method = RequestMethod.POST)
	@ResponseBody
    	public Map<String, ArrayList<String>> register(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, InterruptedException, SQLException, CannotPerformOperationException, InvalidHashException{
    		String uname = request.getParameter("uname");
    		String pass = request.getParameter("pass");
    		if(uname == "" || pass=="") {
    			return null;
    		}
    		if(zvMain.register(uname, pass)) {
    			username = uname; 
    			return zvMain.userinfo(uname);
    		}else {
    			return null;
    		}
    	}
    
    //check file size, no larger than 100 MB
    @RequestMapping(value = "/file_size_checker", method = RequestMethod.POST)    
    	public @ResponseBody Map<String, String> file_size_checker(@RequestParam String size) throws IOException, ServletException, InterruptedException, SQLException, CannotPerformOperationException{
    		int size_limit = 100000000;
    		int file_size = Integer.parseInt(size);
    		String printing = String.format("file size is %s", file_size);
    		Map <String, String> failed_return = new HashMap<String, String> ();
    		failed_return.put("return", "extreme large file size");
    		System.out.println(printing);
    		if (file_size > size_limit) {
    			return failed_return;
    		}
    		else {
    			return null; 
    		}
    	}

	@RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
	@ResponseBody
	public void fileUpload(HttpServletRequest request, HttpServletResponse response) {
		try {
//		logQueries("fileUpload",request,"");
		zvMain.fileUpload(request, response);
//		zvMain.updateUTtables();
		System.out.println("uploaded!");
		} catch (Exception e) {
			e.printStackTrace();
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			} 
		}
	}
	 
    @RequestMapping(value = "/insertUserTablePair", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, ArrayList<String>> insertUserTablePair(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, InterruptedException, SQLException, CannotPerformOperationException, InvalidHashException{
    		
    		String uname = request.getParameter("userName");
    		System.out.println(uname);
    		String tname = request.getParameter("datasetName");
    		if(zvMain.insertUserTablePair(uname,tname)) {
    			//XuMO: check whether split table or not
    			if (zvMain.check_split_table_or_not(tname, uname)){
    				System.out.println("Need to split data table");
    				String join_key = zvMain.get_join_key(tname, uname);
    				System.out.println("join key is "+join_key);
    				zvMain.operations_for_split_table(tname, join_key);
    			}
    			return zvMain.userinfo(uname);
    		}else {
    			return null;
    		}
    	}


	@RequestMapping(value = "/createClasses", method = RequestMethod.POST)
	@ResponseBody
	public String createClasses(HttpServletRequest request, HttpServletResponse response) {
		String type="Create Classes";
		StringBuilder stringBuilder = new StringBuilder();
	    try {
		    Scanner scanner = new Scanner(request.getInputStream());
		    while (scanner.hasNextLine()) {
		    	stringBuilder.append(scanner.nextLine());
		    }
		    String body = stringBuilder.toString();
		    zvMain.runCreateClasses(body);
		    scanner.close();
		    return new ObjectMapper().writeValueAsString(body);
	    } catch (Exception e) {
			e.printStackTrace();
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	    }
	    return null;
	}
	
	/*
	 * /zv/getClassInfo
	 * {“dataset”: “real_estate”}
	 */
	@RequestMapping(value = "/getClassInfo", method = RequestMethod.POST)
	@ResponseBody
	public String getClassInfo(HttpServletRequest request, HttpServletResponse response) throws ClassNotFoundException, InterruptedException, IOException, ServletException, SQLException {
		StringBuilder stringBuilder = new StringBuilder();
		try {
		    Scanner scanner = new Scanner(request.getInputStream());
		    while (scanner.hasNextLine()) {
		    	stringBuilder.append(scanner.nextLine());
		    }
		    String body = stringBuilder.toString();
		    logQueries("getClassInfo",request,body);
		    scanner.close();
		    return zvMain.runRetrieveClasses(body);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}
	
	@RequestMapping(value = "/gettablelist", method = RequestMethod.GET)
	@ResponseBody
	public ArrayList<String> gettablelist(HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, InterruptedException, SQLException {
//		System.out.println(arg);
		try {
			return zvMain.getTablelist();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

//    /* Will be obsolete after separated calls*/
//	@RequestMapping(value = "/getdata", method = RequestMethod.GET)
//	@ResponseBody
//	public String getData(@RequestParam(value="query") String arg) throws InterruptedException, IOException {
//		//System.out.println(arg);
//		return zvMain.runDragnDropInterfaceQuery(arg);
//	}

	/* New Separated API calls
	 * Representative
	 * 	-Distance(Euc/DTW)
	 * Outlier
	 * 	-Distance(Euc/DTW)
	 * Similarity
	 *	-Distance(Euc/DTW)
	 *  -Similarity/Dis-similarity (true/false)
	 */
	@RequestMapping(value = "/postRepresentative", method = RequestMethod.POST)
	@ResponseBody
	public String postRepresentative(HttpServletRequest request, HttpServletResponse response) {
	//	logQueries("postRepresentative",request);
		StringBuilder stringBuilder = new StringBuilder();
		try {
			Scanner scanner = new Scanner(request.getInputStream());
			while (scanner.hasNextLine()) {
				stringBuilder.append(scanner.nextLine());
			}
			String body = stringBuilder.toString();
			String bodyforlogging=removeSketchPoints(body);
			logQueries("postRepresentative",request,bodyforlogging);
			System.out.println("Representative:"+body);
			scanner.close();
			return zvMain.runDragnDropInterfaceQuerySeparated(body, "RepresentativeTrends");
		} catch (Exception e) {
			e.printStackTrace();
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}
	
//	@RequestMapping(value = "/downloadRepresentative", method = RequestMethod.POST)
//	@ResponseBody
//	public String downloadRepresentative(HttpServletRequest request, HttpServletResponse response) throws InterruptedException, IOException, SQLException {
//		StringBuilder stringBuilder = new StringBuilder();
//	    Scanner scanner = new Scanner(request.getInputStream());
//	    while (scanner.hasNextLine()) {
//	        stringBuilder.append(scanner.nextLine());
//	    }
//
//	    String body = stringBuilder.toString();
////	    System.out.println("Representative:"+body);
//		zvMain.runDragnDropInterfaceQuerySeparated(body, "RepresentativeTrends");
//	}

	@RequestMapping(value = "/postOutlier", method = RequestMethod.POST)
	@ResponseBody
	public String postOutlier(HttpServletRequest request, HttpServletResponse response) {
		String type="postOutlier";
	//	logQueries(type,request);
		StringBuilder stringBuilder = new StringBuilder();
		try {
		    Scanner scanner = new Scanner(request.getInputStream());
		    while (scanner.hasNextLine()) {
		        stringBuilder.append(scanner.nextLine());
		    }
		    String body = stringBuilder.toString();
		    String bodyforlogging=removeSketchPoints(body);
		    logQueries(type,request,bodyforlogging);
		    scanner.close();
			return zvMain.runDragnDropInterfaceQuerySeparated(body, "Outlier");
		} catch (Exception e) {
			e.printStackTrace();
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}
	@RequestMapping(value = "/downloadSimilarity", method = RequestMethod.POST)
	@ResponseBody
	public String downloadSimilarity(HttpServletRequest request, HttpServletResponse response) {
		String type="downloadSimilarity";
		//		logQueries(type,request);
		StringBuilder stringBuilder = new StringBuilder();
		try {
			Scanner scanner = new Scanner(request.getInputStream());
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				stringBuilder.append(line);
				System.out.println(line);
			}
			//String resp = "{\"xval\":\""+zvMain.saveDragnDropInterfaceQuerySeparated(body, "SimilaritySearch")+"\"}";
			//System.out.println("resp: "+resp);
			//JSONObject jsonObj = new JSONObject("{\"phonetype\":\"N95\",\"cat\":\"WP\"}");
			String body = stringBuilder.toString();
			String bodyforlogging=removeSketchPoints(body);
			logQueries(type,request,bodyforlogging);
			String resp = zvMain.saveDragnDropInterfaceQuerySeparated(body, "SimilaritySearch");
			scanner.close();
			return resp;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}			
		}
		return null;
	}
	
	@RequestMapping(value = "/downloadOutlier", method = RequestMethod.POST)
	@ResponseBody
	public String downloadOutlier(HttpServletRequest request, HttpServletResponse response) {
		String type="downloadOutlier";
		System.out.println("downloadOutlier");
		StringBuilder stringBuilder = new StringBuilder();
		try {
		    Scanner scanner = new Scanner(request.getInputStream());
		    while (scanner.hasNextLine()) {
		    		String line = scanner.nextLine();
		        stringBuilder.append(line);
		        System.out.println(line);
		    }
		    String body = stringBuilder.toString();
		    String bodyforlogging=removeSketchPoints(body);
		    logQueries(type,request,bodyforlogging);
		    scanner.close();
		    return zvMain.saveDragnDropInterfaceQuerySeparated(body, "Outlier");
		} catch (Exception e) {
			e.printStackTrace();
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	} 
	
	@RequestMapping(value = "/postSimilarity_error", method = RequestMethod.POST)
	@ResponseBody
	public String postSimilarity_error(HttpServletRequest request, HttpServletResponse response) {
		String type="postSimilarity_error";
		StringBuilder stringBuilder = new StringBuilder();
		try { 
		    Scanner scanner = new Scanner(request.getInputStream());
		    while (scanner.hasNextLine()) {
		        stringBuilder.append(scanner.nextLine());
		    }
		    String body = stringBuilder.toString();
		    String bodyforlogging=removeSketchPoints(body);
		    logQueries(type,request,bodyforlogging);
		    scanner.close();
		    return zvMain.runDragnDropInterfaceQuerySeparated_error(body, "SimilaritySearch");
		} catch (Exception e) {
			e.printStackTrace();
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}			
		}
		return null;
	}
	
	@RequestMapping(value = "/postSimilarity", method = RequestMethod.POST)
	@ResponseBody
	public String postSimilarity(HttpServletRequest request, HttpServletResponse response) {
	    Scanner scanner;
		String type="postSimilarity";
		StringBuilder stringBuilder = new StringBuilder();
	    String bodyforlogging;
		try {
			scanner = new Scanner(request.getInputStream());
		    while (scanner.hasNextLine()) {
		        stringBuilder.append(scanner.nextLine());
		    }
			String body = stringBuilder.toString();
			bodyforlogging = removeSketchPoints(body);
		    logQueries(type,request,bodyforlogging);
		    scanner.close();
		    return zvMain.runDragnDropInterfaceQuerySeparated(body, "SimilaritySearch");
		} catch (Exception e) {
			e.printStackTrace();
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}
	

	@RequestMapping(value = "/logger", method = RequestMethod.POST)
	@ResponseBody
	public void logger(HttpServletRequest request, HttpServletResponse response) {
		if(!Readconfig.getBackendLogger()) {
			System.out.print("Logger is off!\n");
			return;
		}
		System.out.print("Username:");
		System.out.println(username);
		System.out.print("logFilename:");
		System.out.println(logFilename);
		try {
			if (logFilename.equals("")){
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
				logFilename = "../"+sdf.format(timestamp)+".log";
			}
			File file = new File(logFilename);

			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
			String log = request.getParameter("timestamp")+","+request.getRemoteAddr()+','+request.getParameter("message")+'\n';
			System.out.println(log);
			writer.write("Username: "+username+'\n');
			writer.write(log);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	 
	
	public void logQueries(String type,HttpServletRequest request,String message) {
		if(!Readconfig.getBackendQueriesLog()) {
			System.out.print("Queries Log is off!\n");
			return;
		}
		System.out.print("Username:");
		System.out.println(username);
		System.out.print("QuerieslogFilename:");
		System.out.println(querieslogFilename);
		if (querieslogFilename.equals("")){
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
			querieslogFilename = "../"+"Queries-"+sdf.format(timestamp)+".log";
		}		
		File file = new File(querieslogFilename);
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
			String log="";
			if(request!=null) {

				if(request.getParameter("timestamp")==null) {
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
					log  = sdf.format(timestamp)+","+request.getRemoteAddr()+","+type+','+message+'\n';
				} else {
					log  = request.getParameter("timestamp")+","+request.getRemoteAddr()+','+type+','+message+'\n';
				}
			} else {
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
				log  = sdf.format(timestamp)+",null,"+type+','+message+'\n';
			}
			System.out.println(log);
			writer.write("Username: "+username+'\n');
			writer.write(log);
			writer.close();
		} catch(Exception e){
			System.out.println("Error while logging: "+e.toString());
		}
	}
	 
	
	
	@RequestMapping(value = "/getDissimilarity", method = RequestMethod.GET)
	@ResponseBody
	public String getDissimilarity(@RequestParam(value="query") String arg, HttpServletResponse response) {
//		System.out.println(arg);
		try {
		return zvMain.runDragnDropInterfaceQuerySeparated(arg, "DissimilaritySearch");
		} catch (Exception e) {
			e.printStackTrace();
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	@RequestMapping(value = "/getformdata", method = RequestMethod.GET)
	@ResponseBody
	public String getformdata(@RequestParam(value="query") String arg, HttpServletResponse response) {
		System.out.println(arg);
		try {
			String ret = zvMain.getInterfaceFormData2(arg);
//			System.out.println("get interface form data: " + ret);
		    return ret;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	public String removeSketchPoints(String body) throws Exception {
		String bodyforlogging = "";
		try {
			ZvQuery args = new ObjectMapper().readValue(body, ZvQuery.class);
			args.dataX=null;
			args.dataY=null;
			args.sketchPoints=null;
			bodyforlogging=new ObjectMapper().writeValueAsString(args);
		} catch (JsonParseException e) {
			throw new Exception("Json Parsing Exception");
		} catch (JsonMappingException e) {
			throw new Exception("Json Mapping Exception");
		} catch (JsonGenerationException e) {
			throw new Exception("Json Generation Exception");
		}catch (IOException e) {
			throw new Exception("IOException");
		}
	    return bodyforlogging;
	}
	
//	@RequestMapping(value = "/getBaselineData", method = RequestMethod.GET)
//	@ResponseBody
//	public String getBaselineData(@RequestParam(value="query")String arg) throws JsonParseException, JsonMappingException, IOException, InterruptedException {
//		return zvMain.getBaselineData(arg);
//	}
//
//	@RequestMapping(value = "/getscatterplot", method = RequestMethod.GET)
//	@ResponseBody
//	public String getscatterplot(@RequestParam(value="query") String arg) throws JsonParseException, JsonMappingException, IOException {
//		return zvMain.getScatterPlot(arg);
//	}

	@RequestMapping(value = "/executeZQL", method = RequestMethod.GET)
	@ResponseBody
	public String executeZQL(@RequestParam(value="query") String arg) throws IOException, InterruptedException {
		//return zvMain.runZQLQuery(arg);
		return "";
	}

	@RequestMapping(value = "/executeZQLComplete", method = RequestMethod.GET)
	@ResponseBody
	public String executeZQLComplete(@RequestParam(value="query")  String arg, HttpServletResponse response) {
		// for testing my query graph executor with zql.html
		// String outputExecutor = zvMain.runZQLCompleteQuery(arg);
		try {
		String outputGraphExecutor = zvMain.runQueryGraph(arg);
		logQueries("ZQL",null,arg);
		return outputGraphExecutor;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}
	

	@RequestMapping(value = "/executeZQLScript", method = RequestMethod.GET)
	@ResponseBody
	public String executeZQLScript(@RequestParam(value="query")  String arg, HttpServletResponse response) {
		// for testing my query graph executor with zql.html
		// String outputExecutor = zvMain.runZQLCompleteQuery(arg);
		try {
			String outputGraphExecutor = zvMain.runZQLScript(arg);
			logQueries("ZQL",null,arg);
			return outputGraphExecutor;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	@RequestMapping(value = "/selectXYZ", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Variables> executeSelectXYZ(@RequestBody Variables variables) throws SQLException, IOException, InterruptedException {
		if( variables != null) {
	      zvMain.insertZenvisageMetatable(variables);
          System.out.println("Variables:"+variables.toString());
        } else {
          System.out.println("Variables are null");
        }
		return null;
	}
	static int num = 26;

	@RequestMapping(value = "/executeSearch", method = RequestMethod.POST)
	public @ResponseBody String executeSearch(@RequestParam String searchTerm, String location, String from, String to, String minuCount, String minhtCount, String state, String retweet, String related) throws SQLException, IOException, InterruptedException, InvalidHashException, CannotPerformOperationException {
		StringBuilder sqlb = new StringBuilder();
		SQLQueryExecutor ex = new SQLQueryExecutor();

		if (minuCount.equals("")) minuCount = "10";
		if (minhtCount.equals("")) minhtCount = "10";
		String startDate = new String();
		if (from.equals("")){
			startDate = "01/01/2018";
		}else{
			startDate = from;
		}

		String terms[] = searchTerm.split(" ");
		StringBuilder posTerms = new StringBuilder();
		StringBuilder negTerms = new StringBuilder();
		String posPattern = new String();
		String negPattern = new String();
		if (!searchTerm.equals("")) {
			for (int i = 0; i < terms.length; ++i){
				if (terms[i].charAt(0) == '-'){
					negTerms.append(terms[i].substring(1, terms[i].length()));
					negTerms.append("|");
				}else{
					posTerms.append(terms[i]);
					posTerms.append("|");
				}
			}
			if (!negTerms.toString().equals("")){
				negPattern = negTerms.toString().substring(0, negTerms.length()-1);
			}
			if (!posTerms.toString().equals("")){
				posPattern = posTerms.toString().substring(0, posTerms.length()-1);
			}
		}

		if (!posPattern.equals("") && related.equals("true")){
			String cooccurrenceSQL = "(SELECT h1 as term,cooccurrencecount FROM uclacooccurrence WHERE h2 similar to '%("+ posPattern+ ")%\' UNION SELECT h2 as term,cooccurrencecount FROM uclacooccurrence WHERE h1 similar to '%(" + posPattern + ")%\') order by cooccurrencecount desc limit 5";
			ResultSet rs = ex.executeQuery(cooccurrenceSQL);
			StringBuilder bd = new StringBuilder(posPattern);
			for (int i = 1; i <= 5; ++i){
				rs.next();
				bd.append("| " + rs.getString(1));
			}
			posPattern = bd.toString();
		}
		sqlb.append("SELECT DATE_PART('day', createdate::timestamp - \'" + startDate + "\'::timestamp) AS ElapsedDays, placeFullName, lower(hashtags) AS hashtags, htCount, uCount, RTSum from uclahiv WHERE ");
		sqlb.append(" htCount > " + minhtCount);
		sqlb.append(" AND uCount > " + minuCount);
		if (!location.equals("")){
			sqlb.append(" AND placeFullName::text LIKE " + "\'%" + location +"%\'");
		}
		if (!state.equals("any")){
			sqlb.append(" AND placeFullName::text SIMILAR TO " + "\'%(" + state +")%\'");
		}
		if (!posPattern.equals("")){
			sqlb.append(" AND lower(hashtags) SIMILAR TO " + "\'%("+ posPattern.toLowerCase() + ")%\'");
		}
		if (!negPattern.equals("")){
			sqlb.append(" AND lower(hashtags) NOT SIMILAR TO " + "\'%("+ negPattern.toLowerCase() + ")%\'");
		}
		if (!from.equals("")){
			sqlb.append(" AND createdate > \'" + from + "\'::timestamp");
		}
		if (!to.equals("")){
			sqlb.append(" AND createdate < \'" + to + "\'::timestamp");
		}
		if (retweet.equals("true")){
			sqlb.append(" AND RTSum > 0");
		}
		sqlb.append(" ORDER BY htCount DESC");
		System.out.println("Generated: " + sqlb.toString());
		ex.executeStatement(sqlb.toString());
		String datasetname = "data" + num;
		num++;
		List<String> dataset4 = new ArrayList<String>(); //cmu
		dataset4.add(datasetname);
		File file = new File("/tmp/temp.csv");
		BufferedReader bfd = new BufferedReader(new FileReader(file));
		for (int i = 0; i < 10; ++i){
			System.out.println("content: " + bfd.readLine());
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
		return posPattern;
	}
	//Add join key attribute to join key table
    @RequestMapping(value = "/join_key_adder", method = RequestMethod.POST)
    	public @ResponseBody Map<String, String> join_key_adder(@RequestParam String username, String dataset, String join_key) throws IOException, ServletException, InterruptedException, SQLException, CannotPerformOperationException{
    		zvMain.insertJoinKey(username, dataset, join_key);
    		return null;
    	}

	@RequestMapping(value = "/executeScatter", method = RequestMethod.GET)
	@ResponseBody
	public String executeScatter(@RequestParam(value="query")  String arg, HttpServletResponse response) {
		try {
			String outputGraphExecutor = zvMain.runScatterQueryGraph(arg);
			logQueries("ZQL-Scatter",null,arg);
			return outputGraphExecutor;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	public File generateMetaFile(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String signature = br.readLine();
		System.out.println("sig " + signature);
		String[] category = signature.split(",");
		String str = br.readLine();
		int quoteCount = 0;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < str.length(); ++i){
			if (str.charAt(i) == ','){
				if (quoteCount % 2 == 1){
					sb.append("_");
				}else {
					sb.append(",");
				}
			}else if (str.charAt(i) == '\"'){
				quoteCount ++;
				sb.append("\"");
			}else {
				sb.append(str.charAt(i));
			}
		}
		str = sb.toString();
		System.out.println("str " + str);
        String[] data = str.split(",");
        String[] types = new String[category.length];
        for(int i = 0; i < data.length; ++i){
            String entry = data[i].replace("\"", "");
            if (entry.equals("dynamic_class")){
                break;
            }
            try{
                Integer.parseInt(data[i]);
            }catch(NumberFormatException e){
                try{
                    Float.parseFloat(data[i]);

                }catch(NumberFormatException ex){
                    //not float
                    types[i] = "string";
                    continue;
                }
                types[i] = "float";
                continue;
            }
            types[i] = "int";
            //check if float

        }
        StringBuilder bd = new StringBuilder();
        for (int i = 0; i < types.length; ++i){
            if (types[i] == null || types[i].equals("")) break;;
            bd.append(category[i].replace("\"", ""));
            bd.append(":");
            bd.append(types[i]);
            bd.append(",");
            if (types[i].equals("string")){
            	bd.append("unindexed,");
			}else{
				bd.append("indexed,");

			}

            if ( types[i].equals("float") || types[i].equals("int")){
                bd.append("T,T,F,F,F,0,Q");
            }else{
                bd.append("F,F,T,F,F,0,Q");
            }
            bd.append("\n");
        }
        bd.setLength(bd.length()-1);
        System.out.println("-------------");
        System.out.println(data[0]);
        System.out.println(data[1]);

        System.out.println(bd.toString());
        File meta = new File("meta");
        FileWriter fileWriter = new FileWriter(meta);
        fileWriter.write(bd.toString());
        fileWriter.flush();
        fileWriter.close();
        return meta;

	}
    @RequestMapping(value = "/easyupload", method = RequestMethod.POST)
    public @ResponseBody void easyupload(@RequestParam String dataset, String csvPath) throws IOException, ServletException, InterruptedException, SQLException, CannotPerformOperationException{
        System.out.println("request succ " + dataset );


        List<String> dataset5 = new ArrayList<String>(); //cmu
        dataset5.add(dataset);
        File file = new File("temp");
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(csvPath);
		fileWriter.flush();
		fileWriter.close();


		dataset5.add(file.getAbsolutePath());
		File meta = generateMetaFile(file);

        dataset5.add(meta.getAbsolutePath());
        ZvMain zvMain=new ZvMain();
        zvMain.uploadDatasettoDB(dataset5,false);


    }
    
	@RequestMapping(value = "/test", method = RequestMethod.GET)
	@ResponseBody
	public String test(@RequestParam(value="query") String arg) {
		 logQueries("test",null,arg);
			// TODO change to graph executor
		return "Test successful:" + arg;
	}
	
//	@RequestMapping(value = "/verifyPassword", method = RequestMethod.GET)
//	@ResponseBody
//	public String verifyPassword(@RequestParam(value="query") String arg) throws IOException {
//		System.out.println("arg:");
//		System.out.println(arg);
//		// Creates a FileReader Object
//		System.out.println("verifyPassword");
//	    FileReader fr = new FileReader("../secret.txt"); 
//	    char [] a = new char[50];
//	    fr.read(a);   // reads the content to the array
//	    for(char c : a)
//	       System.out.print(c);   // prints the characters one by one
//	    fr.close();
//		return "Test successful:" + arg;
//	}
	

}
