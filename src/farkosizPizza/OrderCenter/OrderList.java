package farkosizPizza.OrderCenter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class OrderList extends Activity {
	private ImageButton cancelButton;
	private ImageButton confirmButton;
	private ImageButton homeButton;
	private ImageButton addButton;
	private ImageButton cancelPizzaButton;
	private TextView title;
	private String username;
	private EditText username_field;
	private LinearLayout ll;
	private LinearLayout pzCont;
	
	private int order_id;
	private DatabaseHelper db = new DatabaseHelper(this);
	
	private final int SIZE = 0;
	private final int CRUST = 1; 
	
	//size constants
	private final int SMALL = 0;
	private final int MEDIUM = 1;
	private final int LARGE = 2;
	private final int XLARGE = 3;
	
	//crust constants
	private final int THIN = 0;
	private final int THICK = 1;
	private final int DEEP = 2;
	private final int STUFFED = 3;
	
	//topping location constants
	private final int NONE = 0;
	private final int RIGHT = 1;
	private final int LEFT = 2;
	private final int WHOLE = 3;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_order_list);
        
        try {
        	db.openDatabase();
        } catch (SQLException sqle) {
        	throw sqle;
        }
        
        Intent intent = getIntent();
        order_id = intent.getIntExtra("order_id", -1);
        
        username = db.getUserName(order_id);
        
        //this will build our success message when it's time
		final AlertDialog.Builder successBuilder = new AlertDialog.Builder(this);

        //set the title bar text
        title = (TextView)this.findViewById(R.id.title);
        title.setText(this.getText(R.string.order_view));
        
        this.username_field = (EditText) this.findViewById(R.id.username);
        username_field.setText(db.getUserName(order_id));

        
        //button will cancel current order and return
        //to the home screen
        this.homeButton = (ImageButton)this.findViewById(R.id.home_button);
        this.homeButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		if (db.getPizzasForOrder(order_id) == null) {
        			db.deleteOrder(order_id);
        		}
        		finish();
        	}
        });
        
        //enable action of cancelButton
        this.cancelButton = (ImageButton)this.findViewById(R.id.cancel_button);
        this.cancelButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		if (db.getPizzasForOrder(order_id).length == 0) {
        			db.deleteOrder(order_id);
        		}
        		finish();
        	}
        });
        //commit data in all fields to current order
        this.confirmButton = (ImageButton)this.findViewById(R.id.okay_button);
        this.confirmButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		//send the information to the site, notify the user their order
        		//was successful
        		username = username_field.getText().toString();
                successBuilder.setMessage(
                		(String)getText(R.string.thanks) + " " +
                		username +
                		(String)getText(R.string.confirm))
                	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialog, int which) {
        					// TODO Auto-generated method stub
        					OrderList.this.finish();
        				}
        			});
        		final AlertDialog success = successBuilder.create();
        		success.show();
        	}
        });
        
        this.addButton = (ImageButton)this.findViewById(R.id.add_button);
        this.addButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		//this will launch the addNewPizza activity
        		db.setUserName(order_id, username_field.getText().toString());
        		db.close();
        		Intent intent = new Intent(OrderList.this, NewPizza.class);
        		intent.putExtra("order_id", order_id);
        		startActivity(intent);
        		finish();
        	}
        });
        
        
        
        
        
        

        
        
        
        
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(
          	Context.LAYOUT_INFLATER_SERVICE);
        
        ll = (LinearLayout)this.findViewById(R.id.order_list);
        
        int[] pizzas = db.getPizzasForOrder(order_id);
        
        
        OnClickListener deleteListener = new OnClickListener() {

			public void onClick(View v) {
				//delete pizza from db
				db.deletePizza(((View) v.getParent()).getId());
				//remove the pizza's view form the container
				ll.removeView((View)v.getParent().getParent());
				
			}
        	
        };
        // Cycle through all pizzas
        for(int i = 0; i < pizzas.length; i++) {
        	//this will the the container for each pizza.  Once all the views are loaded into it,
        	//we'll add it to the main view
            pzCont = (LinearLayout) inflater.inflate(R.layout.pizza_container, null); 
            pzCont.setId(pizzas[i]);
            
        	int size = db.getPizzaAttribute(pizzas[i], SIZE);
        	int crust = db.getPizzaAttribute(pizzas[i], CRUST);
        	
        	RelativeLayout pizzaHeader = (RelativeLayout) inflater.inflate(R.layout.pizza_header, null);
        	
        	if (pizzaHeader != null){
	        	pizzaHeader.setId(pizzas[i]);
	        	TextView pizzaNum = (TextView) pizzaHeader.getChildAt(0);
	        	pizzaNum.setText("Pizza " + (i+1) + "\t\tx" + db.getPizzaQty(order_id, pizzas[i]));
	        	ImageButton deletePizzaBtn = (ImageButton) pizzaHeader.getChildAt(3);
	        	deletePizzaBtn.setOnClickListener(deleteListener);
        	}
        	pzCont.addView(pizzaHeader);
        	
        	TextView pizzaSizeHeading = (TextView) inflater.inflate(R.layout.list_subheading, null);
        	pizzaSizeHeading.setText(R.string.size);
        	pzCont.addView(pizzaSizeHeading);
        	
        	TextView pizzaSize = (TextView) inflater.inflate(R.layout.pizza_list_item, null);
        	pizzaSize.setText(findSize(size));
        	pzCont.addView(pizzaSize);
        	
        	TextView pizzaCrustHeading = (TextView) inflater.inflate(R.layout.list_subheading, null);
        	pizzaCrustHeading.setText(R.string.crust);
        	pzCont.addView(pizzaCrustHeading);
        	
        	TextView pizzaCrust = (TextView) inflater.inflate(R.layout.pizza_list_item, null);
        	pizzaCrust.setText(findCrust(crust));
        	pzCont.addView(pizzaCrust);
        	
        	TextView toppingHeading = (TextView) inflater.inflate(R.layout.list_subheading, null);
    		toppingHeading.setText(R.string.t_coverage);
    		pzCont.addView(toppingHeading);
    		
    		
    		boolean hasToppings = false;
    		
    		// Cycle through all positions of toppings
        	for(int j = 3; j > -1; j--) {
        		int[] toppings = db.getPizzaToppingsForPosition(pizzas[i], j);
        		
        		if(toppings.length > 0) {
        			
        			hasToppings = true;
        			
	        		TextView locationHeading = (TextView) inflater.inflate(R.layout.list_subheading2, null);
	        		
	        		
	        		switch(j) {
	        		case (NONE):
	        			break;
	        		case (RIGHT):
	        			locationHeading.setText(R.string.half1);
	        			break;
	        		case (LEFT):
	        			locationHeading.setText(R.string.half2);
	        			break;
	        		case (WHOLE):
	        			locationHeading.setText(R.string.full);
	        			break;
	        		}
	        		
	        		pzCont.addView(locationHeading);
	        		
	        		// List all toppings for location
	        		for(int k = 0; k < toppings.length; k++) {
	        			
	        			TextView topping = (TextView) inflater.inflate(R.layout.pizza_list_item, null);
	        			topping.setText(db.getToppingName(toppings[k]));
	        			pzCont.addView(topping);
	        			
	        		}
        		}
        	} 
        	if(!hasToppings) {
    			TextView topping = (TextView) inflater.inflate(R.layout.pizza_list_item, null);
    			topping.setText(R.string.cheese);
    			pzCont.addView(topping);
    		}
        	ll.addView(pzCont);
        }
        
        
    }
    
    
    
    private String findSize(int i) {
    	String size = "";
    	
    	switch(i) {
		case (SMALL):
			size = "Small";
			break;
		case (MEDIUM):
			size = "Medium";
			break;
		case (LARGE):
			size = "Large";
			break;
		case (XLARGE):
			size = "Extra Large";
			break;
		}
    	
    	return size;
    }
    
    
    
    private String findCrust(int i) {
    	String crust = "";
    	
    	switch(i) {
		case (THIN):
			crust = "Thin Crust";
			break;
		case (THICK):
			crust = "Thick Crust";
			break;
		case (DEEP):
			crust = "Deep Dish";
			break;
		case (STUFFED):
			crust = "Stuffed Crust";
			break;
		}
    	
    	return crust;
    }
    
    @Override
    public void finish() {
    	super.finish();
    	db.close();
    }
    
    
}
