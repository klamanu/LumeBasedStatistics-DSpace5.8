package org.dspace.app.xmlui.statistics;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Paint;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Community;
import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.dspace.core.ConfigurationManager;
import org.dspace.app.xmlui.statistics.GeneralFunctions;
import org.dspace.app.xmlui.statistics.Tuple;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Arrays;
import org.apache.log4j.Logger;/*
import org.krysalis.jcharts.properties.*;
import org.krysalis.jcharts.encoders.*;
import org.krysalis.jcharts.chartData.*;
import org.krysalis.jcharts.Chart;
import org.krysalis.jcharts.properties.PropertyException;
import org.krysalis.jcharts.chartData.ChartDataException;
import org.krysalis.jcharts.axisChart.AxisChart;
import org.krysalis.jcharts.axisChart.customRenderers.axisValue.renderers.*;
import org.krysalis.jcharts.chartData.interfaces.IAxisDataSeries;
import org.krysalis.jcharts.properties.util.ChartStroke;
import org.krysalis.jcharts.properties.util.ChartFont;
import org.krysalis.jcharts.types.ChartType;
import org.krysalis.jcharts.axisChart.customRenderers.axisValue.renderers.ValueLabelPosition;  
import org.krysalis.jcharts.axisChart.customRenderers.axisValue.renderers.ValueLabelRenderer;/*
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/***
	ChartGenerator
	
	Classe para criar gráficos de barra, duplos ou simples

****/
public class ChartGenerator {
		//Nome do arquivo		
		private String name = "";
		
//		//Propriedades do gráfico
//		private ClusteredBarChartProperties barChartProperties = new ClusteredBarChartProperties();
//		private LegendProperties legendProperties = new LegendProperties();
//		private AxisProperties axisProperties;
//		private ChartProperties chartProperties = new ChartProperties();		
//		private ChartFont axisFont = new ChartFont(new Font("SansSerif", Font.PLAIN, 10), Color.black);
//		
//		//Legendas 
//		private String[] legendLabels;
//		//Cores
//		private Paint[] paints;
//		//Dados
//		private IAxisDataSeries dataSeries;
//		private double[][] data;
//		
//		//Rótulos do eixo x
//		private String[] xAxisLabels = new String[0];
//		Map<String,String> data1;
//		Map<String,String> data2;
//				
//		private AxisChart axisChart;
//				
//		int width;
//		int height;
//		int tipo_chart;
//		
//		private static final Logger log = Logger.getLogger(ChartGenerator.class);
//	
//		/*Construtor
//		Parâmetros:
//		cname: nome do arquivo do grafico
//		orient: orientação dos rótulos no eixo X (VERT para vertical)
//		tipo: tipo 0 é com rótulo de ano simples, tipo 1 é ano/mes
//		*/
//		public ChartGenerator (String cname,String orient, int tipo)
//		{
//			name = cname;
//			if (orient.equals("VERT"))
//				axisProperties =  new AxisProperties(false);
//			else
//				axisProperties =  new AxisProperties(true);
//			//Cores de fundo
//			axisProperties.setBackgroundPaint( new GradientPaint( 0, 0, new Color( 255, 255, 255 ), 0, 300, new	Color( 167, 213, 255 ) ) );
//			tipo_chart = tipo;
//			//Espaço nas bordas
//			chartProperties.setEdgePadding(5f);
//			//Fonte utilizada no texto do gráfico
//			ChartFont axisFont= new ChartFont(new Font("Monospaced", Font.BOLD, 13 ), Color.black );
//			axisProperties.getXAxisProperties().setAxisTitleChartFont( axisFont );
//			axisProperties.getYAxisProperties().setAxisTitleChartFont( axisFont );
//			axisProperties.getXAxisProperties().setScaleChartFont( axisFont );
//			axisProperties.getYAxisProperties().setScaleChartFont( axisFont );
//			legendProperties.setChartFont(new ChartFont(new Font("SansSerif", Font.PLAIN, 10 ), Color.black));
//			//axisProperties.getXAxisProperties().setPaddingBetweenLabelsAndTicks(4.0f);
//			
//			if (tipo == 1)
//				axisProperties.setXAxisLabelsAreVertical( true );
//				//axisProperties.getXAxisProperties().setLabelRotationAngle(90);
//
//		}
//		
//		//funcao para definir a cor
//		//uma opção para cada cor, achei mais facil do que pedir um array de Paint como parâmetro
//		public void setColor(int cor)
//		{			
//			switch(cor)
//			{
//			case 0:
//				paints = new Paint[]{new Color(0,128,255)};
//				break;
//			case 1:
//				paints = new Paint[]{Color.red.darker()};
//				break;
//			case 2:
//				paints = new Paint[]{new Color(0,128,255),Color.red.darker()};
//				break;
//			case 3:		
//				paints = new Paint[]{new Color(0,128,255).brighter(),Color.red};
//				break;
//			case 4:
//				paints = new Paint[]{new Color(135,150,196),new Color(207,130,130)};
//				break;
//			case 5:
//				paints = new Paint[]{new Color(0,64,196),new Color(255,128,0)};
//				break;
//			}
//		}		
//		
//		//Legendas
//		public void setLegendLabels(String[] legendlabels)
//		{
//			legendLabels = legendlabels;
//		}
//		
//		//Define o tamanho do grafico
//		public void setSize(int w,int h)
//		{
//			width = w;
//			height = h;
//		}
//		
//		//Salva o gráfico no disco
//		public void saveChart() throws ChartDataException,FileNotFoundException,PropertyException,IOException,NullPointerException
//		{
//			
//			FileOutputStream fileOutputStream = new FileOutputStream(ConfigurationManager.getProperty("dspace.dir")+"/webapps/xmlui/static/temp/"+name);
//			dataSeries.addIAxisPlotDataSet( new AxisChartDataSet( data,legendLabels, paints, ChartType.BAR_CLUSTERED, barChartProperties ) );
//			axisChart = new AxisChart( dataSeries, chartProperties, axisProperties, legendProperties, width, height );
//			// O segundo valor é a qualidade da codificação JPEG, 1.0f é o máximo
//			JPEGEncoder.encode( axisChart, 1.0f, fileOutputStream );
//			fileOutputStream.flush();
//			fileOutputStream.close();
//		
//		}
//		
//		
//		
//		/**
//			Função utilizada em StatsCodigoNovo
//			Para gerar os gráficos de downloads/acessos por data
//			Recebe um map com os anos (YYYY) ou ano/mes (YYYYMM), e, para cada data, puxa os dados de data_downloads e data_acessos além de adicionar a data aos rótulos do eixo X
//		**/
//		
//		public void setDownloadsAcessosData(Map<String,String> subAnos,Map<String,String> data_downloads,Map<String,String> data_acessos) throws ArrayIndexOutOfBoundsException,StringIndexOutOfBoundsException
//		{
//		
//			//Array de duas dimensões para que cada valor no eixo X tenha dois valores no eixo Y
//			data = new double[2][0];
//			
//			for (Iterator iter = subAnos.entrySet().iterator(); iter.hasNext();)
//			{ 
//				Map.Entry entry = (Map.Entry)iter.next();
//				String key = (String)entry.getKey();
//				String value = (String)entry.getValue();
//				String conta_downloads_string = data_downloads.get(key);
//				double count_downloads;
//				double count_acessos;
//			
//				//alguns testes pra ver se o valor existe ou está nulo, pra não dar erro
//				if (conta_downloads_string == null)
//					count_downloads = 0;
//				else
//					count_downloads = Double.valueOf(conta_downloads_string).doubleValue();
//				
//				String conta_acessos_string = data_acessos.get(key);
//				if (conta_acessos_string == null)
//					count_acessos = 0;
//				else
//					count_acessos = Double.valueOf(conta_acessos_string).doubleValue();
//				if (new Double(count_downloads).equals(null))
//					count_downloads = 1;
//				if (new Double(count_acessos).equals(null))
//					count_acessos = 1;
//				try
//				{
//					//adiciona o numero de downloads na dimensão 0
//					//adiciona o número de acessos na dimensão 1
//					data[0] = GeneralFunctions.addArrayElement(data[0],count_downloads);
//					data[1] = GeneralFunctions.addArrayElement(data[1],count_acessos);		
//				}
//				catch (ArrayIndexOutOfBoundsException aoobe)
//				{
//					throw aoobe;
//				}
//				
//				try
//				{
//					//Adiciona o rótulo
//					if (tipo_chart == 0)
//						xAxisLabels = GeneralFunctions.addArrayElement(xAxisLabels,key);
//					else
//						xAxisLabels = GeneralFunctions.addArrayElement(xAxisLabels,new String(key.substring(4,6)+"/"+key.substring(0,4)));
//				}
//				catch (ArrayIndexOutOfBoundsException aoobe)
//				{
//					throw aoobe;
//				}
//				catch (StringIndexOutOfBoundsException sioobe)
//				{
//					throw sioobe;
//				}
//			}
//			dataSeries = new DataSeries( xAxisLabels, null,null, null );
//		}
//		
//		/**	
//			Funcao usada em StatsCodigoNovo
//			Para gerar os gráficos dos países
//			Recebe um arraylist de Tuple, onde a chave é a sigla do país e o valor é o número de acessos ou downloads
//			Recebe também um hashmap com as siglas e com os nomes
//			
//		**/
//		public void setCountryData(ArrayList in_map,HashMap hashmapcountryCodetoindex,String[] countryName) throws ArrayIndexOutOfBoundsException,StringIndexOutOfBoundsException
//		{
//
//			data = new double[1][0];
//			int ind = 0;
//		
//			//Percorre os dados
//			for (Iterator iter = in_map.iterator(); iter.hasNext(); )
//			{ 
//				Tuple entry = (Tuple)iter.next();
//				if (entry == null) 
//					throw new NullPointerException("npe");
//				String string_count = entry.getValue();
//				double count;
//				//Se for nulo, coloca 0 para nao dar erro
//				if (string_count == null)
//					count = 0;
//				else
//					count = Double.valueOf(string_count).doubleValue();
//					
//				//Se for null coloca 1 para nao dar erro
//				if (new Double(count).equals(null))
//					count = 1;
//				try
//				{
//					data[0] = GeneralFunctions.addArrayElement(data[0],count);
//				}
//				catch (ArrayIndexOutOfBoundsException aoobe)
//				{
//					throw aoobe;
//				}
//				
//				try
//				{
//					Integer i = (Integer)hashmapcountryCodetoindex.get(entry.getKey().toUpperCase());
//					if (i == null)
//						throw new NullPointerException("i e null");
//				
//					//Adiciona o nome do pais nos rotulos do eixo X
//					xAxisLabels = GeneralFunctions.addArrayElement(xAxisLabels,countryName[i.intValue()]);
//					/*if(countryName[i.intValue()].length() <= 10)
//					{
//						xAxisLabels = GeneralFunctions.addArrayElement(xAxisLabels,countryName[i.intValue()]);
//					}
//					else
//					{
//						xAxisLabels = GeneralFunctions.addArrayElement(xAxisLabels,countryName[i.intValue()].substring(0,9));
//					}*/
//				}
//				catch (ArrayIndexOutOfBoundsException aoobe)
//				{
//					throw aoobe;
//				}
//				ind++;
//				
//				//Adicionamos somente 10 paises
//				if (ind == 10) break;
//				
//			}
//			
//			dataSeries = new DataSeries( xAxisLabels, null,null, null );				
//			
//		}	
//		
//		/**
//			Funcao usada em StatsPorComunidade
//			Parecida com a funcao setDownloadsAcessosData, porém esta aqui adiciona os nomes de comunidades e coleções nos rótulos ao invés de anos ou países
//		**/
//		public void setCommunityData(Context context,Iterator iter,Iterator iter2) throws SQLException,ArrayIndexOutOfBoundsException,StringIndexOutOfBoundsException
//		{
//			data = new double[2][0];
//			
//			while( iter.hasNext() )
//			{
//				String[] array = (String[]) iter.next();
//				String[] array2 = (String[]) iter2.next();
//				String handle = "";
//				String name = "";
//				
//				if (array[2].equals("COM"))
//				{
//					Community c = Community.find(context,Integer.parseInt(array[0]));					
//					name = c.getName();					
//				}
//				else if (array[2].equals("COL"))
//				{
//					Collection c = Collection.find(context,Integer.parseInt(array[0]));
//					name = c.getName();
//				}
//				
//				//Se tem mais de uma palavra no nome, e a palavra tiver mais de 8 letras, coloca só a primeira letra
//				if(name.length() > 30)
//				{
//					String[] name_split = name.split(" "); 	
//					if (name_split.length > 1)
//					{
//						ArrayList<String> ar = new ArrayList<String>();
//						for (String s : name_split)
//						{
//
//							if (s.length() > 8)	//se a palavra do nome for maior que 8 caracteres abrevia com 1. 
//							{
//								//quero encontrar a segunda vogal
//								String[] word_split = s.split("[aáâãàeéêiíoóôõuúü]"); //separo a palavra pelas vogais
//								int second_vowel_index = word_split[0].length()+1+word_split[1].length();   //nao esquecer que começa no zero
//								while(s.substring(second_vowel_index,second_vowel_index+1).matches("[aáâãàeéêiíoóôõuúü]")==false)  //testa se é mesmo vogal.
//								{
//									second_vowel_index=second_vowel_index+1;
//								}
//								ar.add(s.substring(0, second_vowel_index)+".");			
//								
//							}
//						
//							else
//								ar.add(s);
//						}
//					
//						xAxisLabels = GeneralFunctions.addArrayElement(xAxisLabels,GeneralFunctions.join(ar," "));
//					}
//				}
//				else
//				{
//					xAxisLabels = GeneralFunctions.addArrayElement(xAxisLabels,name);
//				}
//				data[0] = GeneralFunctions.addArrayElement(data[0],Double.valueOf(array[1]).doubleValue());
//				data[1] = GeneralFunctions.addArrayElement(data[1],Double.valueOf(array2[1]).doubleValue());
//			
//				
//				
//			}
//			dataSeries = new DataSeries( xAxisLabels, null,null, null );
//		}
//		
//		
//		//Funcao que inclui os dados de numero de itens em cada comunidade/colecao
//		public void setCommunityData2(Context context,Iterator iter,Iterator iter2) throws SQLException,ArrayIndexOutOfBoundsException,StringIndexOutOfBoundsException
//		{
//			data = new double[2][0];
//			
//			while( iter.hasNext() )
//			{
//				String[] array = (String[]) iter.next();
//				String[] array2 = (String[]) iter2.next();
//				String handle = "";
//				String name = "";
//				
//				if (array[2].equals("COM"))
//				{
//					Community c = Community.find(context,Integer.parseInt(array[0]));					
//					name = c.getName();					
//				}
//				else if (array[2].equals("COL"))
//				{
//					Collection c = Collection.find(context,Integer.parseInt(array[0]));
//					name = c.getName();
//				}
//				
//				
//				String[] name_split = name.split(" ");
//				//Se tem mais de uma palavra no nome, e a palavra tiver mais de 8 letras, coloca só a primeira letra
//				if (name.length() > 30)
//				{
//					if (name_split.length > 1)
//					{
//						ArrayList<String> ar = new ArrayList<String>();
//						for (String s : name_split)
//						{
//
//							if (s.length() > 8)
//							{
//							
//								//quero encontrar a segunda vogal
//								String[] word_split = s.split("[aáâãàeéêiíoóôõuúü]"); //separo a palavra pelas vogais
//								int second_vowel_index = word_split[0].length()+1+word_split[1].length();   //nao esquecer que começa no zero
//								while(s.substring(second_vowel_index,second_vowel_index+1).matches("[aáâãàeéêiíoóôõuúü]")==false)  //testa se é mesmo vogal.
//								{
//									second_vowel_index=second_vowel_index+1;
//								}
//								ar.add(s.substring(0, second_vowel_index)+".");		
//							}
//						
//							else
//								ar.add(s);
//						}
//					
//						xAxisLabels = GeneralFunctions.addArrayElement(xAxisLabels,GeneralFunctions.join(ar," "));
//					}
//				}
//				else
//				{
//					xAxisLabels = GeneralFunctions.addArrayElement(xAxisLabels,name);
//				}
//				if (!array[3].equals("0"))
//				{
//					data[0] = GeneralFunctions.addArrayElement(data[0],Double.valueOf(array[1]).doubleValue()/Double.valueOf(array[3]).doubleValue());
//					data[1] = GeneralFunctions.addArrayElement(data[1],Double.valueOf(array2[1]).doubleValue()/Double.valueOf(array[3]).doubleValue());
//				}
//				else
//				{
//					data[0] = GeneralFunctions.addArrayElement(data[0],Double.valueOf(array[1]).doubleValue());
//					data[1] = GeneralFunctions.addArrayElement(data[1],Double.valueOf(array2[1]).doubleValue());			
//				}
//			
//				
//				
//			}
//			dataSeries = new DataSeries( xAxisLabels, null,null, null );
//		}
//		
//		/***
//		
//			Funcao usada em BrowseStats e QueryStats
//		
//		***/
//		
//		public void setDownloadsAcessosBrowseData(Map<String,String> subAnos,Map<String,String> dados,Map<String,String> dados2) throws ArrayIndexOutOfBoundsException,StringIndexOutOfBoundsException
//		{
//			data = new double[2][0];
//			
//			for (Iterator iter = subAnos.entrySet().iterator(); iter.hasNext();)
//			{ 
//				Map.Entry entry = (Map.Entry)iter.next();
//				String key = (String)entry.getKey();
//				String value = (String)entry.getValue();
//				String conta_downloads_string = dados.get(key);
//				double count_downloads;
//				double count_acessos;
//			
//				if (conta_downloads_string == null)
//					count_downloads = 0;
//				else
//					count_downloads = Double.valueOf(conta_downloads_string).doubleValue();
//				
//				String conta_acessos_string = dados2.get(key);
//				if (conta_acessos_string == null)
//					count_acessos = 0;
//				else
//					count_acessos = Double.valueOf(conta_acessos_string).doubleValue();
//				if (new Double(count_downloads).equals(null))
//					count_downloads = 1;
//				if (new Double(count_acessos).equals(null))
//					count_acessos = 1;
//				try
//				{
//					data[0] = GeneralFunctions.addArrayElement(data[0],count_downloads);
//					data[1] = GeneralFunctions.addArrayElement(data[1],count_acessos);		
//				}
//				catch (ArrayIndexOutOfBoundsException aoobe)
//				{
//					throw aoobe;
//				}
//				
//				try
//				{
//					if (tipo_chart == 0)
//						xAxisLabels = GeneralFunctions.addArrayElement(xAxisLabels,key);
//					else
//						xAxisLabels = GeneralFunctions.addArrayElement(xAxisLabels,new String(key.substring(4,6)+"/"+key.substring(0,4)));
//				}
//				catch (ArrayIndexOutOfBoundsException aoobe)
//				{
//					throw aoobe;
//				}
//				catch (StringIndexOutOfBoundsException sioobe)
//				{
//					throw sioobe;
//				}
//			}
//			dataSeries = new DataSeries( xAxisLabels, null,null, null );
//			
//		}
//		
//		/***
//			Funcao usada em BrowseStats e QueryStats
//		
//		***/
//		
//		public void setBrowseCountryData(Map<String,String> dados,Iterator iterador,String showAll,HashMap hashmapcountryCodetoindex,String[] countryName) throws ArrayIndexOutOfBoundsException,StringIndexOutOfBoundsException
//		{
//		
//			data = new double[1][0];
//			int ind = 0;
//			for(Iterator iter = iterador; iter.hasNext();)
//			{
//				try {    
//				String key = (String) iter.next();
//				String value = dados.get(key);
//				double count;
//				
//				//Se for nulo, coloca 0 para nao dar erro
//				if (value == null)
//					count = 0;
//				else
//					count = Double.valueOf(value).doubleValue();
//				
//				//Se for null coloca 1 pra nao dar erro (?)
//				if (new Double(count).equals(null))
//					count = 1;
//				
//				try
//				{
//					data[0] = GeneralFunctions.addArrayElement(data[0],count);
//				}
//				catch (ArrayIndexOutOfBoundsException aoobe)
//				{
//					throw aoobe;
//				}
//				
//
//				
//				Integer i = (Integer)hashmapcountryCodetoindex.get(key.toUpperCase());
//				if (i == null)
//				throw new NullPointerException("i eh null");
//				
//				//Adiciona o nome do pais nos rotulos do eixo X
//					xAxisLabels = GeneralFunctions.addArrayElement(xAxisLabels,countryName[i.intValue()]);				                         
//				}
//				catch (NullPointerException e)
//			   {
//					throw e;
//			   }
//			   ind++;
//			   //Adicionamos somente 10 paises
//				if (ind == 10) break;
//			}
//			
//			dataSeries = new DataSeries( xAxisLabels, null,null, null );
//		
//		}

}
