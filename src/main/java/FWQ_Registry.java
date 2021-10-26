import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.*;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FWQ_Registry {
    public String leeSocket(Socket p_sk, String p_datos) {
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

    public void escribeSocket(Socket p_sk, String p_datos) {
        try {
            OutputStream out = p_sk.getOutputStream();
            DataOutputStream flujo = new DataOutputStream(out);
            flujo.writeUTF(p_datos);
        } catch(Exception io) {
            io.getMessage();
        }
    }

    public  void insertarUsuario(String alias, String nombre, String contra){

        MongoClient client = MongoClients.create("mongodb+srv://sergiopaco:Sistemas12345@cluster0.wyb5t.mongodb.net/Parque?retryWrites=true&w=majority");

        MongoDatabase db = client.getDatabase("Parque");

        MongoCollection col = db.getCollection("Usuario");

        Document docu = new Document("Alias", alias).append("Nombre", nombre).append("Contrasenya", contra);

        col.insertOne(docu);
    }

    public int formulario(String p_Cadena) {

        String[] informacion = p_Cadena.split(", ");
        String ID = "";
        String nombre = "";
        String password = "";

        if(informacion.length < 3) {
            System.out.println("Falta información por dar.");
            return -1;
        } else if(informacion.length > 4) {
            System.out.println("Sobra información.");
            return -1;
        } else {
            ID = informacion[0];
            nombre = informacion[1];
            password = informacion[2];

            insertarUsuario(ID, nombre, password);

            System.out.println(ID);
            System.out.println(nombre);
            System.out.println(password);

            return 1;
        }

         /*
        System.out.println(p_Cadena);

        String quitar = p_Cadena.substring(1, p_Cadena.length()-1);
        System.out.println(quitar);

        String[] informacion = quitar.split(", ");
        System.out.println(informacion[0]);


         */

    }

    public static void main(String[] args) {

        //SpringApplication.run(FWQ_Registry.class, args);

        int jj = -1;

        while (jj != 0) {  //para que este siemre escuchando

            int verificacion = 0;
            String puerto = "";
            String Cadena = "";

            try {
                FWQ_Registry fwq = new FWQ_Registry();
                if (args.length < 1) {
                    System.out.println("Debe indicar el puerto de escucha del servidor.");
                    System.out.println("$./Servidor puerto_servidor");
                    System.exit(1);
                }
                puerto = args[0];
                ServerSocket skServidor = new ServerSocket(Integer.parseInt(puerto));
                System.out.println("Escucho el puerto " + puerto);


                for (; ; ) {
                    Socket skCliente = skServidor.accept();
                    System.out.println("Sirviendo cliente...");
                    //while (verificacion != -1) {  //hay que quitarlo, hace que este escuchando siempre
                        Cadena = fwq.leeSocket(skCliente, Cadena);

                        verificacion = fwq.formulario(Cadena);
                        fwq.escribeSocket(skCliente, "Registro completado con éxito.");
                    //}

                    skCliente.close();
                    //System.exit(0);
                }

            } catch (Exception io) {
                io.getMessage();
            }
        }
    }
}
