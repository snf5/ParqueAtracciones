import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.MongoClient;
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

        //comprobar si esta en la base de datos para ver si hay que modificar o insertar
        //comprobar(p_datos);

        return p_datos;
    }

    //todo
    //todo
    //todo
    public boolean comprobar(String credencials) {

        System.out.println("recibo: " + credencials);

        String[] credenciales = credencials.split(", ");
        String alias = "";
        String nombre = "";
        String contra = "";
        HashMap<String, String> credence = new HashMap<>();
        boolean solution = false;

        MongoClient cliente = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = cliente.getDatabase("parque");
        MongoCollection<Document> docu = db.getCollection("usuario");
        FindIterable<Document> documents = docu.find();
        for(Document document : documents) {
            alias = document.get("alias").toString();
            nombre = document.get("nombre").toString();

            credence.put(alias,"");
        }

        Document busco = new Document("alias", credenciales[0]);

        Document nuevo = new Document(), nuevo2 = new Document();

        if(credenciales.length == 2) {
            //cambiamos nombre
             nuevo = new Document("$set", new Document("nombre", credenciales[1]));

        }else{
            //cambiamos contra
            if(credenciales[1].equals("")) {
                nuevo = new Document("$set", new Document("contrasenya", credenciales[2]));
            }else{
                nuevo = new Document("$set", new Document("contrasenya", credenciales[2]));
                nuevo2 = new Document("$set", new Document("nombre", credenciales[1]));
            }
            //cambiamos ambas
        }

        //docu.findOneAndUpdate();

        for(String datos : credence.keySet()) {
            if(credenciales[0].equals(datos)) {
                solution = true;
                System.out.println("lo encuentro");
                docu.findOneAndUpdate(busco, nuevo);
                if(nuevo2.isEmpty() == false){
                    docu.findOneAndUpdate(busco, nuevo2);
                }

            }
        }
        return solution;
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

        MongoClient cliente = MongoClients.create("mongodb+srv://sergiopaco:Sistemas12345@cluster0.wyb5t.mongodb.net/Parque?retryWrites=true&w=majority");
        MongoClient client = MongoClients.create("mongodb://localhost:27017");

        MongoDatabase db = client.getDatabase("parque");

        MongoCollection col = db.getCollection("usuario");

        //DBCollection prueba = (DBCollection) db.getCollection("usuario");

        Document docu = new Document("alias", alias).append("nombre", nombre).append("contrasenya", contra);

        col.insertOne(docu);

        //prueba.insert((DBObject) docu);
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
        boolean modifica = false;

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

                        modifica = fwq.comprobar(Cadena);

                        if(modifica == true){
                            //llamar al metodo de modificar
                        }else {
                            verificacion = fwq.formulario(Cadena);
                            fwq.escribeSocket(skCliente, "Registro completado con éxito.");
                        }
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
