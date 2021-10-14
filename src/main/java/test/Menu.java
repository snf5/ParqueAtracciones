package test;

import Formularios.Inicio;

import java.io.*;
import java.net.Socket;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import com.mongodb.MongoClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;


public class Menu {

    private static String usu = "";
    private static String contra = "";
    private static String aliasUsu = "";

    private static String hostRegistry, puertoRegistry, hostKafka, puertoKafka;

    public static void main(String args[]){

        //aqui recibo IP y puerto de FWQ_Registry
        //e IP y puerto de kafka

        //args[0] IP de registry
        //args[1] puerto de registry

        //args[2] IP de kafka
        //args[3] puerto de kafka

        hostRegistry = args[0];
        puertoRegistry = args[1];

        //hostKafka = args[2];
        //puertoKafka = args[3];

        System.out.println(hostRegistry);

        Inicio login = new Inicio();
        login.setVisible(true);

        //comprobar que el registro se hizo correctamente



        //comprobar que la modificacion se hizo correctamente




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
