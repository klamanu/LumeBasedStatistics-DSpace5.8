(function($){
	/*function getUrlParameter(sParam) {
		var sPageURL = decodeURIComponent(window.location.search.substring(1)),
			sURLVariables = sPageURL.split('&'),
			sParameterName,
			i;

		for (i = 0; i < sURLVariables.length; i++) {
			sParameterName = sURLVariables[i].split('=');

			if (sParameterName[0] === sParam) {
				return sParameterName[1] === undefined ? true : sParameterName[1];
			}
		}
		return false;
	};
		
	if(getUrlParameter('htmlChart')=='true'){*/	
	// substitui as imagens por charts no Stats
	/*$('p#aspect_statistics_Stats_p_').html('<div class="lumeChartStatistics">'+
		'<canvas id="myChartPerYear" width="330" height="350"></canvas>'+
	'</div>');*/
	$('p[id^="aspect_statistics_Stats"][id$="_p_"]').html('<div class="lumeChartStatistics">'+
		'<canvas id="myChartPerYear" width="330" height="350"></canvas>'+
	'</div>');

	$('[id^="aspect_statistics_Stats"][id$="_div_percountry"] p[id^="aspect_statistics_Stats"][id$="_p_dl"]:first')
		.html('<canvas id="myChartDownloadPerCountry" width="300" height="350"></canvas>');
		
	$('[id^="aspect_statistics_Stats"][id$="_div_percountry"] p[id^="aspect_statistics_Stats"][id$="_p_dl"]:last')
		.html('<canvas id="myChartAccessPerCountry" width="300" height="350"></canvas>');
	
	// substitui as imagens por charts no GeneralStats
	$('p#aspect_statistics_GeneralStats_p_dl:first')
		.append('<canvas id="generalStatsRootCommunities" width="300" height="550"></canvas>');
					
	$('p#aspect_statistics_GeneralStats_p_dl:last')
		.append('<canvas id="generalStatsRootCommunitiesByItem" width="300" height="550"></canvas>');
	
	// substitui as imagens por charts no BrowseStats
	$('p#aspect_statistics_BrowseStats_p_').html('<div class="lumeChartStatistics">'+
		'<canvas id="myChartPerYearB" width="330" height="350"></canvas>'+
	'</div>');	
	$('p#aspect_statistics_BrowseStats_p_dl:first')
		.html('<canvas id="chartBrowseStatsPerCountry" width="300" height="350"></canvas>');
	$('p#aspect_statistics_BrowseStats_p_dl:last')
		.html('<canvas id="chartBrowseStatsAccessPerCountry" width="300" height="350"></canvas>');
	
	// substitui as imagens por charts no QueryStats
	$('p#aspect_statistics_QueryStats_p_').html('<div class="lumeChartStatistics">'+
		'<canvas id="myChartPerYearC" width="330" height="350"></canvas>'+
	'</div>');	
	$('p#aspect_statistics_QueryStats_p_dl:first')
		.html('<canvas id="chartQueryStatsPerCountry" width="300" height="350"></canvas>');
	$('p#aspect_statistics_QueryStats_p_dl:last')
		.html('<canvas id="chartQueryStatsAccessPerCountry" width="300" height="350"></canvas>');
	
	// estatísticas Stats
	chartsEstatisticasPorAno(
		document.getElementById("myChartPerYear"),
		$('[id^="aspect_statistics_Stats"][id$="_div_columnsPerYear"] table[id^="aspect_statistics_Stats"][id$="_table_listResults"] tr'));
	chartsEstatisticasPorPais(
		document.getElementById("myChartDownloadPerCountry"),
		$('table[id^="aspect_statistics_Stats"][id$="_table_statsPerCountry"]:first tr'));
	chartsEstatisticasPorPais(
		document.getElementById("myChartAccessPerCountry"),
		$('table[id^="aspect_statistics_Stats"][id$="_table_statsPerCountry"]:last tr'), 
		'rgba(178, 0, 0, 0.8)');
	
	// estatísticas BrowseStats
	chartsEstatisticasPorAno(
		document.getElementById("myChartPerYearB"),
		$('#aspect_statistics_BrowseStats_div_columnsPerYear table#aspect_statistics_BrowseStats_table_listResults tr'));
	chartsEstatisticasPorPais(
		document.getElementById("chartBrowseStatsPerCountry"),
		$('table#aspect_statistics_BrowseStats_table_statsPerCountry:first tr'));
	chartsEstatisticasPorPais(
		document.getElementById("chartBrowseStatsAccessPerCountry"),
		$('table#aspect_statistics_BrowseStats_table_statsPerCountry:last tr'), 
		'rgba(178, 0, 0, 0.8)');
		
	// estatísticas QueryStats
	chartsEstatisticasPorAno(
		document.getElementById("myChartPerYearC"),
		$('#aspect_statistics_QueryStats_div_columnsPerYear table#aspect_statistics_QueryStats_table_listResults tr'));
	chartsEstatisticasPorPais(
		document.getElementById("chartQueryStatsPerCountry"),
		$('table#aspect_statistics_QueryStats_table_statsPerCountry:first tr'));
	chartsEstatisticasPorPais(
		document.getElementById("chartQueryStatsAccessPerCountry"),
		$('table#aspect_statistics_QueryStats_table_statsPerCountry:last tr'), 
		'rgba(178, 0, 0, 0.8)');
		
})(jQuery);

var ctx;
var nValue;
var columnLabel = []; // nome a ser exibido
var labels = []; // labels of axis X
var valueA = []; //  values of axis Y, stack0
var valueB = []; // values of axis Y, stack1

function chartsEstatisticasPorAno(ctx,trsTabela, bgColorA, bgColorB){
	var nValue;
	var columnLabel = []; // nome a ser exibido
	var labels = []; // labels of axis X
	var valueA = []; //  values of axis Y, stack0
	var valueB = []; // values of axis Y, stack1
	if(ctx !== null){
		if(bgColorA===undefined){
			bgColor='rgba(0, 128, 225, 0.8)';
		}
		if(bgColorB===undefined){
			bgColor='rgba(178, 0, 0, 0.8)';
		}
		// tabela por ano
		nValue = trsTabela.length;
		trsTabela.each(
		function(indexTr){
			if(indexTr === 0){
				$(this).find('td').each(function(index){
					//console.log($(this).text()+' '+index);
					if(index !== 0){
						columnLabel.push($(this).text());				
						//console.log(columnLabel[index-1] +' '+index);
					}
				});
			} else if (indexTr < (nValue-1)){
				$(this).find('td').each(function(index){
					var texto = $(this).text();
					//	console.log(texto);
					if(index === 0){
						labels.push(texto);
					} else if(index === 1){
						valueA.push(texto);
					} else if(index === 2){
						valueB.push(texto);
					}
				});
			}
		});

		//for(i = 0; i < labels.length; i++){
		//	console.log(valueB[i]);
		//}

		var myChart = new Chart(ctx, {
			type: 'bar',
			data: {
				labels: labels,
				datasets: [
				{
					label: columnLabel[0],
					data: valueA,
					backgroundColor: 'rgba(0, 128, 225, 0.8)',
					borderColor:'rgba(51,51,51,1)',
					stack: 'Stack 0',
					borderWidth: 1
				},{
					label: columnLabel[1],
					data: valueB,
					backgroundColor: 'rgba(178, 0, 0, 0.8)',
					borderColor:'rgba(30,30,30,1)',
					stack: 'Stack 1',
					borderWidth: 1
				}
				]
			},
			options: {
				scales: {
					yAxes: [{
						ticks: {
							beginAtZero:true,						
							suggestedMax: 4,
							maxTicksLimit: 8
						}
					}]
				}
			}
		});
	}	
}

// tanto para a tabela acessos como para a de downloads por país
function chartsEstatisticasPorPais(ctx,trsTabela,bgColor){
	if(ctx !== null){
		if(bgColor===undefined){
			bgColor='rgba(0, 128, 225, 0.8)';
		}
		nValue = trsTabela.length;
		var columnLabel = []; // nome a ser exibido
		var labels = []; // labels of axis X
		var valueA = []; //  values of axis Y, stack0
		trsTabela.each(
		function(indexTr){
			if (indexTr > 10){
				return false;
			} else if(indexTr === 0){
				$(this).find('td').each(function(index){
					//console.log($(this).text()+' '+index);
					if(index !== 0){
						columnLabel.push($(this).text());				
						//console.log(columnLabel[index-1] +' '+index);
					}
				});
			} else {
				$(this).find('td').each(function(index){
					var texto = $(this).text();
					//	console.log(texto);
					if(index === 0){
						labels.push(texto);
					} else if(index === 1){
						valueA.push(texto);
					}
				});
			}
		});

		/*for(i = 0; i < valueA.length; i++){
			console.log(valueA[i]);
		}*/
		
		var myChart = new Chart(ctx, {
			type: 'bar',
			backgroundColor: 'rgba(255, 255, 255, 1)',
			data: {
				labels: labels,
				datasets: [{
					label: columnLabel[0],
					data: valueA,
					backgroundColor: bgColor,
					borderColor:'rgba(51,51,51,1)',
					borderWidth: 1
				}]
			},
			options: {
				scales: {
					yAxes: [{
						ticks: {
							beginAtZero:true,
							suggestedMax: 4,
							maxTicksLimit: 8
						}
					}],
					xAxes: [{
						ticks: {
							autoSkip:false
						}
					}]
				}
			}
		});
	}
}

if(document.getElementById("generalStatsRootCommunities") !== null){
	var ctx = document.getElementById("generalStatsRootCommunities");
	// tabela por ano
	nValue = $('#aspect_statistics_GeneralStats_div_generalStatistics table#aspect_statistics_GeneralStats_table_listResults tr').length;
	var columnLabel = []; // nome a ser exibido
	var labels = []; // labels of axis X
	var valueA = []; //  values of axis Y, stack0
	var valueB = []; //  values of axis Y, stack1
	$('#aspect_statistics_GeneralStats_div_generalStatistics table#aspect_statistics_GeneralStats_table_listResults tr').each(
	function(indexTr){
		if(indexTr === 0){
			$(this).find('td').each(function(index){
				//console.log($(this).text()+' '+index);
				if(index !== 0 && index !== 1){
					columnLabel.push($(this).text());				
					//console.log(columnLabel[index-1] +' '+index);
				}
			});
		} else if (indexTr < (nValue-1)){
			$(this).find('td').each(function(index){
				var texto = $(this).text();
				//	console.log(texto);
				if(index === 0){
					labels.push(texto);
				} else if(index === 2){
					valueA.push(texto);
				} else if(index === 3){
					valueB.push(texto);
				}
			});
		}
	});

	//for(i = 0; i < labels.length; i++){
	//	console.log(valueB[i]);
	//}


	var myChart = new Chart(ctx, {
		type: 'bar',
		data: {
			labels: labels,
			datasets: [
			{
				label: columnLabel[0],
				data: valueA,
				backgroundColor: 'rgba(0, 128, 225, 0.8)',
				borderColor:'rgba(51,51,51,1)',
				stack: 'Stack 0',
				borderWidth: 1
			},{
				label: columnLabel[1],
				data: valueB,
				backgroundColor: 'rgba(178, 0, 0, 0.8)',
				borderColor:'rgba(30,30,30,1)',
				stack: 'Stack 1',
				borderWidth: 1
			}
			]
		},
		options: {
			scales: {
				yAxes: [{
					ticks: {
						beginAtZero:true,						
						suggestedMax: 4,
						maxTicksLimit: 8
					}
				}],
				xAxes: [{
					ticks: {
						autoSkip:false,
						minRotation: 90
					}
				}]
			}
		}
	});
}

if(document.getElementById("generalStatsRootCommunitiesByItem") !== null){
	var ctx = document.getElementById("generalStatsRootCommunitiesByItem");
	// tabela por ano
	nValue = $('#aspect_statistics_GeneralStats_div_generalStatistics table#aspect_statistics_GeneralStats_table_listResults tr').length;
	var columnLabel = []; // nome a ser exibido
	var labels = []; // labels of axis X
	var valueA = []; //  values of axis Y, stack0
	var valueB = []; //  values of axis Y, stack1
	var itemCount = 1;
	$('#aspect_statistics_GeneralStats_div_generalStatistics table#aspect_statistics_GeneralStats_table_listResults tr').each(
	function(indexTr){
		if(indexTr === 0){
			$(this).find('td').each(function(index){
				//console.log($(this).text()+' '+index);
				if(index !== 0 && index !== 1){
					columnLabel.push($(this).text());				
					//console.log(columnLabel[index-1] +' '+index);
				}
			});
		} else if (indexTr < (nValue-1)){
			$(this).find('td').each(function(index){
				var texto = $(this).text();
				//	console.log(texto);
				if(index === 0){
					labels.push(texto);
				} else if(index === 1){
					itemCount = texto;
				} else if(index === 2){
					valueA.push(Math.round(texto/itemCount));
				} else if(index === 3){
					valueB.push(Math.round(texto/itemCount));
				}
			});
		}
	});

	//for(i = 0; i < labels.length; i++){
	//	console.log(valueB[i]);
	//}


	var myChart = new Chart(ctx, {
		type: 'bar',
		data: {
			labels: labels,
			datasets: [
			{
				label: columnLabel[0],
				data: valueA,
				backgroundColor: 'rgba(0, 64, 196, 0.8)',
				borderColor:'rgba(51,51,51,1)',
				stack: 'Stack 0',
				borderWidth: 1
			},{
				label: columnLabel[1],
				data: valueB,
				backgroundColor: 'rgba(255, 127, 0, 0.8)',
				borderColor:'rgba(30,30,30,1)',
				stack: 'Stack 1',
				borderWidth: 1
			}
			]
		},
		options: {
			scales: {
				yAxes: [{
					ticks: {
						beginAtZero:true,						
						suggestedMax: 4,
						maxTicksLimit: 8
					}
				}],
				xAxes: [{
					ticks: {
						autoSkip:false,
						minRotation: 90
					}
				}]
			}
		}
	});
}