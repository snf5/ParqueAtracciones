
import com.mongodb.client.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.bson.Document;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.swing.*;

public class FWQ_Engine {

    // id + usuario
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

        producer.send(new ProducerRecord<String, String>("devolvermapa","keya", mapa2.toString() + ":" + playersPos.toString() + ":" + playersDes.toString() + ":" + players.toString()));
        System.out.println("Envio el mapa entero de posiciones ocupadas: " + mapa2.toString() + playersPos.toString() + playersDes.toString());

        pVis = 1;
    }

    // usuario:id:posicion:destino???????
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
            //System.out.println("espero en consumer vis");
            try {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> datos : records) {
                    visit = datos.value().toString().split(":");
                }

                if(visit.length != 0) {

                    if(visit[1].equals("out")){
                        //borramos al usuario de los maps
                        if(playersDes.containsKey(visit[0])){
                            playersDes.remove(visit[0]);
                        }

                        if(playersPos.containsKey(visit[0])){
                            playersPos.remove(visit[0]);
                        }

                        if(players.containsKey(visit[0])){
                            players.remove(visit[0]);
                        }
                        numVisit--;
                    }else {

                        if (playersDes.containsKey(visit[0])) {
                            playersDes.replace(visit[0], visit[2]);
                        } else {
                            playersDes.put(visit[0], visit[2]);
                        }

                        if (playersPos.containsKey(visit[0])) {
                            playersPos.replace(visit[0], visit[1]);
                        }

                        //System.out.println("Jugadores posicion: " + playersPos.toString());
                        //System.out.println("Jugadores destino: " + playersDes.toString());
                        cVis = 1;
                    }
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
    //envia j1:mapaAtracciones
    //podriamos enviar j1:mapaAtracciones:usuario?? y asi controlamos bien a la hora de leer cuando se cae engine?
    public static void producerEng(boolean parkFull, boolean comprobar, String usuario, String localhost) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, localhost);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String,String> producer = new KafkaProducer<>(properties);

        int num = numVisit;


        if(comprobar) {
            if(parkFull == false) {
                //le tengo que enviar el mapa2 ya que es el que tiene tiempo:ubicacion
                producer.send(new ProducerRecord<String, String>("acceso","keya","J" + String.valueOf(num) + ":" + mapa2.toString() + ":" + usuario));
            } else {

                String noCabe = usuario +":ko:0";
                producer.send(new ProducerRecord<String, String>("acceso","keya", noCabe));
            }
        } else {

            String noCabe = usuario +":ko:0";
            producer.send(new ProducerRecord<String, String>("acceso","keya",noCabe));
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
                    System.out.println("Recibo del visitante: " + result.value().toString());
                    credencials = result.value().toString().split(":");


                    //credencials[0] = usuario
                    boolean siEsta = false;

                    //repasar pq no funciona bienn

                    if (credencials.length != 0) {
                        if (credencials.length == 3) {

                            for (String datos : players.keySet()) {
                                // System.out.println(datos + "-----------" + credencials[0]);
                                if (datos.equals(credencials[0].toString())) {
                                    siEsta = true;
                                    cEng = 1;
                                }
                            }

                            if (credencials.length != 0 && siEsta == false) {
                                comprobacion = comprobar(credencials);

                                if (numVisit < maxVisit) {
                                    lleno = false;
                                } else {
                                    lleno = true;
                                }
                                //System.out.println(lleno + " " + comprobacion + " " + credencials[0] + " " + localhost);


                                //System.out.println("numvisit: " + numVisit + " maxvisit: " + maxVisit);
                                if (comprobacion && numVisit < maxVisit) {
                                    System.out.println("me metoooo");
                                    //System.out.println("me meto a añadir usuarios");
                                    numVisit++;
                                    playersPos.put("J" + numVisit, credencials[2]);
                                    players.put("J" + numVisit, credencials[0]);

                                    producerEng(lleno, comprobacion, credencials[0], localhost);

                                    cEng = 1;

                                } else {
                                    producerEng(lleno, comprobacion, credencials[0], localhost);
                                }
                            }
                        } else {
                            //recibo jugadores que ya estan en el parque
                            //recibo vero:vero:5,5:8,14
                            comprobacion = comprobar(credencials);

                            if (numVisit < maxVisit) {
                                numVisit++;
                                playersPos.put("J" + numVisit, credencials[2]);
                                players.put("J" + numVisit, credencials[0]);

                                lleno = false;

                                producerEng(lleno, comprobacion, credencials[0], localhost);

                                cEng = 1;
                            } else {
                                lleno = true;
                                producerEng(lleno, comprobacion, credencials[0], localhost);
                            }
                        }

                    }
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
        //System.out.println("en actualizo recibo: " + mapa);
        //recibo shambala=35, furius=40...
        HashMap<String,String> newMap = new HashMap<>();

        String mapaVisitante = "";
        String[] elMapa = mapa.split(", ");
        String[] parto, partoMapa2;
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
        //System.out.println(newMap.toString());
        //shambala=25
        //System.out.println(mapa);

        mapa2 = new HashMap<>();


        for(int i = 0;i < elMapa.length; i++){
            parto = elMapa[i].split("=");
            //parto[0]=shambala
            //parto[1]=25
            //partoMapa2 = mapa2.toString().split(", ");
            for(String claveDos : newMap.keySet()) {
                //System.out.println(claveDos);
                //System.out.println(parto[0]);
                if(claveDos.equals(parto[0])) {
                    //System.out.println("añadooo: " + parto[1] + " " + newMap.get(claveDos).toString());
                    //ALGUNA CONDICION...
                    //habra que cambiarlo
                    //mapa2.put(newMap.get(claveDos),parto[1]);
                    //if(newMap.get(claveDos).equals()){
                    //}else {

                        mapa2.put(parto[1], newMap.get(claveDos));


                    //}

                }
            }
        }
        //System.out.println("mapa finaaaaaal: " + mapa2.toString());
        //System.out.println("mapa en actualizo: " + mapaVisitante.toString());

        return mapa2.toString();
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
    public static String transmisionn(String p_host, String p_puerto, boolean cerrar){

        Socket skCliente = new Socket();

        if(cerrar == true){
            try {
                skCliente.close();
                System.exit(0);
            } catch (Exception io) {
            io.getMessage();
        }
        }else {

            try {
                skCliente.close();

                skCliente = new Socket(p_host, Integer.parseInt(p_puerto));

                System.out.println("Pásame el mapa.");

                escribirSocket(skCliente, "hola");

                String datos = "";

                datos = leeSocket(skCliente, datos);

                skCliente.close();

                mapa = datos;

                System.out.println("Recibo el mapa de atracciones: " + datos);

                //cerrar socket

            } catch (Exception io) {
                io.getMessage();
            }
        }

        return mapa;
    }


    public static void main(String[] args) {

        cerrar cierr = new cerrar();
        cierr.setVisible(true);

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

        /*
         */

        while(true) {
            cEng = 0;
            pEng = 0;
            cVis = 0;
            pVis = 0;


            try {
                boolean cierro = false;

                mapa = transmisionn(IPBro, puertoBro, cierro);
                Thread.sleep(100);
                //en mapa tengo shambala=25...
                //hay que actualizar el mapa de 25=5,5
                if(!mapa.equals("")) {
                    actualizo(mapa.substring(1, mapa.length() - 1));
                }
                Thread.sleep(100);
                while (cEng == 0) {
                    consumerEng("", gestorDeColas);
                }
                Thread.sleep(100);
                //aqui no se mete!!!!!!???????
                while (cVis == 0) {
                    consumerVis("", gestorDeColas);

                }
                Thread.sleep(100);
                while (pVis == 0) {
                    producerVis(gestorDeColas);
                }
                Thread.sleep(100);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}

class cerrar extends JFrame{

    private JButton cerrar;

    public cerrar(){
        setTitle("Engine");
        setResizable(false);
        setSize(390, 220);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cerrar = new JButton("Cerrar engine");
        add(cerrar);
        cerrar.reshape(60, 10, 200, 30);

        cerrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cerrarEngine(e);
            }
        });
    }

    private void cerrarEngine(ActionEvent e){

        FWQ_Engine fwq = new FWQ_Engine();

        boolean cerrar = true;

        fwq.transmisionn("localhost", "9998", cerrar);
    }
}
