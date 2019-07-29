/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.statistics;

import java.io.IOException;
//import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Calendar;
import java.util.ArrayList;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
//import org.dspace.app.xmlui.statistics.ChartGenerator;
import org.dspace.app.xmlui.statistics.Country;
import org.dspace.app.xmlui.statistics.StringUtils;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Cell;
import org.dspace.app.xmlui.wing.element.Composite;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Row;
import org.dspace.app.xmlui.wing.element.Select;
import org.dspace.app.xmlui.wing.element.Table;
import org.dspace.app.xmlui.wing.element.Xref;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.statistics.SolrLogger;
import org.apache.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.DSpaceObject;
import org.dspace.handle.HandleManager;
import org.xml.sax.SAXException;

public class GeneralStats extends AbstractDSpaceTransformer 
{
		
    private static Logger log = Logger.getLogger(GeneralStats.class);
	private DSpaceObject dso;
	private Request request;
    private final static HashMap hashMapCountryCodeToIndex = new HashMap(512);
	private String specificTime;
	private ArrayList<String[]> downloadStatistics = new ArrayList<String[]>();
	private	ArrayList<String[]> viewsStatistics = new ArrayList<String[]>();
	
    public void addPageMeta(PageMeta pageMeta) throws SAXException, WingException, UIException, 
		SQLException, IOException, AuthorizeException 
	{
        pageMeta.addMetadata("title").addContent(message("xmlui.administrative.Navigation.stats_gerais"));
		pageMeta.addTrailLink(contextPath + "/", message("xmlui.general.dspace_home"));
		pageMeta.addTrail().addContent(message("xmlui.administrative.Navigation.stats_gerais"));
    }
	
	public void addBody(Body body) throws SAXException, WingException, UIException, SQLException, 
		IOException, AuthorizeException 
	{
		request = ObjectModelHelper.getRequest(objectModel);
        
		Division current = body.addDivision("current");
		
		buildLinks(current);
        buildFilterOptions(current);
		
		for (int i = 0; i < 254; i++) 
		{
			hashMapCountryCodeToIndex.put(Country.countryCode[i],new Integer(i));
        }
		
		buildGeneralStatsTable(current);
        
        Division generalCharts = current.addDivision("generalCharts");
        generalCharts.addPara("dl", null).addContent(message("xmlui.Acessos.stats.counting_chart"));
        generalCharts.addPara("dl", null).addContent(message("xmlui.Acessos.stats.item_chart"));
    }

	private void buildLinks(Division currentDivision) throws WingException, SQLException
	{
		String handle_param = request.getParameter("handle");
		
		currentDivision.setHead(message("xmlui.administrative.Navigation.stats_gerais"));
		currentDivision.addPara("collect", null).addContent(message("xmlui.Acessos.stats.coleta"));
		
		if ((!"".equals(handle_param)) && (handle_param != null)) 
		{
			dso = HandleManager.resolveToObject(context,handle_param);
		}
        if (dso instanceof Community) 
		{
			if ((!"".equals(handle_param)) && (handle_param != null)) 
			{
				Community community = (Community) dso;		
				Community parentCommunity = community.getParentCommunity();
				
				if(parentCommunity != null) 
				{
					currentDivision.addPara("Voltar", null).addXref(contextPath + "/generalstats?handle=" + parentCommunity.getHandle(), 
						message("xmlui.general.return"));
				} 
				else 
				{
					currentDivision.addPara("Voltar", null).addXref(contextPath + "/generalstats", message("xmlui.general.return"));
				}
				currentDivision.addPara().addXref(contextPath + "/handle/" + dso.getHandle(), message("xmlui.title.comm.handle_10183_" + 
					community.getHandle().substring(community.getHandle().indexOf("/") + 1)));
			}
        } 
		else if (dso instanceof Collection) 
		{
            Collection collection = (Collection) dso;
            currentDivision.addPara().addXref(contextPath + "/generalstats?handle=" + dso.getHandle(),
				message("xmlui.title.comm.handle_10183_" + 
				collection.getHandle().substring(collection.getHandle().indexOf("/") + 1)));
        } 
		else if (dso instanceof Item) 
		{
            Item item = (Item) dso;
            currentDivision.addPara().addXref(contextPath + "/generalstats?handle=" + dso.getHandle(), getItemTitle(item));
        }
	}
	
	private void buildFilterOptions(Division currentDiv) throws WingException, UnsupportedEncodingException 
	{
		String[] dates = {request.getParameter("year1"), request.getParameter("month1"),
					request.getParameter("year2"), request.getParameter("month2")};				
				
		String path = contextPath + "/generalstats";
		if (request.getParameter("handle") != null) 
		{
			path += "?handle=" + request.getParameter("handle");
		}
						
        try 
		{			
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
			
			Division query = currentDiv.addInteractiveDivision("stats-filter", path, Division.METHOD_POST, "general stats");
				
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
			year1.addOption(true, "").addContent(message("xmlui.FiltroDeBusca.option.all"));
			Calendar rightNow = Calendar.getInstance();        
			for(int i = 2008; i <= rightNow.get(Calendar.YEAR); i++) 
			{
				year1.addOption((String.valueOf(i).equals(dates[0]) ? true : false),
					String.valueOf(i)).addContent(String.valueOf(i));        
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
			for(int i = 2008; i <= rightNow.get(Calendar.YEAR); i++) 
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
			
			specificTime = "";
			
			if (!sdates[0].equals("xx") && !sdates[2].equals("xx")) 
			{
				//specificTime = StringUtils.buildTimeClause(dates[0], dates[1], dates[2], dates[3]);
				specificTime = StringUtils.buildTimeClause(dates, "0");
				date1 = sdates[0] + StringUtils.pad(sdates[1], 2, 0); 
				date2 = sdates[2] + StringUtils.pad(sdates[3], 2,0);
				log.info(date1 + " - " + date2);
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
	
	private void buildGeneralStatsTable(Division currentDiv) 
	{
		Long totalDownloads = 0L;
		Long totalViews = 0L;
		int totalItems = 0;
		downloadStatistics = new ArrayList<String[]>();
		viewsStatistics = new ArrayList<String[]>();
		String handle_param = request.getParameter("handle");
		
		try 
		{
			Division divRes = currentDiv.addDivision("results");
			
			Division generalStatistics = divRes.addDivision("generalStatistics");		
			
			Table table = generalStatistics.addTable("listResults", 5, 4);

			Row header = table.addRow();
			header.addCellContent(message(""));
			header.addCellContent(message("xmlui.Acessos.stats.num_items"));
			header.addCellContent(message("xmlui.Acessos.stats.downloads"));
			header.addCellContent(message("xmlui.Acessos.stats.views"));
			
			//colect data and build the links
			Row row;
			Cell linkCell;
			Xref itemLink;
			long downloadsResult = 0L;
			long viewsResult = 0L;
			
			// Guilherme: colocado o condicional de handle para identificar a raiz antes por causa do bug do botão voltar
			if (("".equals(handle_param)) || (handle_param == null)) 
			{
				for (Community topLevel : Community.findAllTop(context)) 
				{
					row = table.addRow();
					linkCell = row.addCell();
					itemLink = linkCell.addXref(contextPath + "/generalstats?handle=" + topLevel.getHandle());
					itemLink.addContent(message("xmlui.title.comm.handle_10183_" + 
						topLevel.getHandle().substring(topLevel.getHandle().indexOf("/") + 1)));
					row.addCellContent(String.valueOf(topLevel.countItems()));
					downloadsResult = SolrLogger.queryTotal(StringUtils.buildSolrQuery("DOWNLOADS", false, 
						"owningComm:" + topLevel.getID(), specificTime), "").getCount();
					row.addCellContent(String.valueOf(downloadsResult));	
					downloadStatistics.add(new String[]{String.valueOf(topLevel.getID()), String.valueOf(downloadsResult), 
						"COM", String.valueOf(topLevel.countItems())});
					totalDownloads += downloadsResult;
					viewsResult = SolrLogger.queryTotal(StringUtils.buildSolrQuery("ACESSOS", false, 
						"owningComm:" + topLevel.getID(), specificTime), "").getCount();
					row.addCellContent(String.valueOf(viewsResult));	
					totalViews += viewsResult;
					viewsStatistics.add(new String[]{String.valueOf(topLevel.getID()), String.valueOf(viewsResult), 
						"COM", String.valueOf(topLevel.countItems())});
					totalItems += topLevel.countItems();
				}
			} else			
			if (dso instanceof Community) 
			{
				Community community = (Community) dso;
				if (community.getSubcommunities().length > 0) 
				{
					for (Community com : community.getSubcommunities()) 
					{
						row = table.addRow();
						linkCell = row.addCell();
						itemLink = linkCell.addXref(contextPath + "/generalstats?handle=" + com.getHandle());
						itemLink.addContent(message("xmlui.title.comm.handle_10183_" + 
							com.getHandle().substring(com.getHandle().indexOf("/") + 1)));
						row.addCellContent(String.valueOf(com.countItems()));
						downloadsResult = SolrLogger.queryTotal(StringUtils.buildSolrQuery("DOWNLOADS", false, 
							"owningComm:" + com.getID(), specificTime), "").getCount();
						row.addCellContent(String.valueOf(downloadsResult));	
						downloadStatistics.add(new String[]{String.valueOf(com.getID()), String.valueOf(downloadsResult), 
							"COM", String.valueOf(com.countItems())});
						totalDownloads += downloadsResult;
						viewsResult = SolrLogger.queryTotal(StringUtils.buildSolrQuery("ACESSOS", false, 
							"owningComm:" + com.getID(), specificTime), "").getCount();
						row.addCellContent(String.valueOf(viewsResult));	
						totalViews += viewsResult;
						viewsStatistics.add(new String[]{String.valueOf(com.getID()), String.valueOf(viewsResult), 
							"COM", String.valueOf(com.countItems())});
						totalItems += com.countItems();
					} 
				} 
				else 
				{
					for (Collection coll : community.getCollections()) 
					{
						row = table.addRow();
						row.addCellContent(message("xmlui.title.comm.handle_10183_" + 
							coll.getHandle().substring(coll.getHandle().indexOf("/") + 1)));
						row.addCellContent(String.valueOf(coll.countItems()));
						downloadsResult = SolrLogger.queryTotal(StringUtils.buildSolrQuery("DOWNLOADS", false, 
							"owningColl:" + coll.getID(), specificTime), "").getCount();
						row.addCellContent(Long.toString(downloadsResult));
						downloadStatistics.add(new String[]{String.valueOf(coll.getID()), String.valueOf(downloadsResult), 
							"COL", String.valueOf(coll.countItems())});
						totalDownloads += downloadsResult;
						viewsResult = SolrLogger.queryTotal(StringUtils.buildSolrQuery("ACESSOS", false, 
							"owningColl:" + coll.getID(), specificTime), "").getCount();
						row.addCellContent(String.valueOf(viewsResult));
						totalViews += viewsResult;
						viewsStatistics.add(new String[]{String.valueOf(coll.getID()),String.valueOf(viewsResult), 
							"COL", String.valueOf(coll.countItems())});
						totalItems += coll.countItems();
					}
				}
			} 
			else 
			{
				for (Community topLevel : Community.findAllTop(context)) 
				{
					row = table.addRow();
					linkCell = row.addCell();
					itemLink = linkCell.addXref(contextPath + "/generalstats?handle=" + topLevel.getHandle());
					itemLink.addContent(message("xmlui.title.comm.handle_10183_" + 
						topLevel.getHandle().substring(topLevel.getHandle().indexOf("/") + 1)));
					row.addCellContent(String.valueOf(topLevel.countItems()));
					downloadsResult = SolrLogger.queryTotal(StringUtils.buildSolrQuery("DOWNLOADS", false, 
						"owningComm:" + topLevel.getID(), specificTime), "").getCount();
					row.addCellContent(String.valueOf(downloadsResult));	
					downloadStatistics.add(new String[]{String.valueOf(topLevel.getID()), String.valueOf(downloadsResult), 
						"COM", String.valueOf(topLevel.countItems())});
					totalDownloads += downloadsResult;
					viewsResult = SolrLogger.queryTotal(StringUtils.buildSolrQuery("ACESSOS", false, 
						"owningComm:" + topLevel.getID(), specificTime), "").getCount();
					row.addCellContent(String.valueOf(viewsResult));	
					totalViews += viewsResult;
					viewsStatistics.add(new String[]{String.valueOf(topLevel.getID()), String.valueOf(viewsResult), 
						"COM", String.valueOf(topLevel.countItems())});
					totalItems += topLevel.countItems();
				}
			}
			// last row
			row = table.addRow();
			row.addCellContent("Total");     
			row.addCellContent(Integer.toString(totalItems));
			row.addCellContent(Long.toString(totalDownloads));
			row.addCellContent(Long.toString(totalViews));
		}
		catch (Exception e) 
		{
			log.error("Erro em GeneralStats addBody()", e);
		}
	}
   
    public static String getItemTitle(Item item) 
	{
        return item.getName();
    }
}