package com.xexex.app.myapplication;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.NotYetConnectedException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.drafts.Draft_75;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.os.Message;

public class MainActivity extends AppCompatActivity {
    private static final int STATUS_CLOSE = 0;
    private static final int STATUS_CONNECT = 1;
    private static final int STATUS_MESSAGE = 2;

    DraftLevel mDraftLevel = DraftLevel.Draft17;
    private WebSocketWorker mWebSockClient = null;
    private WebSocketMessageHandler mWebSocketMessageHandler= new WebSocketMessageHandler();

    private ScrollView svContent;
    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        svContent = (ScrollView) findViewById(R.id.svContent);
        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        String url = "msgtest.koumei.tv/api/Msg";
        //int port = 80;
        String address = String.format("ws://%s", url);
        WebSocketConnect(address, mDraftLevel);
    }



    private void WebSocketConnect(String address, DraftLevel level) {
        Draft draft = null;
        switch (level) {
            case Draft10:
                draft = new Draft_10();
                break;
            case Draft17:
                draft = new Draft_17();
                break;
            case Draft75:
                draft = new Draft_75();
                break;
            case Draft76:
                draft = new Draft_76();
                break;
            default:
                draft = new Draft_17();
                break;
        }
        try {
            URI uri = new URI(address);
            mWebSockClient = new WebSocketWorker(uri, draft);
            mWebSockClient.connect();
        } catch (URISyntaxException ex) {
            System.out.println( "Socket connect URISyntaxException" );

            ex.printStackTrace();
            return;
        } catch (Exception ex) {
            System.out.println( "Socket connect Exception" );

            ex.printStackTrace();
            return;
        }
    }

    public void onSendClick(View view) {
        if (null != mWebSockClient) {
            String msg = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
            if (!TextUtils.isEmpty(msg)) {
                try {
                    mWebSockClient.send(msg);
                } catch (NotYetConnectedException ex) {
                    System.out.println( "Socket Send NotYetConnectedException" );

                    ex.printStackTrace();

                    return;
                } catch (Exception ex) {
                    System.out.println( "Socket Send Exception" );

                    ex.printStackTrace();
                    return;
                }
            }
        }
    }

    private class WebSocketMessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STATUS_CONNECT: {
                    String message = String.format("[%d] %s\n", System.currentTimeMillis(), msg.obj.toString());
                    mTextMessage.append(message);
                }
                break;
                case STATUS_CLOSE: {
                    String message = String.format("[%d] %s\n", System.currentTimeMillis(), msg.obj.toString());
                    mTextMessage.append(message);
                }
                break;
                case STATUS_MESSAGE: {
                    String message = String.format("[%d] %s\n", System.currentTimeMillis(), msg.obj.toString());
                    mTextMessage.append(message);
                }
                break;
                default:
                    break;
            }

            svContent.postDelayed(new Runnable() {
                @Override
                public void run() {
                    svContent.fullScroll(View.FOCUS_DOWN);
                }
            }, 100);
        }
    }

    private class WebSocketWorker extends WebSocketClient{

        public WebSocketWorker(URI serverUri, Draft draft) {
            super(serverUri, draft);
        }

        public WebSocketWorker(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handShakeData) {
            // TODO Auto-generated method stub
            System.out.println( "opened connection" );

            Message msg = new Message();
            msg.what = STATUS_CONNECT;
            msg.obj = String.format("[Welcome：%s]", getURI());
            mWebSocketMessageHandler.sendMessage(msg);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            // TODO Auto-generated method stub
            System.out.println( "Connection closed by " + ( remote ? "remote peer" : "us" ) );

            Message msg = new Message();
            msg.what = STATUS_CLOSE;
            msg.obj = String.format("[Bye：%s]", getURI());
            mWebSocketMessageHandler.sendMessage(msg);
        }

        @Override
        public void onError(Exception ex) {
            // TODO Auto-generated method stub
            System.out.println( "Socket Error" );

            ex.printStackTrace();
         }

        @Override
        public void onMessage(String message) {
            // TODO Auto-generated method stub
            System.out.println( "Socket Message" );

            Message msg = new Message();
            msg.what = STATUS_MESSAGE;
            msg.obj = message;
            mWebSocketMessageHandler.sendMessage(msg);
        }
    }

    private enum DraftLevel{
        Draft10,
        Draft17,
        Draft75,
        Draft76,
    }
}
