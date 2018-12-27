package dominio;

import java.util.Scanner;

import jade.core.Agent;
import jade.core.behaviours.*;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

public class Intermediario extends Agent{

  private static Scanner reader = new Scanner (System.in);
  private SequentialBehaviour seq1;
  private AID agentePC = new AID("agentePC", AID.ISLOCALNAME);
  private AID agenteAmazon = new AID("agenteAmazon", AID.ISLOCALNAME);

  private ACLMessage inicial = new ACLMessage(ACLMessage.QUERY_IF);

  protected void setup(){
    seq1 = new SequentialBehaviour();
    seq1.addSubBehaviour(new GetProductInfo());
    seq1.addSubBehaviour(new SendProductInfo(this, inicial));
    addBehaviour(seq1);
  }

  private class GetProductInfo extends OneShotBehaviour{

    public void action(){
      int ram = -1;
      while (ram==-1){
        System.out.println("¿Cuanta RAM deseas que tenga el movil?");
        try {
          ram = Integer.parseInt(reader.nextLine());
        }catch (Exception e){
          System.out.println("Respuesta no válida. Por favor, intentelo de nuevo.");
        }
      }
      System.out.println();
      inicial.setContent(ram+"-gb-ram");
      inicial.addReceiver(agentePC);
    }

  }

  private class SendProductInfo extends AchieveREInitiator{

    public SendProductInfo(Agent ag, ACLMessage msg){
      super(ag, msg);
    }

    protected void handleInform(ACLMessage inform){
      System.out.println("Intermediario: Recibido "+inform.getContent()+". Enviando a los agentes.");
    }

    protected void handleFailure(ACLMessage fallo){
      System.err.println(fallo.getContent());
    }

  }

}
