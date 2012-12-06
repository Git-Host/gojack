package it.ciopper90.gojack2.utils;

public class ParametriServizio {
	private String [] parametri;
	
	public ParametriServizio(String nome,String primo,String secondo,String terzo,String quarto,String url){
		parametri=new String[6];
		parametri[0]=nome;
		parametri[1]=primo;
		parametri[2]=secondo;
		parametri[3]=terzo;
		parametri[4]=quarto;
		parametri[5]=url;
	}

	public String[] getParametri() {
		return parametri;
	}
	public String getUrl(){
		return parametri[5];
	}
	

}
