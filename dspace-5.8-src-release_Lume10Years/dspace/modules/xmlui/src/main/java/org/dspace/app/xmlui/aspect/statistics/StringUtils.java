package org.dspace.app.xmlui.statistics;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.TreeMap;
import org.apache.log4j.Logger;

public final class StringUtils 
{
	private static final Logger log = Logger.getLogger(StringUtils.class);
	private static final int MAX_ITEMS_PER_QUERY = 100;
	public static final String IPV6_UFRGS_PATTERN = "2804\\:1f20\\:*";
	
	public final static String nullify(String value) 
	{
		if ("".equals(value)) 
		{
			return null;
		} 
		else 
		{
			return value;
		}
	}
	
	public final static String pad(String inp, int number, int left) 
	{
		if (inp == null)
		{
			inp = "";	
		}
		int size = inp.length();
		String newString = new String(inp);
		while (size < number) 
		{
			if (left == 0) 
			{
				newString = "0" + newString;
			}
			else 
			{
				newString = newString + "0";
			}
			size = newString.length();
		}
		return newString;			
	}
	
	public final static String getSdateFilter (int index, String [] dates) 
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
				sdates[0] = "xx";
			} 
			else if (i == 1) 
			{
				sdates[1] = "01";
			} 
			else if (i == 2) 
			{
				sdates[2] = "xx";
			} 
			else if (i == 3) 
			{
				sdates[3] = "12";
			}
		}
		return sdates[index];
	}
	
	public final static String getSdate (int index, String[] dates) 
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
				sdates[1] = "01";
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
	
	public final static String buildSolrDateFilter(String[] dates, String perMonth)
	{
		if(dates[0] == null && dates[1] == null && dates[2] == null && dates[3] == null)
		{
			return "";
		}
		
		if(perMonth.equals("0"))
		{
			return "time_ano:[" + getSdate(0, dates) + " TO " + getSdate(2, dates) + "]";
		}
		else
		{
			return "time_anomes:[" + getSdate(0, dates) + "-" + StringUtils.pad(getSdate(1, dates), 2, 0) +
				" TO " + getSdate(2, dates) + "-" + StringUtils.pad(getSdate(3, dates), 2, 0) + "]";
		}
	}
	
	private static final String checkType(String type)
	{
		switch (type.toUpperCase()) 
		{
			case "DOWNLOADS": return " AND type:0 AND bundleName:ORIGINAL";
			case "ACESSOS": return " AND type:2";
			default: return "";
		}
	}
	
	private static final String checkOwner(String type)
	{
		switch (type.toUpperCase()) 
		{
			case "DOWNLOADS": return "owningItem:";
			case "ACESSOS": return "id:";
			default: return "";
		}
	}
	
	public static final String buildSolrQuery(String type, boolean addUfrgsClause, TreeMap<String,String> idHandleList,
		String[] dates, String perMonth) 
	{
		StringBuilder solrQuery = new StringBuilder();
		solrQuery.append(buildSolrIdList(type, idHandleList));
		solrQuery.append(" AND statistics_type:view");
		solrQuery.append(checkType(type));
		
		solrQuery.append(buildTimeClause(dates, perMonth));
		
		if(addUfrgsClause) 
		{
			//ufrgs sub-net ipv4 and ipv6
			//solrQuery.append(" AND (ip:143.54.*.* OR ip:2801*80*40*)");
		}
		return solrQuery.toString();
	}
		
	public static final ArrayList<String> buildArraySolrQuery(String type, boolean addUfrgsClause, TreeMap<String,String> idHandleList) 
	{
		ArrayList<String> solrQueries = new ArrayList<String>();
		StringBuilder solrQuery = new StringBuilder();
		
		for(String ids : buildArraySolrIdList(type, idHandleList))
		{
			solrQuery.append(ids);
			solrQuery.append(" AND statistics_type:view");
			solrQuery.append(checkType(type));
			
			if(addUfrgsClause) 
			{
				//ufrgs sub-net ipv4 and ipv6
				//solrQuery.append(" AND (ip:143.54.*.* OR ip:2801*80*40*)");
			}
			
			solrQueries.add(solrQuery.toString());
			solrQuery.delete(0, solrQuery.length());
		}

		return solrQueries;
	}
	
	public static final String buildSolrQuery(String type, boolean addUfrgsClause, String ownerClause, 
		String timeClause)
	{
		StringBuilder solrQuery = new StringBuilder();
		solrQuery.append("statistics_type:view");
		solrQuery.append(checkType(type));
		solrQuery.append(" AND ");
		solrQuery.append(ownerClause);
		
		if(!timeClause.equals(""))
		{
			//solrQuery.append(" AND ");
			solrQuery.append(timeClause);
		}
		
		if(addUfrgsClause) 
		{
			//ufrgs sub-net ipv4 and ipv6
			//solrQuery.append(" AND (ip:143.54.*.* OR ip:2801*80*40*)");
		}
		return solrQuery.toString();
	}
	
	public static final String buildSolrIdList(String type, TreeMap<String,String> idHandleList) 
	{		
		StringBuilder idList = new StringBuilder();
		
		if(idHandleList.size() > 0)
		{
			idList.append("(");
			for (Map.Entry<String, String> entry : idHandleList.entrySet()) 
			{
				idList.append(checkOwner(type));
				idList.append(entry.getKey());
				idList.append(" OR ");
			}
			// delete last " OR "
			idList.delete(idList.length() - 4, idList.length());
			idList.append(")");
		}
		
		return idList.toString();
	}
	
	public static final ArrayList<String> buildArraySolrIdList(String type, TreeMap<String,String> idHandleList) 
	{
		ArrayList<String> queries = new ArrayList<String>();
		StringBuilder idList = new StringBuilder();
		int queryItemsCounter = 0;

		if(idHandleList.size() > 0)
		{
			idList.append("(");
			for (Map.Entry<String, String> entry : idHandleList.entrySet()) 
			{
				if(queryItemsCounter == MAX_ITEMS_PER_QUERY)
				{
					// delete last " OR "
					idList.delete(idList.length() - 4, idList.length());
					idList.append(")");
					queries.add(idList.toString());
					idList.delete(0, idList.length());
					idList.append("(");
					queryItemsCounter = 0;
				}
				idList.append(checkOwner(type));
				idList.append(entry.getKey());
				idList.append(" OR ");
				queryItemsCounter++;
			}
			// delete last " OR "
			idList.delete(idList.length() - 4, idList.length());
			idList.append(")");
			queries.add(idList.toString());
		}
		
		return queries;
	}
	
	public static final String buildTimeClause(String[] dates, String type)
	{
		//restrict time because month also can be selected in filtering
		//só vai filtrar por tempo se houver ao menos um ano preenchido
		if((dates[0] != null || dates[2] != null) && (!dates[0].equals("") || !dates[2].equals("")))
		{
			String timeClause = "time_ano";
			
			//verifica se tem que trocar cláusula de tempo
			if((dates[1] != null || dates[3] != null) && (!dates[1].equals("") || !dates[3].equals("")) || type.equals("1"))
			{
				timeClause = "time_anomes";
				
				// ajusta meses
				if ("".equals(dates[1]) || dates[1] == null) 
				{
					dates[1] = "01";	
				}
				else
				{
					dates[1] = StringUtils.pad(dates[1],2,0);
				}
				if ("".equals(dates[3]) || dates[3] == null) 
				{
					dates[3] = "12";	
				}
				else
				{
					dates[3] = StringUtils.pad(dates[3],2,0);
				}
			}
			
			// ajusta ano final ou inicial se não veio
			if ("".equals(dates[0]) || dates[0] == null) 
			{
				dates[0] = "1900";	
			}
			if ("".equals(dates[2]) || dates[2] == null) 
			{
				dates[2] = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));				
			}
			
			if(timeClause.equals("time_ano"))
			{
				return " AND time_ano:[" + dates[0] + " TO " + dates[2] + "]";
			} 
			else 
			{
				return " AND time_anomes:[" + dates[0] + "-" + dates[1] + " TO " + dates[2] + "-" + dates[3] + "]";
			}
		}
		else
		{
			return "";
		}
	}
	
	public final static String buildTimeClause(String year1, String month1, String year2, String month2) 
	{
		return "time:[" + year1 + "-" + pad(month1, 2, 0) + "-01T00:00:00:001Z TO " + 
			lastDayInMonthDate(pad(month2, 2, 0), year2) + "T23:59:59.999Z]";
	}
	
	private final static String lastDayInMonthDate(String month, String year)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, new Integer(month));
		calendar.set(Calendar.YEAR, new Integer(year));
		calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
		
		Date date = calendar.getTime();
		DateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy");
		return dateFormat.format(date);
	}
}
