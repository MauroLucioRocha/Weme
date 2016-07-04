package br.com.elede.mauro.weme;

/**
 * Created by Mauro on 28/06/2016.
 */
public class Usuario {

    private String nome_fb, imagem_fb;
    private int id;

    public Usuario (String nome_fb, String imagem_fb, int id){
        this.nome_fb = nome_fb;
        this.imagem_fb = imagem_fb;
        this.id = id;
    }

    public Usuario (){

    }

    public String getImagem_fb() {
        return imagem_fb;
    }

    public void setImagem_fb(String imagem_fb) {
        this.imagem_fb = imagem_fb;
    }

    public String getNome_fb() {
        return nome_fb;
    }

    public void setNome_fb(String nome_fb) {
        this.nome_fb = nome_fb;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
