package Cliente;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author admin-jean
 */
public class Cliente extends javax.swing.JFrame implements Runnable {

    private String nick;

    public Cliente() {
        initComponents();
        configWindowCliente();
        initThread();
        addWindowListener(new SendOnline());
    }

    //Configura el contenido de la ventana
    private void configWindowCliente() {
        inputNick();
        setLocationRelativeTo(null);
        jLblViewImg.setText("");
        jTxtAreaReceive.enable(false);
        jBtnSendMsj.setIcon(setIconBtn("/imagenes/send.png", jBtnSendMsj));
        jBtnSendImg.setIcon(setIconBtn("/imagenes/send.png", jBtnSendImg));
        jBtnUploadImg.setIcon(setIconBtn("/imagenes/img.png", jBtnUploadImg));
        jBtnSendMsj.setPressedIcon(setIconPresionado("/imagenes/send.png", jBtnSendMsj, 10, 10));
        jBtnSendImg.setPressedIcon(setIconPresionado("/imagenes/send.png", jBtnSendImg, 10, 10));
        jBtnUploadImg.setPressedIcon(setIconPresionado("/imagenes/img.png", jBtnUploadImg, 10, 10));
    }

    //Pedir nombre del cliente
    private void inputNick() {
        nick = JOptionPane.showInputDialog("Nombre: ");
        jLblNick.setText(nick);
    }

    //Agregar icono al boton
    private Icon setIconBtn(String url, JButton boton) {
        ImageIcon icon = new ImageIcon(getClass().getResource(url));
        int ancho = boton.getWidth();
        int alto = boton.getHeight();
        ImageIcon icono = new ImageIcon(icon.getImage().getScaledInstance(ancho, alto, Image.SCALE_DEFAULT));
        return icono;
    }

    //efecto de presionado 
    private Icon setIconPresionado(String url, JButton boton, int ancho, int altura) {
        ImageIcon icon = new ImageIcon(getClass().getResource(url));
        int width = boton.getWidth() - ancho;
        int height = boton.getHeight() - altura;
        ImageIcon icono = new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
        return icono;
    }

    private void sendMsj() { //Envia msj por socket
            try {
                Socket mySocket = new Socket("192.168.1.61", 9999); //Se crea el socket parametros ip server y puerto
                Paquete pack = new Paquete();
                pack.setNick(jLblNick.getText());
                pack.setMsj(jTxtMsj.getText());
                pack.setIp(jCbxOnLine.getSelectedItem().toString());
                if (!jTxtMsj.getText().isEmpty()) { //Valida que el campo no esté vacío
                pack.setMsj(jTxtMsj.getText());
                pack.setImagen(null);
            } else if (!jTxtUploadImg.getText().isEmpty()) {
                pack.setMsj("Imagen");
                BufferedImage bufferedImage = ImageIO.read(new File(jTxtUploadImg.getText()));
                ByteArrayOutputStream salidaImagen = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", salidaImagen);
                byte[] bytesImagen = salidaImagen.toByteArray();
                pack.setImagen(bytesImagen);
            }
                ObjectOutputStream dataPack = new ObjectOutputStream(mySocket.getOutputStream());
                dataPack.writeObject(pack);
                mySocket.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            jTxtAreaReceive.append("\n" + jTxtMsj.getText());
            jTxtMsj.setText("");
        }

    private void loadImage(String ruta) {
        ImageIcon image = new ImageIcon(ruta);
        jLblViewImg.setIcon(new ImageIcon(image.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
        jLblViewImg.repaint();
    }

    private void uploadImg() {
        JFileChooser vtn = new JFileChooser();
        FileNameExtensionFilter file = new FileNameExtensionFilter("JPG, PNG & GIF", "jpg", "png", "gif");
        vtn.setFileFilter(file);
        int i = vtn.showOpenDialog(this);
        if (i == JFileChooser.APPROVE_OPTION) {
            String ruta = vtn.getSelectedFile().getAbsolutePath();
            jTxtUploadImg.setText(ruta);
        }
    }

    private void initThread() {
        Thread thread = new Thread(this);
        thread.start();
    }

    private void listenConnection() {
        try {
            ServerSocket serverCustomer = new ServerSocket(9090);
            Socket customer;
            Paquete packReceive;
            while (true) {
                customer = serverCustomer.accept();
                ObjectInputStream dataPack = new ObjectInputStream(customer.getInputStream());
                packReceive = (Paquete) dataPack.readObject();
                if (!packReceive.getMsj().equalsIgnoreCase("En linea")) {
                    jTxtAreaReceive.append("\n" + packReceive.getNick() + ": " + packReceive.getMsj());
                    if (packReceive.getMsj().equals("Imagen")) {
                        System.out.println(packReceive.getImagen());
                        byte[] bytesImagen = (byte[]) (packReceive.getImagen());
                        ByteArrayInputStream entradaImagen = new ByteArrayInputStream(bytesImagen);
                        BufferedImage bufferedImage = ImageIO.read(entradaImagen);
                        FileOutputStream out = new FileOutputStream("imagen.png");
                        // esbribe la imagen a fichero
                        ImageIO.write(bufferedImage, "png", out);
                        loadImage("C:\\Users\\Guevara\\IdeaProjects\\ProyectoSockets\\imagen.png");
                    }
                } else {
                    fillComboBxIp(packReceive);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillComboBxIp(Paquete packReceive) {
        try {
            InetAddress getIpLocal = InetAddress.getLocalHost();
            String ipLocal = getIpLocal.getHostAddress();
            ArrayList<String> arrayIp = new ArrayList<>();
            jCbxOnLine.removeAllItems();
            arrayIp = packReceive.getIpList();
            for (String ip : arrayIp) {
                if (!ipLocal.equals(ip)) {
                    jCbxOnLine.addItem(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTxtMsj = new javax.swing.JTextField();
        jTxtUploadImg = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLblNick = new javax.swing.JLabel();
        jCbxOnLine = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jBtnSendMsj = new javax.swing.JButton();
        jBtnUploadImg = new javax.swing.JButton();
        jBtnSendImg = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTxtAreaReceive = new javax.swing.JTextArea();
        jLblViewImg = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Cliente");

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setFont(new java.awt.Font("Perpetua", 1, 16)); // NOI18N
        jLabel1.setText("SALIDA");

        jTxtMsj.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTxtMsjActionPerformed(evt);
            }
        });

        jTxtUploadImg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTxtUploadImgActionPerformed(evt);
            }
        });

        jLabel3.setText("MENSAJE");

        jLabel4.setText("IMAGEN");

        jLblNick.setFont(new java.awt.Font("MS Gothic", 1, 14)); // NOI18N
        jLblNick.setText("Nick");

        jCbxOnLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxOnLineActionPerformed(evt);
            }
        });

        jLabel6.setText("EN LINEA");

        jBtnSendMsj.setContentAreaFilled(false);
        jBtnSendMsj.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBtnSendMsj.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSendMsjActionPerformed(evt);
            }
        });

        jBtnUploadImg.setContentAreaFilled(false);
        jBtnUploadImg.setPreferredSize(new java.awt.Dimension(30, 30));
        jBtnUploadImg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnUploadImgActionPerformed(evt);
            }
        });

        jBtnSendImg.setContentAreaFilled(false);
        jBtnSendImg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSendImgActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jTxtUploadImg, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jBtnUploadImg, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jTxtMsj, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jBtnSendMsj, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jBtnSendImg, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(30, 30, 30))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLblNick)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(201, 201, 201)
                                .addComponent(jLabel1)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jCbxOnLine, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(43, 43, 43))))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLblNick)
                    .addComponent(jCbxOnLine, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                        .addComponent(jBtnSendMsj, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jBtnSendImg, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTxtMsj, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTxtUploadImg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(9, 9, 9))
                            .addComponent(jBtnUploadImg, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(11, 11, 11))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel2.setFont(new java.awt.Font("Perpetua", 1, 16)); // NOI18N
        jLabel2.setText("ENTRADA");

        jTxtAreaReceive.setColumns(20);
        jTxtAreaReceive.setRows(5);
        jScrollPane1.setViewportView(jTxtAreaReceive);

        jLblViewImg.setText("                                  --imagen--");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(241, 241, 241)
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLblViewImg, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLblViewImg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnSendImgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSendImgActionPerformed
        sendMsj();
        jTxtUploadImg.setText("");
    }//GEN-LAST:event_jBtnSendImgActionPerformed

    private void jBtnUploadImgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnUploadImgActionPerformed
        uploadImg();
    }//GEN-LAST:event_jBtnUploadImgActionPerformed

    private void jBtnSendMsjActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSendMsjActionPerformed
        sendMsj();
    }//GEN-LAST:event_jBtnSendMsjActionPerformed

    private void jCbxOnLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCbxOnLineActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCbxOnLineActionPerformed

    private void jTxtUploadImgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTxtUploadImgActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTxtUploadImgActionPerformed

    private void jTxtMsjActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTxtMsjActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTxtMsjActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Cliente.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Cliente.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Cliente.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Cliente.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Cliente().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnSendImg;
    private javax.swing.JButton jBtnSendMsj;
    private javax.swing.JButton jBtnUploadImg;
    private javax.swing.JComboBox<String> jCbxOnLine;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLblNick;
    private javax.swing.JLabel jLblViewImg;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTxtAreaReceive;
    private javax.swing.JTextField jTxtMsj;
    private javax.swing.JTextField jTxtUploadImg;
    // End of variables declaration//GEN-END:variables

    @Override
    public void run() {
        listenConnection();
    }
}
