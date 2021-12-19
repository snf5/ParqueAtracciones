
import com.mongodb.client.*;
import io.scalajs.nodejs.crypto.Hash;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.conversions.Bson;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;

import com.google.gson.*;
import com.google.gson.reflect.*;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;


public class FWQ_Engine {

    // id + usuario
    private static HashMap<String, String> players = new HashMap<>();
    // id + pos
    private static HashMap<String, String> playersPos = new HashMap<>();
    // id + dest
    private static HashMap<String, String> playersDes = new HashMap<>();
    // tiempoEsp + pos
    private static HashMap<String, String> mapa2 = new HashMap<>();
    // ciudad + temperatura
    private static Map<String, String> cuidades = new HashMap<>();
    // ciudad (1, 2, 3 o 4) + temperatura
    private static HashMap<Integer, String> ciudadesCerradas = new HashMap<Integer, String>();

    private static boolean introducirCiudades = false;

    private static String mapa = "";

    private static String elMapaComprobar = "";
    private static int igualesW = 0;

    // num visitantes
    private static int numVisit = 0;
    // num máximo visitantes
    private static int maxVisit;

    private static int cEng;
    private static int pEng;
    private static int cVis;
    private static int pVis;

    private static SecretKey key;
    private static Cipher cipher;
    private static String algoritmo = "AES";
    private static int keysize = 16;

    public static void addKey(String value){
        byte[] valueBytes = value.getBytes();
        key = new SecretKeySpec(Arrays.copyOf(valueBytes, keysize), algoritmo);
    }

    public static String encriptar(String texto){
        String value = "";

        try{
            cipher = Cipher.getInstance(algoritmo);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] textobytes = texto.getBytes();
            byte[] cipherbytes = cipher.doFinal(textobytes);
            value = Base64.getEncoder().encodeToString(cipherbytes);

        }catch(NoSuchAlgorithmException ex){

        }catch (NoSuchPaddingException ex2){

        }catch (InvalidKeyException ex3){

        }catch (IllegalBlockSizeException ex4){

        }catch(BadPaddingException ex5){

        }

        return value;
    }

    public static String desencriptar(String texto){
        String str = "";

        try{
            byte[] value = Base64.getDecoder().decode(texto);
            cipher = Cipher.getInstance(algoritmo);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] cipherbytes = cipher.doFinal(value);
            str = new String(cipherbytes);

        }catch(NoSuchAlgorithmException ex){

        }catch (NoSuchPaddingException ex2){

        }catch (InvalidKeyException ex3){

        }catch (IllegalBlockSizeException ex4){

        }catch(BadPaddingException ex5){

        }
        return str;
    }


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

        String wait = "";

        if(igualesW >= 5){
            wait = "ko";
        }else{
            wait = "ok";
        }

        String pasarInf = mapa2.toString() + ":" + playersPos.toString() + ":" + playersDes.toString() + ":" + players.toString() + ":" + cuidades.toString() + ":" + wait;
        String pasarInfEncriptadas = encriptar(pasarInf);

        producer.send(new ProducerRecord<String, String>("devolvermapa","keya", pasarInfEncriptadas));
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
        String visitEncrptadas = "";
        while(visit.length == 0) {
            //System.out.println("espero en consumer vis");
            try {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> datos : records) {
                    visitEncrptadas = desencriptar(datos.value().toString());
                    visit = visitEncrptadas.split(":");
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
        String noCabe = usuario +":ko:0";
        String nocabeEncriptada = encriptar(noCabe);
        String pasarInf = "J" + String.valueOf(num) + ":" + mapa2.toString() + ":" + usuario + ":" + ciudadesCerradas.toString();
        String pasarInfEncriptada = encriptar(pasarInf);
        System.out.println("envio???");
        System.out.println(pasarInf);

        if(comprobar) {
            if(parkFull == false) {
                //le tengo que enviar el mapa2 ya que es el que tiene tiempo:ubicacion
                producer.send(new ProducerRecord<String, String>("acceso","keya",pasarInfEncriptada));
            } else {
                System.out.println("GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG");
                producer.send(new ProducerRecord<String, String>("acceso","keya", nocabeEncriptada));
            }
        } else {

            producer.send(new ProducerRecord<String, String>("acceso","keya",nocabeEncriptada));
        }
        System.out.println("si que envio");
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
        String credencialsEncriptadas = "";

        while(credencials.length == 0) {
            System.out.println("espero");
            try {

                ConsumerRecords<String, String> records = consumer.poll(100);

                for (ConsumerRecord<String, String> result : records) {
                    System.out.println("Recibo del visitante: " + result.value().toString());

                    String comprueboEncriptado = result.value().toString();
                    if(comprueboEncriptado.contains("1,1")){
                        System.out.println("no esta encriptadoooooo");
                        producerEng(lleno, comprobacion, "no encriptado", localhost);
                    }else {


                        credencialsEncriptadas = desencriptar(result.value().toString());
                        credencials = credencialsEncriptadas.split(":");
                        System.out.println(credencialsEncriptadas);
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

    //todo practica 2
    public static String comprobarAtraccionTemperatura(String localizacion){

        String caracter = "0";
        String[] parto = localizacion.split(",");
        int x = Integer.parseInt(parto[0]), y = Integer.parseInt(parto[1]);
        System.out.println("x: " + x + " y: " + y);

        for (Integer clave:ciudadesCerradas.keySet()) {
            int valor = clave;
            System.out.println(valor);
            switch (valor){
                case 1:
                    if(x <= 10 && y <= 10){
                        caracter = "X";
                    }
                    break;
                case 2:
                    if(x > 10 && y <= 10){
                        caracter = "X";
                    }
                    break;
                case 3:
                    if(x <= 10 && y > 10){
                        caracter = "X";
                    }
                    break;
                case 4:
                    if(x > 10 && y > 10){
                        caracter = "X";
                    }
                    break;
                default:
                    break;
            }
            System.out.println("Clave: " + clave + ", valor: " + valor);
        }
        return caracter;
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
        String cerrado = "", iniciales = "";

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
                    cerrado = comprobarAtraccionTemperatura(newMap.get(claveDos));

                    //todo practica 2
                    if(cerrado.equals("0")){
                        mapa2.put(parto[1], newMap.get(claveDos));
                    }else{
                        //esta cerrada la atraccion
                        iniciales = String.valueOf(parto[0].charAt(0)) + cerrado;
                        mapa2.put(iniciales, newMap.get(claveDos));
                    }
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

    //todo si el socket no se abre TRANSMITIR ERRORRRRRRRR
    //todo
    //todo
    //todo
    //todo
    //todo
    //todo
    //todo
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

    //todo practica 2
    public static void guardarMapa(String mapa){

        String id = "";
        HashMap<String, String> credence = new HashMap<>();

        MongoClient cliente = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = cliente.getDatabase("parque");
        MongoCollection<Document> docu = db.getCollection("mapa");
        FindIterable<Document> documents = docu.find();

        for(Document document : documents) {
            id = document.get("id").toString();
            credence.put(id,"");
        }

        Document busco = new Document("id", "mapa");
        Document nuevo = new Document("$set" , new Document("composicion", mapa));

        for(String datos : credence.keySet()){
            if(datos.equals("mapa")){
                docu.findOneAndUpdate(busco, nuevo);
            }
        }

    }

    //borrar
    public static Map<String, Object> jsonToMap(String cadena){
        Map<String, Object> map = new Gson().fromJson(
                cadena, new TypeToken<HashMap<String, Object>>() {}.getType()
        );
        return map;
    }

    public static boolean urlValida(String url) {
        try{
            new URL(url).openStream().close();
            return true;
        }catch(Exception e){
        }
        return false;
    }

    //todo practica 2
    public static String servidorWeather(String ciudad){

        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;

        String linea;
        String key1 = "", htt = "";

        try {
            archivo = new File("C:\\datosTemperatura\\servidorTemperatura.txt");
            fr = new FileReader(archivo);
            br = new BufferedReader(fr);

            while((linea = br.readLine()) != null){
                if(linea.charAt(0) == 'h'){
                    htt = linea;
                }else{
                    key1 = linea;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        //String nombre = "Alicante";
        String nombre = ciudad;
        String devolver = "";
        /*
        String key = "b60702fba4c689bc2bff1247db983f4e";
        String urlString = "http://api.openweathermap.org/data/2.5/weather?q=" + nombre + "&appid=" + key + "&units=metric";
         */

        String urlString = htt + nombre + "&appid=" + key1 + "&units=metric";
        boolean urlValida = urlValida(urlString);

        if(urlValida) {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            HttpGet get = new HttpGet(urlString);
            HttpResponse respuesta = null;

            try {
                respuesta = client.execute(get);

                String responseString = new BasicResponseHandler().handleResponse(respuesta);
                //System.out.println(responseString);
                String[] parto = responseString.split("main");
                //System.out.println(parto[2]);
                String[] parto2 = parto[2].split(",");
                //System.out.println(parto2[0]);
                String[] parto3 = parto2[0].split(":");
                //aqui tengo guardado la temperatura
                //System.out.println(parto3[2]);
                //cuidades.put(nombre, parto3[2]);

                devolver = parto3[2].toString();
            } catch (IOException ioe) {

            } catch (Exception e) {

            }
        /*
        try{
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;

            while((line = rd.readLine()) != null){
                result.append(line);
            }
            rd.close();
            System.out.println(result);

            Map<String, Object> respuesta = jsonToMap(result.toString());
            Map<String, Object> temperatura = jsonToMap(respuesta.get("main").toString());

            System.out.println("Temperatura en " + nombre + ": " + temperatura.get("temp"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
         */
        }else{
            devolver = "0";
        }
        return devolver;
    }

    //todo practica 2
    public static void anyadirCerradas(int numero, String temperatura, String ciudad){

        float valorTemp = 0;
        valorTemp = Float.parseFloat(temperatura);

        if(valorTemp < 20 || valorTemp > 30){
            String tempyCiudad = ciudad + ":" + temperatura;
            ciudadesCerradas.put(numero, temperatura);
        }
    }

    //todo practica 2
    public static void anyadirCiudades(String a, String b, String c, String d){

        String  ciudad = "";
        String total = a + ":" + b + ":" + c + ":" + d;
        String[] parto = total.split(":");
        System.out.println("eeeeeeeeeeeeeeeee" + total);

        System.out.println("me meto a pasar ciudades");
        if(servidorWeather(a).equals("0") == false) {
            cuidades.clear();
            ciudadesCerradas.clear();
        }
        System.out.println(cuidades.toString());


        for(int i = 0;i < 4;i++){
            System.out.println("PASOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO: " + parto[i]);
            ciudad = servidorWeather(parto[i]);
            System.out.println(ciudad);
            if(ciudad.equals("0")){
                //System.out.println("URL no valida, introducir bien la URL");
            }else {
                String entero = "";
                switch (i){
                    case 0:
                        entero = "A-" + parto[i];
                        break;
                    case 1:
                        entero = "B-" + parto[i];
                        break;
                    case 2:
                        entero = "C-" + parto[i];
                        break;
                    case 3:
                        entero = "D-" + parto[i];
                        break;

                }
                cuidades.put(entero, ciudad);
                System.out.println("asi va el mapa: " + cuidades.toString());
                anyadirCerradas(i + 1, ciudad, parto[i]);
            }
        }

        if(ciudad.equals("0") == false) {
            for (String clave : cuidades.keySet()) {
                String valor = cuidades.get(clave);
                System.out.println("Clave: " + clave + ", valor: " + valor);
            }
            //System.out.println("yeeeeee" + cuidades.toString());
            //System.out.println("eeeeeeeeeeeeeeeee" + total);
            System.out.println(ciudadesCerradas.toString());
        }else{
            System.out.println("URL no valida, introducir bien la URL");
            System.out.println("No se ha podido conectar bien a la URL");
        }

        introducirCiudades = true;

        System.out.println("asi quedaria " + cuidades.toString());

        Map<String, String> ordenado = new TreeMap<>(cuidades);
        System.out.println("ordenadoooooo " + ordenado.toString());
        cuidades = ordenado;
    }


    public static void main(String[] args) {

        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;

        String linea;
        String lakey = "";

        try {
            archivo = new File("C:\\datosKey\\laKey.txt");
            fr = new FileReader(archivo);
            br = new BufferedReader(fr);

            while((linea = br.readLine()) != null){
                lakey = linea;
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        System.out.println(lakey);
        addKey(lakey);

        cerrar cierr = new cerrar();
        cierr.setVisible(true);

        //todo
        //todo     MUY IMPORTANTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE
        //todo
        //guardarMapa("holaaa");
        //servidorWeather();
        cuidadesTemp ciud = new cuidadesTemp();
        ciud.setVisible(true);

        //todo practica 2
        while(introducirCiudades == false){
            //System.out.print(introducirCiudades);
            System.out.print("");
        }

        System.out.println("paso");
        for (String clave : cuidades.keySet()) {
            String valor = cuidades.get(clave);
            System.out.println("Clave: " + clave + ", valor: " + valor);
        }


        FWQ_Engine fwq = new FWQ_Engine();
        String IPBro = "";
        String puertoBro = "";
        String visit = "";
        String IPColas = "";
        String puertoColas = "";

        numVisit = 0;

        if(args.length < 5) {
            System.out.println("Faltan argumentos por pasar.");
            //System.exit(1);
        }

        IPColas = args[0];
        puertoColas = args[1];
        maxVisit = Integer.parseInt(args[2]);
        IPBro = args[3];
        puertoBro = args[4];
        String gestorDeColas = "";
        gestorDeColas = args[0] + ":" + args[1];
        String mapaBBDD = "";

        while(true) {
            cEng = 0;
            pEng = 0;
            cVis = 0;
            pVis = 0;

            try {
                boolean cierro = false;


                numVisit = players.size();
                System.out.println("NUMERO VISITANTEEEEEEEEEEEEEEEEE " + numVisit);

                mapa = transmisionn(IPBro, puertoBro, cierro);

                if(elMapaComprobar != ""){
                    if(elMapaComprobar.equals(mapa)){
                        igualesW++;
                    }else{
                        igualesW = 0;
                    }
                }

                elMapaComprobar = mapa;
                Thread.sleep(100);
                //en mapa tengo shambala=25...
                //hay que actualizar el mapa de 25=5,5
                if(!mapa.equals("")) {
                    actualizo(mapa.substring(1, mapa.length() - 1));
                    System.out.println("aquiiiii: " + mapa2.toString());
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

                //todo anyadir si se cae waiting o sensores
                //nuevopractica2
                mapaBBDD = mapa2.toString() + ":" + playersPos.toString() + ":" + playersDes.toString() + ":" + players.toString() + ":" + cuidades.toString();
                if(igualesW >= 5){
                    mapaBBDD += ":" + "ko";
                }else{
                    mapaBBDD += ":" + "ok";
                }
                guardarMapa(mapaBBDD);

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

class cuidadesTemp extends JFrame{

    private JButton anyadir;

    private JLabel cA;
    private JLabel cB;
    private JLabel cC;
    private JLabel cD;

    private JTextField txtA;
    private JTextField txtB;
    private JTextField txtC;
    private JTextField txtD;

    public cuidadesTemp(){
        setTitle("Introduzca las 4 ciudades");
        setResizable(false);
        setSize(390, 220);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        anyadir = new JButton("Anyadir ciudades");
        cA = new JLabel("Ciudad A: ");
        cB = new JLabel("Ciudad B: ");
        cC = new JLabel("Ciudad C: ");
        cD = new JLabel("Ciudad D: ");
        txtA = new JTextField(10);
        txtB = new JTextField(10);
        txtC = new JTextField(10);
        txtD = new JTextField(10);

        add(anyadir);
        add(cA);
        add(cB);
        add(cC);
        add(cD);
        add(txtA);
        add(txtB);
        add(txtC);
        add(txtD);

        cA.reshape(20, 10, 100, 20);
        txtA.reshape(120, 10, 100, 20);

        cB.reshape(20, 35, 100, 20);
        txtB.reshape(120, 35, 100, 20);

        cC.reshape(20, 60, 100, 20);
        txtC.reshape(120, 60, 100, 20);

        cD.reshape(20, 85, 100, 20);
        txtD.reshape(120, 85, 100, 20);

        anyadir.reshape(20, 110, 200, 20);

        anyadir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                anyadirC(e);
            }
        });

    }

    private void anyadirC(ActionEvent e){

        FWQ_Engine fwq = new FWQ_Engine();

        boolean cerrar = true;

        fwq.anyadirCiudades(txtA.getText(), txtB.getText(), txtC.getText(), txtD.getText());
    }
}
