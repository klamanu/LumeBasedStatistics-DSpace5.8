/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.statistics;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ArrayIndexOutOfBoundsException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.statistics.Country;
import org.dspace.app.xmlui.statistics.GeneralFunctions;
import org.dspace.app.xmlui.statistics.StringUtils;
import org.dspace.app.xmlui.statistics.Tuple;
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
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.statistics.ObjectCount;
import org.dspace.statistics.SolrLogger;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.DSpaceObject;
import org.xml.sax.SAXException;

public class Stats extends AbstractDSpaceTransformer 
{		
    private String showAllCountries;
	private String type;
	private String[] dates;
	private Request request; 
	private DSpaceObject dso;	
    private final static HashMap hashMapCountryCodeToIndex = new HashMap(512);
	private static final Logger log = Logger.getLogger(Stats.class);
	private TreeMap<String, String> years;
	private SortedMap<String,String> subYears;
	
    public void addPageMeta(PageMeta pageMeta) throws SAXException, WingException, 
		UIException, SQLException, IOException, AuthorizeException 
	{
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if (dso instanceof Community) 
		{
			// Set up the major variables
			Community community = (Community) dso;
			// Set the page title
			pageMeta.addMetadata("title").addContent(message("xmlui.title.comm.handle_10183_" + 
				community.getHandle().substring(community.getHandle().indexOf("/") + 1)));
			// Add the trail back to the repository root.
			pageMeta.addTrailLink(contextPath + "/", message("xmlui.general.dspace_home"));
			HandleUtil.buildHandleTrail(community, pageMeta, contextPath);
        } 
		else if (dso instanceof Collection) 
		{
			// Set up the major variables
			Collection collection = (Collection) dso;
			// Set the page title
			pageMeta.addMetadata("title").addContent(message("xmlui.title.comm.handle_10183_" + 
				collection.getHandle().substring(collection.getHandle().indexOf("/") + 1)));
			// Add the trail back to the repository root.
			pageMeta.addTrailLink(contextPath + "/", message("xmlui.general.dspace_home"));
			HandleUtil.buildHandleTrail(collection, pageMeta, contextPath);
		} 
		else if (dso instanceof Item) 
		{
            Item item = (Item) dso;
            pageMeta.addMetadata("title").addContent(getItemTitle(item));
            pageMeta.addTrailLink(contextPath + "/",message("xmlui.general.dspace_home"));
            HandleUtil.buildHandleTrail(item, pageMeta, contextPath);
        }
        pageMeta.addTrail().addContent(message("xmlui.Acessos.stats.title"));   
    }

	public void addBody(Body body) throws SAXException, WingException, UIException, 
		SQLException, IOException, AuthorizeException 
	{ 
		Division top = body.addDivision("head");
        top.setHead(message("xmlui.Acessos.stats.title"));
		top.addPara("collect", null).addContent(message("xmlui.Acessos.stats.coleta"));
		
		Division current = body.addDivision("current");
		
		init();
		buildLinkToItem(current);
		buildFilterOptions(current);
		
		Division divRes = current.addDivision("results");
		
		if (dates[0] != null && dates[2] != null && !dates[0].equals("") && !dates[2].equals("") && 
			(dates[0] + StringUtils.pad(dates[1],2,0)).compareTo(dates[2] + StringUtils.pad(dates[3],2,0)) > 0) {
			return;
		}
		
		Map<String,String> downloadStatistics = collectData("DOWNLOADS");
		Map<String,String> viewsStatistics = collectData("ACESSOS");
			
		Division perYear = divRes.addDivision("columnsPerYear");
		
		buildStatsTable(downloadStatistics, viewsStatistics, perYear);
		//buildStatsChart(downloadStatistics, viewsStatistics, perYear);
        perYear.addPara("", null);
		
		for (int i = 0; i < 254;i++) 
		{
			hashMapCountryCodeToIndex.put(Country.countryCode[i], new Integer(i));     
        }
		
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
	
	private void init() throws SQLException, UnsupportedEncodingException 
	{
		request = ObjectModelHelper.getRequest(objectModel);
		dso = HandleUtil.obtainHandle(objectModel);
		type = request.getParameter("type");
		if ("".equals(type) || type == null) 
		{
			type = "0";
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
	
	private void buildLinkToItem(Division currentDivision) throws WingException, UnsupportedEncodingException
	{
		if (dso instanceof Community) 
		{
			Community community = (Community) dso;
			currentDivision.addPara().addXref(contextPath + "/handle/" + dso.getHandle(),
				message("xmlui.title.comm.handle_10183_" + 
				community.getHandle().substring(community.getHandle().indexOf("/") + 1)));
        } 
		else if (dso instanceof Collection) 
		{
            Collection collection = (Collection) dso;
            currentDivision.addPara().addXref(contextPath + "/handle/" + dso.getHandle(),
				message("xmlui.title.comm.handle_10183_" + 
				collection.getHandle().substring(collection.getHandle().indexOf("/") + 1)));
        } 
		else if (dso instanceof Item) 
		{
            Item item = (Item) dso;
            currentDivision.addPara().addXref(contextPath + "/handle/" + dso.getHandle(), getItemTitle(item));
        }
	}
	
	private void buildFilterOptions(Division currentDiv) throws WingException, UnsupportedEncodingException 
	{
		Para para = currentDiv.addPara("group", null);
		para.addContent(message("xmlui.Acessos.stats.agrupar"));
		
		String url = contextPath + "/handle/" + dso.getHandle();
		String tailUrl = "";
		
		tailUrl += "&year1=" + getSdate(0);
		tailUrl += "&month1=" + getSdate(1);
		tailUrl += "&year2=" + getSdate(2);
		tailUrl += "&month2=" + getSdate(3);
		
		showAllCountries = request.getParameter("showAll");
		if ("".equals(showAllCountries) || showAllCountries == null) 
		{
			showAllCountries = "0";
		}
	
		tailUrl += "&showAll=" + showAllCountries;
				
		para.addXref(url + "/stats?type=0" + tailUrl, message("xmlui.Acessos.stats.agruparporano"));
		para.addXref(url + "/stats?type=1" + tailUrl, message("xmlui.Acessos.stats.agruparpormes"));
		
        try 
		{
			Comparator intComp = GeneralFunctions.integerComparator(); 
			
			if (type.equals("1")) 
			{
				years = new TreeMap<String,String>(intComp);
			} else {
				years = new TreeMap<String,String>();
			}				
           
			String[] sdates = new String[4];
		
			String date1 = new String();
			String date2 = new String();
			
			for (int i = 0; i < 4; i++) 
			{
				if ((dates[i] != null) && (!dates[i].equals(""))) 
				{
					sdates[i] = dates[i];
				} 
				else 
				{
					switch(i)
					{
						case 0:
							sdates[0] = "xx";
							break;
						case 1:
							sdates[1] = "01";
							break;
						case 2:
							sdates[2] = "xx";
							break;
						case 3:
							sdates[3] = "12";
							break;
					}
				}
			}
				
			Division query = currentDiv.addInteractiveDivision("stats-filter", contextPath + 
				"/handle/" + dso.getHandle() + "/stats?type=" + type + 
				"&showAll=" + showAllCountries, Division.METHOD_POST, "individual stats");
				
			List list = query.addList("filter-search");
			list.setHead(message("xmlui.Acessos.stats.filterDate"));
			//Adicionando o campo composto data1
			list.addLabel(message("xmlui.Acessos.stats.data1"));
			Composite dateComp = list.addItem().addComposite("initialDate", "date1");
			//Ano
			Select year1 = dateComp.addSelect("year1");
			year1.setLabel(message("xmlui.Acessos.stats.year"));
			year1.setMultiple(false);
			year1.setSize(1);
			year1.addOption(true,"").addContent(message("xmlui.FiltroDeBusca.option.all"));
			Calendar rightNow = Calendar.getInstance();        
			if (type.equals("0")) 
			{
				for(int i = rightNow.get(Calendar.YEAR); i >= 2008; i--)  
				{
					year1.addOption((String.valueOf(i).equals(dates[0]) ? true : false),
						String.valueOf(i)).addContent(String.valueOf(i));        
					years.put(String.valueOf(i),String.valueOf(i));
				}
			} 
			else 
			{
				for(int i = rightNow.get(Calendar.YEAR); i >= 2008; i--) 
				{
					year1.addOption((String.valueOf(i).equals(dates[0]) ? true : false),
						String.valueOf(i)).addContent(String.valueOf(i));        
					for (int j = 1; j <= 12; j++)	
					{
						years.put(String.valueOf(i) + StringUtils.pad(String.valueOf(j), 2, 0), 
							String.valueOf(i) + StringUtils.pad(String.valueOf(j), 2, 0));
					}
				}
			}
			//Mes
			Select month1 = dateComp.addSelect("month1");
			month1.setLabel(message("xmlui.Acessos.stats.month"));
			month1.setMultiple(false);
			month1.setSize(1);
			month1.addOption(true, "").addContent(message("xmlui.FiltroDeBusca.option.all"));
			for (int i = 1; i <= 12; i++) 
			{
				month1.addOption((String.valueOf(i).equals(dates[1]) ? true : false),
					String.valueOf(i)).addContent(String.valueOf(i));
			}						
			//Adicionando campo composto data2
			list.addLabel(message("xmlui.Acessos.stats.data2"));
			Composite dateComp2 = list.addItem().addComposite("finalDate", "date2");
			//Ano
			Select year2 = dateComp2.addSelect("year2");
			year2.setLabel(message("xmlui.Acessos.stats.year"));
			year2.setMultiple(false);
			year2.setSize(1);
			year2.addOption(true, "").addContent(message("xmlui.FiltroDeBusca.option.all"));      
			for(int i = rightNow.get(Calendar.YEAR); i >= 2008; i--) 
			{
				year2.addOption((String.valueOf(i).equals(dates[2]) ? true : false),
					String.valueOf(i)).addContent(String.valueOf(i));        
			}				
			//Mes
			Select month2 = dateComp2.addSelect("month2");
			month2.setLabel(message("xmlui.Acessos.stats.month"));
			month2.setMultiple(false);
			month2.setSize(1);
			month2.addOption(true, "").addContent(message("xmlui.FiltroDeBusca.option.all"));
			for(int i = 1; i <= 12; i++) 
			{
				month2.addOption((String.valueOf(i).equals(dates[3])? true : false),
					String.valueOf(i)).addContent(String.valueOf(i));
			}			
      
			query.addPara(null, "button-list").addButton("submit").setValue(message("xmlui.general.go"));
			
			if (!sdates[0].equals("xx") && !sdates[2].equals("xx")) 
			{
				date1 = sdates[0] + StringUtils.pad(sdates[1], 2, 0); 
				date2 = sdates[2] + StringUtils.pad(sdates[3], 2, 0);
				log.debug(date1 + " - " + date2);
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
	
	private void buildStatsTable(Map<String,String> downloadStatistics, Map<String,String> viewsStatistics, 
		Division currentDiv) throws WingException 
	{
		Table table = currentDiv.addTable("listResults", downloadStatistics.size() + 1, 3);
		
		if (type.equals("0")) 
		{
			table.setHead(message("xmlui.Acessos.stats.perYear"));
		}
		else 
		{
			table.setHead(message("xmlui.Acessos.stats.perMonth"));
		}
		
		Comparator intComp = GeneralFunctions.integerComparator(); 
		if (type.equals("1")) 
		{
			subYears = new TreeMap<String,String>(intComp);
		} else {
			subYears = new TreeMap<String,String>();
		}
		
		if (type.equals("0")) 
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
				subYears = years.headMap(dates[2] + StringUtils.pad(getSdate(3),2,0) + "\0");
			} 
			else if ((StringUtils.nullify(dates[0]) != null) && (StringUtils.nullify(dates[2]) == null)) 
			{
				subYears = years.tailMap(dates[0] + StringUtils.pad(dates[1],2,0));
			} 
			else if ((StringUtils.nullify(dates[0]) != null) && (StringUtils.nullify(dates[2]) != null)) 
			{
				//datas são iguais: subMap retorna empty, então aumenta-se um mês, já que intervalo final é aberto
				if ((dates[0] + StringUtils.pad(dates[1],2,0)).equals(dates[2] + StringUtils.pad(getSdate(3),2,0)))
				{
					int lastMonth = Integer.parseInt(dates[3]) + 1;
					int lastYear = Integer.parseInt(dates[2]);
					 //aumenta mes e passa a ser exclusive
					if(lastMonth == 13)
					{
						lastMonth = 1;
						lastYear += 1;
					}
					subYears = years.subMap(dates[0] + StringUtils.pad(dates[1],2,0), String.valueOf(lastYear) + StringUtils.pad(String.valueOf(lastMonth),2,0));
					//log.debug(dates[0] + StringUtils.pad(dates[1],2,0), String.valueOf(lastYear) + StringUtils.pad(String.valueOf(lastMonth),2,0));
				}
				else 
				{
					subYears = years.subMap(dates[0] + StringUtils.pad(dates[1],2,0), dates[2] + StringUtils.pad(dates[3],2,0) + "\0");
				}
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
				dates[2] + StringUtils.pad(getSdate(3),2,0));  //MANUELA
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
			if (type.equals("1")) 
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
		
	private void buildCountryTable(ArrayList statisticsPerContry, Division countryDiv, 
		String tableTitleMessage, String tableHeaderMessage) throws WingException, UnsupportedEncodingException 
	{
		Iterator iterador;
			
		iterador = statisticsPerContry.iterator();
 
		Table table = countryDiv.addTable("statsPerCountry", statisticsPerContry.size() + 1, 2);
		table.setHead(message(tableTitleMessage));
		Row header = table.addRow();
		header.addCellContent(message("xmlui.Acessos.stats.country"));
		header.addCellContent(message(tableHeaderMessage));
	
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
	
	private String getSdate (int index) 
	{
		String[] sdates = new String[4];
		for (int i = 0; i < 4; i++) 
		{
			if ((dates[i] != null) && (!dates[i].equals(""))) 
			{
				sdates[i] = dates[i];
			} 
			else if (i == 0) 
			{
				sdates[0] = "2008";
			} 
			else if (i == 1) 
			{
				sdates[1] = "1";
			} 
			else if (i == 2) 
			{
				sdates[2] = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
			} 
			else if (i == 3) 
			{
				sdates[3] = "12";
			}
		}
		return sdates[index];
	}
	
	private void buildLinkToShowAllCountries(Division currentDiv) throws WingException
	{
		String urlShowAll =  contextPath + "/handle/" + dso.getHandle() + "/stats?type=" + type;
			
		String[] otherDates = {request.getParameter("year1"), request.getParameter("month1"),
				request.getParameter("year2"), request.getParameter("month2")};
		
		if ((otherDates[0] != null) && !"".equals(otherDates[0])) 
		{
			urlShowAll += "&year1=" + otherDates[0];
		}
		if ((otherDates[1] != null) && !"".equals(otherDates[1])) 
		{
			urlShowAll += "&month1=" + otherDates[1];
		}
		if ((otherDates[2] != null) && !"".equals(otherDates[2])) 
		{
			urlShowAll += "&year2=" + otherDates[2];
		}
		if ((otherDates[3] != null) && !"".equals(otherDates[3])) 
		{
			urlShowAll += "&month2=" + otherDates[3];
		}
		if (showAllCountries.equals("0")) 
		{
			currentDiv.addPara().addXref(urlShowAll + "&showAll=1", message("xmlui.Acessos.stats.vertodosospaises"));
		} 
		else
		{
			currentDiv.addPara().addXref(urlShowAll + "&showAll=0", message("xmlui.Acessos.stats.naovertodosospaises"));
		}
	}

	private String buildSolrQuery(String statisticsType, boolean addUfrgsClause) throws SQLException 
	{
		StringBuilder solrQuery = new StringBuilder();
		solrQuery.append("statistics_type:view");
		
		DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
		
		if (dso instanceof Item) 
		{
			Item item = (Item) dso;	
			if (statisticsType.equals("DOWNLOADS")) 
			{
				solrQuery.append(" AND type:0 AND bundleName:ORIGINAL");
				solrQuery.append(" AND owningItem:");
			} 
			else if (statisticsType.equals("ACESSOS")) 
			{
				solrQuery.append(" AND type:2");
				solrQuery.append(" AND id:");
			}
			solrQuery.append(item.getID());
		} 
		else if (dso instanceof Community)  //(owningComm:10 && (type:2)) || (type:4 && id:10)
		{
			Community com = (Community) dso;
			if (statisticsType.equals("DOWNLOADS")) 
			{
				solrQuery.append(" AND type:0 AND bundleName:ORIGINAL");
				solrQuery.append(" AND owningComm:");
				solrQuery.append(com.getID());
			} 
			else if (statisticsType.equals("ACESSOS")) 
			{
				solrQuery.append(" AND ((type:2 AND owningComm:"+com.getID()+") OR (type:4 AND id:"+com.getID()+"))");
			}

		} 
		else if (dso instanceof Collection) 
		{
			Collection col = (Collection) dso;
			if (statisticsType.equals("DOWNLOADS")) 
			{
				solrQuery.append(" AND type:0 AND bundleName:ORIGINAL");
				solrQuery.append(" AND owningColl:");
				solrQuery.append(col.getID());
			} 
			else if (statisticsType.equals("ACESSOS")) 
			{
				solrQuery.append(" AND ((type:2 AND owningColl:"+col.getID()+") OR (type:3 AND id:"+col.getID()+"))");
			}
		
		}
		
		solrQuery.append(StringUtils.buildTimeClause(dates, type));
				
		if(addUfrgsClause) 
		{
			//ufrgs sub-net ipv4 and ipv6
			//solrQuery.append(" AND (ip:143.54.*.* OR ip:" + StringUtils.IPV6_UFRGS_PATTERN + ")");
		}
		log.debug("QUERY: " + solrQuery.toString());
		return solrQuery.toString();
	}
    
    protected TreeMap<String,String> collectData(String accessType) 
		throws SQLException, WingException 
	{    
        TreeMap<String,String> result = new TreeMap<String,String>();     
		ObjectCount[] statisticsCount = null;	
		String facetField = "time_ano";
		// per month
		if (type.equals("1")) 
		{
			facetField = "time_anomes";
		} 
		try 
		{
			statisticsCount = SolrLogger.queryFacetField(buildSolrQuery(accessType, false), "", facetField, 
					Integer.MAX_VALUE, false, null);	
				
			for(int i = 0; i < statisticsCount.length; i++) 
			{
				result.put(statisticsCount[i].getValue().replace("-",""), Long.toString(statisticsCount[i].getCount()));
			}
		}
		catch (Exception e) 
		{
			log.error("Error occurred while creating statistics in (collectData) Stats.java", e);
		}
		return result;
    }

	protected ArrayList collectCountryData(String type) 
		throws SQLException, WingException 
	{
		ArrayList result = new ArrayList();

		try 
		{
			ObjectCount[] downloadsPerCountry = null;
			if (showAllCountries.equals("0")) 
			{
				downloadsPerCountry = SolrLogger.queryFacetField(buildSolrQuery(type, false), "", "countryCode", 
					10, false, null);
			} 
			else 
			{
				downloadsPerCountry = SolrLogger.queryFacetField(buildSolrQuery(type, false), "", "countryCode", 
					Integer.MAX_VALUE, false, null);	
			}			
			for(int i = 0; i < downloadsPerCountry.length; i++) 
			{
				Integer index = (Integer) hashMapCountryCodeToIndex.get(downloadsPerCountry[i].getValue().toUpperCase());
				log.debug("country="+downloadsPerCountry[i].getValue().toUpperCase());
				log.debug("index="+index);
				log.debug("countryName="+Country.countryName[index]);
				if (index == null) 
				{
					throw new NullPointerException("index is null, country not found!");
				}
				result.add(new Tuple(downloadsPerCountry[i].getValue(), Long.toString(downloadsPerCountry[i].getCount()), Country.countryName[index]));

				//result.add(new Tuple(downloadsPerCountry[i].getValue(), Long.toString(downloadsPerCountry[i].getCount())));
			}
			// add ufrgs internal statistics
			long totalUFRGS = SolrLogger.queryTotal(buildSolrQuery(type, true), "").getCount();	
			if (totalUFRGS > 0) 
			{
				result.add(new Tuple("UFRGS", Long.toString(totalUFRGS), "UFRGS"));
			}
			
			//Sorting
			Collections.sort(result, new Comparator<Tuple>() 
			{
				@Override
				public int compare(Tuple stats1, Tuple stats2) 
				{
					int ret = Long.compare(Long.parseLong(stats1.getValue()), Long.parseLong(stats2.getValue()));
					if(ret != 0)
					{
						return ret;
					}
					//se valores forem iguais, compara por nome
					return stats2.getName().compareTo(stats1.getName());
				}
			});
			Collections.reverse(result);
			
			// restore top10
			if (showAllCountries.equals("0") && result.size() == 11) 
			{
				result.remove(10);
			}
		} 
		catch (Exception e) 
		{
			log.error("Error occurred while creating statistics in (collectCountryData) Stats.java", e);
		}
		
		return result;             
    }
	
    public static String getItemTitle(Item item) 
	{
        return item.getName();
    }
}
