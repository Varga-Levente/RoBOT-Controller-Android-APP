package hu.unideb.vlevente.robotcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    //* Global variables
    private static final String TAG = "MainActivity";

    private final String APP_VERSION = "1.1 Rev.: 0012";
    private final String APP_DEFAULT_IP = "0.0.0.0";

    //* UI elements
    Button btnCtrl, gitUrlBtn, btnQuickConnect;
    EditText ipInput;
    TextView version;

    //! This checkbox is for development purposes only and should be removed in the final version
    CheckBox devMode;
    boolean devModeState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCtrl = findViewById(R.id.btnCtrl);
        btnQuickConnect = findViewById(R.id.btnQuickConnect);

        gitUrlBtn = findViewById(R.id.gitUrl);
        ipInput = findViewById(R.id.botIP);
        version = findViewById(R.id.version);

        devMode = findViewById(R.id.devmode);

        version.setText(version.getText().toString().replace("#.#", APP_VERSION));

        //* Button control click
        btnCtrl.setOnClickListener(v -> {
            if (validateIP(ipInput.getText().toString())) {
                ipInput.setError("Invalid IP address");
                return;
            }
            openButtonControl();
        });

        //* GitHub button click
        gitUrlBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse("https://github.com/Varga-Levente/RoBOT"));
            startActivity(intent);
        });

        //* DevMode checkbox
        devMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                devModeState = true;
                Toast.makeText(getApplicationContext(), "Development mode enabled", Toast.LENGTH_SHORT).show();
                ipInput.setText("Development mode enabled");
                ipInput.setEnabled(false);
            } else {
                devModeState = false;
                Toast.makeText(getApplicationContext(), "Development mode disabled", Toast.LENGTH_SHORT).show();
                ipInput.setText(APP_DEFAULT_IP);
                ipInput.setEnabled(true);
            }
        });

        //* Quick connect button (automatikus hotspot subnet detektálás + párhuzamos scan)
        btnQuickConnect.setOnClickListener(v -> {
            btnQuickConnect.setEnabled(false);
            Log.d(TAG, "QuickConnect started: scanning network...");
            Toast.makeText(getApplicationContext(), "Scanning network...", Toast.LENGTH_SHORT).show();

            new Thread(() -> {
                try {
                    String subnet = detectSubnetAutomatic();
                    Log.d(TAG, "Detected subnet: " + subnet);
                    if (subnet == null) {
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), "Cannot determine subnet", Toast.LENGTH_SHORT).show();
                            btnQuickConnect.setEnabled(true);
                        });
                        return;
                    }

                    final AtomicReference<String> foundIp = new AtomicReference<>(null);
                    ExecutorService pool = Executors.newFixedThreadPool(30);

                    for (int i = 1; i <= 254; i++) {
                        final String candidate = subnet + i;
                        if (foundIp.get() != null) break;

                        pool.submit(() -> {
                            if (foundIp.get() != null) return;
                            String urlStr = "http://" + candidate + "/check";
                            HttpURLConnection connection = null;
                            try {
                                Log.d(TAG, "Trying " + urlStr);
                                URL url = new URL(urlStr);
                                connection = (HttpURLConnection) url.openConnection();
                                connection.setConnectTimeout(350);
                                connection.setReadTimeout(350);
                                connection.setRequestMethod("GET");
                                int rc = connection.getResponseCode();
                                Log.d(TAG, candidate + " HTTP response: " + rc);
                                if (rc == 200) {
                                    InputStream is = connection.getInputStream();
                                    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                                    String response = s.hasNext() ? s.next() : "";
                                    is.close();
                                    Log.d(TAG, candidate + " response: " + response.trim());
                                    if ("LIVE".equalsIgnoreCase(response.trim())) {
                                        if (foundIp.compareAndSet(null, candidate)) {
                                            Log.d(TAG, "Device found at " + candidate);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.d(TAG, "Error connecting to " + candidate + ": " + e.toString());
                            } finally {
                                if (connection != null) connection.disconnect();
                            }
                        });
                    }

                    pool.shutdown();
                    long waitUntil = System.currentTimeMillis() + 10_000;
                    while (!pool.isTerminated() && System.currentTimeMillis() < waitUntil) {
                        if (foundIp.get() != null) {
                            Log.d(TAG, "Device found, shutting down pool");
                            pool.shutdownNow();
                            break;
                        }
                        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                    }
                    if (!pool.isTerminated()) {
                        Log.d(TAG, "Pool timeout reached, forcing shutdown");
                        pool.shutdownNow();
                    }

                    final String resultIp = foundIp.get();
                    runOnUiThread(() -> {
                        btnQuickConnect.setEnabled(true);
                        if (resultIp != null) {
                            Log.d(TAG, "Opening ButtonControl with IP: " + resultIp);
                            ipInput.setText(resultIp);
                            openButtonControl();
                        } else {
                            Log.d(TAG, "No device found on network");
                            Toast.makeText(getApplicationContext(), "No device found on network", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    Log.d(TAG, "QuickConnect exception: " + e.toString());
                    runOnUiThread(() -> {
                        btnQuickConnect.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });
    }

    public void openButtonControl(){
        Intent intent = new Intent(this, ButtonControl.class);
        String ipAddress = devMode.isChecked() ? "127.0.0.1" : ipInput.getText().toString();
        intent.putExtra("IP_ADDRESS", ipAddress);
        intent.putExtra("VERSION", APP_VERSION);
        intent.putExtra("DEV_MODE", devModeState);
        startActivity(intent);
    }

    public boolean validateIP(String IP){
        if (devMode.isChecked()) return false;
        return !IP.matches("^(10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|(172\\.(1[6-9]|2[0-9]|3[0-1])\\.\\d{1,3}\\.\\d{1,3})|(192\\.168\\.\\d{1,3}\\.\\d{1,3})$");
    }

    /**
     * Próbál automatikusan meghatározni a helyi /24 subnet előtagot.
     * 1) NetworkInterface-ekből keres privát IPv4 címet
     * 2) WifiManager DHCP gateway
     * 3) WifiManager connection IP
     * 4) fallback list of common hotspot gateways
     * Visszaad: pl. "192.168.135."
     */
    private String detectSubnetAutomatic() {
        // 1) Network interfaces
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface ni : Collections.list(interfaces)) {
                if (!ni.isUp() || ni.isLoopback()) continue;
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                for (InetAddress addr : Collections.list(addrs)) {
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();
                        Log.d(TAG, "Interface IP found: " + ip);
                        if (isPrivateIPv4(ip)) {
                            Log.d(TAG, "Using subnet from interface IP: " + ip);
                            return ip.substring(0, ip.lastIndexOf('.') + 1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Error checking interfaces: " + e.toString());
        }

        // 2) DHCP gateway via WifiManager
        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                android.net.DhcpInfo dhcp = wm.getDhcpInfo();
                if (dhcp != null) {
                    int gw = dhcp.gateway;
                    if (gw != 0) {
                        String gwIp = Formatter.formatIpAddress(gw);
                        Log.d(TAG, "DHCP gateway found: " + gwIp);
                        if (isPrivateIPv4(gwIp)) {
                            return gwIp.substring(0, gwIp.lastIndexOf('.') + 1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Error getting DHCP gateway: " + e.toString());
        }

        // fallback common hotspot gateways
        String[] common = new String[] {"192.168.43.", "192.168.44.", "192.168.1.", "192.168.135."};
        for (String s : common) {
            Log.d(TAG, "Trying common subnet fallback: " + s);
            return s;
        }

        Log.d(TAG, "No subnet detected");
        return null;
    }

    private boolean isPrivateIPv4(String ip) {
        if (ip == null) return false;
        // gyors regex a privát tartományokra
        return ip.matches("^(10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|(172\\.(1[6-9]|2[0-9]|3[0-1])\\.\\d{1,3}\\.\\d{1,3})|(192\\.168\\.\\d{1,3}\\.\\d{1,3})$");
    }
}