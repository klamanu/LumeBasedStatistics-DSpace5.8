/**
 * The contents of this file are subject to the license and copyright detailed
 * in the LICENSE and NOTICE files at the root of the source tree and available
 * online at
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
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.core.Context;
import org.dspace.statistics.ObjectCount;
import org.dspace.statistics.SolrLogger;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.xml.sax.SAXException;

public class Downloads extends AbstractDSpaceTransformer {

    private static Logger log = Logger.getLogger(Downloads.class);
    private static final Message T_dspace_home = message("xmlui.general.dspace_home");
    //private static final Message T_head_title = message("xmlui.statistics.title");
    private static final Message T_head_title = message("xmlui.statistics.visits.bitstreams.download.title");
    //private static final Message T_statistics_trail = message("xmlui.statistics.trail");
	private static final Message T_statistics_trail = message("xmlui.statistics.visits.bitstreams.download.title");
    private static final Message T_head_visits_bitstream_total = message("xmlui.statistics.visits.bitstreams.total");
    private static final String T_head_visits_bitstream_desc = "xmlui.statistics.visits.bitstreams.communities_and_collections.desc";
    private static final Message T_head_registeredUsers = message("xmlui.statistics.visits.totalusers");
    private static final Message T_head_UfrgsUsers = message("xmlui.statistics.visits.userufrgs");
    private static final Message T_head_notUfrgsUsers = message("xmlui.statistics.visits.usernaoufrgs");
    private static final Message T_head_downloads_title = message("xmlui.statistics.visits.bitstreams.communities_and_collections.title");
    private static final Message T_head_users = message("xmlui.statistics.visits.users");

    private Date dateStart = null;
    private Date dateEnd = null;
    private Map<String, Long> downloads_community_hash = null;
    private Map<String, Long> downloads_collection_hash = null;
    private ObjectCount[] downloads_community = null;
    private ObjectCount[] downloads_collection = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public Downloads(Date dateStart, Date dateEnd) {
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;

        try {
            this.context = new Context();
        } catch (SQLException e) {
            log.error("Error getting context in Downloads:" + e.getMessage());
        }
    }

    public Downloads() {
        try {
            this.context = new Context();
        } catch (SQLException e) {
            log.error("Error getting context in Downloads:" + e.getMessage());
        }
    }

    /**
     * Add a page title and trail links
     */
    public void addPageMeta(PageMeta pageMeta) throws SAXException, WingException, UIException,
            SQLException, IOException, AuthorizeException {
        //Try to find our dspace object
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

        //pageMeta.addMetadata("title").addContent(message("xmlui.administrative.Navigation.statistics"));
		pageMeta.addMetadata("title").addContent(T_head_title);
        pageMeta.addTrailLink(contextPath + "/", T_dspace_home);

        if (dso != null) {
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
            UIException, SQLException, IOException, AuthorizeException {
        try {
            renderHome(body);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private void updateCommunityDownloadsHash() {
        downloads_community_hash = new HashMap<String, Long>();
        for (int i = 0; i < downloads_community.length; i++) {
            downloads_community_hash.put(downloads_community[i].getValue(), downloads_community[i].getCount());
        }
    }

    private void updateCollectionDownloadsHash() {
        downloads_collection_hash = new HashMap<String, Long>();
        for (int i = 0; i < downloads_collection.length; i++) {
            downloads_collection_hash.put(downloads_collection[i].getValue(), downloads_collection[i].getCount());
        }
    }

    public void renderHome(Body body) throws WingException, SQLException {
		
        Division title = body.addDivision("title");
        Division access = body.addDivision("access");
        Division users = body.addDivision("users");
        Division footer = body.addDivision("footer");

        title.setHead(T_head_downloads_title);
        title.addPara("description", null).addContent(message(T_head_visits_bitstream_desc));

        List list = access.addList("estatisticas");

        try {
            downloads_community = SolrLogger.queryFacetField("statistics_type:view AND type:0 AND "
                    + "bundleName:ORIGINAL", "", "owningComm", -1, true, null);
            // -1 pelo que eu vi limita a um certo numero.
            downloads_collection = SolrLogger.queryFacetField("statistics_type:view AND type:0 AND "
                    + "bundleName:ORIGINAL", "", "owningColl", Integer.MAX_VALUE, true, null);

            updateCommunityDownloadsHash();
            updateCollectionDownloadsHash();

            this.containerListBuilder("", list, null);

            long totalDownloads = downloads_community[downloads_community.length - 1].getCount();

            title.addPara(T_head_visits_bitstream_total.parameterize(" (" + Long.toString(totalDownloads) + ")"));

            addDisplayTable(footer);

        } catch (Exception e) {
            log.error("Error occurred while creating statistics in Downloads.java", e);
        }
    }

    /* A recursive helper method to build the community/collection hierarchy list */
    private void containerListBuilder(String baseURL, List parentList, Community currentCommunity)
            throws SQLException, WingException, NullPointerException {
        if (currentCommunity == null) {
            for (Community topLevel : Community.findAllTop(context)) {
                containerListBuilder(baseURL, parentList, topLevel);
            }
        } else {
            try {
                String cid = Integer.toString(currentCommunity.getID());

                Item auxiliaryItem = parentList.addItem();
                auxiliaryItem.addXref("/handle/"+currentCommunity.getHandle() , currentCommunity.getName());
                /** Guilherme: Comentei para substituir pelas linhas acima, pois elas serão usadas para o template xsl de tradução em LumeUtil.xsl
                auxiliaryItem.addContent(message("xmlui.title.comm.handle_10183_"
                        + currentCommunity.getHandle().substring(currentCommunity.getHandle().indexOf("/") + 1)));  */
                auxiliaryItem.addContent(" (" + (downloads_community_hash.get(cid) == null ? 0 : downloads_community_hash.get(cid)) + ")");
                
                List containerSubList = null;

                for (Collection subCols : currentCommunity.getCollections()) {
                    if (containerSubList == null) {
                        containerSubList = parentList.addList("subList" + currentCommunity.getID());
                    }
                    cid = Integer.toString(subCols.getID());

                    Item auxiliaryItem2 = containerSubList.addItem();
                    auxiliaryItem2.addXref("/handle/"+subCols.getHandle() , subCols.getName());
                    /** Guilherme: Comentei para substituir pelas linhas acima, pois elas serão usadas para o template xsl de tradução em LumeUtil.xsl
                    auxiliaryItem2.addContent(message("xmlui.title.comm.handle_10183_"
                            + subCols.getHandle().substring(subCols.getHandle().indexOf("/") + 1))); */
                    auxiliaryItem2.addContent(" (" + (downloads_collection_hash.get(cid) == null
                            ? 0 : downloads_collection_hash.get(cid)) + ")");
                }

                for (Community subComs : currentCommunity.getSubcommunities()) {
                    if (containerSubList == null) {
                        containerSubList = parentList.addList("subList" + currentCommunity.getID());
                    }
                    containerListBuilder(baseURL, containerSubList, subComs);
                }
            } catch (NullPointerException e) {
                log.info("nullpointer");
                log.info(e.getMessage());
            }
        }
    }

    /**
     * Adds a table layout to the page
     *
     * @param mainDiv the div to add the table to
     * @throws SAXException
     * @throws WingException
     * @throws ParseException
     * @throws IOException
     * @throws SolrServerException
     * @throws SQLException
     */
    private void addDisplayTable(Division mainDiv) throws SAXException, WingException, SQLException,
            SolrServerException, IOException, ParseException {
        Division wrapper = mainDiv.addDivision("tablewrapper");
        Table table = wrapper.addTable("list-table", 1, 1, "tableWithTitle detailtable");

        table.setHead(message("xmlui.statistics.visits.users"));

        TableRow row = DatabaseManager.querySingle(this.context, "SELECT CAST(COUNT(*) AS varchar) AS total "
                + "FROM eperson WHERE netid IS NOT NULL");
        String ufrgsTotal = row.getStringColumn("total");

        row = DatabaseManager.querySingle(this.context, "SELECT CAST(COUNT(*) AS varchar) AS total "
                + "FROM eperson WHERE netid IS NULL");
        String notUfrgsTotal = row.getStringColumn("total");

        long total_cadastrados = Long.parseLong(ufrgsTotal) + Long.parseLong(notUfrgsTotal);

        Row valListRow = table.addRow();
        valListRow.addCell("" + 0, Cell.ROLE_DATA, "labelcell")
                .addContent(T_head_registeredUsers);
        Cell cell = valListRow.addCell(0 + "-" + 0,
                Cell.ROLE_DATA, "datacell");
        cell.addContent(" [" + Long.toString(total_cadastrados) + "]");

        valListRow = table.addRow();
        valListRow.addCell("" + 1, Cell.ROLE_DATA, "labelcell")
                .addContent(T_head_UfrgsUsers);
        cell = valListRow.addCell(1 + "-" + 0,
                Cell.ROLE_DATA, "datacell");
        cell.addContent(" [" + ufrgsTotal + "]");

        valListRow = table.addRow();
        valListRow.addCell("" + 2, Cell.ROLE_DATA, "labelcell")
                .addContent(T_head_notUfrgsUsers);
        cell = valListRow.addCell(1 + "-" + 0,
                Cell.ROLE_DATA, "datacell");
        cell.addContent(" [" + notUfrgsTotal + "]");
    }
}
