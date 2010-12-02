package farkosizPizza.OrderCenter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;


/** 
 * The major bulk of this code is from the tutorial by Juan-Manuel Fluxà from 
 * ReignDesign posted 3 March 2009 at:
 * http://www.reigndesign.com/blog/using-your-own-sqlite-database-in-android-applications/
 * 
 * This DatabaseHelper will allow access to the application's database. If the 
 * database doesn't exist, then it will create a new application db and migrate
 * the included database into the application db.
 * 
 * ToDo: create accessors and get content from the database
 * research option: return cursors by doing "return orderDB.query(...)" so it'd
 * be easy to create adapters for the views.
 * 
 * @author Joshua Kovach
 * @version 02 October 2010
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	private static String DB_PATH = "/data/data/farkosizPizza.OrderCenter/databases/";
	private static String DB_NAME = "db_farkosizPizza.db";
	
	//table names
	private static String T_ORDER = "pizza_order";
	private static String T_PIZZA = "pizza_item";
	private static String T_TOPPING = "topping_item";
	private static String T_ORDER_ITEM = "order_contents";
	private static String T_TOPPING_PLACEMENT = "topping_coverage";
	
	//pizza attributes
	public static int SIZE = 0;
	public static int CRUST = 1;
	
	private SQLiteDatabase orderDB;
	private final Context orderContext;
	
	/**
	 * Constructor
	 * Takes and keeps a reference of teh passed context in order to access
	 * the application assets and resources
	 * @param context
	 */
	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, 1);
		this.orderContext = context;
	}
	
	/**
	 * Creates an empty database on the system and rewrites it with my database
	 */
	public void createDatabase() throws IOException {
		boolean dbExist = checkDatabase();
		
		if (dbExist) {
			//do nothing - database already exists
		}
		else {
			//by calling this method, an empty database will be created into the
			//default system path of your application, and we will be able to 
			//overwrite that database with the orderDB
			this.getReadableDatabase();
			
			try {
				copyDatabase();
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
		}
	}
	
	/**
	 * Check if the database already exists to avoid re-copying the file each 
	 * time you open the application
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDatabase() {
		SQLiteDatabase checkDB = null;
		
		try {
			String dbPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			//database doesn't exist yet
		}
		
		if (checkDB != null) {
			checkDB.close();
		}
		
		return checkDB != null ? true : false;
	}
	
	/**
	 * Copies order database from local assets folder to the just-created empty
	 * database in the application system folder, from which it can be accessed 
	 * and handled. This is done by transfering bytestream.
	 */
	private void copyDatabase() throws IOException { 
		//open local db as the input stream
		InputStream dbInput = orderContext.getAssets().open(DB_NAME);
		
		//path to the just-created empty db
		String outFileName = DB_PATH + DB_NAME;
		
		//open the empty db as the output stream
		OutputStream dbOutput = new FileOutputStream(outFileName);
		
		//transfer bytes from th einput file to the output file
		byte[] buffer = new byte[1024];
		int length;
		while((length = dbInput.read(buffer)) > 0) {
			dbOutput.write(buffer, 0, length);
		}
		
		//close the streams
		dbOutput.flush();
		dbOutput.close();
		dbInput.close();
		
	}
	
	public void openDatabase() throws SQLException {
		//open the database
		String dbPath = DB_PATH + DB_NAME;
		orderDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
		
	}
	
	@Override
	public synchronized void close() {
		if (orderDB != null)
			orderDB.close();
		
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}
	
	
	/**
	 * Get the list of toppings for a selected pizza in a specific location.
	 * @param pizzaId
	 * @param t_loc None = 0, Right = 1, Left = 2, Whole = 3
	 * @return the list of integer topping ids for selected location
	 */
	public int[] getPizzaToppingsForPosition(int pizzaId, int t_loc) {
		String[] columns = {"t_id"};
		String table = T_TOPPING_PLACEMENT;
		String selection = "p_id = " + pizzaId + " AND location = " + t_loc;
		String[] selectionArgs = null;
		String groupBy = null;
		String having = null;
		String orderBy = null;
		
		//special case, topping list, independent of pizza or location
		if (pizzaId == -1 || t_loc == -1) {
			table = T_TOPPING;
			columns[0] = "_id"; //change from "t_id" to "_id"
			selection = null;
		}
		
		Cursor toppings = orderDB.query(table, columns, selection , 
								selectionArgs, groupBy, having, orderBy);
		
		if (toppings.moveToFirst()) {	//move the cursor to the first entry
		
			ArrayList<Integer> toppingIds = new ArrayList<Integer>();
			while (!toppings.isAfterLast()) {
				toppingIds.add(toppings.getInt(0));
				toppings.moveToNext();
			}
			
			int length = toppingIds.size();
			int[] topIds = new int[length];
			for (int i = 0; i < length; i++) {
				topIds[i] = toppingIds.get(i);
			}
			
			return topIds;
		}
		else return new int[0];
	}
	
	public int[] getToppingList() {
		return getPizzaToppingsForPosition(-1, -1);
	}
	
	public ArrayList<Topping> getToppingAList() {
		int[] list = getToppingList();
		ArrayList<Topping> toppings = new ArrayList<Topping>();
		for (int i = 0; i < list.length; i++) {
			toppings.add(new Topping(this, i));
		}
		
		return toppings;
	}
	
	/** 
	 * Converts the topping_id to the descripting String name
	 * @param topping_id
	 * @return String topping_name
	 */
	public String getToppingName(int topping_id) {
		String table = T_TOPPING;
		String[] columns = {"name"};
		String selection = "_id = " + topping_id;
		String[] selectionArgs = null;
		String groupBy = null;
		String having = null;
		String orderBy = null;
		
		Cursor cursor = orderDB.query(table, columns, selection, 
				selectionArgs, groupBy, having, orderBy);
		cursor.moveToFirst();	//move the cursor to the first entry
		return cursor.getString(0);
	}
	
	/**
	 * Get the value of a pizza's attributes based on pizza id and attribute.
	 * @param pizzaId
	 * @param attribute - Size = 0, Crust = 1
	 * @return integer value of the selected attribute
	 */
	public int getPizzaAttribute(int pizzaId, int attribute) {

		
		String table = T_PIZZA;
		String[] columns = {"size", "crust"};
		String selection = "_id = " + pizzaId;
		String[] selectionArgs = null;
		String groupBy = null;
		String having = null;
		String orderBy = null;
		
		Cursor attr = orderDB.query(table, columns, selection, selectionArgs, 
									 groupBy, having, orderBy);
		
		attr.moveToFirst();	//move the cursor to the first entry
		
		return attr.getInt(attribute);
	}
	
	
	/**
	 * Get the list of pizzas for the selected order. Pass -1 for all pizzas.
	 * regardless of order.
	 * @param orderId
	 * @return the list of integer pizza ids
	 */
	public int[] getPizzasForOrder(int orderId) {
		String table = T_ORDER_ITEM;
		String[] columns = {"p_id"};
		String selection = "o_id = " + orderId;
		String[] selectionArgs = null;
		String groupBy = null;
		String having = null;
		String orderBy = null;
		
		if (orderId == -1) selection = null ;
		
		Cursor order_list = orderDB.query(table, columns, selection, selectionArgs, 
									 groupBy, having, orderBy);
		
		if(order_list.moveToFirst()) {	//move the cursor to the first entry
		
			ArrayList<Integer> pizza_list = new ArrayList<Integer>();
			while (!order_list.isAfterLast()) {
				pizza_list.add(order_list.getInt(0));
				order_list.moveToNext();
			}
			
			int length = pizza_list.size();
			int[] pizzas = new int[length];
			for (int i = 0; i < length; i++) {
				pizzas[i] = pizza_list.get(i);
			}
			
			return pizzas;
		}
		else return new int[0];
	}
	
	/**
	 * Get the quantity of the selected pizza for the selected order.
	 * @param orderId
	 * @param pizzaId
	 * @return integer number of pizzas of this type for this order
	 */
	public int getPizzaQty(int orderId, int pizzaId) {
		String table = T_ORDER_ITEM;
		String[] columns = {"qty"};
		String selection = "o_id = " + orderId + " AND p_id = " + pizzaId;
		String[] selectionArgs = null;
		String groupBy = null;
		String having = null;
		String orderBy = null;
		
		Cursor attr = orderDB.query(table, columns, selection, selectionArgs, 
									 groupBy, having, orderBy);
		
		attr.moveToFirst();	//move the cursor to the first entry
		
		return attr.getInt(0);
	}
	
	/**
	 * Get the user name for the order.
	 * @param order_id
	 * @return String user name
	 */
	public String getUserName(int order_id) {
		String table = T_ORDER;;
		String[] columns = {"user_name"};
		String selection = "_id = " + order_id;
		String[] selectionArgs = null;
		String groupBy = null;
		String having = null;
		String orderBy = null;
		
		Cursor cursor =  orderDB.query(table, columns, selection, selectionArgs, 
				groupBy, having, orderBy);
		cursor.moveToFirst();	//move the cursor to the first entry
		return cursor.getString(0);
	}
	
	/**
	 * Creates a new order in the pizza_order table and returns the id for 
	 * later use.
	 * @return int order_id of the new order
	 */
	public int createNewOrder() {
		String table = T_ORDER;		
		String nullColumnHack = "user_name";
		ContentValues values = null;
		
		return (int)orderDB.insert(table, nullColumnHack, values);
	}

	/**
	 * Creates a new pizza item:
	 * 	first we create a new pizza item with a size and crust, and establish a
	 * 	pizza_id for the new pizza.  Then we tell the topping_coverage table
	 * 	which toppings are going where for this pizza.  Finally, we associate 
	 * 	the pizza with the current order, and tell the order how many of this 
	 * 	pizza instance will be ordered.
	 * @param order_id	int id of the current order
	 * @param size		int size of the pizza
	 * @param crust		int crust of the pizza
	 * @param qty		int number of instances of this pizza to order
	 * @param toppings	int[] of toppings, where the index is the topping_id and
	 * 						the value is its location on the pizza
	 * @return pizza_id int the id of the new pizza we just created
	 */
	public int createNewPizza(int order_id, int size, int crust, int qty, int[] toppings) {
		String table = T_PIZZA;
		String nullColumnHack = null;
		ContentValues values = new ContentValues();
		values.put("size", size);
		values.put("crust", crust);
		
		int pizza_id = (int) orderDB.insert(table, nullColumnHack, values);
		//associate the topping/location sets for this pizza
		//For each topping item, this statement will create a new content set
		//to insert, insert it, then clear the content set for the next topping.
		//Of course, this only happens if the topping placement > 0, since a zero
		//value will mean these toppings are not included on this pizza.
		String topping_table = T_TOPPING_PLACEMENT;
		ContentValues topping_values = new ContentValues();
		for (int i = 0; i < toppings.length; i++) {
			if (toppings[i] != 0) {
				topping_values.put("t_id", i+1);	//topping ids start at 1
				topping_values.put("p_id", pizza_id);
				topping_values.put("location", toppings[i]);
				orderDB.insert(topping_table, nullColumnHack, topping_values);
			}
			topping_values.clear();
		}
		
		//add the pizza to the current order with the quantity
		String order_list_table = T_ORDER_ITEM;
		ContentValues order_values = new ContentValues();
		order_values.put("o_id", order_id);
		order_values.put("p_id", pizza_id);
		order_values.put("qty", qty);
		orderDB.insert(order_list_table, nullColumnHack, order_values);
		
		return pizza_id;
	}
	
	/**
	 * This will update the value of the order with the user's name.  This will
	 * likely be one of the last things the user will do, but every time we 
	 * branch from the order display page, we'll call this to update the db with
	 * this information so they won't have to keep inserting it.
	 * @param order_id
	 * @param user_name
	 * @return int the number of rows affected, should always be 1
	 */
	public int setUserName(int order_id, String user_name) {
		String table = T_ORDER;
		ContentValues values = new ContentValues();
		String whereClause = "_id = " + order_id; 
		String[] whereArgs = null;
		
		values.put("user_name", user_name);
		
		return orderDB.update(table, values, whereClause, whereArgs);
		
	}
	
	/**
	 * Deletes an order. Does not necessarily remove the pizzas on the order, 
	 * but it does remove pizza association with a the order removed.
	 * 
	 * todo: figure out a way to remove pizzas associated <i>only</i> with the
	 * given order.
	 * 
	 * @param order_id
	 * @return int number of rows affected across all tables.
	 */
	public int deleteOrder(int order_id) {
		String[] whereArgs = null;
		
		//int[] pizzas = this.getPizzasForOrder(order_id);
		
		
		int affRows = 0;
		
		affRows += orderDB.delete(T_ORDER_ITEM, "o_id = " + order_id, whereArgs);
		affRows += orderDB.delete(T_ORDER, "_id = " + order_id, whereArgs);
		
		/* this part will be used if we only do a "recent orders" screen
		//find and delete pizzas without parent orders
		for (int p : pizzas) {
			String[] columns = {"p_id"};
			Cursor c = orderDB.query(T_ORDER_ITEM, columns, "p_id = "
					+ p, null, null, null, null);
			if (c.moveToFirst() == false) {	//the pizza is not associated with an order
				affRows += orderDB.delete(T_PIZZA, "_id = " + p, null);
			}
			
		}
		*/
		
		return affRows;
	}
	
	/**
	 * Deletes a given pizza.  This will remove all references to this pizza in 
	 * the order_items, topping_placement, and pizza_item tables. 
	 * @param pizza_id
	 * @return int total number of rows affected (in all tables)
	 */
	public int deletePizza(int pizza_id) {

		String[] whereArgs = null;
		int affRows = 0;
		affRows += orderDB.delete(T_PIZZA, "_id = " + pizza_id, whereArgs);
		affRows += orderDB.delete(T_ORDER_ITEM, "p_id = " + pizza_id, whereArgs);
		affRows += orderDB.delete(T_TOPPING_PLACEMENT, "p_id = " + pizza_id, whereArgs);
		
		return affRows;
	}

}


