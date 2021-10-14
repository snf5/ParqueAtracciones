package Formularios;

import test.Menu;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.awt.*;



public class Registrar extends JFrame{

    private JLabel usuario;
    private JLabel contra;
    private JLabel alias;

    private JTextField txtAlias;
    private JTextField txtUsuario;
    private JPasswordField txtContra;

    private JButton registrar;


    public Registrar(){
        setTitle("Registro del parque");
        setResizable(false);
        setSize(390, 220);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        alias = new JLabel("Alias: ");
        txtAlias = new JTextField(10);
        usuario = new JLabel("Usuario: ");
        txtUsuario = new JTextField(10);
        contra = new JLabel("Contraseña: ");
        txtContra = new JPasswordField(10);

        registrar = new JButton("Registrarse");

        add(alias);
        add(txtAlias);
        add(usuario);
        add(txtUsuario);
        add(contra);
        add(txtContra);
        add(registrar);

        alias.reshape(20, 20, 100, 20);
        txtAlias.reshape(120, 20, 100, 20);

        usuario.reshape(20, 45, 100, 20);
        txtUsuario.reshape(120, 45, 100, 20);

        contra.reshape(20, 75, 100, 20);
        txtContra.reshape(120, 75, 100, 20);

        registrar.reshape(20, 110, 120, 30);



        registrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarParque(e);
            }
        });



    }

    private boolean registrarParque(ActionEvent e){

        test.Menu nuevoM = new Menu();

        if(nuevoM.registrar(txtAlias.getText(),txtUsuario.getText(), txtContra.getText()) == true){

            Inicio ini = new Inicio();

            ini.setVisible(true);

            this.setVisible(false);

        }

        return true;
    }
}
