import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by yana on 05.05.2018.
 */
public class Server implements Runnable {
    public static final int ADD_NEW_CARD = 1;
    public static final int ADD_MONEY = 2;
    public static final int SUB_MONEY = 3;
    public static final int GET_CARD_BALANCE = 4;
    public static final int EXIT_CLIENT = 5;

    private int port = 2345;
    private ServerSocketChannel ssc;
    private Selector selector;
    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    private HashMap<String, Double> date;

    public Server(int port) throws IOException{
        this.port = port;
        this.ssc = ServerSocketChannel.open();
        this.ssc.bind(new InetSocketAddress(port));
        this.ssc.configureBlocking(false);
        this.selector = Selector.open();
        this.date = new HashMap<String, Double>();

        this.ssc.register(selector, SelectionKey.OP_ACCEPT);

    }

    @Override
    public void run() {
        try {
            System.out.println("Server starting on port " + this.port);

            SelectionKey key;

            while (ssc.isOpen()){
                int select = selector.select();

                if(select == 0)
                    continue;

                Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                while (selectionKeys.hasNext()){
                    key = selectionKeys.next();
                    selectionKeys.remove();

                    if(key.isAcceptable())
                        this.handleAccept(key);

                    if(key.isReadable())
                        this.handleRead(key);
                }
            }
        }

        catch (IOException e){
            e.printStackTrace();
            System.out.println("IOException, server of port " +this.port+ " terminating. Stack trace:");
        }
    }


    private void handleAccept(SelectionKey key) throws IOException{
        SocketChannel client = ((ServerSocketChannel)key.channel()).accept();

        client.configureBlocking(false);

        client.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE);

        String address = (new StringBuilder( client.socket().getInetAddress().toString() ))
                .append(":").append( client.socket().getPort() ).toString();
        System.out.println("accepted connection from: "+address);
    }

    private void handleRead(SelectionKey key) throws IOException{
        SocketChannel ch = (SocketChannel) key.channel();
        buffer.clear();

        boolean work = true;
        while (ch.read(buffer)>0 && work) {
            buffer.flip();
            int command;

            while (buffer.remaining() > 0){
                command = buffer.getInt();

                switch (command) {

                    case ADD_NEW_CARD:
                        addNewCard();
                        break;

                    case ADD_MONEY:
                        addMoney();
                        break;

                    case SUB_MONEY:
                        subMoney();
                        break;

                    case GET_CARD_BALANCE:
                        getCardBalance(ch);
                        break;

                    case EXIT_CLIENT:
                        work = false;
                        break;

                    default:
                        System.out.println("Bad operation:" + command);

                }
            }
        }

    }

    void addNewCard()throws IOException{
        int len = buffer.getInt();
        byte[] personNameBytes = new byte[len];
        buffer.get(personNameBytes);
        String personName = new String(personNameBytes);

        System.out.println("Name "+ personName);

        len = buffer.getInt();
        byte[] cardBytes = new byte[len];
        buffer.get(cardBytes);
        String card = new String(cardBytes);

        System.out.println("Card "+ card);

        buffer.compact();
        buffer.flip();

        date.put(card, new Double(0.0));

    }

    void addMoney() throws IOException{
        double money = buffer.getDouble();

        System.out.println("Money " +money);


        int len = buffer.getInt();
        byte[] cardBytes = new byte[len];
        buffer.get(cardBytes);
        String card = new String(cardBytes);

        System.out.println("Card" +card);

        String rest = new String(buffer.array(), buffer.position(), buffer.remaining(),
                StandardCharsets.UTF_8);

        System.out.println("Rest "+ rest + " remainimg" + buffer.remaining());

        buffer.compact();
        buffer.flip();


        Double d = (Double)date.get(card);
        if(d!=null) date.put(card, d+money);
        System.out.println("Card from hashmap add: " + date.get(card));


    }

    void subMoney() throws IOException{
        double money = buffer.getDouble();

        int len = buffer.getInt();
        byte[] cardBytes = new byte[len];
        buffer.get(cardBytes);
        String card = new String(cardBytes);

        buffer.compact();
        buffer.flip();

        Double d = (Double)date.get(card);
        if(d!=null) date.put(card, d-money);
        System.out.println("Card from hashmap sub: " + date.get(card));


    }

    void getCardBalance(SocketChannel ch) throws IOException{
        int len = buffer.getInt();
        byte[] cardBytes = new byte[len];
        buffer.get(cardBytes);
        String card = new String(cardBytes);

        buffer.compact();
        buffer.flip();

        Double d = (Double)date.get(card);
        System.out.println("getCardBalance money: " + d);
        if(d!=null){
            double money = d;
            ByteBuffer answerBuf = ByteBuffer.allocate(1024);
            answerBuf.putDouble(money);
            answerBuf.flip();
            System.out.println("Array: "+ new String(answerBuf.array(), answerBuf.position(),answerBuf.remaining()));
            int num = ch.write(answerBuf);
            System.out.println("Number of send bytes: " + num);
            answerBuf.clear();

        }
    }

    public static void main(String[] args) throws IOException{
        Server server = new Server(2345);
        new Thread(server).start();
    }
}
