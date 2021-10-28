
//recibe a traves de kafka el numero de visitantes de cada sensor, se trata de un consumidor de recursos

//esta a la escucha de que engine le solicite información (es un servidor)

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.bson.BSONCallback;
import org.bson.Document;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.*;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.conversions.Bson;

import javax.print.Doc;


public class FWQ_WaitingTimeServer {

    public String funciona = "";

    public static Map<String, String> map = new HashMap<>();

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
        String hostKafka = "";
        String puertoKafka = "";

        int verificacion = 0;

        try{
            FWQ_WaitingTimeServer fwq = new FWQ_WaitingTimeServer();

            if(args.length < 3){
                System.out.println("Debe indicar el puerto de escucha del servidor.");
                System.out.println("$./Servidor puerto_servidor");
                System.exit(1);
            }else{

                puerto = args[0];
                hostKafka = args[1];
                puertoKafka = args[2];

                consumidor(hostKafka, puertoKafka, fwq);

                ServerSocket skServidor = new ServerSocket(Integer.parseInt(puerto));
                System.out.println("Escucho el puerto " + puerto);

                for(;;) {
                    Socket skCliente = skServidor.accept();
                    System.out.println("Sirviendo cliente...");
                    while(verificacion != -1) {
                        Cadena = fwq.leeSocket(skCliente, Cadena);

                        verificacion = fwq.tiemposEspera(Cadena);
                        fwq.escribeSocket(skCliente, map.toString());
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

    //consumidor que va recibiendo de los sensore slos datos de personas de cada atraccion
    public static String consumidor(String hostKafka, String puertoKafka, FWQ_WaitingTimeServer fwq){

        Properties proper = new Properties();

        String datos = "";

        String id = "", nuevoTiempo = "";
        String[] informacion ;

        datos += hostKafka;
        datos += ":";
        datos += puertoKafka;

        System.out.println(datos);


        proper.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, datos);
        proper.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        proper.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        proper.put(ConsumerConfig.GROUP_ID_CONFIG, "sensorGroup");
        proper.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(proper);
        consumer.subscribe(Collections.singleton("prueba"));

        try{
            while(true) {

                ConsumerRecords<String, String> records = consumer.poll(100);

                int tiempoCola = 0;
                String tiempo = "";

                for (ConsumerRecord<String, String> record : records) {
                    //System.out.println(record.value().toString());

                    //CON ESTO SAMOS EL TIEMPO DE ESPERA DE LA ATRACCIÓN
                    informacion = record.value().toString().split(":");

                    int personas = 0;
                    nuevoTiempo = "";

                    id = informacion[0];
                    personas = Integer.parseInt(informacion[1]);

                    tiempoCola = (int)(calcularCola(id, personas));
                    tiempo += tiempoCola;

                    nuevoTiempo += id;
                    nuevoTiempo += ":";
                    nuevoTiempo += tiempoCola;
                    nuevoTiempo += " ";

                    System.out.println(nuevoTiempo);

                }

                //REPASAR EL MAP PARA PASARSELO A ENGINE
                if(nuevoTiempo != "") {

                    if (map.isEmpty()) {
                        map.put(id, tiempo);
                    } else {
                        int num = 0;
                        for (Map.Entry entry : map.entrySet()) {
                            if (entry.getKey() == id) {
                                //map.replace(id, tiempo);
                                num = 1;
                            } else {
                                //map.put(id, tiempo);
                                num =  2;
                            }
                        }

                        if(num == 1){
                            map.replace(id, tiempo);
                        }else if(num == 2){
                            map.put(id, tiempo);
                        }
                    }

                    System.out.println(map);
                    nuevoTiempo = "";
                }

                /*
                System.out.println("holaaa");
                map.put(id, nuevoTiempo);
                System.out.println(map);
                 */
            }

        }finally {
            consumer.close();
        }


    }


    //calucla el tiempo de espera de cada atracción segun el numero de personas
    //recibidas a traves de los sensores
    public static double calcularCola(String id, int personas){

        double  encola = 0, calculo = 0, tiempo = 0,  mins = 0;
        String prueba = id;
        String minutos = "", visi = "";

        System.out.println("Personas: " + personas);

        MongoClient cliente = MongoClients.create("mongodb+srv://sergiopaco:Sistemas12345@cluster0.wyb5t.mongodb.net/Parque?retryWrites=true&w=majority");
        MongoClient client = MongoClients.create("mongodb://localhost:27017");

        MongoDatabase db = client.getDatabase("parque");

        MongoCollection<Document> col = db.getCollection("sensor");

        FindIterable<Document> docum = col.find(eq("id", id));

        //parte de engine
       // MongoCollection<Document> cola = db.getCollection("Atraccion");

        //FindIterable<Document> documm = cola.find();

        /*
        for(Document documennt : documm){
            System.out.println(documennt.get("ubicacion").toString());
        }
         */


        for (Document document : docum) {
            visi = document.get("visitantes").toString();
            minutos = document.get("ciclo").toString();
        }

        encola = Integer.parseInt(visi);
        mins = Integer.parseInt(minutos);

        calculo = personas/encola;

        tiempo = Math.ceil(calculo*mins);


        return tiempo;
    }


}
