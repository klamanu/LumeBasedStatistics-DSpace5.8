/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.statistics;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.*;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.statistics.ChartGenerator;
import org.dspace.app.xmlui.statistics.Country;
import org.dspace.app.xmlui.statistics.GeneralFunctions;
import org.dspace.app.xmlui.statistics.StringUtils;
import org.dspace.app.xmlui.statistics.Tuple;
import org.dspace.app.xmlui.aspect.discovery.DiscoveryUIUtils;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Cell;
import org.dspace.app.xmlui.wing.element.Composite;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Para;
import org.dspace.app.xmlui.wing.element.Row;
import org.dspace.app.xmlui.wing.element.Select;
import org.dspace.app.xmlui.wing.element.Table;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.browse.BrowseInfo;
import org.dspace.content.DCDate;
import org.dspace.discovery.SearchUtils;
import org.dspace.discovery.*;
import org.dspace.statistics.ObjectCount;
import org.dspace.statistics.SolrLogger;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.core.Constants;
import org.dspace.content.Item;
import org.dspace.content.DSpaceObject;
import org.dspace.handle.HandleManager;
import org.dspace.search.QueryResults;
import org.xml.sax.SAXException;

public class QueryStats extends AbstractDSpaceTransformer 
{
	private final static Logger log = Logger.getLogger(QueryStats.class);
	 
	private BrowseInfo browseInfo;
	private Request request;
	private DiscoverResult queryResults;
	private DiscoverQuery queryArgs;
	private DSpaceObject dso;
	private TreeMap<String,String> idHandleList;
	private TreeMap<String, String> years;
	private SortedMap<String, String> subYears;
	private Map<String, String> params;
	private String query = "";
	private String stats_type = "";
	private String showAllCountries = "";
	private String[] dates;
	
	private static final Message T_top_downloads = message("xmlui.Acessos.stats.topDownloads");
	private static final Message T_head_nodownloads = message("xmlui.statistics.visits.nodownloads");	
	private static final Message T_head_backlink = message("xmlui.Acessos.stats.linkParaVoltar");
	private static final Message T_item_downloads = message("xmlui.Acessos.stats.itemDownloads");
	private static final Message T_item_date_accessioned = message("xmlui.Acessos.stats.accessioned");
	private static final Message T_dspace_home = message("xmlui.general.dspace_home");
	private static final Message T_bottom_downloads = message("xmlui.Acessos.stats.bottomDownloads");
	private static final Message T_item_downloads_and_date = message("xmlui.Acessos.stats.itemDownloadsAndDate");
	private static final HashMap hashMapCountryCodeToIndex = new HashMap(512);
	
	public void addPageMeta(PageMeta pageMeta) throws SAXException, WingException, UIException,
            SQLException, IOException, AuthorizeException 
	{	 
		 pageMeta.addMetadata("title").addContent(message("xmlui.administrative.Navigation.statistics"));
		 pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
		 pageMeta.addTrail().addContent(message("xmlui.administrative.Navigation.statistics"));		 	
	}
	
	public void addBody(Body body) throws SAXException, WingException, UIException, SQLException,
            IOException, AuthorizeException 
	{
		Division top = body.addDivision("head");
        top.setHead(message("xmlui.Acessos.stats.title"));
		top.addPara("coleta", null).addContent(message("xmlui.Acessos.stats.coleta"));
        
		Division current = body.addDivision("current");
		init();
		collectHandles();
		buildLinkToDiscover(current);
		buildDownloadLists(current, getDownloadItems());
		buildFilterOptions(current);
				
		try
		{
			Division divRes = current.addDivision("results");
			Division perYear = divRes.addDivision("columnsPerYear");
			
			Map<String,String> downloadStatistics = collectData("DOWNLOADS");
			Map<String,String> viewsStatistics = collectData("ACESSOS");
			
			buildStatsTable(downloadStatistics, viewsStatistics, perYear);

			perYear.addPara("", null);
		
			for (int i = 0; i < 254;i++)
			{
				hashMapCountryCodeToIndex.put(Country.countryCode[i], new Integer(i));     
			}
			
			// Andre: adiciona para separar dos elementos com float.
			Division blockDiv = divRes.addDivision("blockDiv","clear");	
				
			Division countryDiv = divRes.addDivision("countryTable");
			
			ArrayList downloadStatisticsPerCountry = collectCountryData("DOWNLOADS");
			ArrayList viewsStatisticsPerCountry = collectCountryData("ACESSOS");	
			
			// Guilherme: Divisões para colocar as tabelas em duas colunas
			if(!downloadStatisticsPerCountry.isEmpty()){
				Division  firstColumn = countryDiv.addDivision("firstColumn","first-column");
				buildCountryTable(downloadStatisticsPerCountry, firstColumn,
					"xmlui.Acessos.stats.dlperCountry", "xmlui.Acessos.stats.downloads");
			}
			if(!viewsStatisticsPerCountry.isEmpty()){
				Division  secondColumn = countryDiv.addDivision("firstColumn","second-column");
				buildCountryTable(viewsStatisticsPerCountry, secondColumn, 
					"xmlui.Acessos.stats.acperCountry", "xmlui.Acessos.stats.views");
			}			
			Division perCountry = divRes.addDivision("percountry");
			if(!downloadStatisticsPerCountry.isEmpty()){
				perCountry.addPara("dl",null);
			}
			if(!viewsStatisticsPerCountry.isEmpty()){
				perCountry.addPara("dl",null);
			}
			
			// Guilherme: adiciona para separar dos elementos com float.
			Division finalMessage = current.addDivision("finalMessage","clear");
			//finalMessage.addPara(message("xmlui.Acessos.stats.note"));
				
			buildLinkToShowAllCountries(current);		
		}
		catch (IllegalArgumentException iae)
		{}		
	}
	
	private void init() throws SQLException, UnsupportedEncodingException 
	{
		request = ObjectModelHelper.getRequest(objectModel);
		dso = HandleUtil.obtainHandle(objectModel);
		idHandleList = new TreeMap<String,String>();
		query = request.getParameter("query") == null ? "" : URLDecoder.decode(request.getParameter("query"), "UTF-8");
		stats_type = request.getParameter("stats_type");
		params = new HashMap<String,String>();
		if ("".equals(stats_type) || stats_type == null) 
		{
			stats_type = "0";
		}
		dates = new String[4];
		dates[0] = request.getParameter("year1");
		dates[1] = request.getParameter("month1");
		dates[2] = request.getParameter("year2");
		dates[3] = request.getParameter("month2");
		showAllCountries = request.getParameter("showAll");
		if ("".equals(showAllCountries) || showAllCountries == null) 
		{
			showAllCountries = "0";
		}
	}
	
	private void collectHandles() 
	{
		try 
		{
			QueryResults qResults = null;
			String scope = request.getParameter("scope");
			if (scope != null)
			{
				params.put("scope", scope);
			}

			/*** faz a busca ***/
			DSpaceObject dso_scope;
			if (scope == null || "".equals(scope))
			{
				// get the search scope from the url handle
				dso_scope = HandleUtil.obtainHandle(objectModel);
			}
			else
			{
				// Get the search scope from the location parameter
				dso_scope = HandleManager.resolveToObject(context, scope);
			}
			
			this.queryArgs = new DiscoverQuery();
			
			queryArgs.setQuery(query != null && !query.trim().equals("") ? query : null);
			queryArgs.setDSpaceObjectFilter(Constants.ITEM);
			queryArgs.setMaxResults(Integer.MAX_VALUE);
			queryArgs.addFilterQueries(DiscoveryUIUtils.getFilterQueries(request, context));
			
			this.queryResults = SearchUtils.getSearchService().search(context, dso_scope, queryArgs, false);
			java.util.List<DSpaceObject> results = this.queryResults.getDspaceObjects();
			for (DSpaceObject dso : results) 
			{
				if (dso instanceof Item)
				{
					idHandleList.put(String.valueOf(dso.getID()), String.valueOf(dso.getHandle()));
					//log.debug(dso.getHandle() + " -- " + dso.getID());
				}
            }
		} 
		catch (Exception ex) 
		{
			log.error("Unable to process collectHandles()", ex);
		}
	}

	private void buildLinkToDiscover(Division currentDiv) throws WingException, UnsupportedEncodingException 
	{
		currentDiv.addPara().addXref(generateURLfromParameters(), 
			message("xmlui.Acessos.stats.consulta_sem_param").parameterize(query));
		currentDiv.addPara().addContent(T_head_backlink);
	}
	
	private String generateURLfromParameters() throws UnsupportedEncodingException
	{
        return super.generateURL(contextPath + (dso == null ? "" : "/handle/" + dso.getHandle()) + "/discover", getParams(false));
	}
	
	private void buildDownloadLists(Division currentDiv, TreeMap<String, String> download_list) 
		throws WingException, SQLException 
	{
		String title = "";
		
		currentDiv.addPara(T_top_downloads);
		Division topDiv = currentDiv.addDivision("top");
		List topList = topDiv.addList("browseTop");
		
		Iterator iterador = GeneralFunctions.sortByValue(download_list, -1).iterator();
		int counter = 0;	
		for(Iterator iter = iterador; iter.hasNext() && counter < 5;) 
		{
			try 
			{
				String key = (String) iter.next();
				Item item = Item.find(context, Integer.parseInt(key.substring(key.indexOf(":")+1)));
				title = (item.getName() == "" ? "Sem título" : item.getName());
				DCDate dcDate = new DCDate(item.getMetadata("dc.date.accessioned"));
				topList.addItemXref("/handle/" + item.getHandle(), title); 
				topList.addItem(T_item_downloads_and_date.parameterize(download_list.get(key), 
					dcDate.getDay() + "/" + dcDate.getMonth() + "/" + dcDate.getYear()));
            } 
			catch (NullPointerException e) 
			{
                log.error(e.toString());
			}
			counter++;
        }
		
		
		if(download_list.size() == 0) 
		{
			currentDiv.addPara(T_head_nodownloads);
		}
		
		currentDiv.addPara(T_bottom_downloads);
		Division lastDiv = currentDiv.addDivision("last");
		List lastList = lastDiv.addList("browseLast");
		
		iterador = GeneralFunctions.sortByValue(download_list, 1).iterator();
		counter = 0;
		for(Iterator iter = iterador; iter.hasNext() && counter < 5;) 
		{
			try 
			{
				String key = (String) iter.next();
				Item item = Item.find(context, Integer.parseInt(key.substring(key.indexOf(":")+1)));
				title = (item.getName() == "" ? "Sem título" : item.getName());
				DCDate dcDate = new DCDate(item.getMetadata("dc.date.accessioned"));
				lastList.addItemXref("/handle/" + item.getHandle(), title); 
				lastList.addItem(T_item_downloads_and_date.parameterize(download_list.get(key), 
					dcDate.getDay() + "/" + dcDate.getMonth() + "/" + dcDate.getYear()));
			}
			catch (NullPointerException e) 
			{
				log.error(e.toString());
			}
			counter++;
		}
		
		if(download_list.size() == 0) 
		{
			currentDiv.addPara(T_head_nodownloads);
		}	
	}
	
	private TreeMap<String,String> getDownloadItems() 
	{
    	TreeMap<String,String> result = new TreeMap<String,String>();
		try 
		{
			StringBuilder solrQuery = new StringBuilder();
			java.util.List<String> idOrList = StringUtils.buildArraySolrIdList("DOWNLOADS", idHandleList);
			java.util.List<String> idList;
			if(!idOrList.isEmpty())
			{
				for(String ids : idOrList)
				{
					idList = new ArrayList<String>(Arrays.asList(ids.substring(1,ids.length()-1).split(" OR ")));
					solrQuery.append(ids);
					solrQuery.append(" AND statistics_type:view AND type:0 AND bundleName:ORIGINAL");
					
					Map<String,Integer> solrQueryResult = SolrLogger.queryFacetQuery(solrQuery.toString(), 
						StringUtils.buildSolrDateFilter(dates, stats_type), idList);
					
					for (Map.Entry<String,Integer> entry : solrQueryResult.entrySet()) 
					{
						result.put(entry.getKey(), 
							result.containsKey(entry.getKey())
							? String.valueOf(Integer.valueOf(result.get(entry.getKey())) + entry.getValue())
							: entry.getValue().toString());
						log.debug(entry.getKey() + " " + entry.getValue());
					}
					solrQuery.delete(0, solrQuery.length());
				}
			}
		} 
		catch (Exception e) 
		{
			log.error("Error occurred while creating statistics for query stats.", e);
		} 
		return result;		
    }
	
	private void buildFilterOptions(Division currentDiv) throws WingException, UnsupportedEncodingException 
	{
		Para para = currentDiv.addPara("agrupar", null);
		para.addContent(message("xmlui.Acessos.stats.agrupar"));

		String urlByYear =  contextPath + "/querystats";
		String urlByMonth = contextPath + "/querystats";
		
		para.addXref(super.generateURL(urlByYear, getParams(true)) + "&stats_type=0", message("xmlui.Acessos.stats.agruparporano"));
		para.addXref(super.generateURL(urlByMonth, getParams(true)) + "&stats_type=1", message("xmlui.Acessos.stats.agruparpormes"));	
			
		try 
		{
			Comparator intComp = GeneralFunctions.integerComparator(); 
			if (stats_type == null) 
			{
				stats_type = "0";
			}

			if (stats_type.equals("1")) 
			{
				years = new TreeMap<String, String>(intComp);
			} 
			else 
			{
				years = new TreeMap<String, String>();	
			}
			
			String date1 = new String();
			String date2 = new String();
						
			Division query = currentDiv.addInteractiveDivision("stats-filter", super.generateURL(contextPath + "/querystats", 
				getParams(false)) + "&stats_type=" + stats_type, Division.METHOD_POST, "browse stats");
					
			List list = query.addList("filter-search");
			list.setHead(message("xmlui.Acessos.stats.filterDate"));
			list.addLabel(message("xmlui.Acessos.stats.data1"));
			//Adicionando o campo composto data1
			Composite dataComp = list.addItem().addComposite("datainicial", "data1");
			//Ano
			Select year1 = dataComp.addSelect("year1");
			year1.setLabel(message("xmlui.Acessos.stats.year"));
			year1.setMultiple(false);
			year1.setSize(1);
			year1.addOption(true, "").addContent(message("xmlui.FiltroDeBusca.option.all"));
			Calendar rightNow = Calendar.getInstance();        
			if (stats_type.equals("0")) 
			{
				for(int i = 2008; i <= rightNow.get(Calendar.YEAR); i++) 
				{
					year1.addOption((String.valueOf(i).equals(dates[0]) ? true : false), String.valueOf(i)).addContent(String.valueOf(i));        
					years.put(String.valueOf(i), String.valueOf(i));
				}
			} 
			else 
			{
				for(int i = 2008; i <= rightNow.get(Calendar.YEAR); i++) 
				{
					year1.addOption((String.valueOf(i).equals(dates[0]) ? true : false), String.valueOf(i)).addContent(String.valueOf(i));        
					for (int j = 1; j <= 12; j++) 
					{
						years.put(String.valueOf(i) + StringUtils.pad(String.valueOf(j), 2, 0), String.valueOf(i) + 
							StringUtils.pad(String.valueOf(j), 2, 0));
					}
				}
			}			
			//Mes
			Select month1 = dataComp.addSelect("month1");
			month1.setLabel(message("xmlui.Acessos.stats.month"));
			month1.setMultiple(false);
			month1.setSize(1);
			month1.addOption(true, "").addContent(message("xmlui.FiltroDeBusca.option.all"));
			for(int i = 1; i <= 12; i++) 
			{
				month1.addOption((String.valueOf(i).equals(dates[1]) ? true : false), String.valueOf(i)).addContent(String.valueOf(i));
			}
			list.addLabel(message("xmlui.Acessos.stats.data2"));
			//Adicionando campo composto data2
			Composite dataComp2 = list.addItem().addComposite("datafinal", "data2");
			//Ano
			Select year2 = dataComp2.addSelect("year2");
			year2.setLabel(message("xmlui.Acessos.stats.year"));
			year2.setMultiple(false);
			year2.setSize(1);
			year2.addOption(true, "").addContent(message("xmlui.FiltroDeBusca.option.all"));      
			for(int i = 2008; i <= rightNow.get(Calendar.YEAR); i++) 
			{
				year2.addOption((String.valueOf(i).equals(dates[2]) ? true : false), String.valueOf(i)).addContent(String.valueOf(i));        
			}
			//Mes
			Select month2 = dataComp2.addSelect("month2");
			month2.setLabel(message("xmlui.Acessos.stats.month"));
			month2.setMultiple(false);
			month2.setSize(1);
			month2.addOption(true,"").addContent(message("xmlui.FiltroDeBusca.option.all"));
			for(int i = 1; i <= 12; i++) 
			{
				month2.addOption((String.valueOf(i).equals(dates[3]) ? true : false), String.valueOf(i)).addContent(String.valueOf(i));       
			}
			query.addPara(null, "button-list").addButton("submit").setValue(message("xmlui.general.go"));
			
			//se os anos nao estiverem vazios
			if (!StringUtils.getSdateFilter(0, dates).equals("xx") && !StringUtils.getSdateFilter(2, dates).equals("xx")) 
			{
				date1 = StringUtils.getSdateFilter(0, dates) + StringUtils.pad(StringUtils.getSdateFilter(1, dates),2,0); 
				date2 = StringUtils.getSdateFilter(2, dates) + StringUtils.pad(StringUtils.getSdateFilter(3, dates),2,0);
				
				if (date1.compareTo(date2) > 0) 
				{
					throw new IllegalArgumentException(date1 + " - " + date2);
				}
			}
		} 
		catch (IllegalArgumentException iae) 
		{
			currentDiv.addPara(message("xmlui.Acessos.stats.invalid"));
			currentDiv.addPara(iae.getMessage());
		}
	}
	
	private Map<String,String> getParams(boolean withDate) throws UnsupportedEncodingException 
	{
		Map<String,String> params = new HashMap<String,String>();
		
		params.put("query", URLEncoder.encode(query, "UTF-8"));
		params.put("showAll", showAllCountries);
		
		if(withDate)
		{
			if ((dates[0] != null) && !"".equals(dates[0])) 
			{
				params.put("year1", dates[0]);
			}
			if ((dates[1] != null) && !"".equals(dates[1])) 
			{
				params.put("month1", dates[1]);
			}
			if ((dates[2] != null) && !"".equals(dates[2])) 
			{
				params.put("year2", dates[2]);
			}
			if ((dates[3] != null) && !"".equals(dates[3])) 
			{
				params.put("month2", dates[3]);
			}
		}
		
		java.util.List<String> filterTypes = DiscoveryUIUtils.getRepeatableParameters(request, "filtertype");
        java.util.List<String> filterOperators = DiscoveryUIUtils.getRepeatableParameters(request, "filter_relational_operator");
        java.util.List<String> filterValues = DiscoveryUIUtils.getRepeatableParameters(request, "filter");

        for (int i = 0; i < filterTypes.size(); i++) 
		{
            String filterType = filterTypes.get(i);
            String filterValue = filterValues.get(i);
            String filterOperator = filterOperators.get(i);

            params.put("filtertype_" + i, filterType);
            params.put("filter_relational_operator_" + i, filterOperator);
            params.put("filter_" + i, URLEncoder.encode(filterValue, "UTF-8"));
        }
		
		return params;
	}

	private void buildStatsTable(Map<String,String> downloadStatistics, Map<String,String> viewsStatistics, 
		Division currentDiv) throws WingException, IllegalArgumentException 
	{
		Table table = currentDiv.addTable("listResults", downloadStatistics.size() + 1, 3);
			
		Comparator intComp = GeneralFunctions.integerComparator(); 
		if (stats_type.equals("1")) 
		{
			subYears = new TreeMap<String,String>(intComp);
		} 
		else 
		{
			subYears = new TreeMap<String,String>();
		}
		
		if (stats_type.equals("0")) 
		{
			if ((StringUtils.nullify(dates[0]) == null) && (StringUtils.nullify(dates[2]) != null)) 
			{
				subYears = years.headMap(dates[2] + "\0");
			} 
			else if ((StringUtils.nullify(dates[0]) != null) && (StringUtils.nullify(dates[2]) == null)) 
			{
				subYears = years.tailMap(dates[0]);
			} 
			else if ((StringUtils.nullify(dates[0]) != null) && (StringUtils.nullify(dates[2]) != null)) 
			{
				subYears = years.subMap(dates[0], dates[2] + "\0");
			} 
			else 
			{
				subYears = years;
			} 
		} 
		else 
		{
			if ((StringUtils.nullify(dates[0]) == null) && (StringUtils.nullify(dates[2]) != null)) 
			{
				subYears = years.headMap(dates[2] + StringUtils.pad(StringUtils.getSdate(3,dates),2,0));
			} 
			else if ((StringUtils.nullify(dates[0]) != null) && (StringUtils.nullify(dates[2]) == null)) 
			{
				subYears = years.tailMap(dates[0] + StringUtils.pad(dates[1],2,0));
			} 
			else if ((StringUtils.nullify(dates[0]) != null) && (StringUtils.nullify(dates[2]) != null)) 
			{
				subYears = years.subMap(dates[0] + StringUtils.pad(StringUtils.getSdate(1,dates),2,0), 
					dates[2] + StringUtils.pad(StringUtils.getSdate(3,dates),2,0) + "\0");
			} 
			else 
			{
				subYears = years;
			}
		}
		
		if (subYears.isEmpty()) 
		{	
			Row header = table.addRow();
			throw new IllegalArgumentException(dates[0] + StringUtils.pad(dates[1],2,0) + " - " + 
				dates[2] + StringUtils.pad(StringUtils.getSdate(3,dates),2,0));  //MANUELA
		}
		
		if (stats_type.equals("0")) 
		{
			table.setHead(message("xmlui.Acessos.stats.perYear"));
		} 
		else 
		{
			table.setHead(message("xmlui.Acessos.stats.perMonth"));
		}
		
		Row header = table.addRow();
		header.addCellContent(message("xmlui.Acessos.stats.year"));
		header.addCellContent(message("xmlui.Acessos.stats.downloads"));
		header.addCellContent(message("xmlui.Acessos.stats.views"));
		
		Integer totalViews = 0;
		Integer totalDownloads = 0;
		
		//Para cada data, adiciona na table 
		//Se for ano/mes, adiciona a barra ("/") para separar		
		for (Iterator iter = subYears.entrySet().iterator(); iter.hasNext();) 
		{ 
			Map.Entry entry = (Map.Entry)iter.next();
			String key = (String)entry.getKey();
			String value = (String)entry.getValue();
			Row row = table.addRow();
			
			//Se for ano/mes
			if (stats_type.equals("1")) 
			{
				row.addCellContent(new String(key.substring(4,6) + "/" + key.substring(0,4)));
			} 
			else 
			{
				row.addCellContent(key);
			}
			
			//Puxa o valor do map data correspondente à data "key"
			String count = downloadStatistics.get(key);
			if (count == null) 
			{
				count = "0";
			} 
			
			row.addCellContent(count);
			totalViews += new Integer(count);
			
			count = viewsStatistics.get(key);
			if (count == null) 
			{
				count = "0";
			}
			
			row.addCellContent(count);
			totalDownloads += new Integer(count);
		}
		
		Row row = table.addRow();
		row.addCellContent("Total");
		row.addCellContent(totalViews.toString());
		row.addCellContent(totalDownloads.toString());
	}
	
	private TreeMap<String,String> collectData(String accessType) 
		throws SQLException, WingException 
	{    
        TreeMap<String,String> result = new TreeMap<String,String>();     
		ObjectCount[] statisticsCount = null;	
		String facetField = "time_ano";
		// per month
		if (stats_type.equals("1")) 
		{
			facetField = "time_anomes";
		} 
		try 
		{
			for(String query : StringUtils.buildArraySolrQuery(accessType, false, idHandleList))
			{
				statisticsCount = SolrLogger.queryFacetField(query, StringUtils.buildSolrDateFilter(dates, stats_type), 
					facetField, Integer.MAX_VALUE, false, null);
								
				for(int i = 0; i < statisticsCount.length; i++) 
				{
					result.put(statisticsCount[i].getValue().replace("-",""), 
							result.containsKey(statisticsCount[i].getValue().replace("-",""))
							? String.valueOf(Integer.parseInt(result.get(statisticsCount[i].getValue().replace("-",""))) + (int)statisticsCount[i].getCount())
							: Long.toString(statisticsCount[i].getCount()));
				}
			}
			
			/*statisticsCount = SolrLogger.queryFacetField(StringUtils.buildSolrQuery(accessType, false, idHandleList, dates, stats_type), 
				"", facetField, Integer.MAX_VALUE, false, null);	
				
			for(int i = 0; i < statisticsCount.length; i++) 
			{
				result.put(statisticsCount[i].getValue().replace("-",""), Long.toString(statisticsCount[i].getCount()));
			}*/
		}
		catch (Exception e) 
		{
			log.error("Error occurred while creating statistics in (collectData) QueryStats.java", e);
		}
		return result;
    }
	
	private ArrayList collectCountryData(String type) throws SQLException, WingException 
	{
		ArrayList orderedResult = new ArrayList();	
		ArrayList top10Result = new ArrayList();	
		Map<String,Integer> result = new TreeMap<String,Integer>();     
		
		try 
		{
			for(String query : StringUtils.buildArraySolrQuery(type, false, idHandleList))
			{
				ObjectCount[] downloadsPerCountry = SolrLogger.queryFacetField(query, StringUtils.buildSolrDateFilter(dates, stats_type), 
					"countryCode", showAllCountries.equals("0") ? 10 : Integer.MAX_VALUE, false, null);
/*				
				long totalUFRGS = SolrLogger.queryTotal(query  + " AND (ip:143.54.*.* OR ip:" + StringUtils.IPV6_UFRGS_PATTERN + ")", 
					StringUtils.buildSolrDateFilter(dates, stats_type)).getCount();
*/
				long totalUFRGS= 0;				
				for(int i = 0; i < downloadsPerCountry.length; i++) 
				{
					result.put(downloadsPerCountry[i].getValue(), 
							result.containsKey(downloadsPerCountry[i].getValue())
							? result.get(downloadsPerCountry[i].getValue()) + (int)downloadsPerCountry[i].getCount()
							: (int)downloadsPerCountry[i].getCount());
				}
				// add ufrgs internal statistics
				if (totalUFRGS > 0) 
				{
					result.put("UFRGS", 
							result.containsKey("UFRGS")
							? result.get("UFRGS") + (int)totalUFRGS
							: (int)totalUFRGS);
					//resultados da ufrgs estao também no brasil
					/*result.put("BR", 
							result.containsKey("BR")
							? result.get("BR") + (int)totalUFRGS
							: (int)totalUFRGS);*/
				}
			}
			
			for (Map.Entry<String, Integer> entry  : entriesSortedByValues(result)) {
				orderedResult.add(new Tuple(entry.getKey(), String.valueOf(entry.getValue())));
			}
			
			Collections.reverse(orderedResult);
			// restore top10
			if (showAllCountries.equals("0") && orderedResult.size() >= 11) 
			{
				for (int i = 0; i < orderedResult.size(); i++) 
				{		
					if (i < 10)
					{
						top10Result.add(orderedResult.get(i));
					}
				}
				return top10Result; 
			}
		} 
		catch (Exception e) 
		{
			log.error("Error occurred while creating statistics in collectCountryData BrowseStats.java", e);
		}
		
		return orderedResult; 
	}
	
	static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>>entriesSortedByValues(Map<K,V> map) 
	{
		SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
				new Comparator<Map.Entry<K,V>>() {
					@Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
						int res = e1.getValue().compareTo(e2.getValue());
						return res != 0 ? res : 1;
						//return e1.getValue().compareTo(e2.getValue());
					}
				});
		sortedEntries.addAll(map.entrySet());
		return sortedEntries;
	}
	
	private void buildCountryTable(ArrayList statisticsPerContry, Division countryDiv, 
		String tableTitleMessage, String tableHeaderMessage) throws WingException, UnsupportedEncodingException 
	{
		Iterator iterador;
			
		//Seta o iterador para os downloads por país
		iterador = statisticsPerContry.iterator();
 
		Table table = countryDiv.addTable("statsPerCountry", statisticsPerContry.size() + 1, 2);
		table.setHead(message(tableTitleMessage));
		Row header = table.addRow();
		header.addCellContent(message("xmlui.Acessos.stats.country"));
		header.addCellContent(message(tableHeaderMessage));
	
		//Percorre os dados de DOWNLOADS POR PAÍS
		for(Iterator iter = iterador; iter.hasNext();) 
		{
			try 
			{    			
				Tuple value = (Tuple) iter.next();
				Row row = table.addRow();
				Cell cell = row.addCell();
				cell.addFigure(contextPath + "/static/flags/" + value.getKey().toLowerCase() + ".png", null, null);
				Integer i = (Integer) hashMapCountryCodeToIndex.get(value.getKey().toUpperCase());
				if (i == null) 
				{
					throw new NullPointerException("i is null, country not found!");
				}
				//cell.addContent("  " + Country.countryName[i.intValue()]);
				cell.addContent("  ");
                cell.addContent(message(Country.COUNTRY_KEY_TO_TRANSLATE + value.getKey().toUpperCase()));
				if (!"".equals(value.getValue()) && value.getValue() != null) 
				{
					row.addCellContent(new String(value.getValue().getBytes("UTF-8")));
				} 
				else 
				{
					row.addCellContent("0");                            
				}
			} 
			catch (NullPointerException e) 
			{
				log.error(e.toString());
			}
		}
	}
	
	private void buildLinkToShowAllCountries(Division currentDiv) throws WingException, UnsupportedEncodingException 
	{
		String urlShowAll = super.generateURL(contextPath + "/querystats", getParams(true)) + "&stats_type=" + stats_type;
		
		if (showAllCountries.equals("0")) 
		{
			currentDiv.addPara().addXref(urlShowAll.replace("showAll=0","showAll=1"), 
				message("xmlui.Acessos.stats.vertodosospaises"));
		} 
		else 
		{
			currentDiv.addPara().addXref(urlShowAll.replace("showAll=1","showAll=0"), 
				message("xmlui.Acessos.stats.naovertodosospaises"));
		}
	}
}
