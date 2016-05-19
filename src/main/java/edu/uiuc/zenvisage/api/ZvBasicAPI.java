package edu.uiuc.zenvisage.api;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonParseException;

import edu.uiuc.zenvisage.service.ZvMain;

@Controller
public class ZvBasicAPI {

	@Autowired
	private ZvMain zvMain;
	
    public ZvBasicAPI(){
    	
		}
	
	@RequestMapping(value = "/getdata", method = RequestMethod.GET)
	@ResponseBody
	public String getData(@RequestParam(value="query") String arg) throws InterruptedException, IOException {
		System.out.println(arg);
		return zvMain.getData(arg);
	}

	@RequestMapping(value = "/getformdata", method = RequestMethod.GET)
	@ResponseBody
	public String getformdata( @RequestParam(value="query") String arg) throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println(arg);
		return zvMain.getInterfaceFomData(arg);
	}

	@RequestMapping(value = "/getBaselineData", method = RequestMethod.GET)
	@ResponseBody
	public String getBaselineData(@RequestParam(value="query")String arg) throws JsonParseException, JsonMappingException, IOException, InterruptedException {
		return zvMain.getBaselineData(arg);
	}

	@RequestMapping(value = "/getscatterplot", method = RequestMethod.GET)
	@ResponseBody
	public String getscatterplot(@RequestParam(value="query") String arg) throws JsonParseException, JsonMappingException, IOException {
		return zvMain.getScatterPlot(arg);
	}

	@RequestMapping(value = "/executeZQL", method = RequestMethod.GET)
	@ResponseBody
	public String executeZQL(@RequestParam(value="query") String arg) throws IOException, InterruptedException {
		return zvMain.runZQLQuery(arg);
	}

	@RequestMapping(value = "/test", method = RequestMethod.GET)
	@ResponseBody
	public String test(@RequestParam(value="query") String arg) {
		return "Test successful:" + arg;
	}


}