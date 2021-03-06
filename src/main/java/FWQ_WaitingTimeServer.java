
//recibe a traves de kafka el numero de visitantes de cada sensor, se trata de un consumidor de recursos

//esta a la escucha de que engine le solicite información (es un servidor)

import com.mongodb.ServerAddress;
import com.mongodb.MongoCredential;
import com.mongodb.MongoClientOptions;

import com.mongodb.*;
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
import java.util.spi.CalendarDataProvider;

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

    //para saber si han caido sensores o no
    public static Map<String, String> copiaMap = new HashMap<>();

    public static Map<String, Integer> contadorCaida = new HashMap<String, Integer>();

    public static int pareceCaido = 0;



    public static String leeSocket (Socket p_sk, String p_datos){
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

    public static void escribeSocket(Socket p_sk, String p_datos){

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

        /*
        map.put("furius", "40");
        map.put("shambala", "30");

        copiaMap.put("furius", "40");
        copiaMap.put("shambala", "30");


        for(int i = 0;i < 20;i++){
            int num = 40 + i + 1;
            String cambio = String.valueOf(num);
            map.replace("furius",cambio);
            if(i > 12){
                map.replace("shambala",cambio);
            }
            System.out.println("sumooooo " + map.toString());
            System.out.println("sumooooo " + copiaMap.toString());

            caidaSensor();
            System.out.println("map: " + map.toString());
            System.out.println("copiaMap: " + copiaMap.toString());
            for(Map.Entry<String, String> entry: map.entrySet()){
                copiaMap.put(entry.getKey(), entry.getValue());
            }
        }
         */


        //try{
            FWQ_WaitingTimeServer fwq = new FWQ_WaitingTimeServer();

            if(args.length < 3){
                System.out.println("Debe indicar el puerto de escucha del servidor.");
                System.out.println("$./Servidor puerto_servidor");
                System.exit(1);
            }else {

                puerto = args[0];
                hostKafka = args[1];
                puertoKafka = args[2];

                //socketDatos(hostKafka, puertoKafka, fwq,Integer.parseInt(puerto));
                //System.out.println("seguimos a lo nuestro");

                //consumidor(hostKafka, puertoKafka, fwq, Integer.parseInt(puerto));

                while (true) {
                    //añadido esto juevea


                    consumidor(hostKafka, puertoKafka, fwq, Integer.parseInt(puerto));
                    while(map.isEmpty()) {
                        consumidor(hostKafka, puertoKafka, fwq, Integer.parseInt(puerto));
                    }
                    if (!map.isEmpty()) {
                        socketDatos(hostKafka, puertoKafka, fwq, Integer.parseInt(puerto));
                    }
                    consumidor(hostKafka, puertoKafka, fwq, Integer.parseInt(puerto));


                }

                //todo hacer funcion de socket para ir pasandole la informacióny añadir en este metodo un while(true)
            }

    }

    public static void socketDatos(String hostKafka, String puertoKafka, FWQ_WaitingTimeServer fwq,int puerto){

        String Cadena = "";

            try {
                ServerSocket skServidor = new ServerSocket(puerto);

                System.out.println("Escucho el puerto " + puerto);

                for (;;) {
                    Socket skCliente = skServidor.accept();
                    System.out.println("Sirviendo cliente...");

                    //modificado esto jueves

                    Thread t = new HiloServidor(skCliente, hostKafka, puertoKafka, fwq, puerto);
                    t.start();


                }
            } catch (IOException e) {
                //System.out.println("nova nova nova....");
                e.printStackTrace();
            }


    }

    //consumidor que va recibiendo de los sensore slos datos de personas de cada atraccion
    public static String consumidor(String hostKafka, String puertoKafka, FWQ_WaitingTimeServer fwq, int puerto){

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
        consumer.subscribe(Collections.singleton("sensores"));

        String datoss = "";


        try{
            //while(true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                //todo
                int fre = 0;

                if(!map.isEmpty() && fre == 0){
                    //fwq.socketDatos(puerto, fwq);
                    fre = 1;
                }
                int tiempoCola = 0;
                String tiempo = "";

                for (ConsumerRecord<String, String> record : records) {
                    tiempo = "";
                    id = "";

                    //CON ESTO SAMOS EL TIEMPO DE ESPERA DE LA ATRACCIÓN
                    String inf = record.value().toString();
                    System.out.println("Leo: " + inf);
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
                    comprobar(id, tiempo);
                    System.out.println(map);
                    nuevoTiempo = "";
                }
        }finally {
            consumer.close();
            //System.out.println("se petaaaaaaaa");
        }


        System.out.println("map antes: " + map.toString());
        System.out.println("copiaMap antes: " + copiaMap.toString());
        caidaSensor();
        System.out.println("map: " + map.toString());
        System.out.println("copiaMap: " + copiaMap.toString());
        for(Map.Entry<String, String> entry: map.entrySet()){
            copiaMap.put(entry.getKey(), entry.getValue());
        }



        return "";
    }

    public static void caidaSensor(){

        String valor1 = "", valor2 = "", caido = "";
        char primero;

        for(String clave:map.keySet()){
            for(String clave2:copiaMap.keySet()){
                if(clave.equals(clave2)){
                    valor1 = map.get(clave);
                    valor2 = copiaMap.get(clave2);

                    System.out.println("valor1: " + valor1 + " valor2: " + valor2);

                    System.out.println("contador: " + contadorCaida.get(clave2));
                    Integer cont = contadorCaida.get(clave2);
                    System.out.println("ooo: " + cont);

                    if(valor1.equals(valor2)){
                        contadorCaida.remove(clave2);
                        cont++;
                        contadorCaida.put(clave2, cont);
                    }else{
                        contadorCaida.remove(clave2);
                        contadorCaida.put(clave2, 0);
                    }

                    if(cont >= 15 && valor1.equals(valor2)){
                        primero = clave2.charAt(0);
                        caido = primero + "?";
                        map.replace(clave, caido);
                    }
                }
            }
        }
    }

    public static void comprobar(String id, String tiempo){

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
                    map.remove(id);
                    map.put(id, tiempo);
                    copiaMap.remove(id);
                    copiaMap.put(id, tiempo);
                }else if(num == 2){
                    map.put(id, tiempo);
                    copiaMap.put(id, tiempo);
                    contadorCaida.put(id, 0);
                }
            }
            System.out.println(map);
    }


    //calucla el tiempo de espera de cada atracción segun el numero de personas
    //recibidas a traves de los sensores
    public static double calcularCola(String id, int personas){

        double  encola = 0, calculo = 0, tiempo = 0,  mins = 0;
        String prueba = id;
        String minutos = "", visi = "";

        //MongoClient cliente = MongoClients.create("mongodb+srv://sergiopaco:Sistemas12345@cluster0.wyb5t.mongodb.net/Parque?retryWrites=true&w=majority");
        MongoClient client = MongoClients.create("mongodb://localhost:27017");

        //com.mongodb.MongoClient client = new com.mongodb.MongoClient("localhost", 27017);
        //Mongoclient clientee = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));


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

        int numero1 = (int)(Math.random()*10+1);
        tiempo += numero1;

        return tiempo;
    }

    public String pasarMapa(){
        return map.toString();
    }

}

class HiloServidor extends Thread{

    private Socket skCliente;
    private String host, puerto;
    private int pPuerto;
    private  FWQ_WaitingTimeServer fw;


    public HiloServidor(Socket p_cliente, String hostKafka, String puertoKafka, FWQ_WaitingTimeServer fwq, int puerto){
        this.skCliente = p_cliente;
        this.host = hostKafka;
        this.puerto = puertoKafka;
        this.pPuerto = puerto;
        this.fw = fwq;
    }

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
            System.out.println("Escribo: " + p_datos);
            flujo.writeUTF(p_datos);
        } catch(Exception io) {
            io.getMessage();
        }
        return;
    }

    public void run(){
        String cadena = "";

        FWQ_WaitingTimeServer wait = new FWQ_WaitingTimeServer();

        try{
            cadena = this.leeSocket(skCliente, cadena);
            this.escribeSocket(skCliente, wait.pasarMapa());

            wait.consumidor(host, puerto, fw, pPuerto);

            skCliente.close();

        }catch(Exception e){
            System.out.println("error: " + e.toString());
        }
    }
}
