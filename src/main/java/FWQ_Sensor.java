import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Scanner;
import java.util.Timer;

public class FWQ_Sensor {

    //holaaa

    public static void main(String[] args){




        //recibe ip y puerto de kafka

        //id de la atraccón


        String hostKafka = "", puertoKafka = "", id = "";


        hostKafka = args[0];
        puertoKafka = args[1];
        id = args[2];



        int numero = (int)(Math.random()*3+1);

        //cada x tiempo (entre 1 y 3 segundos) se enviará el numero de visitantes


        //crear funcion de productor, ya que solo es productor el sensor

        String datos = "";

        datos += hostKafka;
        datos += ":";
        datos += puertoKafka;


        String finalDatos = datos;
        String finalId = id;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while(true){

                    try{
                        int numero = (int)(Math.random()*6+3);

                        numero *= 1000;

                        Thread.sleep(numero);

                        productor(finalDatos, finalId);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread hilo = new Thread(runnable);
        hilo.start();

    }


    public static void productor(String datos, String id){

        Properties proper = new Properties();

        System.out.println(datos);

        proper.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,datos);
        proper.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        proper.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> productor = new KafkaProducer<>(proper);

        String tiempo = "";

        //NO HACER RANDOM, valor aleartorio entre 2 numeros
        int numero;
        //(int)(Math.random()*100+1);

        //hacer que se ingrese por teclado el numero de personas
        Scanner scan = new Scanner(System.in);
        System.out.print("Introduce el numero de visitantes: ");
        numero = scan.nextInt();

        System.out.println(numero);

        tiempo += id;
        tiempo += ":";
        tiempo += numero;

        productor.send(new ProducerRecord<String, String>("prueba", "keyA", tiempo));

        productor.flush();
        productor.close();
    }

}
