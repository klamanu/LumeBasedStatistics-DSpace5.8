package org.dspace.app.xmlui.statistics;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.dspace.app.xmlui.statistics.Tuple;

public class GeneralFunctions
{
	public static List sortByValue(final Map m, final int by) 
	{		
        List keys = new ArrayList();
        keys.addAll(m.keySet());
        Collections.sort(keys, new Comparator() 
		{
            public int compare(Object o1, Object o2) 
			{
                Object v1 = m.get(o1);
                Object v2 = m.get(o2);
                if (v1 == null) 
				{
                    return (v2 == null) ? 0 : 1;
                } 
				else if (v1 instanceof Comparable) 
				{
					Integer i1 = new Integer((String) v1);
					Integer i2 = new Integer((String) v2);
					if (i1.compareTo(i2) == 0) 
					{
						return ((String) o2).compareTo((String) o2);			
					} 
					else 
					{
						return by*i1.compareTo(i2);
					}
                } 
				else 
				{
                    return 0;
                }
            }
        });
        return keys;
    }
	
	//Funcao para adicionar no fim de um array
	//Mas nao eh utilizada
	protected String[] addToArray(String[] array, String s)
	{
		String[] ans = new String[array.length+1];
		System.arraycopy(array, 0, ans, 0, array.length);
		ans[ans.length] = s;
		return ans;
	}
	
	//Outra funcao para adicionar um elemento ao final de um array de String
	public static String[] addArrayElement(String[] array, String s)
	{
		String[] newarray = new String[array.length + 1];
		for (int i = 0;i < array.length;i++)
		{
			newarray[i] = array[i];
		}
		newarray[array.length] = s;
		return newarray;
	} 
	
	//Funcao para adicionar elemento ao final de um array de double
	public static double[] addArrayElement(double[] array, double d)
	{
		double[] newarray = new double[array.length + 1];
		for (int i = 0;i < array.length;i++)
		{
			newarray[i] = array[i];
		}
		newarray[array.length] = d;
		return newarray;
	} 

	//Retorna o maior valor em int de um Map<String,String>
	public int getMax(Map sm)
	{
		Integer max = 0;
		for (Iterator iter = sm.entrySet().iterator(); iter.hasNext();)
		{
			Map.Entry entry = (Map.Entry)iter.next();
            String key = (String)entry.getKey();
			String value = (String)entry.getValue();
			Integer chave = new Integer(value);
			if (max.compareTo(chave) < 0)
				max = chave;
		}
		return max.intValue();
	}
	
	//Retorna o maior valor em int de um ArrayList<Tuple>
	public int getMax2(ArrayList sm)
	{
		Integer max = 0;
		for (Iterator iter = sm.iterator(); iter.hasNext();)
		{			
			try
			{
			Tuple value = (Tuple)iter.next();
			
			Integer chave = new Integer(value.getValue());
			if (max.compareTo(chave) < 0)
				max = chave;
			}
			catch (NullPointerException npe)
			{
				//log.info("NullPointer"+npe.getMessage());
				throw npe;
			}
			catch (ClassCastException cce)
			{
				//log.info("ClassCastException"+cce.getMessage());
				throw cce;
			}

			
		}
		return max.intValue();
	}
	
	//Preenche uma string com 0's à esquerda ou à direita
	public String pad(String inp,int number, int left)
	{
			int tamanho = inp.length();
			String novastring = new String(inp);
			while (tamanho < number)
			{
				if (left == 0)
					novastring = "0" + novastring;
				else
					novastring = novastring + "0";
				tamanho = novastring.length();
			}
			return novastring;	
		
	}
	
	
	//Comparador para strings
	//Compara os valores inteiros das strings
	public static Comparator integerComparator() {
	return new Comparator() {
	   
		public int compare(Object o1, Object o2) {
		String s1 = (String)o1;
		String s2 = (String)o2;
		if (s1.contains("\0") || s2.contains("\0"))
		{		
			if (s1.contains("\0"))
				s1 = s1.substring(0,s1.length()-1);
			if (s2.contains("\0"))
				s2 = s2.substring(0,s2.length()-1);
	
			int val1 = Integer.parseInt(s1);
			int val2 = Integer.parseInt(s2);
			//log.info(s1 + " - " + s2);
			return (val1<val2 ? -1 : (val1==val2 ? 1 : 1));
			
			
		}
		
		else
		{
			int val1 = Integer.parseInt(s1);
			int val2 = Integer.parseInt(s2);
			return (val1<val2 ? -1 : (val1==val2 ? 0 : 1));
		}
		
		}
	};
	}
	
	//funcao equivalente à implode do PHP
	//Transforma uma lista de string em uma string unida por "delimiters"
	public static String join(List<? extends CharSequence> s, String delimiter)
	{
		int capacity = 0;
		int delimLength = delimiter.length();
		Iterator<? extends CharSequence> iter = s.iterator();
		if (iter.hasNext()) {
			capacity += iter.next().length() + delimLength;
		}

		StringBuilder buffer = new StringBuilder(capacity);
		iter = s.iterator();
		if (iter.hasNext()) {
			buffer.append(iter.next());
			while (iter.hasNext()) {
			buffer.append(delimiter);
			buffer.append(iter.next());
			}
		}
		return buffer.toString();
	}
	
}