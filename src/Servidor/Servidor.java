package Servidor;

import Cliente.Paquete;
import com.jtattoo.plaf.aluminium.AluminiumLookAndFeel;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author admin-jean
 */
public class Servidor extends javax.swing.JFrame implements Runnable {

    private ArrayList<String> ipList = new ArrayList<>();

    public Servidor() {
        initComponents();
        configWindowServer();
        initThread();
    }

    private void initThread() {
        Thread thread = new Thread(this);
        thread.start();
    }

    private void configWindowServer() {
        setLocationRelativeTo(null);
        jTxtAreaMsjServer.enable(false);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTxtAreaMsjServer = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Servidor");

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jTxtAreaMsjServer.setColumns(20);
        jTxtAreaMsjServer.setRows(5);
        jScrollPane1.setViewportView(jTxtAreaMsjServer);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void listenConnection() {
        try {
            ServerSocket server = new ServerSocket(9999);
            String nick, ip, msj;
            Paquete packReceive;
            while (true) {
                Socket mySocket = server.accept();
                ObjectInputStream dataPack = new ObjectInputStream(mySocket.getInputStream());
                packReceive = (Paquete) dataPack.readObject();
                nick = packReceive.getNick();
                ip = packReceive.getIp();
                msj = packReceive.getMsj();
                if (!msj.equals("En linea")) {
                    jTxtAreaMsjServer.append("\n De " + nick + ": " + msj + " -→  mensaje para " + ip);
                    sendDestination(ip, packReceive);
                    mySocket.close();
                } else {
                    detectsOnline(mySocket, packReceive);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void detectsOnline(Socket mySocket, Paquete packReceive) {
        InetAddress location = mySocket.getInetAddress();
        String ipRemote = location.getHostAddress();
        ipList.add(ipRemote);
        packReceive.setIpList(ipList);
        jTxtAreaMsjServer.append("\nCliente en linea con IP: " + ipRemote);
        try {
            for (String ip : ipList) {
                Socket sendIpCustomer = new Socket(ip, 9090);
                ObjectOutputStream packSend = new ObjectOutputStream(sendIpCustomer.getOutputStream());
                packSend.writeObject(packReceive);
                packSend.close();
                sendIpCustomer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendDestination(String ip, Paquete packReceive) {
        try {
            Socket sendDestination = new Socket(ip, 9090);
            ObjectOutputStream packReenvio = new ObjectOutputStream(sendDestination.getOutputStream());
            packReenvio.writeObject(packReceive);
            sendDestination.close();
            packReenvio.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

                new Servidor().setVisible(true);

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTxtAreaMsjServer;
    // End of variables declaration//GEN-END:variables

    @Override
    public void run() {
        listenConnection();
    }
}
