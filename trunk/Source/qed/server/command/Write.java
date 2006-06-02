package qed.server.command;

import qed.server.*;
import dart.server.command.*;
import dart.server.*;
import qed.server.wrap.*;
import java.io.File;
import java.io.*;
import java.util.Properties;
import java.util.zip.*;
import net.sourceforge.jaxor.*;
import java.sql.*;

import org.apache.log4j.Logger;

/*
 * Class to handle submission commands
 * @author Dan Blezek
 */
public class Write implements Command {
  QED qed = null;
  static Logger logger = Logger.getLogger ( Write.class );   
  Properties properties;

  public Write ( Container p, Properties prop ) throws Exception {
    if ( p instanceof QED ) {
      qed = (QED)p;
      properties = prop;
      logger.debug ( qed.getTitle() + ": Created Submit Command" );
    } else {
      throw new Exception ( "Submit can only work on Qeds" );
    }
  }

  public int getPopulation ( String name ) {
    // Create a new population if it doesn't exist
    // Return the key
    Connection connection = qed.getConnection();
    JaxorContextImpl session = new JaxorContextImpl ( connection );
    PopulationEntity population = null;
    PopulationFinderBase populationFinder = new PopulationFinderBase ( session );
    try {
      QueryParams q = new QueryParams();
      q.add ( name );
      population = populationFinder.selectByName ( name );
    } catch ( EntityNotFoundException notfound ) {
      logger.debug ( "Creating new Population instance: " + name );
      population = populationFinder.newInstance();
      population.setName ( name );
      session.commit();
      population = populationFinder.selectByName ( name );
    }
    return population.getPopulationId().intValue();
  }
      
}
