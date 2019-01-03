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
      uri = uri.replace("/","+");
      String queryAmazon = "https://www.amazon.es/s/url=field-keywords=" + uri;

      Element link = null;
      try{
        Document docAmazon = Jsoup.connect(queryAmazon).get();
        link = docAmazon.select(".a-link-normal").eq(2).first();
      } catch (Exception e){
        System.out.println("Error en la conexión");
      }
      Document docElem = null;
      try{
        docElem = Jsoup.connect(link.absUrl("href")).get();
      } catch(Exception e){
        System.out.println("Error de conexión");
      }

      Element precio1 = docElem.select("#priceblock_dealprice").first();
  		Element precio2 = docElem.select("#priceblock_ourprice").first();
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
    			//return mensaje con valoracion
  		  }
      }catch(Exception e){
        System.out.println("An error occurred taking the rating, by default set to 0.");
        rating="0.0";
      }
      ACLMessage inform = msg.createReply();
      inform.setPerformative(ACLMessage.INFORM);
      inform.setContent("Amazon,"+precio+","+rating+","+link.absUrl("href"));
      return inform;

    }

  }
}
