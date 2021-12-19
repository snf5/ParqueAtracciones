

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.*;

import Formularios.Inicio;
import Formularios.Registrar;
import com.mongodb.MongoClient;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import javax.swing.*;

import java.util.Base64;


import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.net.MalformedURLException;
import java.security.cert.Certificate;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import javax.net.ssl.HttpsURLConnection;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

class FWQ_VisitorApi {

    //
    private static String usu = "";
    private static String contras = "";
    private static String aliasUsu = "";

    //datos para ir pasandole a engine
    private static String id = "";
    private static String destino = "";
    private static String posicion = "";
    private static String xd = "";
    private static String yd = "";
    private static String xp = "";
    private static String yp = "";

    private static HashMap<String, String> primerJugadores = new HashMap<>();

    private static String mapa2 = "", mapa3 = "", todo = "";

    //datos para la conexión de los puertos e ips
    private static String hostRegistry, puertoRegistry, hostKafka, puertoKafka, gestorColas;
    private static String datosUsu = "";
    private static String usua = "";
    private static Inicio login;
    private static Registrar miRegistrar;

    private static logOutApi log;
    private static inicioApi ini;

    private static SecretKey key;
    private static Cipher cipher;
    private static String algoritmo = "AES";
    private static int keysize = 16;

    private static boolean servidorCaido = false;

    private static String ipVisitante = "";

    private static boolean activarEncriptacion = false;


    private static String mapaNova = "";

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

    public static void main(String args[]) {



        InetAddress adress = null;
        try {
            adress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ipVisitante = adress.getHostAddress();

        System.out.println("yeee: " + ipVisitante);

        System.out.println(ipVisitante);


        /* BORRAR
        log = new logOut();
        log.setVisible(true);
         */
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

        ini = new inicioApi();
        ini.setVisible(true);

        xp = "1";
        yp = "1";

        hostRegistry = args[0];
        puertoRegistry = args[1];

        hostKafka = args[2];
        puertoKafka = args[3];

        gestorColas = hostKafka + ":" + puertoKafka;

        int numero = 0;
        String usuario = "", contra = "", nombre = "", alias = "", contra2 = "";


        //comprobar que el registro se hizo correctamente
        //para ello el registry me debe devolver por socket
        //una confirmación
        /*
        topic:
        - credenciales
        - acceso
        - posicion
        - devolver mapa

        para realizar el inicio de sesión tengo que invocar al productor del topic entrarParque
        para mandar información a engine

        el consumidor que es engine devolverá un mensaje de si esta registrado o no y le devuelve un id de sesion

        si no esta registrado se iniciará la pagina de registro

        Productor para enviar datos de credenciales a engine
        Consumidor para recibir el mapa (en caso de que no este registrado, engine enviará mediante su productor un ko)
        Productor para ir madnando a engine sus movimientos y que los actualice en el mapa
         */


    }


    //recibo y guardo los datos del usuario para
    //ya desde aqui ir llamando al resto de modulos para que se lleve a cabo todo
    public static void recibirDatos(String usuario, String contra) {

        if(servidorCaido == false) {
            String entradaParque = "";

            posicion = "1,1";
            xp = "1";
            xd = "1";
            id = "";

            datosUsu = usuario + ":" + contra;
            aliasUsu = usuario;
            usua = usuario;
            contras = contra;
            //en datosUsu ya tengo usuario:contra
            String mitad = "";

            //System.out.println("estoy en recibir datos, antes de productorcredenciales");
            ///usuario y contraseña
            entradaParque = productorCredenciales(usuario, contra, posicion);

            if(entradaParque.contains("no encriptado")){
                while (entradaParque.contains("no enciptado")){
                    entradaParque = productorCredenciales(usuario, contra, posicion);
                }
            }

            //entradaParque va a recibir id:mapa entero Atracciones o ko:0
            //hacemos split para comprobar si ha podido entrar en el parque o no
            String informacion[] = new String[0];

            informacion = entradaParque.split(":");

            if(informacion.length != 0) {
                mitad = informacion[1].toString();
            }

            while(mitad.equals("")){
                mitad = "";
                entradaParque = productorCredenciales(usuario, contra, posicion);
                informacion = entradaParque.split(":");
                mitad = informacion[1].toString();
            }

            //informacion[0] es id/ko
            //informacion[1] es mapaAtracciones/0
            if (informacion[0].equals(usua) && informacion[1].equals("ko")) {
                //no puede entrar al parque hasta o que se registre o meta bien los datos
                System.out.print("Parque lleno...");
                System.exit(0);
            } else {
                //aqui muestro el botón de logout para que cuando se pinche
                //mande J1:out:bhif
                //y se elimine el jugador del parque
                //se puede entrar al parque y llamamos a prodcutor y consumidor
                //podriamods llamar a un metodo que se encargue de llamar todo el rato a productor y consumidor
                //aqui igualo id a informacion[0], para saber que id tiene cada jugador
                //j1, j2, j3...
                id = "";
                id = informacion[0];

                //le paso informacion[1], que es el mapa para que sepa donde esta y cual es su destino
                //OJOOOOO recibo un map de las posiciones ocupadas de las atracciones

                //quitar = 45=5,5, 24=7,10

                String medio = "", temperaturas = "";

                medio = informacion[1].toString();
                temperaturas = informacion[3].toString();
                System.out.println(entradaParque);
                while(medio.equals("")){
                    medio = "";
                    entradaParque = productorCredenciales(usuario, contra, posicion);
                    informacion = entradaParque.split(":");
                    medio = informacion[1].toString();
                    temperaturas = informacion[3].toString();
                }

                String quitar = medio.substring(1, medio.length() - 1);
                String temperaturasQuitar = temperaturas.substring(1, temperaturas.length()-1);
                //le paso la cadena tal que asi: 45=5,5, 24=7,10
                //y al hacer por ahi lo de [] me va sacando shamabal=12 ...
                //a moverse se le pasa el mapa limpio para elegir destino
                int contar = 0;

                if (contar == 0) {
                    FWQ_VisitorApi fwq = new FWQ_VisitorApi();
                    //ini.setOut();
                    if (ini.getSalir() == true) {
                        //salimos del parque
                    }
                }
                contar = 1;
                //todo enviar tambien en tempCiudades
                moverse(quitar, temperaturasQuitar);
            }
        }else{
            //todo
            //si que se ha iniciado sesion, pero el servidor esta caido
            posicion = xp + "," + yp;
            String volverEntrar = "";
            while(volverEntrar.equals("")) {

                volverEntrar = productorCredenciales(usuario, contra, posicion);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println(volverEntrar);
            //el id ya lo tengo asignado, asiq no cambio
            String[] mapa = volverEntrar.split(":");

            if(mapa[2].equals(usua)) {
                String quitar = mapa[1].toString().substring(1, mapa[1].toString().length() - 1);
                String quitar2 = mapa[3].toString().substring(1, mapa[2].toString().length() - 1);
                moverse(quitar, quitar2);
            }

            while(mapa[2].equals(usua) == false) {
                if (mapa[2].equals(usua)) {
                    String quitar = mapa[1].toString().substring(1, mapa[1].toString().length() - 1);
                    String quitar2 = mapa[3].toString().substring(1, mapa[2].toString().length() - 1);
                    moverse(quitar, quitar2);
                }
            }
        }
    }

    //los dos metodos de aqui abajo son para poder entrar al parque
    //consumidor que recibe por parte de engine la confirmación de acceso al parque
    public static String consumir() {

        Properties proper = new Properties();

        String mapa = "";
        String compruebo = "";
        String comprueboEncriptadas = "";
        boolean noEncriptadoVisitor = false;

        //añadir el grupo de consumidores de solo visitantes
        proper.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, gestorColas);
        proper.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        proper.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        proper.put(ConsumerConfig.GROUP_ID_CONFIG, "visitanteGroup"); //otro grupo???
        proper.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(proper);
        consumer.subscribe(Collections.singleton("acceso"));

        try {

            while(mapaNova.equals("")) {
                System.out.println("estoy al principio del whilw: " + mapa);
                //System.out.println("estoy en consumerrecord esperando");
                ConsumerRecords<String, String> records = consumer.poll(100);

                for (ConsumerRecord<String, String> record : records) {
                    //el productor me tiene que devolver
                    //informacion = record.value().toString().split(":");
                    compruebo = record.value().toString();
                    System.out.println("compruebo: " + compruebo);
                    if (compruebo.contains("no enciptado")) {
                        noEncriptadoVisitor = true;
                        System.out.println("pues lo tengo que encriptar");
                        //mapa = "no enciptado";
                    } else {
                        System.out.println("si que he enviado encriptado");
                        comprueboEncriptadas = desencriptar(compruebo);
                    }


                    //compruebo = id:mapaAtracciones
                    //o usuario:ko:0 si no puedo entrar al parque

                    mapa = comprueboEncriptadas;

                    if (noEncriptadoVisitor == false) {
                        String[] parto = comprueboEncriptadas.split(":");
                        mapaNova = comprueboEncriptadas;

                        if (parto[0].equals(usua) && parto[1].equals("ko")) {
                            //mapa = ko:0 ???
                            mapa = comprueboEncriptadas;

                        } else {
                            mapa = comprueboEncriptadas;

                        }
                    }else{
                        mapaNova = "no encriptado";
                    }

                }
            }

             /*

            while (mapaNova.equals("")){
                ConsumerRecords<String, String> records = consumer.poll(100);

                for (ConsumerRecord<String, String> record : records) {
                    compruebo = record.value().toString();
                    comprueboEncriptadas = desencriptar(compruebo);
                }
                String[] parto = comprueboEncriptadas.split(":");
                mapaNova = comprueboEncriptadas;

                if (parto[0].equals(usua) && parto[1].equals("ko")) {
                    //mapa = ko:0 ???
                    mapa = comprueboEncriptadas;

                } else {
                    mapa = comprueboEncriptadas;

                }
            }

             */
        } finally {
            consumer.close();
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mapaNova;
    }

    //productor que envia a traves de topiccredenciales
    //para que engine compruebe los datos del usuario
    //usuario:contra
    public static String productorCredenciales(String usuario, String contra, String posicion) {

        boolean entrar = false;

        String acceso = "";

        Properties proper = new Properties();

        datosUsu = usuario;

        String credenciales = "";
        //usuario,contraseña:posicion (1,1)
        if (id.equals("")) {
            credenciales = usuario + ":" + contra + ":" + posicion;
        } else {
            credenciales = id + ":" + contra + ":" + posicion;
        }

        if(servidorCaido == true){
            credenciales = "";
            credenciales = usuario + ":" + contra + ":" + posicion + ":" + destino;
        }

        proper.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, gestorColas);
        proper.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        proper.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> productor = new KafkaProducer<>(proper);

        //todo no activar encriptacion si encriptar esta a false
        String credencialesEncriptadas = "";

        System.out.println("encriptadooooooooo " + activarEncriptacion);

        if(activarEncriptacion == false){
            credencialesEncriptadas = credenciales;
        }else{
            System.out.println("teeeee");
            credencialesEncriptadas = encriptar(credenciales);
        }

        //System.out.println("Le paso credenciales: " + credenciales);
        System.out.println("envio: " + credencialesEncriptadas);
        if(credencialesEncriptadas.contains("1,1")){
            System.out.println("No esta activado el flag de encriptado, ACTIVARLO");
        }

        productor.send(new ProducerRecord<String, String>("topiccredenciales", "keyA", credencialesEncriptadas));

        //productor.send(new ProducerRecord<String, String>("topiccredenciales", "keyA", credenciales));



        //System.out.println("yeeeeeee");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(consumir().contains("no encriptado")){
            System.out.println("eeeee");
            acceso = consumir();
            System.out.println(acceso);
            mapaNova = "";
            while(activarEncriptacion == false){
                System.out.println("espero encriptacion");
            }
            if(activarEncriptacion  == true){
                credencialesEncriptadas = encriptar(credenciales);
                productor.send(new ProducerRecord<String, String>("topiccredenciales", "keyA", credencialesEncriptadas));
            }
            acceso = consumir();
        }else {
            if (id.equals("")) {
                System.out.println("me meto aqui");
                acceso = consumir();
            } else if (servidorCaido == true) {
                acceso = consumir();
            }
        }
        productor.flush();
        productor.close();

        //en acceso tengo lo siguiente: J1:mapaAtrac:usuario:tempCiudades
        System.out.println(acceso);

        return acceso;
    }

    //FUNCIONA 3/11/21
    //una vez entro al parque me empiezo a mover
    public static void elegirDestino(String mapa) {

        Boolean encuentro = false;
        int elegir = 0, longitud = 0, tiempo = 0;
        String[] mirar, xey;

        //aqui tengo que rellenar las propiedades de destino
        //mapa = 50=5,5, 40=3,3, 15=7,14;

        String[] elMapa = mapa.split(", ");
        //System.out.println(elMapa[0].toString());
        longitud = elMapa.length;

        while (encuentro == false) {
            elegir = (int) (Math.random() * longitud);

            mirar = elMapa[elegir].split("=");
            //mirar[0] = 60
            //mirar[1] = 5,5
            //System.out.println("tiempo: " + mirar[0].toString());
            boolean isNumeric =  mirar[0].matches("[+-]?\\d*(\\.\\d+)?");
            if(isNumeric) {
                tiempo = Integer.parseInt(mirar[0]);
                if (tiempo <= 60 && isNumeric) {
                    //elegimos esa atracción
                    encuentro = true;
                    //y añadimos el destino a nuesta variable destino
                    destino = mirar[1];
                    xey = mirar[1].split(",");
                    xd = xey[0];
                    yd = xey[1];
                }
            }
        }

        //System.out.println(" Mi destino es: X " + xd + " Y " + yd);
    }

    //FUNCIONA 3/11/21
    //todo
    //todo
    //todo
    //todo practica 2 annyadir colores
    public static String comprobarCasilla(String j, String i, String mapa) {

        //System.out.print("Estoy en el metodo comprobarCasilla ");

        String casilla = "  · ";
        String[] informacion = mapa.split(", ");
        String[] datos;
        String[] localizar;
        boolean encuentro = false;
        int i2 = 0;

        while (encuentro == false && i2 < informacion.length) {
            //ejemplo 60=5,5
            //datos[0]=60
            //datos[1]=5,5
            //localizar[0]=5
            //localizar[1]=5
            datos = informacion[i2].split("=");
            localizar = datos[1].split(",");
            if (i.equals(localizar[0]) && j.equals(localizar[1])) {
                casilla = "";
                casilla += "  ";
                if (datos[0].matches("[+-]?\\d*(\\.\\d+)?") == true && Integer.parseInt(datos[0]) < 10) {
                    casilla += datos[0] + " ";
                } else {
                    casilla += datos[0];
                }
                casilla += "";
                encuentro = true;
            }
            i2++;
        }
        //recorrer el mapa para saber si hay algo en esa casilla y mostrarlo y sino mostrar punto
        return casilla;
    }


    public static void moverse(String mapa, String temperaturas){

        //temperaturas = Alicante=10

        String elMapa = "";
        String todoMapa = "";
        String casilla = "", mapaEntero = "";


        //modificado jueves
        if(xd.equals("") == false) {

            elegirDestino(mapa);

            if (elMapa.equals("")) {
                elegirDestino(mapa);
            } else {
                String[] parto = elMapa.split(":");
                String limpio = parto[0].substring(1, parto[0].length() - 1);
                elegirDestino(limpio);
            }
        }
        //una vez elegido el destino, tengo que calcular el recorrido
        //y le voy pasando a engin mi posicion y mi destino todo el rato

        boolean logOut = false;
        int control = 0;
        if(servidorCaido == true){
            control = 1;
            servidorCaido = false;
        }else{
            control = 0;
        }

        while (logOut == false) {

            if (control == 1) {
                //enviamos a engine a traves del  primer productor id para inndicarle que volvemos a conectarnos a el
                //cuando reciba el id y compruebe q ya estabamos desde antes en el parque
                //solo activará el consumidor y productor de vis
                productorCredenciales(id, "nada", "1,1");
                //lo importante es pasarle datosusu ya que es el usuario
                //y es lo q necesita engine para saber si esta en el parque o no
            }

            //envia id:posicion:destino
            productorPosicion();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            todoMapa = consumidorMapa();
            //recibo mapa:mapaJugadoresPosicion:mapaJugadoresDestino:jugadores(id=usuario, para mostrar todos los visitantes junto con su nombre):tempCiudades

            String[] dividir = todoMapa.split(":");

            elMapa = dividir[0] + ":" + dividir[1] + ":" + dividir[2];
            String idNombre = dividir[3];
            String ciudades = dividir[4];
            String wait = dividir[5];
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            movimientoo();
            cambiarPosicion(elMapa);
            mostrarMapa(elMapa, idNombre, ciudades, wait);

            if (xp.equals(xd) && yp.equals(yd)) {
                System.out.println("He llegado a mi destino");
                System.out.print(elMapa);

                if (elMapa.equals("")) {
                    elegirDestino(mapa);
                } else {
                    String[] parto = elMapa.split(":");
                    String limpio = parto[0].substring(1, parto[0].length() - 1);
                    elegirDestino(limpio);
                }
            }
            control = 1;
        }
    }

    //FUNCIONA 3/11/21
    //CAMBIAR POR SWITCH???
    public static void movimiento() {

        //System.out.println("me muevo");

        int xDis = 0, yDis = 0, xHorizontal = 0, yHorizontal = 0, xVertical = 0, yVertical = 0;

        //si no hago esférico
        xDis = Math.abs(Integer.parseInt(xp) - Integer.parseInt(xd));
        yDis = Math.abs(Integer.parseInt(yp) - Integer.parseInt(yd));

        //si hago movimiento esferico horizontal
        xHorizontal = Math.abs((Integer.parseInt(xp)-1) + (20 -  Integer.parseInt(xd)));
        yHorizontal = Math.abs(Integer.parseInt(yp) - Integer.parseInt(yd));

        //si hago movimiento esferico vertical
        xVertical = Math.abs(Integer.parseInt(xp) - Integer.parseInt(xd));
        yVertical = Math.abs((Integer.parseInt(yp)-1) + (20 -  Integer.parseInt(yd)));

        System.out.println("recorrido: " + String.valueOf(xDis) + " " + String.valueOf(yDis));
        System.out.println("la vuelta horizontal: " + String.valueOf(xHorizontal) + " " + String.valueOf(yHorizontal));
        System.out.println("la vuelta vertical: " + String.valueOf(xVertical) + " " + String.valueOf(yVertical));

        Math.min(xDis+yDis, xHorizontal+yHorizontal);



        int xP, yP, xD, yD;
        xP = Integer.parseInt(xp);
        yP = Integer.parseInt(yp);
        xD = Integer.parseInt(xd);
        yD = Integer.parseInt(yd);

        if (xD > xP && yD > yP) {
            xP++;
            yP++;
            xp = String.valueOf(xP);
            yp = String.valueOf(yP);
        } else if (xD > xP && yD == yP) {
            xP++;
            xp = String.valueOf(xP);
        } else if (xD == xP && yD > yP) {
            yP++;
            yp = String.valueOf(yP);
        } else if (xD < xP && yD < yP) {
            xP--;
            yP--;
            xp = String.valueOf(xP);
            yp = String.valueOf(yP);
        } else if (xD < xP && yD == yP) {
            xP--;
            xp = String.valueOf(xP);
        } else if (xD == xP && yD < yP) {
            yP--;
            yp = String.valueOf(yP);
        } else if (xD < xP && yD > yP) {
            xP--;
            yP++;
            xp = String.valueOf(xP);
            yp = String.valueOf(yP);
        } else if (xD > xP && yD < yP) {
            xP++;
            yP--;
            xp = String.valueOf(xP);
            yp = String.valueOf(yP);
        } else {
            System.out.println("no hago nada");
        }
        //System.out.println("Posicion siguiente: X=" + xp + " Y=" + yp);
    }

    public static void movimientoo() {

        //System.out.println("me muevo");

        int xP, yP, xD, yD;
        xP = Integer.parseInt(xp);
        yP = Integer.parseInt(yp);
        xD = Integer.parseInt(xd);
        yD = Integer.parseInt(yd);

        boolean BiggerTwoD = ((xD-xP) + (yD-yP)) >= ((((xP-1)+(20-xD))) + ((yP-1)+(20-yD)));
        boolean BiggerTwoP = ((xP-xD) + (yP-yD)) >= (((20-xP)+(xD-1)) + (((20-yP)+(yD-1))));
        boolean BiggerxPyD = ((xP-xD) + (yD-yP)) >= ((((20-xP)+(xD-1))) + ((yP-1)+(20-yD)));
        boolean BiggerxDyP = ((xD-xP) + (yP-yD)) >= ((((xP-1)+(20-xD))) + ((20-yP)+(yD-1)));

        boolean xDBigger = ((xD-xP)>((xP-1)+(20-xD)));
        boolean yDBigger = ((yD-yP)>((yP-1)+(20-yD)));
        boolean xPBigger = (((20-xP)+(xD-1))<(xP-xD));
        boolean yPBigger = (((20-yP)+(yD-1))<(yP-yD));

        if((xD > xP) && (yD > yP)){
            if(BiggerTwoD) {
                if(xP == 1 && yP == 1) {
                    xP = 20;
                    yP = 20;
                } else if(xP == 1) {
                    xP = 20;
                    yP = 21 - yP;
                } else if(yP == 1) {
                    xP = 21 - xP;
                    yP = 20;
                }
                else {
                    xP--;
                    yP--;
                }
            } else {
                xP++;
                yP++;
            }

            xp = String.valueOf(xP);
            yp = String.valueOf(yP);
        }else if(xD > xP && yD == yP){
            if(xDBigger) {
                if(xP == 1) {
                    xP = 20;
                } else {
                    xP--;
                }
            } else {
                xP++;
            }

            xp = String.valueOf(xP);
        }else if(xD == xP && yD > yP){
            if(yDBigger) {
                if(yP == 1) {
                    yP = 20;
                } else {
                    yP --;
                }
            } else {
                yP++;
            }

            yp = String.valueOf(yP);
        }else if(xD < xP && yD < yP){
            if(BiggerTwoP) {
                if(xP == 20 && yP == 20) {
                    xP = 1;
                    yP = 1;
                } else if(xP == 20) {
                    xP = 1;
                    yP = 21 - yP;
                } else if(yP == 20) {
                    xP = 21 - xP;
                    yP = 1;
                } else {
                    xP++;
                    yP++;
                }
            } else {
                xP--;
                yP--;
            }

            xp = String.valueOf(xP);
            yp = String.valueOf(yP);
        }else if(xD < xP && yD == yP){
            if(xPBigger) {
                if(xP == 20) {
                    xP = 1;
                } else {
                    xP++;
                }
            } else {
                xP--;
            }
            xp = String.valueOf(xP);
        }else if(xD == xP && yD < yP){
            if(yPBigger) {
                if(yP == 20) {
                    yP = 1;
                } else {
                    yP++;
                }
            } else {
                yP--;
            }

            yp = String.valueOf(yP);
        }else if(xD < xP && yD > yP){
            if(BiggerxPyD) {
                if(xP == 20 && yP == 1) {
                    xP = 1;
                    yP = 20;
                } else if(xP == 20) {
                    xP = 1;
                    yP = 21 - yP;
                } else if(yP == 1) {
                    xP = 21 - xP;
                    yP = 20;
                } else {
                    xP++;
                    yP--;
                }
            } else {
                xP--;
                yP++;
            }

            xp = String.valueOf(xP);
            yp = String.valueOf(yP);
        }else if(xD > xP && yD < yP){
            if(BiggerxDyP) {
                if(xP == 1 && yP == 20) {
                    xP = 20;
                    yP = 1;
                } else if(xP == 1) {
                    xP = 20;
                    yP = 21 - yP;
                } else if(yP == 20) {
                    xP = 21 - xP;
                    yP = 1;
                } else {
                    xP--;
                    yP++;
                }
            } else {
                xP++;
                yP--;
            }

            xp = String.valueOf(xP);
            yp = String.valueOf(yP);
        }else{
            System.out.println("no hago nada");
        }
        System.out.println(xP + "::::" + yP);
        //me flata menor y mayor

        //System.out.println("Posicion siguiente: X=" + xp + " Y=" + yp);
    }

    //hay que comprobarlo en cada movimiento
    //todo practica 2
    public static void cambiarPosicion(String elMapa) {

        //System.out.println("Estoy en el metodo cambiarPosicion ");

        //en elMapa tengo lo siguiente: mapa:mapaJugadores
        String[] partir = elMapa.split(":");
        boolean noNumerico = false;

        //igual tengo que quitar { y } AL MAPA
        String limpiar = partir[0].substring(1, partir[0].length() - 1);
        String[] comprobar = limpiar.split(", ");
        String[] numeros, numero;
        int mins = 0;
        boolean encontrado = false;

        for (int i = 0; i < comprobar.length && encontrado == false; i++) {
            numeros = comprobar[i].split("=");
            //numeros[0]
            //numeros[1] = 5,5
            numero = numeros[1].split(",");
            //SI EL DESTINO ES IGUAL A LA POSICION DE UNO DE LAS POSICIONES DEL MAPA
            //SE COMPRUEBA SI ESA ATRACCIÓN HA CAMBIADO DE TIEMPO A MAS DE 60
            //todo practica 2
            boolean isNumeric =  numeros[0].matches("[+-]?\\d*(\\.\\d+)?");
            if(isNumeric) {
                if (xd.equals(numero[0]) && yd.equals(numero[1])) {
                    mins = Integer.parseInt(numeros[0]);
                    encontrado = true;
                }
            }else{
                elegirDestino(limpiar);
                noNumerico = true;
            }
        }

        if (mins > 60 && !noNumerico) {
            elegirDestino(limpiar);
        }

    }

    public static void productorPosicion() {

        Properties proper = new Properties();

        proper.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, gestorColas);
        proper.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        proper.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> productor = new KafkaProducer<>(proper);

        String informacionPosicion = "";
        String posi = xp + "," + yp;

        if(ini.getSalir() == true){
            //le envio id:logout
            informacionPosicion = id + ":" + "out";
        }else{
            informacionPosicion = id + ":" + posi + ":" + destino;
        }


        //System.out.println("yo le paso por topicmapa esto: " + informacionPosicion);

        String informacionPosicionEncriptadas = encriptar(informacionPosicion);

        productor.send(new ProducerRecord<String, String>("topicmapa", "keyA", informacionPosicionEncriptadas));

        //productor.send(new ProducerRecord<String, String>("topicmapa", "keyA", informacionPosicion));

        productor.flush();
        productor.close();

        if(ini.getSalir() == true){
            System.exit(0);
        }

        //System.out.println("funcionaaaaa");

    }

    //consumidor que va recibiendo el mapa para mostrarlo
    //a través del topic devolverMapa
    public static String consumidorMapa() {

        //System.out.println("me meto en consumidorMapa");

        Properties proper = new Properties();

        String datos = "";
        String id = "", nuevoTiempo = "";
        String[] informacion;

        proper.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, gestorColas);
        proper.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        proper.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        proper.put(ConsumerConfig.GROUP_ID_CONFIG, "sensorGroup");
        proper.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(proper);
        consumer.subscribe(Collections.singleton("devolvermapa"));

        String elMapa = "";
        String elMapaEncriptado = "";
        boolean tenerDestino = false;
        int mirar = 0;
        while (elMapa.equals("") && mirar < 10) {

            //System.out.println("holaaaaaaa");
            try {
                ConsumerRecords<String, String> records = consumer.poll(100);

                //recibo mapa:mapaJugadoresPos:mapaJugadoresDes:jugadores(id=usuario)
                for (ConsumerRecord<String, String> record : records) {
                    elMapa = record.value().toString();
                    System.out.println(elMapa);
                    elMapaEncriptado = desencriptar(elMapa);
                }

                //String cons = consumir();

                if(elMapaEncriptado.equals("") && xp.equals("1") == false && destino.equals("") == false ){
                    mirar++;
                }
            } finally {

                if (!elMapaEncriptado.equals("")) {
                    consumer.close();
                    return elMapaEncriptado;
                }
            }
        }
        System.out.println(mirar);
        if(mirar == 10){
            System.out.println("Servidor caido.... Esperando reconexion");

            servidorCaido = true;
            recibirDatos(aliasUsu, contras);

            //podriamos enviar un mapa inventado ko:00
            //para que se de cuenta y vuelva a mandar credenciales

            //elMapa = "ko:00";
        }
        //todo
        System.out.println("elmapaencriptado: " + elMapaEncriptado);
        return elMapaEncriptado;
    }

    //todo tengo que anyadir el mapa de ciudades cerradas
    public static void mostrarMapa(String mapaEntero, String idNombre, String ciudades, String wait) {

        //System.out.println("estoy en mostrarMapa " + mapaEntero.toString());
        String casilla = "";

        System.out.println("idNombre: " + idNombre);


        String[] limpiar = mapaEntero.split(":");
        String mapaLimpio = limpiar[0].toString().substring(1, limpiar[0].toString().length() - 1) + ", " + limpiar[1].toString().substring(1, limpiar[1].toString().length() - 1);

        String posiciones = limpiar[1].toString().substring(1, limpiar[1].toString().length() - 1) + ", ";
        String destinos = limpiar[2].toString().substring(1, limpiar[2].toString().length() - 1) + ", ";

        String[] posicion = posiciones.split(", ");
        String[] destino = destinos.split(", ");

        //System.out.println("mapa limpio en mostrarmapa: " + mapaLimpio);

        System.out.println();
        System.out.println("    ** Fun with queues PortAventura **");
        System.out.println("    ID      Nombre      Pos     Destino");

        for (int k = 0; k < posicion.length; k++) {
            //posicion[0] = J1=8,13
            //destino[0] = J1=8,14
            String[] parto1 = posicion[k].split("=");
            String[] parto2 = new String[0];
            if(posicion.length <= destino.length) {
                parto2 = destino[k].split("=");
            }

            String nombre = "";

            String idNombreLimpio = idNombre.substring(1, idNombre.length() - 1);
            String[] nom = idNombreLimpio.split(", ");
            for (int y = 0; y < nom.length; y++) {
                String[] dividir = nom[y].split("=");

                if (parto1[0].equals(dividir[0])) {
                    nombre = dividir[1];
                }
            }

            if(posicion.length <= destino.length) {
                System.out.println("    " + parto1[0] + " #   " + nombre + " #       " + parto1[1] + "#     " + parto2[1]);
            }else{
                System.out.println("    " + parto1[0] + " #   " + nombre + " #       " + parto1[1] + "#     " + " ");
            }
        }

        //todo mejorar  (anyadir coordenadas, (1-10, 1-10)...)
        //todo practica2
        //hago for para mostrar las ciudades cerradas
        //en engine tengo q añadir que sea A=temperatura:Alicante
        //asi muestro Atracciones de la ciudad de Alicante cerradas (primer cuadrante...)
        //recibo de engie ==> A=Alicante:14.3
        String limpiarCiudades = ciudades.substring(1, ciudades.length()-1);
        String[] dividir = limpiarCiudades.split(", ");
        int ix = 1, iy = 1;

        System.out.println("Ciudades del mapa");
        for(int ciu = 0;ciu < dividir.length;ciu++){
            //String[] parto = dividir[ciu].split("=");
            System.out.println(dividir[ciu]);
        }

        System.out.print("  ");
        for (int i2 = 0; i2 < 20; i2++) {
            if (i2 + 1 > 10) {
                System.out.print("  ");
                System.out.print(i2 + 1 + "");
            } else {
                System.out.print("   ");
                System.out.print(i2 + 1 + "");
            }
        }
        System.out.println();
        for (int i = 1; i <= 20; i++) {
            if (i < 10) {
                System.out.print(i + "  ");
            } else {
                System.out.print(i + " ");
            }
            for (int j = 1; j <= 20; j++) {
                //ver como pasar el mapa de atracciones y jugadores para cuando un jugador este en una atracción
                casilla = comprobarCasilla(String.valueOf(i), String.valueOf(j), mapaLimpio);

                //todo practica 2
                String ansiVerde = "\u001B[32m";
                String ansiAzul = "\u001B[34m";
                String ansiReset = "\u001B[0m";

                int laX = 0;
                if(i <= 10){
                    laX = 1;
                }
                switch (laX){
                    case 0:
                        if(j <= 10){
                            System.out.print(ansiVerde + casilla + ansiReset);
                        }else{
                            System.out.print(ansiAzul + casilla + ansiReset);
                        }
                        break;
                    case 1:
                        if(j <= 10){
                            System.out.print(ansiAzul + casilla + ansiReset);
                        }else{
                            System.out.print(ansiVerde + casilla + ansiReset);
                        }
                        break;
                    default:
                        break;
                }
                //System.out.print(casilla);
                //System.out.print("  * ");
            }
            System.out.println();
        }

        try {
            //modificado jueves
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();

        } catch (Exception e) {
        }

        if(wait.equals("ko")){
            System.out.println("Servidor de tiempos de espera caido, no se estan actualizando los tiempos de espera");
        }


    }


    //todo
    //todo
    //todo cambiar por Api
    //esta es la parte que se comunica con registry
    //faltaria revisar el modificar el usuario
    public static Boolean registrar(String alias, String usuario, String contrasenya) {

        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;

        String linea;
        String laUrl = "";

        try {
            archivo = new File("C:\\datosVisitor\\datosRegistry.txt");
            fr = new FileReader(archivo);
            br = new BufferedReader(fr);

            while((linea = br.readLine()) != null){
                laUrl = linea;
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        System.out.println(laUrl);

        aliasUsu = alias;
        usu = usuario;
        contras = contrasenya;

        boolean correcto = false;
        //respuesta = client.execute(post);
        //responseString = new BasicResponseHandler().handleResponse(respuesta);
        //System.out.println(responseString);

        //todo
        //HttpPost post = new HttpPost(laUrl);
        //HttpResponse respuesta = null;
        //String responseString = null;


        try {
            var values = new HashMap<String, String>(){{
                put("alias", alias);
                put("nombre", usuario);
                put("contrasenya", contrasenya);
                InetAddress adress = InetAddress.getLocalHost();
                ipVisitante = adress.getHostAddress();
                put("ip", ipVisitante);
            }};

            var objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(values);
            System.out.println(requestBody);

            System.out.println(laUrl);



            HttpClient client = HttpClient.newHttpClient();
            System.out.println(HttpRequest.BodyPublishers.ofString(requestBody).toString());

            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            try {
                // Install the all-trusting trust manager
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            }catch(NoSuchAlgorithmException e){

            }catch(KeyManagementException e2){

            }

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);


            URL myUrl = new URL(laUrl);
            System.out.println("URL: " + myUrl);
            HttpsURLConnection conn = (HttpsURLConnection)myUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setDoOutput(true);

            DataOutputStream output = new DataOutputStream(conn.getOutputStream());

            output.writeBytes(requestBody);
            output.flush();
            output.close();

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while((inputLine = in.readLine()) != null){
                response.append(inputLine);
            }

            in.close();

            /*
            HttpRequest request = HttpRequest.newBuilder()
                    .header("Content-Type", "application/json")
                    .uri(URI.create(laUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();



            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(response.body());

             */

        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        } catch (Exception e) {
            System.out.println("no funciona 2");
        }

        return correcto;
    }


    public static boolean modificarUsuario(String alias, String usuario, String contrasenya){

        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;
        String linea;
        String laUrl = "";

        try {
            archivo = new File("C:\\datosVisitor\\datosRegistry.txt");
            fr = new FileReader(archivo);
            br = new BufferedReader(fr);

            while((linea = br.readLine()) != null){
                laUrl = linea;
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        System.out.println(laUrl);

        laUrl += alias;

        aliasUsu = alias;
        usu = usuario;
        contras = contrasenya;

        boolean correcto = false;
        //respuesta = client.execute(post);
        //responseString = new BasicResponseHandler().handleResponse(respuesta);
        //System.out.println(responseString);

        //todo
        //HttpPost post = new HttpPost(laUrl);
        //HttpResponse respuesta = null;
        //String responseString = null;


        try {
            var values = new HashMap<String, String>(){{
                put("alias", alias);
                if(usuario != "") {
                    put("nombre", usuario);
                }
                if(contrasenya != "") {
                    put("contrasenya", contrasenya);
                }
                InetAddress adress = InetAddress.getLocalHost();
                ipVisitante = adress.getHostAddress();
                put("ip", ipVisitante);
            }};

            var objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(values);
            System.out.println(requestBody);

            System.out.println(laUrl);

            HttpClient client = HttpClient.newHttpClient();
            System.out.println(HttpRequest.BodyPublishers.ofString(requestBody).toString());
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };
            try {
                // Install the all-trusting trust manager
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            }catch(NoSuchAlgorithmException e){

            }catch(KeyManagementException e2){

            }
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);


            URL myUrl = new URL(laUrl);
            System.out.println("URL: " + myUrl);
            HttpsURLConnection conn = (HttpsURLConnection)myUrl.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setDoOutput(true);

            DataOutputStream output = new DataOutputStream(conn.getOutputStream());

            output.writeBytes(requestBody);
            output.flush();
            output.close();
            int responseCode = conn.getResponseCode();
            System.out.println("responseCode: " + responseCode);
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while((inputLine = in.readLine()) != null){
                response.append(inputLine);
            }
            in.close();
            /*
            HttpRequest request = HttpRequest.newBuilder()
                    .header("Content-Type", "application/json")
                    .uri(URI.create(laUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
             */

        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        } catch (Exception e) {
            System.out.println("no funciona 2");
        }

        return correcto;

    }



    //todo borrar estos 2 metodos
    public static void escribeSocket(Socket p_sk, String alias, String usu, String contra) {

        String datos = "";

        datos += alias;
        datos += ", ";
        datos += usu;
        datos += ", ";
        datos += contra;

        System.out.println(datos);

        try {
            OutputStream aux = p_sk.getOutputStream();
            DataOutputStream flujo = new DataOutputStream(aux);
            flujo.writeUTF(datos);

            /*
            HashMap<String, String> mapa = new HashMap<>();

            mapa.put("shambala", "10000");
            mapa.put("yeeey", "34");
            mapa.put("fnbibwf", "334");

            flujo.writeUTF(mapa.toString());
            */


        } catch (Exception e) {

        }

        return;

    }

    public static String leeSocket(Socket p_sk, String datos) {


        try {
            InputStream aux = p_sk.getInputStream();
            DataInputStream flujo = new DataInputStream(aux);
            datos = flujo.readUTF();
        } catch (Exception e) {

        }

        return datos;
    }

    public static void activarEncriptacion(Boolean activar){
        activarEncriptacion = activar;

        System.out.println("por aquuuuuuuui " + activarEncriptacion);
    }
}


class logOutApi extends JFrame {

    private JButton log;
    private JButton prueba;

    private boolean salir = false;

    public logOutApi() {
        setTitle("Registro del parquee");
        setResizable(false);
        setSize(390, 220);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        log = new JButton("Logout");
        prueba = new JButton("fueraaa");

        add(log);
        add(prueba);

        log.reshape(20, 75, 120, 30);
        prueba.reshape(20, 75, 120, 30);

        log.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salir(e);
            }
        });

        prueba.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salir(e);
            }
        });
    }

    private boolean salir(ActionEvent e ) {
        salir = true;

        return salir;
    }

    public boolean getSalir(){return salir;}


}

class inicioApi extends  JFrame{

    private JButton iniciar;
    private JButton registrar;
    private JButton modificar;

    private JButton entrar;
    private JButton registrarse;
    private JButton modificarse;

    private JLabel alias;
    private JLabel nombre;
    private JLabel contra;

    private JTextField txtAlias;
    private JTextField txtNombre;
    private JPasswordField txtContra;

    private JCheckBox encriptar;

    private JButton out;
    private boolean salir = false;

    public inicioApi(){
        setTitle("Bienvenido al parque PortAventura");
        setResizable(false);
        setSize(390, 220);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        iniciar = new JButton("Entrar");
        registrar = new JButton("Registrar");
        modificar = new JButton("Modificar");

        add(iniciar);
        add(registrar);
        add(modificar);

        iniciar.reshape(20, 10, 100, 30);
        registrar.reshape(130, 10, 100, 30);
        modificar.reshape(240, 10, 100, 30);



        iniciar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                entrarParque(e);
            }
        });

        registrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarParque(e);
            }
        });

        modificar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modificarUsuario(e);
            }
        });

    }

    public void setOut(){

        out.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salir(e);
            }
        });
    }

    private void salir(ActionEvent e){
        salir = true;

    }

    public boolean getSalir(){return salir;}


    private void entrarParque(ActionEvent e){

        if(alias != null) {
            alias.setVisible(false);
            txtAlias.setVisible(false);
            nombre.setVisible(false);
            txtNombre.setVisible(false);
            contra.setVisible(false);
            txtContra.setVisible(false);
            if(registrar != null) {
                registrarse.setVisible(false);
            }
        }

        if(modificarse != null){
            modificarse.setVisible(false);
        }

        alias = new JLabel("Alias: ");
        txtAlias = new JTextField(10);
        contra = new JLabel("Contrasenya: ");
        txtContra = new JPasswordField(10);
        entrar = new JButton("ENTRAR");
        out = new JButton("LogOut");
        encriptar = new JCheckBox("Encriptar");

        add(alias);
        add(txtAlias);
        add(contra);
        add(txtContra);
        add(entrar);
        add(out);
        add(encriptar);


        alias.reshape(20, 55, 100, 20);
        txtAlias.reshape(120, 55, 100, 20);

        contra.reshape(20, 75, 100, 20);
        txtContra.reshape(120, 75, 100, 20);


        entrar.reshape(20, 110, 100, 20);
        out.reshape(160, 110, 100, 20);

        encriptar.reshape(240, 55, 150, 20);

        out.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salir(e);
            }
        });

        entrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                entro(e);
            }
        });

        encriptar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                encripto(e);
            }
        });
    }

    private void encripto(ActionEvent e){
        Runnable miRunnable = new Runnable() {
            @Override
            public void run() {
                FWQ_VisitorApi probando = new FWQ_VisitorApi();

                System.out.println("me meto por aqui");

                if(encriptar.isSelected()){
                    System.out.println("he seleccionado");
                    probando.activarEncriptacion(true);
                }else {
                    probando.activarEncriptacion(false);
                }
            }
        };
        Thread hilo = new Thread(miRunnable);
        hilo.start();
    }

    private void entro(ActionEvent e){

        Runnable miRunnable = new Runnable() {
            @Override
            public void run() {
                entrar.setVisible(false);

                FWQ_VisitorApi probando = new FWQ_VisitorApi();

                System.out.println(out.isEnabled());
                out.setVisible(true);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException t) {
                    t.printStackTrace();
                }

                if(txtAlias.getText() != "" && txtContra.getText() != "" && out.isEnabled()) {
                    //probando.recibirDatos(txtAlias.getText(), txtContra.getText());
                    String hasContra = doHashing(txtContra.getText());
                    probando.recibirDatos(txtAlias.getText(), hasContra);
                }

            }
        };
        Thread hilo = new Thread(miRunnable);
        hilo.start();


    }

    private static String doHashing(String contra){
        String hash = "";

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            messageDigest.update(contra.getBytes());
            byte[] resultByteArray = messageDigest.digest();

            StringBuilder sb = new StringBuilder();

            for(byte b : resultByteArray){
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }

        System.out.println("el hash: " + hash);

        return hash;
    }

    private void registrarParque(ActionEvent e){

        if(alias != null) {
            alias.setVisible(false);
            txtAlias.setVisible(false);
            contra.setVisible(false);
            txtContra.setVisible(false);
            entrar.setVisible(false);
            out.setVisible(false);
        }

        if(nombre != null){
            nombre.setVisible(false);
            txtNombre.setVisible(false);
            modificarse.setVisible(false);
        }


        alias = new JLabel("Alias: ");
        txtAlias = new JTextField(10);
        nombre = new JLabel("Nombre: ");
        txtNombre = new JTextField(10);
        contra = new JLabel("Contrasenya: ");
        txtContra = new JPasswordField(10);

        add(alias);
        add(txtAlias);
        add(nombre);
        add(txtNombre);
        add(contra);
        add(txtContra);

        alias.reshape(20, 55, 100, 20);
        txtAlias.reshape(120, 55, 100, 20);

        nombre.reshape(20, 75, 100, 20);
        txtNombre.reshape(120, 75, 100, 20);

        contra.reshape(20, 95, 100, 20);
        txtContra.reshape(120, 95, 100, 20);


        registrarse = new JButton("REGISTRAR");
        add(registrarse);
        registrarse.reshape(20, 130, 100, 20);

        registrarse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registro(e);
            }
        });
    }

    private void registro(ActionEvent e){
        FWQ_VisitorApi probando = new FWQ_VisitorApi();

        if(txtAlias.getText() != "" && txtContra.getText() != "") {
            //probando.registrar(txtAlias.getText(), txtNombre.getText(), txtContra.getText());
            String hasContra = doHashing(txtContra.getText());
            probando.registrar(txtAlias.getText(), txtNombre.getText(), txtContra.getText());
        }
    }

    private void modificarUsuario(ActionEvent e) {

        if (alias != null) {
            alias.setVisible(false);
            txtAlias.setVisible(false);
            contra.setVisible(false);
            txtContra.setVisible(false);
            entrar.setVisible(false);
            out.setVisible(false);
        }

        if (nombre != null && registrarse != null) {
            nombre.setVisible(false);
            txtNombre.setVisible(false);
            registrarse.setVisible(false);
        }


        alias = new JLabel("Alias: ");
        txtAlias = new JTextField(10);
        nombre = new JLabel("Nuevo nombre: ");
        txtNombre = new JTextField(10);
        contra = new JLabel("Nueva contrasenya: ");
        txtContra = new JPasswordField(10);

        add(alias);
        add(txtAlias);
        add(nombre);
        add(txtNombre);
        add(contra);
        add(txtContra);

        modificarse = new JButton("Modificar");
        add(modificarse);
        modificarse.reshape(20, 130, 100, 20);


        alias.reshape(20, 55, 120, 20);
        txtAlias.reshape(140, 55, 100, 20);

        nombre.reshape(20, 75, 120, 20);
        txtNombre.reshape(140, 75, 100, 20);

        contra.reshape(20, 95, 120, 20);
        txtContra.reshape(140, 95, 100, 20);

        modificarse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modificar(e);
            }
        });
    }

    private void modificar(ActionEvent e){
        FWQ_VisitorApi probando = new FWQ_VisitorApi();

        if(txtAlias.getText() != "" ) {
            if(txtNombre.getText() != "" && txtContra.getText() != ""){
                probando.modificarUsuario(txtAlias.getText(), txtNombre.getText(),txtContra.getText());
            }else{
                if(txtNombre.getText() != ""){
                    probando.modificarUsuario(txtAlias.getText(), txtNombre.getText(),"");
                }else{
                    if(txtContra.getText() != ""){
                        probando.modificarUsuario(txtAlias.getText(), "",txtContra.getText());
                    }
                }
            }
            //probando.registrar(txtAlias.getText(), txtNombre.getText(), txtContra.getText());
        }
    }
}
