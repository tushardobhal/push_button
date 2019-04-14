package gaze.ece.ucsd.com.pushbutton;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class MainActivity extends AppCompatActivity {
    private final static String QUEUE_NAME = "push-button";
    private BlockingDeque<String> queue = new LinkedBlockingDeque<String>();
    private ConnectionFactory factory = new ConnectionFactory();
    private Thread publishThread;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("", "Connecting to RabbitMQ");
        setupConnectionFactory();
        publishToAMQP();
        setupBallButton();
        setupCardsButton();
        setupMapButton();
        setupDiceButton();
        setupKeyButton();
        setupCancelButton();
    }
    void setupBallButton() {
        Button button = (Button) findViewById(R.id.ball);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishMessage("ball");
            }
        });
    }
    void setupCardsButton() {
        Button button = (Button) findViewById(R.id.cards);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishMessage("cards");
            }
        });
    }
    void setupMapButton() {
        Button button = (Button) findViewById(R.id.map);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishMessage("map");
            }
        });
    }
    void setupDiceButton() {
        Button button = (Button) findViewById(R.id.dice);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishMessage("dice");
            }
        });
    }
    void setupKeyButton() {
        Button button = (Button) findViewById(R.id.key);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishMessage("key");
            }
        });
    }
    void setupCancelButton() {
        Button button = (Button) findViewById(R.id.cancel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishMessage("cancel");
            }
        });
    }
    void publishMessage(String message) {
        try {
            queue.offerLast(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setupConnectionFactory() {
        String uri = "amqp://vgocndla:xP3XY98IZenywMYEVGcg3TDl-Hqie-h6@otter.rmq.cloudamqp.com/vgocndla";
        try {
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUri(uri);
        } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e1) {
            e1.printStackTrace();
        } catch(Exception e) {
            Log.e("", "Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void publishToAMQP()
    {
        publishThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Connection connection = factory.newConnection();
                        Channel ch = connection.createChannel();
                        ch.queueDeclare(QUEUE_NAME, false, false, false, null);
                        while (true) {
                            if(queue.peek() != null) {
                                String message = queue.takeFirst();
                                try {
                                    Log.i("", "" + message);
                                    ch.basicPublish("", QUEUE_NAME, null, message.getBytes());
                                    Log.d("", "[s] " + message);
                                } catch (Exception e) {
                                    Log.d("", "[f] " + message);
                                    throw e;
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        Log.d("", "Connection broken: " + e.getClass().getName());
                        try {
                            Thread.sleep(5000); //sleep and then try again
                        } catch (InterruptedException e1) {
                            break;
                        }
                    }
                }
            }
        });
        publishThread.start();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        publishThread.interrupt();
    }
}
