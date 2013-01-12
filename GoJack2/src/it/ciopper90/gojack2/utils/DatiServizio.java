package it.ciopper90.gojack2.utils;

public class DatiServizio {
	private String nome;
	private String url;
	private int dati[];
	
	public DatiServizio(String nome,int primo,int secondo,int terzo,int quarto,int maxsms,int maxchar,String url){
		this.nome=nome;
		this.url=url;
		dati=new int[6];
		dati[0]=primo;
		dati[1]=secondo;
		dati[2]=terzo;
		dati[3]=quarto;
		dati[4]=maxsms;
		dati[5]=maxchar;	
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int[] getDati() {
		return dati;
	}

	public void setDati(int[] dati) {
		this.dati = dati;
	}
	
	
}
