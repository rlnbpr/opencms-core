// TODO: Quelltext kommentieren :o((
package test;

import java.sql.*;
import java.util.*;

import com.opencms.core.*;

/**
 * This class is used to create an pool of prepared statements.
 * 
 * @author u.roland
 */
public class CmsPreparedStatementPool {
	
	/*
	 * maximum of connections to the database
	 */
	private int m_maxConn = 10;

	/*
	 * the driver for the database
	 */
	private String m_driver = null;
	
	/*
	 * connection to the database
	 */
	private String m_connectString = null;
	
	/*
	 * store the PreparedStatements with an connection
	 */
	private Hashtable m_prepStatements;
	
	/*
	 * store the sql statements
	 */
	private Hashtable m_prepStatementsCache = null;
	
	/*
	 * store the connections
	 */
	private Vector m_connections;
	
	/*
	 * counter
	 */
	private int count = 0;
	
	/**
	 * Init the pool with a specified number of connections.
	 * 
     * @param driver - driver for the database
     * @param url - the URL of the database to which to connect
     * @param maxConn - maximum connections
	 */
	public CmsPreparedStatementPool(String driver, String connectString, int maxConn) throws CmsException {
		this.m_driver = driver;
		this.m_connectString = connectString;
		this.m_maxConn = maxConn;
		
		// register the driver for the database
		try {
			Class.forName(m_driver);
		}
		catch (ClassNotFoundException e) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, e);
		}
		
		// init the hashtables and vector(s)
		m_prepStatements = new Hashtable();
		m_prepStatementsCache  = new Hashtable();
		m_connections = new Vector(m_maxConn);
		
		// init connections
		for (int i = 0; i < m_maxConn; i++) {
			Connection conn = null;
			
			try {
				conn = DriverManager.getConnection(m_connectString);
				m_connections.addElement(conn);
			}
			catch (SQLException e) {
				throw new CmsException(CmsException.C_SQL_ERROR, e);
			}
		}
	}
	
	/**
	 * Init the PreparedStatement on all connections and store the sql statement in an hashtable.
	 * 
	 * @param key - the hashtable key
	 * @param sql - a SQL statement that may contain one or more '?' IN parameter placeholders
	 */
	public void initPreparedStatement(String key, String sql) throws CmsException {
		Vector temp = new Vector(m_maxConn);
		Connection conn = null;

		m_prepStatementsCache.put(key, sql);
		
		for (int i = 0; i < m_maxConn; i++) {
			conn = (Connection) m_connections.elementAt(i);
			
			try {
				PreparedStatement pstmt = conn.prepareStatement(sql);
				temp.addElement(pstmt);
			}
			catch (SQLException e) {
				throw new CmsException(CmsException.C_SQL_ERROR, e);
			}
		}
		
		m_prepStatements.put(key, temp);
	}
	
	/**
	 * Gets a PreparedStatement object and remove it from the list of available statements.
	 * 
	 * @param key - the hashtable key
	 * @return a prepared statement matching the key
	 */
	public PreparedStatement getPreparedStatement(String key) throws CmsException {
		PreparedStatement pstmt = null;
		int num;
		Vector temp = (Vector) m_prepStatements.get(key);
		
		synchronized (temp) {
			if (temp.size() > 0) {
				pstmt =(PreparedStatement) temp.firstElement();
				temp.removeElementAt(0);
			}
			else {
				String sql =(String) m_prepStatementsCache.get(key);

				if (count > (m_maxConn - 1)) {
					count = 0;
				}
				Connection conn = (Connection) m_connections.elementAt(count);
				count++;
				
				try {
					pstmt = conn.prepareStatement(sql);
				}
				catch (SQLException e) {
					throw new CmsException(CmsException.C_SQL_ERROR, e);
				}
			}
			temp.notify();
		}
		
		return pstmt;
	}
	
	/**
	 * Add the given statement to the list of available statements.
	 * 
	 * @param key - the hashtable key
	 * @param pstmt - the statement
	 */
	public void putPreparedStatement(String key, PreparedStatement pstmt) {
		/* TODO: 
		 * 
		 * Bei dem Versuch, ein Statement in den Vector zu schreiben, muss
		 * kontrolliert werden, ob das Statement ein neu geschaffenes Statement ist.
		 * Wenn ja, dann Statement verwerfen, ansonsten eintragen ...
		 */
		Vector temp = (Vector) m_prepStatements.get(key);
		
		synchronized (temp) {
			temp.addElement(pstmt);
			temp.notify();
		}
	}
	
	/**
	 * Returns a vector with all connections.
	 * 
	 * @return a vector with all connections
	 */
	public Vector getAllConnections() {
		
		return m_connections;
	}
	
	/**
	 * Returns all statements matching the key.
	 * 
	 * @param key - the hashtable key
	 * @return all prepared statements matching the key
	 */
	public Vector getAllPreparedStatement(String key) {
		Vector temp = (Vector) m_prepStatements.get(key);
		
		return temp;
	}
}
