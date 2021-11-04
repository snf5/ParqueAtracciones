package test;

import Formularios.Inicio;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.time.Duration;
import java.util.*;

import Formularios.Registrar;
import com.mongodb.MongoClient;
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


public class FWQ_Visitor {

    //
    private static String usu = "";
    private static String contra = "";
    private static String aliasUsu = "";

    //datos para ir pasandole a engine
    private static String id = "";
    private static String destino = "";
    private static String posicion = "";
    private  static String xd = "";
    private  static String yd = "";
    private  static String xp = "";
    private  static String yp = "";

    private static HashMap<String, String> primerJugadores= new HashMap<>();

    private static String mapa2 = "", mapa3 = "", todo = "";

    //datos para la conexión de los puertos e ips
    private static String hostRegistry, puertoRegistry, hostKafka, puertoKafka, gestorColas;
    private static String datosUsu = "";
    private static Inicio login;
    private static Registrar miRegistrar;

    public static void main(String args[]){

        xp = "1";
        yp = "1";

        //aqui recibo IP y puerto de FWQ_Registry
        //e IP y puerto de kafka

        //args[0] IP de registry
        //args[1] puerto de registry

        //args[2] IP de kafka
        //args[3] puerto de kafka

        hostRegistry = args[0];
        puertoRegistry = args[1];

        hostKafka = args[2];
        puertoKafka = args[3];

        gestorColas = hostKafka + ":" + puertoKafka;

        login = new Inicio();
        login.setVisible(true);

        //comprobar que el registro se hizo correctamente
        //para ello el registry me debe devolver por socket
        //una confirmación

        /*
        topic:
        - credenciales
        - acceso
        - posicion
        - devolver mapa
         */

        /*
        para realizar el inicio de sesión tengo que invocar al productor del topic entrarParque
        para mandar información a engine

        el consumidor que es engine devolverá un mensaje de si esta registrado o no y le devuelve un id de sesion

        si no esta registrado se iniciará la pagina de registro

        Productor para enviar datos de credenciales a engine
        Consumidor para recibir el mapa (en caso de que no este registrado, engine enviará mediante su productor un ko)
        Productor para ir madnando a engine sus movimientos y que los actualice en el mapa


         */

        HashMap<String, String> primerMapa = new HashMap<>();
        primerMapa.put("5", "5,5");
        primerMapa.put("59", "7,10");
        primerMapa.put("30", "8,12");
        primerMapa.put("45", "3,14");

        //mapa2 = primerMapa.toString().substring(1, primerMapa.toString().length()-1);
        mapa2 = primerMapa.toString();


        //id:posision

        primerJugadores.put("j1", "5,6");
        primerJugadores.put("j2", "7,11");
        primerJugadores.put("j3", "12,15");

        //mapa3 = primerJugadores.toString().substring(1, primerJugadores.toString().length()-1);
        mapa3 = primerJugadores.toString();

        //id:nombre=desstino
        HashMap<String, String> primerJugadores2= new HashMap<>();
        primerJugadores2.put("j1:Sergio", "15,5");
        primerJugadores2.put("j2:Paco", "7,16");
        primerJugadores2.put("j3:Vic", "12,19");
        String mapa4 = primerJugadores2.toString().substring(1, primerJugadores2.toString().length()-1);

        todo = mapa2 + ", " + mapa3;


        System.out.println("Mapa atracciones: " + mapa2.toString());


        //pruebas
        /*
        while(xp.equals(xd) == false && yp.equals(yd) == false || xp.equals(xd) == true && yp.equals(yd) == false || xp.equals(xd) == false && yp.equals(yd) == true){
            movimiento();
        }
        System.out.println("yeee");

         */

        /*
        System.out.println(comprobarCasilla("5", "5", mapa2));

        //REPASARRRRRRRR
        //hacer un mapaJugadoresPos id:pos y mapaJugadoresDes j1:nombre=destino??
        System.out.println("****** Fun with queues PortAventura ******");
        System.out.println("    ID      Nombre      Pos     Destino");
        String[] pos = mapa3.split(", ");
        String[] des = mapa4.split(", ");
        String[] parto, parto2, parto3;
        for(int i = 0; i < pos.length ;i++){
            //parto[0] = id parto[1] = posicion
            parto = pos[i].split("=");
            //parto2[0] = id:nombre parto2[1] = destino
            parto2 = des[i].split("=");
            parto3 = parto2[0].split(":");

            System.out.println("    " + parto[0] + " #    " + parto3[1] + " #   " + parto[1] + "  #" + parto2[1]);
        }
         */

    }


    //recibo y guardo los datos del usuario para
    //ya desde aqui ir llamando al resto de modulos para que se lleve a cabo todo
    public void recibirDatos(String usuario, String contra){

        String entradaParque = "";

        posicion = "1:1";
        xp = "1";
        xd = "1";

        datosUsu = usuario + ":" + contra;
        //en datosUsu ya tengo usuario:contra


        //le paso a productorCredenciales el usuario y contraseña y
        //luego tengo que llamar a consumir para recibir el OK!
        //entradaParque = id,mapaAtracciones o ko,0
        //todo
        //entradaParque = productorCredenciales(usuario, contra, posicion);
        entradaParque = "j1" + ":" + mapa2;


        //entradaParque va a recibir id:mapa entero Atracciones o ko:0
        //hacemos split para comprobar si ha podido entrar en el parque o no

        String informacion[] = entradaParque.split(":");

        //informacion[0] es id/ko
        //informacion[1] es mapaAtracciones/0
        if(informacion[0].equals("ko")){
            //no puede entrar al parque hasta o que se registre o meta bien los datos

        }else{
            //se puede entrar al parque y llamamos a prodcutor y consumidor
            //podriamods llamar a un metodo que se encargue de llamar todo el rato a productor y consumidor
            //aqui igualo id a informacion[0], para saber que id tiene cada jugador
            //j1, j2, j3...
            id = informacion[0];

            //le paso informacion[1], que es el mapa para que sepa donde esta y cual es su destino
            //OJOOOOO recibo un map de las posiciones ocupadas de las atracciones

            //quitar = 45=5,5, 24=7,10

            String quitar = informacion[1].toString().substring(1, informacion[1].toString().length()-1);
            //le paso la cadena tal que asi: 45=5,5, 24=7,10
            //y al hacer por ahi lo de [] me va sacando shamabal=12 ...
            //a moverse se le pasa el mapa limpio para elegir destino


            moverse(quitar);
        }


    }


    //los dos metodos de aqui abajo son para poder entrar al parque
    //consumidor que recibe por parte de engine la confirmación de acceso al parque
    public static String consumir(){

        Properties proper = new Properties();

        String mapa = "";

        //añadir el grupo de consumidores de solo visitantes
        proper.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, gestorColas);
        proper.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        proper.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        proper.put(ConsumerConfig.GROUP_ID_CONFIG, "visitanteGroup"); //otro grupo???
        proper.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(proper);
        consumer.subscribe(Collections.singleton("acceso"));

        ConsumerRecords<String, String> records = consumer.poll(100);

        for (ConsumerRecord<String, String> record : records) {
            //el productor me tiene que devolver
            //informacion = record.value().toString().split(":");
            mapa = record.value().toString();
        }

        //mapa = id:mapaAtracciones
        return mapa;

    }


    //productor que envia a traves de topiccredenciales
    //para que engine compruebe los datos del usuario
    //usuario:contra
    public static String productorCredenciales(String usuario, String contra, String posicion){

        boolean entrar = false;

        String acceso = "";

        Properties proper = new Properties();

        String credenciales = usuario + ":" + contra + ":" + posicion;

        proper.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,gestorColas);
        proper.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        proper.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> productor = new KafkaProducer<>(proper);

        productor.send(new ProducerRecord<String, String>("topiccredenciales", "keyA", credenciales));

        productor.flush();
        productor.close();

        System.out.println("yeeeeeee");

        acceso = consumir();

        //acceso = id:mapaAtracciones
        return acceso;
    }


    //FUNCIONA 3/11/21
    //una vez entro al parque me empiezo a mover
    public static void elegirDestino(String mapa){

        System.out.print("Estoy en el metodo elegirDestino ");

        Boolean encuentro = false;
        int elegir = 0, longitud = 0, tiempo = 0;
        String[] mirar, xey;

        //aqui tengo que rellenar las propiedades de destino
        //mapa = 50=5,5, 40=3,3, 15=7,14;

        String[] elMapa = mapa.split(", ");
        System.out.println(elMapa[0].toString());
        longitud = elMapa.length;

        while(encuentro == false){
            elegir = (int)(Math.random()*longitud);

            mirar = elMapa[elegir].split("=");
            //mirar[0] = 60
            //mirar[1] = 5,5
            tiempo = Integer.parseInt(mirar[0]);
            if(tiempo <= 60){
                //elegimos esa atracción
                encuentro = true;
                //y añadimos el destino a nuesta variable destino
                destino = mirar[1];
                xey = mirar[1].split(",");
                xd = xey[0];
                yd = xey[1];
            }
        }

        System.out.println(" Mi destino es: X " + xd + " Y " + yd);
    }

    //FUNCIONA 3/11/21
    public static String comprobarCasilla(String i, String j, String mapa){

        //System.out.print("Estoy en el metodo comprobarCasilla ");

        String casilla = "  · ";
        String[] informacion = mapa.split(", ");
        String[] datos;
        String[] localizar;
        boolean encuentro = false;
        int i2 = 0;

        while(encuentro == false && i2 < informacion.length){
            //ejemplo 60=5,5
            //datos[0]=60
            //datos[1]=5,5
                //localizar[0]=5
                //localizar[1]=5
            datos = informacion[i2].split("=");
            localizar = datos[1].split(",");
            if(i.equals(localizar[0]) && j.equals(localizar[1])){
                casilla = "";
                casilla += "  ";
                if(datos[0].matches("[+-]?\\d*(\\.\\d+)?") == true &&Integer.parseInt(datos[0]) < 10){
                    casilla += datos[0] + " ";
                }else{
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

    public static void moverse(String mapa){

        System.out.println("Estoy en el metodo moverse ");

        String casilla = "", mapaEntero = "";

        //LOGICAAAAAAA

        //en mapa tengo
        // 45=5:5, 24=7:10

        //una vez tengo esto tengo que elegir destino
        //en mapa solo tengo las atracciones
        elegirDestino(mapa);

        //una vez elegido el destino, tengo que calcular el recorrido
        //y le voy pasando a engin mi posicion y mi destino todo el rato

        boolean logOut = false;

        //prueba
        //añadir boton que sea logout
        while(logOut == false){

            System.out.println("destino: " + xd + " " + yd);
            System.out.println("posicion: " + xp + " " + yp);

            int numero;
            Scanner scan = new Scanner(System.in);
            System.out.print("Introduce el numero de visitantes: ");
            numero = scan.nextInt();

            //llamo a productor que le paso mi id y mi posicion
            //en cada iteración voy avanzando
            //productorPosicion(); //envia id:posicion:destino


            //recibo mapa y compruebo que mi destino no haya superado 60 mins
            //y muestro mapa
            String elMapa = "";//consumidorMapa(); //recibo mapa:mapaJugadoresPosicion:mapaJugadoresDestino

            mapa3 = primerJugadores.toString();

            elMapa = mapa2 + ":" + mapa3;
            System.out.println("Elmapa " + elMapa);

            movimiento();

            //una vez hago esto cambio a mi nueva posicion para llegar a mi destino
            cambiarPosicion(elMapa);

            //a mostrarMapa solo pasarle mapa y mapa jugadores
            mostrarMapa(elMapa);

            if(xp.equals(xd) && yp.equals(yd)){
                System.out.println("he llegado a mi destino");

                elegirDestino(mapa);
            }

            primerJugadores.replace("j1", xp + "," + yp);

        }


        /*
        //FUNCIONA 3/11/21 FALTARIA VER JUGADORES EN CASILLAS DE ATRACCIONES
        //TODO PARA MOSTRAR EL MAPA
        System.out.println("****** Fun with queues PortAventura ******");
        System.out.println("    ID      Nombre      Pos     Destino");

        //hacer un for y recorrer tantas veces como visitantes hayan
        //hay que mostrar los datos de todos los jugadores
        System.out.print("  ");
        for(int i2 = 0;i2 < 20; i2++){
            if(i2+1 > 10){
                System.out.print("  ");
                System.out.print(i2+1 + "");
            }else{
                System.out.print("   ");
                System.out.print(i2+1 + "");
            }
        }
        System.out.println();
        for(int i = 1;i <= 20; i++){
            if(i < 10){
                System.out.print(i + "  ");
            }else{
                System.out.print(i + " ");
            }
            for(int j = 1;j <= 20; j++){
                //ver como pasar el mapa de atracciones y jugadores para cuando un jugador este en una atracción
                casilla = comprobarCasilla(String.valueOf(i), String.valueOf(j), mapaEntero);
                System.out.print(casilla);
                //System.out.print("  * ");
            }
            System.out.println();
        }

         */


    }

    //FUNCIONA 3/11/21
    public static void movimiento(){

        System.out.println("me muevo");

        int xP, yP, xD, yD;
        xP = Integer.parseInt(xp);
        yP = Integer.parseInt(yp);
        xD = Integer.parseInt(xd);
        yD = Integer.parseInt(yd);

        if(xD > xP && yD > yP){
            xP++;
            yP++;
            xp = String.valueOf(xP);
            yp = String.valueOf(yP);
        }else if(xD > xP && yD == yP){
            xP++;
            xp = String.valueOf(xP);
        }else if(xD == xP && yD > yP){
            yP++;
            yp = String.valueOf(yP);
        }else if(xD < xP && yD < yP){
            xP--;
            yP--;
            xp = String.valueOf(xP);
            yp = String.valueOf(yP);
        }else if(xD < xP && yD == yP){
            xP--;
            xp = String.valueOf(xP);
        }else if(xD == xP && yD < yP){
            yP--;
            yp = String.valueOf(yP);
        }else{
            System.out.println("no hago nada");
        }

        System.out.println("Posicion: X=" + xp + " Y=" + yp);
    }

    //hay que comprobarlo en cada movimiento
    public static void cambiarPosicion(String elMapa){

        System.out.println("Estoy en el metodo cambiarPosicion ");

        //en elMapa tengo lo siguiente: mapa:mapaJugadores
        String[] partir = elMapa.split(":");

        //igual tengo que quitar { y }
        //substring(1, informacion[1].length()-1);
        String limpiar = partir[0].substring(1, partir[0].length()-1);
        String[] comprobar = limpiar.split(", ");
        String[] numeros, numero;
        int mins = 0;
        boolean encontrado = false;

        for(int i = 0;i < comprobar.length && encontrado == false; i++){
            numeros = comprobar[i].split("=");
            //numeros[0]
            //numeros[1] = 5,5
            numero = numeros[1].split(",");
            if(xd.equals(numero[0]) && yd.equals(numero[1])){
                mins = Integer.parseInt(numeros[0]);
                encontrado = true;
            }
        }

        if(mins > 60){
            elegirDestino(limpiar);
        }

    }

    public static void productorPosicion(){

        Properties proper = new Properties();

        proper.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,gestorColas);
        proper.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        proper.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> productor = new KafkaProducer<>(proper);

        String informacionPosicion = "";

        informacionPosicion = id + ":" + posicion + ":" + destino;

        productor.send(new ProducerRecord<String, String>("topicPosicion", "keyA", informacionPosicion));

        productor.flush();
        productor.close();

    }

    //consumidor que va recibiendo el mapa para mostrarlo
    //a través del topic devolverMapa
    public static String consumidorMapa() {

        Properties proper = new Properties();

        String datos = "";

        String id = "", nuevoTiempo = "";
        String[] informacion;

        /*
        datos += hostKafka;
        datos += ":";
        datos += puertoKafka;

        System.out.println(datos);
         */

        proper.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, gestorColas);
        proper.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        proper.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        proper.put(ConsumerConfig.GROUP_ID_CONFIG, "sensorGroup");
        proper.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(proper);
        consumer.subscribe(Collections.singleton("devolverMapa"));


        String elMapa = "";
        try{

            while(true) {

                //elMapa = "2";
                ConsumerRecords<String, String> records = consumer.poll(100);

                //recibo mapa:mapaJugadores

                for (ConsumerRecord<String, String> record : records) {
                    //record.value().toString();
                }

            }


        }finally{
            consumer.close();
            return elMapa;
        }
    }

    public static void mostrarMapa(String mapaEntero){

        System.out.println("estoy en mostrarMapa " + mapaEntero.toString());
        String casilla = "";


        String[] limpiar = mapaEntero.split(":");
        String mapaLimpio = limpiar[0].toString().substring(1, limpiar[0].toString().length()-1) + ", " + limpiar[1].toString().substring(1, limpiar[1].toString().length()-1);

        System.out.print("  ");
        for(int i2 = 0;i2 < 20; i2++){
            if(i2+1 > 10){
                System.out.print("  ");
                System.out.print(i2+1 + "");
            }else{
                System.out.print("   ");
                System.out.print(i2+1 + "");
            }
        }
        System.out.println();
        for(int i = 1;i <= 20; i++){
            if(i < 10){
                System.out.print(i + "  ");
            }else{
                System.out.print(i + " ");
            }
            for(int j = 1;j <= 20; j++){
                //ver como pasar el mapa de atracciones y jugadores para cuando un jugador este en una atracción
                casilla = comprobarCasilla(String.valueOf(i), String.valueOf(j), mapaLimpio);
                System.out.print(casilla);
                //System.out.print("  * ");
            }
            System.out.println();
        }
    }

    //todo listo
    //esta es la parte que se comunica con registry
    //faltaria revisar el modificar el usuario
    public static Boolean registrar(String alias, String usuario, String contrasenya){

        aliasUsu = alias;
        usu = usuario;
        contra = contrasenya;

        try {
            Socket skCliente = new Socket(hostRegistry, Integer.parseInt(puertoRegistry));

            escribeSocket(skCliente, aliasUsu, usu, contra);

            String datos = "";

            datos = leeSocket(skCliente, datos);

            System.out.print(datos);

        }catch(Exception e){

        }
        return true;
    }

    public static void escribeSocket(Socket p_sk, String alias, String usu, String contra){

        String datos = "";

        datos += alias;
        datos += ", ";
        datos += usu;
        datos += ", ";
        datos += contra;

        System.out.println(datos);

        try{
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


        }catch(Exception e){

        }

        return;

    }

    public static String leeSocket(Socket p_sk, String datos){


        try{
            InputStream aux = p_sk.getInputStream();
            DataInputStream flujo = new DataInputStream(aux);
            datos = flujo.readUTF();
        }catch(Exception e){

        }

        return datos;
    }
}
