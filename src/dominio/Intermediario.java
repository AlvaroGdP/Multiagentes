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
  private ParallelBehaviour par1;
  private AskProductInfo api1;
  private SendProductInfo spi1;
  private SendName sn1;
  private SendName sn2;
  private AID agentePC = new AID("agentePC", AID.ISLOCALNAME);
  private AID agenteAmazon = new AID("agenteAmazon", AID.ISLOCALNAME);

  private ACLMessage inicial = new ACLMessage(ACLMessage.QUERY_IF);
  private ACLMessage msgNombrePC = new ACLMessage(ACLMessage.QUERY_IF);
  private ACLMessage msgNombreAmazon = new ACLMessage(ACLMessage.QUERY_IF);
  //Sin utilizar
  private String nombre;

  protected void setup(){
    seq1 = new SequentialBehaviour();
    par1 = new ParallelBehaviour();
    api1 = new AskProductInfo();
    spi1 = new SendProductInfo(this,inicial);
    sn1 = new SendName(this,msgNombrePC);
    sn2 = new SendName(this,msgNombreAmazon);
    seq1.addSubBehaviour(api1);
    seq1.addSubBehaviour(spi1);
    par1.addSubBehaviour(sn1);
    par1.addSubBehaviour(sn2);
    seq1.addSubBehaviour(par1);
    addBehaviour(seq1);
  }

  private class AskProductInfo extends OneShotBehaviour{

    public void action(){
      String mensaje = "";
      int ram = -1;
      while (ram==-1){
        System.out.println("\n¿Cuanta RAM deseas que tenga el movil? (0 si no te importa esta característica)");
        try {
          ram = Integer.parseInt(reader.nextLine());
        }catch (Exception e){
          System.out.println("Respuesta no válida. Por favor, intentelo de nuevo.");
        }
      }
      System.out.println();
      if (ram != 0){
        mensaje+="/"+ram+"-gb-ram";
      }

      int almacenamiento = -1;
      while(almacenamiento==-1){
        System.out.println("¿Cuánto almacenamiento quieres en el móvil? (0 si no te importa esta característica)");
        try{
          almacenamiento = Integer.parseInt(reader.nextLine());
        }catch(Exception e){
          System.out.println("Respuesta no válida. Por favor, intentelo de nuevo.");
        }
      }
      if (almacenamiento != 0){
        mensaje+="/"+almacenamiento+"-gb";
      }
      int megapixeles = -1;
      while(megapixeles==-1){
        System.out.println("¿Cuántos megapixeles quieres en la cámara del móvil? (0 si no te importa esta característica)");
        try{
          megapixeles = Integer.parseInt(reader.nextLine());
        }catch(Exception e){
          System.out.println("Respuesta no válida. Por favor, intentelo de nuevo.");
        }
      }
      if (megapixeles != 0){
          mensaje+="/de-"+megapixeles+"-mp";
      }
      inicial.setContent(mensaje);
      inicial.addReceiver(agentePC);
      inicial.setOntology("Nombre");
    }

  }

  private class SendProductInfo extends AchieveREInitiator{

    public SendProductInfo(Agent ag, ACLMessage msg){
      super(ag, msg);
    }

    protected void handleInform(ACLMessage inform){
      System.out.println("Intermediario: Recibido "+inform.getContent()+". Obteniendo datos del producto de los agentes.");
      nombre = inform.getContent();
      msgNombrePC.setContent(nombre);
      msgNombrePC.addReceiver(agentePC);
      msgNombrePC.setOntology("Info");
      msgNombreAmazon.setContent(nombre);
      msgNombreAmazon.addReceiver(agenteAmazon);
      msgNombreAmazon.setOntology("Info");
    }

    protected void handleFailure(ACLMessage fallo){
      System.err.println(fallo.getContent());
    }

  }

  private class SendName extends AchieveREInitiator{

    public SendName(Agent ag, ACLMessage msg){
      super(ag, msg);
    }

    protected void handleInform(ACLMessage inform){
      String[] splitted = inform.getContent().split(",");
      System.out.println("\nIntermediario: Recibida información del producto "+nombre+".");
      System.out.println("\tWeb: "+splitted[0]);
      System.out.println("\tPrecio: "+splitted[1]+ " €");
      System.out.println("\tValoración: "+splitted[2]+"/ 100");
      System.out.println("\tEnlace: "+splitted[3]+"\n");
    }

    protected void handleFailure(ACLMessage fallo){
      System.err.println(fallo.getContent());
    }

  }

}
