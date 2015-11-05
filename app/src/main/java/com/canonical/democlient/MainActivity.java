package com.canonical.democlient;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.iotivity.base.ErrorCode;
import org.iotivity.base.ModeType;
import org.iotivity.base.ObserveType;
import org.iotivity.base.OcConnectivityType;
import org.iotivity.base.OcException;
import org.iotivity.base.OcHeaderOption;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcResource;
import org.iotivity.base.OcResourceIdentifier;
import org.iotivity.base.PlatformConfig;
import org.iotivity.base.QualityOfService;
import org.iotivity.base.ServiceType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements
        OcPlatform.OnResourceFoundListener,
        OcResource.OnGetListener,
        OcResource.OnPutListener,
        OcResource.OnPostListener,
        OcResource.OnObserveListener{

    private Map<OcResourceIdentifier, OcResource> mFoundResources = new HashMap<>();
    private OcResource mSensorResourceA = null;
    private DemoResource mDemo = new DemoResource();

    private final static String TAG = MainActivity.class.getSimpleName();

    private final static String sensor_name_temp = "(Arduino) Temperature sensor";
    private final static String sensor_name_light = "(Arduino) Light sensor";
    private final static String sensor_name_sound = "(Arduino) Sound sensor";

    private final static String led_name = "(Arduino) LED";
    private final static String lcd_name = "(Arduino) LCD";
    private final static String buzzer_name = "(Arduino) Buzzer";
    private final static String button_name_button = "(Arduino) Button";
    private final static String button_name_touch = "(Arduino) Touch";

    ArrayList<String> list_item;
    ArrayAdapter<String> list_adapter;
    private int found_devices = 0;

    private final static int DEV_UN_CONTROLLABLE = 0;
    private final static int DEV_CONTROLLABLE = 1;

    private void startDemoClient() {
        Context context = this;

        PlatformConfig platformConfig = new PlatformConfig(
                context,
                ServiceType.IN_PROC,
                ModeType.CLIENT,
                "0.0.0.0", // By setting to "0.0.0.0", it binds to all available interfaces
                0,         // Uses randomly available port
                QualityOfService.LOW
        );

        msg("Configuring platform.");
        OcPlatform.Configure(platformConfig);

        try {
            msg("Finding all resources of type \"grove.sensor\".");
            String requestUri = OcPlatform.WELL_KNOWN_QUERY + "?rt=grove.sensor";
            OcPlatform.findResource("",
                    requestUri,
                    EnumSet.of(OcConnectivityType.CT_DEFAULT),
                    this
            );

        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Failed to invoke find resource API");
        }

        printLine();
    }

    private void add_device() {
        if(found_devices != 0) {
            list_item.add("");
        }
    }
    /**
     * An event handler to be executed whenever a "findResource" request completes successfully
     *
     * @param ocResource found resource
     */
    @Override
    public synchronized void onResourceFound(OcResource ocResource) {
        if (null == ocResource) {
            msg("Found resource is invalid");
            return;
        }

        if (mFoundResources.containsKey(ocResource.getUniqueIdentifier())) {
            msg("Found a previously seen resource again!");
        } else {
            msg("Found resource for the first time on server with ID: " + ocResource.getServerId());
            mFoundResources.put(ocResource.getUniqueIdentifier(), ocResource);
        }

        // Get the resource URI
        String resourceUri = ocResource.getUri();
        // Get the resource host address
        String hostAddress = ocResource.getHost();
        msg("\tURI of the resource: " + resourceUri);
        msg("\tHost address of the resource: " + hostAddress);
        // Get the resource types
        msg("\tList of resource types: ");
        for (String resourceType : ocResource.getResourceTypes()) {
            msg("\t\t" + resourceType);
        }
        msg("\tList of resource interfaces:");
        for (String resourceInterface : ocResource.getResourceInterfaces()) {
            msg("\t\t" + resourceInterface);
        }
        msg("\tList of resource connectivity types:");
        for (OcConnectivityType connectivityType : ocResource.getConnectivityTypeSet()) {
            msg("\t\t" + connectivityType);
        }
        printLine();

        //In this example we are only interested in the light resources
        if (resourceUri.equals("/grove/sensor")) {
            if (mSensorResourceA != null) {
                msg("Found another sensor resource (Arduino), ignoring");
                return;
            }

            //Assign resource reference to a global variable to keep it from being
            //destroyed by the GC when it is out of scope.
            mSensorResourceA = ocResource;

            add_device();
            mDemo.setTempIndex(found_devices++);
            add_device();
            mDemo.setLightIndex(found_devices++);
            add_device();
            mDemo.setSoundIndex(found_devices++);
            //((BaseAdapter)((ListView)findViewById(R.id.listView)).getAdapter()).notifyDataSetChanged();


            // Call a local method which will internally invoke "get" API on the SensorResource
            getSensorResourceRepresentation();
        }
    }

    /**
     * Local method to get representation of a found sensor resource
     */
    private void getSensorResourceRepresentation() {
        msg("Getting Sensor Representation...");

        Map<String, String> queryParams = new HashMap<>();
        try {
            // Invoke resource's "get" API with a OcResource.OnGetListener event
            // listener implementation
            sleep(1);
            mSensorResourceA.get(queryParams, this);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Error occurred while invoking \"get\" API");
        }
    }

    /**
     * An event handler to be executed whenever a "get" request completes successfully
     *
     * @param list             list of the header options
     * @param ocRepresentation representation of a resource
     */
    @Override
    public synchronized void onGetCompleted(List<OcHeaderOption> list,
                                            OcRepresentation ocRepresentation) {
        msg("GET request was successful");
        msg("Resource URI: " + ocRepresentation.getUri());

        try {
            //Read attribute values into local representation of a light
            mDemo.sensorSetOcRepresentation(ocRepresentation);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Failed to read the attributes of a light resource");
        }
        msg("Sensor attributes: ");
        msg(String.valueOf(mDemo.getTemp()));
        msg(String.valueOf(mDemo.getLight()));
        msg(String.valueOf(mDemo.getSound()));
        String str = sensor_name_temp + ": " + String.valueOf(mDemo.getTemp());
        list_item.set(mDemo.getTempIndex(), str);
        str = sensor_name_light + ": " + String.valueOf(mDemo.getLight());
        list_item.set(mDemo.getLightIndex(), str);
        str = sensor_name_sound + ": " + String.valueOf(mDemo.getSound());
        list_item.set(mDemo.getSoundIndex(), str);
        ((BaseAdapter) ((ListView)findViewById(R.id.listView)).getAdapter()).notifyDataSetChanged();

        printLine();

        //Call a local method which will internally invoke put API on the foundLightResource
        //putSensorRepresentation();
    }

    /**
     * An event handler to be executed whenever a "get" request fails
     *
     * @param throwable exception
     */
    @Override
    public synchronized void onGetFailed(Throwable throwable) {
        if (throwable instanceof OcException) {
            OcException ocEx = (OcException) throwable;
            Log.e(TAG, ocEx.toString());
            ErrorCode errCode = ocEx.getErrorCode();
            //do something based on errorCode
            msg("Error code: " + errCode);
        }
        msg("Failed to get representation of a found sensor resource");
    }

    /**
     * Local method to put a different state for this light resource
     */
    private void putSensorRepresentation() {
        //set new values
        //mDemo.setLed();
        //mDemo.setPower(15);

        msg("Putting LED representation...");
        OcRepresentation representation = null;
        try {
            representation = mDemo.getOcRepresentation();
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Failed to get OcRepresentation from sensors");
        }

        Map<String, String> queryParams = new HashMap<>();

        try {
            sleep(1);
            // Invoke resource's "put" API with a new representation, query parameters and
            // OcResource.OnPutListener event listener implementation
            mSensorResourceA.put(representation, queryParams, this);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Error occurred while invoking \"put\" API");
        }
    }

    /**
     * An event handler to be executed whenever a "put" request completes successfully
     *
     * @param list             list of the header options
     * @param ocRepresentation representation of a resource
     */
    @Override
    public synchronized void onPutCompleted(List<OcHeaderOption> list, OcRepresentation ocRepresentation) {
        msg("PUT request was successful");
        try {
            mDemo.sensorSetOcRepresentation(ocRepresentation);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Failed to create Light representation");
        }
        msg("Sensor attributes: ");
        msg(String.valueOf(mDemo.getTemp()));
        printLine();

        //Call a local method which will internally invoke post API on the foundLightResource
        //postSensorRepresentation();
    }

    /**
     * An event handler to be executed whenever a "put" request fails
     *
     * @param throwable exception
     */
    @Override
    public synchronized void onPutFailed(Throwable throwable) {
        if (throwable instanceof OcException) {
            OcException ocEx = (OcException) throwable;
            Log.e(TAG, ocEx.toString());
            ErrorCode errCode = ocEx.getErrorCode();
            //do something based on errorCode
            msg("Error code: " + errCode);
        }
        msg("Failed to \"put\" a new representation");
    }

    /**
     * Local method to post a different state for this light resource
     */
    private void postLightRepresentation() {
        //set new values
        //mDemo.setState(false);
        //mDemo.setPower(105);

        msg("Posting light representation...");
        OcRepresentation representation = null;
        try {
            representation = mDemo.getOcRepresentation();
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Failed to get OcRepresentation from a light");
        }

        Map<String, String> queryParams = new HashMap<>();
        try {
            sleep(1);
            // Invoke resource's "post" API with a new representation, query parameters and
            // OcResource.OnPostListener event listener implementation
            mSensorResourceA.post(representation, queryParams, this);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Error occurred while invoking \"post\" API");
        }
    }

    /**
     * An event handler to be executed whenever a "post" request completes successfully
     *
     * @param list             list of the header options
     * @param ocRepresentation representation of a resource
     */
    @Override
    public synchronized void onPostCompleted(List<OcHeaderOption> list,
                                             OcRepresentation ocRepresentation) {
        msg("POST request was successful");
        try {
            if (ocRepresentation.hasAttribute(OcResource.CREATED_URI_KEY)) {
                msg("\tUri of the created resource: " +
                        ocRepresentation.getValue(OcResource.CREATED_URI_KEY));
            } else {
                mDemo.sensorSetOcRepresentation(ocRepresentation);
                //msg(mDemo.toString());
            }
        } catch (OcException e) {
            Log.e(TAG, e.toString());
        }

        //setting new values
        //mDemo.setState(true);
        //mDemo.setPower(55);
        msg("Posting again light representation...");
        OcRepresentation representation2 = null;
        try {
            representation2 = mDemo.getOcRepresentation();
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Failed to get OcRepresentation from a light");
        }

        Map<String, String> queryParams = new HashMap<>();
        try {
            // Invoke resource's "post" API with a new representation, query parameters and
            // OcResource.OnPostListener event listener implementation
            mSensorResourceA.post(representation2, queryParams, onPostListener2);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Error occurred while invoking \"post\" API");
        }
    }

    /**
     * An event handler to be executed whenever a "post" request fails
     *
     * @param throwable exception
     */
    @Override
    public synchronized void onPostFailed(Throwable throwable) {
        if (throwable instanceof OcException) {
            OcException ocEx = (OcException) throwable;
            Log.e(TAG, ocEx.toString());
            ErrorCode errCode = ocEx.getErrorCode();
            //do something based on errorCode
            msg("Error code: " + errCode);
        }
        msg("Failed to \"post\" a new representation");
    }

    /**
     * Declare and implement a second OcResource.OnPostListener
     */
    OcResource.OnPostListener onPostListener2 = new OcResource.OnPostListener() {
        /**
         * An event handler to be executed whenever a "post" request completes successfully
         * @param list             list of the header options
         * @param ocRepresentation representation of a resource
         */
        @Override
        public synchronized void onPostCompleted(List<OcHeaderOption> list,
                                                 OcRepresentation ocRepresentation) {
            msg("Second POST request was successful");
            try {
                if (ocRepresentation.hasAttribute(OcResource.CREATED_URI_KEY)) {
                    msg("\tUri of the created resource: " +
                            ocRepresentation.getValue(OcResource.CREATED_URI_KEY));
                } else {
                    mDemo.sensorSetOcRepresentation(ocRepresentation);
                    msg(mDemo.toString());
                }
            } catch (OcException e) {
                Log.e(TAG, e.toString());
            }

            //Call a local method which will internally invoke observe API on the foundLightResource
            observeFoundLightResource();
        }

        /**
         * An event handler to be executed whenever a "post" request fails
         *
         * @param throwable exception
         */
        @Override
        public synchronized void onPostFailed(Throwable throwable) {
            if (throwable instanceof OcException) {
                OcException ocEx = (OcException) throwable;
                Log.e(TAG, ocEx.toString());
                ErrorCode errCode = ocEx.getErrorCode();
                //do something based on errorCode
                msg("Error code: " + errCode);
            }
            msg("Failed to \"post\" a new representation");
        }
    };

    /**
     * Local method to start observing this light resource
     */
    private void observeFoundLightResource() {
        try {
            sleep(1);
            // Invoke resource's "observe" API with a observe type, query parameters and
            // OcResource.OnObserveListener event listener implementation
            mSensorResourceA.observe(ObserveType.OBSERVE, new HashMap<String, String>(), this);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Error occurred while invoking \"observe\" API");
        }
    }

    // holds current number of observations
    private static int mObserveCount = 0;

    /**
     * An event handler to be executed whenever a "post" request completes successfully
     *
     * @param list             list of the header options
     * @param ocRepresentation representation of a resource
     * @param sequenceNumber   sequence number
     */
    @Override
    public synchronized void onObserveCompleted(List<OcHeaderOption> list,
                                                OcRepresentation ocRepresentation,
                                                int sequenceNumber) {
        if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
            msg("Observe registration action is successful:");
        } else if (OcResource.OnObserveListener.DEREGISTER == sequenceNumber) {
            msg("Observe De-registration action is successful");
        } else if (OcResource.OnObserveListener.NO_OPTION == sequenceNumber) {
            msg("Observe registration or de-registration action is failed");
        }

        msg("OBSERVE Result:");
        msg("\tSequenceNumber:" + sequenceNumber);
        try {
            mDemo.sensorSetOcRepresentation(ocRepresentation);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Failed to get the attribute values");
        }
        msg(String.valueOf(mDemo.getTemp()));

        if (++mObserveCount == 11) {
            msg("Cancelling Observe...");
            try {
                mSensorResourceA.cancelObserve();
            } catch (OcException e) {
                Log.e(TAG, e.toString());
                msg("Error occurred while invoking \"cancelObserve\" API");
            }
            msg("DONE");

            //prepare for the next restart of the SimpleClient
            resetGlobals();
            //enableStartButton();
        }
    }

    /**
     * An event handler to be executed whenever a "observe" request fails
     *
     * @param throwable exception
     */
    @Override
    public synchronized void onObserveFailed(Throwable throwable) {
        if (throwable instanceof OcException) {
            OcException ocEx = (OcException) throwable;
            Log.e(TAG, ocEx.toString());
            ErrorCode errCode = ocEx.getErrorCode();
            //do something based on errorCode
            msg("Error code: " + errCode);
        }
        msg("Observation of the found light resource has failed");
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    private void msg(final String text) {
        runOnUiThread(new Runnable() {
            public void run() {
                mConsoleTextView.append("\n");
                mConsoleTextView.append(text);
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
        Log.i(TAG, text);
    }

    private void printLine() {
        msg("------------------------------------------------------------------------");
    }

    private synchronized void resetGlobals() {
        mSensorResourceA = null;
        mFoundResources.clear();
        mDemo = new DemoResource();
        mObserveCount = 0;
    }

    private TextView mConsoleTextView;
    private ScrollView mScrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConsoleTextView = (TextView) findViewById(R.id.consoleTextView);
        mConsoleTextView.setMovementMethod(new ScrollingMovementMethod());
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mScrollView.fullScroll(View.FOCUS_DOWN);
        final Button button = (Button) findViewById(R.id.button_findserver);

        ListView listview = (ListView)findViewById(R.id.listView);

        list_item = new ArrayList<String>();
        list_item.add("");

        list_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                list_item);
        listview.setAdapter(list_adapter);



        if (null == savedInstanceState) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    button.setText("Re-start");
                    button.setEnabled(false);
                    new Thread(new Runnable() {
                        public void run() {
                            startDemoClient();
                        }
                    }).start();
                }
            });
        } else {
            String consoleOutput = savedInstanceState.getString("consoleOutputString");
            mConsoleTextView.setText(consoleOutput);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
