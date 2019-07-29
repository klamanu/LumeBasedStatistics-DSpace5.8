package org.dspace.app.xmlui.statistics;

public class Tuple implements Comparable<Tuple> 
{

	private String key;
	private String name;
	private String value;
	
	public Tuple(String chave,String valor) 
	{
		this.key = chave;
		this.value = valor;
	}
	
	public Tuple(String chave,String valor, String name) 
	{
		this.key = chave;
		this.value = valor;
		this.name = name;
	}
	
	public String getKey() 
	{
		return this.key;
	}
	
	public String getValue() 
	{
		return this.value;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public int compareTo(Tuple t) 
	{
		Integer i1 = new Integer(this.value);
		Integer i2 = new Integer(t.getValue());
		
		if (i1.compareTo(i2) == 0) 
		{
			return this.key.compareTo(t.getKey());			
		}
		else 
		{
			return (-1) * i1.compareTo(i2);
		}
	}
}