
//recibe a traves de kafka el numero de visitantes de cada sensor, se trata de un consumidor de recursos

//esta a la escucha de que engine le solicite información (es un servidor)

import com.mongodb.client.*;
import org.bson.Document;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FWQ_WaitingTimeServer {

    public String leeSocket (Socket p_sk, String p_datos){
        try{
            InputStream aux = p_sk.getInputStream();
            DataInputStream flujo = new DataInputStream(aux);
            p_datos = new String();
            p_datos = flujo.readUTF();
        } catch(Exception io) {
            io.getMessage();
        }
        return p_datos;

    }

    public void escribeSocket(Socket p_sk, String p_datos){

        try {
            OutputStream out = p_sk.getOutputStream();
            DataOutputStream flujo = new DataOutputStream(out);
            flujo.writeUTF(p_datos);
        } catch(Exception io) {
            io.getMessage();
        }

    }

    public int tiemposEspera(String cadena){

        MongoClient client = MongoClients.create("mongodb+srv://sergiopaco:Sistemas12345@cluster0.wyb5t.mongodb.net/Parque?retryWrites=true&w=majority");

        MongoDatabase db = client.getDatabase("Parque");

        MongoCollection col = db.getCollection("Atraccion");

        FindIterable<Document> resultado = col.find();

        System.out.println(resultado.first().toString());


        return 0;

    }

    public static void main(String[] args) {

        String puerto = "";
        String Cadena = "";
        String ip = "";
        String broker = "";

        int verificacion = 0;

        try{

            FWQ_WaitingTimeServer fwq = new FWQ_WaitingTimeServer();

            if(args.length < 3){
                System.out.println("Debe indicar el puerto de escucha del servidor.");
                System.out.println("$./Servidor puerto_servidor");
                System.exit(1);
            }else{

                puerto = args[0];
                ip = args[1];
                broker = args[2];

                ServerSocket skServidor = new ServerSocket(Integer.parseInt(puerto));
                System.out.println("Escucho el puerto " + puerto);

                for(;;) {
                    Socket skCliente = skServidor.accept();
                    System.out.println("Sirviendo cliente...");
                    while(verificacion != -1) {
                        Cadena = fwq.leeSocket(skCliente, Cadena);

                        verificacion = fwq.tiemposEspera(Cadena);
                        fwq.escribeSocket(skCliente, "Registro completado con éxito.");
                    }

                    //mirarlo bien

                    int j = 0;
                    if(j == -1){
                        skCliente.close();
                        System.exit(0);
                    }

                }



            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
