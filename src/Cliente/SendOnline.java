package Cliente;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author jean_
 */
public class SendOnline extends WindowAdapter {

    @Override
    public void windowOpened(WindowEvent e) {
        try {
            Socket mySocket = new Socket("192.168.1.61", 9999);
            Paquete data = new Paquete();
            data.setMsj("En linea");
            ObjectOutputStream dataPack = new ObjectOutputStream(mySocket.getOutputStream());
            dataPack.writeObject(data);
            mySocket.close();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
}
}