package dominio;

import java.io.IOException;
import java.util.regex.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import jade.core.Agent;
import jade.core.behaviours.*;

public class AgentePC extends Agent{

  private String query = "portatil acer";
  private SequentialBehaviour seq1;

  protected void setup(){
    seq1 = new SequentialBehaviour();
    seq1.addSubBehaviour(new GetURL());
    addBehaviour(seq1);
  }

  public class GetURL extends OneShotBehaviour{

    Element link;

    public void action(){
      String queryPc = "https://www.pccomponentes.com/buscar/?query=" + query;
      try{
        Document docPc = Jsoup.connect(queryPc).get();
        link = docPc.select(".GTM-productClick").first();
      } catch (Exception e){
        System.out.println("Error en la conexión");
      }
    }

    public int onEnd(){
      System.out.println(link);
      seq1.addSubBehaviour(new GetName(link));
      return super.onEnd();
    }

  }

  public class GetName extends OneShotBehaviour{

    Element link;
    String nombre;

    public GetName(Element link){
      this.link = link;
    }

    Document docElem = null;
    public void action(){
      try{
        docElem = Jsoup.connect(link.absUrl("href")).get();
      } catch(Exception e){
        System.out.println("Error de conexión");
      }

  		Element nombre = docElem.select(".h4").first();
      this.nombre = nombre.text();
    }

    public int onEnd(){
      System.out.println(this.nombre);
      seq1.addSubBehaviour(new GetPrice(link));
      return super.onEnd();
    }

  }

  public class GetPrice extends OneShotBehaviour{

    Element link;
    Double precio;

    public GetPrice(Element link){
      this.link = link;
    }

    Document docElem = null;
    public void action(){
      try{
        docElem = Jsoup.connect(link.absUrl("href")).get();
      } catch(Exception e){
        System.out.println("Error de conexión");
      }

      Element precio = docElem.select("#precio-main").first();
  		//Comprobamos que hemos recibido un precio correcto
  		Pattern p = Pattern.compile("[0-9]*(.,[0-9]*)? [€$]");
  		Matcher m = p.matcher(precio.text());
  		if (m.matches()) {
  			String coste = precio.text();
  			coste = coste.substring(0, coste.length() - 2);
  			coste = coste.replace(",", ".");
  			this.precio = Double.parseDouble(coste);
  			//Return mensaje con el precio como double
  		}
    }

    public int onEnd(){
      System.out.println("Precio: "+this.precio);
      seq1.addSubBehaviour(new GetRating(link));
      return super.onEnd();
    }

  }

  public class GetRating extends OneShotBehaviour{

    Element link;
    Double rating;

    public GetRating(Element link){
      this.link = link;
    }

    Document docElem = null;
    public void action(){
      try{
        docElem = Jsoup.connect(link.absUrl("href")).get();
      } catch(Exception e){
        System.out.println("Error de conexión");
      }

      Element ratingStars = docElem.select(".rating-stars").first();
  		String rating = ratingStars.attr("style");
  		//Comprobamos que coincide con el patron encontrado en la pagina web
  		Pattern p = Pattern.compile("width: [0-9]*.[0-9]*%;");
  		Matcher m = p.matcher(rating);
  		if (m.matches()) {
  			rating = rating.substring(7, rating.length() - 2);

        rating = rating.replace(",", ".");
        this.rating = Double.parseDouble(rating);
  			//Return Mensaje con el rating sin
  		}
    }

    public int onEnd(){
      System.out.println("Valoración: "+this.rating);
      return super.onEnd();
    }

  }

}
