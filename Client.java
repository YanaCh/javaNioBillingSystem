import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by yana on 04.05.2018.
 */
public class Client {
    public final static int PORT = 2345;
    public final static String ADDRESS = "localhost";
    SocketChannel channel;
    ByteBuffer buffer;



    public static void main(String[] args){
        Client client = new Client();

        try {
            client.startTest();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    public void startTest() throws IOException{
        connectToServer();
        sendNewCardOperation("Piter","1");
        sendAddMoneyOperation("1", 10);
        sendAddMoneyOperation("1", 10);
        sendSubMoneyOperation("1", 5);
        sendGetCardBalanceOperation("1");
        sendSubMoneyOperation("1", 5);

    }


    private void connectToServer() throws IOException{
        channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(ADDRESS,PORT));
        buffer = ByteBuffer.allocate(1024);

    }

    private void sendNewCardOperation(String personName, String card) throws IOException{

        buffer.putInt(Server.ADD_NEW_CARD);

        byte[] personNameBytes = personName.getBytes();
        buffer.putInt(personNameBytes.length);
        buffer.put(personNameBytes);

        byte[] cardBytes = card.getBytes();
        buffer.putInt(cardBytes.length);
        buffer.put(cardBytes);

        buffer.flip();

        channel.write(buffer);

        buffer.clear();

    }

    private void sendAddMoneyOperation(String card, double money) throws IOException{

        buffer.putInt(Server.ADD_MONEY);

        buffer.putDouble(money);

        byte[] cardBytes = card.getBytes();
        buffer.putInt(cardBytes.length);
        buffer.put(cardBytes);

        buffer.flip();

        channel.write(buffer);

        buffer.clear();

    }

    private   void sendSubMoneyOperation(String card, double money) throws IOException{
        buffer.putInt(Server.SUB_MONEY);

        buffer.putDouble(money);

        byte[] cardBytes = card.getBytes();
        buffer.putInt(cardBytes.length);
        buffer.put(cardBytes);

        buffer.flip();

        channel.write(buffer);

        buffer.clear();

    }

    private  double sendGetCardBalanceOperation(String card) throws IOException{
        buffer.putInt(Server.GET_CARD_BALANCE);

        byte[] cardBytes = card.getBytes();
        buffer.putInt(cardBytes.length);
        buffer.put(cardBytes);

        buffer.flip();

        channel.write(buffer);

        buffer.clear();

        ByteBuffer answerBuf = ByteBuffer.allocate(1024);
        channel.read(answerBuf);
        answerBuf.flip();
        double money = answerBuf.getDouble();
        System.out.println("Client side money: " + money);
        return money;


    }



}
