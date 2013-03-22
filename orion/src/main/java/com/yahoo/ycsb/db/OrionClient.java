/**
 * Copyright (c) 2010 Yahoo! Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */

package com.yahoo.ycsb.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import com.tomting.orion.ThrfL2cl;
import com.tomting.orion.ThrfL2cv;
import com.tomting.orion.ThrfL2ks;
import com.tomting.orion.ThrfL2qb;
import com.tomting.orion.ThrfL2qr;
import com.tomting.orion.ThrfL2st;
import com.tomting.orion.ThrfLkey;
import com.tomting.orion.iEcolumntype;
import com.tomting.orion.iEquerytype;
import com.tomting.orion.iEstatetype;
import com.tomting.thor.orion.connection.OrionConnection;
import com.yahoo.ycsb.*;


public class OrionClient extends DB {
	
	private final static Logger LOGGER = Logger.getLogger(OrionClient.class.getName());	
	
	public static final int Ok = 0;
	public static final int Error = -1;	
	private static final Pattern URL = Pattern.compile("orion:([\\w|.]+):(\\d+):(\\w+)");	
	private OrionConnection orionConnection = null;
	
	
	public void init() throws DBException {
	    String hosts = getProperties().getProperty("hosts");
	    if (hosts == null) {
	      throw new DBException("Required property \"hosts\" missing for OrionClient");
	    }	
	    
        Matcher hostMatcher = URL.matcher(hosts);
		if (!hostMatcher.matches()) 
			throw new UnsupportedOperationException("orion:host:port:namespace");
		
		String host = hostMatcher.group(1);
        int port = Integer.parseInt (hostMatcher.group(2));
        String namespace = hostMatcher.group(3);	
        LOGGER.info(hosts);
        try {
			orionConnection = new OrionConnection (host, port, namespace);
		} catch (Exception e) {
			throw new DBException("Cant connect to Orion");
		}	    	    
	}
	
	public void cleanup() throws DBException {
		
		try {
			orionConnection.close();
		} catch (TException e) {
		}
	}	

	/**
	 * insert a key
	 */
	@Override
	public int insert(String table, String key, HashMap<String, ByteIterator> values) {
		boolean result = false;
		
		if (key == null || values == null) {
			LOGGER.fatal("null input");
			return Error;
		}
		ThrfL2st statement = OrionConnection.getStatement();
		statement.cVkey.sVmain = key;
		statement.cVkey.iVstate = iEstatetype.UPSERT;
		statement.cVmutable.sVtable = table.toUpperCase();
        
		Iterator<Entry<String, ByteIterator>> it = values.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, ByteIterator> pairs = (Map.Entry<String, ByteIterator>)it.next();	
			ThrfL2cl cVcolumn = new ThrfL2cl ();
	        cVcolumn.cVvalue = new ThrfL2cv ();
	        cVcolumn.cVvalue.iVtype = iEcolumntype.STRINGTYPE;
	        cVcolumn.cVvalue.sVvalue = pairs.getValue().toString();
	        cVcolumn.sVcolumn = pairs.getKey(); 	        
	        statement.cVcolumns.add(cVcolumn);  	        
	        it.remove(); 
	    }		
		 
		try {
        	result = orionConnection.runStatement(statement);        	
		} catch (TException e) {
		}  
        return result ? Ok : Error;	
	}	
	
	/**
	 * single key read
	 */
	@Override
	public int read(String table, String key, Set<String> fields, HashMap<String, ByteIterator> result) {
		boolean returnedKeyslices = false;
		ThrfL2ks keyslice;
	
		if (key == null) {
			LOGGER.fatal("null input");
			return Error;
		}			
		ThrfL2qr query = OrionConnection.getQuery();
		query.cVmutable.sVtable = table.toUpperCase();	
		query.iVquery = iEquerytype.EXACTQUERY;
		query.cVkey_start = new ThrfLkey ();					
		query.cVkey_start.sVmain = key;		
		if (fields != null)
			for (String s : fields) {
		        ThrfL2cl cVcolumn = new ThrfL2cl ();
		        cVcolumn.sVcolumn = s;
		        query.cVselect.add(cVcolumn);  		
			}
        try {
        	ThrfL2qb queryReturn = orionConnection.runQuery (query);
        	returnedKeyslices = queryReturn.bVreturn;
        	if (returnedKeyslices) {
        		keyslice = queryReturn.cKeyslices.get(0);
        		for (ThrfL2cl column : keyslice.cVcolumns) {
        			result.put(column.sVcolumn, new StringByteIterator(column.cVvalue.sVvalue));
        		}
        	}
		} catch (TException e){}        
        return returnedKeyslices ? Ok : Error;	
	}
	
	/**
	 * update
	 */
	@Override	
	public int update(String table, String key, HashMap<String, ByteIterator> values) {
	    
		return insert(table, key, values);
	}	
	
	@Override
	public int scan(String table, String startkey, int recordcount, Set<String> fields,
			Vector<HashMap<String, ByteIterator>> result) {
		boolean returnedKeyslices = false;
	
		if (startkey == null) {
			LOGGER.fatal("null input");
			return Error;
		}			
		ThrfL2qr query = OrionConnection.getQuery();
		query.cVmutable.sVtable = table.toUpperCase();	
		query.iVquery = iEquerytype.RANGEQUERY;
		query.cVkey_start = new ThrfLkey ();					
		query.cVkey_start.sVmain = startkey;	
		query.iVcount = recordcount;
		if (fields != null)
			for (String s : fields) {
		        ThrfL2cl cVcolumn = new ThrfL2cl ();
		        cVcolumn.sVcolumn = s;
		        query.cVselect.add(cVcolumn);  		
			}
        try {
        	ThrfL2qb queryReturn = orionConnection.runQuery (query);
        	returnedKeyslices = queryReturn.bVreturn;
        	if (returnedKeyslices) {
        		for (ThrfL2ks keyslice : queryReturn.cKeyslices) {
        			HashMap<String, ByteIterator> row = new HashMap<String, ByteIterator> ();
	        		for (ThrfL2cl column : keyslice.cVcolumns) 
	        			row .put(column.sVcolumn, new StringByteIterator(column.cVvalue.sVvalue));
	        		result.add(row);
        		}
        	}
		} catch (TException e){}        
        return returnedKeyslices ? Ok : Error;	
	}

	/**
	 * delete a key
	 */
	@Override
	public int delete(String table, String key) {
		boolean result = false;
		
		if (key == null) {
			LOGGER.fatal("null input");
			return Error;
		}
		ThrfL2st statement = OrionConnection.getStatement();
		statement.cVkey.sVmain = key;
		statement.cVkey.iVstate = iEstatetype.DELTMB;
		statement.cVmutable.sVtable = table.toUpperCase();
        		 
		try {
        	result = orionConnection.runStatement(statement);        	
		} catch (TException e) {
		}  
        return result ? Ok : Error;	
	}		
	
	/**
	 * test client
	 * @param args
	 */
	public static void main(String[] args) {	
		OrionClient cli = new OrionClient ();
		Properties props = new Properties();
		props.setProperty("hosts", "orion:localhost:9001:DEFAULT");
	    cli.setProperties(props);

	    try {
	      cli.init();
	    } catch (Exception e) {
	      e.printStackTrace();
	      System.exit(0);
	    }
		
	    HashMap<String, ByteIterator> vals = new HashMap<String, ByteIterator>();
	    vals.put("age", new StringByteIterator("57"));
	    vals.put("middlename", new StringByteIterator("bradley"));
	    vals.put("favoritecolor", new StringByteIterator("blue"));
	    int res = cli.insert("USERTABLE", "BrianFrankCooper", vals);
	    System.out.println("Result of insert: " + res);	
	 	    
	    HashMap<String, ByteIterator> result = new HashMap<String, ByteIterator>();
	    HashSet<String> fields = new HashSet<String>();
	    fields.add("middlename");
	    fields.add("age");
	    fields.add("favoritecolor");
	    res = cli.read("usertable", "BrianFrankCooper", fields, result);
	    System.out.println("Result of read: " + res);
	    for (String s : result.keySet()) {
	      System.out.println("[" + s + "]=[" + result.get(s) + "]");
	    }	   	    
	    
	    res = cli.delete("usertable", "BrianFrankCooper");
	    System.out.println("Result of delete: " + res);	  		    
	    
	    result = new HashMap<String, ByteIterator>();
	    fields = new HashSet<String>();
	    fields.add("middlename");
	    fields.add("age");
	    fields.add("favoritecolor");
	    res = cli.read("usertable", "BrianFrankCooper", fields, result);
	    System.out.println("Result of read: " + res);
	    for (String s : result.keySet()) {
	      System.out.println("[" + s + "]=[" + result.get(s) + "]");
	    }	   	    
	    	    
	    try {
	    	cli.cleanup();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	System.exit(0);
	    }	    
	}
}
