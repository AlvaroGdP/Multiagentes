package dominio;

import java.io.IOException;
import java.io.*;
import java.util.regex.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jade.core.Agent;
import jade.core.behaviours.*;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREResponder;
import jade.lang.acl.MessageTemplate;

import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;

public class AgenteAmazon extends Agent{

  private AID intermediario = new AID("intermediario", AID.ISLOCALNAME);
  private ParallelBehaviour par1;

  protected void setup(){
    par1 = new ParallelBehaviour()/*{
      public int onEnd(){
        //par1.addSubBehaviour(new GetProductInfo(myAgent, MessageTemplate.MatchSender(intermediario)));
        par1.reset();
        addBehaviour(par1);
        return 0;
      }
    }*/;
    par1.addSubBehaviour(new GetProductInfo(this, MessageTemplate.and(MessageTemplate.MatchSender(intermediario),MessageTemplate.MatchOntology("Info"))));
    addBehaviour(par1);
  }

  private class GetProductInfo extends AchieveREResponder{

    public GetProductInfo(Agent ag, MessageTemplate mt){
      super(ag, mt);
    }

    protected String obtenerPrecio(Elements precio3) {
      for (int i = 1; i < precio3.size(); i++) {
        if (!precio3.get(i).text().isEmpty() && precio3.get(i) != null) {
          return precio3.get(i).text();
        }
      }
      return "";
    }

    protected ACLMessage handleRequest(ACLMessage msg) throws NotUnderstoodException, RefuseException{
      //Este metodo es necesario para poder lanzar FailureException en prepareResultNotification
      return null;
    }

    protected ACLMessage prepareResultNotification(ACLMessage msg, ACLMessage sent) throws FailureException{
      System.out.println("AgenteAmazon: Obteniendo información del producto. Por favor, espere un momento.");
      String uri = msg.getContent().replace("Libre","");
      uri = uri.replace("Dual Sim", "");
      uri = uri.replace("/"," ");
      String queryAmazon = "https://www.amazon.es/s/url=field-keywords=" + uri;

      Document docAmazon = null;
      Element link = null;
      try{
        docAmazon = Jsoup.connect(queryAmazon).get();
      } catch (Exception e){
        throw new FailureException("Error en la conexión a Amazon.");
      }

      link = docAmazon.select(".a-link-normal.s-access-detail-page.s-color-twister-title-link.a-text-normal").eq(1).first();

      Document docElem = null;
      try{
        docElem = Jsoup.connect(link.absUrl("href")).get();
      } catch(Exception e){
        throw new FailureException("AgenteAmazon: Error accediendo al producto.");
      }

      Element precio1 = null;
      try{
        precio1 = docElem.select("#priceblock_dealprice").first();
      }catch (Exception e){
        //
      }

      Element precio2 = null;
      try{
  		    precio2 = docElem.select("#priceblock_ourprice").first();
      } catch (Exception e){
        //
      }
  		String precio;

  		// Hay varios formatos de precio, debemos comprobarlos todos
  		if (precio2 != null && !precio2.text().isEmpty()) {
  			precio = precio2.text();
  		}else {
  			if (precio1 != null && !precio1.text().isEmpty()) {
  				precio = precio1.text();
  			}else {
  				//Si hemos llegado aqui, hay varias opciones con distinto precio para el producto
  				Elements precio3 = docElem.select(".a-size-mini");
  				precio = obtenerPrecio(precio3);
          precio = precio.replace(",", ".");
  			}
  		}

  		Pattern p = Pattern.compile("EUR [0-9]*(.,[0-9]*)?");
  		Matcher m = p.matcher(precio);
  		if (m.matches()) {
  			precio = precio.substring(4, precio.length());
  			precio = precio.replace(",", ".");
        try{
          Double.parseDouble(precio);
        }catch (Exception e){
          //Forma de comprobar si se ha obtenido de forma correcta
          // En ocasiones se obtenian precios erroneos, incluso tras el match con la expresion regular
          throw new FailureException("AgenteAmazon: Error obteniendo precio del producto.");
        }
  		}

      // Valoración
      String rating="";
      Element ratingStars = docElem.select(".a-declarative .a-icon-alt").first();
  		//Ejemplo de formato: 4.2 de un máximo de 5 estrellas
  		p = Pattern.compile("[0-9]([.,][0-9])? de[ a-zA-Zá0-9]*");
      try{
    		m = p.matcher(ratingStars.text());
    		if(m.matches()) {
    			String[] valoracion = ratingStars.text().split(" de"); //Obtenemos la primera parte de la cadena
          valoracion[0] = valoracion[0].replace(",",".");
    			double valoracionDouble = Double.parseDouble(valoracion[0]);
    			valoracionDouble = valoracionDouble/5; //Dividimos por el maximo
    			valoracionDouble = valoracionDouble*100; //Obtenemos el valor porcentual
          Long aux = Math.round(valoracionDouble);
          rating = Double.toString(aux.doubleValue());
  		  }
      }catch(Exception e){
        throw new FailureException("AgenteAmazon: Error obteniendo valoración del producto.");
      }
      ACLMessage inform = msg.createReply();
      inform.setPerformative(ACLMessage.INFORM);
      inform.setContent("Amazon,"+precio+","+rating+","+link.absUrl("href"));
      return inform;

    }

  }
}
