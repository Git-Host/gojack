package ciopper90.gojack.utility;


public class Servizio{
	private String dati[];//nome,url,servizio,primo,secondo,terzo,quarto
	@SuppressWarnings("unused")
	private int rimanenti;
	private String id;
 
	public Servizio(String name,String servizio, String primo, String secondo,String terzo,String quarto,String url,String firma,String id) {
		super();
		dati=new String[8];
		dati[0]=name;
		dati[1]=url;
		dati[2]=primo;
		dati[3]=secondo;
		dati[4]=terzo;
		dati[5]=quarto;
		dati[6]=firma;
		dati[7]=servizio;
		this.id=id;
	}
	public String getName(){
		return dati[0];
	}
	public String getUrl(){
		return dati[1];
	}
	public String getPrimo(){
		return dati[2];
	}
	public String getSecondo(){
		return dati[3];
	}
	public String getTerzo(){
		return dati[4];
	}
	public String getQuarto(){
		return dati[5];
	}
	public String getFirma(){
		return dati[6];
	}
	public void setName(String name){
		dati[0]=name;
	}
	public String getId() {
		return id;
	}
	public String getServizio() {
		return dati[7];
	}
	
	
}

	