package Formularios;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import test.Menu;

public class Inicio extends JFrame {

    //objetos del formulario
    private JLabel usuario;
    private JLabel contra;

    private JTextField txtUsuario;
    private JPasswordField txtContra;

    private JButton entrar;
    private JButton registrar;

    //constructor
    public Inicio(){
            setTitle("Bienvenido al parque");
            setResizable(false);
            setSize(390, 180);
            setLayout(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


            usuario = new JLabel("Usuario: ");
            txtUsuario = new JTextField(10);
            contra = new JLabel("Contraseña: ");
            txtContra = new JPasswordField(10);

            entrar = new JButton("Entrar");
            registrar = new JButton("Registrarse");

            add(usuario);
            add(txtUsuario);
            add(contra);
            add(txtContra);
            add(entrar);
            add(registrar);

            usuario.reshape(20, 20, 100, 20);
            txtUsuario.reshape(120, 20, 100, 20);

            contra.reshape(20, 45, 100, 20);
            txtContra.reshape(120, 45, 100, 20);

            entrar.reshape(180, 75, 70, 30);
            registrar.reshape(20, 75, 120, 30);

            entrar.addActionListener(new ActionListener() {
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



    }

    private void entrarParque(ActionEvent e){

    }

    private void registrarParque(ActionEvent e){

        Registrar miRegistrar = new Registrar();
        miRegistrar.setVisible(true);



    }

    public JTextField textoUsuario(){
        return txtUsuario;
    }
}
