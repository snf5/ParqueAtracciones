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
    public HashMap<String, String> players = new HashMap<>();
    // id + pos
    public HashMap<String, String> playersPos = new HashMap<>();
    // id + dest
    public HashMap<String, String> playersDes = new HashMap<>();
    // tiempoEsp + pos
    public static HashMap<String, String> mapa = new HashMap<>();


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

    public void producerVis() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        KafkaProducer<String,String> producer = new KafkaProducer<>(properties);
    }

    // id:posicion:destino
    public void consumerVis(String visitante) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
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
                //todo
                //playersDes.replace();
            }
        }
    }

    /*
    if the credentials are correct, producer will send the map
     */
    public void producerEng(boolean comprobar, String usuario) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        KafkaProducer<String,String> producer = new KafkaProducer<>(properties);

        if(comprobar) {
            producer.send(new ProducerRecord<String, String>("topiccredenciales",usuario + ":" + mapa.toString()));
        } else {
            producer.send(new ProducerRecord<String, String>("topiccredenciales","ko"));
        }

        producer.flush();
        producer.close();
    }

    /*
    Consumer read and check if the credentials are correct
     */
    // usuario:contraseña:posición
    public void consumerEng(String credenciales) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "topiccredenciales");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
        consumer.subscribe(Collections.singleton("topiccredenciales"));

        boolean comprobacion;
        String[] credencials = new String[0];
        while(true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for(ConsumerRecord<String, String> result : records) {
                credencials = result.value().toString().split(":");
            }
            comprobacion = comprobar(credencials);
            producerEng(comprobacion, credencials[0]);
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
        String IPColas = "";
        String puertoColas = "";

        if(args.length < 5) {
            System.out.println("Faltan argumentos por pasar.");
            System.exit(1);
        }

        IPBro = args[0];
        puertoBro = args[1];
        visit = args[2];
        IPColas = args[3];
        puertoColas = args[4];

        HashMap<String, String> ey = new HashMap<>();
        ey.put("joder","quéAsco");
        ey.put("joder2","quéAsco2");
        ey.put("joder3","quéAsco3");
        ey.put("joder4","quéAsco4");
        ey.put("joder5","quéAsco5");
        for(String hola : ey.keySet()) {
            System.out.println(hola);
            System.out.println(ey.get(hola));
        }

        //mapa = actualizarMapa(fwq.transmision(IPBro,puertoBro));


    }
}