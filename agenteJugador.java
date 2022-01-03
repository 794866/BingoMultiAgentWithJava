/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BINGO;

import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.gui.GuiEvent;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import java.awt.Color;
import javax.swing.JTextField;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Norman
 */
public class agenteJugador extends GuiAgent{
    Jugador ventana;
    String lastSend="";
    
    @Override
    protected void setup(){
        ventana = new Jugador(this, this.getLocalName());
        //ventana.setBounds(10,10,400,400);
        ventana.setVisible(true);
        generarCarton();

        ServiceDescription servicio = new ServiceDescription();
        servicio.setType("CHAT");
        servicio.setName("CHAT");
 
        DFAgentDescription descripcion = new DFAgentDescription();
        descripcion.setName(getAID());
        descripcion.addLanguages("castellano");
        descripcion.addServices(servicio);
        
        try {
            DFService.register(this, descripcion);
            System.out.println("Servicio de " +servicio.getName() + " Por el agente " + this.getLocalName());
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        MessageTemplate protocolo = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        MessageTemplate performativa = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        MessageTemplate plantilla = MessageTemplate.and(protocolo, performativa);
 
        addBehaviour(new ManejadorResponder(this, plantilla));
    }

    
    private void generarCarton(){
        ventana.txf1.setText(Integer.toString(ThreadLocalRandom.current().nextInt(1, 3 + 1)));
        ventana.txf2.setText(Integer.toString(ThreadLocalRandom.current().nextInt(4, 6 + 1)));
        ventana.txf3.setText(Integer.toString(ThreadLocalRandom.current().nextInt(7, 9 + 1)));
        ventana.txf4.setText(Integer.toString(ThreadLocalRandom.current().nextInt(10, 12 + 1)));
        ventana.txf5.setText(Integer.toString(ThreadLocalRandom.current().nextInt(13, 16 + 1)));
        ventana.txf6.setText(Integer.toString(ThreadLocalRandom.current().nextInt(17, 20 + 1)));
    }
    
    public void validarNumero2(String value){
        for(int x=0;x<ventana.panelJugador.getComponentCount();x++){
            if(ventana.panelJugador.getComponent(x) instanceof JTextField){
                JTextField textField = (JTextField) ventana.panelJugador.getComponent(x);
                //textField.setEditable(false);
                if(textField.getText().equals(value)){
                
                }
            }
        }
    }
    
    public boolean validarLineas(){
        boolean result = false;
            if("recived".equals(ventana.txf1.getAccessibleContext().getAccessibleDescription())
                &&"recived".equals(ventana.txf2.getAccessibleContext().getAccessibleDescription())
                &&"recived".equals(ventana.txf3.getAccessibleContext().getAccessibleDescription())){
                    result = true;

            }
            else if("recived".equals(ventana.txf4.getAccessibleContext().getAccessibleDescription())
                &&"recived".equals(ventana.txf5.getAccessibleContext().getAccessibleDescription())
                &&"recived".equals(ventana.txf6.getAccessibleContext().getAccessibleDescription())){
                    result = true;
        }
        return result;
    }
    public boolean validarBingo(){
        boolean result = false;
            if("recived".equals(ventana.txf1.getAccessibleContext().getAccessibleDescription())
                &&"recived".equals(ventana.txf2.getAccessibleContext().getAccessibleDescription())
                &&"recived".equals(ventana.txf3.getAccessibleContext().getAccessibleDescription())
                &&"recived".equals(ventana.txf4.getAccessibleContext().getAccessibleDescription())    
                &&"recived".equals(ventana.txf5.getAccessibleContext().getAccessibleDescription())
                &&"recived".equals(ventana.txf6.getAccessibleContext().getAccessibleDescription())){
                    result = true;
            }
        return result;
    }
    
    public void resaltarCampoActual(int n){
        for(int x=0;x<ventana.panelJugador.getComponentCount();x++){
            if(ventana.panelJugador.getComponent(x) instanceof JTextField){
                JTextField textField = (JTextField) ventana.panelJugador.getComponent(x);
                //textField.setEditable(false);
                if(textField.getText().equalsIgnoreCase(Integer.toString(n))){
                    textField.setBackground(Color.RED);
                    textField.getAccessibleContext().setAccessibleDescription("recived");
                    break;
                }
            }
        }
    }
    
    @Override
    protected void onGuiEvent(GuiEvent ge) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
    }
    
    boolean sended_Bingo=false, sended_Line=false;
    class ManejadorResponder extends AchieveREResponder
    {
        public ManejadorResponder(GuiAgent a,MessageTemplate mt) {
            super(a,mt);
        }
            protected ACLMessage handleRequest(ACLMessage request)throws NotUnderstoodException, RefuseException
            {
                System.out.println("Parametro recibido: " + request.getContent());
                ventana.txf_recived.setText(request.getContent());
                resaltarCampoActual(Integer.parseInt(request.getContent()));
                boolean validate_Line = validarLineas();
                boolean validate_Bingo = validarBingo();
                
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                
                if((validate_Bingo == true) && sended_Bingo==false){
                    System.out.println("BINGO");
                    sended_Bingo = true;
                    ventana.txf_send.setText("BINGO");
                    agree.setContent("BINGO");
                    return agree;
                }
                
                if((validate_Line == true) && (sended_Line==false)){
                    System.out.println("LINEA");
                    sended_Line=true;
                    ventana.txf_send.setText("LINEA");
                    agree.setContent("LINEA");
                    return agree;
                }
                
                else
                {
                    throw new RefuseException("Fuego demasiado lejos");
                }
            }
        }
 
        protected ACLMessage prepareResultNotification(ACLMessage request,ACLMessage response) throws FailureException
        {
            if (Math.random() > 0.2) {
                System.out.println("Central "+getLocalName()+": Hemos vuelto de apagar el fuego.");
                ACLMessage inform = request.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                return inform;
            }
            else
            {
                System.out.println("Central "+getLocalName()+": Nos hemos quedado sin agua");
                throw new FailureException("Nos hemos quedado sin agua");
            }
        }
    }

