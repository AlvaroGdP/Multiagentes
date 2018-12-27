package dominio;

import java.io.IOException;
import java.util.regex.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import jade.core.Agent;
import jade.core.behaviours.*;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREResponder;
import jade.lang.acl.MessageTemplate;

import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;

public class AgentePC extends Agent{

  private AID intermediario = new AID("intermediario", AID.ISLOCALNAME);
  private SequentialBehaviour seq1;

  protected void setup(){
    seq1 = new SequentialBehaviour();
    seq1.addSubBehaviour(new GetName(this, MessageTemplate.MatchSender(intermediario)));
    addBehaviour(seq1);
  }

  private class GetName extends AchieveREResponder{

    public GetName(Agent ag, MessageTemplate mt){
      super(ag, mt);
    }

    protected ACLMessage handleRequest(ACLMessage msg) throws NotUnderstoodException, RefuseException{
      //Este metodo es necesario para poder lanzar FailureException en prepareResultNotification
      return null;
    }

    protected ACLMessage prepareResultNotification(ACLMessage msg, ACLMessage sent) throws FailureException{
      System.out.println("AgentePC: Buscando producto con las especificaciones dadas. Por favor, espere un momento.");
      String queryPc = "https://www.pccomponentes.com/smartphone-moviles/" + msg.getContent();
      Element link = null;
      try{
        Document docPc = Jsoup.connect(queryPc).get();
        link = docPc.select(".GTM-productClick.enlace-superpuesto").first();
      } catch (Exception e){
        throw new FailureException("Error en la conexión a PC Componentes.");
      }

      Document docElem = null;
      try{
        docElem = Jsoup.connect(link.absUrl("href")).get();
      } catch(Exception e){
        throw new FailureException("AgentePC: Error obteniendo nombre del producto en PC Componentes.");
      }

  		Element nombre = docElem.select(".h4").first();

      ACLMessage inform = msg.createReply();
      inform.setPerformative(ACLMessage.INFORM);
      inform.setContent(nombre.text());
      return inform;
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
  		}
    }

    public int onEnd(){
      System.out.println("Valoración: "+this.rating);
      return super.onEnd();
    }

  }

}

