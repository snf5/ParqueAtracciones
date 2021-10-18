package test;

import Formularios.Inicio;

import java.io.*;
import java.net.Socket;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import Formularios.Registrar;
import com.mongodb.MongoClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;


public class Menu {

    private static String usu = "";
    private static String contra = "";
    private static String aliasUsu = "";

    private static String hostRegistry, puertoRegistry, hostKafka, puertoKafka, gestorColas;

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

        System.out.println(hostRegistry);

        login = new Inicio();
        login.setVisible(true);

        //comprobar que el registro se hizo correctamente
        //para ello el registry me debe devolver por socket
        //una confirmación

        /*
        topic:
        - credenciales
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

    public void consumir(){


        Properties proper = new Properties();

        String datos = hostKafka + puertoKafka;

        //añadir el grupo de consumidores de solo visitantes

        proper.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, datos);
        proper.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        proper.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        proper.put(ConsumerConfig.GROUP_ID_CONFIG, "testGroup");
        proper.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(proper);
        consumer.subscribe(Collections.singleton("parque1"));

        long count = 0L;
        while(count < 100){
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));

            for(ConsumerRecord<String, String> record : records){
                System.out.println("MSG: " + record.toString());
                count++;
            }
        }



    }

    //productor que envia a traves de topiccredenciales
    public static void productorCredenciales(String usuario, String contra){

        boolean entrar = false;

        Properties proper = new Properties();

        String credenciales = usuario + ":" + contra;

        proper.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,gestorColas);
        proper.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        proper.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> productor = new KafkaProducer<>(proper);

        productor.send(new ProducerRecord<String, String>("topiccredenciales", "keyA", credenciales));

        productor.flush();
        productor.close();

    }

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
