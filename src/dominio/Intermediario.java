package dominio;

import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

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
  private ShowResults sr;
  private AID agentePC = new AID("agentePC", AID.ISLOCALNAME);
  private AID agenteAmazon = new AID("agenteAmazon", AID.ISLOCALNAME);

  private ACLMessage inicial = new ACLMessage(ACLMessage.QUERY_IF);
  private ACLMessage msgNombrePC = new ACLMessage(ACLMessage.QUERY_IF);
  private ACLMessage msgNombreAmazon = new ACLMessage(ACLMessage.QUERY_IF);
  private String nombre;

  private String infoPC = null;
  private String infoAmazon = null;

  protected void setup(){

    seq1 = new SequentialBehaviour();
    par1 = new ParallelBehaviour();
    api1 = new AskProductInfo();
    spi1 = new SendProductInfo(this,inicial);
    sn1 = new SendName(this, msgNombrePC);
    sn2 = new SendName(this, msgNombreAmazon);
    sr = new ShowResults();

    seq1.addSubBehaviour(api1);
    seq1.addSubBehaviour(spi1);
    par1.addSubBehaviour(sn1);
    par1.addSubBehaviour(sn2);
    seq1.addSubBehaviour(par1);
    seq1.addSubBehaviour(sr);
    addBehaviour(seq1);
  }

  private class AskProductInfo extends OneShotBehaviour{

    public void action(){
      String mensaje = "";
      int ram = -1;
      HashSet<Integer> ram_set = new HashSet<Integer>(Arrays.asList(1, 2, 4, 6, 8, 0));
      while (!ram_set.contains(ram)){
        System.out.println("\n¿Cuanta RAM deseas que tenga el movil?");
        System.out.println("Opciones: 1, 2, 4, 6, 8 (GB), 0 si no le importa esta característica");
        try {
          ram = Integer.parseInt(reader.nextLine());
        }catch (Exception e){
          System.out.println("Respuesta no válida. Por favor, intentelo de nuevo.");
        }
      }
      if (ram != 0){
        mensaje+="/"+ram+"-gb-ram";
      }

      int almacenamiento = -1;
      HashSet<Integer> alm_set = new HashSet<Integer>(Arrays.asList(1, 3, 4, 8, 16, 32, 64, 128, 256, 512, 0));
      while(!alm_set.contains(almacenamiento)){
        System.out.println("\n¿Cuánto almacenamiento quieres en el móvil?");
        System.out.println("Opciones: 1, 3, 4, 8, 16, 32, 64, 128, 256, 512 (GB), 0 si no le importa esta característica");
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
      HashSet<Integer> mp_set = new HashSet<Integer>(Arrays.asList(2, 5,8, 10, 16, 32, 64, 128, 256, 512, 0));
      while(!mp_set.contains(megapixeles)){
        System.out.println("\n¿Cuántos megapixeles quieres en la cámara del móvil?");
        System.out.println("Opciones: 2, 5, 8, 10, 12, 13, 16, 19, 20, 21, 23, 24, 40 (MP), 0 si no le importa esta característica");
        try{
          megapixeles = Integer.parseInt(reader.nextLine());
        }catch(Exception e){
          System.out.println("Respuesta no válida. Por favor, intentelo de nuevo.");
        }
      }
      if (megapixeles != 0){
          mensaje+="/de-"+megapixeles+"-mp";
      }
      System.out.println();
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
      if (inform.getSender().equals(agentePC)){
        infoPC = inform.getContent();
      }else{
        infoAmazon = inform.getContent();
      }

    }

    protected void handleFailure(ACLMessage fallo){
      System.err.println(fallo.getContent());
    }

  }

  private class ShowResults extends OneShotBehaviour{

    public void action(){

      String[] splittedPC = null;
      String[] splittedAmazon = null;

      if (infoPC!=null) {
        splittedPC = infoPC.split(",");
      }

      if (infoAmazon!=null){
        splittedAmazon = infoAmazon.split(",");
      }

      System.out.println("\nIntermediario: Mostrando información recibida del producto "+nombre+".");
      if (infoPC!=null){
        System.out.println("\tWeb: "+splittedPC[0]);
        System.out.println("\tPrecio: "+splittedPC[1]+ " €");
        System.out.println("\tValoración: "+splittedPC[2]+"/ 100");
        System.out.println("\tEnlace: "+splittedPC[3]+"\n");
      }
      if (infoAmazon!=null){
        System.out.println("\tWeb: "+splittedAmazon[0]);
        System.out.println("\tPrecio: "+splittedAmazon[1]+ " €");
        System.out.println("\tValoración: "+splittedAmazon[2]+"/ 100");
        System.out.println("\tEnlace: "+splittedAmazon[3]+"\n");
      }

      if (infoPC!=null && infoAmazon!=null){
        //Comparamos ambos productos
        if (Double.parseDouble(splittedPC[1]) > Double.parseDouble(splittedAmazon[1])){
          System.out.println("\nDado que el precio en Amazon es menor, sugerimos comprarlo en dicha web.");
        }else{
          System.out.println("\nDado que el precio en PCComponentes es menor, sugerimos comprarlo en dicha web.");
        }
        System.out.println("Esto le ahorrará un total de "+Math.abs(Double.parseDouble(splittedPC[1]) - Double.parseDouble(splittedAmazon[1]))+"€");
        if (Math.abs(Double.parseDouble(splittedPC[2]) - Double.parseDouble(splittedAmazon[2])) > 30.0){
          System.out.println("Las valoraciones del producto en ambas webs difieren en gran medida.\n\tQuizá desee visitarlas manualmente para conocer el motivo.");
        }

      }else{
        //Solo hay informacion de dicho producto en una web
        if (infoPC!=null){
          System.out.println("Dado que el producto obtenido en la búsqueda solo se encuentra en la web PCComponentes, sugerimos comprarlo en dicha tienda.");
        }
      }

    }
  }

}
