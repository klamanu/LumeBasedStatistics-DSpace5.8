/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.statistics;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.statistics.content.*;
import org.dspace.statistics.Dataset;
import org.dspace.statistics.ObjectCount;
import org.dspace.statistics.SolrLogger;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;
import org.xml.sax.SAXException;

public class TopTen extends AbstractDSpaceTransformer 
{
	private static Logger log = Logger.getLogger(TopTen.class);
    private static final Message T_dspace_home = message("xmlui.general.dspace_home");
    private static final Message T_head_title = message("xmlui.statistics.title");
	private static final Message T_head_topten_title = message("xmlui.statistics.visits.bitstreams.topten.title");
    private static final Message T_statistics_trail = message("xmlui.statistics.visits.bitstreams.topten.title");
	//private static final Message T_statistics_trail = message("xmlui.statistics.trail");
	private static final String T_head_nodownloads = "xmlui.statistics.visits.nodownloads";
    private static final String T_head_visits_bitstream = "xmlui.statistics.visits.bitstreams";
	private static final String T_head_visits_bitstream_desc = "xmlui.statistics.visits.bitstreams.topten.desc";
    private Date dateStart = null;
    private Date dateEnd = null;

    public TopTen(Date dateStart, Date dateEnd) 
	{
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;

        try 
		{
            this.context = new Context();
        } 
		catch (SQLException e) 
		{
            log.error("Error getting context in TopTen:" + e.getMessage());
        }
    }

    public TopTen() 
	{
        try 
		{
            this.context = new Context();
        } 
		catch (SQLException e) 
		{
            log.error("Error getting context in TopTen:" + e.getMessage());
        }
    }

    /**
     * Add a page title and trail links
     */
    public void addPageMeta(PageMeta pageMeta) throws SAXException, WingException, UIException, 
		SQLException, IOException, AuthorizeException 
	{
        //Try to find our dspace object
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
		//pageMeta.addMetadata("title").addContent(message("xmlui.administrative.Navigation.statistics"));
		pageMeta.addMetadata("title").addContent(T_head_topten_title);
        pageMeta.addTrailLink(contextPath + "/",T_dspace_home);

        if(dso != null) 
		{
            HandleUtil.buildHandleTrail(dso, pageMeta, contextPath, true);
        }
		if(dso != null && dso.getHandle() != null){
			// estatística com handle
            //pageMeta.addTrailLink(contextPath + "/handle/" + dso.getHandle() + "/stats/downloads", T_statistics_trail);
			pageMeta.addTrailLink(contextPath + "/handle/" + dso.getHandle(), dso.getName());			
            pageMeta.addTrail().addContent(T_statistics_trail);
        } else {
			// estatística geral
			pageMeta.addTrailLink(contextPath + "/estatisticas" , message("xmlui.menu.estatisticas"));
            pageMeta.addTrail().addContent(T_statistics_trail);
        }
    }

    /**
	 * What to add at the end of the body
	 */
	public void addBody(Body body) throws SAXException, WingException,
		UIException, SQLException, IOException, AuthorizeException 
	{
		try 
		{
			renderHome(body);
        } 
		catch (RuntimeException e) 
		{
            throw e;
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage(), e);
		}
	}

	public void renderHome(Body body) throws WingException, SQLException 
	{
		Division title = body.addDivision("title");
		Division header = body.addInteractiveDivision("header", "/statistics-home", Division.METHOD_POST, null);
		Division access = body.addDivision("access");
		
		title.setHead(T_head_topten_title);
		title.addPara("description", null).addContent(message(T_head_visits_bitstream_desc));
			
		List list = access.addList("statistics");
		try 
		{
			this.communityListBuilder("", list, null, 0);
		} 
		catch (Exception e) 
		{
			log.error("Error occurred while creating statistics for home page", e);
		}
	}
	
	private void communityListBuilder (String baseURL, List parentList, Community currentCommunity, Integer level) 
		throws SQLException, WingException 
	{
		if (level < 3) 
		{
			if (currentCommunity == null) 
			{
				for (Community topLevel : Community.findAllTop(context)) 
				{
					communityListBuilder(baseURL, parentList, topLevel, level + 1);
				}
			} 
			else 
			{
				try 
				{
					parentList.addItem(message("xmlui.title.comm.handle_10183_" + currentCommunity.getHandle()
						.substring(currentCommunity.getHandle().indexOf("/") + 1)));
					if(level == 2 || currentCommunity.getSubcommunities().length == 0) 
					{
						this.getTopTenItems(parentList, Integer.toString(currentCommunity.getID()));
					}						
					List containerSubList = null;
					if (level < 2) 
					{
						for (Community subComs : currentCommunity.getSubcommunities()) 
						{
							if (containerSubList == null) 
							{
								containerSubList = parentList.addList("subList" + currentCommunity.getID());
							}
							communityListBuilder(baseURL, containerSubList, subComs, level + 1);
						}
					}
				} 
				catch (SQLException e) 
				{
					log.info(e.getMessage());
				}
			}
		}
	}
	
	private void getTopTenItems (List topList, String communityId) throws SQLException, WingException 
	{
    	try 
		{
			ObjectCount[] top10_community = SolrLogger.queryFacetField("owningComm:" + communityId + 
				" AND statistics_type:view AND type:0 AND " +
				"bundleName:ORIGINAL", "", "owningItem", 10, false, null);
			List topComm = topList.addList("top10_" + communityId);
			String query = "";
			TableRowIterator result = null;
			TableRow tuple = null;
			String title = "";
			for(int i = 0; i < top10_community.length; i++) 
			{
				Item item = Item.find(context, Integer.parseInt(top10_community[i].getValue()));
				if(item == null)
				{
					title = "Item não encontrado.";
					topComm.addItem(message(title + 
						" [" + Long.toString(top10_community[i].getCount()) + "]"));
				}
				else 
				{
					title = (item.getName() == "" ? "Sem título" : item.getName());
					topComm.addItemXref("/handle/"+item.getHandle(), title + 
						" [" + Long.toString(top10_community[i].getCount()) + "]");				
				}
			}
			if(top10_community.length == 0) 
			{
				topComm.addItem(message(T_head_nodownloads));
			}
		} 
		catch (Exception e) 
		{
			log.error("Error occurred while creating statistics for top 10 itens of comunity", e);
		}    	
    }
}
