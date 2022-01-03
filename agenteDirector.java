/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BINGO;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import BINGO.Director;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.proto.AchieveREInitiator;
import jade.proto.ContractNetInitiator;
import java.awt.Color;
import java.util.Date;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;
import java.util.stream.IntStream;
import javax.accessibility.AccessibleContext;
import javax.swing.JOptionPane;
import javax.swing.JTextField;


/**
 *
 * @author Norman
 */
public class agenteDirector extends GuiAgent{
    Director ventana;
    int iter;
    private static int  num_Agree, num_Request;
    
    @Override
    protected void setup(){
        
        ventana = new Director(this, this.getLocalName());
        ventana.setVisible(true);
    }
    
    @Override
    protected void takeDown(){
        System.exit(0);
    }

    
    public class CrearMensaje_Comportamiento extends CyclicBehaviour{
        @Override
        public void action(){
            MyCiclicBehaivour();
            this.myAgent.doWait(5000);
        }
    }
    
    
    public void resaltarCampoActual(int n){
        for(int x=0;x<ventana.panelDirector.getComponentCount();x++){
            if(ventana.panelDirector.getComponent(x) instanceof JTextField){
                JTextField textField = (JTextField) ventana.panelDirector.getComponent(x);
                textField.setEditable(false);
                if(textField.getText().equalsIgnoreCase(Integer.toString(n))){
                    textField.setBackground(Color.yellow);
                    textField.getAccessibleContext().setAccessibleDescription("sended");
                    iter++;
                    break;
                }
            }
        }
    }
    
    private int generarAleatorio(){
        int[] numerosAleatorios = IntStream.rangeClosed(1, 20).toArray();
        //desordenando los elementos
        Random r = new Random();
        int ret=0;
        for (int i = numerosAleatorios.length; i > 0; i--) {
            int posicion = r.nextInt(i);
            int tmp = numerosAleatorios[i-1];
            numerosAleatorios[i - 1] = numerosAleatorios[posicion];
            numerosAleatorios[posicion] = tmp;
            ret = tmp;
        }
        return ret;
    }
    
    public boolean validarAleatorio(int n){
        boolean result = true;
        for(int x=0;x<ventana.panelDirector.getComponentCount();x++){
            if(ventana.panelDirector.getComponent(x) instanceof JTextField){
                JTextField textField = (JTextField) ventana.panelDirector.getComponent(x);
                if(textField.getText().equals(Integer.toString(n))){
                    if(textField.getAccessibleContext().getAccessibleDescription() == "sended"){
                        //System.out.println("This value has been sended");
                        result = false;
                        break;
                    }
                }
                
            }
        }
        return result;
    }
    
    public void MyCiclicBehaivour(){
        ACLMessage mensajeCFP = new ACLMessage(ACLMessage.REQUEST);
        ServiceDescription servicio = new ServiceDescription();
        servicio.setType("CHAT");
        servicio.setName("CHAT");

        DFAgentDescription descripcion = new DFAgentDescription();
        descripcion.addLanguages("castellano");
        descripcion.addServices(servicio);

        if(iter == 20){
            JOptionPane.showMessageDialog(ventana, "Reinicie el juego");
        }else{
                //ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
            int numero = generarAleatorio();
            //ventana.jTextArea1.insert("Numero --> " + numero + "\n", 1);
            boolean s1 = false;
            while(s1 == false){ //don't resend all sended values
                boolean s2 = validarAleatorio(numero); //if actual number has ben sended recall other number
                if(s2 == true){
                    s1 = true;
                }else{
                    numero = generarAleatorio();
                }
            }
                    
            //numero = (int) (Math.random() * 20) + 1; //new value
            try{
                DFAgentDescription[] resultados = DFService.search(this, descripcion);
                System.out.println("Empezamos el juego con " + resultados.length + " Jugadores...");
                if (resultados.length <= 0) {
                    System.out.println("Aun no hay jugadores.");
                }
                else{
                    System.out.println("Hay " + resultados.length + " jugadores listos");
                }
                for (DFAgentDescription agente:resultados){
                    mensajeCFP.addReceiver(agente.getName());
                    num_Request++;
                }

                mensajeCFP.setContent(Integer.toString(numero));
                mensajeCFP.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

                addBehaviour(new ManejadorInitiator(this,mensajeCFP));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            resaltarCampoActual(numero);
            ventana.jTextArea1.insert("Numero --> " + numero + "\n", 1);
            mensajeCFP.setReplyByDate(new Date(System.currentTimeMillis() + 3000));
        }
    }
    
    @Override
    protected void onGuiEvent(GuiEvent ge) {
        if (ge.getType() == 0){
            System.out.println("INCIANDO");
            addBehaviour(new CrearMensaje_Comportamiento());
        }
        if (ge.getType() == 1){
            iter=0;
            System.out.println("RESETEANDO");
            addBehaviour(new CrearMensaje_Comportamiento());
        }
        if (ge.getType() == 2){
            System.out.println("MATANGO AGENTES");
            
            ServiceDescription servicio = new ServiceDescription();
            servicio.setType("CHAT");
            servicio.setName("CHAT");

            DFAgentDescription descripcion = new DFAgentDescription();
            descripcion.addLanguages("castellano");
            descripcion.addServices(servicio);
            
            try{
                DFAgentDescription[] resultados = DFService.search(this, descripcion);
                for (DFAgentDescription agente:resultados){
                    takeDown();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }    
        }
    }
    
    class ManejadorInitiator extends AchieveREInitiator
    {
        public ManejadorInitiator(Agent a, ACLMessage msg) {
            super(a,msg);
        }
 
        protected void handleAgree(ACLMessage agree)
        {
            String Cadena = "El agente " + agree.getSender().getLocalName() + " informa que tiene " + agree.getContent() + "\n";
            System.out.println(Cadena);
            ventana.jTextArea2.insert(Cadena, 1);
            num_Agree++;
        }
        
        //======================
        protected void handleRefuse(ACLMessage refuse)
        {
            System.out.println("Central de bomberos " + refuse.getSender().getName()
                    + " responde que el fuego est� muy lejos y no puede apagarlo.");
        }
 
        protected void handleNotUnderstood(ACLMessage notUnderstood)
        {
            System.out.println("Central de bomberos " + notUnderstood.getSender().getName()
                    + " es idiota y no entiende el mensaje.");
        }
 
        protected void handleInform(ACLMessage inform)
        {
            System.out.println("Central de bomberos " + inform.getSender().getName()
                    + " informa que ha apagado el fuego.");
        }
 
        protected void handleFailure(ACLMessage fallo)
        {
            if (fallo.getSender().equals(myAgent.getAMS())) {
                System.out.println("Alguna de las centrales de bomberos no existe");
            }
            else
            {
                System.out.println("Fallo en central de bomberos " + fallo.getSender().getName()
                        + ": " + fallo.getContent().substring(1, fallo.getContent().length()-1));
            }
        }
    }
}
