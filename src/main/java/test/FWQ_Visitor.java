package test;

import Formularios.Inicio;

import java.io.*;
import java.net.Socket;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.Stack;

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
    private  static String x = "", y = "";

    //datos para la conexión de los puertos e ips
    private static String hostRegistry, puertoRegistry, hostKafka, puertoKafka, gestorColas;

    private static String datosUsu = "";

    private static Inicio login;
    private static Registrar miRegistrar;

    public static void main(String args[]){

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


    }

    //recibo y guardo los datos del usuario para
    //ya desde aqui ir llamando al resto de modulos para que se lleve a cabo todo
    public void recibirDatos(String usuario, String contra){

        String entradaParque = "";

        String posicion = "01:01";

        datosUsu = usuario + ":" + contra;
        //en datosUsu ya tengo usuario:contra

        System.out.println("si que me llega");
        System.out.println(datosUsu);

        if(usuario.equals("jj")){
            System.out.println("bieen");
        }else{
            System.out.println("errorrrrr");
        }



        //le paso a productorCredenciales el usuario y contraseña y
        //luego tengo que llamar a consumir para recibir el OK!

        entradaParque = productorCredenciales(usuario, contra, posicion);

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
            id = informacion[0];


            //le paso informacion[1], que es el mapa para que sepa donde esta y cual es su destino
            //OJOOOOO recibo un map de las posiciones ocupadas de las atracciones

            String quitar = informacion[1].substring(1, informacion[1].length()-1);
            //le paso la cadena tal que asi: shambala=12, estampida=10, j1=0204
            //y al hacer por ahi lo de [] me va sacando shamabal=12 ...
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

        return acceso;
    }


    //una vez entro al parque me empiezo a mover
    public void elegirDestino(String mapa){

        Boolean encuentro = false;
        int elegir = 0, longitud = 0, tiempo = 0;
        String[] mirar, xey;

        //aqui tengo que rellenar las propiedades de destino
        //50=5:5, 40=3:3, 15=7:14;

        String[] elMapa = mapa.split(", ");
        longitud = elMapa.length;

        while(encuentro == false){
            elegir = (int)(Math.random()*longitud);

            mirar = elMapa[elegir].split("=");
            //mirar[0] = 60
            //mirar[1] = 5:5
            tiempo = Integer.parseInt(mirar[0]);
            if(tiempo <= 60){
                //elegimos esa atracción
                encuentro = true;
                //y añadimos el destino a nuesta variable destino
                destino = mirar[1];
                xey = mirar[1].split(":");
                x = xey[0];
                y = xey[1];
            }
        }
    }

    public String comprobarCasilla(String i, String j, String mapa){

        String casilla = "";
        String[] informacion = mapa.split(", ");
        String[] datos;
        String[] localizar;
        boolean encuentro = false;
        int i2 = 0;

        while(encuentro == false && i2 < informacion.length){
            //ejemplo 60=5:5
            //datos[0]=60
            //datos[1]=5:5
                //localizar[0]=5
                //localizar[1]=5
            datos = informacion[i2].split("=");
            localizar = datos[1].split(":");
            if(i == localizar[0] && j == localizar[1]){
                casilla = datos[0];
                encuentro = true;
            }else{
                casilla = "·";
            }
            i2++;
        }
        //recorrer el mapa para saber si hay algo en esa casilla y mostrarlo y sino mostrar punto
        return casilla;
    }


    public void moverse(String mapa){

        String casilla = "", mapaEntero = "";

        //LOGICAAAAAAA

        //en mapa tengo
        // shambala=10, fiurius=5, j1=1005

        //una vez tengo esto tengo que elegir destino
        //en mapa solo tengo las atracciones
        elegirDestino(mapa);

        //una vez elegido el destino, tengo que calcular el recorrido
        //y le voy pasando a engin mi posicion y mi destino todo el rato

        boolean logOut = false;
        while(logOut == false){

            //llamo a productor que le paso mi id y mi posicion
            //en cada iteración voy avanzando
            productorPosicion(); //envia id:posicion:destino


            //recibo mapa y compruebo que mi destino no haya superado 60 mins
            //y muestro mapa
            consumidorMapa(); //recibo mapa:mapaJugadores


            //una vez hago esto cambio a mi nueva posicion para llegar a mi destino


        }





        //TODO PARA MOSTRAR EL MAPA
        System.out.println("****** Fun with queues PortAventura ******");
        System.out.println("    ID      Nombre      Pos     Destino");
        //hacer un for y recorrer tantas veces como visitantes hayan
        //hay que mostrar los datos de todos los jugadores
        for(int i2 = 0;i2 < 20; i2++){
            System.out.println(i2+1 + " ");
        }
        System.out.println();
        //mostrar mapa
        for(int i = 0;i < 20; i++){
            for(int j = 0;j < 20; j++){
                casilla = comprobarCasilla(String.valueOf(i), String.valueOf(j), mapaEntero);
                System.out.print(casilla);
            }
            System.out.println();
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
    public static void consumidorMapa() {

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


        try{
            while(true) {
                ConsumerRecords<String, String> records = consumer.poll(100);

                //me va a devolver una matriz
                //la tengo que representar en un mapa

                for (ConsumerRecord<String, String> record : records) {
                    //record.value().toString();
                }

            }


                }finally{
            consumer.close();
        }

    }

    public void mostrarMapa(){

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
