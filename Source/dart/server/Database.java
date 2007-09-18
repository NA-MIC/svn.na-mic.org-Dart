package dart.server;

import java.sql.Connection;
import java.sql.*;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import java.util.*;
import java.lang.*;


/**
 * Database is a utility case to manage the JDBC connection.
 * @author Dan Blezek, Jim Miller
 * @version $Revision$
 */
public class Database {
  static Logger logger = Logger.getLogger ( Database.class );   

  PoolingDataSource dataSource = null;
  Container owner = null;
  String driver = "derby";
  String url = null;
  String shutdownURL = null;
  String username = "Default";
  String password = "Default";
  int maxActive = 10;
  int maxIdle = 3;
  int currentActive = 0;
  long timeBetweenEvictionRunsMillis = -1;
  long minEvictableIdleTimeMillis = -1;

  Connection connection = null;
  GenericObjectPool connectionPool = null;

  Map<Connection,Object> connectionInfo = Collections.synchronizedMap ( new HashMap<Connection,Object> () );

  /**
   * Constructor, does nothing
   */
  public Database() {
    // logger.info ( "Creating Database" );
  }

  /**
   * Set the database driver class.
   * The set method is normally called by Digestor to configure the
   * Database class before it's started.  When the Database is started,
   * a <code>Class.forName ( driverclass )</code> is called to
   * load the driver. 
   * @param driverclass Name of the JDBC driver.
   */
  public void setDriver ( String driverclass ) { driver = driverclass; }

  /**
   * Set the database URL.
   * The set method is normally called by Digestor to configure the
   * Database class before it's started.  The URL specifies how to
   * connect to the SQL databbase.
   * @param connectionurl Connection URL.
   */
  public void setURL ( String connectionurl ) { url = connectionurl; }

  /**
   * Set the database shutdown URL.
   * The set method is normally called by Digestor to configure the
   * Database class before it's started.  If the database requires
   * shutdown, this URL used.  The ShutdownURL is used by Derby to
   * gracefully manage DB shutdown.  If not provide, no shutdown 
   * connection is done.
   * @param u Shutdown URL.
   */
  public void setShutdownURL ( String u ) { shutdownURL = u; }

  /**
   * Set username to connect with.
   * The set method is normally called by Digestor to configure the
   * Database class before it's started.
   * @param name Username.
   */
  public void setUsername ( String name ) { username = name; }

  /**
   * Set the password for DB connection.
   * The set method is normally called by Digestor to configure the
   * Database class before it's started.
   * @param pw Password
   */
  public void setPassword ( String pw ) { password = pw; }

  /**
   * Set the maximum number of concurrent connections.
   * The set method is normally called by Digestor to configure the
   * Database class before it's started.  The Database maintains a pool
   * of connections.  If this max is reached, new connection requests
   * will block until a Connection is returned.
   * @see Database#getConnection
   * @param max Integer string indicating maximum connections
   */
  public void setMaxActive ( String max ) { maxActive = Integer.parseInt ( max ); }
  /**
   * Set the number of idle Connections to maintain in the pool.
   * The set method is normally called by Digestor to configure the
   * Database class before it's started.  The Database maintains a pool
   * of connections.  MaxIdle is the number of connections maintained at
   * the ready by the Database object.
   * @see Database#getConnection
   * @param idle Integer string indicating idle connection
   */
  public void setMaxIdle ( String idle ) { maxIdle = Integer.parseInt ( idle ); }

  /**
   * Set the time (in Millis) between eviction runs on the connection pool.
   * This is called by Digestor to set the time between eviction runs.  If a
   * Connection does not correctly validate itself, it is dropped from the pool.
   * This should clean up connection pool exhaustion.
   * @param time Long string indicationg the time between runs in milliseconds.
   */
  public void setTimeBetweenEvictionRunsMillis ( String time ) { timeBetweenEvictionRunsMillis = Long.parseLong ( time ); }

  /**
   * Set the minimum amount of time a Connection must be in the connection pool before being considered for eviction.
   * This is called by Digestor to set the minimum amount of time a Connection
   * must have been in the pool before being considered for eviction.
   * @param time Long string indicationg the minimum age of a Connection to be considered for eviction in milliseconds.
   */
  public void setMinEvictableIdleTimeMillis ( String time ) { minEvictableIdleTimeMillis = Long.parseLong ( time ); }

  /**
   * Start up the database object
   * The start method starts up the Database.  The object
   * creates a pool of connections using the specified
   * settings.
   * @param p Container of the Database
   */
  public void start ( Container p ) throws Exception {
    owner = p;
    logger.debug ( p.getTitle() + ": Starting database" );

    // Try to create a connection
    try {
      Class.forName ( driver );
      connectionPool = new GenericObjectPool(null);
      connectionPool.setMaxActive ( maxActive );
      connectionPool.setMaxIdle ( maxIdle );
      ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, username, password);
      // The PoolableConnectionFactory registers itself with the connectionPool and is used for new connections.
      PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);

      // Setup the validation query
      poolableConnectionFactory.setValidationQuery ( p.getValidationQuery() );

      /* Have the pool test connections to make sure they are ok */
      connectionPool.setTestOnReturn ( true );

      dataSource = new PoolingDataSource(connectionPool);
      
      
      /* eviction of dead connections */
      connectionPool.setTimeBetweenEvictionRunsMillis ( timeBetweenEvictionRunsMillis );
      connectionPool.setMinEvictableIdleTimeMillis ( minEvictableIdleTimeMillis );
      if ( timeBetweenEvictionRunsMillis > 0 && minEvictableIdleTimeMillis > 0 ) {
        connectionPool.setTestWhileIdle ( true );
      }

    } catch ( Exception e ) {
      logger.error ( owner.getTitle() + ": Failed to connect to database via: " + url + " driver: " + driver, e );
      throw e;
    }
  }

  /**
   * Shutdown the Database connection
   * If the ShutdownURL is not null, it is connected to.
   * In the case of Derby, this is required for clean
   * shutdown.
   */
  public void shutdown() throws Exception {
    if ( shutdownURL != null ) {
      try {
        DriverManager.getConnection ( shutdownURL );
      } catch ( Exception e ) {
        logger.info ( owner.getTitle() + ": Caught exception, if this is a derby database, this is normal" );
      }
    }
  }

  /**
   * Get a JDBC Connection object
   * The Database maintains a pool
   * of connections.  If this MaxActive is reached, new connection requests
   * will block until a Connection is returned.  MaxIdle connections are
   * available at any given time.
   * @see Database#setMaxIdle
   * @see Database#setMaxActive
   * @see java.sql.Connection
   * @return JDBC Connection object
   */
  public Connection getConnection() {
    synchronized ( this ) {
      try {
        if ( ( connectionPool.getMaxActive() - connectionPool.getNumActive() ) < 2 ) {
          logger.warn ( owner.getTitle() 
                        + ": getConnection Connection pool nearly exhausted  " 
                        + connectionPool.getNumActive() + " of " 
                        + connectionPool.getMaxActive() 
                        + " currently active " );
        }
        
        logger.debug ( owner.getTitle() + ": getConnection " + connectionPool.getNumActive() + " active " + connectionPool.getNumIdle() + " idle " );
        Connection connection = dataSource.getConnection();
        logger.debug ( owner.getTitle() + ": got connection" );

        
        Throwable t;
        try {
          throw new Throwable();
        } catch ( Throwable tt ) {
          t = tt;
        }
        connectionInfo.put ( connection, t );
        currentActive++;
        if ( currentActive != connectionPool.getNumActive() && false ) {
          // logger.debug ( "getConnection: Dart's count: " + currentActive + " Pool count: " + connectionPool.getNumActive() );
          logger.error ( "getConnection: Dart's count: " + currentActive + " Pool count: " + connectionPool.getNumActive() + "\n" + this.toString() );
        }
        
        return connection;
      } catch ( Exception e ) {
        logger.error ( owner.getTitle() + ": Failed to get connection", e );
      }
    }
    return null;
  }

  public void closeConnection ( Connection c ) throws Exception {
    synchronized ( this ) {
      Throwable t = null;
      /*
      if ( !connectionInfo.containsKey ( c ) ) {
        logger.error ( "Do not have connection info for connection!!!" );
      } else {
        t = (Throwable) connectionInfo.get ( c );
        connectionInfo.remove ( c );
      }
      */
      try {
        c.close();
      } catch ( SQLException sqle ) {
        // got a problem, mark this guy as invalid and re-throw
        logger.error ( "Something was wrong with this connection, marking it invalid.", sqle );
        connectionPool.invalidateObject ( c );
        throw sqle;
      } finally {
        currentActive--;
        if ( currentActive != connectionPool.getNumActive() && false ) {
          logger.error ( "closeConnection: Dart's count: " + currentActive + " Pool count: " + connectionPool.getNumActive() + "\n" + this.toString() );
        }
      }
    }
  }

  /**
   * Print a brief summary of the Database
   * @return String representation of this object
   */
  public String toString () {
    synchronized ( this ) {
      StringBuffer buffer = new StringBuffer();
      if ( connectionPool != null ) {
        buffer.append ( "Database: " + connectionPool.getNumActive() + " active " + connectionPool.getNumIdle() + " idle Dart's Count: " + currentActive + "\n" );
      }
      Iterator it = connectionInfo.keySet().iterator();
      while ( it.hasNext () ) {
        Object o = it.next();
        
        Throwable t = (Throwable) connectionInfo.get ( o );
        if ( t == null ) { continue; }
        StackTraceElement[] stacks = t.getStackTrace();
        buffer.append ( "Got connection: " + o + "\n" );
        
        if ( stacks != null ) {
          for ( int l = 2; l < 5; l++ ) {
            buffer.append ( "\t" + stacks[l].toString() + "\n");
          }
          buffer.append ( "\n" );
        }
      }
      return buffer.toString();
    }
  }
}

/*
 * $Log:$
 */

