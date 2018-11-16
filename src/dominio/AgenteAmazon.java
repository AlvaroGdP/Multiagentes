package dominio;

import java.io.IOException;
import java.util.regex.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jade.core.Agent;
import jade.core.behaviours.*;

public class AgenteAmazon extends Agent{

  private String query = "OnePlus 6T 8GB/128GB Mirror Black Libre";
  private SequentialBehaviour seq1;

  protected void setup(){
    seq1 = new SequentialBehaviour();
    seq1.addSubBehaviour(new GetURL());
    addBehaviour(seq1);
  }

  protected String obtenerPrecio(Elements precio3) {
    for (int i = 1; i < precio3.size(); i++) {
      if (!precio3.get(i).text().isEmpty() && precio3.get(i) != null) {
        return precio3.get(i).text();
      }
    }
    return "";
  }

  public class GetURL extends OneShotBehaviour{

    Element link;

    public void action(){
      String queryPc = "https://www.amazon.es/s/url=field-keywords=" + query;
      try{
        Document docPc = Jsoup.connect(queryPc).get();
        link = docPc.select(".a-link-normal").first();
      } catch (Exception e){
        System.out.println("Error en la conexión");
      }
    }

    public int onEnd(){
      seq1.addSubBehaviour(new GetPrice(link));
      seq1.addSubBehaviour(new GetRating(link));
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

      Element precio1 = docElem.select("#priceblock_dealprice").first();
  		Element precio2 = docElem.select("#priceblock_ourprice").first();
  		String precio;
  		// Hay varios formatos de precio, debemos comprobarlos todos
  		if (!precio2.text().isEmpty() && precio2 != null) {
  			precio = precio2.text();
  		}else {
  			if (!precio1.text().isEmpty() && precio1 != null) {
  				precio = precio1.text();
  			}else {
  				//Si hemos llegado aqui, hay varias opciones con distinto precio para el producto
  				Elements precio3 = docElem.select(".a-size-mini");
  				precio = obtenerPrecio(precio3);
          precio = precio.replace(",", ".");
          this.precio = Double.parseDouble(precio);
  			}
  		}

  		Pattern p = Pattern.compile("EUR [0-9]*(.,[0-9]*)?");
  		Matcher m = p.matcher(precio);
  		if (m.matches()) {
  			precio = precio.substring(4, precio.length());
  			precio = precio.replace(",", ".");
  			this.precio = Double.parseDouble(precio);
  			// Return mensaje con el precio como double
  		}
    }

    public int onEnd(){
      System.out.println("Precio: "+this.precio);
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

      Element ratingStars = docElem.select(".a-declarative .a-icon-alt").first();
  		//Ejemplo de formato: 4.2 de un máximo de 5 estrellas
  		Pattern p = Pattern.compile("[0-9]([.,][0-9])? de[ a-zA-Zá0-9]*");
  		Matcher m = p.matcher(ratingStars.text());
  		if(m.matches()) {
  			String[] valoracion = ratingStars.text().split(" de"); //Obtenemos la primera parte de la cadena
  			double valoracionDouble = Double.parseDouble(valoracion[0]);
  			valoracionDouble = valoracionDouble/5; //Dividimos por el maximo
  			valoracionDouble = valoracionDouble*100; //Obtenemos el valor porcentual
        Long cosa = Math.round(valoracionDouble);
        this.rating = cosa.doubleValue();
  			//return mensaje con valoracion
  		}
    }

    public int onEnd(){
      System.out.println("Valoración: "+this.rating);
      return super.onEnd();
    }

  }

}
