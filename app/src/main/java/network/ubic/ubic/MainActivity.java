package network.ubic.ubic;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import network.ubic.ubic.BitiAndroid.AbstractNfcActivity;
import network.ubic.ubic.Fragments.BalanceFragment;
import network.ubic.ubic.Fragments.KycFragment;
import network.ubic.ubic.Fragments.MyUBIFragment;
import network.ubic.ubic.Fragments.NewPrivateKeyFragment;
import network.ubic.ubic.Fragments.PrivateKeyFragment;
import network.ubic.ubic.Fragments.ReadingPassportFragment;
import network.ubic.ubic.Fragments.ReceiveFragment;
import network.ubic.ubic.Fragments.RegisterPassportFragment;
import network.ubic.ubic.Fragments.SendFragment;
import network.ubic.ubic.Fragments.WaitForNfcFragment;
import network.ubic.ubic.Interfaces.QrCodeCallbackInterface;

public class MainActivity extends AbstractNfcActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                   BalanceFragment.OnFragmentInteractionListener,
                   SendFragment.OnFragmentInteractionListener,
                   ReceiveFragment.OnFragmentInteractionListener,
                   MyUBIFragment.OnFragmentInteractionListener,
                   RegisterPassportFragment.OnFragmentInteractionListener,
                   PrivateKeyFragment.OnFragmentInteractionListener,
                   WaitForNfcFragment.OnFragmentInteractionListener,
                   ReadingPassportFragment.OnFragmentInteractionListener,
                   KycFragment.OnFragmentInteractionListener,
                   NewPrivateKeyFragment.OnFragmentInteractionListener,
                   QrCodeCallbackInterface,
                   Serializable {

    private WaitForNfcFragment waitForNfcFragment;
    private List<Integer> currenciesInWallet;
    private IntentIntegrator qrScan;
    private QrCodeCallbackInterface qrcodeCallback;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("onCreate()");
        super.onCreate(savedInstanceState);

        qrScan = new IntentIntegrator(this);
        qrScan.setOrientationLocked(false);
        qrScan.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        qrScan.setPrompt("Scan the QrCode");
        qrScan.setCameraId(0);  // Use a specific camera of the device
        qrScan.setBeepEnabled(false);
        qrScan.setBarcodeImageEnabled(true);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = findViewById(R.id.qr_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // @TODO
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currenciesInWallet = new ArrayList<Integer>();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        PrivateKeyStore privateKeyStore = new PrivateKeyStore();

        try {
            String privateKeyStr = new String(privateKeyStore.getPrivateKey(getBaseContext()), "ASCII");

        } catch (Exception e) {
        }


        this.findViewById(R.id.qr_fab).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startQrCodeScan(MainActivity.this);
                    }
                }
        );

        this.goToNavBalance();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

        int id = item.getItemId();

        if (id == R.id.nav_balance) {
            goToNavBalance();
        } else if (id == R.id.nav_my_ubi) {
            goToNavMyUbi();
        } else if (id == R.id.nav_receive) {
            goToNavReceive();
        } else if (id == R.id.nav_send) {
            goToNavSend();
        } else if (id == R.id.nav_private_keys) {
            goToNavPrivateKey();
        } else if (id == R.id.nav_my_kyc) {
            goToNavKYC();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void goToNavRegisterPassport() {
        findViewById(R.id.qr_fab).setVisibility(View.GONE);
        RegisterPassportFragment rpFragment = new RegisterPassportFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentContainer, rpFragment);
        transaction.commit();
    }

    public void goToNavKYCPassport(String challenge) {
        findViewById(R.id.qr_fab).setVisibility(View.GONE);
        RegisterPassportFragment rpFragment = new RegisterPassportFragment();
        rpFragment.setChallenge(challenge);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentContainer, rpFragment);
        transaction.commitAllowingStateLoss();
    }

    public void goToNavKYC() {
        findViewById(R.id.qr_fab).setVisibility(View.GONE);
        KycFragment kycF = new KycFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentContainer, kycF);
        transaction.commitAllowingStateLoss();
    }

    public void goToNavNewPrivateKey() {
        findViewById(R.id.qr_fab).setVisibility(View.GONE);
        NewPrivateKeyFragment newPrivateKeyF = new NewPrivateKeyFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentContainer, newPrivateKeyF);
        transaction.commitAllowingStateLoss();
    }

    public void goToNavBalance() {
        findViewById(R.id.qr_fab).setVisibility(View.VISIBLE);
        BalanceFragment bf = new BalanceFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentContainer, bf);
        transaction.commit();
    }

    public void goToNavMyUbi() {
        findViewById(R.id.qr_fab).setVisibility(View.GONE);
        MyUBIFragment myUbiF = new MyUBIFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentContainer, myUbiF);
        transaction.commit();
    }

    public void goToNavSend() {
        findViewById(R.id.qr_fab).setVisibility(View.GONE);
        SendFragment sendF = new SendFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentContainer, sendF);
        transaction.commit();
    }

    public void goToNavSend(String address) {
        findViewById(R.id.qr_fab).setVisibility(View.GONE);
        SendFragment sendF = new SendFragment();
        sendF.setAddress(address);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentContainer, sendF);
        transaction.commit();
    }

    public void goToNavReceive() {
        findViewById(R.id.qr_fab).setVisibility(View.GONE);
        ReceiveFragment receiveF = new ReceiveFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentContainer, receiveF);
        transaction.commit();
    }

    public void goToNavPrivateKey() {
        findViewById(R.id.qr_fab).setVisibility(View.GONE);
        PrivateKeyFragment privateKeyF = new PrivateKeyFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentContainer, privateKeyF);
        transaction.commit();
    }

    public void goToNavWaitForNfc() {
        findViewById(R.id.qr_fab).setVisibility(View.GONE);
        waitForNfcFragment = WaitForNfcFragment.newInstance();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentContainer, waitForNfcFragment);
        transaction.commit();
    }

    public void goToNavReadingPassport() {
        findViewById(R.id.qr_fab).setVisibility(View.GONE);
        ReadingPassportFragment readingassportF = ReadingPassportFragment.newInstance();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentContainer, readingassportF);
        transaction.commit();
    }

    public void onNewIntent(Intent intent)
    {
        System.out.println("MainActivity onNewIntent");
        super.onNewIntent(intent);
        //((TextView)view.findViewById(R.id.placeYourDeviceInstructions)).setText(getResources().getString(R.string.found_nfc_text));
        //((TextView)view.findViewById(R.id.placeYourDeviceInstructions)).setGravity(Gravity.CENTER_HORIZONTAL);
        if(waitForNfcFragment != null) {
            waitForNfcFragment.readPassport();
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public List<Integer> getCurrenciesInWallet() {
        return currenciesInWallet;
    }

    public void setCurrenciesInWallet(List<Integer> currenciesInWallet) {
        this.currenciesInWallet = currenciesInWallet;
    }

    public void startQrCodeScan(QrCodeCallbackInterface qrcodeCallback) {
        qrScan.initiateScan();
        this.qrcodeCallback = qrcodeCallback;
    }

    //Getting the qr code scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                //result content is null
            } else {
                //if qr contains data
                qrcodeCallback.qrCodeResult(result.getContents());
            }
        } else {
        }
    }

    public void qrCodeResult(String qrcodeContent) {
        ChallengeParser challengeParser = new ChallengeParser(qrcodeContent);
        if(challengeParser.validateChallenge()) {
            // it is a KYC request
            goToNavKYCPassport(qrcodeContent);
        } else if(qrcodeContent.substring(0, 1).equals("\01")) {
            // it is a version 1 address
            goToNavSend(qrcodeContent);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.error_qr_code))
                    .setMessage(getResources().getString(R.string.error_unsupported))
                    .setNegativeButton(getResources().getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.cancel();
                                }
                            })
                    .setCancelable(true).create().show();
        }
    }
}
