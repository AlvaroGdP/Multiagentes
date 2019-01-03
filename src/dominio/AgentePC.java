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
  private ParallelBehaviour par1;

  protected void setup(){
    par1 = new ParallelBehaviour()/*{
      public int onEnd(){
        //par1.addSubBehaviour(new GetName(myAgent, MessageTemplate.MatchSender(intermediario)));
        //par1.addSubBehaviour(new GetProductInfo(myAgent, MessageTemplate.MatchSender(intermediario)));
        par1.reset();
        addBehaviour(par1);
        return 0;
      }
    }*/;
    par1.addSubBehaviour(new GetName(this, MessageTemplate.and(MessageTemplate.MatchSender(intermediario),MessageTemplate.MatchOntology("Nombre"))));
    par1.addSubBehaviour(new GetProductInfo(this, MessageTemplate.and(MessageTemplate.MatchSender(intermediario),MessageTemplate.MatchOntology("Info"))));
    addBehaviour(par1);
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
      String queryPc = "https://www.pccomponentes.com/smartphone-moviles" + msg.getContent();

      Element link = null;
      try{
        Document docPc = Jsoup.connect(queryPc).get();
        link = docPc.select(".GTM-productClick.enlace-superpuesto").first();
      } catch (Exception e){
        throw new FailureException("Error en la conexión a PC Componentes o no existen coincidencias con las características indicadas.");
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

  private class GetProductInfo extends AchieveREResponder{

    public GetProductInfo(Agent ag, MessageTemplate mt){
      super(ag, mt);
    }

    protected ACLMessage handleRequest(ACLMessage msg) throws NotUnderstoodException, RefuseException{
      //Este metodo es necesario para poder lanzar FailureException en prepareResultNotification
      return null;
    }

    protected ACLMessage prepareResultNotification(ACLMessage msg, ACLMessage sent) throws FailureException{
      System.out.println("AgentePC: Obteniendo información del producto. Por favor, espere un momento.");
      String queryPc = "https://www.pccomponentes.com/buscar/?query=" + msg.getContent();

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
        throw new FailureException("AgentePC: Error accediendo al producto.");
      }

      // Precio
      Element precio = docElem.select("#precio-main").first();
      String coste = precio.text();
  		//Comprobamos que hemos recibido un precio correcto
  		Pattern p = Pattern.compile("[0-9]*(.,[0-9]*)? [€$]");
  		Matcher m = p.matcher(coste);
  		if (m.matches()) {
  			coste = coste.substring(0, coste.length() - 2);
  			coste = coste.replace(",", ".");
  		}else{
        throw new FailureException("AgentePC: Error obteniendo precio del producto.");
      }

      // Valoración
      Element ratingStars = docElem.select(".rating-stars").first();
  		String rating = ratingStars.attr("style");
  		//Comprobamos que coincide con el patron encontrado en la pagina web
  		p = Pattern.compile("width: [0-9]*.[0-9]*%;");
  		m = p.matcher(rating);
  		if (m.matches()) {
  			rating = rating.substring(7, rating.length() - 2);
        rating = rating.replace(",", ".");
  		}else{
        throw new FailureException("AgentePC: Error obteniendo valoración del producto.");
      }

      ACLMessage inform = msg.createReply();
      inform.setPerformative(ACLMessage.INFORM);
      inform.setContent("PCComponentes,"+coste+","+rating+","+link.absUrl("href"));
      return inform;
    }

  }

}
