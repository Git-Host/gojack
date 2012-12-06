package it.ciopper90.gojack2.utils;

public class SMS {
	private int id;	
	private String nome;
	private String numero;
	private String testo;
	
	
	public SMS(int id,String nome,String numero,String testo){
		this.nome=nome;
		this.testo=testo;
		this.numero=numero;
		this.id=id;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getNome() {
		return nome;
	}


	public void setNome(String nome) {
		this.nome = nome;
	}


	public String getNumero() {
		return numero;
	}


	public void setNumero(String numero) {
		this.numero = numero;
	}


	public String getTesto() {
		return testo;
	}


	public void setTesto(String testo) {
		this.testo = testo;
	}


	@Override
	public String toString() {
		return "\""+ nome + "\"<" + numero + ">\n " + testo;
	}

	
}
