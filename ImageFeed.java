package br.com.elede.mauro.weme;

/**
 * Created by Mauro on 28/06/2016.
 */
public class ImageFeed {

    private String nome_fb, imagem_feed;
    private int id, curtidas;

    public ImageFeed(String nome_fb, String imagem_feed, int id, int curtidas){
        this.nome_fb = nome_fb;
        this.imagem_feed = imagem_feed;
        this.id = id;
        this.curtidas = curtidas;
    }

    public ImageFeed(){

    }

    public String getImagem_feed() {
        return imagem_feed;
    }

    public void setImagem_feed(String imagem_feed) {
        this.imagem_feed = imagem_feed;
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

    public int getCurtidas() {
        return curtidas;
    }

    public void setCurtidas(int curtidas) {
        this.curtidas = curtidas;
    }
}
