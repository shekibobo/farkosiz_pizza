package farkosizPizza.OrderCenter;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
//import android.widget.TextView;	//uncomment for debugging

public class Dashboard extends Activity {

	private ImageButton newOrderHomeButton;
	private ImageButton recentPizzasHomeButton;
    private ImageButton cancelButton;
    private ImageButton addButton;
   
    DatabaseHelper db = new DatabaseHelper(this);
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_main);
        
        
        //TextView testText = (TextView)this.findViewById(R.id.test_text);
        //String str = "";
        
        
        //create and migrate the database
        try {
        	db.createDatabase();
        } catch (IOException ioe) {
        	throw new Error("Unable to create database");
        }
        try {
        	db.openDatabase();
        } catch (SQLException sqle) {
        	throw sqle;
        }
        
        //enable action of cancelButton
        this.cancelButton = (ImageButton)this.findViewById(R.id.cancel_button);
        this.cancelButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		db.close();
        		finish();
        	}
        });
        
        this.newOrderHomeButton = (ImageButton)this.findViewById(R.id.new_order_button);
        this.newOrderHomeButton.setOnClickListener(newOrderListener);
        
        this.addButton = (ImageButton)this.findViewById(R.id.add_button);
        this.addButton.setOnClickListener(newOrderListener);
        
        this.recentPizzasHomeButton = (ImageButton)this.findViewById(R.id.recent_pizzas_button);
        this.recentPizzasHomeButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				startActivity(new Intent(Dashboard.this, RecentPizzas.class));
				
			}
        	
        });
        
        
        /*//code for debugging: displays content in testText
        
        int order_id = db.createNewOrder();
        str += "o_id=" + order_id + " : ";
        if (order_id > 0) {
        	try {
		        int[] toppings = {1, 0, 2, 0, 3, 3};
		        int[] toppings2 = {3, 3, 3, 3, 0, 0};
		        int pizza1 = db.createNewPizza(order_id, 1, 2, 2, toppings);
		        str += "p1_id=" + pizza1 + " : ";
		        int pizza2 = db.createNewPizza(order_id, 2, 3, 1, toppings2);
		        str += "p2_id=" + pizza2 + " : ";
		        
		        int[] pizzaList = db.getPizzasForOrder(order_id);	//throws exception
		        if (pizzaList != null) {
		        	testText.setText(str + db.getToppingName(db.getPizzaToppingsForPosition(pizzaList[0], 1)[0]));
		        }
		        else testText.setText(db.getUserName(order_id));
        	} catch (Exception e) {
        		testText.setText(str + e.getMessage());
        	}
        }
        else testText.setText(str);
        */
    }
    
    private OnClickListener newOrderListener = new OnClickListener() {
    	public void onClick(View v) {
    		//this will launch the addNewPizza activity
    		int order_id = db.createNewOrder();
    		db.close();
    		Intent intent = new Intent(Dashboard.this, NewPizza.class);
    		intent.putExtra("order_id", order_id);
    		startActivity(intent);
    	}
    };
    
    @Override
    public void onPause() {
    	super.onPause();
    	db.close();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	db.openDatabase();
    }
    
    @Override
    public void finish() {
    	super.finish();
    	db.close();
    }
    
}