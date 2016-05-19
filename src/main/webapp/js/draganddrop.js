var scale;
var itemId = undefined;
//the following global variables should have their function calls. e.g getChartData(); ...
var chartData;
var max_X;
var min_X;
var max_Y;
var min_Y;
//var rawData;
//
/*  the id of an item dragged by a user, but before being dropped on graph
    if user drops item in graph we set itemId = tempItemId
*/
var chart0Information = [0,0,0,0,null]; //[ min_X , max_X , min_Y , max_Y, chartData]
var chart1Information = [0,0,0,0,null]; //
var tempItemId;
function allowDrop(ev) {
    ev.preventDefault();
}

// OR DOUBLE CLICK
$(document).on( "dblclick", ".graph", function() {
  itemId = $(this).attr("id");
  updateGraphOnDblClick();
});

function updateGraphOnDblClick(){

    //If both charts are visible and we double clicked a result chart
    if(isGraphVisible("1") && itemId.indexOf("table_view")>=0){ //only do for both chart case
        var IdStr = itemId.replace("table_view", "");
        var IdNum = parseInt(IdStr)
        if(IdNum % 2 == 0){
            //even ids should be with drawGraphIndex 0
            drawTrend();
            onSubmit();
            updatelist("");	//need to update the list so once you click modify, you are using new list.
        }
        else{
            //odd ids
            drawTrend1();
            onSubmit();
            updatelist("1");	//need to update the list so once you click modify, you are using new list.
        }
    }
    else if(isGraphVisible("1") && itemId.indexOf("existing-trend")>=0){
        //if both charts visible and we dbl clicked an existing trend
        if(ExTrendindex == 0){
            drawTrend();
            onSubmit();
            updatelist("");	//need to update the list so once you click modify, you are using new list.
        }
        else if(ExTrendindex == 1){
            drawTrend1();
            onSubmit();
            updatelist("1");	//need to update the list so once you click modify, you are using new list.
        }
    }
    else{
        //single chart case
        drawTrend();
        onSubmit();
        updatelist("");	//need to update the list so once you click modify, you are using new list.
    }
}

function drag(item, ev) {
	console.log("dragstart");
	console.log(item)
	//init();
	//document.getElementById("svgLayer").style.display = "none";
	//document.getElementById("tools_sketch").style.display = "none";
	//document.getElementById("draganddrop").style.zIndex = 0;
    $("#draganddrop").css("display","block");
    $("#draganddrop1").css("display","block");

	//document.getElementById("visualisation").style.display = "none";
	//console.log(ev.srcElement.id);
	tempItemId = ev.srcElement.id; //use temp because user may not actually drop chart in
  console.log(ev.srcElement.id);
	//if(dragEnable){
	//	enableDragAndDrop();
	//}
}

function droppable(itemId, drawGraphIndex){
    if(!isGraphVisible("1")){
        return true;
    }
    if(itemId.indexOf("existing-trend")>-1){
    	if (ExTrendindex == drawGraphIndex) {
    		return true; // drag and drop to same index of chart
    	}
    	else {
            alert("can't drag and drop across!");
            return false;
    	}
    }
    tempData = canDrop(itemId, drawGraphIndex);
    if(tempData == false){
        alert("can't drag and drop across!")
        return false
    }
    return true
}

function drop(ev) {
	console.log("drop")
    ev.preventDefault();
    $("#tools_sketch").css("display","none");
    $("#draganddrop").css("display","none");
    $("#draganddrop1").css("display","none");
    if(!histogram){
    	$("#visualisation").css("display","none");
    }
    if(!droppable(tempItemId, 0)){
        return
    }

    itemId = tempItemId; // If we actually do drop graph in, update itemId
	if(!histogram){
	    clickmodify = true; //?correct??
		drawTrend();	//should not update list yet
		onSubmit();
		updatelist("");	//need to update the list so once you click modify, you are using new list.
		console.log("!histogram")
	}
	else{
		drawBarsAfterDragDrop();
		console.log("bar config")
		onSubmit();
	}
}

function drop1(ev){
    ev.preventDefault();
    $("#tools_sketch1").css("display","none");
    $("#draganddrop").css("display","none");
    $("#draganddrop1").css("display","none");
    $("#visualisation1").css("display","none");
    if(!droppable(tempItemId, 1)){
        return
    }

    itemId = tempItemId;
    clickmodify = true;
    drawTrend1();
    onSubmit();
    updatelist("1");	//need to update the list so once you click modify, you are using new list.

}


function updatelist(suffix){
	for(var b = 0; b< 30; b++){
		var str = "point";
		var myP = str.concat(b.toString());

		var myDots = document.getElementById(myP);
		if(myDots != null ){
			myDots.setAttribute("cx", 0);
			myDots.setAttribute("cy", 0);
		}
	}
	clickmodify = true;
	var x; var y;
	chartData=existingTrends[ExTrendindex]["outputCharts"][0];
	if(itemId == "existing-trend-0"){

		x = existingTrends[ExTrendindex]["outputCharts"][0].xData;
		y = existingTrends[ExTrendindex]["outputCharts"][0].yData;
		chartData = existingTrends[ExTrendindex]["outputCharts"][0];
	 }
	else if(itemId == "existing-trend-1"){
		x = existingTrends[ExTrendindex]["outputCharts"][1].xData;
		y = existingTrends[ExTrendindex]["outputCharts"][1].yData;
		chartData = existingTrends[ExTrendindex]["outputCharts"][1];
	 }
	else if(itemId == "existing-trend-2"){
		x = existingTrends[ExTrendindex]["outputCharts"][2].xData;
		y = existingTrends[ExTrendindex]["outputCharts"][2].yData;
		chartData = existingTrends[ExTrendindex]["outputCharts"][2];
	 }
	else if(checkOutputCharts(itemId,0)!=null){
		//console.log("inside drag data", outputData);
		x = checkOutputCharts(itemId,0).xData;
		y = checkOutputCharts(itemId,0).yData;
		chartData = checkOutputCharts(itemId,0);
	}
    else{
        return;
    }

	var myList = [];
	max_X = Math.max.apply(Math,x);
	min_X = Math.min.apply(Math,x);
	max_Y = Math.max.apply(Math,y);
	min_Y = Math.min.apply(Math,y);
	//console.log(existingTrends["outputCharts"]);
	//console.log(chartData);
	changeScaleBlankChart( min_X , max_X , min_Y , max_Y, chartData, suffix);
    if(suffix == ""){
    chart0Information[0] = min_X
    chart0Information[1] = max_X
    chart0Information[2] = min_Y
    chart0Information[3] = max_Y
    chart0Information[4] = chartData
  }
  else if(suffix == "1"){
    chart1Information[0] = min_X
    chart1Information[1] = max_X
    chart1Information[2] = min_Y
    chart1Information[3] = max_Y
    chart1Information[4] = chartData
  }

	var range_X = max_X - min_X;
	var range_Y = max_Y - min_Y;
	scale = canvas.width/(x.length-1); //! or /(x.length) ??

	if(x.length>= 30){
		sm_scale = false;
		var offset = (x.length)/30;  // or /41??

		for(var i =0; i< 30; i++){
			if(range_Y ==0){
				//myList.push([(x[parseInt(i*offset)])/range_X*drawGraphWidth,drawGraphHeight-min_Y]);
				myList.push([Math.round((x[Math.round(i*offset)]-min_X)*(drawGraphWidth/range_X)), drawGraphHeight-min_Y]);
			}
			else{

				//console.log("x",Math.round((x[Math.round(i*offset)]-min_X)*(drawGraphWidth/range_X)));
				//console.log("y",Math.round(drawGraphHeight-(y[Math.round(i*offset)]-min_Y)*(drawGraphHeight/range_Y)));
				myList.push([Math.round((x[Math.round(i*offset)]-min_X)*(drawGraphWidth/range_X)),Math.round(drawGraphHeight-(y[Math.round(i*offset)]-min_Y)*(drawGraphHeight/range_Y))]);
			}
		}

	}
	else{


		for(var i = 0; i< x.length; i++){
			if(range_Y == 0){
				myList.push([Math.round((x[i]-min_X)*(drawGraphWidth/range_X)),drawGraphHeight-max_Y]);
			}
			else{
				myList.push([Math.round((x[i]-min_X)*(drawGraphWidth/range_X)), Math.round(drawGraphHeight-(y[i]-min_Y)*(drawGraphHeight/range_Y))]);     //get the max and min range!
			}
		}

		sm_scale = true;

	}

	if(suffix == "")
		list = myList;
	else
		list1 = myList;
}

function checkOutputCharts(itemId, drawGraphIndex){
    if(outputData == null){
        return null
    }
    if(itemId == null){
        return null
    }
    if(drawGraphIndex === undefined)
        return null
	//var temp = outputData["outputCharts"];
	var numOfCharts = outputData["outputCharts"].length;
	for(var i = 0; i< numOfCharts; i++){
		var id = "table_view"+i;
		if(itemId == id){
			return outputData["outputCharts"][i];
		}
	}
	return;
}

function canDrop(itemId, drawGraphIndex){
    //drawGraphIndex = 0, 1
    //for drawGraphIndex = 1, only accept table_view1, 3, 5, 7, ...
    //for drawGraph INdex = 0, only accept table_view0, 2, 4, 6, ...
    //itemId = "table_viewID" where ID = number for the id
    if(isGraphVisible("1") && itemId.indexOf("table_view")>=0){ //only do for both chart case
        var IdStr = itemId.replace("table_view", "");
        var IdNum = parseInt(IdStr)
        if(IdNum % 2 == 0){
            //even ids should be with drawGraphIndex 0
            if(drawGraphIndex !=0){
                return false
            }
        }
        else{
            //odd ids

            if(drawGraphIndex !=1){
                return false
            }
        }
    }
    return true
}

function drawTrend(){
    if(updateChart("", chart0Information, 0)){
        //if we have successfully updated our chartData
        drawData("", chart0Information, list, myPath)
    }
}

function drawTrend1(){
    if(updateChart("-1", chart1Information, 1)){
        //if we have successfully updated our chartData
        drawData("1", chart1Information, list1, myPath1)
    }
}

/*
 * Updates the global chartData datastructure that holds our chart data
*/
function updateChart(suffix, chartInformation, chartNum){
//	console.log("chartNum", chartNum)
	console.log("itemId", itemId)
    for(var b = 0; b< 30; b++){
		//var str = "point";
		//var myP = str.concat(b.toString());
    var myP = "point" + b + suffix
		var myDots = document.getElementById(myP);
		if(myDots != null ){
			myDots.setAttribute("cx", 0);
			myDots.setAttribute("cy", 0);
		}
	}
	clickmodify = true;
	var x; var y;
	var chartData=existingTrends[ExTrendindex]["outputCharts"][0];
	if(itemId == "existing-trend-0"){

		x = existingTrends[ExTrendindex]["outputCharts"][0].xData;
		y = existingTrends[ExTrendindex]["outputCharts"][0].yData;
		chartData = existingTrends[ExTrendindex]["outputCharts"][0];
	 }
	else if(itemId == "existing-trend-1"){
		x = existingTrends[ExTrendindex]["outputCharts"][1].xData;
		y = existingTrends[ExTrendindex]["outputCharts"][1].yData;
		chartData = existingTrends[ExTrendindex]["outputCharts"][1];
	 }
	else if(itemId == "existing-trend-2"){
		x = existingTrends[ExTrendindex]["outputCharts"][2].xData;
		y = existingTrends[ExTrendindex]["outputCharts"][2].yData;
		chartData = existingTrends[ExTrendindex]["outputCharts"][2];
	 }
	else if(checkOutputCharts(itemId,chartNum)!=null){
		//console.log("inside drag data", outputData);
		x = checkOutputCharts(itemId,chartNum).xData;
		y = checkOutputCharts(itemId,chartNum).yData;
		chartData = checkOutputCharts(itemId,chartNum);
	}
    else{
        return false;
    }
    max_X = Math.max.apply(Math,x);
    min_X = Math.min.apply(Math,x);
    max_Y = Math.max.apply(Math,y);
    min_Y = Math.min.apply(Math,y);
    chartInformation[0] = min_X
    chartInformation[1] = max_X
    chartInformation[2] = min_Y
    chartInformation[3] = max_Y
    chartInformation[4] = chartData
    return true;
}

/*
 * Draw the output charts from chartData
*/
function drawData(suffix, chartInformation, globalList, globalmyPath){
    	//list = [[9,206],[27,194],[51,184],[71,174],[92,163],[112,151],[119,145],[152,129],[172,119],[194,107],[214,97],[234,86],[253,76],[276,64],[296,56],[317,46],[337,30],[357,20],[379,9],[395,5],[399,0]];
    	//for(var b = 0; b< 41; b++){
        if(chartInformation[4] == null){
            return
        }
        min_X = chartInformation[0]
        max_X = chartInformation[1]
        min_Y = chartInformation[2]
        max_Y = chartInformation[3]
        var chartData = chartInformation[4]
        var x = chartData.xData
        var y = chartData.yData

    	changeScaleBlankChart( min_X , max_X , min_Y , max_Y, chartData, suffix);
        //changeScaleBlankChart( chartInformation[0] , chartInformation[1]
            //, chartInformation[2] , chartInformation[3], chartInformation[4], suffix);

        //var distance = bl.offsetLeft - bl.scrollLeft + bl.clientLeft;
    	var range_X= max_X-min_X;
    	var range_Y= max_Y-min_Y;
    	scale = drawGraphWidth/(x.length-1); //! or /(x.length) ??

      globalList.length = 0; //clears the list or mylist
      //Updates the array that globalList is pointing to, list or list1
    	for(var i = 0; i< x.length; i++){
    		if(range_Y ==0){
    			globalList.push([Math.round((x[i]-min_X)*(drawGraphWidth/range_X)),drawGraphHeight-max_Y]);
    		}
    		else{
    			globalList.push([Math.round((x[i]-min_X)*(drawGraphWidth/range_X)), Math.round(drawGraphHeight-(y[i]-min_Y)*(drawGraphHeight/range_Y))]);     //get the max and min range!
    		}
    	}
    	//globalList = myList; //This acutally just points globalList to the myList array
      //So this loses the reference to list and list1

    	document.getElementById("svgLayer"+suffix).style.display = "block";
    	document.getElementById("tools_sketch"+suffix).style.display = "none";

    	drawInitialPath(globalList, globalmyPath);
}