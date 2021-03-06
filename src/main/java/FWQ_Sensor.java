import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

public class FWQ_Sensor {

    //holaaa

    private static int contador = 0;
    private static int numFijo = 0;
    private static int numNum = 0;

    public static void main(String[] args){

        /*
        //ejemplo de como mostrar el mapa
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
        for(int i = 0;i < 20; i++){
            if(i+1 < 10){
                System.out.print(i+1 + "  ");
            }else{
                System.out.print(i+1 + " ");
            }
            for(int j = 0;j < 20; j++){
                //casilla = comprobarCasilla(i, j, mapa);
                //System.out.print(casilla);
                System.out.print("  * ");
            }
            System.out.println();
        }

         */

        /*


        //ejemplo para mirar a que destino quiero ir y también para mostrar el mapa
        //y asi mirar las posiciones...
        HashMap<String, String> prueba = new HashMap<>();

        prueba.put("60", "5:5");
        prueba.put("40", "7:15");
        prueba.put("j1", "6,12");

        String prueba2 = prueba.toString();

        System.out.println(prueba2);

        String yuju = prueba2.substring(1, prueba2.length()-1);

        String[] todoo = yuju.split(", ");

        for(int i = 0;i < todoo.length; i++){
            System.out.println(todoo[i]);
            String[] pro;
            pro = todoo[i].split("=");
            System.out.print(pro[0]);


            System.out.println();
        }

         */

        //recibe ip y puerto de kafka

        //id de la atraccón

        String hostKafka = "", puertoKafka = "", id = "";

        hostKafka = args[0];
        puertoKafka = args[1];
        id = args[2];

        int opcion = 0;
        Scanner scan = new Scanner(System.in);
        System.out.println("Sensor de la atraccion " + id);
        System.out.println("1. Meter a mano el numero de visitantes");
        System.out.println("2. Enviar cada 1/3 segundos visitantes aleatorios");
        opcion = scan.nextInt();

        String datos = "";
        datos += hostKafka;
        datos += ":";
        datos += puertoKafka;

        String finalDatos = datos;
        String finalId = id;
        int finalOpcion = opcion;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while(true){

                    try{
                        int numero = (int)(Math.random()*6+3);
                        numero *= 1000;
                        Thread.sleep(numero);
                        productor(finalDatos, finalId, finalOpcion);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread hilo = new Thread(runnable);
        hilo.start();
    }


    public static void productor(String datos, String id, int opcion){

        Properties proper = new Properties();

        System.out.println(datos);

        proper.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,datos);
        proper.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        proper.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> productor = new KafkaProducer<>(proper);

        String tiempo = "";

        int numero = 0;

        if(opcion == 1){
            //todo si mete opcion 1 llamar a una funcion que vaya sumando 1 y restando 1 para que no
            //se vaya mandando todo el rato el mismo numero y sepa si ha caido o no
            if(contador == 0) {
                System.out.print("Introduce el numero de visitantes: ");
                Scanner scan = new Scanner(System.in);
                numero = scan.nextInt();
                numNum = numero;
            }else{
                numFijo++;
                if(numFijo % 2 == 0){
                    numero = numNum + 1;
                }else{
                    numero = numNum - 1;
                }
            }
            contador ++;

        }else if(opcion == 2){
            numero = (int)(Math.random()*100+25);
        }

        System.out.println(numero);

        tiempo = "";

        tiempo += id;
        tiempo += ":";
        tiempo += numero;


        productor.send(new ProducerRecord<String, String>("sensores", "keyA", tiempo));

        productor.flush();
        productor.close();
    }
}
