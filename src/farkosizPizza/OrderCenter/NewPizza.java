package farkosizPizza.OrderCenter;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class NewPizza extends Activity {
	
	private DatabaseHelper db = new DatabaseHelper(this);
	
	private ImageButton cancelButton;
	private ImageButton confirmButton;
	private ImageButton homeButton;
	private TextView title;
	private RadioGroup sizeSelector;
	private RadioGroup crustSelector;
	private LinearLayout ll;
	//private TextView testText;
	
	private Button qtyLessBtn;
	private Button qtyMoreBtn;
	private EditText quantityBox;
	
	private int qty;
	private int order_id;
	private int size;
	private int crust;
	private ArrayList<Topping> toppingAList;
	int[] toppings;
	
	
	
	
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
        setContentView(R.layout.main_new_pizza);
        
        qty = 1;	//default minimum value
        
        try {
        	db.openDatabase();
        } catch (SQLException sqle) {
        	throw sqle;
        }
        
        
        Intent intent = getIntent();
        order_id = intent.getIntExtra("order_id", -1);
        
        title = (TextView)this.findViewById(R.id.title);
        title.setText(this.getText(R.string.new_pizza));
        
        //set up the properties of the quantity selector
        this.quantityBox = (EditText) this.findViewById(R.id.pizzaQuantity);
        this.quantityBox.setText(" " + qty);
        
        OnClickListener qtyListener = new OnClickListener() {
			public void onClick(View v) {
				switch(v.getId()) {
				case(R.id.quantity_less):
					if (qty == 1) {
						//don't change it
					} else {
						qty -= 1;
						quantityBox.setText("" + qty);
					}
					break;
				case(R.id.quantity_more):
					qty += 1;
					quantityBox.setText("" + qty);
					break;
				}
			}
        	
        };

        
        
        this.qtyLessBtn = (Button) this.findViewById(R.id.quantity_less);
        this.qtyLessBtn.setOnClickListener(qtyListener);
        this.qtyMoreBtn = (Button) this.findViewById(R.id.quantity_more);
        this.qtyMoreBtn.setOnClickListener(qtyListener);
        
        
        //set default position for toppings
        this.toppings = new int[db.getToppingList().length];
        for (int t : toppings) {
        	t = NONE;
        }
        
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(
        		Context.LAYOUT_INFLATER_SERVICE);
        
        ll = (LinearLayout)this.findViewById(R.id.new_pizza_form);
        toppingAList = db.getToppingAList();
        
        final int toppingRootId = 0;
        int[] toppingsId = new int[toppingAList.size()];
        for (int i = 0; i < toppings.length; i++) {
        	toppingsId[i] = toppingRootId + i;
        }
        
        OnClickListener toppingClickListener = new OnClickListener() {
			public void onClick(View v) {
				RelativeLayout rl = (RelativeLayout)v;
				ImageView iv = (ImageView)rl.getChildAt(1);
				int id = rl.getId();
				toppingAList.get(id).nextPosition();
				toppings[id] = toppingAList.get(id).getToppingPosition();
				switch(toppingAList.get(id).getToppingPosition()) {
				case(NONE):
					iv.setImageResource(R.drawable.topping_position_none);
					break;
				case(RIGHT):
					iv.setImageResource(R.drawable.topping_position_right);
					break;
				case(LEFT):
					iv.setImageResource(R.drawable.topping_position_left);
					break;
				case(WHOLE):
					iv.setImageResource(R.drawable.topping_position_whole);
					break;
				}
				
			}

        };
        
        
        
        for (int i = 0; i < toppingAList.size(); i++) {
        	 RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.topping_selection_item, null);

        	if (rl != null){
	        	rl.setId(toppingRootId + i);
	        	TextView tv = (TextView) rl.getChildAt(0);
	        	tv.setText(toppingAList.get(i).getToppingName());
	        	ImageView iv = (ImageView) rl.getChildAt(1);
	        	switch(toppingAList.get(i).getToppingPosition()) {
				case(NONE):
					iv.setImageResource(R.drawable.topping_position_none);
					break;
				case(RIGHT):
					iv.setImageResource(R.drawable.topping_position_right);
					break;
				case(LEFT):
					iv.setImageResource(R.drawable.topping_position_left);
					break;
				case(WHOLE):
					iv.setImageResource(R.drawable.topping_position_whole);
					break;
				}
	        	rl.setOnClickListener(toppingClickListener);
	        	
	        	
	        	ll.addView(rl);
        	}
        }

        //testText = (TextView)this.findViewById(R.id.form_test_text);

        
        this.sizeSelector = (RadioGroup)this.findViewById(R.id.size_selector);
        this.crustSelector = (RadioGroup)this.findViewById(R.id.crust_selector);
        
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
        			finish();
        		}
        		else {
        			db.close();
            		Intent intent = new Intent(NewPizza.this, OrderList.class);
            		intent.putExtra("order_id", order_id);
            		startActivity(intent);
            		finish();
        		}
        		
        	}
        });
        //commit data in all fields to current order
        this.confirmButton = (ImageButton)this.findViewById(R.id.okay_button);
        this.confirmButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		/*//debugging code
        		String debug_output = "";
        		
        		setSize(sizeSelector.getCheckedRadioButtonId());
        		debug_output += "size: " + size + " | ";
        		setCrust(crustSelector.getCheckedRadioButtonId());
        		debug_output += "crust: " + crust + " | ";
        		debug_output += "top_loc: ";
        		for (int t : toppings) {
        			debug_output += t + " ";
        		}
        		testText.setText(debug_output);
        		*/ //end debugging code
        		
        		//commit data to storage unless there is an error in the input
        		setSize(sizeSelector.getCheckedRadioButtonId());
        		setCrust(crustSelector.getCheckedRadioButtonId());
        		db.createNewPizza(order_id, size, crust, qty, toppings);

        		db.close();
        		Intent intent = new Intent(NewPizza.this, OrderList.class);
        		intent.putExtra("order_id", order_id);
        		startActivity(intent);
        		finish();
        		
        	}
        });
        


        
        
        
        
    }
    
    private void setSize(int selectedSizeId) {
    	switch(selectedSizeId) {
		case (R.id.btn_small_pizza):
			this.size = SMALL;
			break;
		case (R.id.btn_medium_pizza):
			this.size = MEDIUM;
			break;
		case (R.id.btn_large_pizza):
			this.size = LARGE;
			break;
		case (R.id.btn_xlarge_pizza):
			this.size = XLARGE;
			break;
		}
    }
    
    private void setCrust(int selectedCrustId) {
    	switch(selectedCrustId) {
		case (R.id.btn_thin_crust):
			this.crust = THIN;
			break;
		case (R.id.btn_thick_crust):
			this.crust = THICK;
			break;
		case (R.id.btn_deep_crust):
			this.crust = DEEP;
			break;
		case (R.id.btn_stuffed_crust):
			this.crust = STUFFED;
			break;
		}
    }
    
    @Override
    public void finish() {
    	super.finish();
    	db.close();
    }
}
