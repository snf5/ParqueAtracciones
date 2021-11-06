
import com.mongodb.client.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.bson.Document;

import java.io.*;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class FWQ_Engine {
    // id + nombre
    private static HashMap<String, String> players = new HashMap<>();
    // id + pos
    private static HashMap<String, String> playersPos = new HashMap<>();
    // id + dest
    private static HashMap<String, String> playersDes = new HashMap<>();
    // tiempoEsp + pos
    private static HashMap<String, String> mapa2 = new HashMap<>();

    private static String mapa = "";

    // num visitantes
    private static int numVisit = 0;
    // num máximo visitantes
    private static int maxVisit;

    private static int cEng;
    private static int pEng;
    private static int cVis;
    private static int pVis;


    public static String leeSocket(Socket p_sk, String p_Datos) {
        try {
            InputStream aux = p_sk.getInputStream();
            DataInputStream flujo = new DataInputStream(aux);
            p_Datos = new String();
            p_Datos = flujo.readUTF();
        } catch (Exception io) {
            io.getMessage();
        }
        return p_Datos;
    }

    public static void escribirSocket(Socket p_sk, String p_Datos) {
        try {
            OutputStream aux = p_sk.getOutputStream();
            DataOutputStream flujo = new DataOutputStream(aux);
            flujo.writeUTF(p_Datos);
        } catch(Exception io) {
            io.getMessage();
        }
    }

    public static void producerVis(String localhost) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, localhost);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String,String> producer = new KafkaProducer<>(properties);

        producer.send(new ProducerRecord<String, String>("devolvermapa","keya", mapa2.toString() + ":" + playersPos.toString() + ":" + playersDes.toString()));
        System.out.println("envio el mapa bien en producerVis " + mapa2.toString() + playersPos.toString() + playersDes.toString());

        pVis = 1;
    }

    // id:posicion:destino
    public static void consumerVis(String visitante, String localhost) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, localhost);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "topicmapa");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
        consumer.subscribe(Collections.singleton("topicmapa"));

        String[] visit = new String[0];
        while(visit.length == 0) {
            System.out.println("espero en consumer vis");
            try {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> datos : records) {
                    visit = datos.value().toString().split(":");
                }
                if(visit.length != 0) {

                    if (playersDes.containsKey(visit[0])) {
                        playersDes.replace(visit[0], visit[2]);
                    }else{
                        playersDes.put(visit[0], visit[2]);
                    }
                    if (playersPos.containsKey(visit[0])) {
                        playersPos.replace(visit[0], visit[1]);
                    }
                    System.out.println("Jugadores posicion: " + playersPos.toString());
                    System.out.println("Jugadores destino: " + playersDes.toString());
                    cVis = 1;
                }
            }finally {
                if(visit.length != 0){
                    consumer.close();
                }
            }
        }
    }


    //funciona
    //if the credentials are correct, producer will send the map
    public static void producerEng(boolean parkFull, boolean comprobar, String usuario, String localhost) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, localhost);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String,String> producer = new KafkaProducer<>(properties);

        if(comprobar) {
            if(parkFull) {
                //le tengo que enviar el mapa2 ya que es el que tiene tiempo:ubicacion
                producer.send(new ProducerRecord<String, String>("acceso","keya",usuario + ":" + mapa2.toString()));
            } else {
                producer.send(new ProducerRecord<String, String>("acceso","keya", "Parque lleno."));
            }
        } else {
            producer.send(new ProducerRecord<String, String>("acceso","keya","ko:0"));
        }

        producer.flush();
        producer.close();
    }

    //funciona
    //Consumer read and check if the credentials are correct
    // usuario:contraseña:1,1
    public static void consumerEng(String credenciales, String localhost) {

        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, localhost);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "creden");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
        consumer.subscribe(Collections.singleton("topiccredenciales"));

        boolean comprobacion = false;
        boolean lleno = false;
        String[] credencials = new String[0];

        while(credencials.length == 0) {
            System.out.println("espero");
            try {
                ConsumerRecords<String, String> records = consumer.poll(100);

                for (ConsumerRecord<String, String> result : records) {
                    System.out.println("Estoy en consumerEng y recibo: " + result.value().toString());
                    credencials = result.value().toString().split(":");
                }

                //credencials[0] = usuario

                for(int i = 0;i < players.size();i++){

                }

                if(credencials.length != 0) {
                    comprobacion = comprobar(credencials);

                    if (numVisit < maxVisit) {
                        lleno = true;
                    } else {
                        lleno = false;
                    }
                    System.out.println(lleno + " " + comprobacion + " " + credencials[0] + " " + localhost);
                    producerEng(lleno, comprobacion, credencials[0], localhost);

                    System.out.println("numvisit: " + numVisit + " maxvisit: " + maxVisit);
                    if (comprobacion && numVisit < maxVisit) {
                        System.out.println("me meto a añadir usuarios");
                        numVisit++;
                        playersPos.put("J" + numVisit, credencials[2]);
                        players.put("J" + numVisit, credencials[0]);
                    }
                    System.out.println("Jugadores existente: " + playersPos.toString());
                    cEng = 1;
                }
            } finally {
                if(credencials.length != 0) {
                    consumer.close();
                }
            }
        }
    }

    //FUNCIONA 4/11/21
    public static boolean comprobar(String[] credenciales) {
        String client = "";
        String password = "";
        HashMap<String, String> credence = new HashMap<>();
        boolean solution = false;

        MongoClient cliente = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = cliente.getDatabase("parque");
        MongoCollection<Document> docu = db.getCollection("usuario");
        FindIterable<Document> documents = docu.find();
        for(Document document : documents) {
            client = document.get("alias").toString();
            password = document.get("contrasenya").toString();
            credence.put(client, password);
        }
        for(String datos : credence.keySet()) {
            if(credenciales[0].equals(datos) && credenciales[1].equals(credence.get(datos))) {
                solution = true;
            }
        }
        return solution;
    }

    //borrar
    public static HashMap<String,String> actualizarMapa(HashMap<String, String> map) {
        HashMap<String,String> newMap = new HashMap<>();
        String hols = "";
        String holsPos = "";
        //MongoClient cliente = MongoClients.create("mongodb+srv://sergiopaco:Sistemas12345@cluster0.wyb5t.mongodb.net/par?retryWrites=true&w=majority");
        MongoClient cliente = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = cliente.getDatabase("parque");
        MongoCollection<Document> docu = db.getCollection("atraccion");
        FindIterable<Document> documents = docu.find();
        for(Document document : documents) {
            hols = document.get("id").toString();
            holsPos = document.get("ubicacion").toString();
            newMap.put(hols,holsPos);
        }
        for(String clave : map.keySet()) {
            for(String claveDos : newMap.keySet()) {
                if(claveDos.equals(clave)) {
                    newMap.put(map.get(clave),newMap.get(claveDos));
                }
            }
        }
        return newMap;
    }

    //todo metodonuevo
    //funciona
    public static String actualizo(String mapa){
        //recibo shambala=35, furius=40...
        HashMap<String,String> newMap = new HashMap<>();

        String mapaVisitante = "";
        String[] elMapa = mapa.split(", ");
        String[] parto;
        String hols = "";
        String holsPos = "";

        MongoClient cliente = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = cliente.getDatabase("parque");
        MongoCollection<Document> docu = db.getCollection("atraccion");
        FindIterable<Document> documents = docu.find();
        for(Document document : documents) {
            hols = document.get("id").toString();
            holsPos = document.get("ubicacion").toString();
            newMap.put(hols,holsPos);
        }

        //shambala=5,5
        System.out.println(newMap.toString());
        //shambala=25
        System.out.println(mapa);

        for(int i = 0;i < elMapa.length; i++){
            parto = elMapa[i].split("=");
            //parto[0]=shambala
            //parto[1]=25
            for(String claveDos : newMap.keySet()) {
                System.out.println(claveDos);
                System.out.println(parto[0]);
                if(claveDos.equals(parto[0])) {
                    mapa2.put(parto[1],newMap.get(claveDos));
                }
            }
        }

        System.out.println(mapa2.toString());


        return mapaVisitante;
    }

    //borrar
    public String[] separador(String[] fragmento) {
        String[] resultado = new String[0];
        String[] resultadoDos = new String[0];
        String[] resultadoTres = new String[0];
        for(int i = 0; i < fragmento.length; i++) {
            resultado = fragmento[i].split(",");
        }
        for(int i = 0; i < resultado.length; i++) {
            resultadoDos = resultado[i].split("\\}");
        }
        for(int i = 0; i < resultadoDos.length; i++) {
            resultadoTres = resultadoDos[i].split("=");
        }
        return resultadoTres;
    }

    //borrar
    public  HashMap<String, String> transmision(String p_host, String p_puerto) {
        String[] mensajeUno;
        String[] mensajeTerminado;
        HashMap<String,String> mapaServidor = new HashMap<>();
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);

        try {
            Socket skCliente = new Socket(p_host, Integer.parseInt(p_puerto));
            System.out.println("Pásame el mapa.");
            mensajeUno = br.readLine().toString().split("\\{");
            mensajeTerminado = separador(mensajeUno);
            for(int i = 0; i < mensajeTerminado.length; i++) {
                mapaServidor.put(mensajeTerminado[i],mensajeTerminado[i+1]);
                i++;
            }
            escribirSocket(skCliente,"Operación realizada con éxito");
            skCliente.close();
        } catch(Exception io){
            io.getMessage();
        }
        return mapaServidor;
    }

    //todo metodo nuevo
    //funciona
    public static String transmisionn(String p_host, String p_puerto){

        try{
            Socket skCliente = new Socket(p_host, Integer.parseInt(p_puerto));

            System.out.println("Pásame el mapa.");

            escribirSocket(skCliente, "hola");

            String datos = "";

            datos = leeSocket(skCliente, datos);

            mapa = datos;

            System.out.println(datos);

        }catch(Exception io){
            io.getMessage();
        }

        return mapa;
    }

    public static void main(String[] args) {
        FWQ_Engine fwq = new FWQ_Engine();
        String IPBro = "";
        String puertoBro = "";
        String visit = "";
        String IPColas = "";
        String puertoColas = "";

        numVisit = 0;

        if(args.length < 5) {
            System.out.println("Faltan argumentos por pasar.");
            System.exit(1);
        }

        IPColas = args[0];
        puertoColas = args[1];
        maxVisit = Integer.parseInt(args[2]);
        IPBro = args[3];
        puertoBro = args[4];
        String gestorDeColas = "";
        gestorDeColas = args[0] + ":" + args[1];



        while(true) {
            cEng = 0;
            pEng = 0;
            cVis = 0;
            pVis = 0;

            mapa = transmisionn(IPBro, puertoBro);
            //en mapa tengo shambala=25...
            //hay que actualizar el mapa de 25=5,5
            actualizo(mapa.substring(1, mapa.length()-1));
            while(cEng == 0) {
                consumerEng("", gestorDeColas);
            }
            //aqui no se mete!!!!!!???????
            while(cVis == 0){
                consumerVis("", gestorDeColas);

            }
            while(pVis == 0){
                producerVis(gestorDeColas);
            }
        }

        /*HashMap<String, String> ey = new HashMap<>();
        ey.put("joder","quéAsco");
        ey.put("joder2","quéAsco2");
        ey.put("joder3","quéAsco3");
        ey.put("joder4","quéAsco4");
        ey.put("joder5","quéAsco5");
        for(String hola : ey.keySet()) {
            //System.out.println(hola);
            //System.out.println(ey.get(hola));
        }

        //mapa = actualizarMapa(fwq.transmision(IPBro,puertoBro));
        */

    }
}

/*
import com.mongodb.client.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.bson.Document;

import java.io.*;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class FWQ_Engine {
    // id + nombre
    private HashMap<String, String> players = new HashMap<>();
    // id + pos
    private HashMap<String, String> playersPos = new HashMap<>();
    // id + dest
    private HashMap<String, String> playersDes = new HashMap<>();
    // tiempoEsp + pos
    private static HashMap<String, String> mapa = new HashMap<>();
    // num visitantes
    private int numVisit = 0;
    // num máximo visitantes
    private static int maxVisit;


    public String leeSocket(Socket p_sk, String p_Datos) {
        try {
            InputStream aux = p_sk.getInputStream();
            DataInputStream flujo = new DataInputStream(aux);
            p_Datos = new String();
            p_Datos = flujo.readUTF();
        } catch (Exception io) {
            io.getMessage();
        }
        return p_Datos;
    }

    public void escribirSocket(Socket p_sk, String p_Datos) {
        try {
            OutputStream aux = p_sk.getOutputStream();
            DataOutputStream flujo = new DataOutputStream(aux);
            flujo.writeUTF(p_Datos);
        } catch(Exception io) {
            io.getMessage();
        }
    }

    public void producerVis(String localhost) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, localhost);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        KafkaProducer<String,String> producer = new KafkaProducer<>(properties);

        producer.send(new ProducerRecord<String, String>("topicmapa",mapa.toString() + ":" + playersPos.toString() + ":" + playersPos.toString()));
    }

    // id:posicion:destino
    public void consumerVis(String visitante, String localhost) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, localhost);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "topicmapa");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
        consumer.subscribe(Collections.singleton("topicmapa"));

        String[] visit = new String[0];
        while(true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for(ConsumerRecord<String, String> datos : records) {
                visit = datos.value().toString().split(":");
            }
            if(playersDes.containsKey(visit[0])) {
                playersDes.replace(visit[0],visit[2]);
            }
            if(playersPos.containsKey(visit[0])) {
                playersPos.replace(visit[0],visit[1]);
            }
        }
    }


    //if the credentials are correct, producer will send the map

public void producerEng(boolean parkFull, boolean comprobar, String usuario, String localhost) {
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, localhost);
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

    KafkaProducer<String,String> producer = new KafkaProducer<>(properties);

    if(comprobar) {
        if(parkFull) {
            producer.send(new ProducerRecord<String, String>("topiccredenciales",usuario + ":" + mapa.toString()));
        } else {
            producer.send(new ProducerRecord<String, String>("topiccredenciales", "Parque lleno."));
        }
    } else {
        producer.send(new ProducerRecord<String, String>("topiccredenciales","ko"));
    }

    producer.flush();
    producer.close();
}


    //Consumer read and check if the credentials are correct

    // usuario:contraseña:posición
    public void consumerEng(String credenciales, String localhost) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, localhost);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "topiccredenciales");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
        consumer.subscribe(Collections.singleton("topiccredenciales"));

        boolean comprobacion;
        boolean lleno;
        String[] credencials = new String[0];
        while(true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for(ConsumerRecord<String, String> result : records) {
                credencials = result.value().toString().split(":");
            }
            comprobacion = comprobar(credencials);
            if(numVisit < maxVisit) {
                lleno = true;
            } else {
                lleno = false;
            }
            producerEng(lleno, comprobacion, credencials[0], localhost);
            if(comprobacion && numVisit < maxVisit) {
                numVisit++;
                playersPos.put("J" + numVisit,credencials[2]);
                players.put("J" + numVisit,credencials[0]);
            }
        }
    }

    public boolean comprobar(String[] credenciales) {
        String client = "";
        String password = "";
        HashMap<String, String> credence = new HashMap<>();
        boolean solution = false;

        MongoClient cliente = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = cliente.getDatabase("Parque");
        MongoCollection<Document> docu = db.getCollection("Atraccion");
        FindIterable<Document> documents = docu.find();
        for(Document document : documents) {
            client = document.get("alias").toString();
            password = document.get("contrasenya").toString();
            credence.put(client, password);
        }
        for(String datos : credence.keySet()) {
            if(credenciales[0].equals(datos) && credenciales[1].equals(credence.get(datos))) {
                solution = true;
            }
        }
        return solution;
    }

    public static HashMap<String,String> actualizarMapa(HashMap<String, String> map) {
        HashMap<String,String> newMap = new HashMap<>();
        String hols = "";
        String holsPos = "";
        //MongoClient cliente = MongoClients.create("mongodb+srv://sergiopaco:Sistemas12345@cluster0.wyb5t.mongodb.net/par?retryWrites=true&w=majority");
        MongoClient cliente = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = cliente.getDatabase("Parque");
        MongoCollection<Document> docu = db.getCollection("Atraccion");
        FindIterable<Document> documents = docu.find();
        for(Document document : documents) {
            hols = document.get("id").toString();
            holsPos = document.get("ubicacion").toString();
            newMap.put(hols,holsPos);
        }
        for(String clave : map.keySet()) {
            for(String claveDos : newMap.keySet()) {
                if(claveDos.equals(clave)) {
                    newMap.put(map.get(clave),newMap.get(claveDos));
                }
            }
        }
        return newMap;
    }

    public String[] separador(String[] fragmento) {
        String[] resultado = new String[0];
        String[] resultadoDos = new String[0];
        String[] resultadoTres = new String[0];
        for(int i = 0; i < fragmento.length; i++) {
            resultado = fragmento[i].split(",");
        }
        for(int i = 0; i < resultado.length; i++) {
            resultadoDos = resultado[i].split("\\}");
        }
        for(int i = 0; i < resultadoDos.length; i++) {
            resultadoTres = resultadoDos[i].split("=");
        }
        return resultadoTres;
    }

    public HashMap<String, String> transmision(String p_host, String p_puerto) {
        String[] mensajeUno;
        String[] mensajeTerminado;
        HashMap<String,String> mapaServidor = new HashMap<>();
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);

        try {
            Socket skCliente = new Socket(p_host, Integer.parseInt(p_puerto));
            System.out.println("Pásame el mapa.");
            mensajeUno = br.readLine().toString().split("\\{");
            mensajeTerminado = separador(mensajeUno);
            for(int i = 0; i < mensajeTerminado.length; i++) {
                mapaServidor.put(mensajeTerminado[i],mensajeTerminado[i+1]);
                i++;
            }
            escribirSocket(skCliente,"Operación realizada con éxito");
            skCliente.close();
        } catch(Exception io){
            io.getMessage();
        }
        return mapaServidor;
    }


    public static void main(String[] args) {
        FWQ_Engine fwq = new FWQ_Engine();
        String IPBro = "";
        String puertoBro = "";
        String visit = "";
        String gestorDeColas = "";

        if(args.length < 5) {
            System.out.println("Faltan argumentos por pasar.");
            System.exit(1);
        }

        IPBro = args[0];
        puertoBro = args[1];
        visit = args[2];
        gestorDeColas = args[3] + ":" + args[4];
        maxVisit = Integer.parseInt(visit);
        while(true) {
            mapa = actualizarMapa(fwq.transmision(IPBro,puertoBro));
            fwq.consumerEng("",gestorDeColas);
            fwq.consumerVis("",gestorDeColas);
            fwq.producerVis(gestorDeColas);
        }
    }
}
 */


